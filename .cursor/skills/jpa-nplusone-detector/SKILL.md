---
id: jpa-nplusone-detector
name: jpa-nplusone-detector
description: >
  Audits Hibernate database interactions to prevent N+1 query problems in JVM apps.
user-invokable: true
license: MIT
metadata:
  author: lgzarturo
  category: performance
compatibility:
  tools: [claude, codex, gemini, agy, opencode]
  stacks:
    languages: [kotlin, java]
    frameworks: [spring-boot, spring-data-jpa, hibernate]
paths:
  - "**/*.kt"
  - "**/*.java"
---
# JPA N+1 Query Detector

## Core Principles

1. **Hibernate Fetches**: Avoid lazy-loading relationships within loops or stream operations.
2. **Batch Fetching & Joins**: Resolve N+1 issues by using `@EntityGraph` or `JOIN FETCH` inside custom JPQL queries.
3. **Hibernate Statistics**: Enable Hibernate statistics in development to verify query counts.

## Remediation Patterns

### Ineffective Pattern (Triggers N+1 queries)
```kotlin
val users = userRepository.findAll()
for (user in users) {
    println(user.posts.size) // Post is loaded lazily for each user
}
```

### Remediation A: Using JOIN FETCH
```kotlin
@Query("SELECT u FROM User u LEFT JOIN FETCH u.posts")
fun findAllWithPosts(): List<User>
```

### Remediation B: Using @EntityGraph
```kotlin
@EntityGraph(attributePaths = ["posts"])
override fun findAll(): List<User>
```
