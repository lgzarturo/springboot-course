---
id: api-versioning
name: api-versioning
description: >
  Provides expert knowledge for designing, implementing, and managing REST API
  versioning strategies with deprecation workflows.

user-invokable: true
license: MIT
metadata:
  author: lgzarturo
  category: api

compatibility:
  tools: [claude, codex, gemini, agy, opencode]
  stacks:
    languages: [kotlin, java, typescript, python, go]
    frameworks: [spring-boot, spring-mvc, express, fastapi]

risk:
  level: high
  can_execute_shell: false
  can_modify_files: true
  requires_network: false

inputs:
  - source_files
  - openapi spec files
  - existing controller classes

outputs:
  - versioned controller classes
  - OpenAPI spec updates
  - deprecation headers
  - changelog entries
  - contract test scaffolding

quality:
  reviewed_by: codeconductor-core
  version: 0.1.0
---

# API Versioning

## Versioning Strategies

### URL Path Versioning (recommended for breaking changes)

```text
GET /api/v1/users
GET /api/v2/users
```

Tradeoffs:

- Explicit and visible in logs, proxies, and browser history
- Easy to cache at the CDN level — the URL uniquely identifies the resource
  version
- Easy to route at the load balancer
- Results in some duplication of controller code
- Changing the URL violates REST HATEOAS principles, though in practice this is
  acceptable

Use this when: you have breaking changes and need maximum visibility and
cacheability.

### Header Versioning

```text
GET /api/users
Accept: application/vnd.myapp+json;version=1
```

Tradeoffs:

- Cleaner URLs
- Harder to test manually — browsers and curl require extra flags
- Cannot be bookmarked or linked directly
- CDN caching requires `Vary: Accept` header, which reduces cache hit rates

Use this when: you need clean URLs and your clients are all programmatic (no
browsers).

### Query Parameter Versioning (avoid)

```text
GET /api/users?version=1
```

This approach contaminates resource URLs with transport concerns. The version is
not part of the resource identity. Do not use it. The only valid exception is
temporary backward-compat support during a migration window.

## When to Version

Version when the change is breaking. Not every change requires a version bump.

**Breaking — requires new version:**

- Removing a field from a response
- Renaming a field
- Changing a field's type (e.g., `string` to `object`)
- Changing the meaning of an existing field
- Removing an endpoint
- Changing required fields in a request
- Changing status codes in a non-additive way

**Not breaking — no version bump needed:**

- Adding an optional field to a response
- Adding a new endpoint
- Adding an optional request parameter
- Deprecating a field (marking it, but still returning it)
- Performance improvements
- Bug fixes that restore documented behavior

## Deprecation Process

When a version or endpoint is being phased out, follow this process:

**Step 1: Mark in OpenAPI.**

```yaml
paths:
  /api/v1/users/{id}:
    get:
      deprecated: true
      description: |
        Deprecated since 2026-05-07. Use /api/v2/users/{id} instead.
        Sunset date: 2026-11-07.
      summary: Get user by ID (deprecated)
```

**Step 2: Add deprecation headers to responses.**

```kotlin
@GetMapping("/{id}")
fun getUserV1(@PathVariable id: UUID, response: HttpServletResponse): ResponseEntity<UserV1Response> {
    response.addHeader("Deprecation", "date=\"Wed, 07 May 2026 00:00:00 GMT\"")
    response.addHeader("Sunset", "Mon, 07 Nov 2026 00:00:00 GMT")
    response.addHeader("Link", "</api/v2/users/$id>; rel=\"successor-version\"")
    return ResponseEntity.ok(userService.getById(id).toV1Response())
}
```

**Step 3: Document in CHANGELOG.**

```markdown
## Deprecated

- `GET /api/v1/users/{id}` — deprecated in favor of `GET /api/v2/users/{id}`.
  Sunset: 2026-11-07.
```

**Step 4: Maintain dual support.**

Keep at least two active major versions at all times. When v3 ships, v1 can be
removed (v2 and v3 remain active).

**Step 5: Communicate the sunset date.**

Notify consumers before the sunset date through:

- API changelog
- Developer portal announcements
- Deprecation headers (machine-readable)
- Direct contact if you have consumer registration data

Do not remove a version without a minimum 6-month notice period. 3 months is the
absolute minimum if forced.

## OpenAPI Conventions

### One file per version (simple cases)

```text
openapi-v1.yaml
openapi-v2.yaml
```

Each file is self-contained and independently valid.

### Single file with version in info (evolving APIs)

```yaml
openapi: '3.1.0'
info:
  title: Users API
  version: '2.0.0'
```

Use `$ref` to share schemas across versions without duplication.

### Cross-version schema reuse

