# Temario

## 1. Introducción y Configuración Inicial

**Objetivo:** Entender qué es Spring Boot, por qué usar Kotlin, y configurar el entorno de desarrollo.

- ¿Qué es Spring Boot y por qué es popular?
- Ventajas de usar Kotlin con Spring Boot
- Requisitos previos: Conocimientos básicos de Kotlin y Java
- Instalación y configuración:
    - JDK 17+
    - IntelliJ IDEA o VS Code
    - Spring Initializr para generar el proyecto base
    - Estructura básica de un proyecto Spring Boot con Kotlin

---

## 2. Conceptos Básicos de Spring Boot

**Objetivo:** Familiarizarse con los componentes esenciales de Spring Boot.

- Inyección de dependencias con `@Autowired` y `@Component`
- Creación de un controlador REST básico con `@RestController`
- Manejo de rutas con `@GetMapping`, `@PostMapping`, etc.
- Configuración de propiedades con `application.properties`/`application.yml`
- Ejemplo práctico: API REST simple para gestionar "Hola Mundo"

---

## 3. Persistencia de Datos con Spring Data JPA

**Objetivo:** Aprender a conectar y manipular bases de datos relacionales.

- Configuración de H2, MySQL o PostgreSQL
- Definición de entidades con `@Entity` y repositorios con `JpaRepository`
- Operaciones CRUD básicas
- Relaciones entre entidades: `@OneToMany`, `@ManyToOne`, `@ManyToMany`
- Ejemplo práctico: API para gestionar "Usuarios" y "Tareas"

---

## 4. Validación y Manejo de Errores

**Objetivo:** Implementar validaciones y manejar errores de forma profesional.

- Validación de datos con `@Valid` y anotaciones de Jakarta Validation
- Manejo de excepciones con `@ControllerAdvice` y `@ExceptionHandler`
- Creación de respuestas de error personalizadas
- Ejemplo práctico: Validar y manejar errores en un formulario de registro

---

## 5. Seguridad con Spring Security

**Objetivo:** Proteger la API con autenticación y autorización.

- Configuración básica de Spring Security
- Autenticación con JWT (JSON Web Tokens)
- Autorización basada en roles
- Protección de endpoints con `@PreAuthorize`
- Ejemplo práctico: API segura con login y roles de usuario

---

## 6. Pruebas Unitarias e Integración

**Objetivo:** Garantizar la calidad del código con pruebas automatizadas.

- Pruebas unitarias con JUnit 5 y Mockito
- Pruebas de integración con `@SpringBootTest`
- Pruebas de controladores con `@WebMvcTest`
- Ejemplo práctico: Pruebas para un servicio de "Reservas"

---

## 7. Documentación de la API con Swagger/OpenAPI

**Objetivo:** Documentar la API de forma automática y profesional.

- Configuración de SpringDoc OpenAPI
- Anotaciones para documentar endpoints: `@Operation`, `@ApiResponse`
- Generación automática de la interfaz de Swagger UI
- Ejemplo práctico: Documentar una API de "Productos"

---

## 8. Despliegue y Monitoreo

**Objetivo:** Llevar la aplicación a producción y monitorear su rendimiento.

- Creación de un ejecutable JAR con `spring-boot-maven-plugin`
- Despliegue (_Aún está por definir_)
- Monitoreo con Actuator: `/health`, `/metrics`, `/info`
- Ejemplo práctico: Desplegar una API en Heroku y monitorear su estado

---

## 9. Temas Avanzados

**Objetivo:** Profundizar en funcionalidades avanzadas de Spring Boot.

- Eventos y listeners con `@EventListener`
- Programación asíncrona con `@Async`
- Integración con Kafka o RabbitMQ para mensajería
- Caching con `@Cacheable` y Redis
- Ejemplo práctico: Sistema de notificaciones asíncronas

---

## 10. Proyecto Final: API para Gestión Hotelera

**Objetivo:** Aplicar todo lo aprendido en un proyecto real.

- Requisitos: API para gestionar reservas, habitaciones y usuarios
- Arquitectura por capas: Controladores, Servicios, Repositorios
- Integración con base de datos y seguridad
- Documentación y despliegue
