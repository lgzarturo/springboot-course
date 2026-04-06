# 🏨 ROADMAP HOTEL POKÉMON: GUÍA DEL ENTRENADOR SPRING BOOT

## 📋 CONTEXTO DEL PRODUCTO FINAL

**Hotel Pokémon** es un sistema de gestión hotelera temática donde los entrenadores (usuarios) pueden:

- Reservar habitaciones temáticas por tipo Pokémon (Fuego, Agua, Planta, Eléctrico)
- Registrar a sus Pokémon compañeros durante la estadía
- Contratar servicios especiales (Centro Pokémon, Restaurante, Tienda de objetos)
- Realizar pagos y dejar reseñas
- Los roles son: TRAINER (huesped), GYM_LEADER (staff), PROFESOR_OAK (admin)

**Arquitectura Base:** Hexagonal (Ports & Adapters) con Domain-Driven Design ligero.

---

## 🗿 FASE 1: GIMNASIO PEWTER - "LOS CIMIENTOS DE LA PERSISTENCIA"

**Líder:** Brock (Especialista en estructuras sólidas)
**Insignia:** Boulder Badge 💎
**Duración Total:** 24 horas distribuidas en 4 ejercicios

### Ejercicio 1.1: El Registro de Huéspedes (User Entity)

**Historia:**
El Profesor Oak necesita registrar a los entrenadores que llegan al hotel. Actualmente tiene un cuaderno desordenado. Debes digitalizar el registro de usuarios incluyendo sus credenciales y roles.

**Objetivo Técnico:**
Mapear la entidad `User` con JPA, implementar herencia de roles mediante Enum, y establecer relaciones One-to-Many con Reservation.

**Desarrollo Esperado:**

1. Crear la entidad `User` en `domain.model` con:
   - Campos básicos: id, username, email, password, firstName, lastName, phoneNumber
   - Enum `UserRole` con valores: TRAINER, GYM_LEADER, PROFESOR_OAK
   - Timestamps automáticos con `@CreationTimestamp` y `@UpdateTimestamp`
   - Relación One-to-Many con Reservation (un usuario tiene muchas reservas)
2. Implementar validaciones de columna: email único, username único, constraints de nullable
3. Crear el primer migration de Flyway: `V1__create_users_table.sql`
4. Configurar el `application.yml` para conexión a PostgreSQL con pool HikariCP

**Criterios de Aceptación:**

- [ ] La tabla `users` se crea correctamente con todas las constraints
- [ ] No permite insertar usuarios con email duplicado (test manual con psql)
- [ ] El campo `created_at` se pobla automáticamente al insertar
- [ ] Se puede hacer `findAll()` y traer usuarios sin lazy loading errors
- [ ] Script de Flyway es reproducible en base de datos limpia

**Tiempo Estimado:** 4 horas
**Insignia Ganada:** *JPA Básico y Migraciones*

---

### Ejercicio 1.2: El Catálogo de Habitaciones (Room Entity + Tipos)

**Historia:**
Brock te muestra el catálogo de habitaciones temáticas. Cada habitación tiene un tema Pokémon (Pikachu Suite, Charizard Chamber, etc.), capacidad distinta y precio variable según la temporada. Debes modelar esto considerando que una habitación puede tener muchas reservas en el tiempo.

**Objetivo Técnico:**
Mapear `Room` con campos complejos (BigDecimal para precios, Enums para tipos), implementar soft delete mediante `RoomStatus`, y crear relación One-to-Many bidireccional con Reservation.

**Desarrollo Esperado:**

1. Crear entidad `Room` con:
   - `roomNumber` como String único (ej: "P-101" para planta 1)
   - Enum `RoomType`: STANDARD, DELUXE, SUITE, GYM_LEADER_SUITE, ELITE_FOUR_SUITE
   - Enum `PokemonTheme`: PIKACHU, CHARIZARD, BLASTOISE, MEWTWO, etc.
   - `pricePerNight` como BigDecimal con precisión 10,2
   - `status` como Enum: AVAILABLE, OCCUPIED, MAINTENANCE, CLEANING
   - Lista de amenities como String (o mejor: crear tabla separada `RoomAmenity` con relación Many-to-Many)
2. Implementar migration `V2__create_rooms_and_relations.sql` incluyendo foreign keys
3. Crear `RoomRepository` con método `findAvailableRoomsByTypeAndCapacity()`
4. Implementar `RoomPersistenceAdapter` siguiendo patrón Port-Adapter

**Criterios de Aceptación:**

- [ ] Se pueden insertar habitaciones con precios decimales exactos (test: 199.99)
- [ ] Consulta `findAvailableRoomsByTypeAndCapacity` filtra correctamente por capacidad >= solicitada
- [ ] No se permite eliminar físicamente habitaciones (solo cambiar status a MAINTENANCE)
- [ ] El SQL generado por JPA incluye índices en room_number y status para performance
- [ ] Se implementa `@Version` para optimistic locking en concurrencia de reservas

**Tiempo Estimado:** 6 horas
**Insignia Ganada:** *Modelado de Dominio Complejo*

---

### Ejercicio 1.3: Sistema de Reservas (Reservation Entity + Lógica de Negocio)

**Historia:**
Misty llega al hotel y quiere reservar la "Charizard Chamber" por 3 noches. El sistema debe calcular el precio total, validar disponibilidad y permitir registrar a su Staryu como Pokémon compañero. Una reserva puede incluir múltiples servicios adicionales.

**Objetivo Técnico:**
Crear entidad `Reservation` con relaciones Many-to-One (User y Room), manejar fechas con LocalDate, implementar cálculo de precios, y crear entidad intermedia `ReservationService` para servicios contratados.

**Desarrollo Esperado:**

1. Entidad `Reservation` con:
   - Fechas: checkInDate, checkOutDate (tipo LocalDate)
   - numberOfGuests con validación @Min(1) @Max(10)
   - totalPrice calculado (no persistido si es derivado, o persistido con @Formula)
   - Enum ReservationStatus: PENDING, CONFIRMED, CHECKED_IN, CHECKED_OUT, CANCELLED
   - Relación Many-to-One con User (usuario que reserva)
   - Relación Many-to-One con Room (habitación asignada)
   - cancelledAt nullable para auditoría
2. Entidad `ReservationService` (tabla intermedia):
   - Cantidad, precio unitario, precio total
   - Relación Many-to-One con Service (catálogo de servicios)
   - scheduledAt (cuándo se agendó el servicio)
