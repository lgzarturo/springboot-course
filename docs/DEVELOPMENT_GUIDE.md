# Guía de Desarrollo - Spring Boot Course

## 🚀 Inicio Rápido

### Prerrequisitos
- Java 21 o superior
- Gradle 8.x
- IDE (IntelliJ IDEA recomendado)

### Ejecutar la aplicación

```bash
# Usando Gradle Wrapper
./gradlew bootRun

# O en Windows
gradlew.bat bootRun
```

La aplicación estará disponible en: `http://localhost:8080`

### Acceder a la documentación

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/api-docs
- **H2 Console**: http://localhost:8080/h2-console

## 🧪 Ejecutar Tests

```bash
# Todos los tests
./gradlew test

# Tests con reporte
./gradlew test --info

# Tests de una clase específica
./gradlew test --tests "PingServiceTest"
```

## 📝 Convenciones de Código

### Estructura de Archivos

```kotlin
// 1. Package declaration
package com.lgzarturo.springbootcourse.domain.service

// 2. Imports (agrupados y ordenados)
import com.lgzarturo.springbootcourse.domain.model.Ping
import org.springframework.stereotype.Service

// 3. KDoc
/**
 * Descripción de la clase
 */

// 4. Anotaciones
@Service

// 5. Declaración de clase
class PingService : PingUseCase {
    // Implementación
}
```

### Naming Conventions

#### Clases
```kotlin
// Controllers
class PingController

// Services
class PingService

// Use Cases (interfaces)
interface PingUseCase

// DTOs
data class PingResponse
data class CreateUserRequest

// Mappers
class PingMapper

// Entities
@Entity
class User
```

#### Funciones
```kotlin
// Use Cases (verbos en infinitivo)
fun getPing(): Ping
fun createUser(request: CreateUserRequest): User
fun updateUserProfile(id: Long, data: UpdateProfileRequest): User

// Mappers
fun toResponse(ping: Ping): PingResponse
fun toEntity(request: CreateUserRequest): User
fun toDomain(entity: UserEntity): User
```

#### Variables
```kotlin
// camelCase para variables
val userName: String
val isActive: Boolean
val createdAt: LocalDateTime

// UPPER_SNAKE_CASE para constantes
const val MAX_PAGE_SIZE = 100
const val DEFAULT_TIMEOUT = 30
```

### Kotlin Best Practices

#### 1. Data Classes para DTOs y Models
```kotlin
// ✅ Correcto
data class PingResponse(
    val message: String,
    val timestamp: LocalDateTime
)

// ❌ Incorrecto
class PingResponse {
    var message: String = ""
    var timestamp: LocalDateTime = LocalDateTime.now()
}
```

#### 2. Inmutabilidad (val sobre var)
```kotlin
// ✅ Correcto
val message: String = "pong"
val items: List<String> = listOf("a", "b", "c")

// ❌ Incorrecto (solo si realmente necesitas mutabilidad)
var message: String = "pong"
```

#### 3. Null Safety
```kotlin
// ✅ Correcto
fun findUser(id: Long): User?

fun processUser(user: User?) {
    user?.let {
        // Procesar usuario
    }
}

// ❌ Incorrecto
fun findUser(id: Long): User  // Puede lanzar NullPointerException
```

#### 4. Extension Functions
```kotlin
// ✅ Correcto
fun LocalDateTime.toIsoString(): String {
    return this.format(DateTimeFormatter.ISO_DATE_TIME)
}

// Uso
val timestamp = LocalDateTime.now()
val isoString = timestamp.toIsoString()
```

#### 5. Constructor Injection
```kotlin
// ✅ Correcto
@RestController
class PingController(
    private val pingUseCase: PingUseCase,
    private val pingMapper: PingMapper
)

// ❌ Incorrecto
@RestController
class PingController {
    @Autowired
    private lateinit var pingUseCase: PingUseCase
}
```

## 🏗️ Cómo Agregar una Nueva Funcionalidad

### Ejemplo: Agregar un módulo de Usuarios

#### 1. Crear el Modelo de Dominio
```kotlin
// domain/model/User.kt
package com.lgzarturo.springbootcourse.domain.model

data class User(
    val id: Long? = null,
    val username: String,
    val email: String,
    val createdAt: LocalDateTime = LocalDateTime.now()
)
```

#### 2. Crear el Puerto de Entrada (Use Case)
```kotlin
// domain/port/input/UserUseCase.kt
package com.lgzarturo.springbootcourse.domain.port.input

interface UserUseCase {
    fun createUser(username: String, email: String): User
    fun findUserById(id: Long): User?
    fun getAllUsers(): List<User>
}
```

#### 3. Crear el Puerto de Salida (Repository Interface)
```kotlin
// domain/port/output/UserRepository.kt
package com.lgzarturo.springbootcourse.domain.port.output

interface UserRepository {
    fun save(user: User): User
    fun findById(id: Long): User?
    fun findAll(): List<User>
}
```

#### 4. Implementar el Servicio de Dominio
```kotlin
// domain/service/UserService.kt
package com.lgzarturo.springbootcourse.domain.service

@Service
class UserService(
    private val userRepository: UserRepository
) : UserUseCase {
    
    override fun createUser(username: String, email: String): User {
        val user = User(username = username, email = email)
        return userRepository.save(user)
    }
    
    override fun findUserById(id: Long): User? {
        return userRepository.findById(id)
    }
    
    override fun getAllUsers(): List<User> {
        return userRepository.findAll()
    }
}
```

#### 5. Crear la Entidad JPA
```kotlin
// infrastructure/persistence/entity/UserEntity.kt
package com.lgzarturo.springbootcourse.infrastructure.persistence.entity

@Entity
@Table(name = "users")
data class UserEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    
    @Column(nullable = false, unique = true)
    val username: String,
    
    @Column(nullable = false, unique = true)
    val email: String,
    
    @Column(nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
)
```

