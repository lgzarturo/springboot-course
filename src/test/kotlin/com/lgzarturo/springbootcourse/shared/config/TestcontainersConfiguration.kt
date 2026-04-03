package com.lgzarturo.springbootcourse.shared.config

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Condition
import org.springframework.context.annotation.ConditionContext
import org.springframework.context.annotation.Conditional
import org.springframework.core.type.AnnotatedTypeMetadata
import org.testcontainers.DockerClientFactory
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.utility.DockerImageName

class DockerAvailableCondition : Condition {
    override fun matches(
        context: ConditionContext,
        metadata: AnnotatedTypeMetadata,
    ): Boolean =
        try {
            DockerClientFactory.instance().isDockerAvailable()
        } catch (e: Throwable) {
            false
        }
}

@TestConfiguration(proxyBeanMethods = false)
@Conditional(DockerAvailableCondition::class)
class TestcontainersConfiguration {
    @Bean
    @ServiceConnection
    fun postgresContainer(): PostgreSQLContainer<*> =
        PostgreSQLContainer(DockerImageName.parse("postgres:17-alpine"))
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test")
}
