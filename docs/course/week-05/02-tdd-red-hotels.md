# Siguiendo el flujo TDD

En este artículo, exploramos cómo implementar la gestión de hoteles en una
aplicación Spring Boot con Kotlin, siguiendo Test-Driven Development (TDD) y una
arquitectura hexagonal combinada con screaming architecture (donde el código
"grita" su propósito a través de nombres y estructuras claras). El enfoque es
escribir pruebas primero para guiar la implementación, asegurando código robusto
y mantenible.

El ciclo TDD es simple: Rojo (prueba falla porque la funcionalidad no existe),
Verde (implementa lo mínimo para que pase), Refactor (mejora el código sin
romper pruebas). Aplicaremos esto a casos como agregar, eliminar, actualizar y
listar hoteles. Nuestro objetivo es garantizar la calidad y el diseño
desacoplado escribiendo las pruebas antes de la implementación real.

## Pasos Iniciales en TDD

1. Escribir Pruebas Unitarias Primero: Define casos de uso clave en pruebas.
   Estas fallarán inicialmente, validando que detectan ausencias de
   funcionalidad.
2. Crear Clases Esqueléticas: Define clases mínimas (e.g., entidades, servicios)
   para que el proyecto compile y las pruebas se ejecuten (aunque fallen).
3. Estructura por Capas (Hexagonal + Screaming): Organiza en dominio (lógica de
   negocio), aplicación (orquestación) e infraestructura (adaptadores como JPA).
   Replica esta estructura en pruebas para aislar capas.

> Recuerda: Enfócate solo en el código nuevo. No pruebes frameworks como Spring
> ni funcionalidades existentes. Usa anotaciones como
> @file:Suppress("LongMethod") temporalmente en Kotlin con Detekt para
> desactivar reglas durante el esqueleto; elimínalas al refactorizar.

## Estrategia de Pruebas: Los 6 Niveles

1. [Pruebas Unitarias del Servicio de Dominio](../../../src/test/kotlin/com/lgzarturo/springbootcourse/hotels/service/HotelServiceTest.kt)
   (HotelService): Estas pruebas deben estar aisladas del framework Spring y
   validar la lógica de negocio pura. Se utiliza Mockk para simular los puertos
   de salida.
2. [Pruebas Integración del Controlador](../../../src/test/kotlin/com/lgzarturo/springbootcourse/hotels/adapters/rest/HotelControllerTest.kt)
   (HotelController): Estas pruebas sí utilizan Spring Boot y validan el
   comportamiento del endpoint REST. Se simula el servicio de dominio para
   aislar la capa de entrada.
3. [Pruebas Unitarias del Adaptador de Salida](../../../src/test/kotlin/com/lgzarturo/springbootcourse/hotels/adapters/persistence/HotelJpaRepositoryTest.kt)
   (JpaHotelRepository): Estas pruebas se centran en la lógica de mapeo y en la
   interacción con el repositorio JPA simulado. Deben ser rápidas y no requerir
   una base de datos real.
4. [Pruebas de Integración con @SpringBootTest y H2](../../../src/test/kotlin/com/lgzarturo/springbootcourse/hotels/integration/HotelIntegrationTest.kt):
   Estas pruebas levantan el contexto completo de Spring y utilizan una base de
   datos en memoria para verificar el comportamiento real de la aplicación.
5. [Pruebas de Integración con @DataJpaTest (Opcional):](../../../src/test/kotlin/com/lgzarturo/springbootcourse/hotels/adapters/persistence/HotelJpaRepositoryIntegrationTest.kt)
   Si deseas probar específicamente la capa de persistencia, puedes usar
   @DataJpaTest.
6. [Pruebas E2E con @SpringBootTest y TestRestTemplate](../../../src/test/kotlin/com/lgzarturo/springbootcourse/hotels/integration/e2e/HotelE2ETest.kt):
   Esta es la forma más común de realizar pruebas E2E para API's REST en Spring
   Boot. Se levanta la aplicación completa y se realizan llamadas reales a los
   endpoints.

