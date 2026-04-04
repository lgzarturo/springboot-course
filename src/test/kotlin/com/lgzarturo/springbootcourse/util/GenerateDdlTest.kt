package com.lgzarturo.springbootcourse.util

import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@Tag("migration")
@SpringBootTest
@ActiveProfiles("migration")
class GenerateDdlTest {

    @Test
    fun printMigrationProcess() {
        println("\n" + "=".repeat(70))
        println("GUIA PARA GENERAR MIGRACIONES DE FLYWAY (FLUJO AUTOMATIZADO)")
        println("=".repeat(70))
        println("Ejecuta el siguiente comando desde la raíz del proyecto:")
        println("  powershell ./springboot-course/generate-migration.ps1 -Description \"Add_New_Fields\"")
        println("\nEste comando:")
        println("1. Levanta un Docker temporal con PostgreSQL y Redis.")
        println("2. Genera automáticamente el SQL en src/main/resources/db/migration/V<N>__<desc>.sql")
        println("3. Detiene el Docker automáticamente.")
        println("=".repeat(70) + "\n")
    }
}
