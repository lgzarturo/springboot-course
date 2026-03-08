package com.lgzarturo.springbootcourse.shared.config

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.info.License
import io.swagger.v3.oas.models.servers.Server
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * Configuración de OpenAPI/Swagger para documentación de la API
 * Accesible en: http://localhost:8080/swagger-ui.html
 */
@Configuration
class OpenApiConfig {
    @Bean
    fun customOpenAPI(): OpenAPI =
        OpenAPI()
            .info(
                Info()
                    .title("Spring Boot Course API")
                    .version("0.0.2")
                    .description("API REST para el curso de Spring Boot con Kotlin")
                    .contact(
                        Contact()
                            .name("Arturo López")
                            .email("lgzarturo@gmail.com")
                            .url("https://github.com/lgzarturo"),
                    ).license(
                        License()
                            .name("Creative Commons Attribution 4.0 International (CC BY 4.0)")
                            .url("https://creativecommons.org/licenses/by/4.0/deed.en"),
                    ),
            ).servers(
                listOf(
                    Server()
                        .url("http://localhost:8080")
                        .description("Servidor de desarrollo"),
                    Server()
                        .url("https://api.example.com")
                        .description("Servidor de producción"),
                ),
            )
}
