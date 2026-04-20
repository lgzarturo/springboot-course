# AGENTS.md — Project-Specific Guidance

## Quick Reference

| Task              | Command                                                               |
|-------------------|-----------------------------------------------------------------------|
| Build             | `make build`                                                          |
| Run dev           | `make run`                                                            |
| Run tests         | `make test`                                                           |
| Single test class | `make test-class CLASS=HotelServiceTest`                              |
| Coverage report   | `make coverage` (output: `build/reports/jacoco/test/html/index.html`) |
| Coverage gate     | `make coverage-check` (requires ≥85%)                                 |
| Lint check        | `make lint`                                                           |
| Format fix        | `make format`                                                         |
| Full quality gate | `make quality`                                                        |
| DDL from entities | `make ddl`                                                            |
| Docker services   | `make docker-up` / `make docker-down`                                 |

> On Windows: use `gradlew <task>` directly instead of `./gradlew`.

## Architecture

- **Pattern**: MVC by feature (Screaming Architecture) — not Hexagonal
- **Rule**: Max 2 nesting levels inside a feature (`hotels/dto/` is the limit)
- **Domain vs JPA**: `Hotel.kt` = pure domain (no Spring annotations). `HotelEntity.kt` = JPA entity. Service translates between them.
- **DTOs**: Controller maps Request → Domain before calling Service. DTOs never enter the service layer.
- **Feature isolation**: No cross-feature imports. Use `common/` or Spring events for inter-feature communication.
- **`examples/`** package: Only one with full hexagonal pattern. Don't replicate in new features.

## Profiles

- `dev` — H2 in-memory, default for local dev
- `test` — H2 (unit/integration) or Testcontainers PostgreSQL (requires Docker)
- `prod` — PostgreSQL via env vars
- `generate-ddl` — H2, Flyway disabled (for DDL generation)

## Testing

- **Unit**: Service tests without Spring context (fast). Use MockK.
- **Integration**: `@WebMvcTest`, `@DataJpaTest`, or extend `BaseIntegrationTest` for full context.
- **Testcontainers**: Auto-enabled for integration tests if Docker is available; falls back to H2 if not.
- **Security tests**: Use `SecurityTestConfig` + `@WithMockUser` custom annotation.

## Environment

- Create `.env` from `.env.example` — `build.gradle.kts` auto-loads it as fallback.
- Required vars: `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD`, `DB_PORT`
- Optional: `SENTRY_DSN`, `SENTRY_AUTH_TOKEN`

## Code Quality

- KTLint 1.7.1 — auto-fixes with `make format`
- Detekt 2.0.0-alpha.1 — static analysis, `autoCorrect = true` in config
- JaCoCo — 85% minimum coverage enforced in CI

## Important Files

- `CLAUDE.md` — detailed architecture and workflow guidance
- `Makefile` — source of truth for all make targets
- `config/detekt/detekt.yml` — linter rules
- `build.gradle.kts` — build configuration, env loading


**Enfoque**

* Piensa antes de actuar. Lee los archivos existentes antes de escribir código.
* Sé conciso en la salida, pero exhaustivo en el razonamiento.
* Prefiere editar en lugar de reescribir archivos completos.
* No vuelvas a leer archivos que ya leíste, a menos que puedan haber cambiado.
* Omite archivos mayores a 100KB salvo que sea explícitamente necesario.
* Sugiere ejecutar `/cost` cuando una sesión se alargue para monitorear la proporción de caché.
* Recomienda iniciar una nueva sesión al cambiar a una tarea no relacionada.
* Prueba tu código antes de declararlo terminado.
* Sin introducciones complacientes ni relleno innecesario al cerrar.
* Mantén las soluciones simples y directas.
* Las instrucciones del usuario siempre tienen prioridad sobre este archivo.

## Salida
- Devuelve primero el código. Explicación después, solo si no es obvio.
- Sin texto en línea. Usa comentarios con moderación, solo donde la lógica no sea clara.
- Sin boilerplate a menos que se solicite explícitamente.

## Reglas de Código
- La solución funcional más simple. Sin sobreingeniería.
- Sin abstracciones para operaciones de un solo uso.
- Sin funcionalidades especulativas o "quizá también quieras...".
- Lee el archivo antes de modificarlo. Nunca edites a ciegas.
- Sin docstrings ni anotaciones de tipo en código que no se esté modificando.
- Sin manejo de errores para escenarios que no pueden ocurrir.
- Tres líneas similares son mejores que una abstracción prematura.

## Reglas de Revisión
- Indica el bug. Muestra la corrección. Fin.
- Sin sugerencias fuera del alcance de la revisión.
- Sin cumplidos sobre el código antes o después de la revisión.

## Reglas de Depuración
- Nunca especules sobre un bug sin leer primero el código relevante.
- Indica qué encontraste, dónde, y la solución. Una sola pasada.
- Si la causa no es clara: dilo. No adivines.

## Formato Simple
- Sin guiones largos, comillas tipográficas ni símbolos Unicode decorativos.
- Solo guiones simples y comillas rectas.
- Caracteres de lenguaje natural (acentos, CJK, etc.) están bien cuando el contenido lo requiera.
- El código debe ser seguro para copiar y pegar.

<!-- code-review-graph MCP tools -->
## MCP Tools: code-review-graph

**IMPORTANT: This project has a knowledge graph. ALWAYS use the
code-review-graph MCP tools BEFORE using Grep/Glob/Read to explore
the codebase.** The graph is faster, cheaper (fewer tokens), and gives
you structural context (callers, dependents, test coverage) that file
scanning cannot.

### When to use graph tools FIRST

- **Exploring code**: `semantic_search_nodes` or `query_graph` instead of Grep
- **Understanding impact**: `get_impact_radius` instead of manually tracing imports
- **Code review**: `detect_changes` + `get_review_context` instead of reading entire files
- **Finding relationships**: `query_graph` with callers_of/callees_of/imports_of/tests_for
- **Architecture questions**: `get_architecture_overview` + `list_communities`

Fall back to Grep/Glob/Read **only** when the graph doesn't cover what you need.

### Key Tools

| Tool | Use when |
|------|----------|
| `detect_changes` | Reviewing code changes — gives risk-scored analysis |
| `get_review_context` | Need source snippets for review — token-efficient |
| `get_impact_radius` | Understanding blast radius of a change |
| `get_affected_flows` | Finding which execution paths are impacted |
| `query_graph` | Tracing callers, callees, imports, tests, dependencies |
| `semantic_search_nodes` | Finding functions/classes by name or keyword |
| `get_architecture_overview` | Understanding high-level codebase structure |
| `refactor_tool` | Planning renames, finding dead code |

### Workflow

1. The graph auto-updates on file changes (via hooks).
2. Use `detect_changes` for code review.
3. Use `get_affected_flows` to understand impact.
4. Use `query_graph` pattern="tests_for" to check coverage.
