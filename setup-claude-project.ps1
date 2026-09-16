# ================================================
# Setup Claude + OpenCode - Windows 11 PowerShell
# Version: 1.0.2
# ================================================

Write-Host "=== Setup Claude Project v1.0.2 ===" -ForegroundColor Cyan

# ====================== SELECCIÓN DE HERRAMIENTA ======================
Write-Host ""
Write-Host "¿Qué herramienta quieres configurar?" -ForegroundColor Yellow
Write-Host "1. Solo Claude"
Write-Host "2. Solo OpenCode"
Write-Host "3. Claude + OpenCode"
$toolSel = Read-Host "Selecciona (1-3)"

$targets = switch ($toolSel) {
    "1" { @("claude") }
    "2" { @("opencode") }
    default { @("claude", "opencode") }
}

Write-Host "Herramienta(s) seleccionada(s): $($targets -join ', ')" -ForegroundColor Green

# ====================== SELECCIÓN DE STACKS ======================
Write-Host ""
Write-Host "Selecciona el tipo de proyecto (puedes elegir varios separados por coma):" -ForegroundColor Yellow
Write-Host "1. Spring Boot + Kotlin"
Write-Host "2. Python + UV + Django"
Write-Host "3. Python + FastAPI"
Write-Host "4. Next.js + TypeScript"
Write-Host "5. Astro"
Write-Host "6. Todos los stacks (recomendado)"
$rawInput = Read-Host "Ingresa los números (ejemplo: 1,3,6)"
$selections = $rawInput -split ',' | ForEach-Object { $_.Trim() }

# ====================== CREAR DIRECTORIOS ======================
foreach ($t in $targets) {
    New-Item -ItemType Directory -Force -Path ".$t/skills", ".$t/commands", ".$t/agents" | Out-Null
}

# ====================== CLAUDE.md (solo si Claude está en targets) ======================
if ($targets -contains "claude") {
    $claudeApproach = @'

# Approach

- Think before acting. Read existing files before writing code.
- Be concise in output but thorough in reasoning.
- Prefer editing over rewriting whole files.
- Do not re-read files you have already read unless the file may have changed.
- Skip files over 100KB unless explicitly required.
- Suggest running /cost when a session is running long to monitor cache ratio.
- Recommend starting a new session when switching to an unrelated task.
- Test your code before declaring done.
- No sycophantic openers or closing fluff.
- Keep solutions simple and direct.
- User instructions always override this file.
'@
    $existingContent = if (Test-Path "CLAUDE.md") { Get-Content "CLAUDE.md" -Raw -Encoding UTF8 } else { "" }
    if ($existingContent -notmatch "Think before acting") {
        Add-Content -Path "CLAUDE.md" -Value $claudeApproach -Encoding UTF8
        Write-Host "✓ CLAUDE.md actualizado con reglas de Approach" -ForegroundColor Green
    } else {
        Write-Host "✓ CLAUDE.md ya contiene las reglas de Approach (sin cambios)" -ForegroundColor Green
    }
}

# ====================== FUNCIÓN PARA CREAR SKILL ======================
function Create-Skill {
    param([string]$name, [string]$content)

    foreach ($t in $targets) {
        $dir = ".$t/skills/$name"
        New-Item -ItemType Directory -Force -Path $dir | Out-Null
        Set-Content -Path "$dir/SKILL.md" -Value $content -Encoding UTF8
    }
    Write-Host "✓ Skill creado: $name" -ForegroundColor Green
}

# ====================== FUNCIÓN PARA CREAR COMANDO ======================
function Create-Command {
    param([string]$name, [string]$content)

    foreach ($t in $targets) {
        Set-Content -Path ".$t/commands/$name.md" -Value $content -Encoding UTF8
    }
    Write-Host "✓ Comando creado: /$name" -ForegroundColor Green
}

# ====================== FUNCIÓN PARA CREAR AGENTE ======================
function Create-Agent {
    param([string]$name, [string]$content)

    foreach ($t in $targets) {
        Set-Content -Path ".$t/agents/$name.md" -Value $content -Encoding UTF8
    }
    Write-Host "✓ Agente creado: $name" -ForegroundColor Green
}

