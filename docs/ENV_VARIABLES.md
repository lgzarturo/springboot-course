# Variables de entorno

Las variables de entorno sirven para configurar aspectos importantes del sistema.

En este documento se describen las variables de entorno que se utilizan en el proyecto.

## Sentry

Para el proyecto se utiliza Sentry para el registro de errores.

- `SENTRY_DSN` - DSN del proyecto en Sentry, esta variable es necesaria para que el proyecto funcione y registre errores en Sentry.
- `SENTRY_AUTH_TOKEN` - Este token es necesario para la integración continua y despliegue automático (CI/CD) para poder subir los sourcemaps a Sentry.