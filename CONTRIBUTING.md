# Guía de Contribución

¡Gracias por tu interés en contribuir a este curso/proyecto! Este repositorio es un recurso didáctico y abierto donde construiremos, paso a paso, una API REST real con Spring Boot 3 y Kotlin, inspirada en escenarios de hotelería, ecommerce y ocio.

Repositorio: https://github.com/lgzarturo/springboot-course

Objetivo: aprender haciendo, con desarrollo incremental, TDD cuando sea posible, y documentación clara del proceso.

---

## Formas de participar

Puedes contribuir de varias maneras:

- Reportando errores (*bugs*).
- Proponiendo mejoras o nuevas funcionalidades.
- Sugiriendo temas o módulos para el curso (*peticiones de temas*).
- Comentando tu experiencia de aprendizaje (*feedback didáctico*).
- Mejorando la documentación (*README, docs/ y Wiki*).
- Revisando PR's y ayudando a otras personas en Issues.

Usaremos los recursos de GitHub para coordinar el trabajo:

- **Issues:** tickets de tareas, dudas y seguimiento.
- **Projects:** tablero para visualizar el flujo (*Backlog, En progreso, Revisión, Hecho*).
- **Wiki:** documentación viva del proceso y conceptos complementarios.

---

## Requisitos y puesta en marcha local (resumen)

- JDK 21 o superior
- Git
- IntelliJ IDEA (*Community o Ultimate*) recomendado
- Gradle Wrapper incluido (*no necesitas instalar Gradle*)

Comandos útiles:

- Windows: `./gradlew.bat test` y `./gradlew.bat bootRun`
- macOS/Linux: `./gradlew test` y `./gradlew bootRun`

**Archivo principal:** `src/main/kotlin/com/lgzarturo/springbootcourse/SpringbootCourseApplication.kt`
**Configuración:** `src/main/resources/application.yml`

---

## Cómo abrir un Issue

Antes de crear un nuevo Issue, por favor:

1) Busca si ya existe uno similar.
2) Describe claramente el contexto y el objetivo.

Tipos de Issues sugeridos (*usa etiquetas si están disponibles*):

- **bug:** reportes de errores.
- **enhancement:** mejoras o nuevas funcionalidades.
- **documentation:** mejoras a la documentación o ejemplos.
- **topic-request:** peticiones de temas/módulos para el curso.
- **question:** dudas técnicas o de aprendizaje.
- **good first issue:** tareas amigables para comenzar.

Plantillas sugeridas (*puedes copiar y pegar*):

Reporte de bug:

- Contexto/versión (*SO, JDK, comandos ejecutados*)
- Pasos para reproducir
- Comportamiento esperado vs. actual
- Evidencia (*logs, mensajes de error, capturas*)
- Posible causa/idea (*opcional*)

Propuesta de mejora/tema:

- Problema/tema a resolver
- Motivación (*valor para el curso o producto*)
- Alcance (*qué se incluye, qué no*)
- Criterios de aceptación
- Impacto en docs/pruebas

Feedback de aprendizaje:

- Módulo o sección
- Qué fue claro/útil
- Qué puede mejorar
- Sugerencias concretas

Al crear el Issue, vincúlalo al Project del repositorio (*si está visible en la barra lateral*) y asigna etiquetas.

---

## Uso del Wiki

El Wiki servirá para:

- Explicar conceptos clave (*arquitectura, DDD, patrones, testing*).
- Documentar decisiones de diseño (*ADR ligeros si es necesario*).
- Guardar guías prácticas (*cómo depurar, cómo probar endpoints, etc.*).

Sugerencias:

- Estructura propuestas: "Arquitectura", "Casos de Uso", "Guías", "Decisiones".
- Mantén entradas breves, con ejemplos y enlaces al código o commits relevantes.
- Si propones cambios, crea un Issue con el bosquejo y enlázalo al Wiki cuando se publique.

---

## Project (tablero de trabajo)

- Columnas sugeridas: Backlog → En progreso → En revisión → Hecho.
- Vincula cada Issue/PR al Project para dar visibilidad.
- Actualiza el estado al avanzar (*lo puede hacer la persona asignada o quien revisa*  ).

---

## Flujo de trabajo para Pull Requests (PR)

1) Haz un fork del repositorio y crea una rama descriptiva desde `main`:
   - Convención sugerida: `feat/<area>-<breve-descripcion>` o `fix/<area>-<breve-descripcion>`
   - Ejemplos: `feat/reservas-creacion`, `fix/catalogo-paginacion`

2) Asegúrate de que tu cambio compila y pasa las pruebas:
   - `./gradlew[.bat] test`

3) Añade/actualiza pruebas cuando aplique (*TDD recomendado*):
   - Casos felices y bordes
   - Comportamiento observable (*controladores/servicios*)

4) Documenta los cambios:
   - README y/o docs/ si afecta a la comprensión del sistema
   - Wiki si es conocimiento conceptual o guía

5) Estilo de código:
   - Kotlin idiomático, claridad y simplicidad
   - Mantén funciones pequeñas y con responsabilidad única
   - Nombrado expresivo y pruebas legibles

6) Commits:
   - Mensajes claros y en español
   - Puedes seguir Conventional Commits (recomendado): `feat: ...`, `fix: ...`, `docs: ...`, `test: ...`, `refactor: ...`

7) Abre tu PR:
   - Describe el "qué" y el "por qué" (*contexto, diseño, alcance*)
   - Adjunta capturas/logs si son útiles
   - Indica cómo probarlo localmente
   - Enlaza el/los Issue(s) usando palabras clave (*Close/Fix/Resolve #n*)
   - Vincula el PR al Project correspondiente

8) Revisión y merge:
   - Se espera al menos una revisión
   - Pueden solicitarse cambios para mejorar claridad, pruebas o diseño
   - El merge se realizará cuando las comprobaciones pasen y haya conformidad

---

## Principios de diseño y pruebas

- Prioriza la claridad y la intención sobre la "magia".
- Construcción incremental, pequeñas PR's.
- TDD cuando sea posible: primero las pruebas, luego la implementación.
- Preferir composición sobre herencia; principios SOLID.
- Evita el sobre-diseño; documenta decisiones importantes.

---

## Conducta y licencia

- Sé respetuoso y constructivo. Este es un espacio de aprendizaje.
- No spam, no contenido ofensivo. Sigue las [GitHub Community Guidelines].
- Al contribuir, aceptas que tu aportación se publique bajo la licencia del repositorio: [CC-BY-4.0](LICENSE).
  - La atribución requerida es para: Arturo López (ver LICENSE).

Contacto del autor: lgzarturo@gmail.com

---

## ¿Primera contribución?

- Revisa Issues con etiqueta `good first issue` o `help wanted`.
- Comenta en el Issue para que te asignen (*si aplica*) y evita duplicar trabajo.
- Si tienes dudas, abre un Issue con la etiqueta `question`.

---

## Enlaces rápidos

- Repositorio: https://github.com/lgzarturo/springboot-course
- Preguntas frecuentes: [docs/FAQ.md](docs/FAQ.md)
- Documentación del curso: [docs/README.md](docs/README.md)
- Licencia: [LICENSE](LICENSE)

¡Gracias por contribuir! Tu participación ayuda a que más personas aprendan a construir software con buenas prácticas y propósito.
