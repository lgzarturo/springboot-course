## Descripción

<!-- Resumen claro de los cambios. Enlaza el issue que resuelve. -->

Closes #<issue_number>

## Tipo de cambio

<!-- Marca con [x] lo que aplique -->

- [ ] 🐛 Bug fix (cambio que corrige un issue)
- [ ] ✨ Nueva funcionalidad (cambio que añade funcionalidad)
- [ ] 💥 Breaking change (fix o feature que rompe compatibilidad)
- [ ] 📝 Documentación (cambios solo en docs)
- [ ] 🔧 Configuración/Infra (cambios en CI, Docker, Gradle)
- [ ] ♻️ Refactor (cambio que no corrige bug ni añade feature)
- [ ] ✅ Tests (añadir o corregir tests)

## Enfoque TDD

<!-- Describe el ciclo Red-Green-Refactor seguido -->

- [ ] **Red**: escribí test(s) que fallan antes de implementar
- [ ] **Green**: implementé el código mínimo para pasar los tests
- [ ] **Refactor**: limpié el código manteniendo tests verdes

**Tests añadidos/modificados:**
- `<NombreTest>.kt` — describe qué cubre

## Arquitectura

<!-- Marca las capas afectadas -->

- [ ] **Domain** (model, port, service, exception)
- [ ] **Infrastructure** (rest, persistence, exception)
- [ ] **Config** (WebConfig, OpenApiConfig, SecurityConfig)
- [ ] **Shared** (constants, utils, extensions)

**Principios aplicados:**
- [ ] SOLID
- [ ] Ports & Adapters (Hexagonal)
- [ ] Domain-Driven Design

## Checklist de calidad

- [ ] El código compila sin warnings
- [ ] Todos los tests pasan (`./gradlew test`)
- [ ] Cobertura de tests ≥ 80% en código nuevo
- [ ] Lint/estilo pasa (`./gradlew ktlintCheck` o `detekt`)
- [ ] Documentación actualizada (KDoc, OpenAPI, Wiki si aplica)
- [ ] No expongo entidades JPA en DTOs
- [ ] Manejo de errores consistente (GlobalExceptionHandler)
- [ ] Commits atómicos y mensajes descriptivos

## Documentación

<!-- Marca si actualizaste -->

- [ ] OpenAPI/Swagger (ejemplos, descripciones)
- [ ] README o Wiki
- [ ] CHANGELOG.md (entrada en [Unreleased])
- [ ] Comentarios KDoc en clases/funciones públicas

## Cómo probar

<!-- Instrucciones para revisor -->

1. Checkout de la rama: `git checkout feature/ISSUE-X-<slug>`
2. Ejecutar: `./gradlew clean bootRun`
3. Probar endpoint(s):
   ```bash
   curl http://localhost:8080/api/v1/<endpoint>
   ```
4. Verificar Swagger UI: http://localhost:8080/swagger-ui.html
5. Ejecutar tests: `./gradlew test`
