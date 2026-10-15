---
id: jpa-postgres
name: jpa-postgres
description: >
  Provides expert knowledge of JPA entity design, relationship mapping, Flyway
  migrations, and PostgreSQL-specific optimizations.

user-invokable: true
license: MIT
metadata:
  author: lgzarturo
  category: database

compatibility:
  tools: [claude, codex, gemini, agy, opencode]
  stacks:
    languages: [kotlin, java]
    frameworks: [spring-boot, spring-data-jpa, hibernate]
    databases: [postgresql]

risk:
  level: high
  can_execute_shell: false
  can_modify_files: true
  requires_network: false

inputs:
  - source_files
  - migration scripts
  - entity classes
  - repository interfaces

outputs:
  - JPA entity classes
  - repository interfaces
  - Flyway migration scripts
  - JPQL and native queries
  - integration test classes

quality:
  reviewed_by: codeconductor-core
  version: 0.1.0
---

# JPA + PostgreSQL

## Entity Design

### Primary Keys

Use UUIDs. Do not use auto-increment integers as public-facing identifiers.

```kotlin
@Entity
@Table(name = "users")
class User(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID = UUID.randomUUID(),

    @Column(name = "email", nullable = false, unique = true, length = 255)
    var email: String,

    @Column(name = "name", nullable = false, length = 100)
    var name: String
)
```

UUID generation strategy `GenerationType.UUID` is available in Hibernate 6+
(Spring Boot 3+). For earlier versions, use `@UuidGenerator` from Hibernate or
generate manually.

### Auditing

Enable automatic timestamp management with Spring Data auditing.

```kotlin
// Enable in main application class or a @Configuration class
@EnableJpaAuditing
@SpringBootApplication
class Application

// Base class for auditable entities
@MappedSuperclass
@EntityListeners(AuditingEntityListener::class)
abstract class AuditableEntity {

    @Column(name = "created_at", nullable = false, updatable = false)
    @CreatedDate
    lateinit var createdAt: Instant

    @Column(name = "updated_at", nullable = false)
    @LastModifiedDate
    lateinit var updatedAt: Instant
}

// Entity extends the base class
@Entity
@Table(name = "users")
class User(
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID = UUID.randomUUID(),

    @Column(name = "email", nullable = false, unique = true)
    var email: String,

    var name: String
) : AuditableEntity()
```

### Soft Delete

Do not delete rows — mark them as deleted and filter them transparently.

```kotlin
@Entity
@Table(name = "users")
@SQLDelete(sql = "UPDATE users SET deleted_at = NOW() WHERE id = ?")
@FilterDef(name = "deletedFilter", parameters = [ParamDef(name = "isDeleted", type = Boolean::class)])
@Filter(name = "deletedFilter", condition = "deleted_at IS NULL")
class User(
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID = UUID.randomUUID(),

    @Column(name = "email", nullable = false, unique = true)
    var email: String,

    @Column(name = "deleted_at")
    var deletedAt: Instant? = null
)
```

Alternative using `@Where` (simpler, but applies globally without ability to
disable):

```kotlin
@Entity
@Table(name = "users")
@SQLDelete(sql = "UPDATE users SET deleted_at = NOW() WHERE id = ?")
@Where(clause = "deleted_at IS NULL")
class User(...)
```

### Column Annotations

Always be explicit. Never rely on Hibernate defaults.

```kotlin
@Column(
    name = "email",           // explicit column name
    nullable = false,         // maps to NOT NULL constraint
    unique = true,            // maps to UNIQUE constraint
    length = 255              // VARCHAR(255) — ignored for TEXT type
)
var email: String
```

For PostgreSQL `TEXT` type (unbounded), use `columnDefinition`:

```kotlin
@Column(name = "description", nullable = false, columnDefinition = "TEXT")
var description: String
```

### Indexes

Define indexes on the entity, not in migration scripts, for searchable fields.

