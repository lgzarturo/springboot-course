# Sistema Pokémon Hotelero — Ejercicios Complementarios

## Índice

1. [Propósito de este Documento](#1-propósito-de-este-documento)
2. [Mapa de Entidades y Relaciones](#2-mapa-de-entidades-y-relaciones)
3. [Fase 1 — Pewter: Módulos de Persistencia Pendientes](#3-fase-1--pewter-módulos-de-persistencia-pendientes)
4. [Fase 2 — Vermilion: Seguridad para Módulos Adicionales](#4-fase-2--vermilion-seguridad-para-módulos-adicionales)
5. [Fase 3 — Cerulean: Validaciones en Módulos Pendientes](#5-fase-3--cerulean-validaciones-en-módulos-pendientes)
6. [Fase 4 — Celadon: Testing por Capa](#6-fase-4--celadon-testing-por-capa)
7. [Fase 5 — Cinnabar: Estadísticas y Automatización](#7-fase-5--cinnabar-estadísticas-y-automatización)
8. [Fase 6 — Viridian: Configuración Avanzada de Producción](#8-fase-6--viridian-configuración-avanzada-de-producción)
9. [Módulos Integradores para la Liga Pokémon](#9-módulos-integradores-para-la-liga-pokémon)
10. [Tabla Resumen](#10-tabla-resumen)

---

## 1. Propósito de este Documento

Este documento es un complemento al [Roadmap de Ejercicios](exercises-roadmap.md). El roadmap construye el
sistema usando las reservas y habitaciones como hilo conductor, pero deja como stubs varios módulos del Hotel
Pokémon: `amenities`, `tours`, `pokemon`, `payments`, `reviews`, `cart` y `gamification`.

**Este documento propone ejercicios para:**

1. **Completar los módulos stub** con el mismo nivel de cuidado que el roadmap principal
2. **Ofrecer variantes de menor complejidad** que sirven como calentamiento antes de los ejercicios avanzados
   del roadmap
3. **Plantear escenarios de integración** entre módulos que el roadmap no cubre directamente

**Cómo usar este documento:**
- Trabaja en paralelo: termina la Fase N del roadmap, luego practica con los ejercicios de la Fase N de aquí
- O úsalos al terminar el roadmap completo, como desafíos de consolidación
- Cada ejercicio tiene marcador de complejidad: ★ (básico) a ★★★★ (avanzado)

---

## 2. Mapa de Entidades y Relaciones

El Hotel Pokémon maneja las siguientes entidades. Los módulos marcados con 🚧 son los que el roadmap deja
como stub y este documento propone completar.

### Entidades de Hotelería

| Entidad       | Atributos Clave                                                                   | Estado en Roadmap |
|---------------|-----------------------------------------------------------------------------------|-------------------|
| `User`        | `id`, `email`, `password`, `role` (TRAINER/GYM_LEADER/PROFESOR_OAK)               | ✅ Cubierto        |
| `Hotel`       | `id`, `name`, `address`, `city`, `category` (ECONOMIC/STANDARD/LUXURY)            | ✅ Cubierto        |
| `Room`        | `id`, `roomNumber`, `type`, `pricePerNight`, `capacity`, `status`, `pokemonTheme` | ✅ Cubierto        |
| `Reservation` | `id`, `checkInDate`, `checkOutDate`, `status`, `totalPrice`                       | ✅ Cubierto        |
| `Amenity`     | `id`, `name`, `description`, `category`                                           | 🚧 Stub           |
| `Tour`        | `id`, `name`, `location`, `price`, `durationMinutes`, `capacity`                  | 🚧 Stub           |
| `Payment`     | `id`, `amount`, `method`, `status`, `transactionReference`                        | 🚧 Stub           |
| `Review`      | `id`, `rating` (1-5), `comment`, `date`                                           | 🚧 Stub           |

### Entidades Pokémon

| Entidad       | Atributos Clave                                                                     | Estado en Roadmap |
|---------------|-------------------------------------------------------------------------------------|-------------------|
| `Pokemon`     | `id`, `nickname`, `species`, `type`, `level`, `specialNeeds`, `isActive`            | 🚧 Stub           |
| `TrainerCard` | `id`, `userId`, `trainerSince`, `badgesEarned`, `totalStays`                        | 🚧 Stub           |

### Relaciones Clave

```
Hotel ──< Room ──< Reservation >── User
Hotel >──< Amenity  (via HotelAmenity con costo extra)
Hotel ──< Tour ──< TourSchedule ──< TourBooking >── User
User ──< Pokemon
Reservation ──< Payment
Reservation >── Review
```

---

## 3. Fase 1 — Pewter: Módulos de Persistencia Pendientes

> Realiza estos ejercicios después de completar los Ejercicios 1.1 a 1.4 del Roadmap.
> Brock ya te enseñó los fundamentos. Ahora aplica el mismo patrón a módulos que el hotel necesita
> urgentemente.

---

### 3.1 La Biblioteca de Servicios — Módulo de Amenidades ★★

**Historia**: El Profesor Oak revisó el sistema y notó que todos los hoteles dicen tener "WiFi y piscina" en
el campo `description`, en texto libre. El GYM_LEADER no puede buscar hoteles por amenidad específica, ni
saber cuántos hoteles ofrecen spa. Brock pide que las amenidades sean entidades propias con relación
Many-to-Many a `Hotel`.

**Objetivo Técnico**: Implementar el módulo `Amenity` completo con relación Many-to-Many a `Hotel`,
incluyendo tabla intermedia explícita y CRUD básico. Usar el patrón Port-Adapter del módulo de habitaciones.

**Desarrollo Esperado:**

1. Entidad `Amenity` (catálogo global):
   - `name`: String único (ej: "WiFi", "Piscina Olímpica", "Centro Pokémon de Guardia")
   - `description`: String nullable
   - Enum `AmenityCategory`: CONNECTIVITY, WELLNESS, POKEMON_SERVICES, FOOD, ENTERTAINMENT
   - `available`: Boolean (soft-disable sin borrar registros)
2. Entidad intermedia `HotelAmenity` (relación Many-to-Many con atributos extra):
   - Campos: `included` (Boolean — si está en el precio base o es cargo extra), `extraCost` (BigDecimal nullable)
   - No usar `@ManyToMany` directamente; modelar `HotelAmenity` como entidad propia con su PK compuesta
3. Migración `V5__create_amenities.sql`
4. Repositorio `AmenityRepository` con:
   - `findByCategoryAndAvailableTrue(category: AmenityCategory): List<Amenity>`
   - `findByHotelId(hotelId: Long): List<HotelAmenity>` (con sus costos)
5. Endpoints REST (GYM_LEADER y PROFESOR_OAK para escritura, público para lectura):
   - `GET /api/amenities` — catálogo completo paginado
   - `POST /api/amenities` — crear amenidad en el catálogo
   - `POST /api/hotels/{id}/amenities` — asignar amenidad a hotel con su costo
   - `DELETE /api/hotels/{id}/amenities/{amenityId}` — desasignar amenidad del hotel

**Criterios de Aceptación:**

- [ ] La tabla `hotel_amenities` tiene foreign keys a `hotels` y `amenities` con constraint UNIQUE (hotel_id,
      amenity_id)
- [ ] `GET /api/hotels/{id}` ahora incluye lista de amenidades con sus costos en la respuesta
- [ ] No se puede asignar la misma amenidad dos veces al mismo hotel (409 Conflict con mensaje claro)
- [ ] Soft-delete de amenidad (`available=false`) no borra registros en `hotel_amenities`
- [ ] Test de integración: crear hotel, crear 3 amenidades, asignar 2 de ellas, verificar que
      `findByHotelId` devuelve exactamente 2

**Tiempo Estimado:** 5 horas | **Complejidad:** ★★

---

### 3.2 El Safari Zone — Módulo de Tours Pokémon ★★★

**Historia**: Los entrenadores preguntan todos los días si pueden salir a explorar. Brock decide formalizar
esto: el hotel ofrece "Tours del Safari Zone" con horarios fijos, capacidad limitada y tipos de Pokémon
visibles en cada recorrido. Un entrenador puede reservar un tour igual que reserva una habitación, pero con
reglas distintas: la capacidad se gestiona por slots, no por habitaciones.

**Objetivo Técnico**: Implementar el módulo `Tour` con entidades `TourSchedule` (fecha y hora concreta de un
recorrido) y `TourBooking` (reserva de plaza). Usar `@Version` para optimistic locking en la gestión de
slots disponibles.

**Desarrollo Esperado:**

1. Entidad `Tour` (catálogo de recorridos del hotel):
   - `name`: "Safari Zone Express", "Tour Nocturno de Gengar", "Expedición al Monte Pelado"
   - `description`, `location` (zona del hotel)
   - `basePrice`: BigDecimal
   - `durationMinutes`: Int
   - `maxCapacity`: Int (máximo de participantes por horario)
   - Lista `availablePokemonTypes`: @ElementCollection de PokemonType
   - Relación Many-to-One con `Hotel`
2. Entidad `TourSchedule` (ocurrencia concreta del tour):
   - `scheduledAt`: LocalDateTime
   - `availableSlots`: Int con `@Version` para optimistic locking
   - Enum `ScheduleStatus`: OPEN, FULL, CANCELLED, COMPLETED
   - Relación Many-to-One con `Tour`
3. Entidad `TourBooking` (el entrenador reserva una plaza):
   - Relación Many-to-One con `TourSchedule` y con `User`
   - `numberOfParticipants`: Int (puede ir acompañado)
   - Enum `BookingStatus`: CONFIRMED, CANCELLED
4. Migración `V6__create_tours.sql`
5. Lógica de negocio en `TourService`:
   - Al crear `TourBooking`, decrementar `availableSlots` y validar que no sea negativo
   - Al cancelar `TourBooking`, devolver los slots al `TourSchedule`
   - Cuando `availableSlots == 0`, cambiar `ScheduleStatus` a FULL automáticamente
6. Endpoints:
   - `GET /api/hotels/{id}/tours` — catálogo de tours del hotel (público)
   - `GET /api/tours/{id}/schedules` — próximos horarios disponibles (público)
   - `POST /api/tours/schedules/{scheduleId}/book` — reservar plaza (TRAINER autenticado)
   - `DELETE /api/tours/bookings/{bookingId}` — cancelar reserva de tour (dueño o GYM_LEADER)

**Criterios de Aceptación:**

- [ ] Intentar reservar más plazas de las disponibles devuelve 409 con los slots disponibles actuales
- [ ] `availableSlots` se actualiza correctamente al reservar y al cancelar (test de integración)
- [ ] Dos threads intentan reservar los últimos 2 slots simultáneamente: solo uno lo logra
      (`@Version` garantiza la consistencia)
- [ ] Tour con `ScheduleStatus=FULL` no aparece en el listado de horarios disponibles
- [ ] Test: tour con capacidad 5, tres bookings de 2 plazas cada uno — el tercero devuelve 409

**Tiempo Estimado:** 8 horas | **Complejidad:** ★★★

---

### 3.3 La Mochila del Entrenador — Perfil y Equipo Pokémon ★★

**Historia**: Los entrenadores quieren registrar su equipo. Un entrenador lleva máximo 6 Pokémon activos.
Cada Pokémon tiene necesidades especiales que el hotel debe conocer (ej: "Charizard necesita habitación con
ventilación reforzada"). El Pokédex del hotel quiere estadísticas de qué tipos de Pokémon visitan más.

**Objetivo Técnico**: Implementar el módulo `Pokemon` con relación al usuario, validación de límite de
equipo, y endpoint de estadísticas del Pokédex del hotel.

**Desarrollo Esperado:**

1. Entidad `Pokemon`:
   - `nickname`: String (nombre que le puso el entrenador, 1-50 chars)
   - `species`: String (ej: "Charizard", "Pikachu", "Mewtwo")
   - Enum `PokemonType`: FIRE, WATER, GRASS, ELECTRIC, PSYCHIC, GHOST, DRAGON, NORMAL, FIGHTING,
     POISON, GROUND, FLYING, ROCK, BUG, ICE
   - `level`: Int (1-100)
   - `specialNeeds`: String nullable (texto libre para el staff del hotel)
   - `isActive`: Boolean (activo en el equipo vs. en el PC Box)
   - Relación Many-to-One con `User`
2. Migración `V7__create_pokemon.sql`
3. Lógica de validación en `PokemonService`:
   - Un `User` no puede tener más de 6 Pokémon con `isActive=true` simultáneamente
4. Proyección `PokemonSpeciesCount(species: String, count: Long)` para estadísticas
5. Endpoints:
   - `GET /api/me/pokemon` — ver mi equipo actual y PC Box (TRAINER autenticado)
   - `POST /api/me/pokemon` — añadir Pokémon al equipo (falla si ya tiene 6 activos)
   - `PUT /api/me/pokemon/{id}` — actualizar nickname y specialNeeds
   - `DELETE /api/me/pokemon/{id}` — retirar al PC Box (`isActive=false`, no borrado físico)
   - `GET /api/admin/pokemon/stats` — top 10 especies más frecuentes (solo PROFESOR_OAK)

**Criterios de Aceptación:**

- [ ] Añadir el séptimo Pokémon activo devuelve 422 con mensaje "Equipo lleno: máximo 6 Pokémon activos"
- [ ] `DELETE` pone `isActive=false` pero el registro permanece en la BD
- [ ] `GET /api/me/pokemon` solo devuelve los Pokémon del usuario autenticado (nunca los de otro)
- [ ] El endpoint de estadísticas devuelve las 10 especies más frecuentes entre todos los huéspedes actuales
- [ ] Test unitario: `PokemonService.addPokemon()` falla correctamente cuando el equipo está lleno
- [ ] Test de integración: endpoint de estadísticas con datos conocidos

**Tiempo Estimado:** 5 horas | **Complejidad:** ★★

---

### 3.4 La Caja Registradora — Módulo de Pagos ★★★

**Historia**: Actualmente las reservas pasan a CONFIRMED de forma mágica, sin procesar ningún pago real.
Brock pide que cada reserva tenga un `Payment` asociado con su ciclo de vida propio. El flujo: reserva en
PENDING → entrenador inicia pago → confirmación del pago → reserva pasa a CONFIRMED. Si el pago falla, la
reserva vuelve a PENDING.

**Objetivo Técnico**: Implementar el módulo `Payment` con transiciones de estado controladas. El cambio de
estado del pago y el cambio de estado de la reserva deben ocurrir en la misma transacción `@Transactional`.

**Desarrollo Esperado:**

1. Entidad `Payment`:
   - Relación One-to-One con `Reservation`
   - `amount`: BigDecimal (snapshot del precio en el momento del pago)
   - Enum `PaymentMethod`: CREDIT_CARD, DEBIT_CARD, POKE_WALLET, POKEMON_LEAGUE_POINTS
   - Enum `PaymentStatus`: PENDING, COMPLETED, FAILED, REFUNDED
   - `transactionReference`: String (UUID único generado por el sistema)
   - `processedAt`: LocalDateTime nullable
   - `failureReason`: String nullable
2. Migración `V8__create_payments.sql`
3. `PaymentService` con lógica transaccional:
   - `initiatePayment(reservationId, method)`: crea Payment en PENDING con transactionReference único
   - `confirmPayment(paymentId)`: simula éxito → actualiza Payment a COMPLETED + Reservation a CONFIRMED
   - `failPayment(paymentId, reason)`: actualiza a FAILED + Reservation vuelve a PENDING
   - `refundPayment(paymentId)`: solo si Reservation está CANCELLED, marca REFUNDED
4. Endpoints:
   - `POST /api/reservations/{id}/payment` — iniciar pago (TRAINER dueño de la reserva)
   - `POST /api/payments/{id}/confirm` — confirmar pago (simula respuesta de pasarela)
   - `POST /api/payments/{id}/fail` — simular fallo (útil para tests y desarrollo)
   - `GET /api/payments/{id}` — ver estado del pago (dueño o GYM_LEADER/OAK)
   - `POST /api/payments/{id}/refund` — solicitar reembolso (solo si reserva CANCELLED)

**Criterios de Aceptación:**

- [ ] No se puede confirmar un pago si el Payment ya está COMPLETED o FAILED
- [ ] Si `confirmPayment` falla al actualizar la Reservation, el Payment no queda en COMPLETED (rollback)
- [ ] El `transactionReference` es único con constraint en BD (dos pagos nunca tienen el mismo)
- [ ] Solo el dueño de la Reservation o GYM_LEADER/OAK puede ver el detalle del pago
- [ ] Test de rollback: simular excepción en el paso de actualización de la Reservation → verificar que
      Payment sigue en PENDING

**Tiempo Estimado:** 7 horas | **Complejidad:** ★★★

---

## 4. Fase 2 — Vermilion: Seguridad para Módulos Adicionales

> Realiza estos ejercicios después de completar los Ejercicios 2.1 a 2.4 del Roadmap.
> Lt. Surge ya blindó las reservas y habitaciones. Ahora toca asegurar los módulos nuevos.

---

### 4.1 El Carnet de Identidad — Registro y Perfil del Entrenador ★★

**Historia**: Lt. Surge nota que el sistema solo tiene `POST /api/auth/login` pero no un registro público.
Los entrenadores llegan al hotel sin carnet. Necesita que cualquiera pueda registrarse como TRAINER, con
validación de email y contraseña robusta. También faltan endpoints para que el entrenador gestione su perfil
propio sin que un GYM_LEADER tenga que hacerlo por él.

**Objetivo Técnico**: Implementar endpoint de registro público con validación de contraseña, flujo de
verificación de email (simulado via log), y endpoints de gestión del perfil autenticado.

**Desarrollo Esperado:**

1. Endpoint `POST /api/auth/register` (público, sin autenticación):
   - Acepta: `username`, `email`, `password`, `firstName`, `lastName`
   - Valida complejidad de contraseña: mínimo 8 caracteres, 1 mayúscula, 1 número
   - Crea usuario con `role=TRAINER` y `emailVerified=false`
   - Genera token de verificación (UUID, TTL de 24h) guardado en tabla `email_verifications`
   - Simula envío: `log.info("Email de verificación enviado a: {}", email)` con el token en el log (nivel
     DEBUG solo)
2. Endpoint `POST /api/auth/verify?token={uuid}` (público):
   - Valida el token y su expiración
   - Marca `emailVerified=true` en el usuario
   - Invalida el token (no puede reutilizarse)
   - Usuarios no verificados pueden registrarse pero no pueden autenticarse
3. Endpoints de perfil propio (TRAINER autenticado):
   - `GET /api/me` — ver mi perfil (nunca incluye el hash de contraseña)
   - `PUT /api/me` — actualizar `firstName`, `lastName`, `phoneNumber` (no email ni password aquí)
   - `PUT /api/me/password` — cambiar contraseña (requiere `currentPassword` para confirmar identidad)

**Criterios de Aceptación:**

- [ ] Usuario no verificado intenta login → 401 con mensaje "Email no verificado, revisa tu bandeja"
- [ ] Token de verificación expirado (>24h) → 400 con mensaje "Token expirado, solicita uno nuevo"
- [ ] `GET /api/me` nunca incluye el campo `password` en ninguna respuesta
- [ ] `PUT /api/me/password` con `currentPassword` incorrecta → 400 (no 401)
- [ ] Test E2E: registrar → intentar login (falla) → verificar email → login (exitoso)

**Tiempo Estimado:** 5 horas | **Complejidad:** ★★

---

### 4.2 Los Permisos del Safari Zone — Seguridad en el Módulo de Tours ★★

**Historia**: Lt. Surge revisa el módulo de Tours y detecta dos problemas: los TRAINER pueden reservar tours
en hoteles donde no tienen ninguna reserva activa, y los GYM_LEADER pueden editar tours de otros hoteles. El
acceso a un tour debe estar ligado a una estadía activa, no solo a estar autenticado.

**Objetivo Técnico**: Implementar autorización a nivel de recurso para el módulo de Tours usando
`@PreAuthorize` con SpEL y un componente de seguridad personalizado `TourSecurityService`.

**Desarrollo Esperado:**

1. Crear `TourSecurityService` como `@Service`:
   - `isHostedByUserHotel(tourId: Long, userId: Long): Boolean` — verifica que el tour pertenece al hotel
     del GYM_LEADER autenticado
   - `hasActiveReservationInHotel(hotelId: Long, userId: Long): Boolean` — verifica que el TRAINER tiene
     reserva en estado CONFIRMED o CHECKED_IN en ese hotel
2. Aplicar `@PreAuthorize` con SpEL en los endpoints de Tour:
   - `POST /api/tours/schedules/{scheduleId}/book`:
     `@PreAuthorize("@tourSecurity.hasActiveReservationInHotel(#scheduleId, principal.id)")`
   - `PUT /api/tours/{id}` (editar tour):
     `@PreAuthorize("hasRole('PROFESOR_OAK') or @tourSecurity.isHostedByUserHotel(#id, principal.id)")`
   - `DELETE /api/tours/schedules/{id}`:
     `@PreAuthorize("hasRole('PROFESOR_OAK') or @tourSecurity.isHostedByUserHotel(#id, principal.id)")`
3. Endpoint adicional: `GET /api/me/tours` — ver mis bookings de tours activos (solo los propios)

**Criterios de Aceptación:**

- [ ] TRAINER sin reserva activa en el hotel intenta reservar tour → 403 con mensaje "Requieres una estadía
      activa en este hotel para unirte a tours"
- [ ] GYM_LEADER del Hotel A intenta editar tour del Hotel B → 403
- [ ] PROFESOR_OAK puede editar cualquier tour sin restricción
- [ ] `GET /api/me/tours` devuelve solo los bookings del usuario autenticado
- [ ] Tests de integración con `@WithMockUser` para cada combinación de rol y acción crítica

**Tiempo Estimado:** 4 horas | **Complejidad:** ★★

---

## 5. Fase 3 — Cerulean: Validaciones en Módulos Pendientes

> Realiza estos ejercicios después de completar los Ejercicios 3.1 a 3.4 del Roadmap.
> Misty ya validó las reservas y los usuarios. Ahora aplica la misma precisión a los módulos pendientes.

---

### 5.1 El Guardián del PC Box — Validaciones del Equipo Pokémon ★★

**Historia**: Misty descubre que los entrenadores pueden registrar Pokémon de nivel 0 o de nivel 200 por
error. También encontró que alguien registró un "Charmander" de tipo WATER sin ningún aviso del sistema. El
registro de Pokémon necesita validaciones estrictas tanto en los datos básicos como en la compatibilidad
entre especie y tipo.

**Objetivo Técnico**: Implementar Bean Validation en el módulo Pokémon, incluyendo un `ConstraintValidator`
personalizado que verifique la compatibilidad especie-tipo usando un servicio de dominio.

**Desarrollo Esperado:**

1. Validaciones estándar en `CreatePokemonRequest`:
   - `@Size(min=1, max=50)` en `nickname`
   - `@Min(1) @Max(100)` en `level`
   - `@NotBlank` en `species`
   - `@NotNull` en `type`
2. Validador personalizado `@ValidPokemonSpeciesAndType`:
   - Verifica que la especie declarada es compatible con el tipo declarado
   - Ejemplo: Charmander solo puede ser FIRE, Squirtle solo WATER, Pikachu solo ELECTRIC
   - Implementar con `ConstraintValidator<ValidPokemonSpeciesAndType, CreatePokemonRequest>` que inyecta
     `PokemonDexService` (componente con el mapa de compatibilidades de las 151 especies de Kanto)
   - El mensaje de error incluye el tipo correcto: "Pikachu no puede ser de tipo WATER (tipo esperado:
     ELECTRIC)"
3. Regla de negocio en servicio (no en validador): si el tipo del Pokémon es incompatible con el
   `pokemonTheme` de la habitación reservada, añadir texto automático a `specialNeeds`: "Requiere adaptador
   de ambiente: tipo {pokemonType} en habitación tema {roomTheme}"

**Criterios de Aceptación:**

- [ ] Registrar Charmander con tipo WATER → 400 con mensaje que indica el tipo esperado (FIRE)
- [ ] Especie desconocida (ej: "MegaMewthree") → 400 con mensaje "Especie no registrada en el Pokédex de
      Kanto"
- [ ] Nivel 0 o nivel 101 → 400 con el nombre del campo y el valor rechazado
- [ ] Pikachu tipo ELECTRIC en habitación tema WATER → `specialNeeds` actualizado automáticamente
- [ ] Test unitario para `PokemonDexService.isCompatible()` cubriendo los 151 casos de Kanto

**Tiempo Estimado:** 5 horas | **Complejidad:** ★★

---

### 5.2 El Mapa de Tours Válidos — Validaciones del Módulo Tours ★★★

**Historia**: Misty descubre que los GYM_LEADER pueden crear horarios de tours en fechas pasadas, con
capacidad de 0 personas, o con horarios que se solapan entre sí. También pueden cancelar un horario aunque
haya entrenadores con booking confirmado, dejándolos sin alternativa. Necesita validaciones que cubran DTOs
y reglas de negocio de solapamiento.

**Objetivo Técnico**: Implementar validaciones de DTO y de negocio para el módulo Tours, incluyendo un
validador de solapamiento de horarios y excepción de dominio con detalles del conflicto.

**Desarrollo Esperado:**

1. Validaciones en `CreateTourScheduleRequest`:
   - `@Future` en `scheduledAt`
   - `@Min(1) @Max(50)` en `availableSlots`
   - `@PositiveOrZero` en `basePrice` (tours gratuitos son válidos)
2. Validador personalizado `@NonOverlappingSchedule` a nivel de clase:
   - Verifica que el nuevo horario no solapa con un existente del mismo tour
   - Solapamiento: un tour de 120 min que empieza a las 10:00 ocupa hasta las 12:00; el siguiente
     horario válido empieza a las 12:00 o después
   - Usa `TourScheduleRepository.findOverlappingSchedules()` query nativa con operador OVERLAPS
3. Excepción de dominio `TourScheduleConflictException` con campos: `conflictingScheduleId`,
   `conflictStart`, `conflictEnd`
4. Regla de negocio en servicio: no se puede cancelar un `TourSchedule` con bookings CONFIRMED activos;
   la excepción incluye la lista de entrenadores afectados para notificarlos

**Criterios de Aceptación:**

- [ ] Horario para ayer → 400 con mensaje "La fecha del tour debe ser en el futuro"
- [ ] Horario que solapa con uno existente → 409 con los datos del horario conflictivo en el body
- [ ] Cancelar horario con bookings confirmados → 409 con la lista de usernames afectados
- [ ] Tour con `availableSlots=0` → 400 (antes de llegar al servicio)
- [ ] Test de integración: tour de 60 min a las 10:00; crear otro a las 10:30 → 409; a las 11:00 → 409;
      a las 11:00:00 exactas → 409; a las 11:00:01 → 201

**Tiempo Estimado:** 5 horas | **Complejidad:** ★★★

---

### 5.3 La Opinión del Entrenador — Módulo de Reseñas ★★★

**Historia**: Misty quiere que los entrenadores puedan dejar reseñas, pero con reglas claras: solo quien
tuvo una estadía completada puede opinar, una sola reseña por estadía, y el comentario debe ser
significativo (mínimo 20 caracteres). Las reseñas son públicas y cualquiera puede leerlas.

**Objetivo Técnico**: Implementar el módulo `Review` completo con sus reglas de validación y negocio.
Este módulo también es el núcleo del proyecto final de la Liga Pokémon; aquí se construye con énfasis en
las validaciones y el flujo de datos.

**Desarrollo Esperado:**

1. Entidad `Review`:
   - `rating`: Int (1-5)
   - `comment`: String (20-500 caracteres)
   - `createdAt`: LocalDateTime
   - Relación Many-to-One con `User` (el autor)
   - Relación Many-to-One con `Reservation` (la estadía que se reseña)
   - Relación Many-to-One con `Hotel` (para facilitar listados por hotel)
   - Constraint UNIQUE en `(userId, reservationId)` — una sola reseña por estadía
2. Migración `V9__create_reviews.sql`
3. Validaciones de negocio en `ReviewService`:
   - La `Reservation` debe estar en estado CHECKED_OUT para poder reseñar
   - La `Reservation` debe pertenecer al usuario autenticado
4. Proyección `ReviewSummary` para el endpoint de resumen (promedio y distribución)
5. Endpoints:
   - `POST /api/reservations/{id}/review` — crear reseña (solo TRAINER, post-checkout)
   - `GET /api/hotels/{id}/reviews` — listar reseñas del hotel (público, paginado)
   - `GET /api/hotels/{id}/reviews/summary` — promedio de calificación y distribución de estrellas

**Criterios de Aceptación:**

- [ ] Rating 0 o 6 → 400 (validación de Bean Validation)
- [ ] Comentario de menos de 20 caracteres → 400 con mensaje que indica el mínimo
- [ ] Reseñar reserva que no está CHECKED_OUT → 409 con el estado actual de la reserva
- [ ] Segunda reseña a la misma reserva → 409 "Ya existe una reseña para esta estadía"
- [ ] `GET /api/hotels/{id}/reviews/summary` devuelve: `avgRating`, `totalReviews`, y `distribution`
      (mapa de estrellas a cantidad: `{5: 10, 4: 5, 3: 2, 2: 1, 1: 0}`)
- [ ] Test E2E: crear reserva → check-in → check-out → crear reseña → verificar en listado del hotel

**Tiempo Estimado:** 6 horas | **Complejidad:** ★★★

---

## 6. Fase 4 — Celadon: Testing por Capa

> Realiza estos ejercicios después de completar los Ejercicios 4.1 a 4.5 del Roadmap.
> Erika ya te enseñó la pirámide de tests. Ahora aplícala a los módulos que completaste.

---

### 6.1 El Dojo de Controladores — WebMvcTest Exhaustivo ★★

**Historia**: Erika revisa los tests del equipo y nota que todos los `@WebMvcTest` solo prueban el happy
path. Para ella, un test que solo verifica que 200 es 200 no es un test, es teatro. Quiere cobertura
completa de la capa web para los nuevos módulos: 401 sin token, 403 con rol incorrecto, 400 con DTO
inválido, 409 con conflicto de negocio.

**Objetivo Técnico**: Escribir tests de controlador exhaustivos usando `@WebMvcTest` + `MockMvc` para cada
controlador de los módulos nuevos. Cada controlador debe tener mínimo 5 tests cubriendo happy path y 4
casos de error distintos.

**Desarrollo Esperado:**

Para cada uno de estos controladores, implementar su clase de test con `@WebMvcTest`:

1. `TourControllerTest`:
   - `GET /api/hotels/{id}/tours` sin autenticación → 401
   - `POST /api/tours` sin rol GYM_LEADER → 403
   - `POST /api/tours` con body inválido (capacity negativa) → 400 con estructura de error correcta
   - `POST /api/tours` con datos válidos → 201 con `Location` header apuntando al recurso creado
   - `POST /api/tours/schedules/{id}/book` cuando el servicio lanza `TourScheduleConflictException` → 409
2. `ReviewControllerTest`:
   - `POST /api/reservations/{id}/review` sin autenticación → 401
   - `POST` con rating inválido (ej: 7) → 400 con el campo `rating` en el array de errores
   - `POST` cuando el servicio lanza `ReservationNotCheckedOutException` → 409
   - `GET /api/hotels/{id}/reviews` → 200 con estructura de paginación correcta
   - `GET /api/hotels/{id}/reviews/summary` → 200 con campos `avgRating` y `distribution`
3. `PokemonControllerTest`:
   - `POST /api/me/pokemon` con nivel 0 → 400 con mensaje del validador
   - `POST /api/me/pokemon` cuando servicio lanza `PokemonTeamFullException` → 422
   - `DELETE /api/me/pokemon/{id}` de un Pokémon de otro usuario → 403

**Criterios de Aceptación:**

- [ ] Cada controlador tiene mínimo 5 tests
- [ ] Todos los tests usan `andExpect(jsonPath("$.field", is(...)))` para verificar contenido del body, no
      solo el status code
- [ ] Los tests de autenticación usan `@WithMockUser(roles=["TRAINER"])` con roles específicos
- [ ] Ningún test hace llamadas reales al repositorio (todos los servicios están mockeados con
      `@MockBean`)
- [ ] Cada test ejecuta en menos de 500ms (sin contexto completo de Spring)

**Tiempo Estimado:** 6 horas | **Complejidad:** ★★

---

### 6.2 El Laboratorio del Repositorio — DataJpaTest en Queries Críticas ★★★

**Historia**: Erika encontró que los tests de repositorio del proyecto solo verifican `save()` y
`findById()`. Las queries JPQL y nativas que detectan solapamientos, calculan agregaciones, o filtran por
estado compuesto nunca se prueban aisladas. Esas queries son donde los bugs realmente se esconden.

**Objetivo Técnico**: Escribir tests de repositorio con `@DataJpaTest` para todas las queries personalizadas
de los módulos nuevos. Incluir casos de borde que los tests de controlador no pueden cubrir.

**Desarrollo Esperado:**

1. `TourScheduleRepositoryTest`:
   - Test: `findOverlappingSchedules` — tour de 60 min a las 10:00; consultar solapamiento a las 10:59
     → devuelve conflicto; a las 11:00 → no devuelve conflicto (caso borde de exclusividad de extremo)
   - Test: `findAvailableSchedules` no incluye schedules con `status=FULL` ni `status=CANCELLED`
   - Test: conteo de `availableSlots` es correcto después de un booking parcial
2. `ReviewRepositoryTest`:
   - Test: `findAverageRatingByHotelId` con 0 reseñas devuelve `Optional.empty()`, no lanza excepción
   - Test: promedio correcto con 3 reseñas de ratings 5, 3, 4 → average = 4.0
   - Test: intentar insertar segunda reseña para misma combinación (userId, reservationId) →
     `DataIntegrityViolationException`
3. `PokemonRepositoryTest`:
   - Test: `findTop10SpeciesByCount()` con 3 especies (5 Pikachu, 3 Charizard, 2 Mewtwo) devuelve en orden
     correcto
   - Test: cuando hay empate, el resultado es determinístico (hay un `ORDER BY` secundario en la query)

**Criterios de Aceptación:**

- [ ] Tests usan `@DataJpaTest` con Flyway aplicando las migraciones completas (no solo las entidades del
      test)
- [ ] Cada test popula sus propios datos en `@BeforeEach` (no dependencia de datos de otros tests)
- [ ] Los casos de borde tienen un comentario que documenta la decisión de negocio que están verificando
- [ ] Mínimo 3 tests por repositorio, con al menos uno de caso borde explícito
- [ ] Tests de queries nativas usan `@DataJpaTest` con H2 (y documentan si la query es específica de
      PostgreSQL)

**Tiempo Estimado:** 6 horas | **Complejidad:** ★★★

---

### 6.3 El Gran Torneo — Test E2E de Flujos Completos ★★★★

**Historia**: Erika quiere un test que simule exactamente lo que haría un entrenador real durante su estadía
completa: desde registrarse hasta dejar su reseña, pasando por añadir su Pokémon, reservar, pagar, hacer
check-in, unirse a un tour y hacer check-out. No basta con probar endpoints aislados; el flujo completo debe
pasar sin errores usando autenticación JWT real.

**Objetivo Técnico**: Implementar tests E2E que cubran flujos completos de negocio usando
`@SpringBootTest(webEnvironment = RANDOM_PORT)` con `TestRestTemplate`, autenticación JWT real y base de
datos real (H2 o Testcontainers según disponibilidad).

**Desarrollo Esperado:**

1. Flujo "El Entrenador Completo" (happy path end-to-end):
   - `POST /api/auth/register` → usuario creado
   - `POST /api/auth/verify?token=...` → email verificado
   - `POST /api/auth/login` → JWT obtenido
   - `POST /api/me/pokemon` → Pikachu añadido al equipo
   - `GET /api/hotels/{id}/rooms/search` → habitación disponible seleccionada
   - `POST /api/reservations` → reserva en PENDING
   - `POST /api/reservations/{id}/payment` → pago iniciado
   - `POST /api/payments/{id}/confirm` → reserva pasa a CONFIRMED
   - `PATCH /api/reservations/{id}/check-in` → CHECKED_IN
   - `POST /api/tours/schedules/{id}/book` → plaza en tour reservada
   - `PATCH /api/reservations/{id}/check-out` → CHECKED_OUT + PokéCoins acreditados
   - `POST /api/reservations/{id}/review` → reseña creada
   - `GET /api/hotels/{id}/reviews` → reseña visible públicamente
2. Flujo "El Team Rocket Fallido" (casos de error):
   - Intentar acceder a `GET /api/me` sin token → 401
   - TRAINER intenta ver reserva de otro TRAINER → 403
   - Crear reseña sin haber hecho checkout → 409 con estado actual de la reserva
3. Flujo "El GYM_LEADER Eficiente":
   - Login como GYM_LEADER
   - Crear tour con horario para mañana → 201
   - Cambiar estado de habitación a CLEANING → 200
   - `GET /api/admin/dashboard` → estadísticas del hotel

**Criterios de Aceptación:**

- [ ] El flujo del Entrenador Completo termina exitosamente: PokéCoins son los correctos al final (noches ×
      multiplicador de tipo de habitación)
- [ ] El JWT obtenido en login funciona para todas las peticiones subsecuentes sin re-autenticar
- [ ] Cada test empieza con BD limpia (usar `@Transactional` con rollback o `@Sql` para reset)
- [ ] El flujo del Team Rocket verifica exactamente el mensaje de error de cada respuesta 4xx
- [ ] Tiempo total del flujo del Entrenador Completo < 15 segundos incluyendo arranque del contexto

**Tiempo Estimado:** 8 horas | **Complejidad:** ★★★★

---

## 7. Fase 5 — Cinnabar: Estadísticas y Automatización

> Realiza estos ejercicios después de completar los Ejercicios 5.1 a 5.5 del Roadmap.
> Blaine ya instrumentó el sistema con logs y métricas. Ahora añade semántica de negocio.

---

### 7.1 El Panel del Profesor Oak — Dashboard de Estadísticas ★★★

**Historia**: Blaine entrega al Profesor Oak un endpoint de dashboard. Oak quiere ver en una sola llamada:
tasa de ocupación, ingresos de la semana, tours más populares, promedio de calificación y top de Pokémon
visitantes. Toda esta información debe ser eficiente: máximo 5 queries SQL, y el resultado debe cachearse
por 5 minutos.

**Objetivo Técnico**: Implementar un endpoint de estadísticas agregadas usando proyecciones JPA y queries
nativas. Cachear con TTL configurable. Verificar eficiencia con conteo de queries SQL.

**Desarrollo Esperado:**

1. DTO `HotelDashboardResponse` con secciones:
   - `occupancy`: `totalRooms`, `occupiedRooms`, `availableRooms`, `occupancyRate` (%)
   - `revenue`: `weeklyTotal`, `monthlyTotal`, `avgReservationValue` (todos como BigDecimal)
   - `popularTours`: List de `{tourName, bookingsThisWeek}` (top 5)
   - `pokemonStats`: `topSpecies` (top 3), `totalPokemonCurrentlyHosted`
   - `reviewStats`: `avgRating`, `totalReviews`, `recentReviews` (últimas 3, solo rating y snippet)
2. `DashboardService` con queries optimizadas:
   - Una query SQL para `occupancy` (GROUP BY status)
   - Una query para `revenue` con filtro de fechas (SUM y AVG)
   - Una query para `popularTours` con COUNT y GROUP BY esta semana
3. Cachear con `@Cacheable("hotel-dashboard")` y TTL de 5 minutos
4. Invalidar caché cuando se produce check-out (usando `@CacheEvict`)
5. Endpoint: `GET /api/admin/dashboard` (solo PROFESOR_OAK y GYM_LEADER del hotel)

**Criterios de Aceptación:**

- [ ] El endpoint ejecuta máximo 5 queries SQL (verificable con Hibernate SQL logging en nivel DEBUG)
- [ ] `occupancyRate` correcto: 3 habitaciones ocupadas de 10 = 30.0%
- [ ] Segunda llamada con caché activo responde en <10ms
- [ ] Después de un check-out, la siguiente llamada al dashboard actualiza `occupancy` (caché invalidado)
- [ ] Test de integración: insertar datos conocidos, llamar dashboard, verificar exactitud de cada sección

**Tiempo Estimado:** 7 horas | **Complejidad:** ★★★

---

### 7.2 El Radar del Hotel — Health Checks con Semántica de Negocio ★★

**Historia**: Blaine configura el monitoreo. `GET /actuator/health` solo dice "UP/DOWN" para la BD. Él
quiere health checks con semántica hotelera: ¿hay habitaciones disponibles? ¿el sistema de pagos responde?
¿hay tours programados para hoy? Un hotel sin habitaciones disponibles está "degradado" aunque la BD
funcione perfectamente.

**Objetivo Técnico**: Implementar `HealthIndicator` personalizados con semántica de negocio. Los detalles
numéricos solo son visibles para usuarios autenticados con rol administrativo.

**Desarrollo Esperado:**

1. `RoomAvailabilityHealthIndicator`:
   - UP: ≥20% habitaciones disponibles
   - DOWN: <5% disponibles
   - Incluir en detalles: `availableRooms`, `totalRooms`, `availabilityRate`
2. `TourScheduleHealthIndicator`:
   - UP: hay al menos un `TourSchedule` con `status=OPEN` en las próximas 24 horas
   - UNKNOWN: no hay tours programados para hoy (estado informativo, no crítico)
3. Configurar exposición en `application.yml`:
   - `management.endpoint.health.show-details: when-authorized`
   - Solo usuarios con rol `PROFESOR_OAK` o `GYM_LEADER` ven los detalles numéricos
4. Agregar ambos indicadores al health check principal (aparecen bajo sus nombres de bean)

**Criterios de Aceptación:**

- [ ] `GET /actuator/health` sin autenticar muestra el status global pero sin detalles
- [ ] `GET /actuator/health` con token de PROFESOR_OAK muestra los 2 indicadores con sus métricas
- [ ] Con 0 habitaciones disponibles: `roomAvailability` muestra status DOWN con `availableRooms: 0`
- [ ] Si un indicador lanza excepción inesperada, el health global pasa a DOWN pero el otro indicador sigue
      mostrándose correctamente
- [ ] Test unitario: mockear el repositorio devolviendo 0 habitaciones → verificar status DOWN

**Tiempo Estimado:** 4 horas | **Complejidad:** ★★

---

### 7.3 El Sistema de Notificaciones del Hotel — Desacoplamiento con Eventos ★★★

**Historia**: Blaine nota que `checkout()` en `ReservationService` llama directamente a 3 servicios
distintos: `PokeCoinService.award()`, `ReviewReminderService.schedule()` y `TourService.releaseSlotsForUser()`.
Cuando se añade un cuarto efecto (enviar webhook), hay que modificar `ReservationService` de nuevo. Blaine
pide desacoplamiento: el servicio de reservas no debe saber qué ocurre después del checkout.

**Objetivo Técnico**: Refactorizar el flujo de checkout para publicar un evento de dominio
`TrainerCheckedOutEvent` con `ApplicationEventPublisher`. Los efectos secundarios se implementan como
listeners independientes que no se conocen entre sí.

**Desarrollo Esperado:**

1. Data class `TrainerCheckedOutEvent(reservationId: Long, userId: Long, roomType: RoomType, nights: Int,
   checkOutDate: LocalDate)`
2. En `ReservationService.checkOut()`: publicar el evento en lugar de llamar directamente a los servicios
3. Tres listeners independientes, cada uno en su propio `@Component`:
   - `PokeCoinCheckoutListener`: llama a `PokeCoinService.awardCoins()`
   - `ReviewReminderListener`: crea una `ReviewReminder` en BD (entidad nueva con `userId`, `reservationId`,
     `remindAt = checkOutDate + 1 día`, `sent = false`)
   - `TourSlotReleaseListener`: cancela `TourBookings` activos del usuario si ya hizo checkout
4. Todos los listeners usan `@TransactionalEventListener(phase = AFTER_COMMIT)` — si fallan, el checkout
   ya ocurrió y no hace rollback

**Criterios de Aceptación:**

- [ ] `ReservationService` no contiene ningún import de `PokeCoinService`, `ReviewReminderService` ni
      `TourService` (el desacoplamiento es real y verificable)
- [ ] Si `PokeCoinCheckoutListener` lanza excepción, la reserva sigue en CHECKED_OUT (AFTER_COMMIT lo
      garantiza)
- [ ] Test: publicar `TrainerCheckedOutEvent` directamente desde el test → verificar que los 3 listeners
      reaccionan sin necesidad de crear una reserva real
- [ ] `ReviewReminder` creada contiene el `reservationId` correcto para enviar el link exacto al entrenador
- [ ] Los logs de cada listener incluyen `reservationId` como campo estructurado para correlación

**Tiempo Estimado:** 5 horas | **Complejidad:** ★★★

---

## 8. Fase 6 — Viridian: Configuración Avanzada de Producción

> Realiza estos ejercicios después de completar los Ejercicios 6.1 a 6.3 del Roadmap.
> Giovanni ya containerizó y tiene CI/CD. Ahora refina la configuración para operar en producción real.

---

### 8.1 El Mapa de la Región — Configuración Multi-Entorno ★★

**Historia**: Giovanni prepara el Hotel Pokémon para operar en múltiples regiones: Kanto (producción),
Johto (staging) y Sinnoh (desarrollo). Actualmente todo está hardcodeado en `application.yml` y hay
`@Value("${...}")` dispersos por clases de servicio. Giovanni quiere configuración tipada, validada y
organizada por perfiles.

**Objetivo Técnico**: Estructurar la configuración usando `@ConfigurationProperties` tipadas por dominio,
perfiles de Spring por entorno, y eliminar todos los `@Value` de las clases de servicio.

**Desarrollo Esperado:**

1. Archivos de configuración por entorno:
   - `application.yml` — configuración base sin valores específicos de entorno
   - `application-dev.yml` — H2, logging DEBUG, Sentry desactivado
   - `application-staging.yml` — PostgreSQL staging, logging INFO, Sentry en modo staging
   - `application-prod.yml` — PostgreSQL prod, logging WARN, Redis habilitado, Sentry prod
2. `@ConfigurationProperties` por grupos lógicos (`@Validated` en cada una):
   - `HotelProperties`: `maxPokemonPerTrainer: Int`, `maxReservationDays: Int`,
     `defaultCurrency: String`
   - `SecurityProperties`: `jwtSecret: String`, `jwtExpirationHours: Long`,
     `refreshTokenDays: Long`
   - `CacheProperties`: `roomAvailabilityTtl: Duration`, `dashboardTtl: Duration`
3. Eliminar todos los `@Value("${...}")` de clases de servicio y reemplazar por inyección de
   `@ConfigurationProperties`
4. Actualizar `.env.example` documentando todas las variables de entorno requeridas por entorno

**Criterios de Aceptación:**

- [ ] `./gradlew bootRun --args='--spring.profiles.active=dev'` arranca sin PostgreSQL ni Redis
- [ ] `./gradlew bootRun --args='--spring.profiles.active=prod'` falla claramente si `DB_URL` no está
      definida (con mensaje de qué falta, no con NPE)
- [ ] No hay ningún `@Value` en clases de servicio (solo permitido en `@Configuration` si es estrictamente
      necesario)
- [ ] `HotelProperties` tiene validación: `@Min(1) @Max(6)` en `maxPokemonPerTrainer`
- [ ] Test: en perfil `dev`, verificar que `cacheProperties.dashboardTtl` tiene el valor configurado en
      `application-dev.yml`

**Tiempo Estimado:** 4 horas | **Complejidad:** ★★

---

### 8.2 La Muralla de Giovanni — Migraciones Zero-Downtime ★★★

**Historia**: Giovanni exige que cualquier migración de base de datos pueda ejecutarse con el servicio en
producción, sin tiempos de inactividad. El equipo planea renombrar la columna `hotels.city` a
`hotels.city_name`. En una tabla con miles de registros activos, un `ALTER TABLE` naive puede bloquear la
tabla durante minutos.

**Objetivo Técnico**: Implementar el patrón expand-contract para una migración zero-downtime. Documentar
qué operaciones de Flyway son seguras en PostgreSQL y cuáles requieren mantenimiento programado.

**Desarrollo Esperado:**

1. Implementar la migración de `city` → `city_name` en 3 migraciones Flyway separadas:
   - `V10__expand_add_city_name.sql`: añadir columna `city_name` nullable (operación segura)
   - `V11__migrate_city_data.sql`: copiar datos de `city` a `city_name` en batches de 1000 registros
     (sin bloquear la tabla)
   - `V12__contract_remove_city.sql`: eliminar columna `city` (solo ejecutar cuando el código no la use)
2. Implementar en `V11` la migración en batches con un `DO $$ BEGIN ... END $$` block:
   ```sql
   -- Migrar en lotes de 1000 para no bloquear la tabla
   UPDATE hotels SET city_name = city WHERE id IN (
       SELECT id FROM hotels WHERE city_name IS NULL LIMIT 1000
   );
   ```
3. Crear al menos un índice usando `CREATE INDEX CONCURRENTLY` para no bloquear inserts durante su creación
4. Añadir en los comentarios SQL por qué cada operación es segura (o cuándo no lo sería)

**Criterios de Aceptación:**

- [ ] Las 3 migraciones de Flyway se aplican en orden sin errores (`./gradlew flywayMigrate`)
- [ ] Los datos en `city` quedan correctamente copiados en `city_name` tras `V11`
- [ ] El índice creado con `CONCURRENTLY` está presente en la BD sin haber bloqueado inserts
- [ ] `./gradlew flywayInfo` muestra las 3 migraciones en estado "Success"
- [ ] Cada migración tiene comentario SQL explicando la estrategia y por qué es segura

**Tiempo Estimado:** 5 horas | **Complejidad:** ★★★

---

### 8.3 La Inspección de la Liga — Hardening de Seguridad en Producción ★★★

**Historia**: Giovanni prepara el Hotel Pokémon para una auditoría de seguridad de la Liga. El auditor
buscará headers faltantes, dependencias con CVEs conocidos, endpoints que filtren stacktraces en producción,
y la imagen Docker que corra como root. Giovanni quiere cero hallazgos críticos.

**Objetivo Técnico**: Implementar una checklist de hardening de seguridad: security headers HTTP,
sanitización de respuestas de error en producción, escaneo de dependencias con OWASP, y configuración
segura del contenedor Docker.

**Desarrollo Esperado:**

1. Security headers en todas las respuestas (en `SecurityFilterChain`):
   - `Strict-Transport-Security: max-age=31536000; includeSubDomains`
   - `X-Content-Type-Options: nosniff`
   - `X-Frame-Options: DENY`
   - `Content-Security-Policy: default-src 'self'`
   - `Referrer-Policy: no-referrer`
2. Sanitización de respuestas de error por perfil:
   - En `dev`: stacktraces incluidos (útil para desarrollo)
   - En `prod`: errores 5xx devuelven solo `{"title": "Internal Server Error", "status": 500}` sin traza
   - Los errores de BD (constraint violations) nunca revelan nombres de tabla ni columnas internas
3. Escaneo de dependencias:
   - Configurar OWASP Dependency Check: `./gradlew dependencyCheckAnalyze`
   - Generar reporte HTML en `build/reports/`
   - El build falla si hay vulnerabilidades con CVSS ≥ 7.0
4. Seguridad del contenedor Docker:
   - Crear usuario no-root en el Dockerfile: `RUN adduser --disabled-password appuser && USER appuser`
   - Añadir flags JVM de producción: `-XX:+ExitOnOutOfMemoryError`, `-Djava.security.egd=file:/dev/./urandom`

**Criterios de Aceptación:**

- [ ] `curl -I http://localhost:8080/api/hotels` muestra todos los security headers definidos
- [ ] En perfil `prod`, error 500 devuelve solo `{"title":"Internal Server Error","status":500}` sin
      stacktrace ni nombres de clase
- [ ] Reporte OWASP disponible en `build/reports/dependency-check-report.html`
- [ ] 0 vulnerabilidades con CVSS ≥ 7.0 en dependencias directas del proyecto
- [ ] `docker inspect hotel-pokemon:latest` confirma que el proceso no corre como `root` (UID != 0)

**Tiempo Estimado:** 6 horas | **Complejidad:** ★★★

---

## 9. Módulos Integradores para la Liga Pokémon

Estos ejercicios integran múltiples módulos del sistema. Se realizan después de completar el roadmap
completo y los ejercicios complementarios anteriores. Son los desafíos finales antes de la Liga.

---

### 9.1 El Gran Torneo — Módulo de Gamificación ★★★★

**Historia**: El Profesor Oak quiere celebrar el primer aniversario del Hotel Pokémon con un gran torneo.
Los entrenadores con más estadías completas reciben insignias digitales y aparecen en el ranking mensual.
El sistema debe calcular un ranking semanal, otorgar badges automáticamente al alcanzar hitos, y notificar
internamente a los ganadores. No existe nada de esto en el sistema actual.

**Objetivo Técnico**: Implementar el módulo de gamificación desde cero integrando: reservas (para contar
estadías), PokéCoins (para el ranking), reviews (para el score de reputación), tareas programadas (para el
cálculo semanal), y el sistema de eventos (para otorgar badges al hacer checkout).

**Desarrollo Esperado:**

1. Entidad `Badge`:
   - `name`: "Entrenador Regular", "Turista de Temporada", "Maestro del Hotel"
   - `description`, `criteria`, `iconUrl`
   - Relación Many-to-Many con `User` via tabla `user_badges` con `awardedAt: LocalDateTime`
   - Constraint UNIQUE en `(userId, badgeId)` — no se puede otorgar el mismo badge dos veces
2. Entidad `Notification`:
   - `userId`, `title`, `message`, `read: Boolean`, `createdAt`
   - Relación Many-to-One con `User`
3. `GamificationService` con reglas:
   - Al llegar a 5 estadías CHECKED_OUT: otorgar "Entrenador Regular" + notificación
   - Al llegar a 10 estadías: otorgar "Turista de Temporada"
   - Primer puesto del ranking mensual: otorgar "Maestro del Hotel"
4. Listener `GamificationCheckoutListener` (escucha `TrainerCheckedOutEvent`) que verifica y otorga badges
5. `RankingService` con tarea `@Scheduled(cron = "0 0 0 * * MON")`:
   - Calcula top 10 entrenadores: (noches totales del mes × 1) + (PokéCoins × 0.5) + (avgReviews × 10)
   - Guarda el ranking en tabla `weekly_ranking`
6. Endpoints:
   - `GET /api/me/badges` — mis insignias con fecha de obtención
   - `GET /api/me/notifications` — mis notificaciones (paginado, no leídas primero)
   - `POST /api/me/notifications/{id}/read` — marcar como leída
   - `GET /api/admin/ranking` — top 10 con puntuación desglosada (solo OAK)

**Criterios de Aceptación:**

- [ ] Al hacer checkout de la 5ª estadía, se crea la `Notification` y se otorga el badge en la misma
      transacción
- [ ] El ranking se recalcula cada lunes a las 00:00 automáticamente
- [ ] Intentar otorgar badge duplicado → silencioso (no excepción), simplemente no crea registro
- [ ] `GET /api/admin/ranking` muestra la puntuación desglosada por componente (noches, coins, reviews)
- [ ] Test E2E: simular 5 checkouts para un usuario → verificar badge "Entrenador Regular" y notificación
      en BD

**Tiempo Estimado:** 12 horas | **Complejidad:** ★★★★

---

### 9.2 La Opinión Moderada — Sistema de Reseñas con Moderación ★★★★

**Historia**: El módulo de Reviews del ejercicio 5.3 funciona, pero las reseñas se publican sin revisión.
Un entrenador molesto dejó texto inapropiado y estuvo visible horas antes de ser detectado. La Liga Pokémon
exige que las reseñas pasen por un flujo de moderación antes de ser públicas. Además, los GYM_LEADER pueden
responder oficialmente a las reseñas publicadas.

**Objetivo Técnico**: Extender el módulo `Review` con un flujo de moderación (`PENDING_MODERATION` →
`PUBLISHED` / `REJECTED`), respuestas oficiales del hotel, y un análisis de sentimiento basado en palabras
clave (sin ML, usando un diccionario curado).

**Desarrollo Esperado:**

1. Añadir campo `status` a `Review`: Enum `PENDING_MODERATION`, `PUBLISHED`, `REJECTED`
   - Las reseñas nuevas empiezan en `PENDING_MODERATION`
   - Solo las `PUBLISHED` son visibles en `GET /api/hotels/{id}/reviews`
2. Entidad `ReviewResponse` (respuesta oficial):
   - Relación One-to-One con `Review`
   - `respondedBy`: Long (userId del GYM_LEADER que responde)
   - `responseText`: String (20-1000 chars)
   - `respondedAt`: LocalDateTime
3. Campo `sentimentScore: Double` calculado al crear la reseña:
   - Lista curada de 50 palabras positivas en español: "excelente", "increíble", "limpio",
     "amable", etc.
   - Lista curada de 50 palabras negativas: "terrible", "sucio", "lento", "horrible", etc.
   - Score = (positivas - negativas) / totalPalabras → normalizado a [-1.0, 1.0]
   - Enum `Sentiment`: POSITIVE (>0.15), NEUTRAL (-0.15 to 0.15), NEGATIVE (<-0.15)
4. Endpoints adicionales:
   - `GET /api/admin/reviews/pending` — cola de moderación (GYM_LEADER y OAK)
   - `PUT /api/admin/reviews/{id}/approve` — aprobar reseña
   - `PUT /api/admin/reviews/{id}/reject` — rechazar con `rejectionReason: String`
   - `POST /api/hotels/{id}/reviews/{reviewId}/response` — respuesta oficial (GYM_LEADER del hotel)

**Criterios de Aceptación:**

- [ ] Reseña recién creada no aparece en `GET /api/hotels/{id}/reviews` hasta ser `PUBLISHED`
- [ ] Solo una respuesta oficial por reseña (constraint UNIQUE en `review_id` de `review_responses`)
- [ ] "Excelente servicio, habitación increíble y muy limpia" → sentimentScore positivo (>0.15)
- [ ] "Terrible experiencia, habitación sucia, staff horrible" → sentimentScore negativo (<-0.15)
- [ ] Test: crear reseña → verificar estado `PENDING_MODERATION` → aprobar → verificar `PUBLISHED` y
      visible en listado público
- [ ] Rechazar reseña → el autor sigue viendo su reseña pero con estado `REJECTED`, no desaparece de
      `GET /api/me/reviews`

**Tiempo Estimado:** 10 horas | **Complejidad:** ★★★★

---

## 10. Tabla Resumen

| #   | Ejercicio                               | Fase          | Módulo          | Complejidad | Horas     |
|-----|-----------------------------------------|---------------|-----------------|-------------|-----------|
| 3.1 | Módulo de Amenidades                    | 1 — Pewter    | Amenity         | ★★          | 5h        |
| 3.2 | Módulo de Tours Pokémon                 | 1 — Pewter    | Tour            | ★★★         | 8h        |
| 3.3 | Perfil y Equipo Pokémon                 | 1 — Pewter    | Pokemon         | ★★          | 5h        |
| 3.4 | Módulo de Pagos                         | 1 — Pewter    | Payment         | ★★★         | 7h        |
| 4.1 | Registro y Perfil del Entrenador        | 2 — Vermilion | Auth/User       | ★★          | 5h        |
| 4.2 | Seguridad en el Módulo Tours            | 2 — Vermilion | Tour/Security   | ★★          | 4h        |
| 5.1 | Validaciones del Equipo Pokémon         | 3 — Cerulean  | Pokemon         | ★★          | 5h        |
| 5.2 | Validaciones del Módulo Tours           | 3 — Cerulean  | Tour            | ★★★         | 5h        |
| 5.3 | Módulo de Reseñas                       | 3 — Cerulean  | Review          | ★★★         | 6h        |
| 6.1 | WebMvcTest Exhaustivo                   | 4 — Celadon   | Testing         | ★★          | 6h        |
| 6.2 | DataJpaTest en Queries Críticas         | 4 — Celadon   | Testing         | ★★★         | 6h        |
| 6.3 | Test E2E de Flujos Completos            | 4 — Celadon   | Testing         | ★★★★        | 8h        |
| 7.1 | Dashboard de Estadísticas               | 5 — Cinnabar  | Admin           | ★★★         | 7h        |
| 7.2 | Health Checks con Semántica de Negocio  | 5 — Cinnabar  | Actuator        | ★★          | 4h        |
| 7.3 | Desacoplamiento con Eventos en Checkout | 5 — Cinnabar  | Events          | ★★★         | 5h        |
| 8.1 | Configuración Multi-Entorno             | 6 — Viridian  | Config          | ★★          | 4h        |
| 8.2 | Migraciones Zero-Downtime               | 6 — Viridian  | Flyway/DB       | ★★★         | 5h        |
| 8.3 | Hardening de Seguridad en Producción    | 6 — Viridian  | Security/Docker | ★★★         | 6h        |
| 9.1 | Módulo de Gamificación                  | Liga Pokémon  | Gamification    | ★★★★        | 12h       |
| 9.2 | Sistema de Reseñas con Moderación       | Liga Pokémon  | Review          | ★★★★        | 10h       |
|     | **TOTAL**                               |               |                 |             | **~123h** |

**Producto al completar todos los ejercicios:** Un sistema de gestión hotelera completo con todos los
módulos del Hotel Pokémon implementados, testeados y listos para producción — no solo las reservas y
habitaciones del roadmap, sino el ecosistema completo: amenidades, tours, Pokémon, pagos, reseñas,
gamificación y observabilidad con semántica de negocio.
