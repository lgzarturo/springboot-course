---
id: spring-boot-testing-strategy
name: spring-boot-testing-strategy
description: >
  Provides expert knowledge of the testing pyramid, test design principles, MockK patterns, and integration testing conventions for Spring Boot + Kotlin projects.

user-invokable: true
license: MIT
metadata:
  author: lgzarturo
  category: testing

compatibility:
  tools: [claude, codex, gemini, agy, opencode]
  stacks:
    languages: [kotlin, java]
    frameworks: [spring-boot, kotest, mockk, junit5, mockk, testcontainers, assertj]
    databases: [postgresql, sqlite, h2]

risk:
  level: low
  can_execute_shell: false
  can_modify_files: true
  requires_network: false

inputs: []

outputs: []

quality:
  reviewed_by: codeconductor-core
  version: 0.1.0
paths:
  - "**/*.kt"
  - "**/src/test/**"
---

# Testing Strategy

## Testing Pyramid

```text
       /\
      /  \
     / E2E\       10% — full API, happy path + main error cases
    /------\
   /  Integ \     20% — components with real dependencies (DB, HTTP)
  /----------\
 /    Unit    \   70% — isolated, mocked dependencies, fast
/______________\
```

Unit tests are the foundation. They are fast, deterministic, and cheap to run.
Integration tests validate that components work together. E2E tests validate
that the system works end to end — keep them minimal.

If you find yourself writing more integration tests than unit tests, the code
under test has too many responsibilities bundled together.

## Test Naming Convention

Format: `should [expected behavior] when [condition]`

```kotlin
@Test
fun `should return user when found by id`() { ... }

@Test
fun `should return 404 when user does not exist`() { ... }

@Test
fun `should throw ConflictException when email already exists`() { ... }

@Test
fun `should not return deleted users in list`() { ... }
```

Group related tests with `@Nested`:

```kotlin
@ExtendWith(MockKExtension::class)
class UserServiceTest {

    @Nested
    inner class GetById {
        @Test
        fun `should return user when found`() { ... }

        @Test
        fun `should return NotFound when user does not exist`() { ... }
    }

    @Nested
    inner class Create {
        @Test
        fun `should create and return user when email is unique`() { ... }

        @Test
        fun `should throw ConflictException when email already exists`() { ... }
    }
}
```

## Unit Test Structure (AAA)

Every test follows Arrange → Act → Assert. Use blank lines to separate each
phase.

```kotlin
@Test
fun `should return user when found by id`() {
    // Arrange
    val userId = UUID.randomUUID()
    val user = User(id = userId, email = "user@example.com", name = "Test User")
    every { userRepository.findById(userId) } returns Optional.of(user)

    // Act
    val result = userService.getById(userId)

    // Assert
    assertThat(result).isInstanceOf(UserResult.Found::class.java)
    val found = result as UserResult.Found
    assertThat(found.user.id).isEqualTo(userId)
    verify(exactly = 1) { userRepository.findById(userId) }
}
```

No inline comments between phases once the structure is clear. The blank lines
are enough.

## What NOT to Test

**Framework wiring.** Spring handles dependency injection. Do not write tests
that verify `@Autowired` works.

**JPA mapping.** Do not test that `@Column(name = "email")` maps to the right
column. That is Hibernate's job.

**Trivial getters and setters.** A data class property has no logic. There is
nothing to test.

**Private methods directly.** Private methods are implementation details. Test
them through the public behavior that uses them. If a private method is complex
enough to need its own test, it should be extracted into a separate class.

**Implementation, not behavior.** Tests that verify HOW something is done (mock
call order, internal state) are brittle. Test WHAT the output or side effect is.

```kotlin
// bad — tests implementation detail
verify { userRepository.findById(any()) }
verify { cacheService.put(any(), any()) }
verify(ordering = Ordering.ORDERED) {   // this is too coupled to internals
    userRepository.findById(userId)
    cacheService.put(userId, user)
}

// good — tests observable behavior
assertThat(result).isEqualTo(expectedUser)
```

