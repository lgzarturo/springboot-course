---
id: fastapi-pydantic-strict
name: fastapi-pydantic-strict
description: >
  Enforces Pydantic v2 strict models, asynchronous dependency generators, and uv environment setup.
user-invokable: true
license: MIT
metadata:
  author: lgzarturo
  category: validation
compatibility:
  tools: [claude, codex, gemini, agy, opencode]
  stacks:
    languages: [python]
    frameworks: [fastapi, pydantic]
---
# FastAPI Pydantic Strict

## Core Principles

1. **Pydantic v2 Model Configuration**: Always configure models with `model_config = ConfigDict(strict=True)` to enforce type correctness.
2. **Async Dependencies**: Rely on async FastAPI dependency generators (`async def get_db()`) to handle context managers correctly.
3. **Environment Management**: Use `uv` for lightning-fast package installs and lockfile consistency.

## Code Blueprint

```python
from fastapi import FastAPI, Depends
from pydantic import BaseModel, ConfigDict, Field

app = FastAPI()

class StrictUser(BaseModel):
    model_config = ConfigDict(strict=True)
    
    id: int
    name: str = Field(..., min_length=2)
    email: str

@app.post("/users", response_model=StrictUser)
async def create_user(user: StrictUser):
    return user
```
