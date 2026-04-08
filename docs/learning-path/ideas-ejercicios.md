# Sistema Pokémon Hotelero - Guía de Ejercicios

## Índice

1. [Visión General del Sistema](#1-visión-general-del-sistema)
2. [Entidades y Relaciones](#2-entidades-y-relaciones)
3. [Nivel 1: Fundamentos (CRUD Básico)](#3-nivel-1-fundamentos-crud-básico)
4. [Nivel 2: Core de Negocio (Reservas y Usuarios)](#4-nivel-2-core-de-negocio-reservas-y-usuarios)
5. [Nivel 3: Funcionalidades Avanzadas (Pokémon, Tours y Pagos)](#5-nivel-3-funcionalidades-avanzadas-pokémon-tours-y-pagos)
6. [Nivel 4: Casos de Uso Integrados](#6-nivel-4-casos-de-uso-integrados)
7. [Roadmap de Implementación](#7-roadmap-de-implementación)
8. [Recursos y Referencias](#8-recursos-y-referencias)

---

## 1. Visión General del Sistema

### Objetivo

Crear un sistema integrado de **hotelería + Pokémon** usando **Spring Boot +
Kotlin** que permita:

- **Administradores de hoteles**: Gestionar hoteles, habitaciones, amenities,
  reservas y servicios.
- **Entrenadores Pokémon**: Buscar hoteles, realizar reservas, explorar tours,
  capturar Pokémon y dejar reseñas.

### Arquitectura de Alto Nivel

```
┌─────────────────────────────────────────────────────────┐
│                    API REST (Kotlin)                    │
├─────────────────────────────────────────────────────────┤
│  HotelModule │ ReservationModule │ PokemonModule      │
├─────────────────────────────────────────────────────────┤
│              Spring Data JPA (Hibernate)                │
├─────────────────────────────────────────────────────────┤
│     PostgreSQL / H2    │    JWT Security               │
└─────────────────────────────────────────────────────────┘
```

---

## 2. Entidades y Relaciones

### Diagrama Entidad-Relación

```

┌─────────┐ ┌─────────┐ ┌───────────┐ │ User │────<│ Trainer │────<│ Pokemon │ │
(auth) │ │ (role) │ │ (captura) │ └────┬────┘ └────┬────┘ └───────────┘ │ │ │
┌─────┴─────┐ │ │ │ ▼ ▼ ▼ ┌─────────┐ ┌──────────┐ ┌───────────┐ │ Payment │
│Reservation│ │TrainingSession └────┬────┘ └────┬─────┘ └───────────┘ │ │ │ ▼ │
┌───────────┐ ┌─────────┐ └─────>│ Room │────>│ Hotel │ └───────────┘
└────┬────┘ │ ┌──────────────┼──────────────┐ ▼ ▼ ▼ ┌─────────┐ ┌─────────┐
┌─────────┐ │ Review │ │ Service │ │ Amenity │ └─────────┘ └─────────┘
└─────────┘

```

### Diccionario de Entidades

| Entidad       | Atributos Clave                                                     | Descripción                               |
| ------------- | ------------------------------------------------------------------- | ----------------------------------------- |
| `User`        | `id`, `email`, `password`, `role` (ADMIN/TRAINER)                   | Usuario autenticable del sistema          |
| `Hotel`       | `id`, `name`, `address`, `city`, `category`                         | Establecimiento hotelero                  |
| `Room`        | `id`, `number`, `type`, `price`, `available`                        | Habitación dentro de un hotel             |
| `Amenity`     | `id`, `name`, `description`                                         | Servicios del hotel (WiFi, piscina, etc.) |
| `Reservation` | `id`, `checkIn`, `checkOut`, `status` (PENDING/CONFIRMED/CANCELLED) | Reserva de habitación                     |
| `Payment`     | `id`, `amount`, `method`, `status`                                  | Transacción de pago                       |
| `Pokemon`     | `id`, `name`, `type`, `level`, `rarity`                             | Pokémon descubrible en tours              |
| `Trainer`     | `id`, `userId`, `experience`, `capturedPokemon`[]                   | Perfil de entrenador                      |
| `Review`      | `id`, `rating` (1-5), `comment`, `date`                             | Reseña de hotel                           |
| `Tour`        | `id`, `name`, `location`, `price`, `duration`                       | Servicio de exploración                   |

---

## 3. Nivel 1: Fundamentos (CRUD Básico)

### Ejercicio 1.1: Mapeo JPA de Entidades Core

**Complejidad**: ⭐⭐ (Básico)

Implementa las entidades base con sus relaciones JPA:

```kotlin
@Entity
@Table(name = "hotels")
data class Hotel(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    val name: String,

    @Column(nullable = false)
    val city: String,

    @Column(nullable = false)
    val address: String,

    @Enumerated(EnumType.STRING)
    val category: HotelCategory = HotelCategory.STANDARD
) {
    enum class HotelCategory { ECONOMIC, STANDARD, LUXURY }
}

@Entity
@Table(name = "rooms")
data class Room(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    val number: String,

    @Column(nullable = false)
    val pricePerNight: BigDecimal,

    @Column(nullable = false)
    val capacity: Int,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hotel_id")
    val hotel: Hotel,

    @OneToMany(mappedBy = "room", cascade = [CascadeType.ALL])
    val reservations: MutableList<Reservation> = mutableListOf()
)
```

**Criterios de Aceptación**:

- [ ] Todas las entidades tienen `@Entity` y `@Table`
- [ ] IDs son auto-generados
- [ ] Relaciones `@ManyToOne` y `@OneToMany` están correctamente mapeadas
- [ ] Campos obligatorios tienen `@Column(nullable = false)`

---

### Ejercicio 1.2: Repositorios Spring Data

**Complejidad**: ⭐⭐ (Básico)

Crea repositorios con consultas derivadas y personalizadas:

````kotlin
@Repository
interface HotelRepository : JpaRepository<Hotel, Long> {
    // Consulta derivada del nombre
    fun findByCityIgnoreOrderByName(city: String): List<Hotel>

    // Consulta con JPQL
    @Query("SELECT h FROM Hotel h WHERE h.category = :category AND SIZE(h.rooms) >= :minRooms")
    fun findByCategoryWithMinRooms(
        @Param("category") category: HotelCategory,
        @Param("minRooms") minRooms: Int
    ): List<Hotel>

    // Consulta nativa para búsqueda compleja
    @Query(
        value = """
            SELECT h.* FROM hotels h
            JOIN rooms r ON h.id = r.hotel_id
            WHERE r.price_per_night BETWEEN :minPrice AND :maxPrice
            AND r.available = true
            GROUP BY h.id
        """,
        nativeQuery = true
  - Crear una entidad `Payment` con información del pago realizado (fecha, hora,
    monto, método).
  - Implementar la lógica para registrar pagos.
  - **Casos de Uso:**
    1.  `POST /payments`: Crear un nuevo pago.
    2.  `GET /payments/:id`: Obtener detalles de un pago.

**4. Gestión del Hotel:**

- **Ejercicio 8: Agregar y Gestionar Hoteles:**
  - Crear una entidad `Hotel` con nombre, ubicación, tipo, precio.
  - Implementar la lógica para agregar, modificar y eliminar hoteles.
  - **Casos de Uso:**
    1.  `POST /hotels`: Agregar un nuevo hotel.
    2.  `GET /hotels/:id`: Obtener detalles de un hotel.
    3.  `PUT /hotels/:id`: Modificar datos de un hotel.

**5. Funcionalidades Avanzadas (Para futuras iteraciones):**

- **Ejercicio 9: Sistema de Recomendación Pokémon:**
  - Implementar un sistema simple para recomendar Pokémon a los usuarios en
    función de sus preferencias y las estadísticas de los Pokémon que ya han
    visitado el hotel. Esto se podría hacer con algoritmos básicos (por ejemplo,
    filtrado colaborativo).
- **Ejercicio 10: Integración con API Externas:**
  - Integrar con una API externa (por ejemplo, para obtener información sobre
    eventos deportivos o culturales) y usarla para personalizar la experiencia
    del usuario.

**Consideraciones Adicionales:**

- **Diseño de la Base de Datos:** Considera el uso de un ORM como Spring Data
  JPA para facilitar las operaciones de acceso a los datos.
- **Testing:** Escribe pruebas unitarias, de integración y de unidad para
  asegurar que tu sistema funcione correctamente.
- **Seguridad:** Implementa medidas de seguridad para proteger tu sistema contra
  ataques (por ejemplo, validación de entradas, protección contra inyección
  SQL).

**Herramientas Recomendadas:**

- **Spring Boot:** Framework para construir aplicaciones Spring.
- **Kotlin:** Lenguaje de programación moderno para Kotlin.
- **Spring Data JPA:** Framework para acceder a la base de datos (dependiendo
  del tipo de base de datos que uses).
- **Postman/Insomnia:** Herramientas para probar tu API.

---

¡Excelente idea! Tu proyecto combina **hotelería** con el universo **Pokémon**,
creando una experiencia única donde los _trainers_ (entrenadores) puedan
explorar, reservar alojamientos y descubrir nuevos Pokémon en la naturaleza.
Aquí te propongo un **plan estructurado** para desarrollar tu sistema en
**Spring Boot + Kotlin**, incluyendo:

- **Entidades clave** (mapping con JPA/Hibernate).
- **Casos de uso** por módulo.
- **Ejercicios prácticos** para implementar cada funcionalidad.

---

## **1. Entidades y Relaciones del Sistema**

Primero, definamos las entidades principales y sus relaciones:

### **Entidades Básicas (Hotelería)**

| Entidad       | Atributos clave                                                                   |
| ------------- | --------------------------------------------------------------------------------- |
| `User`        | `id`, `nombre`, `email`, `rol` (`ADMIN`/`TRAINER`), `passwordHash`                |
| `Hotel`       | `id`, `nombre`, `dirección`, `categoría` (Lujoso, Económico, etc.), `precioNoche` |
| `Room`        | `id`, `numero`, `tipo` (`Doble`, `Triple`), `hotelId`, `disponible`               |
| `Amenity`     | `id`, `nombre` (WiFi, Sauna, etc.), `hotelId`                                     |
| `Reservation` | `id`, `userId`, `hotelId`, `roomId`, `fechaInicio`, `fechaFin`, `totalPago`       |

### **Entidades Pokémon**

| Entidad   | Atributos clave                                                             |
| --------- | --------------------------------------------------------------------------- |
| `Pokemon` | `id`, `nombre`, `tipo`, `nivel`, `habilidad` (ej: "Rascal", "Fuego/Normal") |
| `Trainer` | Extiende `User`: `pokemonDomesticados` (lista), `experienciaTotal`          |

### **Entidades Adicionales**

| Entidad   | Atributos clave                                                          |
| --------- | ------------------------------------------------------------------------ |
| `Payment` | `id`, `userId`, `reservationId`, `monto`, `metodoPago` (Tarjeta, PayPal) |
| `Review`  | `id`, `hotelId`, `userId`, `calificacion` (1-5), `comentario`, `fecha`   |
| `Tour`    | `id`, `hotelId`, `nombre`, `descripcion`, `precio`, `duraciónMinutos`    |

---

## **2. Casos de Uso por Módulo**

### **Módulo 1: Gestión de Hoteles (Admin)**

#### **Caso de Uso 1: Crear/Editar Hotel**

- **Descripción**: Un admin puede registrar un nuevo hotel con su dirección,
  categoría y precios.
- **Ejercicio**:
  - Implementar el método `HotelService`:
    ```kotlin
    @Transactional
    fun crearHotel(nombre: String, direccion: String, categoria: Categoria, precioNoche: Double): Hotel
    ```
  - Validar que el hotel no exista y guardar en la base de datos.
- **Relación con otras entidades**: Asociar `amenities` (ej: WiFi, piscina) al
  hotel.

#### **Caso de Uso 2: Reservar una Habitación**

- **Descripción**: Un entrenador reserva una habitación para dormir.
- **Ejercicio**:
  - Crear un servicio que:
    1. Verifica disponibilidad de la habitación.
    2. Genera una `Reservation` y un `Payment`.
    3. Marca la habitación como ocupada.
  ```kotlin
  @Transactional
  fun reservarHabitación(
      userId: Long,
      hotelId: Long,
      roomId: Long,
      fechaInicio: LocalDate,
      fechaFin: LocalDate
  ): Reservation
````

---

### **Módulo 2: Gestión de Pokémon (Trainer)**

#### **Caso de Uso 3: Domesticar un Pokémon**

- **Descripción**: Un entrenador captura un Pokémon en la naturaleza y lo
  "domestica" para usarlo como compañero.
- **Ejercicio**:
  - Modificar el `User` (extendido a `Trainer`) con una lista de Pokémon
    domesticados:
    ```kotlin
    class Trainer @JpaEntity data class(
        id: Long,
        user: User,
        pokemonDomesticados: MutableList<Pokemon> = mutableListOf()
    )
    ```
  - Implementar un método para añadir un Pokémon a la lista del entrenador.

#### **Caso de Uso 4: Explorar y Detectar Pokémon**

- **Descripción**: El entrenador visita un hotel/tour para encontrar nuevos
  Pokémon en la zona.
- **Ejercicio**:
  - Crear una relación entre `Hotel`/`Tour` y `Pokemon` (ej: "En este hotel hay
    Pokémon de tipo Agua").
  - Implementar un servicio que:
    - Muestra los Pokémon disponibles en un lugar específico.
    - Permite al entrenador "capturar" uno (si no está domesticado).

---

### **Módulo 3: Reservas y Pagos**

#### **Caso de Uso 5: Realizar Pago**

- **Descripción**: El entrenador paga por su reserva usando tarjeta o PayPal.
- **Ejercicio**:
  - Usar Spring Security para autenticar el usuario.
  - Implementar un servicio que:
    ```kotlin
    @Transactional
    fun pagarReserva(
        reservationId: Long,
        metodoPago: String, // "tarjeta", "paypal"
        monto: Double
    ): Payment
    ```
  - Validar que el pago sea exitoso antes de confirmar la reserva.

#### **Caso de Uso 6: Cancelar Reserva**

- **Descripción**: El entrenador puede cancelar su reserva si no llega a tiempo.
- **Ejercicio**:
  ```kotlin
  @Transactional
  fun cancelarReserva(reservationId: Long): Boolean {
      val reservation = repository.findById(reservationId).orElseThrow()
      // Marcar como cancelada y devolver el dinero (si aplica)
      return true
  }
  ```

---

### **Módulo 4: Tours y Reviews**

#### **Caso de Uso 7: Crear un Tour Pokémon**

- **Descripción**: Un hotel puede ofrecer tours para explorar Pokémon en la
  zona.
- **Ejercicio**:
  - Crear una entidad `Tour` con relación a `Hotel`.

  ```kotlin
  @Entity
  data class Tour(
      @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
      val id: Long,
      val hotelId: Long,
      val nombre: String,
      val descripcion: String,
      val precio: Double,
      val duracionMinutos: Int
  )
  ```

  - Implementar un servicio para gestionar tours.

#### **Caso de Uso 8: Dar una Review al Hotel**

- **Descripción**: El entrenador deja una calificación y comentario después de
  su estancia.
- **Ejercicio**:

  ```kotlin
  @Transactional
  fun dejarReview(
      hotelId: Long,
      userId: Long,
      calificacion: Int, // 1-5
      comentario: String
  ): Review
  ```

  - Mostrar las reviews en el dashboard del hotel.

---

## **3. Ejercicios Prácticos para Implementar**

### **Ejercicio 1: Entidades y Mappings (JPA/Hibernate)**

- Crea los entornos `User`, `Hotel`, `Room`, etc., con relaciones adecuadas.
- Usa `@ManyToOne` o `@OneToMany` según las necesidades.
- Ejemplo para `Reservation`:
  ```kotlin
  @Entity
  data class Reservation(
      @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
      val id: Long,
      user: User,
      hotel: Hotel,
      room: Room,
      fechaInicio: LocalDate,
      fechaFin: LocalDate,
      totalPago: Double
  )
  ```

### **Ejercicio 2: Service Layer**

- Implementa un servicio para cada caso de uso (ej: `HotelService`,
  `PokemonService`).
- Usa `@Transactional` para manejar operaciones que requieran consistencia.
- Ejemplo:
  ```kotlin
  interface HotelService {
      fun crearHotel(nombre: String, direccion: String): Hotel
      fun listarHoteles(): List<Hotel>
  }
  ```

### **Ejercicio 3: Control de Flujos (Flows)**

- Usa `Flow` para mostrar datos dinámicamente (ej: reservas del entrenador).
- Ejemplo:
  ```kotlin
  fun obtenerReservasDelTrainer(userId: Long): Flow<List<Reservation>> = flow {
      val trainer = repository.findById(userId).orElseThrow()
      emit(repository.findAllByUserId(trainer.id))
  }
  ```

### **Ejercicio 4: Integración con Base de Datos**

- Usa Spring Data JPA para manejar consultas (ej: `HotelRepository`).
- Ejemplo:
  ```kotlin
  interface HotelRepository : JpaRepository<Hotel, Long> {
      fun findByNombre(nombre: String): Optional<Hotel>
  }
  ```

### **Ejercicio 5: Autenticación y Autorización**

- Usa Spring Security para manejar roles (`ADMIN`/`TRAINER`).
- Ejemplo de configuración:
  ```kotlin
  @Configuration
  class SecurityConfig : WebMvcConfigurer {
      @Bean
      fun securityFilterChain(http: HttpSecurity): SecurityFilterChain = http
          .authorizeHttpRequests { auth ->
              auth.anyRequest().authenticated()
          }
          .formLogin { form ->
              form.loginPage("/login")
          }
          .build()
  }
  ```

---

## **4. Ejemplo de Flujo Completo**

**Caso: Un entrenador reserva un hotel y domesta un Pokémon.**

1. **El entrenador se registra** (si no existe) como `Trainer`.
2. **Selecciona un hotel** y una habitación disponible.
3. **Reserva la habitación** (crea `Reservation` y `Payment`).
4. **Explora el lugar** para encontrar un Pokémon (ej: "En este hotel hay un
   Pikachu").
5. **Domestica el Pokémon** añadiéndolo a su lista de compañeros.

---

## **5. Recomendaciones Adicionales**

- **Validaciones**: Usa `@Valid` en los DTOs para validar datos (ej: fechas
  válidas).
- **Excepciones**: Crea una capa de excepciones personalizadas (ej:
  `HotelNotFoundException`).
- **Testing**: Implementa pruebas unitarias con Mockito y Spring Boot Test.
- **Documentación**: Usa Swagger/OpenAPI para documentar la API.

---

### **Próximos Pasos**

1. Empieza por implementar las entidades y sus relaciones en JPA.
2. Crea los servicios básicos (ej: `HotelService`).
3. Prueba con casos de uso simples antes de complicarlos.

---

**Ejercicio 1: Gestión de Hoteles (Administrador de Hoteles)**  
_Objetivo:_ Crear un hotel con habitaciones y servicios (amenidades).

**Casos de Uso:**

- **Caso De Uso 1.1:** Creación de un nuevo hotel.
  - _Entradas:_ DTO `CreateHotelRequest` con nombre, ubicación y lista de
    habitaciones/servicios.
  - _Salida Esperada:_ Hotel creado en la base de datos con detalles
    proporcionados.

- **Caso De Uso 1.2:** Actualización de un hotel existente.
  - _Entradas:_ DTO `UpdateHotelRequest` con identificador del hotel y cambios
    deseados.
  - _Salida Esperada:_ Hotel actualizado correctamente sin pérdida de datos
    existentes (si es necesario).

**Ejercicio 2: Registro de una Reserva (Reservaciones)**  
_Objetivo:_ Alinear a un entrenador una habitación disponible en un hotel
mediante reserva.

**Casos de Uso:**

- **Caso De Uso 2.1:** Confirmar disponibilidad y crear una reserva.
  - _Entradas:_ DTO `CreateReservationRequest` con detalles del huésped, hotel,
    habitación y pagos (si aplica).
  - _Salida Esperada:_ Registro de la reserva devuelto un identificador único y
    confirmación por correo/alerta.

- **Caso De Uso 2.2:** Cancelar una reserva y procesar reembolso o creditos para
  futuros alojamientos.
  - _Entradas:_ DTO `CancelReservationRequest` con el identificador de la
    reserva.
  - _Salida Esperada:_ Reserva borrada, habitación liberada y servicio de
    atención al cliente notificado.

**Ejercicio 3: Almacenamiento de Comentarios/Pósters (Revisión)**  
_Objetivo:_ Permitir que los entrenadores realicen comentarios sobre hoteles o
experiencias Pokémon después de una visita.

**Casos de Uso:**

- **Caso De Uso 3.1:** Submisión de un comentario/hora para un hotel.
  - _Entradas:_ DTO `CreateReviewRequest` con identificador del huésped, hotel y
    texto/ratings.
  - _Salida Esperada:_ Comentario guardado en la base de datos y retratable por
    otros usuarios.

- **Caso De Uso 3.2:** Calcular el promedio de calificaciones de un hotel basado
  en comentarios Pokémon o de servicios.
  - _Entradas:_ Identificador del hotel deseado.
  - _Salida Esperada:_ Resultado calculado y devuelto a través de una API
    `@GetMapping`.

**Ejercicio 4: Interacción con Pokémon (Entrenador)**  
_Objetivo:_ Simular la captura o "adicción" de un Pokémon en un entorno
específico (por ejemplo, durante un tour hotel/aire libre).

**Casos de Uso:**

- **Caso De Uso 4.1:** Intentar capturar un Pokémon al utilizar una Amenidad
  determinada.
  - _Entradas:_ Identificador del entrenador y detalles del escenario (hotel,
    tour, ubicación de la Amenidad).
  - _Salida Esperada:_ Registro de éxito en el sistema con mensaje de “¡Pokémon
    capturado!” para el entrenador.

- **Caso De Uso 4.2:** Alinear un Pokémon a los entrenadores que lo han
  encontrado por primera vez mediante una operación virtual (por ejemplo,
  interacción digital).
  - _Entradas:_ Identificador del trainer y detalles del Pokémon hallado.
  - _Salida Esperada:_ Pokémon añadido al perfil del entrenador con atributos de
    dificultad y disponibilidad para otros usuarios.

**Ejercicio 5: Gestión de Pago (Pago)**  
_Objetivo:_ Procesar transacciones financieras durante reservas o alquiler de
servicios adicionales.

**Casos de Uso:**

- **Caso De Uso 5.1:** Procesar un pago por una reserva de hotel incluyendo
  habitaciones y actividades extra.
  - _Entradas:_ DTO `CreatePaymentRequest` con detalles de la cuenta, monto y
    referencia (si aplica).
  - _Salida Esperada:_ Pago validado y credited al equilibrio de la cuenta del
    huésped o servicio prestado en la reserva.

- **Caso De Uso 5.2:** Manejar una devolución parcial debido a cancelación
  tardía de un tour Pokémon.
  - _Entradas:_ Identificador de la reserva y solicitud de refundo por parte del
    usuario.
  - _Salida Esperada:_ Pago revertido, habitación liberada y mensaje de servicio
    al cliente.

**Consideraciones Técnicas:**

- **Entidades en Kotlin con Spring Data JPA:** Utiliza `@Entity`, `@Repository`
  para modelos Hotel, Room, Amenity, Reservation, Trainer, Pokémon etc., con
  relaciones muchas-contadas (ej., hotel-a-habitación, reserva-alocación).
- **Servicios y Controladores:** Implementa métodos de servicio que manejen
  Lógica Estructural (por ejemplo, verificar disponibilidad de habitaciones
  antes de crear una reserva).
- **Casos de Error:** Maneja errores comunes como reservas duplicadas, pagos
  rechazados o intentos repetidos de captura del mismo Pokémon.

**Ejemplo de Código para Ejercicio 1 (Crear Hotel):**

```kotlin
package com.example.pokemon.hotel

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface HotelRepository : JpaRepository<Hotel, String> // Usamos el nombre del hotel como clave primaria.

@Entity
data class Hotel(
    @Id
    val name: String,
    val location: String,
    @Embedded
    val rooms: List<Room>,
    @Embedded
    val amenities: List<String>
)

@Controller
@PostMapping("/hotels")
@Operation(description = "Crear un nuevo hotel con habitaciones y servicios.")
fun createHotel(@RequestBody createHotelRequest: CreateHotelRequest): ResponseEntity<Hotel> {
    // Lógica de servicio para crear el Hotel.
}
```

**Conclusión:**  
Estos ejercicios cubren desde la base de datos/mantenimiento hasta las
interacciones avanzadas con Pokémon, integrando temas de hotel/servicios y
negocios relacionados. La complejidad adicional se incrementa mediante manejo de
errores, transacciones y autenticación basada en roles (administradores vs.
entrenadores).

---

¡Excelente idea! Un sistema Pokémon en Spring Boot con Kotlin e integración
hotelera tiene mucho potencial. Aquí te presento una propuesta detallada de
ejercicios y casos de uso, estructurados para cubrir la temática y los roles que
mencionaste (administradores de hoteles y entrenadores).

**Estructura General del Proyecto:**

- **Modelo de Datos (Entidades):**
  - `Hotel`: Información general del hotel (nombre, ubicación, tipo, etc.).
  - `Room`: Habitación específica dentro de un hotel (tipo, capacidad, precio).
  - `Amenities`: Comodidades que ofrece el hotel (piscina, gimnasio, wifi,
    etc.).
  - `Reservation`: Reserva realizada por un entrenador.
  - `Trainer`: Entrenador Pokémon (nombre, información personal, lista de
    Pokémon).
  - `Pokemon`: Información del Pokémon (nombre, tipo, nivel, habilidad).
  - `User`: Usuario del sistema (administrador de hotel o entrenador).
  - `Payment`: Registro de pagos realizados.
  - `Review`: Reseña escrita por un entrenador sobre un hotel.
  - `Service`: Servicios ofrecidos por el hotel (tour de Pokémon, spa, etc.).

- **Casos de Uso Principales:**
  1.  **Administración del Hotel:**
      - Crear/Editar/Eliminar Hoteles.
      - Crear/Editar/Eliminar Habitaciones.
      - Agregar/Eliminar Amenities a un hotel.
      - Gestionar Reservas (ver, modificar, cancelar).
      - Gestionar Servicios adicionales del Hotel

  2.  **Entrenador Pokémon:**
      - Registrarse/Iniciar Sesión.
      - Crear/Editar su perfil de Entrenador.
      - Agregar Pokémon a su equipo.
      - Buscar Hoteles.
      - Ver Detalles del Hotel (habitaciones disponibles, precios, amenities).
      - Realizar Reservas.
      - Gestionar sus Reservas (ver, cancelar).
      - Hacer Tours de Pokémon (descubrir nuevos Pokémon).
      - Escribir Reviews sobre Hoteles.

**Ejercicios y Casos de Uso Detallados:**

Voy a dividir los ejercicios por funcionalidad y proporcionar detalles sobre la
implementación en Spring Boot con Kotlin. Incluiré ejemplos de APIs REST.

**1. Administración del Hotel:**

- **Ejercicio 1: Creación de un Hotel.**
  - **Casos de Uso:** Administrador crea un nuevo hotel.
  - **Entidad:** `Hotel` (nombre, dirección, tipo, descripción).
  - **API REST:**
    - `POST /hotels`: Crea un nuevo hotel. Requiere datos del hotel en el cuerpo
      de la solicitud (JSON).
      ```json
      {
        "name": "Hotel Pokémon Paradise",
        "address": "Calle Principal 123, Ciudad Pokémon",
        "type": "Hotel Temático",
        "description": "Un hotel para amantes de los Pokémon."
      }
      ```
    - **Implementación:** Crear un servicio `HotelService` con métodos como
      `createHotel(hotelData: HotelData)` y una repositorio `HotelRepository`.

- **Ejercicio 2: Gestión de Habitaciones.**
  - **Casos de Uso:** Administrador crea, edita y elimina habitaciones en un
    hotel.
  - **Entidades:** `Room` (tipo, capacidad, precio, disponibilidad).
  - **API REST:**
    - `GET /hotels/{hotelId}/rooms`: Obtener todas las habitaciones de un hotel.
    - `POST /hotels/{hotelId}/rooms`: Crear una nueva habitación en un hotel.
    - `PUT /hotels/{hotelId}/rooms/{roomId}`: Editar una habitación existente.
    - `DELETE /hotels/{hotelId}/rooms/{roomId}`: Eliminar una habitación.

- **Ejercicio 3: Gestión de Amenities.**
  - **Casos de Uso:** Administrador agrega, edita y elimina comodidades en un
    hotel.
  - **Entidad:** `Amenities` (nombre, descripción).
  - **API REST:**
    - `GET /hotels/{hotelId}/amenities`: Obtener todas las amenities de un
      hotel.
    - `POST /hotels/{hotelId}/amenities`: Agregar una nueva amenity a un hotel.
    - `PUT /hotels/{hotelId}/amenities/{amenityId}`: Editar una amenity
      existente.
    - `DELETE /hotels/{hotelId}/amenities/{amenityId}`: Eliminar una amenity.

**2. Entrenador Pokémon:**

- **Ejercicio 4: Registro e Inicio de Sesión.**
  - **Casos de Uso:** Entrenador se registra y accede al sistema.
  - **Entidades:** `Trainer` (nombre, email, contraseña).
  - **API REST:**
    - `POST /register`: Registrar un nuevo entrenador.
      ```json
      {
        "name": "Ash Ketchum",
        "email": "ash@example.com",
        "password": "password123"
      }
      ```
    - `POST /login`: Iniciar sesión.
      ```json
      {
        "email": "ash@example.com",
        "password": "password123"
      }
      ```
    - **Implementación:** Servicio `UserService`, repositorio `UserRepository`.

- **Ejercicio 5: Búsqueda de Hoteles.**
  - **Casos de Uso:** Entrenador busca hoteles por nombre, ubicación o tipo.
  - **API REST:**
    - `GET /hotels?name=Pokémon&location=Ciudad%20Pokémon`: Buscar hoteles con
      nombre "Pokémon" y en la ciudad Pokémon.
    - `GET /hotels/type/themed`: Buscar hoteles de tipo temático.

- **Ejercicio 6: Realización de Reservas.**
  - **Casos de Uso:** Entrenador reserva una habitación en un hotel.
  - **Entidades:** `Reservation` (trainerId, roomId, checkInDate, checkOutDate,
    status).
  - **API REST:**
    - `POST /reservations`: Crear una nueva reserva. Requiere información del
      entrenador, la habitación y las fechas.
      ```json
      {
        "trainerId": "123",
        "roomId": "456",
        "checkInDate": "2024-03-15",
        "checkOutDate": "2024-03-20"
      }
      ```

**3. Tours de Pokémon y Descubrimiento:**

- **Ejercicio 7: Tours de Pokemon.**
  - **Casos de Uso**: El entrenador inicia un tour para detectar pokemones en
    una ubicación específica.
  - **Entidades:** `Tour` (trainerId, location, date, pokemonDetected).
  - **API REST:**
    - `POST /tours`: Iniciar un nuevo tour.

**4. Reviews de Hoteles.**

- **Ejercicio 8: Escribir Reviews.**
  - **Casos de Uso:** Entrenador escribe una reseña sobre un hotel que visitó.
  - **Entidades:** `Review` (trainerId, hotelId, rating, comment).
  - **API REST:**
    - `POST /hotels/{hotelId}/reviews`: Crear una nueva reseña para un hotel
      específico.
      ```json
      {
        "rating": 4,
        "comment": "Excelente hotel con muy buena ubicación y personal amable."
      }
      ```

**Consideraciones Adicionales:**

- **Autenticación y Autorización:** Implementa JWT (JSON Web Tokens) para la
  seguridad. Los administradores de hoteles tendrán permisos para acceder a
  endpoints que los entrenadores no pueden ver.
- **Validación de Datos:** Asegúrate de validar todos los datos ingresados por
  el usuario, tanto en el frontend como en el backend.
- **Manejo de Errores:** Implementa un manejo robusto de errores con códigos de
  estado HTTP apropiados (400 Bad Request, 401 Unauthorized, 404 Not Found,
  etc.).
- **Testing:** Escribe pruebas unitarias e integración para asegurar la calidad
  del código.

**Tecnologías Recomendadas:**

- **Spring Boot:** Para el framework principal.
- **Kotlin:** Lenguaje de programación.
- **Spring Data JPA:** Para acceso a la base de datos.
- **Spring Security:** Para autenticación y autorización.
- **PostgreSQL o MySQL:** Base de datos relacional.
- **REST Assured/Spring Web Testing:** Para pruebas de API.

**Recursos Útiles:**

- Documentación de Spring Boot: [https://spring.io/](https://spring.io/)
- Documentación de Kotlin:
  [https://kotlinlang.org/docs/](https://kotlinlang.org/docs/)
- Tutoriales de Spring Boot y Kotlin en YouTube, Udemy, etc.

---

Bienvenido a tu proyecto combinado de SpringBoot y Kotlin centrado en Pokémon y
hotelería! A continuación, te presentaremos algunos casos de uso y ejercicios
para cada una de tus entidades:

1. Hotel:
   - Crear un usuario hotelero con sus datos personales y detalles del hotel
     (nombre, dirección, teléfono, correo electrónico, descripción, imagen,
     categoría, tarifa por noche, número de habitaciones disponibles).
   - Registrar un nuevo hotel en el sistema.
   - Buscar hoteles por categoría, ubicación, nombre o tarifa.
   - Actualizar los datos personales y detalles del hotel.
   - Eliminar un hotel del sistema.

2. Room:
   - Registrar una habitación en el hotel (tipo de habitación, número de
     habitación, precio por noche, disponibilidad).
   - Actualizar la disponibilidad de una habitación.
   - Eliminar una habitación del sistema.

3. Amenities:
   - Agregar y eliminar servicios de entretenimiento en los hoteles (spa,
     piscina, fitness center, restaurante, etc.).
   - Mostrar todos los servicios disponibles en un hotel específico.

4. Reservation:
   - Registrar una reservación de una habitación para un entrenador con sus
     datos personales y detalles de la reserva (fecha de entrada, fecha de
     salida, número de personas, nombre del pokémon, cantidad de pokémon).
   - Cancelar una reservación.

5. Trainer:
   - Registrar un entrenador con sus datos personales y detalles sobre sus
     pokemones (nombre de usuario, contraseña, nombre real, correo electrónico,
     pokémons capturados).
   - Buscar entrenadores por nombre de usuario o nombre real.

6. Pokemon:
   - Registrar un nuevo pokémon capturado (nombre, tipo, nivel, habilidades,
     evolución posible).
   - Mostrar los pokemones capturados por un entrenador específico.

7. User:
   - Registrar un usuario sin relación con hoteles o pokémons (nombre de
     usuario, contraseña, nombre real, correo electrónico).
   - Buscar usuarios por nombre de usuario o nombre real.

8. Payment:
   - Realizar una reserva y pagar por ella mediante varios métodos de pago
     (tarjeta de crédito, PayPal, transferencia bancaria).

9. Review:
   - Publicar una reseña sobre un hotel basada en su calidad, servicio, limpieza
     y ubicación.

10. Service:
    - Agendar un tour para que los entrenadores puedan descubrir nuevos
      pokemones en el área del hotel.

---

Claro, puedo ayudarte a diseñar algunos ejercicios y casos de uso para tu curso
de Spring Boot y Kotlin con la temática de Pokémon y hotelería. Aquí te presento
algunas ideas:

1. Creación de entidades:
   - Crea las entidades `Hotel`, `Room`, `Amenity`, `Reservation`, `Trainer`,
     `Pokemon`, `User`, `Payment`, `Review` y `Service`. Para cada entidad,
     asegúrate de incluir los atributos relevantes y las relaciones con otras
     entidades.
2. Gestión de hoteles y habitaciones:
   - Crea un caso de uso para que los administradores de hoteles puedan agregar,
     editar y eliminar hoteles y habitaciones. Incluye la gestión de amenities
     para cada habitación.
   - Implementa la lógica necesaria para reservar una habitación por parte de un
     entrenador. Asegúrate de incluir la verificación de disponibilidad y la
     gestión de pagos.
3. Gestión de entrenadores y Pokémon:
   - Crea un caso de uso para que los entrenadores puedan registrarse y crear
     sus perfiles. Incluye la posibilidad de agregar y gestionar sus Pokémon
     capturados.
   - Implementa la lógica necesaria para que los entrenadores puedan domesticar
     nuevos Pokémon durante su estancia en un hotel o durante un tour de
     detección de Pokémon.
4. Gestión de tours y servicios:
   - Crea un caso de uso para que los administradores de hoteles puedan agregar,
     editar y eliminar tours y servicios ofrecidos por el hotel. Incluye la
     posibilidad de asignar un costo a cada servicio.
   - Implementa la lógica necesaria para que los entrenadores puedan reservar y
     disfrutar de estos tours y servicios durante su estancia en el hotel.
     Asegúrate de incluir la gestión de pagos correspondientes.
5. Gestión de reviews:
   - Crea un caso de uso para que los entrenadores puedan dejar reseñas y
     valoraciones sobre los hoteles en los que se han alojado. Incluye la
     posibilidad de agregar comentarios y puntuar el servicio recibido.
   - Implementa la lógica necesaria para que los administradores de hoteles
     puedan gestionar y responder a estas reseñas, así como para que los
     entrenadores puedan ver las opiniones de otros usuarios sobre un hotel
     antes de reservar.
6. Búsqueda y filtrado:
   - Crea un caso de uso para que los entrenadores puedan buscar y filtrar
     hoteles según sus necesidades. Incluye filtros por ubicación, precio,
     amenities disponibles, servicios ofrecidos, etc.
   - Implementa la lógica necesaria para mostrar a los entrenadores una lista de
     hoteles que se ajustan a sus criterios de búsqueda y permitirles reservar
     habitaciones en los que estén interesados.

---

### **Sistema de Reservación de Hoteles con Pokémons y Servicios**

El sistema integra **hotelera**, **entrenadores**, **pokémon** y **servicios**
para ofrecer una experiencia de usuario multiusos. Los usuarios principales son:

- **Administradores de hoteles**: gestionan reservas, servicios, reviews y
  cuentas de entrenadores.
- **Entrenadores**: reservan hoteles, doméstican pokémon, hacen tours y revisan
  hoteles.

---

### **Ejercicios y Casos de Uso**

#### **1. Entidad: Hotel**

**Objetivo**: Modelar los detalles básicos de un hotel (nombre, ubicación,
servicios, habitaciones).

- **Casos de Uso**:
  - **Crear un hotel**: Registrar nombre, dirección, servicios (ej.: piscina,
    gimnasio), y listado de habitaciones.
  - **Modificar datos del hotel**: Cambiar nombre, servicios, etc.
  - **Eliminar un hotel**: Eliminar todas sus habitaciones y servicios.

---

#### **2. Entidad: Habitación**

**Objetivo**: Modelar las habitaciones disponibles en un hotel (número, tipo,
precio, disponibilidad).

- **Casos de Uso**:
  - **Buscar habitaciones**: Listar habitaciones por precio, tipo o
    disponibilidad.
  - **Reservar una habitación**: Asignar una habitación a un entrenador con
    fecha y hora.
  - **Cancelar una reserva**: Liberar la habitación tras el plazo de pago.

---

#### **3. Entidad: Amenidades**

**Objetivo**: Representar servicios adicionales en un hotel (ej.: spa, cine,
wifi).

- **Casos de Uso**:
  - **Ver amenidades del hotel**: Listar todas las amenities disponibles.
  - **Asignar amenities a un hotel**: Añadir o eliminar servicios.

---

#### **4. Entidad: Reservación**

**Objetivo**: Registrar y gestionar reservas de habitaciones.

- **Casos de Uso**:
  - **Realizar una reserva**: Crear una reserva con detalles del entrenador,
    habitación, fechas.
  - **Ver historial de reservas**: Mostrar todas las reservas realizadas por un
    entrenador o hotel.
  - **Actualizar una reserva**: Cambiar fechas o cancelar si el pago no se ha
    realizado.

---

#### **5. Entidad: Trainer (Entrenador)**

**Objetivo**: Representar a los entrenadores con sus permisos, cuentas y
acciones.

- **Casos de Uso**:
  - **Crear una cuenta de entrenador**: Registrar nombre, email, contraseña, y
    perfil.
  - **Ver historial de reservas**: Mostrar todas las reservas realizadas.
  - **Hacer un tour para detectar pokémon**: Recorrer un hotel, ver pokémon,
    tomar fotos y registrar en reviews.

---

#### **6. Entidad: Pokemon**

**Objetivo**: Representar los pokémon que se encuentran en los hoteles o en las
zonas de descubrimiento.

- **Casos de Uso**:
  - **Crear un pokemon**: Añadir nombre, tipo (ej.: agua, fuego), nivel y
    imagen.
  - **Domesticar un pokemon**: Enviar un entrenador a un hotel para que el
    pokemon se domine.
  - **Mostrar pokémon en un hotel**: Listar todos los pokémon disponibles en un
    hotel.

---

#### **7. Entidad: User (Usuario General)**

**Objetivo**: Representar usuarios no autorizados (ej.: visitantes, usuarios de
prueba).

- **Casos de Uso**:
  - **Ver listado de hoteles**: Listar todos los hoteles disponibles.
  - **Hacer una reserva sin cuenta**: Reservar una habitación usando un token o
    código.

---

#### **8. Entidad: Payment (Pago)**

**Objetivo**: Registrar pagos realizados por reservas.

- **Casos de Uso**:
  - **Realizar pago**: Pagar por una reserva con método seleccionado.
  - **Ver historial de pagos**: Mostrar todas las transacciones.

---

#### **9. Entidad: Review**

**Objetivo**: Registrar opiniones y comentarios sobre hoteles o pokémon.

- **Casos de Uso**:
  - **Hacer una review de un hotel**: Entrar a un hotel, ver pokémon, tomar
    fotos, y escribir una review.
  - **Ver reviews de un hotel**: Ver todas las opiniones de un hotel.

---

### **Casos de Uso Ejemplos**

#### **Casos de Uso para el Entrenador**

1. **Reservar una habitación**:
   - El entrenador selecciona un hotel, ingresa fechas, y realiza la reserva.
2. **Hacer un tour y capturar pokémon**:
   - El entrenador visita un hotel, detecta un pokemon, toma fotos, y registra
     en su cuenta.
3. **Dar una review de un hotel**:
   - El entrenador pide una revisión de un hotel, describe el entorno y la
     experiencia.

#### **Casos de Uso para el Administrador**

1. **Gestionar reservas**:
   - Ver todas las reservas, cancelar si no se ha realizado el pago.
2. **Ver amenidades de un hotel**:
   - Listar todos los servicios disponibles en un hotel.
3. **Actualizar datos del hotel**:
   - Cambiar nombre, direcciones o servicios.

---

### **Relaciones entre Entidades**

- **Hotel ↔ Reservación**: Un hotel puede tener múltiples reservas.
- **Trainer ↔ Reservación**: Un entrenador realiza múltiples reservas en
  hoteles.
- **Hotel ↔ Pokemon**: Un hotel puede tener varios pokémon disponibles (en un
  tour).
- **Reservación ↔ Payment**: Una reserva tiene un pago asociado.

---

### **Ejercicio de Implementación (Simplificado)**

1. **Crear entidades**:
   - Hotel, Habitación, Amenidad, Reservación, Trainer, Pokemon, User, Payment,
     Review.
2. **Crear vistas para consultas**:
   - Listar todas las reservas por hotel.
   - Mostrar reviews de un hotel con calificación.
3. **Implementar casos de uso**:
   - Un entrenador reserva una habitación, visita un hotel, doméstica un
     pokemon, y deja una review.

---

### **Caso de Uso Integrado**

1. **Escena 1**: Entrenador A reserva una habitación en Hotel X.
2. **Escena 2**: El entrenador visita el hotel, encuentra un pokémon, lo
   domestica, y registra la experiencia en su cuenta.
3. **Escena 3**: El entrenador da una review de Hotel X con calificación alta.
4. **Escena 4**: El administrador revisa la reserva, confirma el pago, y
   actualiza los datos del hotel.

---

Claro, puedo ayudarte a estructurar los ejercicios y casos de uso para el
desarrollo de tu sistema con Spring Boot y Kotlin. A continuación, te propongo
un esquema general para cada uno de ellos.

### Ejercicio 1: Diseño del Modelo

**Objetivo:** Definir las entidades (hotel, room, amenities, reservations,
trainer, pokemon, user, payment, review, service) y sus relaciones utilizando
UML o diagramas entidad-relación.

- **Entidades:**
  - Hotel
  - Room
  - Amenities
  - Reservation
  - Trainer
  - Pokemon
  - User (Administrador, Entrenador)
  - Payment
  - Review
  - Service

### Ejercicio 2: Configuración de Spring Boot y Kotlin

**Objetivo:** Crear una aplicación básica con Spring Boot configurada para
manejar las entidades definidas.

- **Pasos:**
  - Configurar el proyecto en Spring Initializr.
  - Definir los repositorios (Repository) para cada entidad.
  - Establecer las relaciones entre las entidades utilizando anotaciones
    (@ManyToMany, @OneToMany, etc.)

### Ejercicio 3: Implementación de Entidades y Repositorios

**Objetivo:** Desarrollar el código para las clases de entidades y sus
respectivos repositorios.

- **Pasos:**
  - Crear las clases de cada entidad con los atributos necesarios.
  - Implementar los repositorios (Repository) que incluyen métodos CRUD básicos
    y otros específicos según sea necesario.

### Ejercicio 4: Criterio de Usuario y Autenticación

**Objetivo:** Implementar la gestión de usuarios y autenticación para
administradores y entrenadores.

- **Pasos:**
  - Crear un modelo de usuario con roles (Administrador, Entrenador).
  - Configurar Spring Security para manejar la autenticación y autorización.
  - Desarrollar endpoints para registro y login de usuarios.

### Ejercicio 5: Reservas y Servicios

**Objetivo:** Implementar la funcionalidad para reservas de hoteles, servicios
adicionales en los hoteles (como tours de pokémon) y pagos asociados.

- **Pasos:**
  - Definir las entidades y relaciones necesarias para reservas.
  - Implementar el proceso de reserva, incluyendo la confirmación y cancelación.
  - Integrar un sistema de pago, posiblemente utilizando una API externa para
    transacciones en línea.

### Ejercicio 6: Descubrimiento de Pokemones y Tours

**Objetivo:** Permitir a los entrenadores visitar hoteles (usando reservas) y
detectar pokemones, además de realizar tours y hacer reviews de los hoteles.

- **Pasos:**
  - Crear funcionalidades para que los entrenadores puedan marcar visitas a
    hoteles.
  - Implementar la lógica para el descubrimiento de pokemones en diferentes
    ubicaciones (hoteles).
  - Desarrollar métodos para hacer y revisar tours, y generar reviews.

### Ejercicio 7: Casos de Uso y Pruebas

**Objetivo:** Definir casos de uso específicos para cada funcionalidad
implementada y realizar pruebas unitarias/básicas.

- **Pasos:**
  - Identificar los flujos de trabajo principales (registro usuario, reserva
    hotel, detección pokemon, etc.)
  - Desarrollar una serie de casos de uso que cubran todas las funcionalidades.
  - Realizar pruebas para asegurar la correcta operación de cada componente.

### Ejercicio 8: Integración y Pruebas Finales

**Objetivo:** Integrar todos los componentes, realizar pruebas exhaustivas del
sistema completo y preparar el sistema para su despliegue.

- **Pasos:**
  - Realizar la integración final de todas las partes del sistema.
  - Probar el funcionamiento en diferentes escenarios y casos extremos.
  - Preparar el sistema para el entorno de producción, incluyendo optimización
    de rendimiento y seguridad.

---
