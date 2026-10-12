---
id: django-orm
name: django-orm
description: >
  Django ORM patterns for multi-tenant POS projects: efficient queries,
  bulk operations, transactions, and multi-schema upload paths.
  Trigger: When writing queryset logic, model saves, or DB-touching service code.

user-invokable: true
license: MIT
metadata:
  author: lgzarturo
  category: django

compatibility:
  tools: [claude, codex, gemini, agy, opencode]
  stacks:
    languages: [python]
    frameworks: [django, django-tenants]

risk:
  level: medium
  can_execute_shell: true
  can_modify_files: true
  requires_network: false

inputs:
  - model files (models.py)
  - service files (services.py)
  - view files with queryset logic
  - existing queryset or bulk-operation code

outputs:
  - optimized queryset patterns with select_related / prefetch_related
  - bulk_create and bulk_update implementations
  - transaction-safe service methods
  - annotated and aggregated querysets
  - migration commands for multi-schema projects

quality:
  reviewed_by: codeconductor-core
  version: 0.1.0
---

## When to Use

- Writing or reviewing queryset code in any `apps/*/`
- Adding aggregations, filters, or annotations to a queryset
- Writing model `save()` overrides or signals
- Implementing service-layer DB operations
- Optimizing queries that traverse FK relationships

## Performance Fundamentals

### The N+1 Problem

The most common Django mistake: iterating over a queryset and accessing FK
fields without `select_related`. Each access generates a separate DB query.

```
# Initial query: 1 query
products = Product.objects.all()

# In the loop: N additional queries (one per product)
for p in products:
    print(p.category.name)  # category_id → SELECT * FROM category WHERE id = ?

# Total: 1 + N queries
```

### Solution: select_related and prefetch_related

| Method                             | Use                        | SQL Query                |
| ---------------------------------- | -------------------------- | ------------------------ |
| `select_related`                   | FK one-to-one or ManyToOne | Automatic JOIN           |
| `prefetch_related`                 | Reverse FK or ManyToMany   | 2 separate queries       |
| `prefetch_related` with `Prefetch` | Custom queryset            | Filtered in nested query |

```python
# FK traversal — use select_related
products = Product.objects.select_related("category", "tax")

# Multiple FK
products = Product.objects.select_related("category", "tax", "supplier")

# Reverse FK (one-to-many) — use prefetch_related
categories = Category.objects.prefetch_related("products")

# ManyToMany — prefetch_related
product = Product.objects.prefetch_related("tags").first()

# Custom prefetch with filter
from django.db.models import Prefetch

products = Product.objects.prefetch_related(
    Prefetch(
        "order_items",
        queryset=OrderItem.objects.filter(order__status="paid")
    )
)
```

## Query Patterns

### 1. Efficient Filtering

```python
# Basic filtering
Product.objects.filter(is_active=True, stock__gt=0)

# Filtering with Q objects — OR and negations
from django.db.models import Q

Product.objects.filter(
    Q(is_active=True) & (Q(stock__gt=0) | Q(is_service=True))
)

# Exclusion
Product.objects.exclude(status="draft")

# Filter by nested FK
Order.objects.filter(employee__store=store)
```

### 2. Annotations and Aggregations

```python
from django.db.models import Count, Sum, Avg, Max, Min, F, Q, Case, When, Value, CharField
from django.db.models.functions import Coalesce, Concat

# Conditional sum with Coalesce (avoids None)
Product.objects.annotate(
    total_sold=Coalesce(
        Sum(
            "order_items__quantity",
            filter=Q(
                order_items__order__status__in=["paid", "shipped"],
                order_items__order__created_at__gte=cutoff,
            ),
        ),
        0,
    ),
)

# Use F for arithmetic operations
Product.objects.update(stock=F("stock") - 1)

# Annotation with Case/When for conditional logic
Product.objects.annotate(
    status_flag=Case(
        When(stock__lte=0, then=Value("out_of_stock")),
        When(stock__lte=10, then=Value("low_stock")),
        default=Value("available"),
        output_field=CharField(),
    ),
)

# Aggregation with filter
Store.objects.annotate(
    paid_orders=Count("orders", filter=Q(orders__status="paid")),
)
```

