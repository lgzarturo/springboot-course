---
trigger: always_on
---

---
description: Reglas para generar mensajes de commit en español con formato Conventional Commits.
globs: ["**/*"]
---

## Git Commit Messages

Cuando generes mensajes de commit DEBES seguir estas reglas sin excepción:

### Idioma
- Todo el mensaje debe estar en **español neutro**.

### Formato obligatorio (Conventional Commits)
```
<tipo>(<scope opcional>): <descripción corta>

- <detalle 1>
- <detalle 2>

Footer opcional: referencias a issues, breaking changes, etc.
```

### Tipos válidos
feat, fix, docs, style, refactor, test, chore, perf, ci, build, revert

### Límites
- Primera línea (encabezado): máximo 69 caracteres
- Cuerpo: viñetas concisas, una idea por línea
- Footer: solo si hay breaking change o referencia a issue

### Nunca
- No uses gerundios ("agregando", "corrigiendo")
- No pongas punto final en el encabezado
- No inventes tipos fuera de la lista válida

---