```kotlin
@Entity
@Table(
    name = "users",
    indexes = [
        Index(name = "idx_users_email", columnList = "email", unique = true),
        Index(name = "idx_users_created_at", columnList = "created_at")
    ]
)
class User(...)
```

For partial indexes (e.g., soft-delete), use a migration script — JPA cannot
express partial indexes.

## Relationship Mapping

### One-to-Many

```kotlin
@Entity
@Table(name = "orders")
class Order(
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID = UUID.randomUUID(),

    @OneToMany(
        mappedBy = "order",
        cascade = [CascadeType.ALL],
        fetch = FetchType.LAZY,   // ALWAYS lazy by default
        orphanRemoval = true
    )
    val items: MutableList<OrderItem> = mutableListOf()
)

@Entity
@Table(name = "order_items")
class OrderItem(
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID = UUID.randomUUID(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    val order: Order,

    @Column(name = "quantity", nullable = false)
    var quantity: Int
)
```

Rules:

- `fetch = FetchType.LAZY` is the default for `@OneToMany` — make it explicit
  anyway
- `CascadeType.ALL` only when the child lifecycle is completely owned by the
  parent
- `orphanRemoval = true` when removing an item from the collection should delete
  the row

### Many-to-One

```kotlin
@ManyToOne(fetch = FetchType.LAZY)   // LAZY — never EAGER
@JoinColumn(name = "user_id", nullable = false)
val user: User
```

`FetchType.EAGER` on `@ManyToOne` causes N+1 problems. Always use `LAZY` and
fetch eagerly with `JOIN FETCH` when needed.

### Many-to-Many

Do not use `@ManyToMany` directly. Use a join entity with explicit fields.

```kotlin
// bad — opaque join table, no room for additional fields
@ManyToMany
@JoinTable(name = "user_roles")
val roles: MutableSet<Role>

// good — explicit join entity
@Entity
@Table(name = "user_roles")
class UserRole(
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID = UUID.randomUUID(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    val role: Role,

    @Column(name = "assigned_at", nullable = false)
    val assignedAt: Instant = Instant.now()
)
```

The join entity approach is more flexible — you can add fields like
`assignedAt`, `assignedBy`, and query through a repository.

## N+1 Problem

The N+1 problem occurs when loading a collection results in one query for the
parent and N additional queries for each child, where N is the number of
parents.

**Detection:**

Enable SQL logging in tests:

```yaml
# application-test.yml
spring:
  jpa:
    show-sql: true
    properties:
      hibernate:
        format_sql: true
logging:
  level:
    org.hibernate.SQL: DEBUG
    org.hibernate.type.descriptor.sql.BasicBinder: TRACE
```

Count the queries. If you see `SELECT * FROM order_items WHERE order_id = ?`
executed once per order, you have N+1.

**Fix with `@EntityGraph`:**

```kotlin
@Repository
interface OrderRepository : JpaRepository<Order, UUID> {

    @EntityGraph(attributePaths = ["items", "items.product"])
    fun findWithItemsById(id: UUID): Optional<Order>

    @EntityGraph(attributePaths = ["items"])
    fun findAllWithItems(): List<Order>
}
```

**Fix with JPQL JOIN FETCH:**

```kotlin
@Query("SELECT o FROM Order o JOIN FETCH o.items WHERE o.id = :id")
fun findByIdWithItems(@Param("id") id: UUID): Optional<Order>

@Query("SELECT DISTINCT o FROM Order o JOIN FETCH o.items WHERE o.status = :status")
fun findByStatusWithItems(@Param("status") status: OrderStatus): List<Order>
```

Use `DISTINCT` with `JOIN FETCH` on collections to avoid duplicate parent rows
in the result.

**Rule:** never access a lazy collection outside a transaction. This causes
`LazyInitializationException`. If a service method needs related data, fetch it
within the same transaction using `@EntityGraph` or `JOIN FETCH`.

## Projections for Read Queries