## MockK Patterns

### Basic Setup

```kotlin
@ExtendWith(MockKExtension::class)
class UserServiceTest {

    @MockK
    private lateinit var userRepository: UserRepository

    @MockK
    private lateinit var emailService: EmailService

    private lateinit var userService: UserService

    @BeforeEach
    fun setUp() {
        userService = UserService(userRepository, emailService)
    }
}
```

### Stubbing

```kotlin
// Return value
every { userRepository.findById(userId) } returns Optional.of(user)

// Return null (for nullable return types)
every { userRepository.findByEmail(any()) } returns null

// Throw exception
every { userRepository.save(any()) } throws DataIntegrityViolationException("Duplicate")

// Return different values on successive calls
every { userRepository.findById(any()) } returnsMany listOf(Optional.of(user), Optional.empty())

// Answer with computation
every { userRepository.save(any()) } answers { firstArg() }
```

### Verification

```kotlin
// Verify called exactly once with specific argument
verify(exactly = 1) { userRepository.findById(userId) }

// Verify called with any argument
verify { emailService.sendWelcome(any()) }

// Verify never called
verify(exactly = 0) { emailService.sendWelcome(any()) }

// Verify called with specific argument
verify { emailService.sendWelcome(match { it.email == "user@example.com" }) }
```

### Relaxed Mocks

```kotlin
// relaxed = true — returns default values (null, 0, false, empty list)
// Use only when you do not care about the return value of any method
val logger = mockk<Logger>(relaxed = true)
```

Do not use `relaxed = true` on mocks where you need to verify behavior — a
relaxed mock does not fail on unexpected calls, which can hide bugs.

### Coroutines

```kotlin
// Stubbing suspend functions
coEvery { userRepository.findById(userId) } returns user

// Verifying suspend functions
coVerify(exactly = 1) { userRepository.findById(userId) }
```

### Argument Matchers

```kotlin
// Any value
every { service.process(any()) } returns result

// Specific type
every { service.process(any<UserRequest>()) } returns result

// Custom predicate
every { service.notify(match { it.email.endsWith("@example.com") }) } just Runs

// Capture for assertion
val slot = slot<User>()
every { userRepository.save(capture(slot)) } returns savedUser
// ... trigger the action ...
assertThat(slot.captured.email).isEqualTo("expected@example.com")
```

## Integration Test Patterns

### Controller Tests with MockMvc

```kotlin
@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Test
    fun `should return 200 with user when found`() {
        mockMvc.perform(
            get("/api/v1/users/${existingUser.id}")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.email").value(existingUser.email))
            .andExpect(jsonPath("$.id").value(existingUser.id.toString()))
    }

    @Test
    fun `should return 400 when request body is invalid`() {
        val invalidRequest = mapOf("email" to "not-an-email")

        mockMvc.perform(
            post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest))
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
    }

    @Test
    fun `should return 404 when user is not found`() {
        mockMvc.perform(get("/api/v1/users/${UUID.randomUUID()}"))
            .andExpect(status().isNotFound)
    }
}
```

`@AutoConfigureMockMvc` configures MockMvc without starting a real HTTP server.
The test is faster than `RANDOM_PORT` but still tests the full Spring MVC stack.

Use `RANDOM_PORT` only when you need real HTTP (e.g., testing HTTP client
behavior, WebSockets, or SSE).

### Database Tests with Testcontainers

