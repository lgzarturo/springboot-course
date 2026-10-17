---
id: spring-boot-feature
name: spring-boot-feature
description: >
  Guides the creation of complete Spring Boot features following a structured,
  layer-by-layer workflow: entity, repository, service, controller, and tests.
  Applies Kotlin idioms, Bean Validation, and MockK test patterns throughout.

user-invokable: true
license: MIT
metadata:
  author: lgzarturo
  category: spring

compatibility:
  tools: [claude, codex, gemini, agy, opencode]
  stacks:
    languages: [kotlin]
    frameworks: [spring-boot, spring-mvc, spring-data-jpa, mockk, junit5]
    databases: [postgresql, h2]

risk:
  level: medium
  can_execute_shell: false
  can_modify_files: true
  requires_network: false

inputs:
  - feature description
  - existing domain context (if any)
  - build.gradle.kts
  - application.yml

outputs:
  - domain entity
  - JPA repository interface
  - service class with business logic
  - REST controller with validations
  - request/response DTOs
  - unit tests (MockK)
  - controller integration tests (MockMvc)

quality:
  reviewed_by: codeconductor-core
  version: 0.1.0
---

# Spring Boot Feature Creation

When asked to create a feature, follow these steps **in order**. Do not skip
steps or combine layers. Each layer has a single responsibility.

**When to ask questions:** Only ask when there is genuine ambiguity in business
logic — for example, what happens when a duplicate is found, or whether soft
delete is required. Do not ask about technical choices (naming, package
structure, framework configuration) — apply the conventions in this skill.

---

## Step 1 — Entity + Repository

Create the JPA entity and its repository before any other layer.

### Entity

```kotlin
@Entity
@Table(
    name = "orders",
    indexes = [Index(columnList = "customer_id")]
)
class Order(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID = UUID.randomUUID(),

    @Column(name = "customer_id", nullable = false)
    val customerId: UUID,

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    var status: OrderStatus = OrderStatus.PENDING,

    @Column(name = "total_amount", nullable = false)
    var totalAmount: BigDecimal,

    @Column(name = "created_at", nullable = false, updatable = false)
    @CreatedDate
    val createdAt: Instant = Instant.now(),

    @Column(name = "updated_at", nullable = false)
    @LastModifiedDate
    var updatedAt: Instant = Instant.now()
)

enum class OrderStatus { PENDING, CONFIRMED, CANCELLED }
```

Rules:

- `@Table` with explicit `name` — never rely on inferred table names
- `@Column(nullable = false)` always explicit for non-nullable fields
- UUID primary keys with `GenerationType.UUID`
- Enums stored as `STRING`, not `ORDINAL`
- Include `createdAt` and `updatedAt` on every entity
- Never use `data class` for JPA entities — Hibernate requires a no-arg
  constructor and mutable state; use `class`
- `equals`/`hashCode` based on `id` only, or omit (reference equality is safe
  for JPA entities when managed by the same `EntityManager`)

### Repository

```kotlin
@Repository
interface OrderRepository : JpaRepository<Order, UUID> {
    fun findByCustomerId(customerId: UUID): List<Order>
    fun findByCustomerIdAndStatus(customerId: UUID, status: OrderStatus): List<Order>
    fun existsByCustomerIdAndStatus(customerId: UUID, status: OrderStatus): Boolean

    @Query("SELECT o FROM Order o WHERE o.status = :status AND o.createdAt < :before")
    fun findExpiredPending(
        @Param("status") status: OrderStatus,
        @Param("before") before: Instant
    ): List<Order>
}
```

Rules:

- Extend `JpaRepository<Entity, IdType>`
- Use derived query method names for simple conditions
- Use `@Query` with JPQL for joins, multi-condition queries, or aggregations
- Use native SQL only when JPQL cannot express it — add `nativeQuery = true`
- Never add `@Transactional` to repository methods — Spring Data handles it

---

## Step 2 — Service

The service is the single owner of all business logic. No logic in controllers,
no logic in repositories.

