package com.lgzarturo.springbootcourse.features.examples

import org.springframework.data.jpa.repository.JpaRepository

interface ExampleJpaRepository : JpaRepository<ExampleEntity, Long>
