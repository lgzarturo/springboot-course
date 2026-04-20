---
description: Formatea todo el código según las reglas del proyecto (ktlint, ruff, prettier, etc.)
---

Detecta el stack y ejecuta el formateador:
- Kotlin → ./gradlew ktlintFormat
- Python → uv run ruff format . && uv run ruff check --fix
- Next.js / TypeScript / Astro → npm run format

Ejecuta el comando correspondiente y muestra un resumen de archivos modificados.
