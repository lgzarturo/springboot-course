package com.lgzarturo.springbootcourse.hotels.integration.e2e

import org.junit.jupiter.api.Test
import org.springframework.boot.resttestclient.TestRestTemplate

class ClasspathDiagnosticTest {
    @Test
    fun checkTestRestTemplate() {
        val testRestTemplate = TestRestTemplate()
        println("TestRestTemplate class: ${testRestTemplate.javaClass.canonicalName}")

        try {
            val clazz = Class.forName("org.springframework.boot.web.client.RootUriTemplateHandler")
            println("Found RootUriTemplateHandler at org.springframework.boot.web.client.RootUriTemplateHandler")
        } catch (e: Exception) {
            println("RootUriTemplateHandler NOT found at org.springframework.boot.web.client.RootUriTemplateHandler")
        }

        try {
            val clazz = Class.forName("org.springframework.boot.restclient.RootUriTemplateHandler")
            println("Found RootUriTemplateHandler at org.springframework.boot.restclient.RootUriTemplateHandler")
        } catch (e: Exception) {
            println("RootUriTemplateHandler NOT found at org.springframework.boot.restclient.RootUriTemplateHandler")
        }
    }
}
