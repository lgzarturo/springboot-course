---
id: django-uv
name: django-uv
description: >
  Provides expert knowledge for building Django 5+ projects with UV as the package manager, domain-driven app structure, ruff for linting, and pytest-django for testing.

user-invokable: true
license: MIT
metadata:
  author: lgzarturo
  category: django

compatibility:
  tools: [claude, codex, gemini, agy, opencode]
  stacks:
    languages: []
    frameworks: []

risk:
  level: medium
  can_execute_shell: false
  can_modify_files: true
  requires_network: false

inputs: []

outputs: []

quality:
  reviewed_by: codeconductor-core
  version: 0.1.0
---



# Django + UV

## Project Setup with UV

UV replaces pip, pip-tools, virtualenv, and pyenv in a single binary. Always
use UV for dependency management.

```bash
# Create project
uv init my-project
cd my-project

# Add Django and dependencies
uv add django psycopg[binary] djangorestframework django-environ

# Add dev dependencies
uv add --dev pytest pytest-django ruff factory-boy coverage

# Run management commands
uv run python manage.py migrate
uv run python manage.py runserver
```

### pyproject.toml Layout

```toml
[project]
name = "my-project"
version = "0.1.0"
requires-python = ">=3.12"
dependencies = [
    "django>=5.0",
    "psycopg[binary]>=3.1",
    "djangorestframework>=3.15",
    "django-environ>=0.11",
]

[project.optional-dependencies]
dev = [
    "pytest>=8.0",
    "pytest-django>=4.8",
    "ruff>=0.4",
    "factory-boy>=3.3",
    "coverage>=7.4",
]

[tool.ruff]
line-length = 88
target-version = "py312"

[tool.ruff.lint]
select = ["E", "F", "I", "N", "UP", "B", "SIM"]
ignore = ["E501"]

[tool.pytest.ini_options]
DJANGO_SETTINGS_MODULE = "config.settings.test"
python_files = ["test_*.py", "*_test.py"]
python_classes = ["Test*"]
python_functions = ["test_*"]

[tool.coverage.run]
source = ["."]
omit = ["*/migrations/*", "*/tests/*", "manage.py"]
```

## App Structure (Domain-Driven)

Organize by domain, not by technical layer. Each Django app owns its domain
entirely.

```text
my_project/
  config/
    settings/
      base.py       — shared settings
      dev.py        — local overrides
      test.py       — test-specific settings
      prod.py       — production settings
    urls.py
    wsgi.py
  apps/
    users/
      models.py
      services.py   — business logic, never in views
      views.py      — thin; delegates to services
      serializers.py
      admin.py
      urls.py
      tests/
        test_models.py
        test_services.py
        test_views.py
        factories.py
    orders/
      ...
  manage.py
  pyproject.toml
```

## Settings Split

```python
# config/settings/base.py
from pathlib import Path
import environ

env = environ.Env()
BASE_DIR = Path(__file__).resolve().parent.parent.parent

SECRET_KEY = env("SECRET_KEY")
DEBUG = env.bool("DEBUG", default=False)

INSTALLED_APPS = [
    "django.contrib.admin",
    "django.contrib.auth",
    "django.contrib.contenttypes",
    "rest_framework",
    "apps.users",
    "apps.orders",
]

DATABASES = {
    "default": env.db("DATABASE_URL")
}
```

```python
# config/settings/test.py
from .base import *  # noqa: F403

DATABASES = {
    "default": {
        "ENGINE": "django.db.backends.sqlite3",
        "NAME": ":memory:",
    }
}

# Never send real emails in tests
EMAIL_BACKEND = "django.core.mail.backends.locmem.EmailBackend"
```

## Models

```python
# apps/users/models.py
import uuid
from django.db import models
from django.contrib.auth.models import AbstractUser


class User(AbstractUser):
    id = models.UUIDField(primary_key=True, default=uuid.uuid4, editable=False)
    email = models.EmailField(unique=True)
    bio = models.TextField(blank=True)
    created_at = models.DateTimeField(auto_now_add=True)
    updated_at = models.DateTimeField(auto_now=True)

    USERNAME_FIELD = "email"
    REQUIRED_FIELDS = ["username"]

    class Meta:
        ordering = ["-created_at"]

    def __str__(self) -> str:
        return self.email
```

Rules:

- Use UUID primary keys — never expose sequential integer IDs in public APIs
- `auto_now_add` and `auto_now` for timestamps — never set manually
- Always define `__str__` — it appears in admin and error messages
- Use `blank=True` for optional strings (form validation), `null=True` only for
  nullable FK/non-string fields

