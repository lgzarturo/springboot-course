---
name: commit
description: Genera un commit en español siguiendo Conventional Commits basado en los cambios staged
---

// turbo-all

## Pasos

1. Ver cambios staged: `git diff --cached --stat`
2. Si no hay nada staged, sugiere `git add .` o archivos específicos
3. Obtener diff completo: `git diff --cached`
4. Ver estilo reciente: `git log -n 3 --oneline`
5. Generar mensaje siguiendo `.agents/rules/commit-style.md`
6. Mostrar propuesta y confirmar con el usuario
7. Ejecutar: `git commit -m "<mensaje>"`
8. Confirmar éxito: `git status`
