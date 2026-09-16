---
description: Ejecuta el flujo completo de release: commit convencional + changelog + bump versión + tag + PR
---

Ejecuta en este orden (con confirmación en cada paso):
1. /conventional-commit
2. /update-changelog
3. /bump-version (pregunta si major/minor/patch o detecta automáticamente)
4. git push && git push --tags
5. /create-pr (opcional)

Al final, resume todo lo realizado.