3. Implementar `ReservationRepository` con query nativa para detectar superposición de fechas:

   ```sql
   SELECT * FROM reservations
   WHERE room_id = ?
   AND status NOT IN ('CANCELLED', 'CHECKED_OUT')
   AND (check_in_date, check_out_date) OVERLAPS (?, ?)
   ```

4. Crear `AvailabilityChecker` en dominio (puerto) que valide solapamiento antes de crear reserva

**Criterios de Aceptación:**

- [ ] No permite crear reserva si las fechas se solapan con otra reserva activa (test con datos: Reserva A: 10-15 enero, Reserva B intenta 12-18 enero -> debe fallar)
- [ ] El cálculo de noches es correcto: checkout 15 - checkin 10 = 5 noches
- [ ] Al cancelar reserva, se actualiza cancelledAt pero no se borra el registro
- [ ] Query de disponibilidad usa índices y responde en <100ms con 10k registros
- [ ] Implementa @Transactional para evitar race conditions en creación simultánea

**Tiempo Estimado:** 8 horas
**Insignia Ganada:** *Transacciones y Consistencia de Datos*

---

### Ejercicio 1.4: El Centro Pokémon y Servicios (Service Catalog + Pokemon Companion)

**Historia:**
El hotel ofrece servicios especiales: Centro Pokémon (cura), Tienda de Objetos, Restaurante 5 Estrellas, y Entrenamiento especial. Los huéspedes también pueden registrar hasta 6 Pokémon que traen consigo. Brock quiere que implementes el catálogo de servicios y el registro de Pokémon.

**Objetivo Técnico:**
Completar el modelo: entidad `Service` (catálogo), entidad `Pokemon` (compañeros), y relación Many-to-Many entre Reservation y Service con atributos adicionales (cantidad, fecha agendada).

**Desarrollo Esperado:**

1. Entidad `Service` (catálogo hotelero):
   - name, description, price, available (boolean)
   - Enum ServiceType: POKECENTER, RESTAURANT, SHOP, TRAINING
   - Campos específicos por tipo (ej: durationMinutes para servicios)
2. Entidad `Pokemon`:
   - name (nombre del compañero), species (ej: "Staryu")
   - Enum PokemonType: WATER, FIRE, GRASS, ELECTRIC, etc.
   - level (Int), specialNeeds (String)
   - Relación Many-to-One con User (dueño)
3. Implementar `V3__services_and_pokemon.sql` con datos semilla (seed data):
   - Insertar 5 servicios: "Curación Pokémon" ($25), "Buffet Legendario" ($50), etc.
   - Insertar 10 tipos de habitaciones temáticas
4. Crear `PokemonRepository` con búsqueda por userId y species
5. Implementar adaptador `ServiceCatalogAdapter` que consulte servicios disponibles filtrando por tipo de Pokémon del huésped (ej: Pokémon Fuego no pueden usar habitaciones Agua sin upgrade)

**Criterios de Aceptación:**

- [ ] Los datos semilla se cargan automáticamente al iniciar la app (usando CommandLineRunner o Flyway callbacks)
- [ ] Se puede consultar: "Qué servicios contrató la reserva #123?" incluyendo cantidades y precios totales por línea
- [ ] Un usuario puede tener máximo 6 Pokémon registrados (validación a nivel de aplicación)
- [ ] El precio total de la reserva incluye: (noches × precio habitación) + suma(servicios contratados)
- [ ] Implementar soft delete en servicios (disponible=false en lugar de DELETE)

**Tiempo Estimado:** 6 horas
**Insignia Ganada:** *Relaciones Complejas y Seed Data*

---

## ⚡ FASE 2: GIMNASIO VERMILION - "EL ESCUDO ELÉCTRICO"

**Líder:** Lt. Surge (Especialista en defensa)
**Insignia:** Thunder Badge ⚡
**Duración Total:** 20 horas distribuidas en 4 ejercicios

### Ejercicio 2.1: El Sistema de Pase de Entrenador (JWT Authentication)

**Historia:**
Team Rocket ha intentado entrar al sistema robando contraseñas. Lt. Surge exige implementar un sistema de Pases Digitales (JWT) que expiren después de 24 horas, con la posibilidad de renovarlos sin pedir contraseña de nuevo (refresh token).

**Objetivo Técnico:**
Implementar autenticación stateless con JWT, incluyendo access tokens (corto plazo) y refresh tokens (largo plazo almacenados en base de datos).

**Desarrollo Esperado:**