```kotlin
@SpringBootTest
@Testcontainers
class UserRepositoryIntegrationTest {

    companion object {
        @Container
        @JvmStatic
        val postgres = PostgreSQLContainer<Nothing>("postgres:16").apply {
            withDatabaseName("testdb")
            withUsername("test")
            withPassword("test")
        }

        @DynamicPropertySource
        @JvmStatic
        fun overrideProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url", postgres::getJdbcUrl)
            registry.add("spring.datasource.username", postgres::getUsername)
            registry.add("spring.datasource.password", postgres::getPassword)
        }
    }

    @Autowired
    private lateinit var userRepository: UserRepository

    @Test
    fun `should enforce unique email at database level`() {
        userRepository.save(User(email = "unique@example.com", name = "First"))
        assertThrows<DataIntegrityViolationException> {
            userRepository.save(User(email = "unique@example.com", name = "Second"))
        }
    }
}
```

Share the container across test classes using a base class with `@Container` on
a static field. Testcontainers reuses the same container if the configuration is
identical.

### Seed Data with @Sql

```kotlin
@SpringBootTest
@AutoConfigureMockMvc
@Sql("/test-data/users.sql")   // runs before each test in this class
class UserControllerTest { ... }

// Or per-test
@Test
@Sql("/test-data/users-with-orders.sql")
fun `should return orders when user has orders`() { ... }
```

SQL files go in `src/test/resources/test-data/`. Use descriptive names that
explain what state they create, not what feature they serve.

## Test Data Factories

Never hardcode UUIDs or share mutable state between tests.

```kotlin
object UserTestFactory {

    fun create(
        id: UUID = UUID.randomUUID(),
        email: String = "user-${UUID.randomUUID()}@example.com",
        name: String = "Test User",
        role: UserRole = UserRole.USER
    ): User = User(
        id = id,
        email = email,
        name = name,
        role = role
    )

    fun createRequest(
        email: String = "user-${UUID.randomUUID()}@example.com",
        name: String = "Test User"
    ): CreateUserRequest = CreateUserRequest(
        email = email,
        name = name
    )

    fun createAdmin(): User = create(role = UserRole.ADMIN)

    fun createList(count: Int): List<User> = (1..count).map { create() }
}
```

Usage in tests:

```kotlin
val user = UserTestFactory.create(email = "specific@example.com")
val admin = UserTestFactory.createAdmin()
val users = UserTestFactory.createList(5)
```

Default values use `UUID.randomUUID()` to ensure tests do not share state
through hardcoded IDs or emails.

## When Tests Are Required

**New behavior.** Every new feature requires at minimum one unit test and one
integration test covering the happy path. Edge cases and error paths require
additional unit tests.

**Bug fix.** Write a regression test that reproduces the bug before fixing it.
The test must fail before the fix and pass after. This prevents the bug from
reappearing silently.

```kotlin
// Example: bug where findByEmail returned deleted users
@Test
fun `should not return deleted user — regression for bug #42`() {
    val user = userRepository.save(UserTestFactory.create(email = "gone@example.com"))
    userRepository.delete(user)
    val result = userRepository.findByEmail("gone@example.com")
    assertThat(result).isNull()
}
```

**Refactor.** Do not write new tests. The existing tests must pass unchanged. If
they do not, the refactor changed behavior — that is a bug.

**High-risk paths.** Security, payment processing, data migrations, and
permission checks require contract tests in addition to unit and integration
tests. These paths have severe consequences if broken.

## CI Integration

Tests must run in CI on every pull request. The pipeline must fail if any test
fails.

Recommended test execution order:

1. Unit tests (fastest — fail fast)
2. Integration tests with H2 (`@DataJpaTest`)
3. Integration tests with Testcontainers (slowest — run last)

Separate test tasks in Gradle:

```kotlin
// build.gradle.kts
tasks.register<Test>("integrationTest") {
    description = "Runs integration tests"
    group = "verification"
    useJUnitPlatform {
        includeTags("integration")
    }
    shouldRunAfter(tasks.test)
}
```

Tag integration tests:

```kotlin
@Tag("integration")
@SpringBootTest
class UserRepositoryIntegrationTest { ... }
```

Do not mix unit and integration test tags in the same class. A test class is
either one or the other.