> Es importante entender que los casos de uso son solo del código que vamos a
> implementar. No se deben incluir casos de uso que no estén relacionados con la
> funcionalidad que se está desarrollando. Esto es lo que se denomina "test
> driven development".
>
> Tampoco se debe probar funcionalidad del framework Spring o realizar pruebas
> que no estén relacionadas con el código que se está implementando. El objetivo
> es centrarse en la lógica de negocio y asegurarse de que cada caso de uso esté
> correctamente probado antes de implementar la funcionalidad real.

## Anotaciones y Herramientas de Spring Boot

- **Pruebas Aisladas**: El servicio de dominio se prueba sin dependencias de
  infraestructura.
- **Pruebas de Integración Ligera**: El controlador se prueba con Spring, pero
  el servicio está simulado.
- **Flujo Completo**: Se prueba desde la entrada (controlador) hasta la salida
  (base de datos).
- **Isolación**: Cada test debería poder correr de forma aislada. Usar ddl-auto:
  `create-drop` ayuda a lograrlo.
- **TestContainers**: Es una herramienta poderosa que permite lanzar
  contenedores de Docker reales (como PostgreSQL, MySQL, Redis) para pruebas. Es
  ideal para pruebas E2E que requieren una configuración de infraestructura lo
  más real posible. En lugar de @AutoConfigureTestDatabase con H2, usarías
  TestContainers para iniciar una instancia real de tu base de datos elegida.
- `@WebMvcTest`: Carga solo el contexto necesario para probar el controlador,
  acelerando las pruebas.
- `@MockkBean`: Reemplaza beans en el contexto de Spring con mocks.
- `every {}` y `verify{}`: Para definir comportamientos simulados y verificar
  interacciones.
- `withArg {}`: Para verificar argumentos específicos pasados a métodos
  simulados.
- `jsonPath()`: Para verificar el contenido del cuerpo de la respuesta HTTP en
  pruebas de controladores.
- `@SpringBootTest`: Levanta el contexto completo de la aplicación.
- `@AutoConfigureTestDatabase`: Reemplaza la base de datos real por H2.
- `TestRestTemplate`: Cliente HTTP para probar los endpoints REST.
- `webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT`: Asigna un puerto
  aleatorio para evitar conflictos en entornos concurrentes.
- `@ActiveProfiles("tests")`: Activa un perfil específico con configuraciones
  para pruebas.
- `@DataJpaTest`: Para pruebas focalizadas en la capa de datos.
- `@TestPropertySource`: Para cargar configuraciones específicas de prueba.

## Mejora de Cobertura

Para mejorar la cobertura, asegúrate de que tus pruebas E2E con TestContainers
cubran:

- **Todos los endpoints**: GET, POST, PUT, DELETE.
- **Casos de éxito y error**: Operaciones válidas e inválidas (IDs inexistentes,
  datos incorrectos si se validan).
- **Lógica de negocio**: Filtrado, paginación, relaciones (si aplican).
- **Transacciones**: Si tu API maneja transacciones complejas, las pruebas E2E
  con una base de datos real las validan mejor.
- **Configuración real**: Validan que la configuración de la base de datos, JPA,
  etc., funcione correctamente en un entorno más realista que H2 en memoria.
- **Enfoque en Resultados de Negocio**: Las pruebas describen qué se espera que
  haga la aplicación, no cómo lo hace internamente.
- **Escenarios Completos**: Cada test E2E cubre un flujo de inicio a fin.
- **Validación de Resultados Reales**: Se verifica el estado real en la base de
  datos a través de la API.
- **Mayor Realismo**: Al usar PostgreSQL, se prueban características específicas
  del motor de base de datos, diferencias de dialecto SQL, etc., que H2 podría
  no reflejar exactamente.
- **Cobertura Mejorada**: Al probar con la infraestructura real, se aumenta la
  probabilidad de descubrir problemas que no se verían con H2 o mocks.

Además de las pruebas ya vistas (unitarias, integración, E2E), existen otros
tipos de pruebas y prácticas clave que debes considerar para un desarrollo
robusto, controlado y escalable. A continuación, explico las recomendaciones y
las mejores prácticas de TDD.

