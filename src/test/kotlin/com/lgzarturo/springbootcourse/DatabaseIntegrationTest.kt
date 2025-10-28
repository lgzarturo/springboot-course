package com.lgzarturo.springbootcourse

import com.lgzarturo.springbootcourse.config.TestcontainersConfiguration
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import javax.sql.DataSource
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@SpringBootTest
@Import(TestcontainersConfiguration::class)
class DatabaseIntegrationTest : BaseIntegrationTest() {
    @Autowired
    private lateinit var dataSource: DataSource

    @Test
    @DisplayName("Debe cargar el contexto con el contenedor de PostgreSQL")
    fun `context loads with PostgreSQL container`() {
        assertNotNull(dataSource)
        // Verifica que la conexión es válida (el contenedor debe estar arriba)
        dataSource.connection.use { connection ->
            assertTrue(connection.isValid(2))
        }
    }

    @Test
    @DisplayName("Debe poder conectarse a la base de datos de PostgreSQL")
    fun `can connect to PostgreSQL database`() {
        dataSource.connection.use { connection ->
            assertTrue(connection.isValid(2))

            val metaData = connection.metaData
            assertTrue(metaData.databaseProductName.contains("PostgreSQL", ignoreCase = true))
        }
    }
}