When you only need a subset of fields, do not load the full entity. Use
projections.

**Interface projection:**

```kotlin
interface UserSummary {
    val id: UUID
    val email: String
    val name: String
}

@Repository
interface UserRepository : JpaRepository<User, UUID> {
    fun findAllProjectedBy(): List<UserSummary>
    fun findProjectedById(id: UUID): UserSummary?
}
```

**DTO projection with constructor expression:**

```kotlin
data class UserDto(val id: UUID, val email: String)

@Query("SELECT new com.example.user.dto.UserDto(u.id, u.email) FROM User u WHERE u.active = true")
fun findActiveUserDtos(): List<UserDto>
```

Use projections for list endpoints and reports. Load full entities only when you
need to modify them.

## Flyway Migrations

### Naming Convention

```
V{timestamp}__{description}.sql
```

Use a timestamp, not a sequential number, to avoid conflicts in parallel
branches.

```
V20260507120000__create_users_table.sql
V20260507120001__add_users_email_index.sql
V20260508090000__add_orders_table.sql
```

### Migration Rules

- Never modify a migration that has already been applied in any environment
- Each migration must be forward-only — Flyway does not support automatic
  rollbacks
- DDL changes and data migrations must be in separate files
- Destructive operations (DROP COLUMN, DROP TABLE) require a multi-step process:
  1. Migration 1: deploy code that no longer uses the column
  2. Migration 2: drop the column (safe once all instances are updated)

### Schema Migration Example

```sql
-- V20260507120000__create_users_table.sql
CREATE TABLE users (
    id          UUID        NOT NULL PRIMARY KEY,
    email       VARCHAR(255) NOT NULL,
    name        VARCHAR(100) NOT NULL,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at  TIMESTAMPTZ
);

CREATE UNIQUE INDEX idx_users_email ON users (email) WHERE deleted_at IS NULL;
```

### Rollback Strategy

Document rollback SQL in comments at the top of each migration file:

```sql
-- V20260507120001__add_user_role_column.sql
-- Rollback: ALTER TABLE users DROP COLUMN role;

ALTER TABLE users ADD COLUMN role VARCHAR(50) NOT NULL DEFAULT 'USER';
```

Automated rollback is not used. Manual rollback is applied only in emergencies.

## PostgreSQL Specifics

### Column Types

| Use case                        | PostgreSQL type | JPA mapping                           |
| ------------------------------- | --------------- | ------------------------------------- |
| Short text, known max length    | `VARCHAR(n)`    | `@Column(length = n)`                 |
| Long text, no known max         | `TEXT`          | `@Column(columnDefinition = "TEXT")`  |
| Structured semi-structured data | `JSONB`         | `@Column(columnDefinition = "JSONB")` |
| Timestamps with timezone        | `TIMESTAMPTZ`   | `Instant`                             |
| UUIDs                           | `UUID`          | `UUID`                                |
| Money/currency                  | `NUMERIC(19,4)` | `BigDecimal`                          |

Use `TIMESTAMPTZ` (with timezone), not `TIMESTAMP`. Store all timestamps in UTC.

### JSONB

Use `JSONB` for semi-structured data that does not warrant its own table.

```kotlin
@Column(name = "metadata", columnDefinition = "JSONB")
@Type(JsonType::class)   // requires hypersistence-utils dependency
var metadata: Map<String, Any> = emptyMap()
```

JSONB is indexable. JSON is not. Always use JSONB.

### Partial Indexes

Partial indexes cannot be expressed through JPA annotations. Use a migration:

```sql
-- For soft-deleted tables: only index active (non-deleted) rows
CREATE UNIQUE INDEX idx_users_email_active ON users (email) WHERE deleted_at IS NULL;

-- For status-filtered queries
CREATE INDEX idx_orders_pending ON orders (created_at) WHERE status = 'PENDING';
```

### Native Queries with RETURNING

Use `RETURNING` to avoid an extra `SELECT` after an `INSERT` or `UPDATE`:

