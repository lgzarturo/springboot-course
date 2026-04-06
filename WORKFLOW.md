# 🔄 Flujo de Trabajo - Commits, Versionado y Changelog

## 📋 Tabla de Contenidos

- [Introducción](#introducción)
- [Conventional Commits](#conventional-commits)
- [Versionado Semántico](#versionado-semántico)
- [Flujo de Trabajo Completo](#flujo-de-trabajo-completo)
- [Estrategia de Branching](#estrategia-de-branching)
- [Generación del Changelog](#generación-del-changelog)
- [Automatización](#automatización)
- [Herramientas y Configuración](#herramientas-y-configuración)
- [Ejemplos Prácticos](#ejemplos-prácticos)

---

## Introducción

Este documento describe el flujo de trabajo completo para mantener un historial
de commits coherente y profesional, gestionar versiones del proyecto de manera
automática y generar changelogs informativos que documenten la evolución del
proyecto.

### Objetivos

✅ **Commits descriptivos y estandarizados** que cuenten una historia clara ✅
**Versionado automático** basado en el tipo de cambios ✅ **Changelog generado
automáticamente** con información relevante ✅ **Proceso documentado y
reproducible** para todo el equipo ✅ **Integración con CI/CD** para automatizar
el release

---

## Conventional Commits

Los **Conventional Commits** son una especificación para dar formato a los
mensajes de commit de manera consistente. Esto facilita la generación automática
de changelogs y el versionado semántico.

### Estructura Básica

```
<tipo>[alcance opcional]: <descripción>

[cuerpo opcional]

[footer(s) opcional(es)]
```

### Tipos de Commit

| Tipo       | Descripción                                              | Incrementa Versión |
| ---------- | -------------------------------------------------------- | ------------------ |
| `feat`     | Nueva funcionalidad                                      | MINOR (0.x.0)      |
| `fix`      | Corrección de bugs                                       | PATCH (0.0.x)      |
| `docs`     | Cambios en documentación                                 | No incrementa      |
| `style`    | Formato, espacios, etc. (no afecta código)               | No incrementa      |
| `refactor` | Refactorización (no añade funcionalidad ni corrige bugs) | No incrementa      |
| `perf`     | Mejoras de rendimiento                                   | PATCH (0.0.x)      |
| `test`     | Añadir o corregir tests                                  | No incrementa      |
| `build`    | Cambios en build system o dependencias                   | No incrementa      |
| `ci`       | Cambios en CI/CD                                         | No incrementa      |
| `chore`    | Tareas de mantenimiento                                  | No incrementa      |
| `revert`   | Revertir un commit anterior                              | Depende            |

### Breaking Changes

Para cambios que rompen compatibilidad hacia atrás (MAJOR version):

```
feat!: rediseñar API de autenticación

BREAKING CHANGE: El endpoint /auth/login ahora requiere un token CSRF
```

O en el footer:

```
feat: rediseñar API de autenticación

BREAKING CHANGE: El endpoint /auth/login ahora requiere un token CSRF
```

### Ejemplos Prácticos

#### ✅ Buenos Commits

```bash
# Nueva funcionalidad
feat(reservas): agregar endpoint para crear reserva de habitación

# Corrección de bug
fix(pagos): corregir cálculo de impuestos en checkout

# Documentación
docs(readme): actualizar sección de instalación

# Refactorización
refactor(service): extraer lógica de validación a función separada

# Tests
test(controller): agregar tests de integración para PingController

# Con alcance y cuerpo detallado
feat(api): implementar paginación en listado de productos

Se agrega soporte para paginación mediante query parameters:
- page: número de página (default: 0)
- size: tamaño de página (default: 20)
- sort: campo de ordenamiento (default: id)

Closes #42
```

#### ❌ Commits a Evitar

```bash
# Muy genérico
git commit -m "cambios"
git commit -m "fix"
git commit -m "actualización"

# Sin tipo
git commit -m "agregar nueva función"

# Múltiples cambios sin relación
git commit -m "feat: agregar login, fix: corregir bug en pagos, docs: actualizar readme"
```

---

## Versionado Semántico

Seguimos **Semantic Versioning 2.0.0** (SemVer): `MAJOR.MINOR.PATCH`

### Formato: X.Y.Z

- **MAJOR (X)**: Cambios incompatibles con versiones anteriores (Breaking
  Changes)
- **MINOR (Y)**: Nueva funcionalidad compatible con versiones anteriores
- **PATCH (Z)**: Correcciones de bugs compatibles con versiones anteriores

### Ejemplos de Incrementos

| Versión Actual | Cambio                               | Nueva Versión      |
| -------------- | ------------------------------------ | ------------------ |
| 1.0.0          | `fix: corregir validación`           | 1.0.1              |
| 1.0.1          | `feat: agregar endpoint de búsqueda` | 1.1.0              |
| 1.1.0          | `feat!: cambiar estructura de API`   | 2.0.0              |
| 1.1.0          | `docs: actualizar readme`            | 1.1.0 (sin cambio) |

### Pre-releases

- **Alpha**: `1.0.0-alpha.1` - Versión muy temprana, inestable
- **Beta**: `1.0.0-beta.1` - Funcionalidad completa pero puede tener bugs
- **RC**: `1.0.0-rc.1` - Release Candidate, casi lista para producción

---

## Flujo de Trabajo Completo

### 1. Antes de Empezar a Codificar

```bash
# Actualizar tu rama principal
git checkout main
git pull origin main

# Crear una rama para tu feature/fix
git checkout -b feature/nombre-descriptivo
# o
git checkout -b fix/nombre-del-bug
```

### 2. Durante el Desarrollo

```bash
# Ver cambios
git status
git diff

# Agregar cambios al staging area
git add .
# o archivos específicos
git add src/main/kotlin/path/to/file.kt

# Hacer commit siguiendo Conventional Commits
git commit -m "feat(modulo): descripción clara del cambio"

# Si necesitas un mensaje más detallado
git commit
# Esto abrirá tu editor para escribir un mensaje completo
```

### 3. Antes de Hacer Push

```bash
# Asegurarte de que los tests pasan
.\gradlew.bat test

# Verificar el código con linters
.\gradlew.bat ktlintCheck
.\gradlew.bat detekt

# Si hay problemas de formato, auto-corregir
.\gradlew.bat ktlintFormat
```

### 4. Subir Cambios

```bash
# Push de tu rama
git push origin feature/nombre-descriptivo

# Si es la primera vez
git push -u origin feature/nombre-descriptivo
```

### 5. Pull Request y Merge

1. Crear Pull Request en GitHub
2. Asegurarse de que los checks de CI pasen
3. Solicitar revisión de código
4. Una vez aprobado, hacer **Squash and Merge** con un mensaje conventional
   commit

### 6. Release (Automatizado)

El release se genera automáticamente cuando se hace merge a `main`:

1. El CI/CD analiza los commits nuevos
2. Determina el nuevo número de versión según SemVer
3. Genera el changelog
4. Crea un tag de Git
5. Publica el release en GitHub

---

## Estrategia de Branching

Usamos **GitHub Flow** simplificado:

### Ramas Principales

- **`main`**: Rama principal, siempre estable y desplegable
- **`develop`** (opcional): Para proyectos grandes con múltiples features en
  paralelo

### Ramas de Trabajo

```
feature/nombre-descriptivo    # Nueva funcionalidad
fix/nombre-del-bug            # Corrección de bugs
docs/nombre-de-la-doc         # Documentación
refactor/nombre-refactor      # Refactorización
test/nombre-del-test          # Tests
chore/nombre-de-la-tarea      # Tareas de mantenimiento
```

### Reglas

✅ Crear rama desde `main` actualizado ✅ Un commit por cambio lógico ✅ Nombres
descriptivos en inglés o español consistente ✅ Pull Request para todos los
cambios ✅ Squash commits en el merge a `main` ✅ Borrar rama después del merge

---

## Generación del Changelog

El changelog se genera automáticamente a partir de los commits conventional.

### Estructura del CHANGELOG.md

```markdown
# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to
[Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added

- Cambios añadidos pero no lanzados aún

## [1.2.0] - 2024-01-20

### Added

- feat(reservas): implementar sistema de reservas (#45)
- feat(api): agregar paginación a endpoints de listado (#42)

### Fixed

- fix(pagos): corregir cálculo de impuestos (#48)
- fix(auth): resolver error de token expirado (#47)

### Changed

- refactor(service): mejorar estructura de servicios de dominio

## [1.1.0] - 2024-01-15

### Added

- feat(ping): implementar endpoint de health check

### Fixed

- fix(tests): corregir tests de PingController
```

### Secciones del Changelog

- **Added**: Nueva funcionalidad (`feat`)
- **Changed**: Cambios en funcionalidad existente (`refactor`, `perf`)
- **Deprecated**: Funcionalidad marcada como obsoleta
- **Removed**: Funcionalidad eliminada
- **Fixed**: Correcciones de bugs (`fix`)
- **Security**: Vulnerabilidades corregidas

---

## Automatización

### GitHub Actions Workflow

El proyecto incluye un workflow automatizado que:

1. **En cada Push a una rama**:
   - Ejecuta tests
   - Verifica linters (ktlint, detekt)
   - Ejecuta análisis de cobertura

2. **En cada Pull Request**:
   - Valida que los commits sigan Conventional Commits
   - Ejecuta la suite completa de tests
   - Genera reporte de cobertura

3. **En Merge a Main**:
   - Analiza commits desde el último release
   - Calcula la nueva versión según SemVer
   - Actualiza `build.gradle.kts` con la nueva versión
   - Genera/actualiza `CHANGELOG.md`
   - Crea tag de Git
   - Crea GitHub Release con notas generadas

### Comandos Gradle

```bash
# Ver la versión actual
.\gradlew.bat printVersion

# Generar changelog manualmente
.\gradlew.bat generateChangelog

# Simular próximo release (sin crear tag)
.\gradlew.bat whatsnext

# Crear release completo
.\gradlew.bat release
```

---

## Herramientas y Configuración

### Herramientas Incluidas

1. **Gradle Semantic Release Plugin**: Automatiza versionado
2. **Conventional Changelog**: Genera changelog
3. **Git Hooks**: Valida mensajes de commit
4. **GitHub Actions**: CI/CD completo

### Configuración del Editor

#### Visual Studio Code

Instalar extensión: **Conventional Commits**

#### IntelliJ IDEA

1. Settings → Version Control → Commit
2. Usar plantilla: `.gitmessage`
3. Habilitar "Check commit message spelling"

### Plantilla de Commit (.gitmessage)

El proyecto incluye una plantilla para ayudarte con los commits:

```bash
# Configurar la plantilla globalmente
git config --global commit.template .gitmessage

# O solo para este proyecto
git config commit.template .gitmessage
```

---

## Ejemplos Prácticos

### Ejemplo 1: Agregar Nueva Funcionalidad

```bash
# 1. Crear rama
git checkout -b feature/agregar-sistema-reservas

# 2. Hacer cambios y commit
git add .
git commit -m "feat(reservas): implementar endpoint POST para crear reservas

- Agregar modelo de dominio Reserva
- Implementar ReservaService con lógica de negocio
- Crear ReservaController con endpoint POST /api/v1/reservas
- Agregar validaciones de disponibilidad de habitación

Closes #45"

# 3. Verificar tests
.\gradlew.bat test

# 4. Push
git push -u origin feature/agregar-sistema-reservas

# 5. Crear Pull Request en GitHub
# 6. Después del merge, se genera automáticamente versión 1.1.0
```

### Ejemplo 2: Corregir Bug

```bash
# 1. Crear rama
git checkout -b fix/corregir-calculo-impuestos

# 2. Hacer cambios y commit
git add .
git commit -m "fix(pagos): corregir cálculo de impuestos en checkout

El cálculo anterior no consideraba impuestos compuestos.
Ahora se aplica correctamente la fórmula:
total = subtotal * (1 + tax_rate)

Fixes #48"

# 3. Push y PR
git push -u origin fix/corregir-calculo-impuestos

# 4. Después del merge, se genera automáticamente versión 1.0.1
```

### Ejemplo 3: Breaking Change

```bash
git commit -m "feat(api)!: cambiar estructura de respuesta de autenticación

BREAKING CHANGE: El endpoint /api/v1/auth/login ahora retorna un objeto
con la estructura:
{
  \"accessToken\": \"...\",
  \"refreshToken\": \"...\",
  \"expiresIn\": 3600,
  \"user\": {...}
}

La estructura anterior retornaba solo el token directamente.
Actualizar clientes para usar la nueva estructura.

Closes #50"

# Esto generará automáticamente versión 2.0.0
```

### Ejemplo 4: Múltiples Commits en una Feature

```bash
# Trabajar en una feature compleja con múltiples commits
git checkout -b feature/sistema-pagos-completo

# Commit 1: Modelo de dominio
git add src/main/kotlin/domain/model/
git commit -m "feat(pagos): agregar modelos de dominio para pagos"

# Commit 2: Servicio
git add src/main/kotlin/domain/service/PagoService.kt
git commit -m "feat(pagos): implementar PagoService con lógica de negocio"

# Commit 3: Controller
git add src/main/kotlin/infrastructure/rest/controller/PagoController.kt
git commit -m "feat(pagos): agregar PagoController con endpoints REST"

# Commit 4: Tests
git add src/test/
git commit -m "test(pagos): agregar tests unitarios y de integración"

# Al hacer merge con Squash, se combinan en un solo commit conventional
```

---

## Buenas Prácticas

### ✅ DO

- Escribir commits en español consistentemente (o inglés, pero ser consistente)
- Usar verbos en infinitivo: "agregar", "implementar", "corregir"
- Ser específico en la descripción
- Referenciar issues con `Closes #N` o `Fixes #N`
- Mantener commits atómicos (un cambio lógico por commit)
- Ejecutar tests antes de hacer commit
- Revisar el diff antes de hacer commit

### ❌ DON'T

- No hacer commits genéricos: "cambios", "fix", "wip"
- No mezclar múltiples cambios sin relación en un commit
- No hacer commit de código comentado sin eliminar
- No hacer commit de archivos de configuración personal (`.idea/`, etc.)
- No hacer commit sin haber probado el código
- No usar mayúsculas al inicio de la descripción (después del tipo)

---

## Recursos Adicionales

- [Conventional Commits Specification](https://www.conventionalcommits.org/)
- [Semantic Versioning](https://semver.org/)
- [Keep a Changelog](https://keepachangelog.com/)
- [GitHub Flow](https://docs.github.com/en/get-started/quickstart/github-flow)
- [Pro Git Book (Español)](https://git-scm.com/book/es/v2)

---

## Soporte

Si tienes dudas sobre el flujo de trabajo:

1. Revisa este documento
2. Consulta [COMMIT_GUIDE.md](COMMIT_GUIDE.md) para una referencia rápida
3. Abre un Issue con la etiqueta `question`
4. Revisa los commits anteriores del proyecto como ejemplo

---

**¡Happy Coding! 🚀**

Recuerda: Un buen historial de commits es la mejor documentación de la evolución
de tu proyecto.
