# Cómo integrar Flyway & Hibernate para generar migraciones

Uno de los mayores retos en el desarrollo de aplicaciones Java con Spring Boot es mantener la base de datos sincronizada con el modelo de entidades JPA. Hibernate puede generar el esquema automáticamente, pero hacerlo directamente sobre la base de datos en entornos productivos es arriesgado.

La solución profesional es combinar **Hibernate como generador de DDL** y **Flyway como gestor de migraciones**, aprovechando **Gradle** para automatizar el flujo.

En este documento veremos cómo hacerlo correctamente.

> Ojo: Aquí hay dos caminos uno es Flyway y el otro es Liquibase. Son mutuamente excluyentes, no se pueden usar en el mismo proyecto.

---

## 1. Objetivo

Configurar un flujo híbrido y multiplataforma donde:

* Hibernate genera los archivos DDL (CREATE TABLE, etc.) a partir de las entidades.
* Flyway administra las migraciones versionadas.
* Gradle ejecuta tareas personalizadas para:

    * Generar el DDL (`generateDDL`)
    * Crear nuevas migraciones (`createMigration`)
    * Sugerir comparaciones de esquema (`diffMigration`)

> Este flujo permite mantener la base de datos sincronizada de forma controlada y reproducible, sin depender de cambios automáticos.

---

## 2. Configuración de Hibernate para generar el DDL

Crea un archivo `application-generate-ddl.yaml`, este perfil se usará exclusivamente para generar los scripts DDL sin aplicar cambios en la base de datos:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://127.0.0.1:5432/springboot_course?ApplicationName=springboot-course
    username: ${DB_USERNAME:postgres}
    password: ${DB_PASSWORD:postgres}
    driver-class-name: org.postgresql.Driver

  jpa:
    hibernate:
      ddl-auto: validate
    properties:
      hibernate:
        format_sql: true
        use_sql_comments: true
      jakarta:
        persistence:
          schema-generation:
            scripts:
              action: create
              create-target: build/schema-create.sql
              drop-target: build/schema-drop.sql
              create-source: metadata
    show-sql: false
    open-in-view: false

  flyway:
    enabled: false
```

Este perfil genera los archivos `schema-create.sql` y `schema-drop.sql` dentro del directorio `build/`, pero **no aplica los cambios en la base de datos**, manteniendo la seguridad del entorno.

---

## 3. Configuración para desarrollo

Tú `application-dev.yaml` debe mantener Flyway habilitado y usar la misma conexión:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://127.0.0.1:5432/springboot_course
    username: ${DB_USERNAME:postgres}
    password: ${DB_PASSWORD:postgres}
    driver-class-name: org.postgresql.Driver

  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: true
    open-in-view: false

  flyway:
    enabled: true
    locations: classpath:db/migration
    baseline-on-migrate: true
    validate-on-migrate: true
```

> Con esto, las migraciones Flyway se aplican automáticamente al iniciar la aplicación. 

---

## 4. Configuración de Gradle

Edita tu `build.gradle.kts` para agregar las tareas personalizadas:

```kotlin
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

plugins {
    kotlin("jvm") version "2.1.0"
    kotlin("plugin.spring") version "2.1.0"
    kotlin("plugin.jpa") version "2.1.0"
    id("org.springframework.boot") version "3.5.6"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "com.lgzarturo"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.flywaydb:flyway-core")
    runtimeOnly("org.postgresql:postgresql")
}
```

---

### Tarea `generateDDL`

Genera los archivos DDL ejecutando el contexto Spring Boot con el perfil `generate-ddl`:

```kotlin
tasks.register<JavaExec>("generateDDL") {
    group = "database"
    description = "Generate DDL scripts from JPA entities"
    
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("com.lgzarturo.springbootcourse.SpringbootCourseApplicationKt")

    args = listOf(
        "--spring.profiles.active=generate-ddl",
        "--spring.main.web-application-type=none",
        "--spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration"
    )

    doFirst {
        delete("build/schema-create.sql", "build/schema-drop.sql")
        println("Generating DDL scripts...")
    }

    doLast {
        val createFile = file("build/schema-create.sql")
        if (createFile.exists()) {
            println("\n✓ DDL generated successfully:")
            println("  ${createFile.absolutePath}")
        } else {
            throw GradleException("Failed to generate DDL scripts")
        }
    }
}
```

