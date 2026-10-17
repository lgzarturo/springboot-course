# Implementation Tasks

## Setup

- [ ] Confirm scope and OpenSpec change folder

## Foundational

- [ ] Read existing conventions in scope

## Requirements

- [ ] Write failing test for FR-001 (`POST /api/v1/users` returns `201 Created` for valid input and never serializes a password)
- [ ] Implement FR-001 (`POST /api/v1/users` returns `201 Created` for valid input and never serializes a password)
- [ ] Verify SC-001
- [ ] Write failing test for FR-002 (A duplicate email returns `409 Conflict` and does not create a second user)
- [ ] Implement FR-002 (A duplicate email returns `409 Conflict` and does not create a second user)
- [ ] Verify SC-002
- [ ] Write failing test for FR-003 (Passwords are persisted as BCrypt hashes and are never stored in plain text)
- [ ] Implement FR-003 (Passwords are persisted as BCrypt hashes and are never stored in plain text)
- [ ] Verify SC-003
- [ ] Write failing test for FR-004 (`GET /api/v1/users/{id}` returns `404 Not Found` for an unknown identifier)
- [ ] Implement FR-004 (`GET /api/v1/users/{id}` returns `404 Not Found` for an unknown identifier)
- [ ] Verify SC-004
- [ ] Write failing test for FR-005 (`Email`, `Password`, and `PhoneNumber` reject invalid values through their domain invariants)
- [ ] Implement FR-005 (`Email`, `Password`, and `PhoneNumber` reject invalid values through their domain invariants)
- [ ] Verify SC-005
- [ ] Write failing test for FR-006 (Unit, `@WebMvcTest`, and `@DataJpaTest` coverage exists for the implemented user flows)
- [ ] Implement FR-006 (Unit, `@WebMvcTest`, and `@DataJpaTest` coverage exists for the implemented user flows)
- [ ] Verify SC-006

## Phase cards

- [x] BC-006: Test coverage (BC-006-test)
- [x] BC-006: Implementation (BC-006-implement)

## Polish

- [ ] Run openspec analyze and scorecard create --from-diff