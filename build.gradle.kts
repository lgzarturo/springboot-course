import io.gitlab.arturbosch.detekt.Detekt
import java.util.Properties

plugins {
    kotlin("jvm") version "2.0.21"
    kotlin("plugin.spring") version "2.0.21"
    id("org.springframework.boot") version "3.5.6"
    id("io.spring.dependency-management") version "1.1.7"
    kotlin("plugin.jpa") version "2.0.21"

    // Sentry plugin
    id("io.sentry.jvm.gradle") version "5.12.1"

    // Herramientas de calidad y cobertura
    id("org.jlleitschuh.gradle.ktlint") version "13.1.0"
    id("io.gitlab.arturbosch.detekt") version "1.23.8"
    jacoco
}

group = "com.lgzarturo"
version = "0.0.2"
description = "springboot-course"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

sourceSets {
    main {
        java {
            srcDirs("src/main/kotlin")
        }
    }
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
}

// Load variables from .env for local development (fallback when OS env vars are not present)
val dotenv: Map<String, String> by lazy {
    val file = rootProject.file(".env")
    if (!file.exists()) {
        emptyMap()
    } else {
        val props = Properties()
        file.inputStream().use { stream ->
            props.load(stream)
        }
        props
            .stringPropertyNames()
            .associateWith { name -> props.getProperty(name) }
            .filterValues { it != null }
            .mapValues { it.value!! }
    }
}

extra["sentryVersion"] = "8.22.0"
extra["testcontainersVersion"] = "1.20.4"

val sentryAuthToken: String? = System.getenv("SENTRY_AUTH_TOKEN") ?: dotenv["SENTRY_AUTH_TOKEN"]

sentry {
    // Generates a JVM (Java, Kotlin, etc.) source bundle and uploads your source code to Sentry.
    // This enables source context, allowing you to see your source
    // code as part of your stack traces in Sentry.
    includeSourceContext = true

    org = "arthurolg-to"
    projectName = "springboot-course"

    // Prefer OS environment variable, then .env fallback for local dev
    sentryAuthToken?.let { token ->
        authToken = token
    }
}

