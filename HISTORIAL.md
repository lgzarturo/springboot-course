# Lecciones aprendidas

## Configuración de Sentry

**Fecha:** 2025-10-26

**Logros:**

- **Integración de Sentry**: Aprendí cómo configurar y utilizar Sentry para capturar y monitorear errores en una aplicación Spring Boot. Utilicé la biblioteca Sentry para capturar excepciones y enviar informes de errores a la plataforma.
- **Excepciones personalizadas**: Aprendí a crear excepciones personalizadas para simular eventos de error y validar la integración con Sentry. Utilicé la clase SentryTestException para lanzar excepciones específicas para pruebas.
- **Captura de excepciones**: Aprendí cómo capturar excepciones utilizando bloques try-catch y enviar informes de errores a Sentry utilizando el método captureException().
- **Documentación de código**: Aprendí a documentar clases y métodos utilizando comentarios Javadoc para mejorar la legibilidad y mantenibilidad del código.

**Errores y soluciones:**

- **Detekt:** Se encontró que en el archivo SentryController.kt un error de detekt, al momento de capturar la excepcion, ya que al estar capturando la excepcion en el catch se puede lanzar cualquier excepcion con Exception y eso no es recomendable. Lanza el error TooGenericExceptionCaught. 
  - Esto se evita creando una Exception personalizada para capturar la excepcion y lanzarla en el catch.
- **KLint:** Al compilar el proyecto se encontraron errores de KLint, estos errores se corrigieron. Esto es especialmente importante para mantener la calidad del código y evitar problemas de compilación.
  - Es importante corregir estos errores para que el proyecto se compile correctamente.
