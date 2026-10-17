---
id: spring-boot-kotlin
name: spring-boot-kotlin
description: >
  Provides expert knowledge of Spring Boot conventions, Kotlin idioms, and MVC
  patterns for backend API development.

user-invokable: true
license: MIT
metadata:
  author: lgzarturo
  category: spring

compatibility:
  tools: [claude, codex, gemini, agy, opencode]
  stacks:
    languages: [kotlin]
    frameworks: [spring-boot, spring-mvc, spring-data-jpa, spring-security]
    databases: [postgresql, h2]

risk:
  level: medium
  can_execute_shell: false
  can_modify_files: true
  requires_network: false

inputs:
  - source_files
  - test_files
  - build.gradle.kts
  - application.yml

outputs:
  - controller classes
  - service classes
  - repository interfaces
  - domain entities
  - DTO classes
  - test classes

quality:
  reviewed_by: codeconductor-core
  version: 0.1.0
---

# Spring Boot + Kotlin

## Project Structure

Use feature-oriented MVC. One package per feature, not one package per layer.

```
src/main/kotlin/{base-package}/{feature}/
  controller/       # HTTP layer only — no business logic
  service/          # Business logic
  repository/       # Data access — extends JpaRepository or CrudRepository
  domain/           # JPA entities
  dto/              # Request/response objects — no @Entity here

src/test/kotlin/{base-package}/{feature}/
  controller/       # MockMvc tests
  service/          # Unit tests with MockK
  repository/       # @DataJpaTest tests

src/main/resources/
  application.yml
  application-prod.yml    # NOT in repo — use env vars
  db/migration/           # Flyway scripts
```

Never use `src/main/kotlin/controllers/`, `src/main/kotlin/services/`, etc. That
is layer-first structure and it does not scale.

## Kotlin Idioms for Spring

**Data classes for DTOs.** No setters, no mutable state.

```kotlin
data class CreateUserRequest(
    @field:NotBlank val email: String,
    @field:Size(min = 2, max = 100) val name: String
)

data class UserResponse(
    val id: UUID,
    val email: String,
    val name: String
)
```

**Sealed classes for domain results and errors.**

```kotlin
sealed class UserResult {
    data class Found(val user: User) : UserResult()
    data class NotFound(val id: UUID) : UserResult()
    data class Conflict(val email: String) : UserResult()
}
```

**Null safety.** Never use `!!` unless you have already verified the value is
non-null and can document why. Prefer:

```kotlin
// bad
val name = user!!.name

// good
val name = user?.name ?: throw UserNotFoundException(id)

// good — scoped null check
user?.let { sendWelcomeEmail(it.email) }
```

**Extension functions** instead of utility classes.

```kotlin
// bad — Java style utility class
object UserMapper {
    fun toResponse(user: User): UserResponse { ... }
}

// good — extension function
fun User.toResponse(): UserResponse = UserResponse(
    id = id,
    email = email,
    name = name
)
```

**Coroutines**: do not introduce coroutines unless the project already uses
them. Adding coroutines requires a Task Card with explicit scope.

## Spring Boot Patterns

### Controller

```kotlin
@RestController
@RequestMapping("/api/v1/users")
class UserController(private val userService: UserService) {

    @GetMapping("/{id}")
    fun getUser(@PathVariable id: UUID): ResponseEntity<UserResponse> {
        return when (val result = userService.getById(id)) {
            is UserResult.Found -> ResponseEntity.ok(result.user.toResponse())
            is UserResult.NotFound -> ResponseEntity.notFound().build()
        }
    }

    @PostMapping
    fun createUser(
        @Valid @RequestBody request: CreateUserRequest
    ): ResponseEntity<UserResponse> {
        val result = userService.create(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(result.toResponse())
    }
}
```

Rules:

- One controller per feature
- No business logic in controllers — delegate to service
- Use `@Valid` on request body params
- Return `ResponseEntity<T>` for explicit HTTP status control

### Service

```kotlin
@Service
@Transactional
class UserService(private val userRepository: UserRepository) {

    @Transactional(readOnly = true)
    fun getById(id: UUID): UserResult {
        val user = userRepository.findById(id).orElse(null)
            ?: return UserResult.NotFound(id)
        return UserResult.Found(user)
    }

    fun create(request: CreateUserRequest): User {
        if (userRepository.existsByEmail(request.email)) {
            throw UserConflictException(request.email)
        }
        return userRepository.save(User(email = request.email, name = request.name))
    }
}
```

Rules:

- All business logic lives here
- `@Transactional` at class level, `readOnly = true` for queries
- Never `@Transactional` on private methods — Spring proxy does not intercept
  them
- Never inject repositories into controllers

### Repository

