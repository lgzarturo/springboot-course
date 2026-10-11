---
id: django-testing
name: django-testing
description: >
  Testing patterns for multi-tenant Django POS projects using pytest-django,
  RequestFactory, and mocks. No factories, no complex conftest.
  Trigger: When writing or reviewing tests in any apps/ directory.

user-invokable: true
license: MIT
metadata:
  author: lgzarturo
  category: testing

compatibility:
  tools: [claude, codex, gemini, agy, opencode]
  stacks:
    languages: [python]
    frameworks: [django, pytest-django, django-tenants]

risk:
  level: low
  can_execute_shell: true
  can_modify_files: true
  requires_network: false

inputs:
  - view and service files under apps/
  - existing test files (apps/*/tests/ or apps/*/tests.py)
  - pytest configuration (pyproject.toml)

outputs:
  - SimpleTestCase-based unit test classes
  - mock configurations for tenant-app models
  - RequestFactory request builders
  - FakeSession pattern implementations
  - queryset chain mock helpers
  - pytest test files following project conventions

quality:
  reviewed_by: codeconductor-core
  version: 0.1.0
paths:
  - "**/test_*.py"
  - "**/tests/**"
---
## When to Use

- Writing any test in `apps/*/tests/` or `apps/*/tests.py`
- Deciding which test base class to use
- Mocking ORM calls for TENANT_APP models
- Testing views that use custom access decorators
- Testing APIs, PDFs, and cart operations

## Test Architecture in Multi-Tenant Projects

### The Schema Problem

This project uses `django-tenants` with multi-schema PostgreSQL:

- **Shared apps** (public schema): `core`, `users`, Django contrib
- **Tenant apps** (per-store schema): `employees`, `catalog`, `cart`, `orders`,
  `pos`, `storefront`, `analytics`

**The test runner uses the public schema**, so tenant app tables DON'T exist in
tests. This profoundly affects how we write tests.

### Base Class Selection

| Condition                                   | Use                      | Notes                 |
| ------------------------------------------- | ------------------------ | --------------------- |
| No DB access needed                         | `SimpleTestCase`         | No transaction, no DB |
| Only public schema models (`User`, `Store`) | `TestCase`               | Uses public schema    |
| **Any tenant app model**                    | `SimpleTestCase` + mocks | **ALWAYS**            |

```python
# WRONG — crashes because tables don't exist
class TestProductAPI(TestCase):
    def test_list_products(self):
        product = Product.objects.create(...)  # Table does not exist!

# CORRECT — SimpleTestCase with mocks
class TestProductAPI(SimpleTestCase):
    @patch("apps.catalog.views.Product")
    def test_list_products(self, mock_product):
        mock_product.objects.filter.return_value = MagicMock()
        response = api_catalog_products(request)
```

## Mock Patterns

### 1. RequestFactory — Never TestClient

`TestClient` runs `TenantMainMiddleware`, which redirects tenant URLs and breaks
view tests. Use `RequestFactory` and call the view directly:

```python
from django.test import RequestFactory

factory = RequestFactory()

# GET request
request = factory.get("/pos/api/products/")
request.user = mock_user

# POST request with JSON
request = factory.post(
    "/pos/api/cart/add/",
    data=json.dumps({"product_id": 1, "quantity": 2}),
    content_type="application/json"
)
request.user = mock_user

# Call the view directly — no middleware
response = api_search_products(request)
```

### 2. Bypass Access Decorators

`require_pos_access` and `require_manager_access` check `EmployeeProfile`
(tenant app). Bypass with `is_superuser`:

```python
# The decorator does:
# def require_pos_access(view):
#     def wrapper(request, *args, **kwargs):
#         if not request.user.is_superuser:
#             emp = EmployeeProfile.objects.get(user=request.user)
#             if not emp.has_pos_access:
#                 return JsonResponse({"error": "No access"}, status=403)
#         return view(request, *args, **kwargs)

mock_user = MagicMock()
mock_user.is_authenticated = True
mock_user.is_superuser = True  # Skip EmployeeProfile lookup

request.user = mock_user
```

