package com.lgzarturo.springbootcourse.example.adapters.persistence

import com.lgzarturo.springbootcourse.example.adapters.persistence.entity.ExampleEntity
import org.springframework.data.jpa.repository.JpaRepository

interface ExampleJpaRepository : JpaRepository<ExampleEntity, Long>