---

### Tarea `createMigration`

Crea un archivo de migración Flyway basado en el DDL generado:

```kotlin
tasks.register("createMigration") {
    group = "database"
    description = "Create new Flyway migration from generated DDL"

    dependsOn("generateDDL")

    doLast {
        val version = project.findProperty("migration_version")?.toString() ?: "1"
        val description = project.findProperty("description")?.toString() ?: "migration"
        val cleanDescription = description.replace(" ", "_").lowercase()
        val fileName = "V${version}__${cleanDescription}.sql"

        val sourceFile = file("build/schema-create.sql")
        val targetDir = file("src/main/resources/db/migration")
        val targetFile = file("$targetDir/$fileName")

        if (!sourceFile.exists()) throw GradleException("DDL not found. Run generateDDL first.")

        targetDir.mkdirs()
        val content = sourceFile.readText()
            .replace(Regex("(?m)^\\s*--.*$"), "")
            .trim()

        targetFile.writeText(content)
        println("✓ Migration created: ${targetFile.absolutePath}")
    }
}
```

> Nota importante: Si usas `version` en vez de `migration_version`, asegúrate de ajustar el código en consecuencia. Solo que suele tomar la configuración de gradle que indica `version` por defecto.

---

### Tarea `diffMigration`

Sugiere un flujo manual para generar migraciones incrementales:

```kotlin
tasks.register("diffMigration") {
    group = "database"
    description = "Generate incremental migration by comparing entities with current schema"
    
    doLast {
        println("⚠ Manual diff required:")
        println("  1. Run: ./gradlew generateDDL")
        println("  2. Compare build/schema-create.sql with your DB")
        println("  3. Create incremental migration manually (e.g., V2__add_column.sql)")
    }
}
```

---

## 5. Flujo de trabajo completo

### Paso 1: Crear o modificar una entidad

```kotlin
@Entity
data class Product(
    @Id @GeneratedValue val id: Long? = null,
    val name: String,
    val price: BigDecimal
)
```

### Paso 2: Generar DDL

```bash
./gradlew generateDDL
```

Esto crea `build/schema-create.sql`.

### Paso 3: Crear migración

```bash
./gradlew createMigration -Pversion=1 -Pdescription=initial_schema
```

### Paso 4: Revisar el archivo

Edita `src/main/resources/db/migration/V1__initial_schema.sql` para añadir índices o constraints.

### Paso 5: Ejecutar la app con Flyway

```bash
./gradlew bootRun --args='--spring.profiles.active=dev'
```

Flyway detecta y aplica las migraciones automáticamente.

---

## 6. Cambios incrementales

Cuando modificas una entidad existente:

```bash
./gradlew generateDDL
```

Compara `build/schema-create.sql` con tu base actual.
Luego crea un nuevo archivo:

```
src/main/resources/db/migration/V2__add_status_column.sql
```

Si quieres automatizar el diff, puedes usar herramientas externas como:

* **DBeaver Schema Compare**
* **pgAdmin Schema Diff**
* **IntelliJ Database Tools**

---

## 7. Ejecución multiplataforma

Funciona igual en cualquier sistema operativo:

### Windows (PowerShell/CMD)

```powershell
.\gradlew.bat generateDDL
.\gradlew.bat createMigration -Pversion=1 -Pdescription=initial_schema
```

### Linux/Mac (Bash/Zsh)

```bash
./gradlew generateDDL
./gradlew createMigration -Pversion=1 -Pdescription=initial_schema
```

---

## 8. Ventajas de este enfoque

- ✅ Evitas que Hibernate modifique directamente la base de datos
- ✅ Mantienes las migraciones versionadas con Flyway
- ✅ Simplificas la generación de scripts con tareas Gradle
- ✅ Compatible con cualquier entorno o sistema operativo
- ✅ Facilita auditorías y control de cambios

---

## 9. Próximo paso

- Una propuesta de mejora es que se puede automatizar el diff entre el DDL generado y la base de datos real usando JDBC y librerías como `liquibase-diff` o `pg-diff`, si quieres eliminar el paso manual.
- Documentar el proceso de un rollback cuando algo sale mal. De momento no hay ninguna forma de hacerlo, es decir, si algo sale mal, hay que volver a ejecutar todas las migraciones.