### 3. FakeSession Pattern

Carts and session-dependent views need a writable session dict:

```python
class FakeSession(dict):
    modified = False

    def save(self):
        pass

request.session = FakeSession()
request.session["pos_cart"] = {}
```

### 4. MagicMock.DoesNotExist Trap

`except Model.DoesNotExist` won't catch a plain `MagicMock`. Create a real
exception class:

```python
# When mocking a model
mock_cart = MagicMock()
mock_cart.DoesNotExist = type("DoesNotExist", (Exception,), {})

with pytest.raises(mock_cart.DoesNotExist):
    TemporalCart.objects.get(session_key=...)
```

### 5. MagicMock name= Trap

`MagicMock(name="X")` sets the internal repr, NOT the `.name` attribute:

```python
# WRONG — .name will be a MagicMock, not "Papel A4"
mock = MagicMock(name="Papel A4")
print(mock.name)  # <MagicMock name='Papel A4'>

# CORRECT
mock = MagicMock()
mock.name = "Papel A4"
mock.price = "1500.00"
```

### 6. Queryset Chain Mocking

Mock each chained call explicitly:

```python
def _setup_product_mock(mock_product_model, product_data=None):
    """Setup complete Product mock with chained queryset."""
    mock_qs = MagicMock()

    # Each method in the chain returns the same mock
    mock_product_model.objects.filter.return_value = mock_qs
    mock_qs.select_related.return_value = mock_qs
    mock_qs.annotate.return_value = mock_qs
    mock_qs.order_by.return_value = mock_qs
    mock_qs.__getitem__ = lambda self, s: [mock_product] if isinstance(s, slice) else mock_product
    mock_qs.count.return_value = 1

    return mock_qs
```

### 7. Mock Model with Real Data

```python
def _create_mock_product(product_id=1, name="Test Product", price="1000.00"):
    mock = MagicMock()
    mock.id = product_id
    mock.name = name
    mock.price = price
    mock.is_active = True
    mock.stock = 10
    mock.is_service = False
    mock.category_id = 1
    mock.__str__ = lambda self: self.name
    return mock
```

## File Structure

```
apps/{app}/tests.py          # simple apps, one file
apps/{app}/tests/__init__.py # complex apps
apps/{app}/tests/test_{feature}.py
```

### Section Separators

```python
# ── TestClassName ────────────────────────────────────────
```

### Module Docstring

Every test file must explain the multi-tenant constraint:

```python
"""
Tests for catalog API.

NOTE: catalog models are TENANT_APP — they live in per-store schemas.
The test runner uses the public schema, so these tables don't exist.
All tests use SimpleTestCase + mocks.
"""
```

### Naming Conventions

- `_make_{thing}()` — builds a request or object
- `_build_{thing}()` — constructs data structures
- `_setup_{thing}_mock()` — configures a mock for reuse

## Test Types

### 1. API/View Tests

```python
class TestProductSearch(SimpleTestCase):
    """Tests for POS product search."""

    def _make_request(self, query="", offset=0, limit=20):
        factory = RequestFactory()
        request = factory.get(f"/pos/api/search/?q={query}&offset={offset}&limit={limit}")
        request.user = MagicMock()
        request.user.is_authenticated = True
        request.user.is_superuser = True
        return request

    @patch("apps.pos.views.Product")
    def test_search_returns_results(self, mock_product):
        mock_product_obj = _create_mock_product(name="Lápices")
        mock_qs = MagicMock()
        mock_qs.select_related.return_value = mock_qs
        mock_qs.annotate.return_value = mock_qs
        mock_qs.filter.return_value = mock_qs
        mock_qs.__getitem__ = lambda self, s: [mock_product_obj] if isinstance(s, slice) else mock_product_obj
        mock_product.objects.filter.return_value = mock_qs

        request = self._make_request(query="lápices")
        response = api_search_products(request)

        self.assertEqual(response.status_code, 200)
        data = json.loads(response.content)
        self.assertIn("results", data)
```

