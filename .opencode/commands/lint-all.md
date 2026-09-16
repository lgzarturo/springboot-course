---
description: Ejecuta linting completo según el stack del proyecto (Kotlin, Python, TypeScript, Astro)
---

Detecta el stack del proyecto y ejecuta el linter correspondiente:
- Spring Boot + Kotlin → ./gradlew ktlintCheck o detekt
- Python + UV (Django/FastAPI) → uv run ruff check . --fix
- Next.js / TypeScript → npm run lint o eslint .
- Astro → npm run lint

Muestra solo los errores y sugerencias importantes.
Al final, sugiere ejecutar /format-all si es necesario.