### 3. Exists vs Count

**Golden rule**: For existence checks, use `Exists`, never `Count`.

```python
from django.db.models import Exists, OuterRef, Count

# WRONG — loads full count
products = Product.objects.annotate(
    order_count=Count("order_items")
).filter(order_count__gt=0)

# CORRECT — short-circuits at first match
products = Product.objects.annotate(
    has_orders=Exists(OrderItem.objects.filter(product=OuterRef("pk")))
).filter(has_orders=True)

# With more complex subquery
from django.db.models import Subquery

latest_order = Order.objects.filter(
    customer=OuterRef("customer_id")
).order_by("-created_at")

Customer.objects.annotate(
    last_order_date=Subquery(
        latest_order.values("created_at")[:1]
    )
)
```

### 4. Ordering

```python
# Basic ordering
Product.objects.order_by("name")

# Ordering with NullsFirst/NullsLast
Product.objects.order_by(F("price").nulls_last())

# Ordering by annotation
Store.objects.annotate(
    order_count=Count("orders")
).order_by("-order_count")
```

## Write Operations

### 5. Bulk Operations — Anti-N+1

**Never do `save()` inside a loop.** Build lists and use bulk operations:

```python
# Get with lock once
products = {p.id: p for p in Product.objects.select_for_update().filter(id__in=ids)}

order_items_to_create = []
products_to_update = []

for item in cart:
    product = products[item["id"]]
    product.stock -= item["quantity"]
    order_items_to_create.append(
        OrderItem(order=order, product=product, quantity=item["quantity"], ...)
    )
    products_to_update.append(product)

# Bulk create and update
OrderItem.objects.bulk_create(order_items_to_create)
Product.objects.bulk_update(products_to_update, ["stock", "updated_at"])
```

**Bulk operation table:**

| Method                                      | Use case                   | Returns           |
| ------------------------------------------- | -------------------------- | ----------------- |
| `bulk_create(items)`                        | Create multiple records    | List of objects   |
| `bulk_update(items, fields)`                | Update multiple records    | None (in-place)   |
| `bulk_create(items, ignore_conflicts=True)` | Ignore duplicates          | List (IDs only)   |
| `update()`                                  | Mass update without return | Count of affected |

```python
# bulk_create with ignore_conflicts
Product.objects.bulk_create(new_products, ignore_conflicts=True)

# bulk update
Product.objects.filter(category=cat).update(is_active=False)

# update with F expressions
Product.objects.update(stock=F("stock") - 1)
```

### 6. Surgical Saves — update_fields

**Always pass `update_fields`** when saving a subset of fields:

```python
# Update single field
product.stock -= 1
product.save(update_fields=["stock"])

# Update multiple fields
order.status = "paid"
order.paid_at = timezone.now()
order.save(update_fields=["status", "paid_at", "updated_at"])

# Don't use in migrations — that uses reconstructor
# DO use in application code
```

**Why?** Prevents:

- Accidental image reprocessing
- Unnecessary signals
- Race conditions on unrelated fields

### 7. Transactions

Use `transaction.atomic()` as a context manager, never as a decorator:

```python
from django.db import transaction

# CORRECT — clear context, automatic error handling
with transaction.atomic():
    order.save()
    OrderItem.objects.bulk_create(items)
    Product.objects.bulk_update(products, ["stock"])
    cart.clear()

# With select_for_update inside the transaction
with transaction.atomic():
    product = Product.objects.select_for_update().get(pk=product_id)
    product.stock -= quantity
    product.save(update_fields=["stock", "updated_at"])

# WRONG — decorator hides intent
@transaction.atomic
def create_order(...):
    ...
```