dependencies {
    // Spring Boot Starters
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-web")
    // Kotlin
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    // OpenAPI/Swagger Documentation
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.14")
    // Monitoring
    implementation("io.sentry:sentry-spring-boot-starter-jakarta")
    implementation("io.sentry:sentry-logback")
    implementation("org.codehaus.janino:janino:3.1.8")
    implementation("io.micrometer:micrometer-registry-prometheus")
    // Development
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    developmentOnly("org.springframework.boot:spring-boot-docker-compose")
    // Database
    runtimeOnly("com.h2database:h2")
    runtimeOnly("org.postgresql:postgresql")
    // Flyway para migraciones de base de datos
    implementation("org.flywaydb:flyway-core")
    runtimeOnly("org.flywaydb:flyway-database-postgresql")
    // Annotation Processing
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    // Testing
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
    testImplementation("io.kotest:kotest-assertions-core:5.9.1")
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation("org.testcontainers:testcontainers")
    testImplementation("org.testcontainers:postgresql")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("io.mockk:mockk:1.13.8")
    testImplementation("com.ninja-squad:springmockk:4.0.2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

dependencyManagement {
    imports {
        mavenBom("io.sentry:sentry-bom:${property("sentryVersion")}")
        mavenBom("org.testcontainers:testcontainers-bom:${property("testcontainersVersion")}")
    }
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}

allOpen {
    annotation("jakarta.persistence.Entity")
    annotation("jakarta.persistence.MappedSuperclass")
    annotation("jakarta.persistence.Embeddable")
}

tasks.withType<Test> {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
    }
}

// --- KTLint ---
ktlint {
    version.set("1.7.1")
    android.set(false)
    outputColorName.set("RED")
    ignoreFailures.set(false)
}

// --- Detekt ---
detekt {
    toolVersion = "1.23.8"
    config.setFrom(files("$rootDir/config/detekt/detekt.yml"))
    buildUponDefaultConfig = true
    allRules = false
    autoCorrect = true // Habilita la corrección automática
}

tasks.withType<Detekt>().configureEach {
    jvmTarget = "21"
    reports {
        html.required.set(true)
        xml.required.set(true)
        txt.required.set(true)
        sarif.required.set(true)
        md.required.set(true)
    }
}

// --- JaCoCo ---
jacoco {
    toolVersion = "0.8.13"
}

tasks.test {
    useJUnitPlatform()
    finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
}

tasks.jacocoTestCoverageVerification {
    violationRules {
        rule {
            limit {
                minimum = "0.85".toBigDecimal()
            }
        }
    }
}

tasks.register<Exec>("codecovUpload") {
    commandLine("bash", "-c", "bash <(curl -s https://codecov.io/bash)")
}

// --- Version Management ---
tasks.register("printVersion") {
    group = "versioning"
    description = "Imprime la version actual del proyecto"
    doLast {
        println("Version actual: $version")
    }
}

tasks.register("whatsnext") {
    group = "versioning"
    description = "Simula el proximo release sin crear tags"
    doLast {
        val separator = "=".repeat(50)
        println(separator)
        println("SIMULACION DE PROXIMO RELEASE")
        println(separator)
        println("Version actual: $version")
        println()
        println("Para ver que version se generaria:")
        println("1. Revisa los commits desde el ultimo tag")
        println("2. Segun los tipos de commit (feat/fix/BREAKING CHANGE)")
        println("3. Se incrementara la version segun SemVer")
        println()
        println("Usa semantic-release en dry-run mode para ver detalles:")
        println("npx semantic-release --dry-run")
        println(separator)
    }
}

tasks.register("generateChangelog") {
    group = "versioning"
    description = "Genera el changelog basado en commits convencionales"
    doLast {
        val separator = "=".repeat(50)
        println(separator)
        println("GENERACION DE CHANGELOG")
        println(separator)
        println("El changelog se genera automaticamente en CI/CD")
        println("mediante semantic-release cuando se hace merge a main")
        println()
        println("Para generar localmente:")
        println("npx conventional-changelog -p conventionalcommits -i CHANGELOG.md -s")
        println(separator)
    }
}

tasks.register<JavaExec>("generateDDL") {
    group = "database"
    description = "Generate DDL scripts from JPA entities"

    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("com.lgzarturo.springbootcourse.SpringbootCourseApplicationKt")

    args =
        listOf(
            "--spring.profiles.active=generate-ddl",
            "--spring.main.web-application-type=none",
            "--spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration",
        )

    standardOutput = System.out
    errorOutput = System.err

    doFirst {
        delete("build/schema-create.sql", "build/schema-drop.sql")
        println("Generating DDL scripts...")
    }

    doLast {
        val createFile = file("build/schema-create.sql")
        val dropFile = file("build/schema-drop.sql")

        if (createFile.exists()) {
            println("\n✓ DDL generated successfully:")
            println("  - ${createFile.absolutePath}")
            println("  - ${dropFile.absolutePath}")
        } else {
            throw GradleException("Failed to generate DDL scripts")
        }
    }
}

tasks.register("createMigration") {
    group = "database"
    description = "Create new Flyway migration from generated DDL"

    dependsOn("generateDDL")

    doLast {
        val version = project.findProperty("migration_version")?.toString() ?: "1"
        val description = project.findProperty("description")?.toString() ?: "migration"
        val cleanDescription = description.replace(" ", "_").lowercase()
        val fileName = "V${version}__$cleanDescription.sql"

        val sourceFile = file("build/schema-create.sql")
        val targetDir = file("src/main/resources/db/migration")
        val targetFile = file("$targetDir/$fileName")

        if (!sourceFile.exists()) {
            throw GradleException("Source file not found: ${sourceFile.absolutePath}")
        }

        if (targetFile.exists()) {
            throw GradleException("Migration already exists: $fileName")
        }

        targetDir.mkdirs()

        val content = sourceFile.readText()
        val cleanedContent =
            content
                .replace(Regex("(?m)^\\s*--.*$"), "")
                .replace(Regex("\n{3,}"), "\n\n")
                .trim()

        targetFile.writeText(cleanedContent)

        println("\n✓ Migration created successfully:")
        println("  ${targetFile.absolutePath}")
        println("\nNext steps:")
        println("  1. Review and edit the migration file")
        println("  2. Add indexes, constraints, or data migrations")
        println("  3. Run: ./gradlew bootRun --args='--spring.profiles.active=dev'")
    }
}

tasks.register("diffMigration") {
    group = "database"
    description = "Generate incremental migration by comparing entities with current schema"

    doLast {
        println("⚠ Manual diff required:")
        println("  1. Run: ./gradlew generateDDL")
        println("  2. Compare build/schema-create.sql with your current database")
        println("  3. Create incremental migration manually")
        println("\nTip: Use a DB diff tool like:")
        println("  - DBeaver Schema Compare")
        println("  - pgAdmin Schema Diff")
        println("  - IntelliJ Database Tools")
    }
}

// --- Tareas de Calidad de Código ---
tasks.register("checkCodeStyle") {
    group = "verification"
    description = "Ejecuta ktlint y detekt para verificar el estilo del código"
    dependsOn("ktlintCheck", "detekt")
}

tasks.register("formatCode") {
    group = "formatting"
    description = "Aplica correcciones automáticas de ktlint y detekt"
    dependsOn("ktlintFormat", "detektAutoCorrect")
}

tasks.register<Detekt>("detektAutoCorrect") {
    group = "formatting"
    description = "Ejecuta detekt con auto-corrección habilitada"

    autoCorrect = true
    jvmTarget = "21"

    setSource(files("src/main/kotlin", "src/test/kotlin"))
    config.setFrom(files("$rootDir/config/detekt/detekt.yml"))
    buildUponDefaultConfig = true

    reports {
        html.required.set(true)
        xml.required.set(true)
        txt.required.set(true)
    }
}

tasks.register("codeQuality") {
    group = "verification"
    description = "Ejecuta todas las verificaciones de calidad: ktlint, detekt y tests con cobertura"
    dependsOn("ktlintCheck", "detekt", "test", "jacocoTestReport")
}

tasks.register("fixAll") {
    group = "formatting"
    description = "Aplica todas las correcciones automáticas disponibles"
    dependsOn("ktlintFormat", "detektAutoCorrect")
    doLast {
        val separator = "=".repeat(50)
        println(separator)
        println("✓ Correcciones aplicadas exitosamente")
        println(separator)
        println("Cambios aplicados:")
        println("  • ktlint: Formato de código")
        println("  • detekt: Correcciones de estilo")
        println()
        println("⚠ Recuerda revisar los cambios antes de hacer commit")
        println(separator)
    }
}

tasks.register("lintReport") {
    group = "verification"
    description = "Genera reportes completos de análisis de código"
    dependsOn("ktlintCheck", "detekt")
    doLast {
        println("\n✓ Reportes generados en:")
        println("  • ktlint: build/reports/ktlint/")
        println("  • detekt: build/reports/detekt/")
    }
}
