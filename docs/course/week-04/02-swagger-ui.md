# Error con Swagger UI

Hay un error que está rompiendo el endpoint `/api-docs` de Swagger UI en una aplicación Spring Boot. El error es el siguiente:

```bash
Caused by: java.lang.NoSuchMethodError: 'void org.springframework.web.method.ControllerAdviceBean.<init>(java.lang.Object)'
    at org.springdoc.core.service.GenericResponseService.lambda$getGenericMapResponse$8(GenericResponseService.java:702)
```

> Esto se debe a una incompatibilidad entre las versiones de Spring Boot y Springdoc OpenAPI.

Por ejemplo en el archivo `build.gradle.kts` tenemos:

- Spring Boot 3.5.6 (Spring Framework 6.2.x)
- springdoc-openapi-starter-webmvc-ui:2.3.0

## Cómo solucionarlo

La solución es actualizar la dependencia de `springdoc-openapi-starter-webmvc-ui` a una versión compatible con Spring Boot 3.5.6. La versión correcta es la `2.8.14` que se encuentra actualmente en el repositorio de Maven Central, que menciona que tiene [compatibilidad con Spring Boot 3.5.7](https://github.com/springdoc/springdoc-openapi/releases/tag/v2.8.14).

Solo hay que actualizar el archivo `build.gradle.kts` con la siguiente versión:

```kotlin
dependencies {
    // ...
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.14")
    // ...
}
```

## ¿Por qué ocurre esto?

- Los controladores (Ping, Sentry, etc.) usan Spring MVC normal → funcionan bien con Boot 3.5.6.
- `/api-docs` lo maneja springdoc, que internamente usa clases de Spring MVC (ControllerAdviceBean, etc.) con una firma esperada distinta a la que realmente trae tu versión de Spring.
- De ahí el NoSuchMethodError justo cuando springdoc intenta construir la especificación OpenAPI.
- El problema es una incompatibilidad de versiones.