```kotlin
@Service
@Transactional
class OrderService(
    private val orderRepository: OrderRepository,
    private val customerRepository: CustomerRepository
) {

    @Transactional(readOnly = true)
    fun getById(id: UUID): Order =
        orderRepository.findById(id).orElseThrow { OrderNotFoundException(id) }

    @Transactional(readOnly = true)
    fun listByCustomer(customerId: UUID): List<Order> =
        orderRepository.findByCustomerId(customerId)

    fun create(customerId: UUID, request: CreateOrderRequest): Order {
        if (!customerRepository.existsById(customerId)) {
            throw CustomerNotFoundException(customerId)
        }
        val order = Order(
            customerId = customerId,
            totalAmount = request.totalAmount
        )
        return orderRepository.save(order)
    }

    fun cancel(id: UUID): Order {
        val order = getById(id)
        if (order.status == OrderStatus.CANCELLED) {
            throw OrderAlreadyCancelledException(id)
        }
        order.status = OrderStatus.CANCELLED
        return orderRepository.save(order)
    }
}
```

Rules:

- `@Transactional` at class level — applies to all public methods
- `readOnly = true` on read-only methods — prevents dirty checking, faster
- Never put `@Transactional` on private methods — Spring proxies cannot
  intercept them
- Throw typed domain exceptions — never `RuntimeException` or `Exception`
  directly
- Never inject repositories into controllers — only services cross that boundary

### Domain Exceptions

Define typed exceptions per feature:

```kotlin
class OrderNotFoundException(id: UUID) :
    RuntimeException("Order not found: $id")

class OrderAlreadyCancelledException(id: UUID) :
    RuntimeException("Order already cancelled: $id")

class CustomerNotFoundException(id: UUID) :
    RuntimeException("Customer not found: $id")
```

---

## Step 3 — Controller + DTOs

The controller translates HTTP into service calls. No business logic here.

### DTOs

Define request and response data classes with Bean Validation annotations:

```kotlin
data class CreateOrderRequest(
    @field:NotNull(message = "totalAmount is required")
    @field:DecimalMin(value = "0.01", message = "totalAmount must be greater than zero")
    val totalAmount: BigDecimal
)

data class OrderResponse(
    val id: UUID,
    val customerId: UUID,
    val status: String,
    val totalAmount: BigDecimal,
    val createdAt: Instant
)

fun Order.toResponse(): OrderResponse = OrderResponse(
    id = id,
    customerId = customerId,
    status = status.name,
    totalAmount = totalAmount,
    createdAt = createdAt
)
```

Rules for DTOs:

- Use `data class` — immutable, no `@Entity`
- Use `@field:` prefix on validation annotations — Kotlin applies annotations to
  the property by default, not the backing field; Spring reads the field
- Define a `toResponse()` extension function on the entity — do not expose
  entities directly in responses
- Never return `@Entity` from a controller method

### Controller

```kotlin
@RestController
@RequestMapping("/api/v1/customers/{customerId}/orders")
class OrderController(private val orderService: OrderService) {

    @GetMapping
    fun list(@PathVariable customerId: UUID): ResponseEntity<List<OrderResponse>> =
        ResponseEntity.ok(orderService.listByCustomer(customerId).map { it.toResponse() })

    @GetMapping("/{id}")
    fun get(
        @PathVariable customerId: UUID,
        @PathVariable id: UUID
    ): ResponseEntity<OrderResponse> =
        ResponseEntity.ok(orderService.getById(id).toResponse())

    @PostMapping
    fun create(
        @PathVariable customerId: UUID,
        @Valid @RequestBody request: CreateOrderRequest
    ): ResponseEntity<OrderResponse> {
        val order = orderService.create(customerId, request)
        return ResponseEntity.status(HttpStatus.CREATED).body(order.toResponse())
    }

    @DeleteMapping("/{id}/cancel")
    fun cancel(
        @PathVariable customerId: UUID,
        @PathVariable id: UUID
    ): ResponseEntity<OrderResponse> =
        ResponseEntity.ok(orderService.cancel(id).toResponse())
}
```

