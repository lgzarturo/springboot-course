# 🔧 Guía para Corregir el Historial de Commits

Esta guía te ayudará a corregir los errores de commitlint encontrados en tu
historial de Git.

## 📋 Tabla de Contenidos

- [Errores Detectados](#errores-detectados)
- [⚠️ Advertencias Importantes](#️-advertencias-importantes)
- [Solución 1: Reescribir Commits Individuales](#solución-1-reescribir-commits-individuales)
- [Solución 2: Usar un Script Automatizado](#solución-2-usar-un-script-automatizado)
- [Validación de Cambios](#validación-de-cambios)
- [Mejores Prácticas](#mejores-prácticas)

---

## Errores Detectados

### Error 1: Líneas del cuerpo demasiado largas

```
⧗   input: docs: correcciones menores en documentación

Se realizan ajustes en la documentación para estandarizar términos y corregir errores tipográficos en `DEVELOPMENT_GUIDE.md`, `ARCHITECTURE.md`, `CONTRIBUTING.md` y otros. También se actualiza `.gitignore` para mayor consistencia.
✖   body's lines must not be longer than 200 characters [body-max-line-length]
```

**Problema**: La línea del cuerpo excede los 200 caracteres.

### Error 2: Merge commit sin subject/type

```
⧗   input: Feature/issue 1 readme update (#8)
✖   subject may not be empty [subject-empty]
✖   type may not be empty [type-empty]
```

**Problema**: Los merge commits deben seguir el formato convencional.

---

## ⚠️ Advertencias Importantes

### 🚨 ANTES DE COMENZAR

1. **Backup**: Crea una rama de respaldo antes de reescribir el historial

   ```bash
   git branch backup-before-rebase
   ```

2. **Coordinación**: Si trabajas en equipo, coordina con tu equipo antes de
   reescribir el historial.

3. **Force Push**: Reescribir el historial requiere `git push --force`, lo cual
   puede afectar a otros colaboradores.

4. **Ramas Remotas**: Solo reescribe el historial en ramas que no hayan sido
   compartidas o en ramas de desarrollo.

### ⚠️ NUNCA reescribas el historial en:

- La rama `main` o `master` si otros desarrolladores la están usando
- Ramas públicas que otros han clonado
- Commits que ya están en producción

---

## Solución 1: Reescribir Commits Individuales

### Paso 1: Identificar los commits problemáticos

```bash
# Ver el historial de commits
git log --oneline

# Ver un commit específico con su mensaje completo
git log --format=%B -n 1 <commit-hash>
```

### Paso 2: Iniciar un rebase interactivo

```bash
# Rebase desde los últimos N commits
git rebase -i HEAD~10

# O desde un commit específico
git rebase -i <commit-hash>^
```

### Paso 3: Marcar commits para editar

En el editor que se abre, cambia `pick` por `reword` (para cambiar el mensaje) o
`edit` (para modificar el commit):

```
reword a1b2c3d docs: correcciones menores en documentación
reword e4f5g6h Feature/issue 1 readme update (#8)
pick i7j8k9l feat: implementar PingController
```

### Paso 4: Corregir cada commit

#### Para el Error 1 (líneas largas):

Cuando Git te muestre el editor para el primer commit, reescribe el mensaje
dividiendo las líneas:

**Formato correcto:**

```
docs: correcciones menores en documentación

Se realizan ajustes en la documentación para estandarizar
términos y corregir errores tipográficos en:
- DEVELOPMENT_GUIDE.md
- ARCHITECTURE.md
- CONTRIBUTING.md

También se actualiza `.gitignore` para mayor consistencia.
```

#### Para el Error 2 (merge commit sin formato):

Reescribe el merge commit con formato convencional:

**Formato correcto:**

```
chore: merge feature/issue-1-readme-update (#8)

Integra actualizaciones del README y nueva documentación:
- Configuración de ktlint, detekt y jacoco
- Material completo para week-00 y week-02
- Mejoras en pruebas unitarias e integración
- Implementación de PingController y constantes
```

### Paso 5: Continuar el rebase

```bash
# Después de cada corrección
git rebase --continue

# Si hay conflictos, resuélvelos y luego
git add .
git rebase --continue

# Si quieres cancelar el rebase
git rebase --abort
```

### Paso 6: Verificar los cambios

```bash
# Ver el nuevo historial
git log --oneline --graph --decorate

# Validar con commitlint (si tienes configurado)
npx commitlint --from HEAD~10 --to HEAD
```

### Paso 7: Actualizar el repositorio remoto

```bash
# Antes de hacer force push, asegúrate de tener un backup
git branch backup-$(date +%Y%m%d)

# Force push (usa con precaución)
git push --force-with-lease origin <nombre-rama>
```

---

## Solución 2: Usar un Script Automatizado

### Script de PowerShell para Windows

Crea un archivo `fix-commits.ps1`:

```powershell
# fix-commits.ps1
# Script para corregir mensajes de commits con errores de commitlint

$ErrorActionPreference = "Stop"

Write-Host "==================================================" -ForegroundColor Cyan
Write-Host "  Script de Corrección de Commits" -ForegroundColor Cyan
Write-Host "==================================================" -ForegroundColor Cyan
Write-Host ""

# Crear backup
$backupBranch = "backup-$(Get-Date -Format 'yyyyMMdd-HHmmss')"
Write-Host "Creando rama de backup: $backupBranch" -ForegroundColor Yellow
git branch $backupBranch

Write-Host ""
Write-Host "Commits problemáticos encontrados:" -ForegroundColor Red
Write-Host "1. docs: correcciones menores en documentación" -ForegroundColor Yellow
Write-Host "   - Cuerpo con líneas demasiado largas" -ForegroundColor Gray
Write-Host ""
Write-Host "2. Feature/issue 1 readme update (#8)" -ForegroundColor Yellow
Write-Host "   - Merge commit sin formato convencional" -ForegroundColor Gray
Write-Host ""

$confirm = Read-Host "¿Deseas continuar con la corrección? (s/n)"
if ($confirm -ne "s") {
    Write-Host "Operación cancelada" -ForegroundColor Red
    exit 0
}

Write-Host ""
Write-Host "Iniciando rebase interactivo..." -ForegroundColor Green
Write-Host "Por favor, marca los commits con 'reword' en el editor" -ForegroundColor Yellow
Write-Host ""

# Instrucciones
Write-Host "INSTRUCCIONES:" -ForegroundColor Cyan
Write-Host "1. Se abrirá un editor con la lista de commits" -ForegroundColor White
Write-Host "2. Cambia 'pick' por 'reword' en los commits problemáticos" -ForegroundColor White
Write-Host "3. Guarda y cierra el editor" -ForegroundColor White
Write-Host "4. Para cada commit, corrige el mensaje cuando se te solicite" -ForegroundColor White
Write-Host ""

Read-Host "Presiona Enter para continuar"

# Iniciar rebase interactivo
git rebase -i HEAD~10

Write-Host ""
Write-Host "==================================================" -ForegroundColor Green
Write-Host "  Rebase completado" -ForegroundColor Green
Write-Host "==================================================" -ForegroundColor Green
Write-Host ""
Write-Host "Verificando commits..." -ForegroundColor Yellow

# Mostrar los últimos commits
git log --oneline --graph --decorate -10

Write-Host ""
Write-Host "Si los cambios son correctos, ejecuta:" -ForegroundColor Cyan
Write-Host "  git push --force-with-lease origin <nombre-rama>" -ForegroundColor White
Write-Host ""
Write-Host "Si necesitas revertir los cambios:" -ForegroundColor Yellow
Write-Host "  git reset --hard $backupBranch" -ForegroundColor White
```

### Uso del script:

```powershell
# En PowerShell
.\fix-commits.ps1
```

---

## Validación de Cambios

### Verificar formato de commits

```bash
# Instalar commitlint (si no está instalado)
npm install -g @commitlint/cli @commitlint/config-conventional

# Validar los últimos 10 commits
npx commitlint --from HEAD~10 --to HEAD

# Validar todos los commits desde un tag
npx commitlint --from v1.0.0 --to HEAD
```

### Verificar longitud de líneas

```bash
# Script para verificar longitud de líneas en commits
git log --format=%B -10 | awk 'length > 200 {print "Línea larga encontrada: " substr($0, 1, 50) "..."}'
```

---

## Mejores Prácticas

### 1. Formato de Commit Convencional

```
<type>(<scope>): <subject>

<body>

<footer>
```

**Tipos válidos:**

- `feat`: Nueva funcionalidad
- `fix`: Corrección de errores
- `docs`: Cambios en documentación
- `style`: Formateo, punto y coma faltantes, etc.
- `refactor`: Refactorización de código
- `test`: Agregar o modificar tests
- `chore`: Mantenimiento, configuración, etc.

### 2. Reglas para el Mensaje

- **Subject**: Máximo 100 caracteres
- **Body**: Cada línea máximo 200 caracteres
- **Separación**: Línea en blanco entre subject y body
- **Descripción**: Usar viñetas o listas para claridad

### 3. Ejemplo de Commit Correcto

```
docs: actualizar guía de contribución

Se realizan las siguientes mejoras en la documentación:
- Agregar sección de commits convencionales
- Incluir ejemplos de mensajes correctos
- Actualizar guía de configuración de Git hooks

Estos cambios mejoran la claridad para nuevos
contribuidores y estandarizan el proceso.

Closes #123
```

### 4. Para Merge Commits

```
chore: merge feature/nueva-funcionalidad (#42)

Integra nueva funcionalidad de autenticación:
- Implementación de JWT tokens
- Tests de integración
- Documentación de API
```

### 5. Uso de Git Hooks

Configura git hooks para validar commits antes de hacerlos:

```bash
# Instalar husky
npm install --save-dev husky @commitlint/cli @commitlint/config-conventional

# Activar hooks
npx husky install

# Agregar hook de commit-msg
npx husky add .husky/commit-msg 'npx --no -- commitlint --edit "$1"'
```

---

## Recursos Adicionales

- [Conventional Commits](https://www.conventionalcommits.org/)
- [Commitlint Documentation](https://commitlint.js.org/)
- [Git Rebase Documentation](https://git-scm.com/docs/git-rebase)
- [Semantic Versioning](https://semver.org/)

---

## Solución para tu Caso Específico

### Commit 1: "docs: correcciones menores en documentación"

```bash
# Encontrar el hash del commit
git log --oneline | grep "correcciones menores"

# Iniciar rebase
git rebase -i <hash-anterior>^

# En el editor, cambiar 'pick' por 'reword'
# Luego guardar este mensaje:
```

**Nuevo mensaje:**

```
docs: correcciones menores en documentación

Se realizan ajustes en la documentación para
estandarizar términos y corregir errores tipográficos en:
- DEVELOPMENT_GUIDE.md
- ARCHITECTURE.md
- CONTRIBUTING.md

También se actualiza `.gitignore` para mayor consistencia.
```

### Commit 2: "Feature/issue 1 readme update (#8)"

```bash
# Este es un merge commit, necesita formato convencional
# En el rebase, marca con 'reword'
# Nuevo mensaje:
```

**Nuevo mensaje:**

```
chore: merge feature/issue-1-readme-update (#8)

Integra actualizaciones de documentación y funcionalidades:

## Cambios incluidos:
- Configuración de ktlint, detekt y jacoco
- Material completo para week-00 y week-02 sobre
  estrategias de aprendizaje y Spring Boot
- Mejoras en pruebas unitarias e integración con
  nuevas aserciones en PingServiceTest
- Implementación de PingController, PingResponse,
  PingUseCase y PingService con endpoints completos
- Constantes globales en AppConstants con
  configuración CORS y mensajes estándar
```

---

## ✅ Checklist Final

Antes de considerar que el trabajo está completo:

- [ ] Crear rama de backup
- [ ] Identificar todos los commits con errores
- [ ] Reescribir mensajes siguiendo formato convencional
- [ ] Validar con commitlint
- [ ] Verificar longitud de líneas (máx 200 caracteres)
- [ ] Revisar historial con `git log --graph`
- [ ] Coordinar con el equipo antes de force push
- [ ] Ejecutar `git push --force-with-lease`
- [ ] Verificar que CI/CD pasa correctamente
- [ ] Eliminar rama de backup si todo está correcto

---

## 🆘 Ayuda

Si encuentras problemas durante el proceso:

1. **No entres en pánico**: Siempre tienes tu rama de backup
2. **Cancela el rebase**: `git rebase --abort`
3. **Vuelve al estado anterior**: `git reset --hard backup-branch`
4. **Pide ayuda**: Abre un issue en el repositorio con detalles del problema

---

**Última actualización**: 2025-10-20

**Nota**: Esta guía es parte del proyecto Spring Boot Course y está diseñada
para ayudarte a mantener un historial de commits limpio y profesional.