# ====================== SKILLS UNIVERSALES ======================
Create-Skill -name "testing-tdd" -content @'
---
name: testing-tdd
description: Genera y ejecuta tests siguiendo TDD (Red-Green-Refactor) + tests unitarios, integración y e2e
license: MIT
---

## Reglas obligatorias (TDD + Testing)
- Siempre sigue el ciclo TDD: 1. Red, 2. Green, 3. Refactor.
- Usa fixtures/factories y mocks reales.
- Tests independientes, rápidos y deterministas.

## Reglas por stack
**Spring Boot + Kotlin** → JUnit 5 + Kotest + Testcontainers
**Python** → pytest + factories
**Next.js / Astro** → Vitest + React Testing Library + Playwright

## Cuándo usarme
- "Escribe los tests TDD para esta función"
- "Añade test de integración para el endpoint /orders"
'@

Create-Skill -name "security" -content @'
---
name: security
description: Implementa y revisa seguridad OWASP Top 10
license: MIT
---

## Reglas obligatorias
- Valida input, rate limiting, CORS estricto, headers de seguridad.
- Sigue OWASP Top 10 2025.

## Reglas por stack
**Spring Boot** → Spring Security 6 + JWT
**Python** → django-allauth / FastAPI Users
**Next.js / Astro** → NextAuth v5 / Auth.js

## Cuándo usarme
- "Añade autenticación JWT segura"
- "Revisa este código por vulnerabilidades"
'@

Create-Skill -name "code-review" -content @'
---
name: code-review
description: Realiza code reviews completos (calidad, seguridad, performance)
license: MIT
---

## Reglas obligatorias
- Estructura: Lo bueno | Problemas | Sugerencias.
- Prioriza: seguridad > tests > performance > clean code.
- Sugiere diffs exactos.

## Cuándo usarme
- "Haz code review de este archivo"
- "Revisa este PR"
'@

Create-Skill -name "testing-coverage" -content @'
---
name: testing-coverage
description: Configura cobertura de tests y CI pipeline
license: MIT
---

## Reglas obligatorias
- Mínimo 80% coverage.
- Configura GitHub Actions con threshold.

## Cuándo usarme
- "Configura coverage + GitHub Actions"
'@

# ====================== SKILLS POR STACK ======================
if ($selections -contains "1" -or $selections -contains "6") {
    Create-Skill -name "spring-boot-kotlin-rest" -content @'
---
name: spring-boot-kotlin-rest
description: Genera endpoints REST con Kotlin + Spring Boot 3.4+ (Records, Virtual Threads, ProblemDetail)
license: MIT
---

## Reglas obligatorias
- Usa data class / records.
- Controllers -> Service -> Repository.
- Virtual Threads por defecto.

## Cuándo usarme
- "Crea un CRUD para Order"
'@

    # ====================== 18 SKILLS OFICIALES DE SPRING BOOT ======================
    Write-Host ""
    $sbExtra = Read-Host "¿Instalar las 18 skills oficiales de Spring Boot? (s/N)"
    if ($sbExtra -match '^[sS]$') {
        Write-Host "Clonando spring-boot-skills..." -ForegroundColor Yellow
        $tmpDir = "$env:TEMP\spring-boot-skills"
        try {
            git clone --depth 1 https://github.com/rrezartprebreza/spring-boot-skills.git $tmpDir 2>&1 | Out-Null
            foreach ($t in $targets) {
                $dest = ".$t/skills"
                Copy-Item -Path "$tmpDir/skills/*" -Destination $dest -Recurse -Force
            }
            Remove-Item -Recurse -Force $tmpDir
            Write-Host "✓ 18 skills oficiales de Spring Boot instaladas" -ForegroundColor Green
        } catch {
            Write-Host "✗ Error al clonar el repositorio. Verifica tu conexión y que git esté instalado." -ForegroundColor Red
            if (Test-Path $tmpDir) { Remove-Item -Recurse -Force $tmpDir }
        }
    }
}

