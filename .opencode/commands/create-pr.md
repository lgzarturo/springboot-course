---
description: Crea un Pull Request en GitHub con título, descripción y labels basados en los cambios
---

Analiza los cambios en la rama actual (`git diff main...HEAD` o rama base).
Genera:
- Título siguiendo Conventional Commits
- Descripción completa con:
  - ¿Qué cambia?
  - ¿Por qué?
  - Breaking changes (si aplica)
  - Cómo probarlo
- Labels automáticas (feat, fix, chore, breaking, etc.)

Usa `gh pr create` para crear el PR.
Pregúntame antes de ejecutar cualquier comando que modifique GitHub.
