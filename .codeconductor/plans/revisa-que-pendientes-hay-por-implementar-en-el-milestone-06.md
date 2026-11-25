## Question

¿Qué pendientes quedan por implementar o cerrar en el Milestone 06 (`feature/milestone-06-testing`)?

## What the code does today

- En `BACKLOG.md` (líneas 320-390), los tres ítems del Milestone 06 — `BC-009` (*Establish layered feature testing*), `BC-010` (*Verify protected endpoint behavior*) y `BC-011` (*Enforce automated quality gates*) — figuran con `Status: DONE` y `Progress: 100%` en la rama `feature/milestone-06-testing`.
- En `build.gradle.kts` (líneas 197-258 y 734-804) están configurados `ktlint`, `detekt` (`autoCorrect = false` en la tarea `detekt`), `jacocoTestReport`, `jacocoTestCoverageVerification` (con mínimo `LINE COVEREDRATIO >= 0.85`) y la tarea `codeQuality` (`ktlintCheck -> detekt -> test -> jacocoTestReport -> jacocoTestCoverageVerification -> build`). `Makefile` (líneas 153-167) expone `quality` (`./gradlew codeQuality`) y `verify-quality`, y `README.md` (líneas 73, 163-175, 346-347) documenta `make quality`, `checkCodeStyle` y las rutas de reportes.
- La suite de pruebas en `src/test/kotlin/com/lgzarturo/springbootcourse/` ya incluye cobertura por capas para `features/hotels/` (`HotelServiceTest.kt`, `HotelControllerTest.kt`, `HotelJpaRepositoryTest.kt`, `HotelIntegrationTest.kt`, `HotelJpaRepositoryIntegrationTest.kt`, `HotelRoomJpaRepositoryIntegrationTest.kt`, `HotelE2ETest.kt`) y `features/users/` (`CreateUserServiceTest.kt`, `GetUserUseCaseTest.kt`, `UpdateUserUseCaseTest.kt`, `UserTest.kt`, `UserMapperTest.kt`, `valueobjects/*`, `UserControllerTest.kt`, `UserJpaRepositoryTest.kt`, `UserRoleAuthorizationTest.kt`, `UserIntegrationTest.kt`, `UserE2ETest.kt`), además de `config/security/ProtectedEndpointAuthorizationTest.kt`, `security/` (`JwtServiceTest.kt`, `JwtTestHelper.kt`, `WithMockUser.kt`, `SecurityTestConfig.kt`) y `support/` (`LayeredTestSupport.kt`, `SecurityTestConfig.kt`, `SecurityTestConfigIsolationTest.kt`).
- Sin embargo, existen cuatro brechas o desalineaciones observables respecto al cierre real del Milestone 06:
  1. **Reporte JaCoCo obsoleto y cobertura por debajo del umbral**: `build/reports/jacoco/test/jacocoTestReport.xml` tiene fecha `2026-04-08` (anterior al commit `aabb0c9` de `2026-10-19`) y reporta cobertura `LINE` de `428/674` (~63.5%), por debajo del mínimo `0.85` exigido en `build.gradle.kts:254`. No hay evidencia de que `make quality` se haya ejecutado sobre el estado actual.
  2. **Auditoría desactualizada**: `docs/pending-wip/auditoria-pendiente.md` (líneas 1-69) sigue reportando `UserController.kt` como `// PENDING` y un test `SKIPPED` en `UserControllerTest`, cuando `src/main/kotlin/com/lgzarturo/springbootcourse/features/users/UserController.kt` (214 líneas) ya está implementado y no existen tests `@Disabled`/`SKIPPED` en `src/test`. También lista brechas de cobertura anteriores (`features/sentry` 1.3%, `features/examples` 43.4%, `features/rooms` 60.8%, `common/extensions` 0%) y advertencias de nomenclatura (`UserRepository.kt` y `hotels/HotelRepositoryPort.kt`).
  3. **Documentación de historial incompleta**: `HISTORIAL.md` (líneas 209-233) solo documenta hasta el Milestone 5 (`feature/milestone-05-security`, tag `v0.1.0`); falta la entrada del Milestone 6.
  4. **Duplicación de configuración de pruebas de seguridad**: existe `SecurityTestConfig.kt` tanto en `src/test/kotlin/com/lgzarturo/springbootcourse/security/SecurityTestConfig.kt` como en `src/test/kotlin/com/lgzarturo/springbootcourse/support/SecurityTestConfig.kt`.

