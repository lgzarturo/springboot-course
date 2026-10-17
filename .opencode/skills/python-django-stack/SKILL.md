---
id: python-django-stack
name: python-django-stack
description: >
  Python and Django conventions for multi-tenant SaaS POS projects:
  views, services, naming, JSON APIs, PDFs, and cart patterns.
  Trigger: When writing any view, service, API endpoint, or model code in apps/.

user-invokable: true
license: MIT
metadata:
  author: lgzarturo
  category: django

compatibility:
  tools: [claude, codex, gemini, agy, opencode]
  stacks:
    languages: [python]
    frameworks: [django, django-tenants, django-rest-framework, reportlab]

risk:
  level: medium
  can_execute_shell: true
  can_modify_files: true
  requires_network: false

inputs:
  - view files (views.py)
  - service files (services.py)
  - model files (models.py)
  - URL configuration files (urls.py)
  - existing app structure under apps/

outputs:
  - FBV and CBV view implementations
  - service layer classes with static methods
  - JSON API responses with correct status codes
  - pagination with offset/limit pattern
  - PDF generation functions (reportlab)
  - cart session implementations
  - URL configurations with namespaces
  - model definitions with indexes and choices

quality:
  reviewed_by: codeconductor-core
  version: 0.1.0
---

## When to Use

- Writing any new view, service method, or API endpoint
- Designing a new feature and deciding where logic lives
- Adding a JSON API response
- Implementing a PDF report or cart operation
- Structuring the architecture of a new app

## Django Project Structure

### Recommended Apps

A well-structured Django project follows modular app patterns:

```
apps/
├── core/           # Configuration, shared models (Store)
├── users/          # User model, authentication
├── employees/     # Employees (tenant)
├── catalog/        # Products, categories (tenant)
├── cart/           # Storefront cart (tenant)
├── orders/         # Orders (tenant)
├── pos/            # Point of Sale (tenant)
├── storefront/     # Public store (tenant)
└── analytics/      # Reports and metrics (tenant)
```

### Per-App File Structure

```
apps/catalog/
├── __init__.py
├── models.py        # Models (Product, Category, ProductImage)
├── views.py         # Views
├── urls.py          # Routes
├── services.py      # Business logic
├── serializers.py  # Serialization (if using DRF)
├── reports.py      # PDF reports
├── signals.py       # Signals
├── admin.py        # Admin config
├── tests/
│   ├── __init__.py
│   └── test_{feature}.py
└── migrations/
```

## View Patterns

### FBV vs CBV

| Type                     | When to Use                                                            | Examples                                              |
| ------------------------ | ---------------------------------------------------------------------- | ----------------------------------------------------- |
| **FBV** (Function-Based) | All API endpoints, all POS/manager views, any view with business logic | `api_cart_add`, `api_search_products`, `pos_checkout` |
| **CBV** (Class-Based)    | Only basic storefront list/detail                                      | `HomeView`, `CategoryListView`, `ProductDetailView`   |

**Rule**: CBV for generic read-only views with simple templates. FBV for
everything else (APIs, complex logic, POS).

```python
# FBV — always for APIs
@require_pos_access
@require_POST
def api_cart_add(request):
    cart = POSCart(request)
    data = json.loads(request.body)
    cart.add(data["product_id"], data["quantity"])
    return JsonResponse({"success": True, "cart_count": cart.count()})

# CBV — only for basic storefront
class ProductDetailView(TemplateView):
    template_name = "storefront/product.html"

    def get_context_data(self, **kwargs):
        ctx = super().get_context_data(**kwargs)
        ctx["product"] = get_object_or_404(Product, slug=self.kwargs["slug"])
        return ctx
```

### Decorator Order

Custom access decorator outermost, HTTP method decorator innermost:

```python
@require_pos_access      # outermost — runs first, guards access
@require_POST            # innermost — closest to the function
def api_cart_add(request):
    ...
```

**Always use** `@require_POST` / `@require_GET` explicitly. Never
`if request.method == "POST"` inside an API view.

## Service Layer

Business logic lives in `services.py` modules. Services are classes with
`@staticmethod` only — no instance state, no `__init__`:

```python
class OrderService:
    @staticmethod
    def create_order(order: Order, cart: Cart) -> Order:
        with transaction.atomic():
            order.save()
            OrderItem.objects.bulk_create(items)
            Product.objects.bulk_update(products, ["stock"])
            cart.clear()
            return order

    @staticmethod
    def cancel_order(order_id: int, reason: str) -> Order:
        ...
```

