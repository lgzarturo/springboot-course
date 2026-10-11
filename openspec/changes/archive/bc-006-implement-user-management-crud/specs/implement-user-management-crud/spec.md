# Delta Spec: Implement user management CRUD

## ADDED Requirements

### Requirement: FR-001 `POST /api/v1/users` returns `201 Created` for valid input and never serializes a password

The system MUST `POST /api/v1/users` returns `201 Created` for valid input and never serializes a password.

#### Scenario: SC-001 `POST /api/v1/users` returns `201 Created` for valid input and never serializes a password

- GIVEN the current behavior in `build.gradle.kts`, `src/main/kotlin/com/lgzarturo/springbootcourse/features/users/`, `src/main/resources/db/migration/V3__Add_Users_Table.sql`, `src/test/kotlin/com/lgzarturo/springbootcourse/features/users/`
- WHEN this change is applied
- THEN `POST /api/v1/users` returns `201 Created` for valid input and never serializes a password

### Requirement: FR-002 A duplicate email returns `409 Conflict` and does not create a second user

The system MUST A duplicate email returns `409 Conflict` and does not create a second user.

#### Scenario: SC-002 A duplicate email returns `409 Conflict` and does not create a second user

- GIVEN the current behavior in `build.gradle.kts`, `src/main/kotlin/com/lgzarturo/springbootcourse/features/users/`, `src/main/resources/db/migration/V3__Add_Users_Table.sql`, `src/test/kotlin/com/lgzarturo/springbootcourse/features/users/`
- WHEN this change is applied
- THEN A duplicate email returns `409 Conflict` and does not create a second user

### Requirement: FR-003 Passwords are persisted as BCrypt hashes and are never stored in plain text

The system MUST Passwords are persisted as BCrypt hashes and are never stored in plain text.

#### Scenario: SC-003 Passwords are persisted as BCrypt hashes and are never stored in plain text

- GIVEN the current behavior in `build.gradle.kts`, `src/main/kotlin/com/lgzarturo/springbootcourse/features/users/`, `src/main/resources/db/migration/V3__Add_Users_Table.sql`, `src/test/kotlin/com/lgzarturo/springbootcourse/features/users/`
- WHEN this change is applied
- THEN Passwords are persisted as BCrypt hashes and are never stored in plain text

### Requirement: FR-004 `GET /api/v1/users/{id}` returns `404 Not Found` for an unknown identifier

The system MUST `GET /api/v1/users/{id}` returns `404 Not Found` for an unknown identifier.

#### Scenario: SC-004 `GET /api/v1/users/{id}` returns `404 Not Found` for an unknown identifier

- GIVEN the current behavior in `build.gradle.kts`, `src/main/kotlin/com/lgzarturo/springbootcourse/features/users/`, `src/main/resources/db/migration/V3__Add_Users_Table.sql`, `src/test/kotlin/com/lgzarturo/springbootcourse/features/users/`
- WHEN this change is applied
- THEN `GET /api/v1/users/{id}` returns `404 Not Found` for an unknown identifier

### Requirement: FR-005 `Email`, `Password`, and `PhoneNumber` reject invalid values through their domain invariants

The system MUST `Email`, `Password`, and `PhoneNumber` reject invalid values through their domain invariants.

#### Scenario: SC-005 `Email`, `Password`, and `PhoneNumber` reject invalid values through their domain invariants

- GIVEN the current behavior in `build.gradle.kts`, `src/main/kotlin/com/lgzarturo/springbootcourse/features/users/`, `src/main/resources/db/migration/V3__Add_Users_Table.sql`, `src/test/kotlin/com/lgzarturo/springbootcourse/features/users/`
- WHEN this change is applied
- THEN `Email`, `Password`, and `PhoneNumber` reject invalid values through their domain invariants

### Requirement: FR-006 Unit, `@WebMvcTest`, and `@DataJpaTest` coverage exists for the implemented user flows

The system MUST Unit, `@WebMvcTest`, and `@DataJpaTest` coverage exists for the implemented user flows.

#### Scenario: SC-006 Unit, `@WebMvcTest`, and `@DataJpaTest` coverage exists for the implemented user flows

- GIVEN the current behavior in `build.gradle.kts`, `src/main/kotlin/com/lgzarturo/springbootcourse/features/users/`, `src/main/resources/db/migration/V3__Add_Users_Table.sql`, `src/test/kotlin/com/lgzarturo/springbootcourse/features/users/`
- WHEN this change is applied
- THEN Unit, `@WebMvcTest`, and `@DataJpaTest` coverage exists for the implemented user flows

