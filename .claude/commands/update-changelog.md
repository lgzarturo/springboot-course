---
description: Actualiza CHANGELOG.md con los commits recientes siguiendo formato Conventional Commits + Keep a Changelog
---

Lee los commits desde el último tag (`git log --oneline $(git describe --tags --abbrev=0 2>/dev/null || echo "")..HEAD`).
Agrúpalos por tipo (Added, Changed, Fixed, etc.).
Actualiza o crea CHANGELOG.md en la raíz siguiendo el formato Keep a Changelog (https://keepachangelog.com).
Mantén la sección "Unreleased" primero y añade una nueva sección con la fecha de hoy.
Usa el skill @code-review si necesitas validar el formato.
