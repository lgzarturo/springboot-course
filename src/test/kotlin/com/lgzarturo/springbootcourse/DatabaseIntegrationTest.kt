package com.lgzarturo.springbootcourse

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import javax.sql.DataSource
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class DatabaseIntegrationTest : BaseIntegrationTest() {
    @Autowired
    private lateinit var dataSource: DataSource

    @Test
    @DisplayName("Debe cargar el contexto con la base de datos configurada")
    fun `context loads with database`() {
        assertNotNull(dataSource)
        // Verifica que la conexión es válida
        dataSource.connection.use { connection ->
            assertTrue(connection.isValid(2))
        }
    }

    @Test
    @DisplayName("Debe poder conectarse a la base de datos")
    fun `can connect to database`() {
        dataSource.connection.use { connection ->
            assertTrue(connection.isValid(2))

            val metaData = connection.metaData
            val databaseProductName = metaData.databaseProductName
            // Accept either PostgreSQL (from Testcontainers) or H2 (fallback)
            val isSupportedDb =
                databaseProductName.contains("PostgreSQL", ignoreCase = true) ||
                    databaseProductName.contains("H2", ignoreCase = true)
            assertTrue(isSupportedDb, "Database should be PostgreSQL or H2")
        }
    }
}