if ($selections -contains "2" -or $selections -contains "6") {
    Create-Skill -name "python-django-uv" -content @'
---
name: python-django-uv
description: Best practices Django + UV (pyproject.toml, ruff, pytest)
license: MIT
---

## Reglas obligatorias
- UV para dependencias.
- Apps con domain-driven.
- Testing: pytest + pytest-django.

## Cuándo usarme
- "Crea un modelo User con Django + UV"
'@
}

if ($selections -contains "3" -or $selections -contains "6") {
    Create-Skill -name "python-fastapi" -content @'
---
name: python-fastapi
description: FastAPI + UV + Pydantic v2 + SQLAlchemy 2.0
license: MIT
---

## Reglas obligatorias
- Pydantic v2 models.
- Dependency Injection con Depends.
- Testing: TestClient + httpx.

## Cuándo usarme
- "Crea un endpoint POST /items"
'@
}

if ($selections -contains "4" -or $selections -contains "6") {
    Create-Skill -name "nextjs-typescript" -content @'
---
name: nextjs-typescript
description: Next.js 15+ App Router + TypeScript + Server Actions
license: MIT
---

## Reglas obligatorias
- Server Components por defecto.
- Server Actions para mutaciones.
- TanStack Query solo en client.

## Cuándo usarme
- "Crea una página con Server Action"
'@
}

if ($selections -contains "5" -or $selections -contains "6") {
    Create-Skill -name "astro" -content @'
---
name: astro
description: Astro 5+ + TypeScript + Islands + Content Collections
license: MIT
---

## Reglas obligatorias
- Islands con client:load / client:only.
- Content Collections para blog.

## Cuándo usarme
- "Crea un componente Island en Astro"
'@
}

# ====================== AGENTES ======================
# code-reviewer
$codeReviewerOpencode = @'
---
description: Revisa código por calidad y mejores prácticas
mode: subagent
model: kimi-k2.5
temperature: 0.1
tools:
  write: false
  edit: false
  bash: false
---

Proporciona feedback constructivo sin hacer cambios directos. Usa el skill code-review cuando sea necesario.
'@

$codeReviewerClaude = @'
---
description: Revisa código por calidad y mejores prácticas
mode: subagent
model: anthropic/claude-sonnet-4-20250514
temperature: 0.1
tools:
  write: false
  edit: false
  bash: false
---

Proporciona feedback constructivo sin hacer cambios directos. Usa el skill code-review cuando sea necesario.
'@

foreach ($t in $targets) {
    if ($t -eq "opencode") {
        Set-Content -Path ".$t/agents/code-reviewer.md" -Value $codeReviewerOpencode -Encoding UTF8
    } else {
        Set-Content -Path ".$t/agents/code-reviewer.md" -Value $codeReviewerClaude -Encoding UTF8
    }
}
Write-Host "✓ Agente creado: code-reviewer" -ForegroundColor Green

# release-manager
$releaseManagerOpencode = @'
---
description: Agente especializado en gestión de releases, changelog y versioning semántico
mode: subagent
model: kimi-k2.5
temperature: 0.1
tools:
  write: true
  edit: true
  bash: true
---

Eres el Release Manager. Siempre usa Conventional Commits, Keep a Changelog y Semantic Versioning.
Coordina con los skills @testing-coverage, @code-review y los comandos /update-changelog y /bump-version.
Nunca hagas cambios sin confirmar con el usuario.
'@

$releaseManagerClaude = @'
---
description: Agente especializado en gestión de releases, changelog y versioning semántico
mode: subagent
model: anthropic/claude-sonnet-4-20250514
temperature: 0.1
tools:
  write: true
  edit: true
  bash:
    "git *": "ask"
    "gh *": "ask"
---

Eres el Release Manager. Siempre usa Conventional Commits, Keep a Changelog y Semantic Versioning.
Coordina con los skills @testing-coverage, @code-review y los comandos /update-changelog y /bump-version.
Nunca hagas cambios sin confirmar con el usuario.
'@

foreach ($t in $targets) {
    if ($t -eq "opencode") {
        Set-Content -Path ".$t/agents/release-manager.md" -Value $releaseManagerOpencode -Encoding UTF8
    } else {
        Set-Content -Path ".$t/agents/release-manager.md" -Value $releaseManagerClaude -Encoding UTF8
    }
}
Write-Host "✓ Agente creado: release-manager" -ForegroundColor Green

