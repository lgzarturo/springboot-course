---
id: sqlalchemy
name: sqlalchemy
description: >
  SQLAlchemy 2.x patterns for async FastAPI projects: models, sessions, queries,
  bulk operations, transactions, and Alembic migrations.
  Trigger: When writing models, queries, bulk operations, or DB-touching service code.

user-invokable: true
license: MIT
metadata:
  author: lgzarturo
  category: database

compatibility:
  tools: [claude, codex, gemini, agy, opencode]
  stacks:
    languages: [python]
    frameworks: [sqlalchemy, alembic, asyncpg, fastapi]

risk:
  level: medium
  can_execute_shell: true
  can_modify_files: true
  requires_network: false

inputs:
  - model files (models/*.py)
  - service files with DB queries (services/*.py)
  - migration files (alembic/versions/*.py)
  - database setup (db.py)

outputs:
  - DeclarativeBase model definitions (SQLAlchemy 2.x style)
  - AsyncSession setup and get_db dependency
  - select() query patterns with scalars, scalar_one, scalar_one_or_none
  - bulk insert/update via execute()
  - transaction-safe async service methods
  - Alembic async env.py configuration
  - anti-pattern corrections (lazy loading, N+1, commit in service)

quality:
  reviewed_by: codeconductor-core
  version: 0.1.0
paths:
  - "**/*.py"
---
## When to Use

- Defining new SQLAlchemy models
- Writing query logic in services
- Implementing bulk operations
- Setting up or modifying Alembic migrations
- Debugging N+1 queries or session issues

## Engine and Session Setup

```python
# db.py
from sqlalchemy.ext.asyncio import (
    AsyncSession,
    async_sessionmaker,
    create_async_engine,
)
from src.config import settings

engine = create_async_engine(
    settings.database_url,  # postgresql+asyncpg://user:pass@host/db
    echo=settings.debug,
    pool_size=10,
    max_overflow=20,
)

async_session_factory = async_sessionmaker(
    engine,
    class_=AsyncSession,
    expire_on_commit=False,  # required for async — objects usable after commit
)
```

`expire_on_commit=False` is mandatory in async SQLAlchemy. Without it, accessing
attributes after commit triggers lazy load → `MissingGreenlet` error.

## Model Base

```python
# models/base.py
from datetime import datetime
from sqlalchemy import func
from sqlalchemy.orm import DeclarativeBase, Mapped, mapped_column


class Base(DeclarativeBase):
    pass


class TimestampMixin:
    created_at: Mapped[datetime] = mapped_column(
        server_default=func.now(), nullable=False
    )
    updated_at: Mapped[datetime] = mapped_column(
        server_default=func.now(), onupdate=func.now(), nullable=False
    )
```

All models inherit from `Base`. All persistent entities include
`TimestampMixin`.

## Model Definition (SQLAlchemy 2.x style)

```python
# models/product.py
from decimal import Decimal
from sqlalchemy import String, Numeric, ForeignKey, Index
from sqlalchemy.orm import Mapped, mapped_column, relationship
from src.models.base import Base, TimestampMixin


class Product(Base, TimestampMixin):
    __tablename__ = "products"
    __table_args__ = (
        Index("ix_products_is_active_stock", "is_active", "stock"),
    )

    id: Mapped[int] = mapped_column(primary_key=True)
    name: Mapped[str] = mapped_column(String(255), nullable=False)
    slug: Mapped[str] = mapped_column(String(255), unique=True, nullable=False)
    price: Mapped[Decimal] = mapped_column(Numeric(10, 2), nullable=False)
    stock: Mapped[int] = mapped_column(default=0, nullable=False)
    is_active: Mapped[bool] = mapped_column(default=True, nullable=False)

    category_id: Mapped[int | None] = mapped_column(ForeignKey("categories.id"))
    category: Mapped["Category | None"] = relationship(
        back_populates="products", lazy="raise"
    )

    def __repr__(self) -> str:
        return f"<Product id={self.id} name={self.name!r}>"
```

**Rules**:

- Use `Mapped[T]` and `mapped_column()` — never the old `Column()` style
- Always set `lazy="raise"` on relationships — prevents accidental lazy loads
- Always define `__tablename__` explicitly
- Add `Index` in `__table_args__` for composite or frequently filtered columns

## Relationships

```python
# One-to-many: Category → Products
class Category(Base, TimestampMixin):
    __tablename__ = "categories"

    id: Mapped[int] = mapped_column(primary_key=True)
    name: Mapped[str] = mapped_column(String(100), nullable=False)

    products: Mapped[list["Product"]] = relationship(
        back_populates="category", lazy="raise"
    )


# Many-to-many via association table
from sqlalchemy import Table, Column

product_tags = Table(
    "product_tags",
    Base.metadata,
    Column("product_id", ForeignKey("products.id"), primary_key=True),
    Column("tag_id", ForeignKey("tags.id"), primary_key=True),
)


class Product(Base, TimestampMixin):
    ...
    tags: Mapped[list["Tag"]] = relationship(
        secondary=product_tags, back_populates="products", lazy="raise"
    )
```

Always use `back_populates` (explicit) — never `backref` (implicit, hard to
trace). `lazy="raise"` catches N+1 at runtime instead of silently degrading
performance.

## Queries

```python
from sqlalchemy import select, func
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy.orm import selectinload


# Get by PK — fastest for single object
product = await db.get(Product, product_id)

# Select with filter
result = await db.execute(select(Product).where(Product.is_active == True))
products = result.scalars().all()

# Single result — raises if 0 or >1
result = await db.execute(select(Product).where(Product.slug == slug))
product = result.scalar_one()

# Optional single result — returns None if not found
result = await db.execute(select(Product).where(Product.id == product_id))
product = result.scalar_one_or_none()

# With eager load (avoids lazy load with lazy="raise")
result = await db.execute(
    select(Product)
    .where(Product.is_active == True)
    .options(selectinload(Product.category))
    .order_by(Product.name)
)
products = result.scalars().all()

# Count
result = await db.execute(select(func.count()).select_from(Product))
total = result.scalar_one()
```

**Eager loading options**:

| Method         | Use                                           |
| -------------- | --------------------------------------------- |
| `selectinload` | One-to-many, many-to-many — separate IN query |
| `joinedload`   | Many-to-one (FK) — JOIN in same query         |
| `subqueryload` | Large collections — subquery approach         |

Default to `selectinload` for collections, `joinedload` for FK parents.

## Write Operations

### Create

```python
# Single
product = Product(name="Laptop", price=Decimal("999.99"), stock=10)
db.add(product)
await db.flush()        # assigns id without committing
await db.refresh(product)  # reload after flush if needed
return product

# Bulk insert
products = [Product(name=n, price=p) for n, p in data]
db.add_all(products)
await db.flush()
```

### Update

```python
from sqlalchemy import update

# Object-based (for single row with loaded object)
product.stock -= quantity
product.updated_at = datetime.utcnow()
await db.flush()

# Bulk update via execute (no objects loaded — efficient)
await db.execute(
    update(Product)
    .where(Product.category_id == category_id)
    .values(is_active=False)
)
```

### Delete

```python
from sqlalchemy import delete

# Single (object already loaded)
await db.delete(product)

# Bulk delete
await db.execute(delete(Product).where(Product.stock == 0))
```

**Rule**: Never call `session.commit()` in services — commit belongs in
`get_db`. Use `await db.flush()` to flush to DB within a transaction without
committing.

## Transactions

The `get_db` dependency owns the transaction. For nested operations requiring an
explicit savepoint:

```python
from sqlalchemy.ext.asyncio import AsyncSession


async def transfer_stock(
    db: AsyncSession, from_id: int, to_id: int, quantity: int
) -> None:
    async with db.begin_nested():  # SAVEPOINT
        source = await db.get(Product, from_id)
        target = await db.get(Product, to_id)

        if source.stock < quantity:
            raise ValueError("insufficient stock")

        source.stock -= quantity
        target.stock += quantity
        await db.flush()
```

Use `begin_nested()` for savepoints within an existing transaction. Never call
`db.begin()` inside a service — the session is already in a transaction.

## Alembic Setup (Async)

```python
# alembic/env.py
import asyncio
from logging.config import fileConfig
from sqlalchemy import pool
from sqlalchemy.ext.asyncio import async_engine_from_config
from alembic import context
from src.models.base import Base
from src.config import settings

config = context.config
config.set_main_option("sqlalchemy.url", settings.database_url)

if config.config_file_name is not None:
    fileConfig(config.config_file_name)

target_metadata = Base.metadata


def run_migrations_offline() -> None:
    url = config.get_main_option("sqlalchemy.url")
    context.configure(url=url, target_metadata=target_metadata, literal_binds=True)
    with context.begin_transaction():
        context.run_migrations()


def do_run_migrations(connection):
    context.configure(connection=connection, target_metadata=target_metadata)
    with context.begin_transaction():
        context.run_migrations()


async def run_async_migrations() -> None:
    connectable = async_engine_from_config(
        config.get_section(config.config_ini_section, {}),
        prefix="sqlalchemy.",
        poolclass=pool.NullPool,
    )
    async with connectable.connect() as connection:
        await connection.run_sync(do_run_migrations)
    await connectable.dispose()


def run_migrations_online() -> None:
    asyncio.run(run_async_migrations())


if context.is_offline_mode():
    run_migrations_offline()
else:
    run_migrations_online()
```

## Alembic Commands

```bash
# Generate migration from model changes
alembic revision --autogenerate -m "add product slug"

# Apply all pending migrations
alembic upgrade head

# Rollback one migration
alembic downgrade -1

# Show current revision
alembic current

# Show migration history
alembic history --verbose
```

**Always review autogenerated migrations** before applying. Alembic misses:

- `server_default` changes on existing columns
- Index changes inside `__table_args__`
- Column type changes on some dialects

## Anti-Patterns

### Lazy load with lazy="raise" (will crash)

```python
# WRONG — lazy="raise" means this raises MissingGreenlet or InvalidRequestError
products = result.scalars().all()
for p in products:
    print(p.category.name)  # ERROR

# CORRECT — eager load upfront
result = await db.execute(
    select(Product).options(selectinload(Product.category))
)
products = result.scalars().all()
for p in products:
    print(p.category.name)  # OK
```

### Commit in service

```python
# WRONG — service owns the transaction
class ProductService:
    @staticmethod
    async def create(db: AsyncSession, payload: ProductCreate) -> Product:
        product = Product(**payload.model_dump())
        db.add(product)
        await db.commit()  # WRONG — get_db should commit
        return product

# CORRECT — flush only, commit in get_db
class ProductService:
    @staticmethod
    async def create(db: AsyncSession, payload: ProductCreate) -> Product:
        product = Product(**payload.model_dump())
        db.add(product)
        await db.flush()
        await db.refresh(product)
        return product
```

### expire_on_commit=True (default) in async

```python
# WRONG — accessing attributes after commit triggers lazy load → MissingGreenlet
async_session_factory = async_sessionmaker(engine, class_=AsyncSession)
# expire_on_commit defaults to True

# After commit:
await db.commit()
print(product.name)  # MissingGreenlet error

# CORRECT
async_session_factory = async_sessionmaker(
    engine, class_=AsyncSession, expire_on_commit=False
)
```

### N+1 queries

```python
# WRONG — 1 query for products + N queries for categories
products = (await db.execute(select(Product))).scalars().all()
for p in products:
    print(p.category.name)  # N queries

# CORRECT — 2 queries total
products = (
    await db.execute(select(Product).options(selectinload(Product.category)))
).scalars().all()
for p in products:
    print(p.category.name)  # no extra queries
```

## Resources

- **SQLAlchemy 2.x docs**: <https://docs.sqlalchemy.org/en/20/>
- **Async SQLAlchemy guide**:
  <https://docs.sqlalchemy.org/en/20/orm/extensions/asyncio.html>
- **Alembic docs**: <https://alembic.sqlalchemy.org/en/latest/>
- **asyncpg driver**: <https://magicstack.github.io/asyncpg/>
