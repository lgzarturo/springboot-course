package com.lgzarturo.springbootcourse.example.adapters.persistence.entity

import com.lgzarturo.springbootcourse.example.domain.Example
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "example_entity")
data class ExampleEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    @Column(nullable = false)
    val name: String,
    @Column(columnDefinition = "TEXT")
    val description: String? = null,
    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
    @Column(name = "updated_at", nullable = false)
    val updatedAt: LocalDateTime = LocalDateTime.now(),
) {
    fun toDomain() = Example(id, name, description)

    companion object {
        fun fromDomain(example: Example) =
            ExampleEntity(id = example.id, name = example.name!!, description = example.description)
    }
}