**Otros Tipos de Pruebas Recomendadas**

> La mayoría de las aplicaciones se benefician de una combinación de pruebas
> unitarias, de integración y E2E. Sin embargo, dependiendo del contexto, hay
> otros tipos de pruebas que pueden ser muy valiosas, sin embargo, no son
> necesarias para un desarrollo eficiente y escalable. Son herramientas externas
> complementarias que te ayudarán a mejorar la calidad y robustez de tu
> aplicación.
>
> No son pruebas que debas implementar en todos los proyectos, pero conocerlas y
> saber cuándo aplicarlas es crucial para un desarrollo profesional. De hecho,
> en este proyecto, no se harán estas pruebas adicionales, pero es importante
> conocerlas para futuros proyectos.

1.  **Pruebas de Contrato (Contract Tests):**
    - **Qué son:** Validan que dos servicios (por ejemplo, un microservicio
      cliente y un microservicio proveedor) se comuniquen correctamente según un
      contrato acordado (como un archivo OpenAPI/Swagger o un contrato Pact).
    - **Por qué usarlas:** Cruciales en arquitecturas de microservicios para
      garantizar que los cambios en un servicio no rompan a otro.
    - **Herramientas:** Spring Cloud Contract, Pact, OpenAPI Generator.

2.  **Pruebas de Seguridad:**
    - **Qué son:** Buscan vulnerabilidades como inyección de SQL, XSS, CSRF,
      problemas de autenticación/autorización.
    - **Por qué usarlas:** Esencial para proteger la aplicación.
    - **Herramientas:** OWASP ZAP, SonarQube (con plugins), Checkmarx, pruebas
      manuales de penetración.
    - **Aplicación:** Puedes usar `@WithMockUser` o `@WithSecurityContext` en
      pruebas de Spring Security para probar autorizaciones.

3.  **Pruebas de Rendimiento/Carga:**
    - **Qué son:** Evalúan cómo se comporta la aplicación bajo diferentes
      niveles de carga (usuarios concurrentes, peticiones por segundo).
    - **Por qué usarlas:** Para asegurar que la aplicación cumple con requisitos
      de rendimiento y no colapsa bajo presión.
    - **Herramientas:** JMeter, Gatling, k6.

4.  **Pruebas de Mutación:**
    - **Qué son:** Introducen errores artificiales (mutaciones) en el código y
      verifican si las pruebas existentes los detectan.
    - **Por qué usarlas:** Miden la calidad y efectividad de tu suite de
      pruebas. Una buena cobertura de línea no garantiza buena cobertura de
      lógica.
    - **Herramientas:** Pitest (para Java/Kotlin).

5.  **Pruebas de Documentación (Documentation Tests):**
    - **Qué son:** Aseguran que la documentación (como la API Swagger/OpenAPI)
      esté actualizada con el código.
    - **Por qué usarlas:** Mantiene la documentación precisa y útil para otros
      desarrolladores.
    - **Aplicación:** Spring REST Docs permite generar documentación a partir de
      pruebas reales.

## Mejores Prácticas de TDD y Diseño de Pruebas

TDD es una práctica disciplinada donde escribes la _prueba_ _antes_ del código
que la hace pasar. El ciclo clásico es **Rojo (Red) -> Verde (Green) ->
Refactor**. Lleva tiempo, muchas veces el código de las pruebas termina siendo
más código que en la implementación real, pero la clave está en no complicarlas,
realizar y ajustar las pruebas necesarias para cubrir la mayoría de los casos de
uso y no más. Aquí están las mejores prácticas para una experiencia TDD
efectiva:

1.  **Es necesario escribir las pruebas para que Fallen (Rojo):**
    - Es muy importante definir claramente _qué_ debe hacer la funcionalidad
      antes de _cómo_ hacerlo.
    - Crear los diferentes escenarios de prueba es una forma de definir
      claramente los requisitos.
    - La prueba debe fallar porque la funcionalidad aún no existe. Esto confirma
      que la prueba es válida.
    - Si la prueba funciona, entonces la funcionalidad ya existe y la prueba no
      es necesaria.
