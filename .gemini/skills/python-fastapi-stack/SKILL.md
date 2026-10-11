---
id: python-fastapi-stack
name: python-fastapi-stack
description: >
  FastAPI conventions for REST APIs: routers, Pydantic v2 schemas, dependency
  injection, error handling, pagination, and project structure.
  Trigger: When writing any router, endpoint, schema, or dependency in a FastAPI project.

user-invokable: true
license: MIT
metadata:
  author: lgzarturo
  category: fastapi

compatibility:
  tools: [claude, codex, gemini, agy, opencode]
  stacks:
    languages: [python]
    frameworks: [fastapi, pydantic, uvicorn, httpx]

risk:
  level: medium
  can_execute_shell: true
  can_modify_files: true
  requires_network: false

inputs:
  - router files (routers/*.py)
  - schema files (schemas/*.py)
  - dependency files (dependencies/*.py)
  - main application file (main.py)
  - existing endpoint or service code

outputs:
  - APIRouter implementations with prefix and tags
  - Pydantic v2 request/response schemas
  - Dependency injection functions (Depends)
  - HTTP exception handlers
  - paginated list endpoints
  - lifespan-based startup/shutdown
  - settings via pydantic-settings

quality:
  reviewed_by: codeconductor-core
  version: 0.1.0
---

## When to Use

- Writing any new endpoint, router, or schema
- Designing the structure of a new FastAPI feature
- Adding error handling or validation
- Implementing pagination or filtering
- Configuring dependencies (DB session, auth, settings)

## Project Structure

```
src/
├── main.py              # App factory, lifespan, router inclusion
├── config.py            # Settings via pydantic-settings
├── dependencies.py      # Shared Depends() functions (db, auth)
├── routers/
│   ├── __init__.py
│   ├── products.py
│   └── orders.py
├── schemas/
│   ├── __init__.py
│   ├── product.py       # ProductCreate, ProductRead, ProductUpdate
│   └── order.py
├── models/              # SQLAlchemy models (see sqlalchemy skill)
│   ├── __init__.py
│   └── product.py
├── services/            # Business logic — never in routers
│   └── product.py
└── tests/
    ├── conftest.py
    └── test_products.py
```

**Rule**: Routers delegate to services. Services contain all business logic.
Never write business logic directly in an endpoint function.

## Application Factory

```python
# main.py
from contextlib import asynccontextmanager
from fastapi import FastAPI
from src.routers import products, orders
from src.config import settings


@asynccontextmanager
async def lifespan(app: FastAPI):
    # startup
    yield
    # shutdown


def create_app() -> FastAPI:
    app = FastAPI(
        title=settings.app_name,
        version=settings.app_version,
        lifespan=lifespan,
    )
    app.include_router(products.router)
    app.include_router(orders.router)
    return app


app = create_app()
```

Use `lifespan` — never `@app.on_event("startup")` (deprecated).

## Settings

```python
# config.py
from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    model_config = SettingsConfigDict(env_file=".env", env_file_encoding="utf-8")

    app_name: str = "My API"
    app_version: str = "0.1.0"
    database_url: str
    secret_key: str
    debug: bool = False


settings = Settings()
```

Never hardcode secrets. Never import `os.environ` directly in app code — always
go through `settings`.

## Routers

```python
# routers/products.py
from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.ext.asyncio import AsyncSession
from src.dependencies import get_db
from src.schemas.product import ProductCreate, ProductRead, ProductListResponse
from src.services.product import ProductService

router = APIRouter(prefix="/products", tags=["products"])


@router.get("/", response_model=ProductListResponse)
async def list_products(
    offset: int = 0,
    limit: int = 20,
    db: AsyncSession = Depends(get_db),
):
    return await ProductService.list(db, offset=offset, limit=limit)


@router.get("/{product_id}", response_model=ProductRead)
async def get_product(product_id: int, db: AsyncSession = Depends(get_db)):
    product = await ProductService.get_or_404(db, product_id)
    return product


@router.post("/", response_model=ProductRead, status_code=status.HTTP_201_CREATED)
async def create_product(payload: ProductCreate, db: AsyncSession = Depends(get_db)):
    return await ProductService.create(db, payload)


@router.patch("/{product_id}", response_model=ProductRead)
async def update_product(
    product_id: int,
    payload: ProductUpdate,
    db: AsyncSession = Depends(get_db),
):
    return await ProductService.update(db, product_id, payload)


@router.delete("/{product_id}", status_code=status.HTTP_204_NO_CONTENT)
async def delete_product(product_id: int, db: AsyncSession = Depends(get_db)):
    await ProductService.delete(db, product_id)
```

**HTTP method rules**:

- `GET` — read, never mutates
- `POST` — create → `201`
- `PATCH` — partial update → `200`
- `PUT` — full replace → `200`
- `DELETE` — remove → `204` (no body)

## Schemas (Pydantic v2)

```python
# schemas/product.py
from datetime import datetime
from pydantic import BaseModel, Field, model_validator


class ProductBase(BaseModel):
    name: str = Field(..., min_length=1, max_length=255)
    price: float = Field(..., gt=0)
    stock: int = Field(default=0, ge=0)
    is_active: bool = True


class ProductCreate(ProductBase):
    pass


class ProductUpdate(BaseModel):
    name: str | None = Field(default=None, min_length=1, max_length=255)
    price: float | None = Field(default=None, gt=0)
    stock: int | None = Field(default=None, ge=0)
    is_active: bool | None = None

    @model_validator(mode="after")
    def at_least_one_field(self) -> "ProductUpdate":
        if all(v is None for v in self.model_dump().values()):
            raise ValueError("at least one field must be provided")
        return self


class ProductRead(ProductBase):
    model_config = {"from_attributes": True}

    id: int
    created_at: datetime
    updated_at: datetime


class ProductListResponse(BaseModel):
    results: list[ProductRead]
    total: int
    has_more: bool
```

**Schema split rule**:

- `Base` — shared fields
- `Create` — input for POST (no id, no timestamps)
- `Update` — all fields optional (PATCH semantics)
- `Read` — output with `from_attributes = True` (ORM → schema)

Never return ORM model objects directly from endpoints — always go through a
`Read` schema.

## Dependency Injection

```python
# dependencies.py
from collections.abc import AsyncGenerator
from sqlalchemy.ext.asyncio import AsyncSession
from src.db import async_session_factory


async def get_db() -> AsyncGenerator[AsyncSession, None]:
    async with async_session_factory() as session:
        try:
            yield session
            await session.commit()
        except Exception:
            await session.rollback()
            raise
```

**Rule**: commit in the dependency, not in the service. Services are
commit-agnostic. The dependency owns the transaction boundary.

## Error Handling

```python
# Raise HTTPException in services for expected errors
from fastapi import HTTPException, status


class ProductService:
    @staticmethod
    async def get_or_404(db: AsyncSession, product_id: int) -> Product:
        product = await db.get(Product, product_id)
        if product is None:
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND,
                detail=f"Product {product_id} not found",
            )
        return product
```

```python
# main.py — global exception handlers for unexpected errors
from fastapi import Request
from fastapi.responses import JSONResponse


@app.exception_handler(Exception)
async def unhandled_exception_handler(request: Request, exc: Exception) -> JSONResponse:
    return JSONResponse(
        status_code=500,
        content={"detail": "Internal server error"},
    )
```

**Status code table**:

| Code | When                                       |
| ---- | ------------------------------------------ |
| 200  | Successful GET, PATCH, PUT                 |
| 201  | Successful POST (resource created)         |
| 204  | Successful DELETE (no body)                |
| 400  | Validation error, bad input                |
| 401  | Not authenticated                          |
| 403  | Authenticated but not authorized           |
| 404  | Resource not found                         |
| 409  | Conflict (duplicate, constraint violation) |
| 422  | Pydantic validation error (automatic)      |
| 500  | Unhandled server error                     |

Never return `200` for a creation — use `201`. Never return a body for `204`.

## Pagination

Offset/limit — no page numbers. Default 20, max 100:

```python
# schemas
class ProductListResponse(BaseModel):
    results: list[ProductRead]
    total: int
    has_more: bool


# service
class ProductService:
    @staticmethod
    async def list(
        db: AsyncSession,
        offset: int = 0,
        limit: int = 20,
    ) -> ProductListResponse:
        limit = min(limit, 100)

        count_result = await db.execute(select(func.count()).select_from(Product))
        total = count_result.scalar_one()

        result = await db.execute(
            select(Product).offset(offset).limit(limit).order_by(Product.id)
        )
        items = result.scalars().all()

        return ProductListResponse(
            results=items,
            total=total,
            has_more=(offset + limit) < total,
        )
```

## Async vs Sync Endpoints

| Use async                               | Use sync                  |
| --------------------------------------- | ------------------------- |
| DB queries (AsyncSession)               | CPU-bound transformations |
| External HTTP calls (httpx.AsyncClient) | Pure in-memory logic      |
| Any `await` inside the handler          | No I/O at all             |

```python
# async — has I/O
@router.get("/{id}")
async def get_product(id: int, db: AsyncSession = Depends(get_db)):
    return await ProductService.get_or_404(db, id)

# sync — pure computation, no I/O
@router.get("/health")
def health_check():
    return {"status": "ok"}
```

Never mix `async def` with synchronous blocking calls (e.g., `requests.get`).
Use `httpx.AsyncClient` for external HTTP.

## Naming Conventions

| Concern              | Convention                 | Example                         |
| -------------------- | -------------------------- | ------------------------------- |
| Router files         | `snake_case.py`            | `product_variants.py`           |
| Schema classes       | `PascalCase` + suffix      | `ProductCreate`, `ProductRead`  |
| Endpoint functions   | `verb_resource`            | `list_products`, `create_order` |
| Service methods      | `@staticmethod`, verb_noun | `get_or_404`, `create`, `list`  |
| Dependency functions | `get_*`                    | `get_db`, `get_current_user`    |
| Router prefix        | lowercase, plural, kebab   | `/product-variants`             |
| Tags                 | lowercase, plural          | `["product-variants"]`          |

## Service Layer

Business logic lives in `services/`. Services are classes with `@staticmethod`
only:

```python
# services/product.py
from sqlalchemy import select, func
from sqlalchemy.ext.asyncio import AsyncSession
from fastapi import HTTPException, status
from src.models.product import Product
from src.schemas.product import ProductCreate, ProductUpdate, ProductListResponse


class ProductService:
    @staticmethod
    async def get_or_404(db: AsyncSession, product_id: int) -> Product:
        product = await db.get(Product, product_id)
        if product is None:
            raise HTTPException(status_code=404, detail=f"Product {product_id} not found")
        return product

    @staticmethod
    async def create(db: AsyncSession, payload: ProductCreate) -> Product:
        product = Product(**payload.model_dump())
        db.add(product)
        await db.flush()  # get the ID without committing
        await db.refresh(product)
        return product

    @staticmethod
    async def update(db: AsyncSession, product_id: int, payload: ProductUpdate) -> Product:
        product = await ProductService.get_or_404(db, product_id)
        for field, value in payload.model_dump(exclude_unset=True).items():
            setattr(product, field, value)
        await db.flush()
        await db.refresh(product)
        return product

    @staticmethod
    async def delete(db: AsyncSession, product_id: int) -> None:
        product = await ProductService.get_or_404(db, product_id)
        await db.delete(product)
```

**Rules**:

- `db.flush()` to get the ID without committing — commit is in `get_db`
- `db.refresh(product)` to reload relationships after flush
- `payload.model_dump(exclude_unset=True)` for PATCH — only update provided
  fields

## Commands

```bash
uvicorn src.main:app --reload          # dev server
uvicorn src.main:app --host 0.0.0.0   # production-like
pytest tests/ -v                       # test suite
ruff check .                           # lint
ruff format .                          # format
```

## Resources

- **FastAPI docs**: <https://fastapi.tiangolo.com/>
- **Pydantic v2 docs**: <https://docs.pydantic.dev/latest/>
- **pydantic-settings**:
  <https://docs.pydantic.dev/latest/concepts/pydantic_settings/>
