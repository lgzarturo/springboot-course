# Variables de entorno

Las variables de entorno sirven para configurar aspectos importantes del
sistema.

En este documento se describen las variables de entorno que se utilizan en el
proyecto.

## Sentry

Para el proyecto se utiliza Sentry para el registro de errores.

- `SENTRY_AUTH_TOKEN` - Este token es necesario para la integración continua y
  despliegue automático (CI/CD) para poder subir los sourcemaps a Sentry.
- `SENTRY_DSN` - DSN del proyecto en Sentry, esta variable es necesaria para que
  el proyecto funcione y registre errores en Sentry.
- `SENTRY_ENVIRONMENT` - Define el entorno en el que se está ejecutando la
  aplicación (por ejemplo, `development`, `staging`, `production`).
- `SENTRY_DEBUG` - Esta variable activa el modo de depuración de Sentry, lo que
  puede ser útil para solucionar problemas durante el desarrollo.
- `SENTRY_ENABLED` - Indica si Sentry está habilitado o no en la aplicación.
- `SENTRY_LOGGING_ENABLED` - Habilita o deshabilita el registro de eventos de
  Sentry en los logs de la aplicación.
- `SENTRY_TRACES_SAMPLE_RATE` - Define la probabilidad de que se registren los
  eventos de seguimiento de Sentry.

## PostgreSQL

Para la base de datos se utiliza PostgreSQL y estas son las variables de entorno
que se utilizan:

- `DB_URL` - URL de la base de datos.
- `DB_HOST` - Host de la base de datos.
- `DB_PORT` - Puerto de la base de datos.
- `DB_NAME` - Nombre de la base de datos.
- `DB_USER` - Usuario de la base de datos.
- `DB_PASSWORD` - Contraseña de la base de datos.

## Seguridad y JWT

Configuración para autenticación sin estado mediante tokens JWT (firmados con HMAC-SHA256):

- `JWT_SECRET` - Clave secreta para la firma criptográfica de tokens JWT. Requiere un mínimo de 256 bits (32 bytes). Generación recomendada: `python3 -c "import secrets; print(secrets.token_urlsafe(48))"`.
- Contrato JWT vigente: `sub`=userId, claims `email`, `jti`, `v` (versión de revocación). Los tokens legacy sin `jti`/`v` se rechazan.
- `APP_SECURITY_ALLOWED_ORIGINS` - Orígenes CORS permitidos separados por coma (defecto: `http://localhost:3000,http://localhost:4200`). En prod fijar al dominio del frontend.
- `AUTH_RATE_LIMIT_ENABLED` - Activa el rate limiting en auth (defecto: `true`; `false` en perfil `test`).
- `AUTH_RATE_LIMIT_PER_MINUTE` - Intentos por minuto e IP+endpoint en login/registro (defecto: `5`).
- `AUTH_RATE_LIMIT_TRUST_FORWARDED_HEADERS` - Confía en `X-Forwarded-For` para la IP cliente (defecto: `false`; activar solo tras un proxy que lo sanee).