2.  **Ahora solo es necesario escribir el Código Mínimo pasar las pruebas
    (Verde):**
    - Debes crear las clases necesarias para que compile y ejecute la prueba.
      Esto es importante porque si no compila, no hay ninguna prueba que pueda
      fallar. (Lo veo como crear el esqueleto del código).
    - Haz que la prueba pase lo más rápido posible. El código puede no ser
      elegante aún.
    - El objetivo es pasar la prueba, no escribir la solución perfecta de
      inmediato.
    - Si el código es imperativo, funcional o complicado, está bien. El
      siguiente paso es refactorizarlo.
    - Si la prueba falla, es necesario corregirla.
3.  **El arte de Refactorizar (Refactor):**
    - Mejora la claridad, estructura y eficiencia del código _mientras todas las
      pruebas continúan pasando_.
    - Elimina duplicaciones, mejora nombres, simplifica lógica.
    - Aquí es donde el código se vuelve limpio y mantenible. Entonces el
      objetivo es ir limpiando y mejorando, mientras las pruebas siguen pasando.
    - Si las pruebas fallan, es necesario corregir el código implementado, no la
      prueba. Lo importante es que las pruebas sigan pasando porque son la guía
      de que el código funciona correctamente.
4.  **Itera:** Vuelve al paso 1 para la siguiente funcionalidad o requisito.
    Esto es lo que hace TDD eficaz.
5.  **Enfócate en Pequeños Pasos:** TDD funciona mejor cuando divides los
    problemas en unidades pequeñas y manejables. Cuando agregas más casos de uso
    o funcionalidades, hazlo en pequeños incrementos. No piense que mientras más
    pruebas mejor, sino que las pruebas deben cubrir la mayoría de los casos de
    uso sin ser excesivas. El objetivo no es la cobertura de pruebas, sino
    elegir los casos de forma adecuada para cubrir los requisitos.
6.  **Pruebas Claras y Expresivas:** Escribe pruebas que lean como
    especificaciones. `@DisplayName` y `@Nested` ayudan. También en las pruebas
    puedes usar comentarios para explicar el propósito si es necesario. Lo
    importante es que las pruebas sean claras y expresivas, que tengan un
    contexto y que cualquiera pueda entender qué se está probando y por qué.
7.  **Mantén la Prueba Simple:** Evita lógica compleja dentro de la prueba
    misma. De momento no es necesario que la prueba sea exhaustiva, pero siempre
    es bueno tener una prueba que verifique el comportamiento esperado. Si
    puedes, extrae lógica compleja a métodos de ayuda. Pero si eso complica la
    prueba, entonces no es una buena prueba.
8.  **No Pruebes Implementación, Prueba Comportamiento:** Las pruebas deben
    verificar _qué_ hace el código, no _cómo_ lo hace internamente. Esto
    facilita el refactor. Solo se debe probar un objetivo por prueba, pueden
    existir varias verificaciones o expresiones asertivas en una sola prueba,
    pero todas deben estar relacionadas con el mismo objetivo.

## Tipos de pruebas en este proyecto

Cuando agrego funcionalidades nuevas, normalmente sigo esta estrategia que voy
adaptando, en este contexto, pensando en la arquitectura screaming y hexagonal:

1.  **Pruebas Unitarias (La Mayoría):**
    - Prueba la lógica de negocio pura en el dominio.
    - Prueba servicios y reglas de negocio de forma aislada (con mocks).
    - Son rápidas, fáciles de escribir y mantener. Son la base de tu confianza.
    - **Aplicación:** Prueba cada método de tus servicios de dominio,
      validaciones, transformaciones de datos, etc.
    - **Tip:** No tienes que probar los constructores, getters, setters; ya que
      son internos y no aportan valor a la lógica de negocio.

