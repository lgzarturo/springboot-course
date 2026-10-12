---
id: python
name: python
description: >
  Python development best practices: clean code, patterns,
  type hints, decorators, context managers, and architecture.
  Trigger: When writing Python code anywhere in the project.

user-invokable: true
license: MIT
metadata:
  author: lgzarturo
  category: python

compatibility:
  tools: [claude, codex, gemini, agy, opencode]
  stacks:
    languages: [python]
    frameworks: [django, fastapi, flask]

risk:
  level: low
  can_execute_shell: false
  can_modify_files: true
  requires_network: false

inputs:
  - Python source files
  - existing functions, classes, or modules
  - business logic implementations to review or refactor

outputs:
  - clean Python functions and classes with meaningful names
  - type-annotated public API signatures
  - custom domain exceptions
  - value objects and data classes
  - service and repository pattern implementations

quality:
  reviewed_by: codeconductor-core
  version: 0.1.0
---

## When to Use

- Writing any Python code in the project
- Reviewing code for quality and maintainability
- Designing new functions, classes, or modules
- Implementing business logic or utilities

## Clean Code Principles

### Readability > Brevity

Code is read more times than it's written. Prioritize clarity over cleverness:

```python
# WRONG — clever but obscure
def f(x): return x if x else 0

# CORRECT — readable
def calculate_discount(price, has_discount):
    if not has_discount:
        return 0
    return price * DISCOUNT_RATE
```

### Meaningful Names

| What      | Convention             | Example                                      |
| --------- | ---------------------- | -------------------------------------------- |
| Variables | descriptive snake_case | `total_price`, `products_list`               |
| Functions | verb snake_case        | `get_active_products()`, `calculate_total()` |
| Classes   | PascalCase             | `OrderService`, `CartController`             |
| Constants | UPPER_SNAKE_CASE       | `MAX_RETRY_COUNT`, `DEFAULT_PAGE_SIZE`       |
| Modules   | snake_case             | `order_service.py`, `cart_utils.py`          |

```python
# WRONG — cryptic names
d = 1500
p = products.filter(a=True)

# CORRECT — names that say what they are
discount_amount = 1500
active_products = Product.objects.filter(is_active=True)
```

### Small Functions

A function should do ONE thing. Ideally under 30 lines:

```python
# WRONG — function does many things
def process_order(order_data):
    # validates
    # creates order
    # creates items
    # updates stock
    # sends email
    # updates analytics
    ...

# CORRECT — small, focused functions
def process_order(order_data):
    validated = validate_order_data(order_data)
    order = create_order(validated)
    create_order_items(order, validated["items"])
    update_product_stock(validated["items"])
    send_order_confirmation(order)
    track_order_analytics(order)
    return order
```

### DRY — Don't Repeat Yourself

Extract repeated code into functions or classes:

```python
# WRONG — repeat logic
if order.status == "pending":
    send_email(order.customer, "Your order is pending")
    log_event("order_pending", order.id)

if order.status == "paid":
    send_email(order.customer, "Your order was paid")
    log_event("order_paid", order.id)

# CORRECT — extract to function
def notify_order_status(order):
    status_messages = {
        "pending": "Your order is pending",
        "paid": "Your order was paid",
    }
    send_email(order.customer, status_messages[order.status])
    log_event(f"order_{order.status}", order.id)
```

## Type Hints

### When to Use Them

Use type hints on:

- Public function signatures (APIs, services)
- Functions with complex parameters
- Non-obvious returns

```python
from typing import Optional, List, Dict, Any

# Public functions — use hints
def calculate_total(items: List[Dict[str, Any]], tax_rate: float) -> float:
    ...

def find_product(product_id: int) -> Optional[Product]:
    ...

# Private helpers — optional
def _build_cart_response(cart):
    ...
```

### Basic Types

```python
# Primitives
name: str = "Product"
quantity: int = 5
price: float = 1500.99
is_active: bool = True

# Collections
products: List[Product] = []
product_ids: list[int] = []
metadata: Dict[str, Any] = {}
items: tuple[str, int] = ("sku", 5)

# Optionals
user: Optional[User] = None
description: str | None = None
```

### Advanced Types

```python
from typing import Union, Optional, Callable, Any
from decimal import Decimal

# Union for multiple types
def process_payment(amount: float, method: str) -> Union[dict, None]:
    if method == "cash":
        return {"change": amount - 1000}
    return None

# Callable for functions as parameters
def execute_callback(callback: Callable[[str], None], message: str):
    callback(message)

# TypeAlias for complex types
CartData = Dict[str, Dict[str, Union[int, str]]]

def process_cart(cart: CartData):
    ...
```

