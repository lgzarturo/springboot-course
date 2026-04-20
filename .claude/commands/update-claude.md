---
description: Actualiza el archivo CLAUDE.md agregando las reglas de approach si no existen
---

Lee el archivo CLAUDE.md y verifica si ya contiene las reglas de approach.
Si el archivo no existe o no contiene estas reglas, las agrega al final usando append.

Las reglas a verificar/agregar son:

```markdown
# Approach

- Think before acting. Read existing files before writing code.
- Be concise in output but thorough in reasoning.
- Prefer editing over rewriting whole files.
- Do not re-read files you have already read unless the file may have changed.
- Skip files over 100KB unless explicitly required.
- Suggest running /cost when a session is running long to monitor cache ratio.
- Recommend starting a new session when switching to an unrelated task.
- Test your code before declaring done.
- No sycophantic openers or closing fluff.
- Keep solutions simple and direct.
- User instructions always override this file.
- When using tools, be precise and minimal with context.
```

NUNCA sobrescribas el archivo completo. Solo agrega el contenido faltante al final si no existe.
Muestra un resumen de lo que se agregó o indica si todo ya estaba presente.
