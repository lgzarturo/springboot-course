---
description: Genera un commit en español siguiendo Conventional Commits basado en los cambios en staging.
---

// turbo-all

## Pasos

1. Obtén los cambios en staging:
```bash
git diff --cached
```

2. Analiza el diff y genera un mensaje de commit siguiendo estrictamente las reglas definidas en `.agent/rules/commit-style.md`.

3. Ejecuta el commit:
```bash
git commit -m "<mensaje generado>"
```

El mensaje debe estar completamente en español neutro y respetar el límite de 69 caracteres en el encabezado.