2.  **Pruebas de Integración (Moderada Cantidad):**
    - Prueba la interacción entre componentes de tu aplicación (por ejemplo, un
      servicio de dominio con un adaptador de salida).
    - Prueba la correcta configuración de Spring (`@Configuration`, `@Bean`).
    - Pueden usar bases de datos en memoria (H2) para probar el mapeo y la
      persistencia sin la sobrecarga de contenedores.
    - Prueba el controlador REST con `@WebMvcTest` para validar rutas,
      serialización y respuestas HTTP.
    - **Aplicación:** Prueba el adaptador de salida (`JpaHotelRepository`) con
      H2 para verificar mapeos y consultas JPA.

3.  **Pruebas E2E/Contrato (Menor Cantidad, pero Críticas):**
    - Prueba flujos de usuarios completos o la interacción con servicios
      externos.
    - Validan que todo el sistema funcione como un todo.
    - Usan entornos más realistas (TestContainers) o mocks de servicios
      externos.
    - Lo más importante es que estas pruebas validen los casos de uso críticos y
      los contratos con otros servicios.
    - El objetivo es poner el enfoque solo en los casos de uso más importantes.
    - **Aplicación:** Prueba los escenarios CRUD completos de tu API REST con
      TestContainers o pruebas de contrato si interactúas con otros servicios.

### Cómo agregar funcionalidades de forma controlada

1.  **Definir Requisitos Claros:** Antes de codificar, entiende _qué_ se espera
    que haga la nueva funcionalidad. Documentar claramente los requisitos es una
    parte importante de la arquitectura. Los casos de uso por medio de historias
    de usuario o especificaciones funcionales son una buena forma de comenzar.
    Aquí puedes usar los tickets para documentar los casos de uso, si los
    tickets son adecuados, no es necesario documentar más, si hay ambiguedad,
    hay que documentarlo.
2.  **Diseñar (Diseño de Arquitectura):** Considera cómo encaja en la
    arquitectura actual (hexagonal, capas). Define de antemano los componentes
    principales y los roles de cada uno. De esa forma tendrás claro qué pruebas
    necesitas escribir y como debe ser el esqueleto del código, así como las
    dependencias entre componentes.
3.  **Escribir Pruebas (TDD):** Sigue el ciclo Rojo-Verde-Refactor. Esto es
    importante, porque ese ciclo de iteraciones es la base de la confianza. Aquí
    es donde defines los casos de uso y válidas que el código cumple con los
    requisitos, por lo tanto, es importante que las pruebas sean claras y
    expresivas. Además, que el cambio de mentalidad y pragmatismo se tiene que
    prácticar para ir mejorando y tener más soltura en el proceso de desarrollo
    con TDD.
4.  **Implementar Lógica de Dominio Primero:** Asegúrate de que la lógica
    central (en la capa de dominio) esté probada y funcione correctamente. Solo
    hay que poner atención al código necesario para pasar las pruebas y aplicar
    la estrategia de TDD. Las pruebas son la guía para definir que se tiene que
    ir implementando.
