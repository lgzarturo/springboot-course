# Lecciones aprendidas

Kai, el programador aprendiz, llega al Hotel Pokémon, donde cada módulo es una "habitación" o "gimnasio" que debe superar para ganar insignias (badges) y avanzar en su entrenamiento como Maestro Spring Boot. El hotel está dirigido por el Profesor Oak, quien asigna misiones (módulos) y evalúa el progreso. Cada insignia representa un logro en un área específica de Spring Boot/Kotlin, y al final, Kai podrá desafiar a la Liga Pokémon de Desarrolladores (proyecto final).

**Personajes clave:**

- **Profesor Oak:** Guía y mentor (es el perfil de GitHub que comente en los PRs).
- **Líderes de Gimnasio:** Cada módulo tiene un "líder" (ej.: Líder Brock para Seguridad, Líder Misty para Bases de Datos).
- **Team Rocket:** Representa los bugs, errores comunes o malas prácticas que Kai debe evitar/arreglar.

> **[HISTORIAL](HISTORIAL.md):** Revisa el listado de logros para ver las insignias que se pueden conseguir, es la base para el desarrollo del proyecto final.

---

## Misiones

### Configuración de Sentry

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

#### 🏆 Insignia - Reliquias de Entidades (Configuración de Sentry)

![Insignia - Configuración de Sentry](https://raw.githubusercontent.com/wiki/lgzarturo/springboot-course/badges/badget-03-reliquias-entidades_1024x1024.webp)

---

### Perfiles de Spring Boot

**Fecha:** 2025-10-27

**Logros:**

- **Definir los perfiles**: Se agregan 3 perfiles para la aplicación Spring Boot: "dev", "prod" y "test". Cada perfil tiene su propia configuración de base de datos y otras propiedades específicas, además que el archivo `application.yml` queda con la configuración base y los perfiles sobreescriben algunas propiedades y definen otras. 
- **Cobertura completa de perfiles**: Se configuraron y validaron los perfiles dev, prod y test, con `SPRING_PROFILES_ACTIVE` apuntando a dev por defecto y prod utilizado en staging para reproducir el entorno real.
- **Aislamiento de configuraciones sensibles**: Se incorporó `.env` como fuente opcional para credenciales, permitiendo levantar staging sin exponer datos duros y manteniendo los defaults seguros.
- **Optimización por entorno**: Se ajustaron pools de Hikari, logging y parámetros de Hibernate según el caso de uso (diagnóstico en dev, observabilidad controlada en prod y silencio en test) para garantizar estabilidad en staging.
- **Integración con herramientas externas**: Se activó Docker Compose automático en dev y se parametrizó Sentry por entorno, verificando en staging que sólo prod envía eventos con el sample rate esperado.

**Errores y soluciones:**

- **Migraciones en staging**: Al ejecutar staging con prod, Flyway intentó recrear estructuras inexistentes por heredar `ddl-auto=update`; se corrigió usando `ddl-auto=validate` en dev/prod y `create-drop` en test para aislar el esquema.
- **Ruido de logs**: El perfil base heredaba trazas DEBUG/TRACE en staging; se normalizó en prod para dejar root en INFO y reducir la carga de almacenamiento y monitoreo.
- **Eventos duplicados en Sentry**: Con el sample rate por defecto del perfil base se generaban eventos redundantes en staging; se ajustó `traces-sample-rate` en prod y se deshabilitó completamente en test para evitar consumo innecesario.

---