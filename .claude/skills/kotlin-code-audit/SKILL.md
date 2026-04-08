---
name: kotlin-code-audit
description: >
  Audita calidad de código Kotlin/Spring Boot en este proyecto. Úsalo cuando el usuario
  pida revisar código, verificar estándares, hacer un code review, auditar calidad,
  ejecutar linters, verificar cobertura, o antes de hacer commit/PR. También úsalo
  cuando mencionen KTLint, Detekt, JaCoCo, cobertura, o simplemente digan "revisa el
  código" / "está listo para commit" / "qué tan bueno está el código".
---

# Kotlin Code Audit — springboot-course

Audita el código del proyecto contra los estándares definidos: KTLint, Detekt, JaCoCo y convenciones de arquitectura MVC por feature.

## Cuándo usar esta skill

- El usuario pide revisar, auditar, o validar código
- Antes de hacer commit, push, o crear un PR
- Cuando se implementa una feature nueva o se corrige un bug
- Cuando preguntan si el código cumple con los estándares

## Paso 1: Ejecutar las herramientas automáticas

Corre los comandos en este orden. Muestra el output relevante al usuario (errores y warnings, no el ruido).

```bash
# 1. Formateo y lint automático (aplica correcciones)
make format

# 2. Verificación estricta (solo verifica, no modifica)
make lint

# 3. Tests + cobertura
make test
make coverage-check
```

Si `make format` falla, investiga el error antes de continuar — no lo ignores.

Si `make coverage-check` falla, significa que la cobertura está bajo 85%. Reporta qué clases/paquetes tienen menos cobertura abriendo el reporte HTML en `build/reports/jacoco/test/html/index.html`.

## Paso 2: Revisar el output de Detekt y KTLint

**Reportes generados:**
- KTLint: `build/reports/ktlint/`
- Detekt: `build/reports/detekt/` (HTML + SARIF)
- JaCoCo: `build/reports/jacoco/test/html/index.html`

Si hay errores que no se autocorrigieron, reporta exactamente qué archivo y línea, con el mensaje de error. No hagas suposiciones — lee el reporte.

## Paso 3: Revisar convenciones de arquitectura manualmente

Verifica estos puntos en el código modificado o que te indica el usuario:

### Estructura por feature (Screaming Architecture)

```
features/
└── mi-feature/
    ├── MiController.kt     ← solo HTTP, sin lógica de negocio
    ├── MiService.kt        ← lógica de negocio
    ├── MiJpaRepository.kt  ← Spring Data JPA
    ├── MiEntity.kt         ← @Entity JPA
    ├── Mi.kt               ← modelo de dominio puro (sin Spring)
    └── dto/
        ├── CreateMiRequest.kt
        └── MiResponse.kt
```

**Regla de oro:** máximo 2 niveles de anidación (`features/mi-feature/dto/`). Si hay más, la feature es demasiado grande.

### Flujo MVC correcto

- Controllers → Services → Repositories (nunca saltarse pasos)
- El controller mapea Request → dominio antes de llamar al servicio
- Los DTOs **no entran** al servicio como DTOs
- El dominio (`Mi.kt`) no tiene `@Entity` ni dependencias de Spring

### Features autocontenidas

- No hay imports cruzados entre features
- La comunicación entre features va por `common/` o eventos Spring

### Naming conventions

| Artefacto | Convención |
|-----------|------------|
| Controller | `*Controller.kt` |
| Service | `*Service.kt` |
| Repositorio JPA | `*JpaRepository.kt` |
| Entidad JPA | `*Entity.kt` |
| Modelo de dominio | sin sufijo (ej: `Hotel.kt`) |
| DTO entrada | `*Request.kt` |
| DTO salida | `*Response.kt` |
| Value object | en `valueobjects/` |
| Configuración | `*ServiceConfig.kt` |
| Excepción de dominio | `*Exception.kt` |

## Paso 4: Revisar cobertura de tests

El proyecto exige **≥ 85% de cobertura**. Para cada código nuevo:

- Servicios: deben tener tests unitarios con MockK (sin Spring context)
- Controllers: tests con `@WebMvcTest` + `MockMvc`
- Repositorios: tests con `@DataJpaTest`
- E2E: si es un flujo completo, verificar si aplica un test E2E

Si falta cobertura, indica exactamente qué clases/métodos no están cubiertos y sugiere qué tipo de test agregar.

## Paso 5: Revisar convenciones de tests

```kotlin
// ✅ Test unitario correcto
@ExtendWith(MockKExtension::class)
class HotelServiceTest {
    @MockK lateinit var repository: HotelJpaRepository
    @InjectMockKs lateinit var service: HotelService

    @Test
    @DisplayName("should return hotel when found")
    fun `should return hotel when found`() { ... }
}

// ✅ Test de controller correcto
@WebMvcTest(HotelController::class)
@Import(SecurityTestConfig::class)
class HotelControllerTest { ... }
```

Verifica: nombres descriptivos en backticks, `@DisplayName` para legibilidad, MockK (no Mockito).

## Paso 6: Generar reporte de auditoría

Al final, entrega un resumen estructurado:

```
## Resultado de auditoría

### Herramientas automáticas
- KTLint: ✅ sin errores / ❌ N errores
- Detekt: ✅ sin errores / ❌ N warnings
- Tests: ✅ todos pasan / ❌ N fallos
- Cobertura: ✅ 87% / ❌ 72% (bajo el umbral 85%)

### Arquitectura
- [✅/❌] Estructura de features correcta
- [✅/❌] Flujo MVC respetado
- [✅/❌] Naming conventions
- [✅/❌] Features autocontenidas

### Tests
- [✅/❌] Cobertura suficiente
- [✅/❌] Convenciones de tests correctas

### Issues encontrados
1. [CRÍTICO/ADVERTENCIA] descripción del problema — archivo:línea
   Solución sugerida: ...

### Conclusión
[Listo para commit ✅ / Requiere correcciones ❌]
```

## Comandos de referencia rápida

```bash
make lint           # solo verifica (KTLint + Detekt)
make format         # aplica correcciones automáticas
make fix            # alias de format
make quality        # gate completo: lint + tests + cobertura
make test           # solo tests
make coverage       # tests + reporte JaCoCo
make coverage-check # verifica umbral 85%
make ci             # simula CI completo (build + quality)
make lint-report    # genera reportes en build/reports/
```

## Configuración del proyecto

- **KTLint:** 1.7.1 — línea máx 120 chars (`.editorconfig`)
- **Detekt:** 2.0.0-alpha.1 — línea máx 160 chars, return count ≤ 3
- **JaCoCo:** umbral mínimo 85% global
- **Gradle tasks:** `checkCodeStyle`, `formatCode`, `codeQuality`, `detektAutoCorrect`