## Common Patterns

### Context Managers

For resources needing cleanup (files, connections, transactions):

```python
# WRONG — no guaranteed cleanup
def write_report(data):
    f = open("report.txt", "w")
    f.write(data)
    f.close()  # Won't run if exception occurs

# CORRECT — context manager
def write_report(data):
    with open("report.txt", "w") as f:
        f.write(data)
    # Automatic cleanup

# Custom context manager
class Transaction:
    def __init__(self):
        self.entered = False

    def __enter__(self):
        db.begin()
        self.entered = True
        return self

    def __exit__(self, exc_type, exc_val, exc_tb):
        if exc_type:
            db.rollback()
        else:
            db.commit()
        return False  # Don't suppress exceptions
```

### Decorators

Functions that modify the behavior of other functions:

```python
import functools
import time

# Basic decorator
def require_auth(view):
    @functools.wraps(view)
    def wrapper(request, *args, **kwargs):
        if not request.user.is_authenticated:
            return JsonResponse({"error": "Unauthorized"}, status=401)
        return view(request, *args, **kwargs)
    return wrapper

# Decorator with parameters
def require_role(role: str):
    def decorator(view):
        @functools.wraps(view)
        def wrapper(request, *args, **kwargs):
            if request.user.role != role:
                return JsonResponse({"error": "Forbidden"}, status=403)
            return view(request, *args, **kwargs)
        return wrapper
    return decorator

# Decorator with timing
def timing(view):
    @functools.wraps(view)
    def wrapper(*args, **kwargs):
        start = time.perf_counter()
        result = view(*args, **kwargs)
        elapsed = time.perf_counter() - start
        print(f"{view.__name__} took {elapsed:.4f}s")
        return result
    return wrapper
```

### Data Classes

For simple data structures:

```python
from dataclasses import dataclass
from datetime import datetime
from decimal import Decimal

@dataclass
class CartItem:
    product_id: int
    quantity: int
    price: Decimal
    name: str = ""

    @property
    def subtotal(self) -> Decimal:
        return self.price * self.quantity

# Usage
item = CartItem(product_id=1, quantity=2, price=Decimal("1500.00"))
print(item.subtotal)  # 3000.00
```

### Enums

For limited values:

```python
from enum import Enum

class OrderStatus(str, Enum):
    PENDING = "pending"
    PAID = "paid"
    SHIPPED = "shipped"
    CANCELLED = "cancelled"

    @property
    def is_final(self):
        return self in (OrderStatus.SHIPPED, OrderStatus.CANCELLED)

# Usage
order.status = OrderStatus.PAID
if order.status.is_final:
    ...
```

## Errors and Exceptions

### Exception Handling

```python
# WRONG — catch generic Exception
try:
    product = Product.objects.get(id=product_id)
except Exception:
    return JsonResponse({"error": "Not found"}, status=404)

# CORRECT — catch specific exception
try:
    product = Product.objects.get(id=product_id)
except Product.DoesNotExist:
    return JsonResponse({"error": "Product not found"}, status=404)

# With fallback
product = Product.objects.filter(id=product_id).first()
if not product:
    return JsonResponse({"error": "Product not found"}, status=404)
```

### Custom Exceptions

```python
class BusinessException(Exception):
    """Exception for business logic errors."""
    def __init__(self, message: str, code: str = "BUSINESS_ERROR"):
        self.message = message
        self.code = code
        super().__init__(self.message)

class InsufficientStockException(BusinessException):
    def __init__(self, product_id: int, requested: int, available: int):
        super().__init__(
            message=f"Insufficient stock: requested {requested}, available {available}",
            code="INSUFFICIENT_STOCK",
        )
        self.product_id = product_id
        self.requested = requested
        self.available = available

# Usage
try:
    if requested > available:
        raise InsufficientStockException(product_id, requested, available)
except InsufficientStockException as e:
    return JsonResponse({"error": e.message, "code": e.code}, status=400)
```

## Architecture Patterns

### Repository Pattern

Abstract data access:

```python
class ProductRepository:
    @staticmethod
    def get_active() -> QuerySet:
        return Product.objects.filter(is_active=True)

    @staticmethod
    def get_by_category(category_id: int) -> QuerySet:
        return Product.objects.filter(
            category_id=category_id,
            is_active=True,
        ).select_related("category")

    @staticmethod
    def search(query: str) -> QuerySet:
        return Product.objects.filter(
            Q(name__icontains=query) | Q(sku__icontains=query),
            is_active=True,
        )
```

