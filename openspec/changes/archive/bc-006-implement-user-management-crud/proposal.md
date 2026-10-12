# Proposal: Implement user management CRUD

## Why

Complete user persistence, application services, and REST endpoints so trainers can register, view, update, deactivate, and list users without exposing passwords.

**Business value:** Provides the account foundation required by authentication and reservations.

## What Changes

- `build.gradle.kts`, `src/main/kotlin/com/lgzarturo/springbootcourse/features/users/`, `src/main/resources/db/migration/V3__Add_Users_Table.sql`, `src/test/kotlin/com/lgzarturo/springbootcourse/features/users/`

## Capabilities

- **New Capabilities:** Implement user management CRUD
- **Modified Capabilities:** none

## Impact

Risks: Existing value objects and Flyway version ordering must remain compatible.

**Out of scope:** JWT authentication, roles, authorities, and user-facing administration UI