**Isolation levels:**

```python
from django.db import transaction

# Serializable — maximum isolation
with transaction.atomic():
    ...

# Read committed (default) — may cause dirty reads on Edge
```

## Multi-Tenant and Upload Paths

### 8. Upload Paths with Schema

Every `FileField`/`ImageField` must include `connection.schema_name` to isolate
files per tenant:

```python
import os
from django.db import connection

def get_upload_path(instance, filename):
    schema = connection.schema_name
    ext = filename.split(".")[-1].lower()
    safe_name = f"{uuid4().hex}.{ext}"
    return os.path.join(schema, "products", safe_name)

def get_thumb_path(instance, filename):
    schema = connection.schema_name
    return os.path.join(schema, "products", "thumbs", filename)

class Product(models.Model):
    image = models.ImageField(upload_to=get_upload_path, storage=S3Storage())
    thumbnail = models.ImageField(upload_to=get_thumb_path, blank=True)
```

## Advanced Queries

### 9. Subqueries

```python
from django.db.models import Subquery, OuterRef

# Subquery to get last value
latest_price = ProductPrice.objects.filter(
    product=OuterRef("pk")
).order_by("-valid_from").values("price")[:1]

Product.objects.annotate(current_price=Subquery(latest_price))

# Subquery with aggregate
order_total = OrderItem.objects.filter(
    order=OuterRef("pk")
).values("order").annotate(total=Sum("subtotal")).values("total")

Order.objects.annotate(order_total=Subquery(order_total))
```

### 10. Raw Queries — WHEN TO USE

Avoid raw queries unless necessary. Use when:

- Complex aggregation functions not supported by ORM
- Queries with multiple JOINs manually optimized
- Very complex queries where ORM generates inefficient SQL

```python
# With safe parameters (never string interpolation!)
Product.objects.raw(
    "SELECT * FROM catalog_product WHERE tsvector @@ plainto_tsquery(%s)",
    [search_term]
)

# With cursor for complex cases
from django.db import connection

with connection.cursor() as cursor:
    cursor.execute("SELECT ...", [params])
    results = cursor.fetchall()
```

## Migration Commands

```bash
# Always migrate both schemas
uv run python manage.py migrate_schemas --shared
uv run python manage.py migrate_schemas --tenant

# Check for pending migrations
make verifymigrations

# Create specific migration
uv run python manage.py makemigrations catalog --name add_thumbnail

# Shows SQL without running
uv run python manage.py migrate --fake catalog 0003
```

## Common Mistakes and How to Avoid Them

### N+1 in templates

```python
# WRONG — N queries
{% for product in products %}
    {{ product.category.name }}
{% endfor %}

# CORRECT — 1 query with select_related
products = Product.objects.select_related("category")
```

### ForeignKey without related_name

```python
# WRONG
class Order(models.Model):
    customer = models.ForeignKey("users.User", on_delete=...)

# CORRECT — always add related_name
class Order(models.Model):
    customer = models.ForeignKey(
        "users.User",
        on_delete=models.CASCADE,
        related_name="orders",
    )
```

### Queries in signals

```python
# WRONG — signal makes additional query
@receiver(post_save, sender=Order)
def on_order_save(sender, instance, **kwargs):
    customer = instance.customer  # Additional query!
    send_email(customer.email, ...)

# CORRECT — pass the object, not the ID
@receiver(post_save, sender=Order)
def on_order_save(sender, instance, created, **kwargs):
    if created:
        customer = instance.customer
        send_email(customer.email, ...)
```

## Resources

- **Bulk write pattern**: `apps/orders/services.py`, `apps/pos/views.py` lines
  216-246
- **Annotation examples**: `apps/pos/views.py` lines 59-75
- **Exists usage**: `apps/catalog/views.py` line 112
- **Upload paths**: `apps/catalog/models.py`
- **Django ORM docs**: https://docs.djangoproject.com/en/5.2/topics/db/queries/
