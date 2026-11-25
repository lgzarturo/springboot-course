# ADR-001: Revocación de tokens JWT y endurecimiento de autenticación

Fecha: 2026-11-25
Estado: aceptado

## Contexto

La auditoría de seguridad encontró que los JWT eran puramente stateless:
sin `jti`, sin revalidación contra BD y sin logout. Un usuario desactivado
o con rol cambiado conservaba un token válido hasta 15 minutos, y no había
forma de revocar un token robado. Además: sin rate limiting en auth (H1),
lectura de perfiles sin control de objeto (H2), BCrypt cost 10 (H4),
passwords sin longitud máxima (H5: Spring rechaza >72 bytes con 500),
CORS `*` con credenciales fuera de la cadena de seguridad (M1), sin
security headers explícitos (M2), sin logs de seguridad (M3), NPE con
claim `roles` ausente (M4), enumeración de emails en 409 (M5), emails sin
normalizar (M6) y enumeración por timing en login (M7).

## Decisión

### Contrato JWT (cambio rompedor)

- `sub` pasa de email a userId (`Long`); el email queda como claim.
  El principal de Spring es el userId, estable e inmutable.
- Nuevos claims: `jti` (UUID v4), `v` (versión del token), `email`.
- Tokens legacy sin `jti`/`v` o con `sub` no numérico se rechazan (401).
  Ventana máxima de impacto: 15 minutos (TTL vigente).

### Revocación en dos niveles (sin infra nueva)

1. Individual (logout): denylist `revoked_tokens(jti, user_id, expires_at,
   revoked_at, reason)` en Postgres + `POST /api/v1/auth/logout` (204,
   idempotente). Se consulta siempre, sin caché: lookup por PK.
2. Masiva (desactivación, cambio de rol/password): columna
   `users.token_version` comparada con el claim `v`. Un bump invalida
   todos los tokens del usuario sin escribir una fila por token.

El filtro valida firma → denylist → usuario (caché Caffeine 60 s) →
`isActive` + versión. Ventana de inconsistencia: 0 s en logouts,
60 s en cambios de usuario. La tabla se purga a diario (`@Scheduled`,
`deleteByExpiresAtBefore`): vive ~15 min de logouts.

### Resto del endurecimiento

- Autorización por objeto: `@PreAuthorize("hasRole('ADMIN') or
  #id == authentication.principal")` en `GET /users/{id}`; handler
  global `AccessDeniedException` → 403.
- Rate limiting propio con Caffeine (ventana deslizante, 5/min por
  IP+endpoint en `POST /api/v1/auth/*` y `POST /api/v1/users`, 429 con
  `Retry-After`). `X-Forwarded-For` solo se confía con
  `auth.rate-limit.trust-forwarded-headers=true`.
- BCrypt cost 12; passwords 8–72 en DTOs y dominio; emails en
  minúsculas (`V11` normaliza datos existentes); hash dummy cost 12
  cuando el usuario no existe; 409 genérico sin email.
- CORS allowlist explícita dentro de la cadena de seguridad
  (`app.security.allowed-origins`, sin `*`); headers HSTS, frame deny,
  nosniff, referrer y CSP (relajada solo en dev para Swagger UI).
- Logs: login ok/fallo (email enmascarado), denegaciones y logout
  (userId+jti). Nunca passwords, tokens ni emails completos.

## Alternativas rechazadas

- Denylist en Redis: estándar a escala, pero servicio nuevo para una
  tabla de ~15 min de vida. El puerto `TokenRevocationPort` encapsula
  la migración futura.
- Refresh tokens con rotación: duplica el alcance; fase posterior.
- Bucket4j: dependencia externa con dudas de compatibilidad Spring
  Boot 4; el filtro Caffeine (~80 líneas) es suficiente en un nodo.
- Revalidación total sin caché: latencia por request innecesaria.
- Clock skew 60 s: relajaba la expiración y rompía la semántica de
  los tests de tokens expirados; mismo nodo firma y verifica.

## Consecuencias

- Clientes deben re-autenticarse tras el despliegue (tokens legacy
  rechazados) y tolerar `sub` numérico.
- `V9`/`V10`/`V11` (Flyway, validadas en Postgres 17 y H2).
  `V11` falla ruidosamente si existen emails que colisionan en
  minúsculas: resolver manualmente antes de reintentar.
- Rate limiting en memoria: pierde estado al reiniciar y no coordina
  réplicas; suficiente para esta escala.
- En prod, fijar `APP_SECURITY_ALLOWED_ORIGINS` al dominio del
  frontend y `trust-forwarded-headers` solo tras un proxy que
  sanee `X-Forwarded-For`.