## Decision already in the code

- El umbral de calidad global ya está fijado en `build.gradle.kts` (`LINE COVEREDRATIO >= 0.85`, junto con `ktlintCheck` y `detekt` sin `autoCorrect`) y orquestado mediante `./gradlew codeQuality` (`make quality`).
- La arquitectura de pruebas en capas y los ítems `BC-009`, `BC-010` y `BC-011` en `BACKLOG.md` definen el alcance cerrado del Milestone 06, dejando cambios de CI para `BC-016` y evitando reescribir código de producción únicamente para subir cobertura fuera del alcance de `BC-011`.

## Assumptions

- Los ítems `BC-009`, `BC-010` y `BC-011` marcados como `DONE` en `BACKLOG.md` reflejan la intención de cierre funcional del Milestone 06, pero requieren verificación fresca contra el código actual (`make quality`).
- Las brechas listadas en `docs/pending-wip/auditoria-pendiente.md` sobre `features/sentry`, `features/examples`, `features/rooms` y `common/extensions` podrían seguir afectando el cálculo global de JaCoCo si no fueron cubiertas o excluidas después del `2026-04-08`.
- La duplicación de `SecurityTestConfig.kt` entre `security/` y `support/` no es intencional salvo que ambos paquetes sirvan propósitos distintos no documentados.

## Suggested change

1. Ejecutar `make quality` (`./gradlew codeQuality`) en el estado actual del repositorio para obtener un `jacocoTestReport.xml` vigente y verificar si `ktlintCheck`, `detekt`, `test` y `jacocoTestCoverageVerification` (>= 85% `LINE`) pasan realmente.
2. Si `jacocoTestCoverageVerification` falla por debajo de `0.85`, agregar pruebas en los paquetes con menor cobertura (`features/sentry`, `features/examples`, `features/rooms`, `common/extensions` o los que arroje el nuevo reporte) hasta superar el umbral sin relajar la regla en `build.gradle.kts`.
3. Consolidar `SecurityTestConfig.kt` entre `src/test/kotlin/com/lgzarturo/springbootcourse/security/SecurityTestConfig.kt` y `src/test/kotlin/com/lgzarturo/springbootcourse/support/SecurityTestConfig.kt` (manteniendo una sola fuente de verdad en `support/` o `security/` según `BC-010`).
4. Actualizar o archivar `docs/pending-wip/auditoria-pendiente.md` con los resultados reales tras correr `make quality`, y agregar la sección del Milestone 6 en `HISTORIAL.md`.

## Rejected alternatives

- **Bajar el umbral de JaCoCo en `build.gradle.kts` por debajo de `0.85`**: rechazado porque `BC-011` establece explícitamente el fallo del gate cuando la cobertura `LINE` global es menor al 85%.
- **Dar por cerrado el Milestone 06 solo por el estado `DONE` en `BACKLOG.md`**: rechazado porque el reporte JaCoCo en disco es previo a los últimos cambios (`2026-04-08`) y contradice el estado documental.

## Open questions

1. Al ejecutar `make quality` hoy, ¿pasa `jacocoTestCoverageVerification` (>= 85% `LINE`) o qué paquetes siguen por debajo del umbral?
2. ¿Cuál de los dos `SecurityTestConfig.kt` (`security/` vs `support/`) debe permanecer como único archivo?
3. ¿`docs/pending-wip/auditoria-pendiente.md` debe actualizarse con el nuevo resultado o eliminarse/archivarse una vez validado `make quality`?
4. ¿Se deben atender ahora las advertencias de nomenclatura de `auditoria-pendiente.md` (`UserRepository.kt` y `hotels/HotelRepositoryPort.kt`) o diferirlas fuera del Milestone 06?

## If you implement later

Para ejecutar la verificación de `make quality`, resolver las brechas resultantes y actualizar la documentación del Milestone 06 a partir de este memo (`.codeconductor/plans/revisa-que-pendientes-hay-por-implementar-en-el-milestone-06.md`), usa el comando `/cc-fix`.