Views call `OrderService.create_order(...)`. **No business logic in views.**

### When to Create a Service

- Complex business logic involving multiple models
- Transactions spanning multiple DB operations
- Logic reusable across multiple views
- Anything that isn't "delegate to another component"

## Naming Conventions

| Pattern                | Convention                                   | Examples                                                        |
| ---------------------- | -------------------------------------------- | --------------------------------------------------------------- |
| AJAX API views         | `api_{resource}_{action}`                    | `api_cart_add`, `api_search_products`, `api_temporal_cart_save` |
| Module-private helpers | leading `_`                                  | `_build_pos_order`, `_sort_by_category`                         |
| Module constants       | `UPPER_SNAKE_CASE`                           | `VALID_PAYMENT_METHODS = {"cash", "transfer", "card"}`          |
| URL names              | `{scope}_{resource}_{action}`                | `api_catalog_products`, `pos_checkout`                          |
| Service methods        | `camelCase` (PEP8 inconsistent but accepted) | `createOrder`, `calculateTotal`                                 |
| Model methods          | `snake_case`                                 | `get_display_price()`, `is_available()`                         |

## JSON API Response Shapes

### List Endpoint with Pagination

```python
return JsonResponse({
    "results": [...],
    "total": N,
    "has_more": bool,
})
```

### Successful Mutation

```python
return JsonResponse({"success": True, "cart_count": N})
```

### Error Response

```python
return JsonResponse({"success": False, "error": "error message"}, status=400)
```

### HTTP Status Codes

- `200` — OK, success
- `201` — Created
- `400` — Client error (invalid data, missing fields)
- `404` — Not found
- `500` — Server error

**Never return** `200` with `"success": false`. Use the correct status code.

## Pagination

### Offset/Limit — No Page Numbers

Always offset + limit, never page numbers. Default 20 items, max 100:

```python
offset = int(request.GET.get("offset", 0))
limit = min(int(request.GET.get("limit", 20)), 100)

total = qs.count()
page = qs[offset:offset + limit]

return JsonResponse({
    "results": list(page.values("id", "name", "price")),
    "total": total,
    "has_more": (offset + limit) < total,
})
```

## PDF Patterns

Pure functions, no classes. Take primitive data, return `bytes`. Internal
helpers prefixed with `_`:

```python
import io
from reportlab.lib.pagesizes import A4
from reportlab.platypus import SimpleDocTemplate, Table, TableStyle

def generate_restock_pdf(store_name: str, products_raw: list) -> bytes:
    buffer = io.BytesIO()
    doc = _make_doc(buffer)
    story = _build_story(store_name, products_raw)
    doc.build(story)
    buffer.seek(0)
    return buffer.read()

def _make_doc(buffer: io.BytesIO) -> SimpleDocTemplate:
    return SimpleDocTemplate(
        buffer,
        pagesize=A4,
        rightMargin=30,
        leftMargin=30,
        topMargin=30,
        bottomMargin=30,
    )

def _build_story(store_name: str, products_raw: list) -> list:
    data = [["SKU", "Product", "Stock", "Min"]]
    for p in products_raw:
        data.append([p["sku"], p["name"], p["stock"], p["min_stock"]])
    table = Table(data, colWidths=[60, 200, 50, 50])
    table.setStyle(TableStyle([...]))
    return [table]
```

The view wraps the result in `HttpResponse`:

```python
def api_restock_pdf(request):
    products = Product.objects.filter(stock__lte=F("min_stock"))
    pdf_bytes = generate_restock_pdf(request.store.name, list(products.values()))
    response = HttpResponse(pdf_bytes, content_type="application/pdf")
    response["Content-Disposition"] = 'inline; filename="restock.pdf"'
    return response
```

## Cart Patterns

Two independent carts coexist per request. Session key is the only separator:

| Cart       | Session Key | Module                 |
| ---------- | ----------- | ---------------------- |
| Storefront | `cart`      | `apps/cart/cart.py`    |
| POS        | `pos_cart`  | `apps/pos/pos_cart.py` |

Both follow the same contract:

- Initialize with `request`
- Store as `{str(product_id): {"quantity": int, "price": str}}`
- Price stored as `str` (Decimal is not JSON-serializable)
- Always call `self.session.modified = True` in `save()`

```python
class POSCart:
    def __init__(self, request):
        self.session = request.session.get("pos_cart", {})

    def add(self, product_id, quantity):
        key = str(product_id)
        if key in self.session:
            self.session[key]["quantity"] += quantity
        else:
            self.session[key] = {"quantity": quantity, "price": "0.00"}
        self.save()

    def save(self):
        self.session["pos_cart"] = self.session
        self.session.modified = True
```

