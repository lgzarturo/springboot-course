# Variables de entorno

Las variables de entorno sirven para configurar aspectos importantes del sistema.

En este documento se describen las variables de entorno que se utilizan en el proyecto.

## Sentry

Para el proyecto se utiliza Sentry para el registro de errores.

- `SENTRY_AUTH_TOKEN` - Este token es necesario para la integración continua y despliegue automático (CI/CD) para poder subir los sourcemaps a Sentry.
- `SENTRY_DSN` - DSN del proyecto en Sentry, esta variable es necesaria para que el proyecto funcione y registre errores en Sentry.
- `SENTRY_ENVIRONMENT` - Define el entorno en el que se está ejecutando la aplicación (por ejemplo, `development`, `staging`, `production`).
- `SENTRY_DEBUG` - Esta variable activa el modo de depuración de Sentry, lo que puede ser útil para solucionar problemas durante el desarrollo.
- `SENTRY_ENABLED` - Indica si Sentry está habilitado o no en la aplicación.
- `SENTRY_LOGGING_ENABLED` - Habilita o deshabilita el registro de eventos de Sentry en los logs de la aplicación.
- `SENTRY_TRACES_SAMPLE_RATE` - Define la probabilidad de que se registren los eventos de seguimiento de Sentry.

## PostgreSQL

Para la base de datos se utiliza PostgreSQL y estas son las variables de entorno que se utilizan:

- `DB_URL` - URL de la base de datos.
- `DB_HOST` - Host de la base de datos.
- `DB_PORT` - Puerto de la base de datos.
- `DB_NAME` - Nombre de la base de datos.
- `DB_USER` - Usuario de la base de datos.
- `DB_PASSWORD` - Contraseña de la base de datos.