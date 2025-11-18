package com.lgzarturo.springbootcourse

import com.lgzarturo.springbootcourse.shared.config.TestcontainersConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import

@SpringBootTest
@Import(TestcontainersConfiguration::class)
abstract class BaseIntegrationTest
