plugins {
    kotlin("jvm") version "2.0.21"
    kotlin("plugin.spring") version "2.0.21"
    id("org.springframework.boot") version "3.5.6"
    id("io.spring.dependency-management") version "1.1.7"
    kotlin("plugin.jpa") version "2.0.21"

    // Herramientas de calidad y cobertura
    id("org.jlleitschuh.gradle.ktlint") version "13.1.0"
    id("io.gitlab.arturbosch.detekt") version "1.23.8"
    jacoco
}

group = "com.lgzarturo"
version = "0.0.1"
description = "springboot-course"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
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

extra["sentryVersion"] = "8.16.0"

dependencies {
    // Spring Boot Starters
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-web")
    // Kotlin
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    // OpenAPI/Swagger Documentation
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.3.0")
    // Monitoring
    implementation("io.sentry:sentry-spring-boot-starter-jakarta")
    implementation("io.micrometer:micrometer-registry-prometheus")
    // Development
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    // Database
    runtimeOnly("com.h2database:h2")
    runtimeOnly("org.postgresql:postgresql")
    // Annotation Processing
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    // Testing
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("io.mockk:mockk:1.13.8")
    testImplementation("com.ninja-squad:springmockk:4.0.2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

dependencyManagement {
    imports {
        mavenBom("io.sentry:sentry-bom:${property("sentryVersion")}")
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
