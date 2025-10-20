package com.lgzarturo.springbootcourse.config

import com.lgzarturo.springbootcourse.shared.constant.CorsConstants
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

/**
 * Configuración web de la aplicación
 * Configura CORS, interceptores, y otras configuraciones relacionadas con MVC
 */
@Configuration
class WebConfig : WebMvcConfigurer {
    /**
     * Configuración de CORS para permitir peticiones desde diferentes orígenes
     */
    override fun addCorsMappings(registry: CorsRegistry) {
        registry
            .addMapping(CorsConstants.API_PATH_PATTERN)
            .allowedOrigins(*CorsConstants.ALLOWED_ORIGINS.toTypedArray())
            .allowedMethods(*CorsConstants.ALLOWED_METHODS.toTypedArray())
            .allowedHeaders(*CorsConstants.ALLOWED_HEADERS.toTypedArray())
            .allowCredentials(true)
            .maxAge(CorsConstants.MAX_AGE_SECONDS)
    }
}