### Service Layer

Encapsulated business logic:

```python
class ProductService:
    @staticmethod
    def get_products_for_pos(store: Store) -> List[Dict]:
        products = ProductRepository.get_active().select_related(
            "category", "tax"
        )
        return [
            {
                "id": p.id,
                "name": p.name,
                "price": str(p.get_display_price()),
                "stock": p.stock if not p.is_service else None,
                "is_service": p.is_service,
            }
            for p in products
        ]

    @staticmethod
    def check_stock(product_id: int, quantity: int) -> bool:
        try:
            product = Product.objects.get(id=product_id)
        except Product.DoesNotExist:
            return False

        if product.is_service:
            return True
        return product.stock >= quantity
```

### Value Objects

Immutable objects representing values:

```python
from dataclasses import dataclass
from decimal import Decimal

@dataclass(frozen=True)
class Money:
    amount: Decimal
    currency: str = "ARS"

    def __add__(self, other: "Money") -> "Money":
        if self.currency != other.currency:
            raise ValueError("Cannot add different currencies")
        return Money(self.amount + other.amount, self.currency)

    def __str__(self):
        return f"{self.currency} {self.amount:.2f}"

# Usage
subtotal = Money(Decimal("1000.00"))
tax = Money(Decimal("210.00"))
total = subtotal + tax  # Money(amount=Decimal('1210.00'), currency='ARS')
```

## Utility Functions

### Common Helper Functions

```python
import os
import json
from datetime import datetime
from decimal import Decimal
from typing import Any, Dict

def safe_int(value: Any, default: int = 0) -> int:
    """Safely convert to int."""
    try:
        return int(value)
    except (ValueError, TypeError):
        return default

def safe_decimal(value: Any, default: Decimal = Decimal("0")) -> Decimal:
    """Safely convert to Decimal."""
    try:
        return Decimal(str(value))
    except (ValueError, TypeError):
        return default

def format_money(amount: Decimal, currency: str = "ARS") -> str:
    """Format money for display."""
    return f"{currency} {amount:,.2f}".replace(",", ".")

def parse_json(body: bytes) -> Dict[str, Any]:
    """Parse JSON from request body."""
    try:
        return json.loads(body)
    except json.JSONDecodeError:
        raise ValueError("Invalid JSON")

def truncate(text: str, length: int = 100, suffix: str = "...") -> str:
    """Truncate text to length."""
    if len(text) <= length:
        return text
    return text[:length - len(suffix)] + suffix
```

### Testing Utilities

```python
# tests/test_utils.py
import pytest
from decimal import Decimal

class TestSafeInt:
    def test_valid_int(self):
        assert safe_int("42") == 42

    def test_invalid_returns_default(self):
        assert safe_int("not a number") == 0

    def test_custom_default(self):
        assert safe_int("invalid", 100) == 100

class TestMoney:
    def test_add_same_currency(self):
        a = Money(Decimal("100"))
        b = Money(Decimal("50"))
        assert a + b == Money(Decimal("150"))

    def test_different_currency_raises(self):
        a = Money(Decimal("100"), "ARS")
        b = Money(Decimal("50"), "USD")
        with pytest.raises(ValueError):
            _ = a + b
```

## Code to Avoid

### Mutable Default Arguments

```python
# WRONG — mutable default
def add_items(items=[]):
    items.append(1)
    return items

# CORRECT
def add_items(items=None):
    if items is None:
        items = []
    items.append(1)
    return items
```

### Shadowing Builtins

```python
# WRONG
list = [1, 2, 3]  # shadows built-in
id = "abc"        # shadows built-in

# CORRECT
items = [1, 2, 3]
product_id = "abc"
```

### Magic Numbers

```python
# WRONG
if price > 1000:
    discount = price * 0.1

# CORRECT
DISCOUNT_THRESHOLD = Decimal("1000")
DISCOUNT_RATE = Decimal("0.1")

if price > DISCOUNT_THRESHOLD:
    discount = price * DISCOUNT_RATE
```

### Modifying Collections in Loop

```python
# WRONG — modifies while iterating
for item in items:
    if item.deleted:
        items.remove(item)

# CORRECT
items = [item for item in items if not item.deleted]
# or
items_to_keep = []
for item in items:
    if not item.deleted:
        items_to_keep.append(item)
items = items_to_keep
```

## Resources

- **PEP 8** — Style Guide: https://peps.python.org/pep-0008/
- **PEP 484** — Type Hints: https://peps.python.org/pep-0484/
- **Real Python**: https://realpython.com/
- **Python Docs**: https://docs.python.org/3/
