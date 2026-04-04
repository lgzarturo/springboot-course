# Development Guide for Agents

This guide provides project-specific information for developers and automated agents working on the `springboot-course` project.

## 1. Build and Configuration Instructions

### Prerequisites
- **Java 21**: The project uses the Java 21 toolchain.
- **Docker**: Required for local database services and Testcontainers.

### Local Setup
1. **Environment Variables**: Create a `.env` file in the root directory based on `.env.example` (if available) or with the following variables:
   ```env
   DB_NAME=springboot_db
   DB_USERNAME=postgres
   DB_PASSWORD=postgres
   DB_PORT=5432
   SENTRY_AUTH_TOKEN=your_token_here (optional)
   ```
2. **Infrastructure**: Start the local PostgreSQL database using Docker Compose:
   ```bash
   docker-compose up -d
   ```
3. **Build**: Use the Gradle wrapper to build the project:
   ```bash
   ./gradlew build
   ```

## 2. Testing Information

### Configuration
Tests are configured to use the `test` profile. The project uses **JUnit 5**, **Kotest Assertions**, **MockK**, and **Testcontainers** for integration tests.

### Running Tests
- **All tests**: `./gradlew test`
- **Single test class**: `./gradlew test --tests "com.package.ClassName"`
- **Specific test method**: `./gradlew test --tests "com.package.ClassName.methodName"`

### Adding New Tests
1. **Location**: Place unit tests in `src/test/kotlin` following the package structure of the code under test.
2. **Style**: Use descriptive names (backticks allowed in Kotlin). Prefer Kotest matchers (`shouldBe`, `shouldNotBe`).
3. **Integration Tests**: Annotate with `@SpringBootTest` and use `@ActiveProfiles("test")`. Use `@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)` or Testcontainers for database-related tests.

### Sample Test
```kotlin
package com.lgzarturo.springbootcourse.example

import org.junit.jupiter.api.Test
import io.kotest.matchers.shouldBe

class SimpleSmokeTest {
    @Test
    fun `should perform simple addition`() {
        (1 + 1) shouldBe 2
    }
}
```

## 3. Additional Development Information

### Code Style and Quality
The project enforces code style using **ktlint** and **detekt**.
- **Check quality**: `./gradlew checkCodeStyle` (runs ktlint and detekt).
- **Auto-format**: `./gradlew formatCode` or `./gradlew fixAll`.
- **Full report**: `./gradlew codeQuality` (runs style checks, tests, and JaCoCo coverage).

### Key Tasks
- `ktlintFormat`: Automatically fix ktlint style issues.
- `detektAutoCorrect`: Automatically fix detekt issues.
- `jacocoTestReport`: Generates coverage reports in `build/reports/jacoco/`.

### Architecture Note
The project follows an hexagonal-like architecture (Adapters, Domain, etc.). Ensure that domain logic remains decoupled from infrastructure details.
