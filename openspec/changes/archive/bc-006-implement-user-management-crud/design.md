# Design: Implement user management CRUD

## Approach

Keep the existing MVC-by-feature structure and the pure `User` domain model.
Add `spring-security-crypto` as the smallest BCrypt dependency without adding
the Spring Security filter chain reserved for BC-007.

## Design decisions

1. Add `UserEntity`, `UserJpaRepository`, and `UserRepositoryAdapter` directly
   to `features/users`. The adapter translates between JPA records and the
   existing pure domain model.
2. Extend `UserRepository` with `findById`, `findAll`, and `update` behavior.
   `CreateUserService`, `GetUserUseCase`, and `UpdateUserUseCase` become
   constructor-injected Spring services; read methods are read-only
   transactions and mutations use write transactions.
3. `PasswordEncoder` wraps `BCryptPasswordEncoder`. `Password.fromEncrypted`
   accepts only a BCrypt hash so a persisted password is never plain text.
4. Add `CreateUserRequest`, extend `UpdateUserRequest` for partial fields, and
   implement the controller endpoints. The controller maps HTTP requests into
   commands before calling the services and only returns `UserResponse`.
5. `V3__Add_Users_Table.sql` defines a unique email, the BCrypt hash column,
   role, and active state. Authorization for the list endpoint remains deferred
   to BC-007 and BC-008.
6. Add focused unit tests, `@WebMvcTest` coverage, and a `@DataJpaTest` for the
   email uniqueness and JPA mapping.

## Files Affected

`build.gradle.kts`, `src/main/kotlin/com/lgzarturo/springbootcourse/features/users/`, `src/main/resources/db/migration/V3__Add_Users_Table.sql`, `src/test/kotlin/com/lgzarturo/springbootcourse/features/users/`

## Acceptance Criteria

- `POST /api/v1/users` returns `201 Created` for valid input and never serializes a password
- A duplicate email returns `409 Conflict` and does not create a second user
- Passwords are persisted as BCrypt hashes and are never stored in plain text
- `GET /api/v1/users/{id}` returns `404 Not Found` for an unknown identifier
- `Email`, `Password`, and `PhoneNumber` reject invalid values through their domain invariants
- Unit, `@WebMvcTest`, and `@DataJpaTest` coverage exists for the implemented user flows

## Complexity Tracking

| Violation | Why needed | Simpler alternative rejected |
| --------- | ---------- | ---------------------------- |
| none | — | — |