## Type Hints

**Minimal**: Only on public service/utility function signatures, not on views or
helpers:

```python
# Service — annotate
def generate_restock_pdf(store_name: str, products_raw: list) -> bytes:
    ...

# View — no annotations
def api_cart_add(request):
    ...

# Private helpers — no annotations
def _build_cart_item(product, quantity):
    ...
```

## Access Decorators

### require_pos_access

Verifies the employee has POS access:

```python
def require_pos_access(view):
    def wrapper(request, *args, **kwargs):
        if not request.user.is_authenticated:
            return JsonResponse({"error": "Auth required"}, status=401)
        if not request.user.is_superuser:
            emp = EmployeeProfile.objects.get(user=request.user)
            if not emp.has_pos_access:
                return JsonResponse({"error": "No POS access"}, status=403)
        return view(request, *args, **kwargs)
    return wrapper
```

### require_manager_access

Verifies the employee is a manager:

```python
def require_manager_access(view):
    def wrapper(request, *args, **kwargs):
        if not request.user.is_authenticated:
            return JsonResponse({"error": "Auth required"}, status=401)
        if not request.user.is_superuser:
            emp = EmployeeProfile.objects.get(user=request.user)
            if not emp.is_manager:
                return JsonResponse({"error": "Manager required"}, status=403)
        return view(request, *args, **kwargs)
    return wrapper
```

## URLs and Routing

### URL Structure

```python
# apps/catalog/urls.py
from django.urls import path

urlpatterns = [
    # API
    path("api/products/", api_catalog_products, name="api_catalog_products"),
    path("api/products/<int:pk>/", api_product_detail, name="api_product_detail"),
    # Web
    path("", product_list, name="catalog_product_list"),
    path("<slug:slug>/", product_detail, name="catalog_product_detail"),
]
```

### Namespaces

```python
# config/urls.py
urlpatterns = [
    path("catalog/", include("apps.catalog.urls", namespace="catalog")),
    path("pos/", include("apps.pos.urls", namespace="pos")),
]
```

## Models — Best Practices

### Model Fields

```python
class Product(models.Model):
    name = models.CharField(max_length=255)
    slug = models.SlugField(max_length=255, unique=True)
    description = models.TextField(blank=True)
    price = models.DecimalField(max_digits=10, decimal_places=2)
    stock = models.PositiveIntegerField(default=0)
    is_active = models.BooleanField(default=True)
    created_at = models.DateTimeField(auto_now_add=True)
    updated_at = models.DateTimeField(auto_now=True)

    class Meta:
        ordering = ["name"]
        indexes = [
            models.Index(fields=["is_active", "stock"]),
            models.Index(fields=["slug"]),
        ]

    def __str__(self):
        return self.name

    @property
    def is_available(self):
        return self.is_active and (self.is_service or self.stock > 0)
```

### Choices

```python
class Order(models.Model):
    class Status(models.TextChoices):
        PENDING = "pending", "Pending"
        PAID = "paid", "Paid"
        SHIPPED = "shipped", "Shipped"
        CANCELLED = "cancelled", "Cancelled"

    status = models.CharField(
        max_length=20,
        choices=Status.choices,
        default=Status.PENDING,
    )
```

## Code Quality

### Line Length

88 characters (Black + Ruff). Run before every commit:

```bash
make format   # black + ruff --fix
make lint     # ruff check
uv run djlint . --reformat  # templates
```

Pre-commit hooks enforce Black and Ruff automatically.

### Imports

```python
# Standard library
import os
import json
from datetime import datetime

# Third party
from django.db import models
from django.http import JsonResponse
from django.views.decorators.http import require_POST

# Local
from apps.catalog.models import Product
from apps.pos.services import CartService
```

## Commands

```bash
make run          # dev server on 0.0.0.0:8000
make lint         # ruff check
make format       # black + ruff fix
make tests        # pytest (--reuse-db --nomigrations by default)
make migrate      # migrate_schemas --shared + --tenant
```

## Resources

- **FBV API examples**: `apps/pos/views.py`
- **Service layer**: `apps/orders/services.py`
- **PDF pattern**: `apps/catalog/reports.py`
- **Cart pattern**: `apps/pos/pos_cart.py`, `apps/cart/cart.py`
- **Pagination**: `apps/catalog/views.py` (api_catalog_products)
- **Django docs**: <https://docs.djangoproject.com/en/5.2/>
