---
description: Analiza git staging (git add .) y crea un commit siguiendo Conventional Commits (feat, fix, chore, etc.)
---

Analiza exactamente los cambios en el staging (`git diff --staged` y `git status`).
Genera un mensaje de commit que siga el estándar Conventional Commits:
- feat: nueva funcionalidad → minor
- fix: corrección de bug → patch
- chore: mantenimiento, docs, refactor → sin bump
- breaking change: usa "BREAKING CHANGE:" en el body → major

Formato exacto:
<type>(<scope opcional>): <título corto y descriptivo>
<línea en blanco>
<body detallado si es necesario>
<línea en blanco>
BREAKING CHANGE: (solo si aplica)

Después de generar el mensaje, muéstramelo y pregúntame si quiero que ejecutes `git commit -m "mensaje"` automáticamente.
Nunca hagas el commit sin mi confirmación explícita.