Rules:

- One controller per feature
- `@Valid` on every `@RequestBody` parameter
- Return `ResponseEntity<T>` — explicit HTTP status control
- No try/catch in controllers — use `GlobalExceptionHandler`
- Map entities to DTOs before returning — never return the entity itself

### Global Exception Handler

Add new exception mappings to the existing `GlobalExceptionHandler`. Do not
create a second one.

```kotlin
@ControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(OrderNotFoundException::class)
    fun handleOrderNotFound(ex: OrderNotFoundException): ResponseEntity<ErrorResponse> =
        ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ErrorResponse(error = ex.message ?: "Order not found", code = "ORDER_NOT_FOUND"))

    @ExceptionHandler(OrderAlreadyCancelledException::class)
    fun handleAlreadyCancelled(ex: OrderAlreadyCancelledException): ResponseEntity<ErrorResponse> =
        ResponseEntity.status(HttpStatus.CONFLICT)
            .body(ErrorResponse(error = ex.message ?: "Order already cancelled", code = "ORDER_ALREADY_CANCELLED"))

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

---

## Step 4 — Tests with MockK

Write three test classes per feature: unit tests for the service, controller
tests with MockMvc, and repository tests with `@DataJpaTest`.

### Service Unit Tests (MockK)

```kotlin
@ExtendWith(MockKExtension::class)
class OrderServiceTest {

    @MockK
    private lateinit var orderRepository: OrderRepository

    @MockK
    private lateinit var customerRepository: CustomerRepository

    private lateinit var orderService: OrderService

    @BeforeEach
    fun setUp() {
        orderService = OrderService(orderRepository, customerRepository)
    }

    @Nested
    inner class Create {

        @Test
        fun `should create order when customer exists`() {
            // Arrange
            val customerId = UUID.randomUUID()
            val request = CreateOrderRequest(totalAmount = BigDecimal("99.99"))
            val savedOrder = Order(customerId = customerId, totalAmount = request.totalAmount)
            every { customerRepository.existsById(customerId) } returns true
            every { orderRepository.save(any()) } returns savedOrder

            // Act
            val result = orderService.create(customerId, request)

            // Assert
            assertThat(result.customerId).isEqualTo(customerId)
            assertThat(result.totalAmount).isEqualByComparingTo(BigDecimal("99.99"))
            verify(exactly = 1) { orderRepository.save(any()) }
        }

        @Test
        fun `should throw CustomerNotFoundException when customer does not exist`() {
            // Arrange
            val customerId = UUID.randomUUID()
            every { customerRepository.existsById(customerId) } returns false

            // Act + Assert
            assertThrows<CustomerNotFoundException> {
                orderService.create(customerId, CreateOrderRequest(totalAmount = BigDecimal("10.00")))
            }
            verify(exactly = 0) { orderRepository.save(any()) }
        }
    }

