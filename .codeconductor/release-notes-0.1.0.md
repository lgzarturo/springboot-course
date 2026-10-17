Milestone 5: Autenticación, JWT, RBAC y Sistema de Usuarios — Implementación completa del módulo de seguridad con Spring Security 6.x, arquitectura de tokens JWT sin estado, control de acceso basado en roles (RBAC: GUEST, STAFF, ADMIN), CRUD de usuarios con Value Objects, migraciones de base de datos Flyway V3 y V4, y estrategia de pruebas en 5 capas.

### Added

- **Spring Security 6.x & Arquitectura Stateless**:
  - `SecurityConfig` con `SecurityFilterChain` completamente sin estado (`SessionCreationPolicy.STATELESS`)
  - Autenticación mediante tokens JWT usando la biblioteca JJWT 0.12.6
  - `JwtTokenProvider` para generación, firmado criptográfico (HMAC-SHA256) y validación de tokens
  - `JwtAuthenticationFilter` para intercepción e inyección de contexto de seguridad (`SecurityContextHolder`)
  - Manejadores de excepciones REST: `RestAuthenticationEntryPoint` (401 Unauthorized) y `RestAccessDeniedHandler` (403 Forbidden)
  - Configuración de codificador de contraseñas con `BCryptPasswordEncoder`
- **Control de Acceso Basado en Roles (RBAC)**:
  - Definición de roles de dominio: `GUEST`, `STAFF`, `ADMIN`
  - Mapeo y evaluación de autoridades granulares para permisos de acceso
  - Configuración de seguridad a nivel de método (`@EnableMethodSecurity`, `@PreAuthorize`)
  - Reglas de autorización en endpoints para rutas públicas y protegidas
- **Módulo de Usuarios (CRUD & Domain)**:
  - Entidad de dominio `User` con Value Objects seguros:
    - `UserId`, `Email`, `Password`, `PhoneNumber`, `Role`
  - `UserService` con validación de invariantes de negocio y detección de emails duplicados
  - `UserDto`, `UserRequest`, `UserResponse` y `UserMapper` para transformación limpia entre capas
  - `UserRepository` y `UserJpaRepository` con consultas optimizadas
- **Migraciones de Base de Datos (Flyway)**:
  - `V3__create_users_table.sql`: Definición de tabla `users` con índices únicos
  - `V4__add_roles_to_users.sql`: Estructura y constraints para roles de usuario
- **Estrategia de Pruebas en 5 Capas**:
  - Pruebas unitarias de dominio y servicios con MockK
  - Pruebas de capa web (@WebMvcTest) con soporte de seguridad simulada
  - Pruebas de capa de datos (@DataJpaTest) con base de datos H2
  - Pruebas de integración de servicio con contexto de Spring
  - Pruebas End-to-End (E2E) con `TestRestTemplate` / MockMvc
  - Utilidades de prueba: `@WithMockDomainUser`, `SecurityTestConfig`, `LayeredTestSupport`
- **Automatización de Release y Versionado Semántico**:
  - Tareas de Gradle para cálculo e incremento de versiones SemVer (`bumpVersion`, `bumpPatch`, `bumpMinor`, `bumpMajor`, `createRelease`)
  - Comandos en `Makefile`: `make release`, `make release-patch`, `make release-minor`, `make release-major`, `make bump-version`
  - Sincronización automática de versiones entre `build.gradle.kts`, `OpenApiConfig`, `Ping`, pruebas y `README.md`
  - Generación automática de tags de Git (`v0.1.0`) y actualización de `CHANGELOG.md`