1. Configurar `SecurityFilterChain` con Kotlin DSL:
   - Deshabilitar CSRF (API stateless)
   - Permitir /api/auth/** público
   - Proteger todo lo demás con JWT
2. Implementar `JwtTokenProvider`:
   - Generar token con claims: userId, email, role, issuedAt, exp (24h)
   - Generar refreshToken (UUID random) almacenado en tabla `refresh_tokens` con fecha de expiración (7 días)
   - Validar firma usando secret key de 512 bits (HS512)
3. Crear `AuthenticationController` con endpoints:
   - POST /api/auth/login (devuelve ambos tokens)
   - POST /api/auth/refresh (valida refresh token, emite nuevos tokens)
   - POST /api/auth/logout (invalida refresh token en BD)
4. Implementar `UserDetailsService` que cargue usuario por email con roles

**Criterios de Aceptación:**

- [ ] Login con credenciales válidas devuelve accessToken (JWT) y refreshToken (UUID)
- [ ] Acceder a /api/rooms con token válido devuelve 200, sin token devuelve 401
- [ ] Token expirado (simular cambiando fecha del sistema) devuelve 403 con mensaje "Token expired"
- [ ] Refresh token usado más de una vez es invalidado inmediatamente (rotación de tokens)
- [ ] Logout elimina el refresh token de la base de datos (no se puede reutilizar)
- [ ] El payload JWT contiene información mínima (no passwords, no datos sensibles)

**Tiempo Estimado:** 5 horas
**Insignia Ganada:** *Autenticación Stateless*

---

### Ejercicio 2.2: Guardianes de las Habitaciones (Role-Based Access Control)

**Historia:**
No todos pueden acceder a todas las áreas. Los TRAINER solo ven sus propias reservas. Los GYM_LEADER pueden ver todas las reservas y modificar estado de limpieza de habitaciones. El PROFESOR_OAK tiene acceso total incluyendo estadísticas financieras.

**Objetivo Técnico:**
Implementar autorización basada en roles usando `@PreAuthorize`, métodos de seguridad personalizados, y filtrado de datos a nivel de repositorio (Multi-Tenancy ligero).

**Desarrollo Esperado:**

1. Anotaciones de seguridad en Controllers:
   - `@PreAuthorize("hasRole('PROFESSOR_OAK')")` para endpoints administrativos
   - `@PreAuthorize("hasAnyRole('GYM_LEADER', 'PROFESSOR_OAK')")` para gestión de habitaciones
2. Implementar `ReservationSecurityService` con método `isOwner(reservationId, userId)`:
   - Consulta si la reserva pertenece al usuario actual
   - Usar en: `@PreAuthorize("@reservationSecurity.isOwner(#id, principal.id)")`
3. Filtros de seguridad en queries:
   - TRAINER: `findByUserId()` automático en repositorio
   - GYM_LEADER: acceso a todas las reservas pero no a datos financieros sensibles
   - Implementar usando `SpelAwareProxyProjectionFactory` o QueryDSL con filtros dinámicos
4. Crear endpoint GET /api/admin/stats (solo OAK) que devuelva ingresos totales
5. Crear endpoint PUT /api/rooms/{id}/status (LEADER y OAK) para cambiar estado de limpieza

**Criterios de Aceptación:**

- [ ] Usuario TRAINER intenta ver reserva de otro usuario -> 403 Forbidden
- [ ] Usuario TRAINER ve /api/admin/stats -> 403
- [ ] GYM_LEADER puede cambiar status de habitación a CLEANING pero no ver datos de tarjetas de crédito
- [ ] PROFESOR_OAK puede hacer todo incluyendo cancelar reservas de otros
- [ ] Tests de integración con @WithMockUser verificando cada combinación de rol/endpoint

**Tiempo Estimado:** 5 horas
**Insignia Ganada:** *Autorización Granular*

---

### Ejercicio 2.3: Protección Contra Intrusos (Seguridad Avanzada)

**Historia:**
Lt. Surge detectó intentos de fuerza bruta en el login. Además, quiere asegurar que las contraseñas tengan complejidad alta y que las sesiones se invaliden si detectamos comportamiento sospechoso (cambio de IP repentina).

**Objetivo Técnico:**
Implementar rate limiting en autenticación, validación de complejidad de passwords, detección de sesiones sospechosas, y headers de seguridad HTTP.

**Desarrollo Esperado:**

1. Rate Limiting con Bucket4j:
   - Máximo 5 intentos de login por minuto por IP
   - Máximo 3 intentos fallidos por usuario antes de bloqueo temporal de 15 minutos
   - Implementar usando filtros de Spring o Resilience4j
2. Validación de contraseñas:
   - Mínimo 12 caracteres, 1 mayúscula, 1 minúscula, 1 número, 1 especial
   - Verificar que no esté en lista de passwords comunes (usar librería zxcvbn4j)
   - Hash usando BCrypt con fuerza 12 (no 10 por defecto)
3. Auditoría de seguridad:
   - Tabla `security_events` que registre: login exitoso, login fallido, cambio de password, logout
   - Incluir IP address y User-Agent
4. Headers de seguridad:
   - Implementar CSP (Content Security Policy)
   - HSTS, X-Frame-Options, X-Content-Type-Options
   - Usar `ServerHttpSecurity` headers custom

**Criterios de Aceptación:**

- [ ] Después de 5 intentos fallidos de login, el endpoint responde 429 Too Many Requests durante 60 segundos
- [ ] Registro de password "password123" es rechazado con error específico de complejidad
- [ ] La tabla security_events contiene registros de todos los intentos de autenticación
- [ ] Headers de seguridad presentes en todas las respuestas (verificable con curl -I)
- [ ] Las contraseñas en BD nunca se almacenan en texto plano (verificar con query directa a BD)

**Tiempo Estimado:** 5 horas
**Insignia Ganada:** *Hardening de Seguridad*

---

### Ejercicio 2.4: OAuth2 con la Liga Pokémon (Integración Externa)

**Historia:**
La Liga Pokémon (Google/Facebook de este mundo) quiere permitir que los entrenadores se logueen con sus credenciales de la Liga en lugar de crear cuenta nueva. Implementar OAuth2 login.

**Objetivo Técnico:**
Configurar OAuth2 Client con Spring Security 6, manejar usuarios que llegan por primera vez vía OAuth vs usuarios existentes que linkean cuentas.

**Desarrollo Esperado:**

1. Configurar `ClientRegistrationRepository` para OAuth2 providers (simular con GitHub OAuth2 o Google)
2. Implementar `OAuth2UserService` personalizado:
   - Si email existe en BD: actualizar datos de la Liga, generar JWT interno
   - Si no existe: crear usuario nuevo con role TRAINER por defecto, marca `provider=OAUTH`
3. Endpoint /api/auth/oauth2/success que recibe el code y devuelve los tokens internos (access + refresh)
4. Manejar error cuando OAuth provider no entrega email (scope insuficiente)

**Criterios de Aceptación:**

- [ ] Login exitoso via OAuth crea usuario en BD si no existe
- [ ] Usuario existente (registrado manualmente) puede loguearse vía OAuth si el email coincide
- [ ] Si el OAuth provider no provee email, se redirige a formulario para completar datos
- [ ] El token JWT generado post-OAuth es idéntico al de login normal (intercambiable)
- [ ] Logout invalida sesión tanto interna como en provider (revocación de token OAuth si es posible)

**Tiempo Estimado:** 5 horas
**Insignia Ganada:** *Integración OAuth2*

---

## 🌊 FASE 3: GIMNASIO CERULEAN - "LA PRECISIÓN DEL AGUA"

**Líder:** Misty (Perfeccionista)
**Insignia:** Cascade Badge 🌊
**Duración Total:** 16 horas distribuidas en 4 ejercicios

### Ejercicio 3.1: Validación de Datos de Reserva (Bean Validation)

**Historia:**
Misty está harta de que entrenadores intenten reservar habitaciones para fechas pasadas, o que intenten meter 20 personas en una habitación de 2 camas. Necesita validaciones estrictas en los datos de entrada.

**Objetivo Técnico:**
Implementar validaciones complejas usando Jakarta Bean Validation (@Valid, @NotNull, etc.) y validadores personalizados para reglas de negocio específicas.

**Desarrollo Esperado:**

1. DTOs de entrada con validaciones:
   - `CreateReservationRequest`:
     - @FutureOrPresent en checkInDate
     - @Future en checkOutDate
     - @Min(1) @Max(6) en numberOfGuests (máximo 6 Pokémon por entrenador + él mismo)
   - `CreateUserRequest`:
     - @Email válido
     - @Pattern para teléfono (formato internacional)
     - @Size para username (3-20 caracteres, solo alfanumérico)
2. Validador personalizado `@ValidReservationDates`:
   - Check-out debe ser después de check-in
   - Máximo estancia: 30 días
   - No permitir reservas con más de 1 año de anticipación
3. Validación de capacidad:
   - `@RoomCapacityValid` que verifique que numberOfGuests <= room.capacity
   - Implementar `ConstraintValidator` que reciba RoomRepository para consultar capacidad
4. Mensajes de error internacionalizados (i18n) en español e inglés usando MessageSource

**Criterios de Aceptación:**

- [ ] Enviar reserva con checkout antes de checkin devuelve 400 con mensaje específico en español
- [ ] Intentar reservar para 6 personas en habitación de capacidad 4 devuelve error "Capacidad excedida"
- [ ] Reserva con fechas más de 30 días de duración es rechazada
- [ ] Username con caracteres especiales (@#$) es rechazado antes de llegar al servicio
- [ ] Mensajes de error incluyen el campo específico y el valor rechazado

**Tiempo Estimado:** 4 horas
**Insignia Ganada:** *Validaciones Bean*

---

### Ejercicio 3.2: Excepciones de Dominio Personalizadas

**Historia:**
Cuando algo falla en el hotel (habitación no disponible, pago rechazado), Misty quiere que el sistema responda con mensajes claros y códigos específicos para que el frontend sepa cómo reaccionar (ej: mostrar calendario alternativo si la fecha está ocupada).

**Objetivo Técnico:**
Crear jerarquía de excepciones de dominio selladas (sealed classes), implementar GlobalExceptionHandler con @ControllerAdvice, y devolver respuestas de error estructuradas (RFC 7807 Problem Details).

**Desarrollo Esperado:**

1. Clase sellada `DomainException` con subtipos:
   - `RoomNotAvailableException` (con campos: roomId, requestedDates, nextAvailableDate?)
   - `PaymentProcessingException` (con campos: errorCode, transactionId)
   - `PokemonNotAllowedException` (cuando tipo Pokémon no compatible con habitación)
   - `ConcurrentModificationException` (optimistic locking failure)
2. Implementar `ErrorResponse` siguiendo RFC 7807:
   - type: URI que identifica el tipo de error
   - title: mensaje human-readable
   - status: código HTTP
   - detail: descripción específica
   - instance: path de la request
   - timestamp y traceId
3. ExceptionHandler específicos:
   - `MethodArgumentNotValidException` -> mapear a lista de field errors
   - `DataIntegrityViolationException` -> mapear a "Resource already exists" con campo conflictivo
   - `DomainException` -> loguear con nivel WARN, devolver 409 Conflict
4. Logging diferenciado:
   - Errores 4xx: log INFO (errores del cliente)
   - Errores 5xx: log ERROR con stacktrace (errores del servidor)

**Criterios de Aceptación:**

- [ ] Intento de reserva en fechas ocupadas devuelve 409 con body JSON incluyendo fechas alternativas sugeridas
- [ ] Error de validación devuelve 400 con array de campos inválidos y mensajes
- [ ] Error de base de datos (constraint violation) devuelve 409 con mensaje amigable, no stacktrace de SQL
- [ ] Todos los errores incluyen X-Trace-ID en header para correlación con logs
- [ ] Logs de servidor muestran error completo solo para 5xx, para 4xx muestra solo mensaje resumido

**Tiempo Estimado:** 4 horas
**Insignia Ganada:** *Manejo de Errores Profesional*

---

### Ejercicio 3.3: Transacciones y Consistencia

**Historia:**
Cuando un entrenador paga y reserva, deben pasar tres cosas: registrar el pago, confirmar la reserva, y descontar el inventario de objetos si compró en la tienda. Si algo falla a mitad de camino, TODO debe fallar (rollback). No queremos cobrar sin reservar, ni reservar sin cobrar.

**Objetivo Técnico:**
Implementar transacciones distribuidas locales (ACID) usando @Transactional, manejar rollback en casos complejos, y entender isolation levels.

**Desarrollo Esperado:**

1. Servicio `ReservationCreationService` con método anotado `@Transactional`:
   - Paso 1: Validar disponibilidad (pessimistic lock en habitación si es necesario)
   - Paso 2: Crear reserva en estado PENDING
   - Paso 3: Procesar pago (llamar a PaymentService)
   - Paso 4: Si pago OK, actualizar reserva a CONFIRMED
   - Paso 5: Si hay servicios de tienda, descontar stock (otra tabla)
   - Paso 6: Enviar email de confirmación (opcional, puede fallar sin afectar transacción usando @TransactionalEventListener)
2. Implementar compensación manual si el servicio externo de pagos no tiene rollback:
   - Si paso 4 falla después de paso 3 (pago exitoso pero no se pudo confirmar), crear `CompensationTask` para reembolso manual
3. Configurar isolation level READ_COMMITTED para evitar dirty reads pero permitir concurrencia
4. Tests de integración que simulen fallo en paso 5 verificando que paso 2 se hizo rollback (reserva no existe en BD)

**Criterios de Aceptación:**

- [ ] Test: Fallo en procesamiento de pago -> no se crea registro de reserva (rollback)
- [ ] Test: Éxito total -> reserva existe en CONFIRMED, pago existe con estado COMPLETED, stock descontado
- [ ] ConcurrentModificationException manejado correctamente cuando dos usuarios intentan reservar última habitación simultáneamente
- [ ] Timeout configurado en @Transactional (30 segundos) para evitar locks largos
- [ ] Logs muestran inicio y fin de transacción para debugging

**Tiempo Estimado:** 4 horas
**Insignia Ganada:** *Integridad Transaccional*

---

### Ejercicio 3.4: Documentación API con OpenAPI

**Historia:**
El Profesor Oak necesita documentación para que los entrenadores (frontend developers) sepan cómo usar el API. Misty insiste en que sea interactiva y esté siempre actualizada.

**Objetivo Técnico:**
Integrar SpringDoc OpenAPI 3, documentar todos los endpoints con ejemplos, manejar autenticación en Swagger UI, y generar especificación JSON/YAML automáticamente.

**Desarrollo Esperado:**

1. Configurar SpringDoc:
   - Título: "Hotel Pokémon API"
   - Descripción: Sistema de gestión de reservas temáticas
   - Versión: 1.0.0
   - Contacto: Profesor Oak
2. Anotaciones en controllers:
   - @Operation con summary y description
   - @ApiResponses para cada código de estado posible (200, 400, 404, 409)
   - @Schema en DTOs con example values (ej: "pikachu@pokemon.com")
3. Configurar autenticación en Swagger:
   - Botón "Authorize" con JWT Bearer token
   - Incluir descripción de scopes/roles
4. Agrupar endpoints por tags: Auth, Rooms, Reservations, Admin, Services
5. Configurar `springdoc.api-docs.path` para exportar `/api-docs.json`

**Criterios de Aceptación:**

- [ ] Swagger UI accesible en /swagger-ui.html muestra todos los endpoints documentados
- [ ] Cada endpoint muestra códigos de error posibles con descripción
- [ ] Se puede probar autenticación desde Swagger (obtener token, usar en requests)
- [ ] Los DTOs de request/response muestran ejemplos con datos realistas (fechas futuras, precios reales)
- [ ] Exportación JSON de OpenAPI disponible en /v3/api-docs

**Tiempo Estimado:** 4 horas
**Insignia Ganada:** *Documentación Profesional*

---

## 🌱 FASE 4: GIMNASIO CELADON - "EL JARDÍN DE LOS TESTS"

**Líder:** Erika (Maestra de la perfección)
**Insignia:** Rainbow Badge 🌈
**Duración Total:** 24 horas distribuidas en 4 ejercicios

### Ejercicio 4.1: Tests Unitarios con JUnit 5 y MockK

**Historia:**
Erika no confía en el código que no está probado. Quiere que cada pieza de lógica de negocio (cálculo de precios, validación de fechas) tenga tests unitarios aislados, rápidos (<10ms cada uno) y determinísticos.

**Objetivo Técnico:**
Escribir tests unitarios para capa de dominio y aplicación usando MockK (mocking idiomatico Kotlin), JUnit 5, y patrones Given-When-Then.

**Desarrollo Esperado:**

1. Tests para `PriceCalculatorService`:
   - Test: 3 noches × $100/noche = $300
   - Test: Descuento 10% por estancia >7 noches
   - Test: Impuestos 16% calculados sobre subtotal
   - Test: Precio total con servicios adicionales
2. Tests para `AvailabilityService`:
   - Mock de repository para simular habitaciones ocupadas
   - Verificar que devuelve true cuando no hay overlap
   - Verificar que devuelve false cuando hay overlap exacto
   - Verificar manejo de zonas horarias (UTC vs local)
3. Tests para `ReservationValidator`:
   - Fecha check-in es pasado -> IllegalArgumentException
   - Número de huéspedes > capacidad -> false
   - Pokémon tipo FUEGO en habitación tipo AGUA -> warning (permitido pero con costo extra)
4. Configurar `kotest` opcionalmente para estilo BDD más legible

**Criterios de Aceptación:**

- [ ] Cobertura de líneas >80% en paquete domain.service
- [ ] Tests ejecutan en <2 segundos totales (sin Spring context)
- [ ] Todos los tests son independientes (no comparten estado)
- [ ] Uso de `@ParameterizedTest` para probar múltiples casos de cálculo de precios
- [ ] MockK verify utilizado para asegurar que no hay llamadas innecesarias a repositorios

**Tiempo Estimado:** 6 horas
**Insignia Ganada:** *Testing Unitario*

---

### Ejercicio 4.2: Tests de Integración con @SpringBootTest

**Historia:**
Erika quiere verificar que la base de datos, JPA y los repositorios funcionan juntos correctamente. No basta con mocks, necesita datos reales en una base de datos real (pero desechable).

**Objetivo Técnico:**
Implementar tests de integración usando TestContainers (PostgreSQL real), @DataJpaTest, y verificar queries complejas.

**Desarrollo Esperado:**

1. Configurar TestContainers en proyecto:
   - PostgreSQLContainer con imagen 15-alpine
   - Reutilización de contenedor entre tests (@Testcontainers)
   - Flyway ejecuta migraciones en BD de test antes de cada clase
2. Tests para `ReservationRepository`:
   - Test: FindOverlappingResurrences con diferentes casos de borde (mismo día checkout vs checkin)
   - Test: Query nativa de popularidad de habitaciones
   - Test: Paginación de resultados (Pageable)
3. Tests para `UserRepository`:
   - Buscar por email (case insensitive)
   - Verificar que cascade funciona (al borrar usuario se borran sus Pokémon?)
4. Tests de propiedades JPA:
   - Lazy loading funciona (no N+1 en consulta simple)
   - Optimistic locking incrementa version en update concurrente

**Criterios de Aceptación:**

- [ ] Tests levantan contenedor PostgreSQL automáticamente
- [ ] Flyway aplica migraciones y seeds antes de tests
- [ ] Test de concurrencia simula dos threads reservando misma habitación simultáneamente
- [ ] Test de performance: Query de disponibilidad responde en <50ms con 1000 reservas insertadas
- [ ] @Transactional en tests hace rollback automático después de cada método

**Tiempo Estimado:** 6 horas
**Insignia Ganada:** *Testing de Persistencia*

---

### Ejercicio 4.3: Tests E2E con WebTestClient y RestAssured

**Historia:**
El flujo completo debe funcionar: un entrenador llega, se loguea, busca habitación, reserva, paga, y revisa. Erika quiere un test que recorra todo el flujo como si fuera un usuario real.

**Objetivo Técnico:**
Crear tests end-to-end que levanten el contexto completo de Spring, usen base de datos real, y verifiquen flujos HTTP completos incluyendo autenticación.

**Desarrollo Esperado:**

1. Configurar `WebTestClient` (WebFlux) o `MockMvc` (WebMVC) para tests de capa web
2. Test de flujo completo "Happy Path":
   - POST /auth/register -> 201
   - POST /auth/login -> 200 + tokens
   - GET /rooms/available (con token) -> lista de habitaciones
   - POST /reservations (con token) -> 201 + location header
   - GET /reservations/{id} (con token) -> datos de reserva creada
   - POST /payments -> 200
   - PATCH /reservations/{id}/status (check-in) -> 200
3. Test de flujo de error:
   - Intentar reservar sin autenticación -> 401
   - Reservar fechas inválidas -> 400 con mensaje específico
   - Intentar ver reserva de otro usuario -> 403
4. Setup de datos de prueba (@Sql o @BeforeEach) creando habitaciones y servicios necesarios

**Criterios de Aceptación:**

- [ ] Test E2E completo ejecuta en <30 segundos incluyendo arranque de contexto
- [ ] Verificación de headers de seguridad en cada respuesta (X-Content-Type-Options, etc.)
- [ ] Test de idempotencia: llamar dos veces mismo POST de pago con idempotency key devuelve mismo resultado sin duplicar cargo
- [ ] Test de cancelación: crear reserva, cancelar, verificar que habitación vuelve a disponible
- [ ] Screenshots automáticos o logs detallados si falla (usando TestExecutionListener)

**Tiempo Estimado:** 6 horas
**Insignia Ganada:** *Testing End-to-End*

---

### Ejercicio 4.4: Tests de Performance y Carga

**Historia:**
Durante el Campeonato Pokémon anual, el hotel recibe 1000 peticiones simultáneas de reserva. Erika necesita asegurar que el sistema no colapse y mantenga tiempos de respuesta <200ms.

**Objetivo Técnico:**
Implementar tests de carga con JMH (microbenchmarks) o Gatling, medir throughput del endpoint crítico de disponibilidad, y detectar bottlenecks.

**Desarrollo Esperado:**

1. Benchmark con JMH para método crítico:
   - `AvailabilityChecker.checkAvailability()` con 1000 fechas aleatorias
   - Medir ops/segundo y latencia percentil 99
2. Test de carga con Gatling (Kotlin DSL):
   - Escenario: 100 usuarios concurrentes buscan disponibilidad por 60 segundos
   - Ramp up: 10 usuarios/segundo
   - Assertions: 95% de requests <200ms, 0% errores HTTP 5xx
3. Test de estrés:
   - Incrementar hasta 500 usuarios concurrentes
   - Identificar punto de ruptura (cuándo empieza a dar timeouts?)
4. Profiling de memoria:
   - Verificar que no hay memory leaks en caché de disponibilidad
   - Verificar que concurrencia no genera deadlocks en base de datos

**Criterios de Aceptación:**

- [ ] Benchmark JMH muestra throughput >1000 ops/seg en método de disponibilidad
- [ ] Test Gatling con 100 usuarios concurrentes pasa con 99% < 200ms respuesta
- [ ] No hay errores de conexión a base de datos (pool agotado) bajo carga
- [ ] CPU usage se mantiene <70% durante test de carga
- [ ] Reporte HTML de Gatling generado en build/reports

**Tiempo Estimado:** 6 horas
**Insignia Ganada:** *Performance Testing*

---

## 🧪 FASE 5: GIMNASIO CINNABAR - "EL LABORATORIO DE DATOS"

**Líder:** Blaine (Científico de datos)
**Insignia:** Volcano Badge 🌋
**Duración Total:** 16 horas distribuidas en 3 ejercicios

### Ejercicio 5.1: Logging Estructurado y Centralizado

**Historia:**
Cuando algo falla en el hotel (un pago no procesa), Blaine necesita encontrar el problema rápidamente. Los logs dispersos y sin formato dificultan la investigación. Necesita logs estructurados (JSON) con trace IDs.

**Objetivo Técnico:**
Configurar Logback con encoder JSON (Logstash), implementar MDC (Mapped Diagnostic Context) para trace IDs y user IDs, y estructurar logs para análisis con ELK stack.

**Desarrollo Esperado:**

1. Configurar `logback-spring.xml`:
   - Appender Console con `LogstashEncoder` (JSON)
   - Appender File rolling por día
   - Campos incluidos: timestamp, level, logger, message, traceId, userId, method, path, durationMs
2. Implementar `CorrelationIdFilter`:
   - Extraer X-Trace-ID de header o generar UUID
   - Poner en MDC al inicio de request, limpiar al final
   - Incluir traceId en todas las respuestas HTTP (header X-Trace-ID)
3. Logging en capa de servicio:
   - INFO: Operaciones de negocio importantes (reserva creada, pago procesado)
   - DEBUG: Detalles de queries SQL, datos de entrada
   - WARN: Validaciones de negocio que fallan (habitación no disponible)
   - ERROR: Excepciones no controladas
4. Implementar `LoggingAspect` con AOP:
   - Loggear entrada y salida de todos los métodos públicos de @Service
   - Medir y loggear duración de ejecución
   - Mascar datos sensibles en logs (números de tarjeta, passwords)

**Criterios de Aceptación:**

- [ ] Logs aparecen en formato JSON con campos estructurados (parseables por jq)
- [ ] Cada request HTTP tiene traceId único que persiste en todas las líneas de log de esa request
- [ ] Datos sensibles (números de tarjeta) aparecen como [MASKED] en logs
- [ ] En caso de error 500, el log incluye stacktrace completo y datos de contexto (usuario, parámetros)
- [ ] Rotación de archivos: logs mayores a 100MB se comprimen automáticamente

**Tiempo Estimado:** 5 horas
**Insignia Ganada:** *Observabilidad Básica*

---

### Ejercicio 5.2: Métricas de Negocio con Micrometer

**Historia:**
Blaine quiere dashboards que muestren no solo CPU y memoria, sino métricas de negocio: cuántas reservas por hora, ingresos promedio, tasa de cancelación. Esto ayuda a tomar decisiones comerciales.

**Objetivo Técnico:**
Exponer métricas personalizadas usando Micrometer, integrar con Prometheus, y crear métricas de negocio (no solo técnicas).

**Desarrollo Esperado:**

1. Configurar actuator con endpoint `/actuator/prometheus`
2. Métricas técnicas automáticas:
   - JVM memory, GC pauses, threads
   - HTTP server requests (count, sum, max by uri and status)
   - DataSource connection pool (HikariCP metrics)
3. Métricas de negocio personalizadas:
   - Counter: `hotel.reservations.created` (tags: room_type, status)
   - Counter: `hotel.payments.processed` (tags: method, status)
   - Timer: `hotel.reservation.duration` (tiempo entre creación y check-out)
   - Gauge: `hotel.rooms.availability` (habitaciones disponibles ahora mismo)
   - DistributionSummary: `hotel.revenue.per.reservation` (percentiles 50, 90, 99)
4. Implementar `HotelMetrics` componente que registre estas métricas en los servicios correspondientes
5. Configurar alertas básicas (conceptual): si `hotel.reservations.created` < 5 por hora durante 3 horas -> alerta

**Criterios de Aceptación:**

- [ ] Endpoint `/actuator/prometheus` accesible y scrapeable (formato OpenMetrics)
- [ ] Métrica `hotel_reservations_created_total` incrementa al crear reserva (verificable con curl)
- [ ] Métricas incluyen tags dimensional para filtrar (por tipo de habitación, por método de pago)
- [ ] Timer registra percentiles 95 y 99 de duración de estancia
- [ ] Health check personalizado: base de datos debe responder en <1s para estar UP

**Tiempo Estimado:** 5 horas
**Insignia Ganada:** *Métricas de Negocio*

---

### Ejercicio 5.3: Distributed Tracing con Zipkin/Jaeger

**Historia:**
Un pago falló pero no sabemos dónde exactamente: ¿en el API? ¿en el servicio de pagos externo? ¿en la base de datos? Blaine necesita ver el recorrido completo de una transacción a través de todos los componentes.

**Objetivo Técnico:**
Implementar tracing distribuido con Micrometer Tracing y Brave, propagar trace IDs entre servicios (simulando que hay microservicios), y visualizar en Zipkin.

**Desarrollo Esperado:**

1. Configurar Micrometer Tracing con Brave y Zipkin:
   - Sampling rate: 100% en dev, 10% en prod
   - Exportar a Zipkin localhost:9411
2. Instrumentación automática:
   - Web MVC (controller methods)
   - JDBC queries (usando p6spy o datasource-proxy)
   - MongoDB/Redis si se usa (en este caso JPA)
3. Spans personalizados:
   - Crear span manual para "calculate-price" con tags (roomType, nights)
   - Crear span para "external-payment-gateway" midiendo latencia de servicio externo simulado
4. Propagación de contexto:
   - En cabeceras HTTP (B3 propagation)
   - En logs MDC (traceId, spanId)
5. Visualización:
   - Ver trace completo: HTTP POST /reservations -> Service -> Repository -> DB
   - Ver duración de cada span
   - Identificar bottleneck (ej: query lenta de disponibilidad)

**Criterios de Aceptación:**

- [ ] Cada request genera trace visible en Zipkin UI (http://localhost:9411)
- [ ] Trace incluye spans de: controller, service, repository, database query
- [ ] Span de "payment-processing" muestra duración separada del resto
- [ ] Si un request falla, el trace se marca como error y muestra stacktrace
- [ ] Trace ID se propaga a logs (misma ID en logs y en tracing)

**Tiempo Estimado:** 6 horas
**Insignia Ganada:** *Tracing Distribuido*

---

## 🐉 FASE 6: GIMNASIO VIRIDIAN - "LA LIGA FINAL"

**Líder:** Giovanni (Arquitecto de sistemas)
**Insignia:** Earth Badge 🌍
**Duración Total:** 20 horas distribuidas en 3 ejercicios

### Ejercicio 6.1: Dockerización y Docker Compose

**Historia:**
El hotel necesita desplegarse en cualquier servidor fácilmente. Giovanni quiere que la aplicación corra en contenedores, con todas sus dependencias (BD, caché, monitorización), lista para producción.

**Objetivo Técnico:**
Crear Dockerfile multi-stage optimizado, docker-compose completo con healthchecks, y configuración de red segura entre contenedores.

**Desarrollo Esperado:**

1. Dockerfile multi-stage:
   - Stage 1: Build con Gradle (sin daemon, offline mode)
   - Stage 2: Runtime con JRE 17-alpine (mínimo, sin herramientas de desarrollo)
   - Usar usuario no-root (uid 1000) para ejecutar app
   - Healthcheck: curl a /actuator/health cada 30s
2. docker-compose.yml completo:
   - app: build context ., ports 8080, depends_on db/redis
   - db: PostgreSQL 15 con volumen persistente, healthcheck pg_isready
   - redis: para caché de sesiones y rate limiting
   - nginx: reverse proxy, SSL termination (certificados auto-firmados para dev)
   - zipkin: para tracing (opcional pero incluido)
3. Configuración externalizada:
   - application-docker.yml con variables de entorno (SPRING_DATASOURCE_URL, etc.)
   - .env file para secrets (no commiteado)
4. Optimizaciones:
   - Layer caching de Docker (dependencias separadas del código)
   - JVM tuning para containers (-XX:+UseContainerSupport, MaxRAMPercentage)
   - Graceful shutdown (SIGTERM handling)

**Criterios de Aceptación:**

- [ ] `docker-compose up --build` levanta toda la stack funcional en <2 minutos
- [ ] App arranca correctamente y conecta a BD (sin fallos de conexión)
- [ ] Healthcheck de Docker reporta healthy solo cuando /actuator/health está UP
- [ ] Reinicio de contenedor de app no pierde datos (BD persistida en volumen)
- [ ] Imagen final tiene <200MB (usando JRE alpine y sin herramientas de build)

**Tiempo Estimado:** 6 horas
**Insignia Ganada:** *Containerización*

---

### Ejercicio 6.2: Pipeline CI/CD con GitHub Actions

**Historia:**
Giovanni exige que cada cambio pase por pruebas automáticas antes de llegar a producción. Nada debe desplegarse manualmente. El pipeline debe construir, testear, escanear seguridad y desplegar.

**Objetivo Técnico:**
Configurar GitHub Actions con workflow completo: build, test unitarios, test integración, análisis SonarQube, construcción de imagen Docker, y push a registry.

**Desarrollo Esperado:**

1. Workflow `.github/workflows/ci.yml`:
   - Trigger: push a main, pull requests
   - Job 1: Build & Unit Tests (con caché de Gradle)
   - Job 2: Integration Tests (requiere servicio PostgreSQL en el runner)
   - Job 3: SonarCloud Analysis (con cobertura de JaCoCo)
   - Job 4: Security Scan (Trivy en filesystem y en imagen Docker)
   - Job 5: Build & Push Docker (solo en main, tag con SHA y latest)
2. Configurar Quality Gate:
   - Cobertura >80%
   - 0 vulnerabilidades CRITICAL/HIGH en Trivy
   - SonarQube Quality Gate passed
3. Automatización de versiones:
   - Semantic versioning basado en tags (git tag v1.2.3)
   - Changelog automático generado desde commits convencionales
4. (Opcional) Deploy automático a staging:
   - SSH a servidor de staging y ejecutar docker-compose pull && up

**Criterios de Aceptación:**

- [ ] PR creado dispara checks automáticos (tests + sonar)
- [ ] Merge bloqueado si falla cualquier check (branch protection)
- [ ] Imagen Docker subida a DockerHub/GitHub Packages con tag del commit SHA
- [ ] Reporte de Trivy muestra 0 vulnerabilidades CRITICAL en dependencias
- [ ] Badge en README.md muestra estado del build (passing/failing)

**Tiempo Estimado:** 7 horas
**Insignia Ganada:** *Integración Continua*

---

### Ejercicio 6.3: Análisis Estático y Calidad de Código

**Historia:**
Giovanni revisa cada línea de código. No quiere "code smells", deuda técnica, ni vulnerabilidades. El código debe ser idiomático Kotlin y seguir estándares estrictos.

**Objetivo Técnico:**
Configurar Detekt (análisis estático Kotlin), ktlint (formato), OWASP Dependency Check, y SonarQube local/cloud con reglas estrictas.

**Desarrollo Esperado:**

1. Configurar Detekt:
   - Archivo `detekt.yml` con reglas estrictas:
     - Máximo 20 funciones por clase
     - Máximo 3 parámetros por función (usar data classes para configs)
     - ForbiddenComment (no dejar TODO sin issue asociada)
     - Complexity thresholds bajos
   - Integrar con Gradle: `./gradlew detekt`
   - Reporte HTML y Sarif (para GitHub Security tab)
2. Configurar ktlint:
   - Formato automático en pre-commit hook (husky)
   - Reglas específicas: trailing commas, wildcard imports prohibidos
3. OWASP Dependency Check:
   - Tarea Gradle que escanea dependencias buscando CVEs conocidos
   - Falla el build si hay vulnerabilidades CVSS >= 7
4. SonarQube:
   - Reglas específicas para Kotlin (cognitive complexity, code duplication)
   - Coverage integration con JaCoCo
   - Quality Profile "Estricto" activado

**Criterios de Aceptación:**

- [ ] `./gradlew check` ejecuta detekt + ktlint + tests + dependencyCheck
- [ ] 0 issues de severidad HIGH en Detekt
- [ ] Código formateado automáticamente al hacer commit (pre-commit hook)
- [ ] No hay dependencias con vulnerabilidades conocidas (ver reporte HTML)
- [ ] SonarQube reporta <3% de duplicación de código y 0 bugs/blockers

**Tiempo Estimado:** 7 horas
**Insignia Ganada:** *Calidad de Código*

---

## 🏆 PROYECTO FINAL: LA LIGA POKÉMON

**Duración:** 8 horas (proyecto integrador)

### Misión Final: Sistema Completo en "Producción"

**Historia:**
Kai ha completado todos los gimnasios. Ahora debe demostrar que puede operar el Hotel Pokémon completamente: desde la infraestructura hasta el último endpoint. El Profesor Oak hará una inspección final.

**Objetivo:**
Desplegar localmente (o en cloud gratis tipo Render/Railway) el sistema completo con todas las fases integradas, datos de prueba realistas, y documentación de operación.

**Entregables Finales:**

1. **Repositorio GitHub** con:
   - Código completo y funcional (todas las fases integradas)
   - README profesional con instrucciones de instalación
   - Collection de Postman/Insomnia exportada
   - Diagrama de arquitectura (C4 model o similar)

2. **Demo Funcional Local**:
   - `docker-compose up` levanta: App, PostgreSQL, Redis, Zipkin, Nginx
   - Datos semilla: 10 habitaciones, 5 servicios, 3 usuarios (roles distintos)
   - Frontend mínimo (o Swagger) permite hacer reserva completa

3. **Documentación de Operación**:
   - Cómo monitorear (qué métricas mirar)
   - Cómo escalar (qué variables de entorno cambiar)
   - Runbook: "Qué hacer si el pago falla", "Cómo hacer rollback"

4. **Presentación Final (5 minutos)**:
   - Arquitectura explicada
   - Demo en vivo: Crear reserva, ver traza en Zipkin, ver métricas en Prometheus
   - Discusión de trade-offs tomados

**Criterios de Aceptación Finales:**

- [ ] Sistema despliega con un solo comando (docker-compose)
- [ ] Se puede crear usuario, loguear, reservar, pagar, y cancelar sin errores
- [ ] Logs muestran trace IDs correlacionados
- [ ] Métricas disponibles en /actuator/prometheus
- [ ] 0 vulnerabilidades CRITICAL en dependencias
- [ ] Tests pasan en CI (GitHub Actions green)
- [ ] Código coverage >80%

**Recompensa Final:**
**Insignia de Maestro Spring Boot** 🎖️
Y certificación conceptual como "Arquitecto de Software del Hotel Pokémon"

---

## 📊 RESUMEN DE TIEMPOS Y APRENDIZAJES

| Fase | Horas | Insignia | Habilidad Principal |
|------|-------|----------|-------------------|
| **1. Persistencia** | 24h | Boulder 💎 | JPA, SQL, Transacciones |
| **2. Seguridad** | 20h | Thunder ⚡ | JWT, OAuth2, RBAC |
| **3. Validación** | 16h | Cascade 🌊 | Bean Validation, Excepciones |
| **4. Testing** | 24h | Rainbow 🌈 | Unit, Integration, E2E, Performance |
| **5. Observabilidad** | 16h | Volcano 🌋 | Logs, Métricas, Tracing |
| **6. DevOps** | 20h | Earth 🌍 | Docker, CI/CD, Calidad |
| **Proyecto Final** | 8h | Maestro 🎖️ | Integración completa |
| **TOTAL** | **128h** | | (~3 semanas full-time) |

**Producto Final:** Un sistema de reservas hoteleras temático, seguro, observable, testeado y desplegable, listo para escalar a múltiples hoteles de la franquicia Pokémon.