#### 6. Crear el Repositorio JPA
```kotlin
// infrastructure/persistence/repository/JpaUserRepository.kt
package com.lgzarturo.springbootcourse.infrastructure.persistence.repository

interface JpaUserRepository : JpaRepository<UserEntity, Long>
```

#### 7. Implementar el Adaptador de Persistencia
```kotlin
// infrastructure/persistence/adapter/UserRepositoryAdapter.kt
package com.lgzarturo.springbootcourse.infrastructure.persistence.adapter

@Component
class UserRepositoryAdapter(
    private val jpaUserRepository: JpaUserRepository,
    private val userMapper: UserEntityMapper
) : UserRepository {
    
    override fun save(user: User): User {
        val entity = userMapper.toEntity(user)
        val savedEntity = jpaUserRepository.save(entity)
        return userMapper.toDomain(savedEntity)
    }
    
    override fun findById(id: Long): User? {
        return jpaUserRepository.findById(id)
            .map { userMapper.toDomain(it) }
            .orElse(null)
    }
    
    override fun findAll(): List<User> {
        return jpaUserRepository.findAll()
            .map { userMapper.toDomain(it) }
    }
}
```

#### 8. Crear los DTO
```kotlin
// infrastructure/rest/dto/request/CreateUserRequest.kt
data class CreateUserRequest(
    @field:NotBlank(message = "Username is required")
    val username: String,
    
    @field:Email(message = "Email must be valid")
    @field:NotBlank(message = "Email is required")
    val email: String
)

// infrastructure/rest/dto/response/UserResponse.kt
data class UserResponse(
    val id: Long,
    val username: String,
    val email: String,
    val createdAt: LocalDateTime
)
```

#### 9. Crear el Mapper de DTOs
```kotlin
// infrastructure/rest/mapper/UserMapper.kt
@Component
class UserMapper {
    fun toResponse(user: User): UserResponse {
        return UserResponse(
            id = user.id!!,
            username = user.username,
            email = user.email,
            createdAt = user.createdAt
        )
    }
}
```

#### 10. Crear el Controller
```kotlin
// infrastructure/rest/controller/UserController.kt
@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "Users", description = "User management endpoints")
class UserController(
    private val userUseCase: UserUseCase,
    private val userMapper: UserMapper
) {
    
    @PostMapping
    @Operation(summary = "Create a new user")
    fun createUser(
        @Valid @RequestBody request: CreateUserRequest
    ): ResponseEntity<UserResponse> {
        val user = userUseCase.createUser(request.username, request.email)
        val response = userMapper.toResponse(user)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID")
    fun getUserById(@PathVariable id: Long): ResponseEntity<UserResponse> {
        val user = userUseCase.findUserById(id)
            ?: throw UserNotFoundException("User not found with id: $id")
        val response = userMapper.toResponse(user)
        return ResponseEntity.ok(response)
    }
    
    @GetMapping
    @Operation(summary = "Get all users")
    fun getAllUsers(): ResponseEntity<List<UserResponse>> {
        val users = userUseCase.getAllUsers()
        val responses = users.map { userMapper.toResponse(it) }
        return ResponseEntity.ok(responses)
    }
}
```

#### 11. Crear Tests
```kotlin
// Test unitario del servicio
class UserServiceTest {
    private val userRepository = mockk<UserRepository>()
    private val userService = UserService(userRepository)
    
    @Test
    fun `should create user successfully`() {
        // Given
        val user = User(username = "john", email = "john@example.com")
        every { userRepository.save(any()) } returns user.copy(id = 1L)
        
        // When
        val result = userService.createUser("john", "john@example.com")
        
        // Then
        assertNotNull(result.id)
        assertEquals("john", result.username)
        verify { userRepository.save(any()) }
    }
}

// Test de integración del controller
@WebMvcTest(UserController::class)
class UserControllerTest {
    @Autowired
    private lateinit var mockMvc: MockMvc
    
    @MockkBean
    private lateinit var userUseCase: UserUseCase
    
    @Test
    fun `should create user successfully`() {
        // Given
        val request = CreateUserRequest("john", "john@example.com")
        val user = User(id = 1L, username = "john", email = "john@example.com")
        every { userUseCase.createUser(any(), any()) } returns user
        
        // When & Then
        mockMvc.perform(
            post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.username").value("john"))
    }
}
```

## 🔍 Debugging Tips

### 1. Logging
```kotlin
import org.slf4j.LoggerFactory

class UserService {
    private val logger = LoggerFactory.getLogger(UserService::class.java)
    
    fun createUser(username: String, email: String): User {
        logger.debug("Creating user with username: $username")
        // ...
        logger.info("User created successfully with id: ${user.id}")
        return user
    }
}
```

### 2. Actuator Endpoints
```bash
# Health check
curl http://localhost:8080/actuator/health

# Metrics
curl http://localhost:8080/actuator/metrics

# Specific metric
curl http://localhost:8080/actuator/metrics/http.server.requests
```

## 📚 Recursos Adicionales

- [Kotlin Documentation](https://kotlinlang.org/docs/home.html)
- [Spring Boot Reference](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [Spring Data JPA](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/)
- [MockK Documentation](https://mockk.io/)

## 🤝 Contribuir

1. Fork el proyecto
2. Crea una rama para tu feature (`git checkout -b feature/AmazingFeature`)
3. Commit tus cambios (`git commit -m 'Add some AmazingFeature'`)
4. Push à la rama (`git push origin feature/AmazingFeature`)
5. Abre un Pull Request

---

**Happy Coding! 🚀**
