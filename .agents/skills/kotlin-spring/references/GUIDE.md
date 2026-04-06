# Kotlin Spring Guide

## REST Controller

```kotlin
@RestController
@RequestMapping("/api/users")
class UserController(private val service: UserService) {
    
    @GetMapping
    suspend fun getAll(): List<User> = service.findAll()
    
    @PostMapping
    suspend fun create(@RequestBody user: User): User = service.save(user)
}
```

## Repository

```kotlin
interface UserRepository : CoroutineCrudRepository<User, Long> {
    suspend fun findByEmail(email: String): User?
}
```