# git-workflow
$gitWorkflowOpencode = @'
---
description: Agente experto en git, conventional commits, branching y PRs
mode: subagent
model: kimi-k2.5
temperature: 0.2
tools:
  bash: true
---

Maneja todo el workflow de git: branch creation, conventional commits, rebase, PR description.
Siempre sigue Conventional Commits y sugiere comandos seguros.
'@

$gitWorkflowClaude = @'
---
description: Agente experto en git, conventional commits, branching y PRs
mode: subagent
model: anthropic/claude-sonnet-4-20250514
temperature: 0.2
tools:
  bash:
    "git *": "allow"
    "gh pr *": "ask"
---

Maneja todo el workflow de git: branch creation, conventional commits, rebase, PR description.
Siempre sigue Conventional Commits y sugiere comandos seguros.
'@

foreach ($t in $targets) {
    if ($t -eq "opencode") {
        Set-Content -Path ".$t/agents/git-workflow.md" -Value $gitWorkflowOpencode -Encoding UTF8
    } else {
        Set-Content -Path ".$t/agents/git-workflow.md" -Value $gitWorkflowClaude -Encoding UTF8
    }
}
Write-Host "✓ Agente creado: git-workflow" -ForegroundColor Green

# ====================== COMANDOS ======================
Create-Command -name "run-all-tests" -content @'
---
description: Ejecuta todos los tests del proyecto (usa skill testing-tdd)
---

# Detecta el stack y ejecuta tests completos con coverage
# Usa el skill @testing-tdd si es necesario
'@

Create-Command -name "conventional-commit" -content @'
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
'@

Create-Command -name "update-changelog" -content @'
---
description: Actualiza CHANGELOG.md con los commits recientes siguiendo formato Conventional Commits + Keep a Changelog
---