```yaml
# schemas/user-base.yaml
UserBase:
  type: object
  properties:
    id:
      type: string
      format: uuid
    email:
      type: string

# openapi-v1.yaml
components:
  schemas:
    UserResponse:
      allOf:
        - $ref: './schemas/user-base.yaml#/UserBase'
        - properties:
            full_name:
              type: string

# openapi-v2.yaml — splits full_name into first_name + last_name
components:
  schemas:
    UserResponse:
      allOf:
        - $ref: './schemas/user-base.yaml#/UserBase'
        - properties:
            first_name:
              type: string
            last_name:
              type: string
```

### Documenting breaking changes

Put the breaking change in the endpoint description, not just in a changelog:

```yaml
/api/v2/users/{id}:
  get:
    description: |
      Returns user details.

      Breaking changes from v1:
      - `full_name` has been replaced by `first_name` and `last_name`
```

## Spring Boot Implementation

### URL path versioning

```kotlin
// V1 controller — never modify once published
@RestController
@RequestMapping("/api/v1/users")
class UserV1Controller(private val userService: UserService) {

    @GetMapping("/{id}")
    @Deprecated("Use /api/v2/users/{id}", ReplaceWith("UserV2Controller.getUser()"))
    fun getUser(
        @PathVariable id: UUID,
        response: HttpServletResponse
    ): ResponseEntity<UserV1Response> {
        response.addHeader("Deprecation", "date=\"Wed, 07 May 2026 00:00:00 GMT\"")
        response.addHeader("Sunset", "Mon, 07 Nov 2026 00:00:00 GMT")
        return when (val result = userService.getById(id)) {
            is UserResult.Found -> ResponseEntity.ok(result.user.toV1Response())
            is UserResult.NotFound -> ResponseEntity.notFound().build()
        }
    }
}

// V2 controller — new version, new controller, shared service
@RestController
@RequestMapping("/api/v2/users")
class UserV2Controller(private val userService: UserService) {

    @GetMapping("/{id}")
    fun getUser(@PathVariable id: UUID): ResponseEntity<UserV2Response> {
        return when (val result = userService.getById(id)) {
            is UserResult.Found -> ResponseEntity.ok(result.user.toV2Response())
            is UserResult.NotFound -> ResponseEntity.notFound().build()
        }
    }
}
```

Rules:

- Create a new controller for each new version — do not modify the existing one
- The service layer is shared across versions — only the controller and DTO
  change
- DTO mapper functions are version-specific: `toV1Response()`, `toV2Response()`
- Never delete a versioned controller until after the sunset date

### DTO versioning

```kotlin
// V1 — original shape
data class UserV1Response(
    val id: UUID,
    val email: String,
    val full_name: String
)

// V2 — breaking change: split full_name
data class UserV2Response(
    val id: UUID,
    val email: String,
    val first_name: String,
    val last_name: String
)

// Extension functions for mapping
fun User.toV1Response(): UserV1Response = UserV1Response(
    id = id,
    email = email,
    full_name = "$firstName $lastName"
)

fun User.toV2Response(): UserV2Response = UserV2Response(
    id = id,
    email = email,
    first_name = firstName,
    last_name = lastName
)
```

## Contract Testing

Contract tests verify that your API does not break existing consumers before
changes reach production.

**When to run:** in CI, before merging any change that touches a controller,
DTO, or OpenAPI spec.

**Tool: Pact (consumer-driven contracts)**

Consumer writes a pact:

```kotlin
// In the consumer service test
@ExtendWith(PactConsumerTestExt::class)
class UserServiceConsumerTest {

    @Pact(consumer = "order-service", provider = "user-service")
    fun getUserPact(builder: PactDslWithProvider): RequestResponsePact {
        return builder
            .given("user with id exists")
            .uponReceiving("a request for user by id")
            .path("/api/v1/users/123e4567-e89b-12d3-a456-426614174000")
            .method("GET")
            .willRespondWith()
            .status(200)
            .body(LambdaDsl.newJsonBody { body ->
                body.uuid("id")
                body.stringType("email")
                body.stringType("full_name")
            }.build())
            .toPact()
    }
}
```

Provider verifies the pact:

```kotlin
@Provider("user-service")
@PactFolder("pacts")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UserServiceProviderTest {

    @TestTarget
    lateinit var target: HttpTestTarget

    @BeforeEach
    fun setUp(@LocalServerPort port: Int) {
        target = HttpTestTarget("localhost", port)
    }
}
```

**Rule:** run contract tests in CI before any merge that touches an API surface.
A broken contract test means a consumer will break in production.

## Test Structure Per Version

Each API version must have its own test class:

```text
src/test/kotlin/{package}/user/
  controller/
    UserV1ControllerTest.kt   # tests for v1 endpoints
    UserV2ControllerTest.kt   # tests for v2 endpoints
```

Do not share test cases across versions. V1 behavior must be tested
independently from V2 — they can diverge.
