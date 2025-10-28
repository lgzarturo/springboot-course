package com.lgzarturo.springbootcourse.infrastructure.persistence.repository

import com.lgzarturo.springbootcourse.infrastructure.persistence.entity.ExampleEntity
import org.springframework.data.jpa.repository.JpaRepository

interface ExampleJpaRepository : JpaRepository<ExampleEntity, Long>