Lee los commits desde el último tag (`git log --oneline $(git describe --tags --abbrev=0 2>/dev/null || echo "")..HEAD`).
Agrúpalos por tipo (Added, Changed, Fixed, etc.).
Actualiza o crea CHANGELOG.md en la raíz siguiendo el formato Keep a Changelog (https://keepachangelog.com).
Mantén la sección "Unreleased" primero y añade una nueva sección con la fecha de hoy.
Usa el skill @code-review si necesitas validar el formato.
'@

Create-Command -name "bump-version" -content @'
---
description: Detecta bump semántico (major/minor/patch) desde commits o según instrucción del usuario y actualiza versión
---

Detecta el tipo de proyecto:
- package.json → Next.js / Astro / TypeScript
- pyproject.toml → Python + UV (Django o FastAPI)
- build.gradle.kts o pom.xml → Spring Boot + Kotlin

Analiza commits desde el último tag para inferir el bump (feat = minor, fix = patch, BREAKING CHANGE = major).
Si el usuario indica explícitamente "major", "minor" o "patch", respeta eso.
Actualiza la versión en el archivo correspondiente.
Crea un commit con mensaje "chore: bump version to vX.Y.Z"
Crea un tag anotado: git tag -a vX.Y.Z -m "Release vX.Y.Z"
Pregúntame antes de ejecutar cualquier git command.
'@

Create-Command -name "up-version-patch" -content @'
---
description: Aumenta la versión en patch (1.2.3 → 1.2.4)
---

Ejecuta bump de versión patch.
Actualiza el archivo correspondiente (package.json, pyproject.toml, build.gradle.kts, etc.).
Crea commit "chore: bump version to vX.Y.Z" y tag anotado.
Pregúntame antes de ejecutar cualquier git command.
'@

Create-Command -name "up-version-minor" -content @'
---
description: Aumenta la versión en minor (1.2.3 → 1.3.0)
---

Ejecuta bump de versión minor.
Actualiza el archivo correspondiente (package.json, pyproject.toml, build.gradle.kts, etc.).
Crea commit "chore: bump version to vX.Y.Z" y tag anotado.
Pregúntame antes de ejecutar cualquier git command.
'@

Create-Command -name "up-version-major" -content @'
---
description: Aumenta la versión en major (1.2.3 → 2.0.0)
---

Ejecuta bump de versión major (incluye breaking changes).
Actualiza el archivo correspondiente (package.json, pyproject.toml, build.gradle.kts, etc.).
Crea commit "chore: bump version to vX.Y.Z" y tag anotado.
Pregúntame antes de ejecutar cualquier git command.
'@

Create-Command -name "create-pr" -content @'
---
description: Crea un Pull Request en GitHub con título, descripción y labels basados en los cambios
---

Analiza los cambios en la rama actual (`git diff main...HEAD` o rama base).
Genera:
- Título siguiendo Conventional Commits
- Descripción completa con:
  - ¿Qué cambia?
  - ¿Por qué?
  - Breaking changes (si aplica)
  - Cómo probarlo
- Labels automáticas (feat, fix, chore, breaking, etc.)

Usa `gh pr create` para crear el PR.
Pregúntame antes de ejecutar cualquier comando que modifique GitHub.
'@

Create-Command -name "lint-all" -content @'
---
description: Ejecuta linting completo según el stack del proyecto (Kotlin, Python, TypeScript, Astro)
---

Detecta el stack del proyecto y ejecuta el linter correspondiente:
- Spring Boot + Kotlin → ./gradlew ktlintCheck o detekt
- Python + UV (Django/FastAPI) → uv run ruff check . --fix
- Next.js / TypeScript → npm run lint o eslint .
- Astro → npm run lint

Muestra solo los errores y sugerencias importantes.
Al final, sugiere ejecutar /format-all si es necesario.
'@

Create-Command -name "format-all" -content @'
---
description: Formatea todo el código según las reglas del proyecto (ktlint, ruff, prettier, etc.)
---

Detecta el stack y ejecuta el formateador:
- Kotlin → ./gradlew ktlintFormat
- Python → uv run ruff format . && uv run ruff check --fix
- Next.js / TypeScript / Astro → npm run format

Ejecuta el comando correspondiente y muestra un resumen de archivos modificados.
'@

Create-Command -name "deploy-preview" -content @'
---
description: Crea un deployment de preview para el entorno actual (Vercel, Netlify, Railway, etc.)
---

Detecta el tipo de proyecto:
- Next.js / Astro → Vercel / Netlify preview
- Spring Boot → Railway preview
- Python (FastAPI/Django) → Railway o Fly.io

Ejecuta el comando de preview correspondiente y dame el enlace del deployment.
Si no está configurado, dame los pasos para configurarlo.
'@

Create-Command -name "release" -content @'
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
'@

Create-Command -name "update-claude" -content @'
---
description: Actualiza el archivo CLAUDE.md agregando las reglas de approach si no existen
---

Lee el archivo CLAUDE.md y verifica si ya contiene las reglas de approach.
Si el archivo no existe o no contiene estas reglas, las agrega al final usando append.

Las reglas a verificar/agregar son:

```markdown
# Approach

- Think before acting. Read existing files before writing code.
- Be concise in output but thorough in reasoning.
- Prefer editing over rewriting whole files.
- Do not re-read files you have already read unless the file may have changed.
- Skip files over 100KB unless explicitly required.
- Suggest running /cost when a session is running long to monitor cache ratio.
- Recommend starting a new session when switching to an unrelated task.
- Test your code before declaring done.
- No sycophantic openers or closing fluff.
- Keep solutions simple and direct.
- User instructions always override this file.
- When using tools, be precise and minimal with context.
```

NUNCA sobrescribas el archivo completo. Solo agrega el contenido faltante al final si no existe.
Muestra un resumen de lo que se agregó o indica si todo ya estaba presente.
'@

# ====================== FINAL ======================
$targetDirs = ($targets | ForEach-Object { ".$_" }) -join " y "
Write-Host "`n¡Setup completado con éxito!" -ForegroundColor Cyan
Write-Host "Estructura creada en: $targetDirs"
Write-Host "Reinicia Claude Code u OpenCode y prueba con @testing-tdd o /run-all-tests"