## Service Layer

Business logic lives in `services.py`. Views are thin HTTP adapters.

```python
# apps/users/services.py
from django.db import transaction
from django.contrib.auth.hashers import make_password
from .models import User


class UserService:

    @staticmethod
    def create_user(*, email: str, password: str, username: str) -> User:
        if User.objects.filter(email=email).exists():
            raise ValueError(f"Email {email!r} is already registered")

        with transaction.atomic():
            user = User.objects.create(
                email=email,
                username=username,
                password=make_password(password),
            )
        return user

    @staticmethod
    def deactivate_user(*, user_id: str) -> User:
        user = User.objects.get(id=user_id)
        user.is_active = False
        user.save(update_fields=["is_active", "updated_at"])
        return user
```

Rules:

- Use keyword-only arguments (`*` separator) in service methods — prevents
  positional argument confusion
- Use `update_fields` in `save()` — avoids overwriting unintended fields and
  reduces the UPDATE query scope
- Wrap multi-step writes in `transaction.atomic()`
- Never import from `views.py` in `services.py` — keep the dependency direction
  strict: views → services → models

## Views (DRF)

```python
# apps/users/views.py
from rest_framework import status
from rest_framework.decorators import api_view, permission_classes
from rest_framework.permissions import IsAuthenticated
from rest_framework.request import Request
from rest_framework.response import Response

from .serializers import CreateUserSerializer, UserSerializer
from .services import UserService


@api_view(["POST"])
def create_user(request: Request) -> Response:
    serializer = CreateUserSerializer(data=request.data)
    serializer.is_valid(raise_exception=True)

    user = UserService.create_user(**serializer.validated_data)
    return Response(UserSerializer(user).data, status=status.HTTP_201_CREATED)


@api_view(["DELETE"])
@permission_classes([IsAuthenticated])
def deactivate_user(request: Request, user_id: str) -> Response:
    UserService.deactivate_user(user_id=user_id)
    return Response(status=status.HTTP_204_NO_CONTENT)
```

## Testing with pytest-django

### Test Factories

```python
# apps/users/tests/factories.py
import factory
from factory.django import DjangoModelFactory
from apps.users.models import User


class UserFactory(DjangoModelFactory):
    class Meta:
        model = User

    email = factory.Sequence(lambda n: f"user{n}@example.com")
    username = factory.Sequence(lambda n: f"user{n}")
    password = factory.PostGenerationMethodCall("set_password", "testpass123")
    is_active = True
```

### Service Tests

```python
# apps/users/tests/test_services.py
import pytest
from apps.users.services import UserService


@pytest.mark.django_db
class TestUserService:

    def test_create_user_succeeds_with_valid_data(self):
        user = UserService.create_user(
            email="new@example.com",
            password="secure123",
            username="newuser",
        )

        assert user.pk is not None
        assert user.email == "new@example.com"
        assert user.check_password("secure123")

    def test_create_user_raises_when_email_exists(self, user_factory):
        existing = user_factory(email="taken@example.com")

        with pytest.raises(ValueError, match="already registered"):
            UserService.create_user(
                email=existing.email,
                password="any",
                username="other",
            )
```

### View Tests

```python
# apps/users/tests/test_views.py
import pytest
from django.urls import reverse


@pytest.mark.django_db
class TestCreateUser:

    def test_returns_201_with_valid_payload(self, client):
        response = client.post(
            reverse("users:create"),
            data={"email": "new@example.com", "password": "secure123", "username": "new"},
            content_type="application/json",
        )

        assert response.status_code == 201
        assert response.data["email"] == "new@example.com"

    def test_returns_400_with_invalid_email(self, client):
        response = client.post(
            reverse("users:create"),
            data={"email": "not-an-email", "password": "x", "username": "y"},
            content_type="application/json",
        )

        assert response.status_code == 400

    def test_returns_400_when_email_already_exists(self, client, user_factory):
        user_factory(email="taken@example.com")

        response = client.post(
            reverse("users:create"),
            data={"email": "taken@example.com", "password": "pass", "username": "z"},
            content_type="application/json",
        )

        assert response.status_code == 400
```

### conftest.py

```python
# conftest.py
import pytest
from pytest_factoryboy import register
from apps.users.tests.factories import UserFactory

register(UserFactory)   # makes user_factory fixture available everywhere
```

## Linting

```bash
# Check
uv run ruff check .

# Fix auto-fixable issues
uv run ruff check --fix .

# Format
uv run ruff format .
```

Run ruff in CI on every pull request. Do not merge code that fails lint.