### 2. Service Tests

```python
class TestCartOperations(SimpleTestCase):
    """Tests for cart operations."""

    def _build_cart_data(self, items):
        """Build cart data as it arrives from request."""
        return {str(k): {"quantity": v["quantity"], "price": str(v["price"])}
                for k, v in items.items()}

    @patch("apps.pos.services.CartService.get_products_from_db")
    def test_calculate_total(self, mock_get_products):
        mock_get_products.return_value = {
            1: _create_mock_product(price="1000.00"),
            2: _create_mock_product(price="2500.00"),
        }

        cart_data = self._build_cart_data({1: {"quantity": 2}, 2: {"quantity": 1}})
        total = CartService.calculate_total(cart_data)

        self.assertEqual(total, Decimal("4500.00"))
```

### 3. PDF Generation Tests

```python
class TestRestockPDF(SimpleTestCase):
    """Tests for restock PDF generation."""

    def test_pdf_generates_correct_content(self):
        products = [
            {"name": "Product 1", "sku": "SKU001", "stock": 5, "min_stock": 10},
            {"name": "Product 2", "sku": "SKU002", "stock": 0, "min_stock": 5},
        ]

        pdf_bytes = generate_restock_pdf("Test Store", products)

        # Verify it's a valid PDF
        self.assertTrue(pdf_bytes.startswith(b"%PDF"))
```

## Testing Commands

```bash
# Run all tests
make tests

# Run a single file
uv run pytest apps/pos/tests/test_cart_checkout.py

# Run a single test
uv run pytest apps/pos/tests/test_cart_checkout.py::TestCartCheckout::test_checkout_success -v

# Force fresh DB (after schema changes)
uv run pytest --create-db

# With coverage
make tests-coverage

# Run with verbose output
uv run pytest -v --tb=short

# Run only failed tests from last run
uv run pytest --lf
```

## pytest Configuration

In `pyproject.toml`:

```toml
[tool.pytest.ini_options]
DJANGO_SETTINGS_MODULE = "config.settings"
python_files = ["test_*.py", "*_test.py"]
python_classes = ["Test*"]
python_functions = ["test_*"]
addopts = "--reuse-db --nomigrations -x"
testpaths = ["apps"]
```

## Common Errors

### Error: "no such table"

```python
# CAUSE: TestCase for tenant app model
# SOLUTION: SimpleTestCase + mocks

# WRONG
class TestProduct(TestCase):
    def test_create(self):
        Product.objects.create(name="Test")

# CORRECT
class TestProduct(SimpleTestCase):
    @patch("apps.catalog.models.Product")
    def test_create(self, mock_product):
        mock_product.objects.create.return_value = MagicMock(id=1)
```

### Error: "relation does not exist"

Same as above — schema mismatch between test and tenant.

### Mocking transaction.atomic

```python
from unittest.mock import patch, MagicMock

@patch("django.db.transaction.atomic")
def test_order_creation(self, mock_atomic):
    # transaction.atomic as context manager
    mock_context = MagicMock()
    mock_atomic.return_value = mock_context

    # Code uses "with transaction.atomic()"
    # Mock must support context protocol
```

## Resources

- **Canonical queryset mock**: `apps/pos/tests/test_search_products.py`
- **FakeSession + transaction mock**: `apps/pos/tests/test_cart_checkout.py`
- **DoesNotExist + MagicMock.name traps**:
  `apps/pos/tests/test_temporal_cart.py`
- **Multi-decorator bypass**: `apps/catalog/tests/test_api_catalog_products.py`
- **pytest config**: `pyproject.toml` → `[tool.pytest.ini_options]`
- **pytest-django docs**: <https://pytest-django.readthedocs.io/>