```kotlin
@Repository
interface UserRepository : JpaRepository<User, UUID> {
    fun findByEmail(email: String): User?
    fun existsByEmail(email: String): Boolean

    @Query("SELECT u FROM User u WHERE u.role = :role AND u.active = true")
    fun findActiveByRole(@Param("role") role: UserRole): List<User>
}
```

Rules:

- Extend `JpaRepository<Entity, IdType>` or `CrudRepository`
- Derived query methods for simple lookups
- `@Query` with JPQL for complex queries
- Native SQL only when JPQL cannot express it

### Entity

```kotlin
@Entity
@Table(
    name = "users",
    indexes = [Index(columnList = "email", unique = true)]
)
class User(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID = UUID.randomUUID(),

    @Column(name = "email", nullable = false, unique = true)
    var email: String,

    @Column(name = "name", nullable = false)
    var name: String,

    @Column(name = "created_at", nullable = false, updatable = false)
    @CreatedDate
    val createdAt: Instant = Instant.now()
)
```

Rules:

- `@Table` with explicit name — never rely on inferred names
- `@Column(nullable = false)` always explicit
- UUIDs as PKs with `GenerationType.UUID`
- Kotlin data class with JPA requires the Kotlin JPA plugin (adds no-arg
  constructor)
- `equals`/`hashCode` based only on `id`, not all fields — or omit and use
  reference equality

### DTO Separation

Never expose `@Entity` directly in API responses. Always map to a DTO.

```kotlin
// bad
@GetMapping("/{id}")
fun getUser(@PathVariable id: UUID): User  // exposes entity internals

// good
@GetMapping("/{id}")
fun getUser(@PathVariable id: UUID): UserResponse  // controlled surface
```

## Validation

Validate at the DTO layer, not the service layer.

```kotlin
data class CreateUserRequest(
    @field:NotBlank(message = "Email is required")
    @field:Email(message = "Email must be valid")
    val email: String,

    @field:NotBlank(message = "Name is required")
    @field:Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    val name: String
)
```

Use `@field:` prefix on annotations — Kotlin applies annotations to the property
by default, not the backing field.

## Error Handling

```kotlin
@ControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(UserNotFoundException::class)
    fun handleUserNotFound(ex: UserNotFoundException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ErrorResponse(error = ex.message ?: "User not found", code = "USER_NOT_FOUND"))
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(ex: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {
        val message = ex.bindingResult.fieldErrors
            .joinToString(", ") { "${it.field}: ${it.defaultMessage}" }
        return ResponseEntity.badRequest()
            .body(ErrorResponse(error = message, code = "VALIDATION_ERROR"))
    }
}

data class ErrorResponse(val error: String, val code: String)
```

Rules:

- One `@ControllerAdvice` — do not scatter `@ExceptionHandler` across
  controllers
- Typed domain exceptions — never throw `RuntimeException` directly
- Consistent error body: `{ "error": "...", "code": "..." }`
- Never expose stack traces in responses

## Testing

**Unit tests with MockK.**

```kotlin
@ExtendWith(MockKExtension::class)
class UserServiceTest {

    @MockK
    private lateinit var userRepository: UserRepository

    private lateinit var userService: UserService

    @BeforeEach
    fun setUp() {
        userService = UserService(userRepository)
    }

    @Test
    fun `should return NotFound when user does not exist`() {
        // Arrange
        val id = UUID.randomUUID()
        every { userRepository.findById(id) } returns Optional.empty()

        // Act
        val result = userService.getById(id)

        // Assert
        assertThat(result).isInstanceOf(UserResult.NotFound::class.java)
        verify(exactly = 1) { userRepository.findById(id) }
    }
}
```

**Integration tests with MockMvc.**

```kotlin
@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Test
    fun `should return 404 when user is not found`() {
        mockMvc.perform(get("/api/v1/users/${UUID.randomUUID()}"))
            .andExpect(status().isNotFound)
    }
}
```

**Repository tests.**

```kotlin
@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private lateinit var userRepository: UserRepository

    @Test
    fun `should find user by email`() {
        val user = userRepository.save(User(email = "test@example.com", name = "Test"))
        val found = userRepository.findByEmail("test@example.com")
        assertThat(found).isEqualTo(user)
    }
}
```

Rules:

- Use MockK, not Mockito — the Kotlin API is cleaner and null-safe
- Do not use `@MockBean` where `mockk<T>()` is sufficient
- `@DataJpaTest` with H2 for repository tests (fast)
- `@SpringBootTest` + `@AutoConfigureMockMvc` for controller tests

## Security Rules

- No stack traces in API responses — only `@ControllerAdvice` with controlled
  messages
- Validate all input in DTO layer — service layer should receive
  already-validated data
- `application-prod.yml` must not be committed — use environment variables or a
  secrets manager
- Credentials, tokens, and keys are never hardcoded — use `@Value` with
  externalized config