5.  **Implementar Adaptadores:** Conecta la lógica de dominio con la
    infraestructura (bases de datos, API's externas). Una vez que tienes el
    código de dominio funcionando, puedes implementar los adaptadores necesarios
    para que la aplicación funcione en un entorno real, digamos que primero
    validamos los objetos DTO y luego los objetos de dominio.
6.  **Pruebas de Integración y E2E:** Válida que los componentes trabajen juntos
    y que la API se comporte como se espera. Aquí es donde se valida que la
    integración entre componentes funciona correctamente y que la API cumple con
    los casos de uso definidos. Por eso es importante ir probando las
    interacciones entre componentes.
7.  **Revisión de Código:** Otro desarrollador revisa la implementación y las
    pruebas. Si estás en un equipo esto debería ser facil de lograr, si los
    deadlines y el tiempo son limitados, puedes hacerlo con un pair programming.
    Si estás solo, tómate un tiempo para revisar tu propio código con una mente
    fresca.
8.  **Cobertura de Código:** Asegúrate de que las nuevas líneas de código estén
    cubiertas por pruebas. No hay que obsesionarse con la cobertura, pero sí es
    importante que las nuevas funcionalidades estén probadas adecuadamente. El
    código sin pruebas es código que no se puede confiar. Puedes confiar en tus
    pruebas, pero conforme pasa el tiempo, también hay que mantener la cobertura
    de pruebas alta, principalmente en código nuevo para asegurarte de que no se
    rompa nada y se integre bien con el código existente.
9.  **Documentación:** Actualiza la documentación de la API (OpenAPI) y el
    código fuente si es necesario. Pero, aquí no es necesario documentar cada
    línea de código, sino que la documentación debe ser clara y concisa,
    enfocada en los casos de uso y en cómo interactuar con la API. También tener
    documentación en archivos markdown es una buena forma de documentar los
    casos de uso y los requisitos, estos pueden estar versionados en el código
    fuente, para que sea más fácil acceder a ellos.
10. **CI/CD:** Configura tu pipeline para que ejecute _todas_ las pruebas
    (unitarias, integración, E2E) en cada commit o pull request. Esto es crucial
    para detectar errores temprano. Si, haces commit el linter y las pruebas
    deben pasar antes de hacer merge a la rama principal. Esto asegura que el
    código que llega a producción es confiable y ha sido validado adecuadamente.

> Una estrategia de pruebas efectiva combina diferentes tipos de pruebas, desde
> mi punto de vista debe haber más énfasis fuerte en pruebas unitarias y una
> base sólida de TDD para guiar el desarrollo. Las pruebas de integración y E2E
> (con TestContainers) validan la cohesión del sistema. Seguir las prácticas de
> TDD te ayuda a escribir código más limpio, modular y probado desde el
> principio, facilitando la adición de nuevas funcionalidades de manera
> controlada.

## Como escribir pruebas eficientes

Ejemplo de estructura en un test unitario (Kotlin con Mockk y JUnit):

```kotlin
@DisplayName("Dado un hotel válido, cuando se guarda, entonces se debe persistir")
fun `given valid hotel, when saving, then hotel should be persisted`() {
    // Given
    val hotel = Hotel(name = "Test Hotel")
    every { repository.save(hotel) } returns hotel.copy(id = 1L)

    // When
    val result = service.save(hotel)

    // Then
    assertThat(result.id).isEqualTo(1L)
    verify { repository.save(hotel) }
}
```

### **1. Estructura y Nombrado de Pruebas**

- **Nombrado Descriptivo (`@DisplayName`):** No te conformes con
  `testCreateHotel`. Usa
  `@DisplayName("Dado un hotel válido, cuando se guarda, entonces se debe persistir")`.
  Esto convierte tus pruebas en documentación viva.
- **Estructura AAA o Given-When-Then:** Organiza el cuerpo de la prueba en:
  - **Given:** Configura el estado inicial y los objetos simulados.
  - **When:** Ejecuta la acción que estás probando.
  - **Then:** Verifica el resultado esperado.
  - Esto mejora la legibilidad y claridad del propósito de la prueba.
- **`@Nested` para Escenarios Relacionados:** Agrupa pruebas relacionadas con
  `@Nested` y `@DisplayName` para crear una estructura jerárquica que refleje el
  comportamiento del sistema (por ejemplo,
  `@Nested class WhenHotelExists { ... }` y
  `@Nested class WhenHotelDoesNotExist { ... }` dentro de `HotelServiceTest`).

### **2. Gestión de Datos de Prueba y `@Sql`**

- **`@Sql` para Datos de Prueba:** En pruebas de integración/E2E, usa
  `@Sql("/test-data.sql")` para cargar un conjunto de datos conocido antes de
  cada test o clase de test. Esto es más predecible que depender de la ejecución
  de otros tests para crear datos.
- **`@SqlGroup` y `@Sql.ExecutionPhase`:** Puedes usar
  `@Sql(scripts = "/clean.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)`
  para limpiar la base de datos después de cada test, garantizando aislamiento.
- **Fábricas de Datos de Prueba:** Crea métodos estáticos o clases de ayuda para
  generar objetos de dominio o DTO con valores predeterminados, evitando
  repetición de código y facilitando cambios globales. Ejemplo:
  `TestDataFactory.createHotel()`.

### **3. Uso Efectivo de Mocks**

- **Evita Mocks Excesivos:** Si tienes que mockear muchos métodos de un objeto,
  puede ser una señal de que la clase que estás probando tiene demasiadas
  responsabilidades (viola el SRP) o está mal acoplada. Reconsidera el diseño,
  este es un `code smell` que te puede ayudar a mejorar y solo con las pruebas
  se debe hacer evidente.
- **`@SpyBean` con Cautela:** `@SpyBean` puede ser útil para probar un
  componente real pero con un subcomponente mockeado. Úsalo solo cuando sea
  estrictamente necesario, ya que puede ocultar problemas de diseño y es
  complicado de depurar.
- **Verifica Interacciones (`verify`):** No solo verifiques el resultado, sino
  también _cómo_ se interactuó con los colaboradores. Por ejemplo,
  `verify(repository).save(expectedHotel)` confirma que el servicio llamó al
  repositorio con el objeto correcto.

### **4. Configuración y Aislamiento de Pruebas**

- **Perfiles de Prueba (`@ActiveProfiles("test")`):** Asegúrate de que tus
  pruebas usen un perfil de Spring específico (`application-tests.yaml`) con
  configuraciones aisladas (H2, sin logging verbose, etc.).
- **`@DirtiesContext` con Cautela:** Esta anotación fuerza la recarga del
  contexto de Spring después de un test. Úsala solo si un test modifica el
  contexto global de forma que afecta a otros. Debería ser la excepción, no la
  regla, sin embargo, es importante tenerlo en cuenta.
- **`@TestMethodOrder`:** Controla el orden de ejecución de los métodos de
  prueba si es absolutamente necesario (aunque generalmente se debe evitar
  dependencias entre tests). Útil para pruebas E2E donde el estado se mantiene.
  En mi caso prefiero probar flujos de usuarios en orden, por ejemplo, primero
  crea un hotel, luego lo actualiza y finalmente lo elimina, todo en un solo
  test.

### **5. Métricas y Cobertura de Pruebas**

- **No Solo Líneas de Cobertura:** La cobertura de líneas es solo una métrica.
  Presta atención a la **cobertura de rama** (`Branch Coverage`), que mide si
  todas las ramificaciones de tu código (`if`, `else`, `switch`) han sido
  ejecutadas. 100% de cobertura de rama es más valioso que 100% de líneas si hay
  ramificaciones complejas. Las ramificaciones en el codigo son una forma de
  documentar el comportamiento del sistema, ahí es donde se definen los casos de
  uso.
- **Pruebas de Mutación:** Considera usar herramientas como Pitest. Son una
  forma más profunda de evaluar la calidad de tus pruebas. Si una mutación
  (cambio artificial en el código) _no_ es detectada por tus pruebas, significa
  que tus pruebas no son efectivas para esa parte del código.
- **Configura Reportes de Cobertura:** Es importante integrar herramientas como
  JaCoCo en tu build (Gradle/Maven) para generar reportes HTML que muestren
  visualmente qué partes del código están cubiertas y cuáles no, esto te da un
  contexto más claro de la cobertura del código, además que puedes analizar si
  tus pruebas recorren las condiciones de error esperadas o definir nuevos casos
  de prueba.

### **6. Integración con CI/CD**

- **Ejecución Obligatoria:** Las pruebas deben _siempre_ ejecutarse en el
  pipeline de CI/CD. No se debe permitir el merge a `main` si fallan. Lo ideal
  sería proteger la rama principal con un pipeline de CI/CD que ejecute todas
  las pruebas y definir un flujo de trabajo para resolver los problemas que
  surjan.
- **Diferentes Tipos de Pruebas en Diferentes Etapas:** Ejecuta pruebas
  unitarias en la etapa inicial (rápida). Las pruebas de integración y E2E
  pueden ejecutarse en etapas posteriores o en entornos dedicados. Las pruebas
  unitarias deben ser de rapida ejecución, principalmente porque no deben
  depender de infraestructura externa. Las de integración y E2E pueden tardar
  más, pero son cruciales para validar la cohesión del sistema, sin embargo,
  deben ser ejecutadas en entornos dedicados para evitar afectar el rendimiento
  de las pruebas unitarias.
- **Reporte de Resultados:** El pipeline debe publicar los resultados de las
  pruebas y los reportes de cobertura para que sean visibles para el equipo.
  Esto ayuda a detectar problemas temprano y evita que los cambios se integren a
  la rama principal sin tener que revisarlos manualmente, además que fomenta el
  uso de pruebas automatizadas.

### **7. Refactorización Constante de Pruebas**

- **Las pruebas también se Refactorizan:** Ojo esto es muy importante, al igual
  que el código de producción, las pruebas también deben mantenerse limpias,
  legibles y mantenibles. Si refactorizas el código de producción, revisa si las
  pruebas también necesitan actualizarse para reflejar mejor el nuevo
  comportamiento o para mejorar su claridad.
- **Evita Lógica en Pruebas:** Mantén las pruebas simples. Evita bucles,
  condicionales complejos o cálculos dentro del cuerpo de la prueba. Si
  necesitas lógica, encapsúlala en métodos de ayuda.

### **8. Mentalidad de "Prueba como Especificación"**

- **Piensa en el Comportamiento:** Escribe la prueba como si estuvieras
  especificando _cómo debe comportarse_ el código, no _cómo está implementado_.
  La práctica deliberada te ayudará a mejorar la calidad de tus pruebas y a
  mantenerlas limpias.
- **Prueba Casos Límite y de Error:** No solo pruebes el "happy path". Asegúrate
  de probar entradas inválidas, estados inesperados, errores de dependencias.
  Las pruebas deben fallar si el código no maneja estos casos correctamente. Si
  solo pruebas el "happy path", puedes estar confundido por los resultados
  inesperados, además, que la mayoría de los errores en producción provienen de
  casos límite o entradas inesperadas.

### **9. Uso de Extensiones y Bibliotecas de Apoyo**

- **Mockito Kotlin:** Usa `mockito-kotlin` para una sintaxis más idiomática y
  concisa en Kotlin (`whenever`, `verify`, `argThat`).
- **Mockk en Kotlin:** Si usas Kotlin, `Mockk` es una alternativa poderosa a
  Mockito, con mejor soporte para características de Kotlin como corutinas y
  funciones de extensión. Evita mezclar `Mockito` y `Mockk` en el mismo
  proyecto, esto da problemas tanto en IDE como en el build.
- **AssertJ:** Proporciona aserciones fluidas y más legibles
  (`assertThat(result).isNotNull().hasSize(2).first().isEqualTo(expected)`).
- **WireMock:** Si tu API interactúa con servicios externos, WireMock permite
  simularlos en pruebas de integración/E2E sin depender de infraestructura
  externa real.

### **10. Documentación de Pruebas**

- **`@ParameterizedTest` con `@ValueSource` o `@CsvSource`:** Para probar un
  método con múltiples conjuntos de datos de entrada de forma concisa, esto es
  importante, para usar una misma prueba con varios conjuntos de datos.
- **`@RepeatedTest`:** Para repetir una prueba N veces, útil para buscar
  problemas intermitentes (aunque esto a menudo indica un problema de
  concurrencia o estado no limpio).

> Estas son las recomendaciones que sigo y de momento veo que me ayudan a
> construir una suite de pruebas más robusta, confiable y mantenible, lo cual es
> fundamental para la efectividad del TDD y la calidad del software. En este
> proyecto me tomo licencias de agregar más casos, porque lo uso como una
> práctica deliberada para mejorar la calidad de mis pruebas. En los proyectos
> empresariales, es muy importante tener una suite de pruebas robusta y
> confiable, afinar bien los casos de uso y definir los requisitos, para solo
> escribir las pruebas que necesites.
