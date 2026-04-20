---
description: Analiza git staging (git add .) y crea un commit siguiendo Conventional Commits (feat, fix, chore, etc.)
---

Analiza exactamente los cambios en el staging (`git diff --staged` y `git status`).
Genera un mensaje de commit que siga el estándar Conventional Commits:
- feat: nueva funcionalidad → minor
- fix: corrección de bug → patch
- chore: mantenimiento, docs, refactor → sin bump
- breaking change: usa "BREAKING CHANGE:" en el body → major

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
- BREAKING CHANGE: (solo si aplica)

### Nunca

- No uses gerundios ("agregando", "corrigiendo")
- No pongas punto final en el encabezado
- No inventes tipos fuera de la lista válida

Después de generar el mensaje, muéstramelo y pregúntame si quiero que ejecutes `git commit -m "mensaje"` automáticamente.
Nunca hagas el commit sin mi confirmación explícita.
