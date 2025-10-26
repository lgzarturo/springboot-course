# 📝 Guía Rápida de Commits y Versionado

**Guía definitiva para commits, versionado y releases en este proyecto**: Aprende a usar Conventional Commits, Semantic Versioning y el flujo de trabajo completo

> **Referencia rápida para commits con Conventional Commits**
>
> Para documentación completa, consulta [WORKFLOW.md](WORKFLOW.md)

---

## 📋 Tabla de Contenidos

- [Estructura de Commits](#estructura-de-commits)
- [Tipos de Commit y Versionado](#tipos-de-commit-y-versionado)
- [Flujo de Trabajo con Branches](#flujo-de-trabajo-con-branches)
- [Control de Workflows de GitHub](#control-de-workflows-de-github)
- [Proceso de Releases](#proceso-de-releases)
- [Tipos de Releases](#tipos-de-releases)
- [Cálculo Automático de Versión](#cálculo-automático-de-versión)
- [Gestión de Tags](#gestión-de-tags)
- [Comandos Útiles](#comandos-útiles)
- [Errores Comunes](#errores-comunes)
- [Referencias a Issues](#referencias-a-issues)
- [Plantilla de Commit](#plantilla-de-commit)
- [Flujo Rápido](#flujo-rápido)
- [Documentación y Changelog](#documentación-y-changelog)
- [Estructura del Changelog](#estructura-del-changelog)
- [Ejemplos Prácticos](#ejemplos-prácticos)
- [Alcances Comunes](#alcances-comunes)
- [Recursos](#recursos)
- [Ayuda](#ayuda)

---

## Estructura de Commits

```
<tipo>(alcance): <descripción>

[cuerpo opcional]

[footer opcional]
```

### Componentes

- **tipo**: Categoría del cambio (obligatorio)
- **alcance**: Módulo o área afectada (opcional pero recomendado)
- **descripción**: Resumen breve en imperativo (obligatorio)
- **cuerpo**: Explicación detallada (opcional)
- **footer**: Referencias a `issues`, breaking changes (opcional)

---

## Tipos de Commit y Versionado

Este proyecto usa **Semantic Versioning (SemVer)**: `MAJOR.MINOR.PATCH`

### Tipos que Generan Release

| Tipo       | Descripción                               | Incrementa | Ejemplo        |
|------------|-------------------------------------------|------------|----------------|
| `feat`     | Nueva funcionalidad                       | MINOR      | 1.0.0 → 1.1.0  |
| `fix`      | Corrección de bugs                        | PATCH      | 1.0.0 → 1.0.1  |
| `perf`     | Mejoras de rendimiento                    | PATCH      | 1.0.0 → 1.0.1  |
| `refactor` | Refactorización de código                 | PATCH      | 1.0.0 → 1.0.1  |
| `build`    | Cambios en sistema de build/dependencias  | PATCH      | 1.0.0 → 1.0.1  |
| `feat!`    | Breaking change (cualquier tipo con `!`)  | MAJOR      | 1.0.0 → 2.0.0  |

### Tipos que NO Generan Release

| Tipo

| Descripción                          | Uso                           |
|-----------|--------------------------------------|-------------------------------|
| `docs`    | Solo documentación                   | README, guías, comentarios    |
| `style`   | Formato, espacios, lint              | Prettier, formato de código   |
| `test`    | Agregar o modificar tests            | Unit tests, integration tests |
| `ci`      | Cambios en CI/CD                     | GitHub Actions, workflows     |
| `chore`   | Tareas de mantenimiento              | Configuración, scripts        |

### Breaking Changes

Cualquier commit con `!` o `BREAKING CHANGE:` incrementa la versión MAJOR:

```bash
# Opción 1: Usar ! después del tipo
git commit -m "feat(api)!: cambiar estructura de respuesta"

# Opción 2: Usar BREAKING CHANGE en el footer
git commit -m "feat(api): cambiar estructura de respuesta

BREAKING CHANGE: El endpoint /users ahora retorna un objeto
en lugar de un array. Actualizar clientes para usar response

.data"

---

## Tipos de Commit

| Tipo       | Cuándo Usarlo                             | Versión |
|------------|-------------------------------------------|---------|
| `feat`     | Nueva funcionalidad                       | 0.X.0   |
| `fix`      | Corrección de bugs                        | 0.0.X   |
| `docs`     | Solo documentación                        | -       |
| `style`    | Formato, espacios (sin cambios de código) | -       |
| `refactor` | Refactorización                           | -       |
| `perf`     | Mejoras de rendimiento                    | 0.0.X   |
| `test`     | Tests                                     | -       |
| `build`    | Sistema de build, dependencias            | -       |
| `ci`       | CI/CD                                     | -       |
| `chore`    | Mantenimiento                             | -       |

---

## Ejemplos Rápidos

### ✅ Buenos Commits

```bash
feat(reservas): agregar endpoint para crear reservas
fix(pagos): corregir cálculo de impuestos
docs(readme): actualizar sección de instalación
test(controller): agregar tests para PingController
refactor(service): extraer lógica de validación
perf(query): optimizar consulta de disponibilidad
```

### Con Cuerpo

```bash
git commit -m "feat(api): implementar paginación

- Agregar parámetros page, size y sort
- Actualizar DTOs con metadata de paginación
- Documentar endpoints en OpenAPI

Closes #42"
```

### Breaking Change

```bash
git commit -m "feat(auth)!: cambiar estructura de respuesta

BREAKING CHANGE: El token ahora se retorna en un objeto
con accessToken y refreshToken en lugar de string simple"
```

---

### Alcances Comunes

- `api` - Cambios en API REST
- `domain` - Lógica de dominio/negocio
- `service` - Servicios
- `controller` - Controladores
- `repository` - Repositorios
- `model` - Modelos/DTO
- `config` - Configuración
- `test` - Tests
- `docs` - Documentación
- Nombres de módulos específicos: `reservas`, `pagos`, `habitaciones`, etc.

---

## Flujo de Trabajo con Branches

```plaintext
main (producción)
  ↑
  └── develop (desarrollo)
        ↑
        └── feature/nueva-funcionalidad
        └── fix/corregir-bug
        └── refactor/mejorar-codigo
```

### Estrategia de versionado

| Tipo           | Tipo de release          | Incrementa    | Cuando Usar                   |
|----------------|--------------------------|---------------|-------------------------------|
| `main`         | Estable                  | v1.0.0        | Listo para producción         |
| `develop`      | Beta/Pre-release         | v1.0.0-beta.1 | Pruebas antes de integración  |
| `feature/*`    | Sin release              | -             | Desarrollo de nuevas features |

### Flujo completo

```bash
# 1. Crear feature desde develop
git checkout develop
git pull origin develop
git checkout -b feature/agregar-autenticacion

# 2. Hacer commits con conventional commits
git add .
git commit -m "feat(auth): implementar login con JWT"

git commit -m "test(auth): agregar tests de autenticación"
git commit -m "docs(auth): documentar endpoints de auth"

# 3. Push y crear PR a develop
git push -u origin feature/agregar-autenticacion

# Crear PR en GitHub: feature/agregar-autenticación → develop

# 4. Después del merge, crear release beta (manual)
# Ir a GitHub Actions → Release → Run workflow (branch: develop)
# Esto crea: v1.1.0-beta.1

# 5. Cuando esté listo, PR de develop a main
# Crear PR en GitHub: develop → main

# 6. Después del merge, crear release estable (manual)
# Ir a GitHub Actions → Release → Run workflow (branch: main)
# Esto crea: v1.1.0
```

---

## Control de Workflows de GitHub

Evitar Ejecuciones Innecesarias. Usar [skip ci] en el Mensaje de Commit

```bash
# Evita que se ejecuten los workflows de CI
git commit -m "docs: actualizar README [skip ci]"
git commit -m "chore: actualizar .gitignore [skip ci]"

# También funciona con:
[ci skip]
[no ci]
[skip actions]
```

### Cuando usar [skip ci]

✅ **Usar en:**

- Cambios solo de documentación
- Actualizaciones de README, guías
- Cambios en .gitignore, .editorconfig
- Correcciones de typos
- Actualizaciones de comentarios

❌ **NO usar en:**

- Cambios de código fuente
- Modificaciones de tests
- Cambios en dependencias
- Actualizaciones de configuración de build

### Workflows Configurados

1. CI (Integración Continua) - Trigger: Push o PR a main, develop, feature/*

    ```plaintext
    # Se ejecuta automáticamente en:
    - Push a cualquier branch
    - Pull Requests a main o develop
    
    # Ejecuta:
    - Tests unitarios
    - Build de la aplicación
    - Validación de código
    ```

2. Release (Manual)—Trigger: Manual desde GitHub Actions

    ```plaintext
    # Se ejecuta SOLO manualmente:
    - GitHub → Actions → Release → Run workflow
    
    # Ejecuta:
    - Tests
    - Semantic Release (crea tag y release)
    - Build y publica JAR
    - Actualiza CHANGELOG.md
    ```

Ejemplo del flujo completo:

```plaintext
# Commits normales (ejecutan CI)
git commit -m "feat(api): agregar endpoint de usuarios"
git push

# ✅ Ejecuta CI

# Commits de documentación (sin CI)
git commit -m "docs: actualizar guía de instalación [skip ci]"
git push  # ⏭️ Salta CI

# Release (manual)
# 1. Ir a GitHub Actions
# 2. Seleccionar workflow "Release"
# 3. Click en "Run workflow"
# 4. Seleccionar branch (develop o main)
# 5. Click en "Run workflow"
```

---

## Proceso de Releases

> Releases Manuales (Recomendado): Este proyecto usa releases manuales para mantener control total sobre el versionado. Otro objetivo es que los programadores tengan una, guía por medio de las herramientas del repositorio para ver todo lo que se ha realizado en cada versión.


### Paso 1: Checklist Antes de Commit

- [ ] ✅ Tests pasan: `.\gradlew.bat test`
- [ ] ✅ Linter sin errores: `.\gradlew.bat ktlintCheck`
- [ ] ✅ Código revisado: `git diff`
- [ ] ✅ Mensaje sigue Conventional Commits
- [ ] ✅ Descripción clara y específica
- [ ] ✅ Referencia a issue si aplica

### Paso 2: Preparar el Código

```bash
# Asegúrate de estar en el branch correcto
git checkout develop  # Para beta
# o
git checkout main     # Para estable

# Actualiza el branch
git pull origin develop
```

### Paso 3: Verificar Commits

```bash
# Ver commits desde la última release
git log --oneline v1.0.0..HEAD

# Verificar que siguen conventional commits
# Deben tener formato: tipo(alcance): descripción
```

### Paso 4: Ejecutar Release desde GitHub

1. Ve a GitHub → Actions
2. Selecciona el workflow "Release"
3. Click en "Run workflow"
4. Selecciona el branch:

    - develop → Crea release beta (ej: v1.1.0-beta.1)
    - main → Crea release estable (ej: v1.1.0)
    - Click en "Run workflow"

### Paso 5: Verificar el Release

El workflow automáticamente:

✅ Ejecuta todos los tests
✅ Analiza los commits con Semantic Release
✅ Calcula la nueva versión según SemVer
✅ Actualiza build.gradle.kts con la nueva versión
✅ Genera/actualiza CHANGELOG.md
✅ Crea el tag (ej.: v1.1.0)
✅ Crea la GitHub Release con notas
✅ Sube el JAR como asset
✅ Hace commit con [skip ci] para evitar bucles

### Paso 6: Sincronizar Local

```bash
# Actualiza tu repositorio local
git pull origin develop --tags

# Verifica el nuevo tag
git tag -l
```

---

## Tipos de Releases

### Release Beta (develop)

```bash
# Commits en develop:
feat(api): agregar endpoint de búsqueda
fix(auth): corregir validación de token

# Release manual desde develop → v1.1.0-beta.1
```

### Release Estable (main)

```plaintext
# Después de probar en beta:
# 1. PR: develop → main
# 2. Release manual desde main → v1.1.0
```

---

## Cálculo Automático de Versión

Semantic Release analiza los commits y calcula la versión:

```plaintext
# Versión actual: v1.0.0

# Si hay commits tipo "feat":
feat(api): nuevo endpoint
→ v1.1.0 (incrementa MINOR)

# Si solo hay "fix":
fix(bug): corregir validación
→ v1.0.1 (incrementa PATCH)

# Si hay breaking change:
feat(api)!: cambiar estructura
→ v2.0.0 (incrementa MAJOR)

# Si no hay commits que generen release:
docs: actualizar README
→ No crea release
```

---

## Gestión de Tags

Ver Tags

```bash
# Listar todos los tags
git tag

# Listar tags con patrón
git tag -l "v1.*"

# Ver detalles de un tag
git show v1.0.0

# Ver tags remotos
git ls-remote --tags origin
```

Elimina tags incorrectos de forma local

```bash
# Eliminar un tag local
git tag -d v1.0.0-beta.1

# Eliminar múltiples tags locales
git tag -d v1.0.0-beta.1 v1.0.0-beta.2
```

Eliminar un tags de forma remota

```bash
# Opción 1: Usando git push
git push origin :refs/tags/v1.0.0-beta.1

# Opción 2: Usando --delete
git push origin --delete v1.0.0-beta.1

# Eliminar múltiples tags remotos
git push origin --delete v1.0.0-beta.1 v1.0.0-beta.2
```

Elimina realeases de GitHub

```bash
# Usando GitHub CLI (gh)
gh release delete v1.0.0-beta.1 -y

# O manualmente:
# 1. Ve a GitHub → Releases
# 2. Click en el release
# 3. Click en "Delete"
```

---

## Comandos Útiles

```bash
# Ver cambios
git status
git diff

# Commit interactivo
git add -p

# Ver último commit
git log -1

# Modificar último commit (antes de push)
git commit --amend

# Ver historial bonito
git log --oneline --graph --decorate

# Ver commits desde última release
git log --oneline v1.0.0..HEAD

# Listar tags
git tag

# Ver tag específico
git show v1.0.0

# Eliminar tag local
git tag -d v1.0.0

# Eliminar tag remoto
git push origin --delete v1.0.0

# Actualizar tags
git fetch --tags

# Ver releases (con gh CLI)
gh release list

# Ver detalles de release
gh release view v1.0.0

# Eliminar release
gh release delete v1.0.0 -y

# Verificar conventional commits
git log --oneline | grep -E "^[a-z]+(\(.+\))?:"

# Contar commits por tipo
git log --oneline | grep -oE "^[a-z]+" | sort | uniq -c

# Ver commits que generarán release
git log --oneline | grep -E "^(feat|fix|perf|refactor|build)"
```

---

## Errores Comunes

### ❌ Evitar

```bash
git commit -m "cambios"
git commit -m "fix"
git commit -m "WIP"
git commit -m "Actualización"
git commit -m "agregar función"  # Falta tipo
```

#### ❌ Commit sin tipo

```bash
# Mal
git commit -m "agregar función de búsqueda"

# Bien
git commit -m "feat(api): agregar función de búsqueda"
```

#### ❌ Descripción vaga

```bash
# Mal
git commit -m "fix: arreglar bug"

# Bien
git commit -m "fix(auth): corregir validación de token expirado"
```

#### ❌ Múltiples cambios en un solo commit

```bash
# Mal
git commit -m "feat: agregar usuarios y productos"

# Bien
git commit -m "feat(user): agregar gestión de usuarios"
git commit -m "feat(product): agregar catálogo de productos"
```

#### Olvidar [skip ci] en docs

```bash
# Mal (ejecuta CI innecesariamente)
git commit -m "docs: actualizar README"

# Bien
git commit -m "docs: actualizar README [skip ci]"
```

#### Breaking change sin marcar

```bash
# Mal
git commit -m "feat(api): cambiar estructura de respuesta"

# Bien
git commit -m "feat(api)!: cambiar estructura de respuesta

BREAKING CHANGE: La respuesta ahora incluye metadata"
```

### ✅ Correcto

```bash
git commit -m "feat(pagos): agregar validación de tarjeta"
git commit -m "fix(auth): corregir validación de token expirado"
git commit -m "docs(api): actualizar ejemplos de endpoints"
git commit -m "test(service): agregar casos de error"
```

---

## Referencias a Issues

```bash
# Cierra el issue automáticamente
Closes #123
Fixes #456

# Referencia sin cerrar
Refs #789
See #101
```

---

## Plantilla de Commit

Configura la plantilla para ayudarte:

```bash
git config commit.template .gitmessage
```

Luego solo haz `git commit` (sin `-m`) y se abrirá tu editor con la plantilla.

---

## Flujo Rápido

```bash
# 1. Crear rama
git checkout -b feature/mi-feature

# 2. Hacer cambios
# ... editar archivos ...

# 3. Agregar y commit
git add .
git commit -m "feat(modulo): descripción clara"

# 4. Verificar tests
.\gradlew.bat test

# 5. Push
git push -u origin feature/mi-feature

# 6. Crear Pull Request en GitHub
```

---

## Documentación y Changelog

### Changelog Automático

El CHANGELOG.md se genera automáticamente durante el proceso de release.

### Cómo Funciona

1. Semantic Release analiza todos los commits desde la última release
2. Agrupa los commits por tipo
3. Genera las notas de release en formato Markdown
4. Actualiza CHANGELOG.md con la nueva versión
5. Hace commit del changelog con [skip ci]

## Estructura del Changelog

```markdown
# Changelog

## [1.1.0] - 2024-01-15

### ✨ Nuevas Funcionalidades
- **api**: agregar endpoint de búsqueda avanzada (#42)
- **auth**: implementar refresh token (#45)

### 🐛 Correcciones de Bugs
- **validation**: corregir validación de email (#43)
- **database**: solucionar problema de conexión (#44)

### 📦 Refactorizaciones
- **service**: extraer lógica de negocio a servicios (#46)

### 📚 Documentación
- **readme**: actualizar guía de instalación
- **api**: documentar nuevos endpoints

## [1.0.0] - 2024-01-01

### ✨ Nuevas Funcionalidades

- **core**: implementación inicial del proyecto
```

> **Configuración del Changelog:** el formato se configura en `.releaserc.json`

###  Documentar Cambios Importantes

**En el commit**

```bash
# Commit con descripción detallada
git commit -m "feat(api): implementar paginación

- Agregar parámetros page, size y sort
- Actualizar DTOs con metadata de paginación
- Documentar endpoints en OpenAPI
- Agregar tests de integración

Closes #42"
```

**En el PR**

```markdown
## Descripción
Implementa paginación en todos los endpoints de listado.

## Cambios
- ✨ Nuevo: Parámetros de paginación (page, size, sort)
- 📦 Refactor: DTO con PageResponse wrapper
- 📚 Docs: Actualizar OpenAPI specs
- 🚨 Tests: Casos de paginación

## Breaking Changes
Ninguno

## Checklist
- [x] Tests pasan
- [x] Documentación actualizada
- [x] Conventional commits
```

**Mantener la documentación actualizada**

```bash
# Commits de documentación (no generan release)
git commit -m "docs(readme): actualizar guía de instalación [skip ci]"
git commit -m "docs(api): agregar ejemplos de uso [skip ci]"
git commit -m "docs(contributing): crear guía de contribución [skip ci]"

# Estos commits aparecen en el changelog pero no crean release
```

---

## Ejemplos prácticos

### Ejemplo 1: Agregar Características al Sistema

```bash
# 1. Crear branch
git checkout develop
git checkout -b feature/user-management

# 2. Implementar feature con múltiples commits
git commit -m "feat(user): agregar modelo de usuario"
git commit -m "feat(user): implementar repositorio"
git commit -m "feat(user): crear servicio de usuarios"
git commit -m "feat(user): agregar endpoints REST"
git commit -m "test(user): agregar tests unitarios"
git commit -m "test(user): agregar tests de integración"
git commit -m "docs(user): documentar API de usuarios [skip ci]"

# 3. Push y PR
git push -u origin feature/user-management
# Crear PR: feature/user-management → develop

# 4. Después del merge, release beta
# GitHub Actions → Release → Run workflow (develop)
# Resultado: v1.1.0-beta.1

# 5. Pro

bar en beta, luego PR a main
# Crear PR: develop → main

# 6. Release estable
# GitHub Actions → Release → Run workflow (main)
# Resultado: v1.1.0
```

### Ejemplo 2: Hotfix en Producción

```bash
# 1. Crear branch desde main
git checkout main
git checkout -b fix/critical-security-issue

# 2. Hacer fix
git commit -m "fix(auth): corregir vulnerabilidad de seguridad

Corrige CVE-2024-XXXX que permitía bypass de autenticación.

Closes #123"

# 3. Push y PR urgente
git push -u origin fix/critical-security-issue
# Crear PR: fix/critical-security-issue → main

# 4. Después del merge, release inmediata
# GitHub Actions → Release → Run workflow (main)
# Resultado: v1.0.1

# 5. Sincronizar develop
git checkout develop
git merge main
git push origin develop
```

### Ejemplo 3: Breaking Change

```bash
# 1. Feature con breaking change
git checkout develop
git checkout -b feature/api-v2

# 2. Commits con breaking change
git commit -m "feat(api)!: cambiar estructura de respuesta

BREAKING CHANGE: Todos

 los endpoints ahora retornan datos
en un objeto 'data' en lugar de directamente en la raíz.

Antes:
{
  \"id\": 1,
  \"name\": \"John\"
}

Después:
{
  \"data\": {
    \"id\": 1,
    \"name\": \"John\"
  },
  \"meta\": {
    \"timestamp\": \"2024-01-15T10:00:00Z\"
  }
}

Migración: Actualizar clientes para acceder a response.data"

# 3. PR y release
# Resultado: v2.0.0 (incrementa MAJOR)
```

### Ejemplo 4: Múltiples tipos de commits

```bash
# Versión actual: v1.0.0

# Commits en develop:
git commit -m "feat(api): agregar endpoint de búsqueda"
git commit -m "feat(api): agregar filtros avanzados"
git commit -m "fix(validation): corregir validación de email"
git commit -m "perf(query): optimizar consulta de usuarios"
git commit -m "docs(readme): actualizar ejemplos [skip ci]"
git commit -m "test(api): agregar tests de búsqueda"

# Release desde develop:
# Semantic Release analiza:
# - 2 feat → incrementa MINOR
# - 1 fix → ya incluido en MINOR
# - 1 perf → ya incluido en MINOR
# - docs y test → no af

ectan versión
# Resultado: v1.1.0-beta.1

# Changelog generado:
# ## [1.1.0-beta.1] - 2024-01-15
# ### ✨ Nuevas Funcionalidades
# - **api**: agregar endpoint de búsqueda
# - **api**: agregar filtros avanzados
# ### 🐛 Correcciones de Bugs
# - **validation**: corregir validación de email
# ### 🚀 Mejoras de Rendimiento
# - **query**: optimizar consulta de usuarios
# ### 📚 Documentación
# - **readme**: actualizar ejemplos
```

---

## Alcances Comunes

Usa alcances consistentes para mejor organización:

**Por Capa**

- api - Endpoints REST
- service - Lógica de negocio
- repository - Acceso a datos
- model - Modelos y DTO
- controller - Controladores
- config - Configuración

**Por Módulo**

`auth` - Autenticación/Autorización
`user` - Gestión de usuarios
`product` - Productos
`order` - Pedidos
`payment` - Pagos

**Por Tipo de Archivo**

test - Tests
docs - Documentación
build - Sistema de build
ci - CI/CD

**Checklist Antes de Commit**

- [ ] ✅ Tests pasan: ./gradlew test
- [ ] ✅ Build exitoso: ./gradlew build
- [ ] ✅ Linter sin errores: ./gradlew ktlintCheck
- [ ] ✅ Código revisado: git diff
- [ ] ✅ Mensaje sigue Conventional Commits
- [ ] ✅ Tipo correcto según el cambio
- [ ] ✅ Alcance específico y claro
- [ ] ✅ Descripción en imperativo
- [ ] ✅ Referencia a issue si aplica
- [ ] ✅ [skip ci] si es solo docs

**Checklist Antes de Release**

- [ ] ✅ Todos los PR's mezclados
- [ ] ✅ Tests pasan en CI
- [ ] ✅ Branch actualizado: git pull
- [ ] ✅ Commits siguen conventional commits
- [ ] ✅ CHANGELOG.md será generado automáticamente
- [ ] ✅ Versión será calculada por Semantic Release
- [ ] ✅ Documentación actualizada
- [ ] ✅ Breaking changes documentados si aplican

---

## Recursos

- **Documentación completa**: [WORKFLOW.md](WORKFLOW.md)
- **Especificación**: https://www.conventionalcommits.org/
- **Semantic Versioning**: https://semver.org/
- **Semantic Release**: https://semantic-release.gitbook.io/
- **Keep a Changelog**: https://keepachangelog.com/

---

## Ayuda

¿Dudas sobre commits, versionado o releases?

1. Revisa esta guía completa
2. Consulta COMMIT_FIXING_GUIDE.md para cor
regir errores
3. Abre un Issue con la etiqueta `question`

**¡Happy Coding! 🚀**
