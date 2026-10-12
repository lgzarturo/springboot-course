import dev.detekt.gradle.Detekt
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.util.Properties
import java.time.LocalDate
import java.io.File


plugins {
    val kotlinVersion = "2.2.20"
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.spring") version kotlinVersion
    id("org.springframework.boot") version "4.1.1"
    id("io.spring.dependency-management") version "1.1.7"
    kotlin("plugin.jpa") version kotlinVersion
    kotlin("kapt") version kotlinVersion

    // Sentry plugin
    id("io.sentry.jvm.gradle") version "5.12.2"

    // Herramientas de calidad y cobertura
    id("org.jlleitschuh.gradle.ktlint") version "14.1.0"
    id("dev.detekt") version ("2.0.0-alpha.1")
    jacoco
}

group = "com.lgzarturo"
version = "0.1.0"
description = "springboot-course"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
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
    sentryAuthToken?.takeIf { it.isNotBlank() && !it.startsWith("{") }?.let { token ->
        authToken = token
    }
}

dependencies {
    // Spring Boot Starters
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-flyway")
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-restclient")
    implementation("org.springframework.security:spring-security-crypto")
    // JWT (BC-007)
    implementation("io.jsonwebtoken:jjwt-api:0.12.6")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.6")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.6")

    // Kotlin
    implementation("tools.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    // OpenAPI/Swagger Documentation
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:3.0.2")
    // Monitoring
    implementation("io.sentry:sentry-spring-boot-4-starter")
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
    implementation("org.flywaydb:flyway-database-postgresql")
    implementation("org.apache.httpcomponents.client5:httpclient5")
    // Annotation Processing
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    // Testing
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-starter-data-jpa-test")
    testImplementation("org.springframework.boot:spring-boot-starter-flyway-test")
    testImplementation("org.springframework.boot:spring-boot-starter-validation-test")
    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation("org.springframework.boot:spring-boot-starter-restclient-test")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("org.testcontainers:testcontainers-junit-jupiter")
    testImplementation("org.testcontainers:testcontainers-postgresql")
    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
    testImplementation("io.kotest:kotest-assertions-core:5.9.1")
    testImplementation("io.mockk:mockk:1.13.8")
    testImplementation("com.ninja-squad:springmockk:4.0.2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
}

dependencyManagement {
    imports {
        mavenBom("io.sentry:sentry-bom:${property("sentryVersion")}")
        mavenBom("org.testcontainers:testcontainers-bom:${property("testcontainersVersion")}")
    }
}

kapt {
    keepJavacAnnotationProcessors = true
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
        freeCompilerArgs.addAll("-Xjsr305=strict", "-Xannotation-default-target=param-property")
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(21)
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
    toolVersion = "2.0.0-alpha.1"
    config.setFrom(files("$rootDir/config/detekt/detekt.yml"))
    buildUponDefaultConfig = true
    allRules = false
    autoCorrect = true // Habilita la corrección automática
}

tasks.withType<Detekt>().configureEach {
    jvmTarget = "21"
    reports {
        html.required.set(true)
        // xml.required.set(true)
        // txt.required.set(true)
        sarif.required.set(true)
        // md.required.set(true)
    }
}

// --- JaCoCo ---
jacoco {
    toolVersion = "0.8.15"
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
fun calculateNextSemVer(current: String, type: String): String {
    val clean = current.removePrefix("v").trim()
    val base = clean.substringBefore("-").substringBefore("+")
    val parts = base.split(".")
    val major = parts.getOrNull(0)?.toIntOrNull() ?: 0
    val minor = parts.getOrNull(1)?.toIntOrNull() ?: 0
    val patch = parts.getOrNull(2)?.toIntOrNull() ?: 0

    return when (type.lowercase().trim()) {
        "major" -> "${major + 1}.0.0"
        "minor" -> "$major.${minor + 1}.0"
        "patch" -> "$major.$minor.${patch + 1}"
        else -> throw IllegalArgumentException(
            "Tipo de incremento no válido: '$type'. Valores permitidos: 'major', 'minor', 'patch'",
        )
    }
}

fun updateVersionInProject(oldVer: String, newVer: String) {
    println("Sincronizando versión de '$oldVer' a '$newVer' en archivos del proyecto:")

    // 1. build.gradle.kts
    val buildFile = file("build.gradle.kts")
    if (buildFile.exists()) {
        val content = buildFile.readText()
        val regex = Regex("""version\s*=\s*"$oldVer"""")
        if (regex.containsMatchIn(content)) {
            buildFile.writeText(content.replace(regex, """version = "$newVer""""))
            println("  ✓ build.gradle.kts")
        }
    }

    // 2. OpenApiConfig.kt
    val openApiFile = file("src/main/kotlin/com/lgzarturo/springbootcourse/config/OpenApiConfig.kt")
    if (openApiFile.exists()) {
        val content = openApiFile.readText()
        if (content.contains(".version(\"$oldVer\")")) {
            openApiFile.writeText(content.replace(".version(\"$oldVer\")", ".version(\"$newVer\")"))
            println("  ✓ OpenApiConfig.kt")
        }
    }

    // 3. Ping.kt
    val pingFile = file("src/main/kotlin/com/lgzarturo/springbootcourse/features/ping/Ping.kt")
    if (pingFile.exists()) {
        val content = pingFile.readText()
        val regex = Regex("""val\s+version:\s*String\s*=\s*"$oldVer"""")
        if (regex.containsMatchIn(content)) {
            pingFile.writeText(content.replace(regex, """val version: String = "$newVer""""))
            println("  ✓ Ping.kt")
        }
    }

    // 4. PingControllerTest.kt
    val pingControllerTestFile = file("src/test/kotlin/com/lgzarturo/springbootcourse/features/ping/PingControllerTest.kt")
    if (pingControllerTestFile.exists()) {
        val content = pingControllerTestFile.readText()
        if (content.contains(".value(\"$oldVer\")")) {
            pingControllerTestFile.writeText(content.replace(".value(\"$oldVer\")", ".value(\"$newVer\")"))
            println("  ✓ PingControllerTest.kt")
        }
    }

    // 5. PingServiceTest.kt
    val pingServiceTestFile = file("src/test/kotlin/com/lgzarturo/springbootcourse/features/ping/PingServiceTest.kt")
    if (pingServiceTestFile.exists()) {
        val content = pingServiceTestFile.readText()
        if (content.contains("\"$oldVer\", result.version")) {
            pingServiceTestFile.writeText(content.replace("\"$oldVer\", result.version", "\"$newVer\", result.version"))
            println("  ✓ PingServiceTest.kt")
        }
    }

    // 6. README.md
    val readmeFile = file("README.md")
    if (readmeFile.exists()) {
        val content = readmeFile.readText()
        if (content.contains("releases/tag/v$oldVer")) {
            readmeFile.writeText(content.replace("releases/tag/v$oldVer", "releases/tag/v$newVer"))
            println("  ✓ README.md")
        }
    }
}

fun executeGitCommand(rootDir: File, vararg command: String): String {
    val process = ProcessBuilder("git", *command)
        .directory(rootDir)
        .redirectErrorStream(true)
        .start()
    val output = process.inputStream.bufferedReader().readText()
    val exitCode = process.waitFor()
    if (exitCode != 0) {
        throw GradleException("Error ejecutando 'git ${command.joinToString(" ")}' (código $exitCode):\n$output")
    }
    return output.trim()
}

fun updateChangelogContent(changelog: File, newVer: String, notes: String?, recentCommits: String) {
    if (!changelog.exists()) {
        println("  ⚠ No se encontró CHANGELOG.md")
        return
    }

    val content = changelog.readText()
    val today = LocalDate.now().toString()
    val sectionHeader = "## [$newVer] - $today"

    if (content.contains("## [$newVer]")) {
        println("  ℹ CHANGELOG.md ya contiene la sección para la versión $newVer")
        return
    }

    val unreleasedTag = "## [Unreleased]"
    var existingUnreleased = ""
    if (content.contains(unreleasedTag)) {
        val afterUnreleased = content.substringAfter(unreleasedTag)
        val nextSectionIndex = afterUnreleased.indexOf("\n## [")
        val candidate = if (nextSectionIndex != -1) {
            afterUnreleased.substring(0, nextSectionIndex).trim()
        } else {
            afterUnreleased.trim()
        }
        if (candidate.isNotBlank()) {
            existingUnreleased = candidate
        }
    }

    val releaseContent = when {
        !notes.isNullOrBlank() -> notes.trim()
        existingUnreleased.isNotBlank() -> existingUnreleased
        recentCommits.isNotBlank() -> "### Added\n\n$recentCommits"
        else -> """
        ### Added
        - Actualización a versión $newVer
        """.trimIndent()
    }

    val newSection = "\n$sectionHeader\n\n$releaseContent\n"

    val contentWithCleanUnreleased = if (existingUnreleased.isNotBlank() && content.contains(unreleasedTag)) {
        val afterUnreleased = content.substringAfter(unreleasedTag)
        val nextSectionIndex = afterUnreleased.indexOf("\n## [")
        if (nextSectionIndex != -1) {
            val rest = afterUnreleased.substring(nextSectionIndex)
            content.substringBefore(unreleasedTag) + unreleasedTag + "\n" + rest
        } else {
            content
        }
    } else {
        content
    }

    val updatedContent = if (contentWithCleanUnreleased.contains(unreleasedTag)) {
        contentWithCleanUnreleased.replace(unreleasedTag, "$unreleasedTag$newSection")
    } else {
        "# Changelog\n$newSection\n$contentWithCleanUnreleased"
    }

    val repoUrl = "https://github.com/lgzarturo/springboot-course"
    val newUnreleasedRef = "[Unreleased]: $repoUrl/compare/v$newVer...HEAD"
    val newVerRef = "[$newVer]: $repoUrl/releases/tag/v$newVer"

    val unreleasedRegex = Regex("""\[Unreleased\]:\s*https://github\.com/[^\s]+""")
    val finalContent = if (unreleasedRegex.containsMatchIn(updatedContent)) {
        updatedContent.replace(unreleasedRegex, "$newUnreleasedRef\n$newVerRef")
    } else {
        "$updatedContent\n$newUnreleasedRef\n$newVerRef\n"
    }

    changelog.writeText(finalContent)
    println("  ✓ CHANGELOG.md actualizado con sección [$newVer] - $today")
}

tasks.register("printVersion") {
    group = "versioning"
    description = "Imprime la versión actual del proyecto"
    doLast {
        println("Versión actual: $version")
    }
}

tasks.register("whatsnext") {
    group = "versioning"
    description = "Muestra la versión actual y calcula los próximos incrementos SemVer"
    doLast {
        val currentVer = version.toString()
        val nextPatch = calculateNextSemVer(currentVer, "patch")
        val nextMinor = calculateNextSemVer(currentVer, "minor")
        val nextMajor = calculateNextSemVer(currentVer, "major")

        val separator = "=".repeat(50)
        println(separator)
        println("VERSIONADO SEMÁNTICO (SemVer)")
        println(separator)
        println("Versión actual: $currentVer")
        println()
        println("Próximos incrementos sugeridos:")
        println("  • PATCH (fix, perf):        $nextPatch (tag: v$nextPatch)")
        println("  • MINOR (feat, milestone):  $nextMinor (tag: v$nextMinor)")
        println("  • MAJOR (breaking change):  $nextMajor (tag: v$nextMajor)")
        println()
        println("Comandos disponibles:")
        println("  make release-minor   o   ./gradlew createRelease -Ptype=minor")
        println("  make release-patch   o   ./gradlew createRelease -Ptype=patch")
        println("  make release-major   o   ./gradlew createRelease -Ptype=major")
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
        println("El changelog se actualiza automáticamente al ejecutar:")
        println("  make release-minor   o   ./gradlew createRelease -Ptype=minor")
        println()
        println("Para generación interactiva con conventional-changelog:")
        println("  npx conventional-changelog -p conventionalcommits -i CHANGELOG.md -s")
        println(separator)
    }
}

tasks.register("bumpVersion") {
    group = "versioning"
    description = "Incrementa la versión en build.gradle.kts y archivos relacionados (sin commit/tag)"
    doLast {
        val currentVer = version.toString()
        val targetVer = if (project.hasProperty("newVersion")) {
            project.property("newVersion").toString().removePrefix("v").trim()
        } else {
            val type = project.findProperty("type")?.toString() ?: "patch"
            calculateNextSemVer(currentVer, type)
        }

        if (targetVer == currentVer) {
            throw GradleException("La nueva versión ($targetVer) es idéntica a la versión actual ($currentVer).")
        }

        updateVersionInProject(currentVer, targetVer)
        println("\n✓ Versión actualizada con éxito: $currentVer -> $targetVer")
    }
}

tasks.register("bumpPatch") {
    group = "versioning"
    description = "Incrementa la versión PATCH (x.y.Z+1)"
    doLast {
        val currentVer = version.toString()
        val targetVer = calculateNextSemVer(currentVer, "patch")
        updateVersionInProject(currentVer, targetVer)
        println("\n✓ Versión PATCH actualizada con éxito: $currentVer -> $targetVer")
    }
}

tasks.register("bumpMinor") {
    group = "versioning"
    description = "Incrementa la versión MINOR (x.Y+1.0)"
    doLast {
        val currentVer = version.toString()
        val targetVer = calculateNextSemVer(currentVer, "minor")
        updateVersionInProject(currentVer, targetVer)
        println("\n✓ Versión MINOR actualizada con éxito: $currentVer -> $targetVer")
    }
}

tasks.register("bumpMajor") {
    group = "versioning"
    description = "Incrementa la versión MAJOR (X+1.0.0)"
    doLast {
        val currentVer = version.toString()
        val targetVer = calculateNextSemVer(currentVer, "major")
        updateVersionInProject(currentVer, targetVer)
        println("\n✓ Versión MAJOR actualizada con éxito: $currentVer -> $targetVer")
    }
}

tasks.register("createRelease") {
    group = "versioning"
    description = "Genera un release SemVer completo: tests, actualización de archivos, CHANGELOG.md, commit y git tag"

    val skipTests = project.hasProperty("skipTests") && project.property("skipTests").toString() == "true"
    if (!skipTests) {
        dependsOn("test")
    }

    doLast {
        val currentVer = version.toString()
        val targetVer = if (project.hasProperty("newVersion")) {
            project.property("newVersion").toString().removePrefix("v").trim()
        } else {
            val type = project.findProperty("type")?.toString() ?: "minor"
            calculateNextSemVer(currentVer, type)
        }

        if (targetVer == currentVer) {
            throw GradleException("La nueva versión ($targetVer) es idéntica a la versión actual ($currentVer).")
        }

        val targetTag = "v$targetVer"

        // 1. Verificar si el tag ya existe
        val existingTags = try {
            executeGitCommand(rootDir, "tag", "-l", targetTag)
        } catch (e: Exception) {
            ""
        }
        if (existingTags.isNotBlank()) {
            throw GradleException("El tag '$targetTag' ya existe en git.")
        }

        // 2. Verificar estado de git
        val status = try {
            executeGitCommand(rootDir, "status", "--porcelain")
        } catch (e: Exception) {
            ""
        }
        val allowDirty = project.hasProperty("allowDirty") && project.property("allowDirty").toString() == "true"
        if (status.isNotBlank() && !allowDirty) {
            println("⚠ Cambios detectados en el repositorio antes de liberar:")
            status.lines().filter { it.isNotBlank() }.forEach { println("   $it") }
            throw GradleException(
                "El repositorio tiene cambios sin confirmar. Confirma tus cambios antes de liberar, o usa -PallowDirty=true",
            )
        }

        val separator = "=".repeat(60)
        println(separator)
        println("INICIANDO PROCESO DE RELEASE")
        println(separator)
        println("Versión actual: $currentVer")
        println("Nueva versión:  $targetVer (Tag: $targetTag)")
        println()

        // 3. Obtener commits recientes para changelog si no se especifican notas
        val previousTag = try {
            executeGitCommand(rootDir, "describe", "--tags", "--abbrev=0")
        } catch (e: Exception) {
            "v$currentVer"
        }
        val recentCommits = try {
            executeGitCommand(rootDir, "log", "$previousTag..HEAD", "--pretty=format:- %s", "--no-merges")
        } catch (e: Exception) {
            ""
        }

        // 4. Actualizar versión en archivos del proyecto
        updateVersionInProject(currentVer, targetVer)

        // 5. Actualizar CHANGELOG.md
        val notesFileProp = project.findProperty("notesFile")?.toString()
        val customNotes = when {
            !notesFileProp.isNullOrBlank() -> file(notesFileProp).readText()
            project.hasProperty("notes") -> project.property("notes").toString()
            else -> null
        }
        val changelogFile = file("CHANGELOG.md")
        updateChangelogContent(changelogFile, targetVer, customNotes, recentCommits)

        // 6. Git commit y tag
        val filesToCommit = listOf(
            "build.gradle.kts",
            "src/main/kotlin/com/lgzarturo/springbootcourse/config/OpenApiConfig.kt",
            "src/main/kotlin/com/lgzarturo/springbootcourse/features/ping/Ping.kt",
            "src/test/kotlin/com/lgzarturo/springbootcourse/features/ping/PingControllerTest.kt",
            "src/test/kotlin/com/lgzarturo/springbootcourse/features/ping/PingServiceTest.kt",
            "README.md",
            "CHANGELOG.md",
        ).filter { file(it).exists() }

        executeGitCommand(rootDir, "add", *filesToCommit.toTypedArray())

        val commitMessage = "chore(release): $targetTag [skip ci]\n\nRelease version $targetVer"
        executeGitCommand(rootDir, "commit", "-m", commitMessage)
        println("  ✓ Git commit creado: chore(release): $targetTag [skip ci]")

        executeGitCommand(rootDir, "tag", "-a", targetTag, "-m", "Release $targetTag")
        println("  ✓ Git tag creado: $targetTag")

        println()
        println(separator)
        println("✓ RELEASE $targetTag COMPLETADO EXITOSAMENTE")
        println(separator)
        println("Para publicar este release en GitHub ejecuta:")
        println("  git push origin HEAD")
        println("  git push origin $targetTag")
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
        // xml.required.set(true)
        // txt.required.set(true)
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

tasks.withType<Test> {
    useJUnitPlatform {
        if (!project.hasProperty("includeMigrationTests")) {
            excludeTags("migration")
        }
    }
}
