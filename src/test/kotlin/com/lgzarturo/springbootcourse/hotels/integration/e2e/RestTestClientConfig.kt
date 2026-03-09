/*
package com.lgzarturo.springbootcourse.hotels.integration.e2e

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.boot.resttestclient.TestRestTemplate
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.boot.web.client.RootUriTemplateHandler

@TestConfiguration
class RestTestClientConfig {
    @LocalServerPort
    private var port: Int = 0

    @Bean
    fun testRestTemplate(): TestRestTemplate {
        val testRestTemplate = TestRestTemplate()
        testRestTemplate.setUriTemplateHandler(RootUriTemplateHandler("http://localhost:$port"))
        return testRestTemplate
    }
}
*/