    @Nested
    inner class Cancel {

        @Test
        fun `should cancel order when status is PENDING`() {
            // Arrange
            val order = Order(customerId = UUID.randomUUID(), totalAmount = BigDecimal("50.00"))
            every { orderRepository.findById(order.id) } returns Optional.of(order)
            every { orderRepository.save(any()) } answers { firstArg() }

            // Act
            val result = orderService.cancel(order.id)

            // Assert
            assertThat(result.status).isEqualTo(OrderStatus.CANCELLED)
        }

        @Test
        fun `should throw OrderAlreadyCancelledException when order is already cancelled`() {
            // Arrange
            val order = Order(
                customerId = UUID.randomUUID(),
                totalAmount = BigDecimal("50.00"),
                status = OrderStatus.CANCELLED
            )
            every { orderRepository.findById(order.id) } returns Optional.of(order)

            // Act + Assert
            assertThrows<OrderAlreadyCancelledException> {
                orderService.cancel(order.id)
            }
        }
    }
}
```

### Controller Tests (MockMvc)

```kotlin
@SpringBootTest
@AutoConfigureMockMvc
class OrderControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockkBean
    private lateinit var orderService: OrderService

    @Test
    fun `should return 201 and created order when request is valid`() {
        // Arrange
        val customerId = UUID.randomUUID()
        val order = Order(customerId = customerId, totalAmount = BigDecimal("99.99"))
        every { orderService.create(customerId, any()) } returns order

        val body = mapOf("totalAmount" to "99.99")

        // Act + Assert
        mockMvc.perform(
            post("/api/v1/customers/$customerId/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.customerId").value(customerId.toString()))
            .andExpect(jsonPath("$.status").value("PENDING"))
    }

    @Test
    fun `should return 400 when totalAmount is missing`() {
        mockMvc.perform(
            post("/api/v1/customers/${UUID.randomUUID()}/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}")
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
    }

    @Test
    fun `should return 404 when order is not found`() {
        val customerId = UUID.randomUUID()
        val orderId = UUID.randomUUID()
        every { orderService.getById(orderId) } throws OrderNotFoundException(orderId)

        mockMvc.perform(get("/api/v1/customers/$customerId/orders/$orderId"))
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.code").value("ORDER_NOT_FOUND"))
    }
}
```

### Repository Tests

```kotlin
@DataJpaTest
class OrderRepositoryTest {

    @Autowired
    private lateinit var orderRepository: OrderRepository

    @Test
    fun `should find orders by customer id`() {
        // Arrange
        val customerId = UUID.randomUUID()
        orderRepository.save(Order(customerId = customerId, totalAmount = BigDecimal("10.00")))
        orderRepository.save(Order(customerId = UUID.randomUUID(), totalAmount = BigDecimal("20.00")))

        // Act
        val result = orderRepository.findByCustomerId(customerId)

        // Assert
        assertThat(result).hasSize(1)
        assertThat(result.first().customerId).isEqualTo(customerId)
    }
}
```

MockK rules:

- Use `@ExtendWith(MockKExtension::class)` — not `@MockBean` for unit tests
- Use `@MockkBean` (MockK Spring integration) only in `@SpringBootTest` context
- `every { }` for stubbing, `verify { }` for verification
- `answers { firstArg() }` to return the argument passed to `save()`
- `verify(exactly = 0) { }` to assert a method was never called
- Never use `relaxed = true` unless you genuinely do not care about any return
  value
- Test names: `should [expected outcome] when [condition]`
- Test structure: Arrange / Act / Assert, separated by blank lines

---

## Step 5 — When to Ask

Only pause to ask when there is genuine ambiguity in business logic. Do not ask
about technical choices — apply the conventions in this skill.

**Ask when:**

- The feature description does not specify what happens on conflict (duplicate,
  constraint violation, concurrent modification)
- It is unclear whether delete means hard delete or soft delete
- Authorization rules are not specified and cannot be inferred from context
- A domain invariant is referenced but not defined (e.g., "validate the order"
  without specifying which fields or rules)

**Do not ask about:**

- Package structure — always feature-oriented MVC
- Naming conventions — follow the existing codebase
- Whether to use MockK or Mockito — always MockK
- Whether to add `@Transactional` — always at the service class level
- Whether to validate at controller or service — always at DTO layer

---

## Package Structure

Always feature-oriented. One package per feature, not one package per layer.

```
src/main/kotlin/{base-package}/{feature}/
  controller/     # HTTP only — OrderController, DTOs, toResponse() extensions
  service/        # Business logic — OrderService, domain exceptions
  repository/     # Data access — OrderRepository
  domain/         # JPA entities — Order, OrderStatus

src/test/kotlin/{base-package}/{feature}/
  controller/     # OrderControllerTest (MockMvc)
  service/        # OrderServiceTest (MockK)
  repository/     # OrderRepositoryTest (@DataJpaTest)
```

Never create `src/main/kotlin/controllers/`, `src/main/kotlin/services/`, etc.
Layer-first structure does not scale and breaks feature cohesion.
