# Seguridad con Spring Security y JWT, roles

---

## Historia de usuario

Como se ve una historia de usuario para este tipo de funcionalidad, incluyendo
los criterios de aceptación, las capas afectadas, y un enfoque TDD para su
implementación en un proyecto Spring Boot con Kotlin.

**Como** desarrollador del proyecto **Quiero** implementar seguridad básica con
**Spring Security** y autenticación mediante **JWT**, gestionando roles y
privilegios a través de autoridades **Para** proteger los endpoints del backend,
restringir el acceso según el rol del usuario y garantizar una autenticación
segura y sin estado (stateless).

> **Nota:** Este es el enfoque que sigo en la mayoría de los proyectos, a menos
> que se indique lo contrario, o que haya necesidades especiales como el uso de
> OAuth2 con Federated Authentication, aunque esto no es el objetivo principal
> en este momento.

---

### Criterios de aceptación

1. La autenticación debe realizarse mediante **JWT (JSON Web Token)** firmado
   con una clave secreta definida en el archivo de configuración
   (`application.yml`).
2. Los usuarios deben tener asociados uno o más **roles**, y cada rol debe
   contener un conjunto de **authorities** (privilegios granulares).
3. La aplicación debe exponer al menos los siguientes endpoints públicos y
   protegidos:
   - `/api/auth/login` → público, para autenticarse y obtener el token JWT.
   - `/api/auth/register` → público, para registrar un nuevo usuario.
   - `/api/users/**` → protegido, solo accesible con rol `ADMIN`.
   - `/api/profile` → protegido, accesible con rol `USER` o `ADMIN`.

4. Las respuestas ante intentos no autorizados deben devolver:
   - **401 Unauthorized** si el token es inválido o no existe.
   - **403 Forbidden** si el usuario autenticado no tiene permisos suficientes.

5. Las pruebas unitarias y de integración deben validar:
   - Generación y validación correcta del token JWT.
   - Acceso permitido/denegado a endpoints según el rol del usuario.
   - Respuestas correctas ante credenciales inválidas o tokens expirados.

---

### Capas afectadas

- **Domain:**
  - Creación de las entidades `User`, `Role` y `Authority` con sus relaciones
    (`@ManyToMany` o `@OneToMany`).

- **Repository:**
  - Interfaces para manejar usuarios y roles (`UserRepository`,
    `RoleRepository`).

- **Service:**
  - Lógica para autenticación, validación de credenciales y emisión de JWT.

- **Security / Infrastructure:**
  - Configuración de `SecurityFilterChain`, `JwtAuthenticationFilter`,
    `JwtUtils`, y `CustomUserDetailsService`.
  - Controladores públicos para `login` y `register`.

---

### Diseñar las pruebas

Con un enfoque TDD, el ciclo **Red → Green → Refactor** se aplicará así:

1. **Red:**
   - Escribir pruebas de integración con `MockMvc` para los endpoints `/login` y
     `/users/**`.
   - Simular peticiones sin token y con token inválido para verificar respuestas
     `401` y `403`.
   - Validar que usuarios con distintos roles obtengan acceso o rechazo según
     sus permisos.

2. **Green:**
   - Implementar las entidades y repositorios de usuario, rol y autoridad.
   - Crear el servicio de autenticación y generación de JWT.
   - Configurar el `SecurityFilterChain` y los filtros JWT.

3. **Refactor:**
   - Mejorar la organización del código (paquetes `security`, `auth`, `domain`).
   - Centralizar la carga de usuarios con `UserDetailsService`.
   - Ajustar pruebas para mantener consistencia y legibilidad.

---

### Requerimientos

- **Dependencias necesarias:**

  ```kotlin
  implementation("org.springframework.boot:spring-boot-starter-security")
  implementation("io.jsonwebtoken:jjwt-api:0.11.5")
  runtimeOnly("io.jsonwebtoken:jjwt-impl:0.11.5")
  runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.11.5")
  ```

- **Entidades base:**

  ```kotlin
  @Entity
  data class Authority(
      @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
      val id: Long = 0,
      val name: String
  )

  @Entity
  data class Role(
      @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
      val id: Long = 0,
      val name: String,

      @ManyToMany(fetch = FetchType.EAGER)
      val authorities: Set<Authority> = emptySet()
  )

  @Entity
  data class User(
      @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
      val id: Long = 0,
      val username: String,
      val password: String,

      @ManyToMany(fetch = FetchType.EAGER)
      val roles: Set<Role> = emptySet()
  )
  ```

- **Configuración de seguridad (`SecurityConfig.kt`):**

  ```kotlin
  @Configuration
  @EnableWebSecurity
  class SecurityConfig(
      private val jwtFilter: JwtAuthenticationFilter
  ) {

      @Bean
      // CSRF deshabilitado para JWT, seguridad sin estado
      fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
          return http
              .csrf { it.disable() }
              .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
              .authorizeHttpRequests {
                  it.requestMatchers("/api/auth/**").permitAll()
                      .requestMatchers("/api/users/**").hasRole("ADMIN")
                      .anyRequest().authenticated()
              }
              .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter::class.java)
              .build()
      }
  }
  ```

- **Buenas prácticas recomendadas:**
  - Implementar `UserDetails` y `UserDetailsService` personalizados para mapear
    entidades del dominio a usuarios de Spring Security.
  - Mantener el campo de roles con el prefijo `ROLE_` (`ROLE_ADMIN`,
    `ROLE_USER`).
  - Evitar guardar contraseñas sin cifrar (usar `BCryptPasswordEncoder`).
  - Los tokens deben tener expiración y se recomienda incluir `username` y
    `authorities` en los claims.

- **Pruebas:**
  - Usa `@WebMvcTest` con `MockMvc` para validar protección de endpoints.
  - Simula tokens válidos/expirados usando `JwtUtils`.
  - Verifica que cada rol acceda solo a los endpoints que le correspondan.