```kotlin
@Modifying
@Query(
    value = "INSERT INTO audit_log (user_id, action, created_at) VALUES (:userId, :action, NOW()) RETURNING id",
    nativeQuery = true
)
fun insertAndReturnId(
    @Param("userId") userId: UUID,
    @Param("action") action: String
): UUID
```

### Complex Queries

Use native queries when JPQL cannot express what you need:

```kotlin
@Query(
    value = """
        SELECT u.id, u.email, COUNT(o.id) as order_count
        FROM users u
        LEFT JOIN orders o ON o.user_id = u.id AND o.deleted_at IS NULL
        WHERE u.deleted_at IS NULL
        GROUP BY u.id, u.email
        HAVING COUNT(o.id) > :minOrders
        ORDER BY order_count DESC
        LIMIT :limit
    """,
    nativeQuery = true
)
fun findActiveUsersWithMinOrders(
    @Param("minOrders") minOrders: Int,
    @Param("limit") limit: Int
): List<Map<String, Any>>
```

## Query Performance

### Pagination

Always paginate. Never call `findAll()` on a large table.

```kotlin
@Repository
interface UserRepository : JpaRepository<User, UUID> {
    fun findAll(pageable: Pageable): Page<UserSummary>
    fun findByRole(role: UserRole, pageable: Pageable): Page<UserSummary>
}

// In service
fun list(page: Int, size: Int): Page<UserSummary> {
    val pageable = PageRequest.of(page, size, Sort.by("createdAt").descending())
    return userRepository.findAll(pageable)
}
```

Use `Slice<T>` instead of `Page<T>` when you do not need the total count
(cheaper — no COUNT query).

### QueryHints for Fetch Control

```kotlin
@QueryHints(
    QueryHint(name = HINT_FETCHGRAPH, value = "User.withOrders")
)
fun findById(id: UUID): Optional<User>
```

Requires a named entity graph:

```kotlin
@Entity
@NamedEntityGraph(
    name = "User.withOrders",
    attributeNodes = [NamedAttributeNode("orders")]
)
class User(...)
```

## Testing

### Unit — @DataJpaTest with H2

Fast. No server required.

```kotlin
@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private lateinit var userRepository: UserRepository

    @Test
    fun `should find user by email`() {
        val saved = userRepository.save(User(email = "test@example.com", name = "Test User"))
        val found = userRepository.findByEmail("test@example.com")
        assertThat(found).isNotNull
        assertThat(found?.id).isEqualTo(saved.id)
    }

    @Test
    fun `should not find deleted user by email`() {
        val user = userRepository.save(User(email = "deleted@example.com", name = "Gone"))
        userRepository.delete(user)   // triggers @SQLDelete — sets deleted_at
        val found = userRepository.findByEmail("deleted@example.com")
        assertThat(found).isNull()
    }
}
```

### Integration — Testcontainers with PostgreSQL

Real database. Use for migrations, JSONB, partial indexes, and native queries.

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
        fun registerProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url", postgres::getJdbcUrl)
            registry.add("spring.datasource.username", postgres::getUsername)
            registry.add("spring.datasource.password", postgres::getPassword)
        }
    }

    @Autowired
    private lateinit var userRepository: UserRepository

    @Test
    fun `should enforce unique email constraint at db level`() {
        userRepository.save(User(email = "dup@example.com", name = "First"))
        assertThrows<DataIntegrityViolationException> {
            userRepository.save(User(email = "dup@example.com", name = "Second"))
        }
    }
}
```

Use Testcontainers for:

- Flyway migration validation
- PostgreSQL-specific features (JSONB, partial indexes, native queries)
- Constraint enforcement (unique, foreign key, check constraints)
- Query performance checks that differ between H2 and PostgreSQL

Use `@DataJpaTest` with H2 for:

- Derived query methods
- Simple CRUD operations
- JPQL queries with standard JPA behavior
