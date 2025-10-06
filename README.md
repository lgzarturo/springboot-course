# Curso Spring Boot 3 – API REST real

**Idiomas:** Español

¡Bienvenidas y bienvenidos! 

Imagina que eres un desarrollador en una startup de tecnología para hoteles. Tu misión: construir desde cero una plataforma que revolucione la gestión hotelera y el ecommerce de amenities. Pero no estás solo: este curso es tu guía, tus herramientas son Spring Boot y Kotlin, y cada ejercicio es un desafío real que te preparará para el mundo laboral.

Este curso te guiará para construir, paso a paso, una API REST real con Spring Boot 3 y Kotlin. El producto que desarrollaremos se inspira en escenarios del sector de hotelería, ecommerce y ocio, de modo que aprenderás conceptos técnicos mientras enfrentas problemas de negocio reales.

¿Listo para empezar? Aquí no solo aprenderás código: resolverás problemas, optimizarás procesos y crearás soluciones que impacten. ¡Vamos a ello!

---

## ¿Qué vamos a construir?

Este curso es como un mapa del tesoro. Cada módulo es una isla con conocimientos clave, y los ejercicios son cofres que debes abrir para avanzar. Al final, tendrás las habilidades para construir tu propia plataforma hotelera o de ecommerce, lista para escalar.

Desarrollaremos una API REST con capas bien definidas y enfoque de dominio, abordando casos de uso como:

- **Hotelería:** gestión de habitaciones, disponibilidad, reservas y check-in/out.
- **Ecommerce:** catálogo de productos/servicios, carritos/pedidos, pagos y estado de órdenes.
- **Ocio:** ejemplos lúdicos como Pokémon u otras API's públicas para ejemplificar patrones y conceptos.

El objetivo no es solo "hacer que funcione", sino diseñar una base sólida y extensible, con buenas prácticas y foco en las reglas de negocio.

---

## Enfoque didáctico y metodología

- **Aprender haciendo:** cada avance del repositorio añade una pieza del sistema.
- **Desarrollo incremental:** actualizaciones semanales con pequeños incrementos y mejoras continuas.
- **TDD (*Test-Driven Development*):** planificaremos gran parte del desarrollo escribiendo primero las pruebas, usando JUnit y Spring Boot Test para validar comportamientos.
- **Buenas prácticas:** claridad, simplicidad, principios SOLID y orientación a dominio.
- **Más que código:** entender las reglas de negocio y el producto es esencial para crecer profesionalmente.

---

## El repositorio como recurso gratuito

Este repositorio es un recurso abierto para que puedas descargar el código fuente, ejecutarlo localmente y estudiar su evolución. Toda la documentación del proyecto se escribirá dentro del repositorio y se mantendrá junto al código, explicando conceptos y decisiones.

> **Recomendación clave**: revisa el histórico de Git para comprender el proceso de desarrollo. Los commits, ramas y (*cuando corresponda*) tags y releases serán tu mapa de aprendizaje.

---

## Cómo seguir el histórico de desarrollo

- En GitHub: visita la sección de Commits para ver los cambios en orden cronológico.
- En tu entorno local:
  - `git pull` para traer actualizaciones semanales.
  - `git log --oneline --graph --decorate` para visualizar el flujo.
  - Revisa los mensajes de commit y las diferencias para entender el “por qué” de cada cambio.

---

## Actualizaciones semanales

Cada semana publicaré incrementos que pueden incluir nuevas funcionalidades, pruebas, refactorizaciones o documentación adicional. La idea es simular un flujo de trabajo real y constante.

---

## Documentación dentro del proyecto

- Documentos y guías convivirán con el código (*README.md por carpetas/módulos cuando aporte valor*).
- Se explicarán conceptos, arquitectura y decisiones (*por qué se elige una estrategia sobre otra*).
- Usaremos los recursos de GitHub para un repositorio robusto:
  - Issues para tareas y seguimiento.
  - Projects/Boards para planificación.
  - Discussions (*opcional*) para preguntas y debate.
  - Releases/Tags para hitos.
  - (*Opcional*) GitHub Actions para CI, cuando sea pertinente.

---

## Requisitos

Es importante contar con lo siguiente:

- Conocimientos básicos de Kotlin.
- JDK 21 o superior instalado.
- Git instalado.
- IDE recomendado: IntelliJ IDEA (*Community o Ultimate*).
- No necesitas instalar Gradle: este proyecto incluye Gradle Wrapper.

---

## Puesta en marcha (local)

1) Clona el repositorio
   - `git clone https://github.com/lgzarturo/springboot-course.git`
   - `cd springboot-course`

2) Ejecuta pruebas
   - Windows: `.\gradlew.bat test`
   - macOS/Linux: `./gradlew test`

3) Levanta la aplicación
   - Windows: `.\gradlew.bat bootRun`
   - macOS/Linux: `./gradlew bootRun`

4) Abre en tu IDE y explora el código
   - Archivo principal: `src/main/kotlin/com/lgzarturo/springbootcourse/SpringbootCourseApplication.kt`
   - Configuración: `src/main/resources/application.yml`

---

## Estructura de aprendizaje sugerida

1) Lee el README inicial y los documentos de la carpeta que estés trabajando.
2) Revisa el histórico de commits para ver cómo se construyó la funcionalidad.
3) Corre las pruebas y lee sus casos para entender el comportamiento esperado (*TDD*).
4) Ejecuta la app, prueba los endpoints y mira los logs.
5) Reflexiona sobre las decisiones de diseño y las reglas de negocio involucradas.

---

## Objetivo para perfiles junior

Este curso busca crear una base de formación sólida: aprenderás a implementar buenas prácticas, entender flujos de trabajo profesionales y, sobre todo, valorar la importancia de las reglas de negocio y el conocimiento del producto para avanzar en tu carrera.

---

## Preguntas y soporte

Abre un Issue con tus dudas o propuestas de mejora. Tu retroalimentación ayudará a que el contenido sea cada vez más claro y útil para la comunidad.

- [Sección de preguntas y respuestas](docs/FAQ.md)

---

## El Legado del Desarrollador

Ahora eres parte de una comunidad de desarrolladores que no solo escriben código, sino que resuelven problemas reales. Tu viaje no termina aquí: sigue explorando, contribuye a proyectos open source, y recuerda que cada línea de código que escribes puede mejorar la experiencia de miles de usuarios. ¡El mundo necesita más arquitectos como tú!

- [Prefacio del autor](AUTHOR.md)
- [Temario: El principio del viaje ¿Qué puedes aprender?](docs/README.md)

---

## Licencia

Este repositorio está licenciado bajo [CC-BY-4.0](LICENSE). Atribución requerida a: **Arturo López** ([lgzarturo@gmail.com](mailto:lgzarturo@gmail.com))
