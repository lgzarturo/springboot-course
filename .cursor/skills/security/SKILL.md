---
id: security
name: security
description: >
  Provides expert knowledge for implementing and reviewing application security following OWASP Top 10 (2021), with stack-specific patterns for Spring Boot, Django, Next.js, and Astro.

user-invokable: true
license: MIT
metadata:
  author: lgzarturo
  category: security

compatibility:
  tools: [claude, codex, gemini, agy, opencode]
  stacks:
    languages: []
    frameworks: []

risk:
  level: high
  can_execute_shell: false
  can_modify_files: true
  requires_network: false

inputs: []

outputs: []

quality:
  reviewed_by: codeconductor-core
  version: 0.1.0
paths:
  - "src/**"
---



# Security

## OWASP Top 10 (2021) — Quick Reference

| # | Category | What to check |
|---|----------|--------------|
| A01 | Broken Access Control | Is authorization enforced on every endpoint? |
| A02 | Cryptographic Failures | Are secrets encrypted at rest and in transit? |
| A03 | Injection | Is all input sanitized or parameterized? |
| A04 | Insecure Design | Is the threat model documented? |
| A05 | Security Misconfiguration | Are defaults changed? Debug off in prod? |
| A06 | Vulnerable Components | Are dependencies up to date? |
| A07 | Auth & Session Failures | Are sessions invalidated on logout? Token expiry set? |
| A08 | Software Integrity Failures | Are supply-chain dependencies verified? |
| A09 | Logging & Monitoring Failures | Are security events logged without leaking PII? |
| A10 | SSRF | Are outbound URLs allowlisted? |

## Input Validation

Validate all input at system boundaries. Never trust data from: HTTP request
body, query parameters, headers, cookies, or environment variables passed by
external systems.

**Rules:**

- Validate type, format, length, and range
- Reject unknown fields — do not silently ignore them
- Use allowlists (accept known-good), not blocklists (reject known-bad)
- Validate at the API layer before it reaches the service layer

```kotlin
// Spring Boot — Bean Validation
data class CreateUserRequest(
    @field:NotBlank @field:Email val email: String,
    @field:Size(min = 8, max = 72) val password: String,
    @field:Size(max = 100) val name: String
)

@PostMapping
fun createUser(@Valid @RequestBody request: CreateUserRequest): ResponseEntity<UserResponse> {
    // request is guaranteed valid here
}
```

```python
# Django REST Framework — serializer validation
class CreateUserSerializer(serializers.Serializer):
    email = serializers.EmailField(max_length=254)
    password = serializers.CharField(min_length=8, max_length=72, write_only=True)
    name = serializers.CharField(max_length=100)

    def validate_email(self, value):
        if User.objects.filter(email=value).exists():
            raise serializers.ValidationError("Email already registered.")
        return value.lower()
```

```typescript
// Next.js / Astro — Zod
import { z } from 'zod';

const CreateUserSchema = z.object({
  email: z.string().email().max(254),
  password: z.string().min(8).max(72),
  name: z.string().max(100),
});

const result = CreateUserSchema.safeParse(body);
if (!result.success) {
  return Response.json({ error: result.error.flatten() }, { status: 400 });
}
```

## Authentication

### JWT (Stateless)

Use JWTs for stateless API authentication. Keep access tokens short-lived.

**Token lifetimes:**

| Token | Lifetime | Storage |
|-------|----------|---------|
| Access token | 15 minutes | Memory (never localStorage) |
| Refresh token | 7–30 days | HttpOnly cookie |

Never store access tokens in `localStorage` — XSS can read them. Store in
memory (React state, module variable) and refresh via the HttpOnly cookie.

```kotlin
// Spring Security 6 + JWT
@Configuration
@EnableWebSecurity
class SecurityConfig(private val jwtFilter: JwtAuthenticationFilter) {

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain = http
        .csrf { it.disable() }                         // stateless API — CSRF not needed
        .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
        .authorizeHttpRequests { auth ->
            auth
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
        }
        .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter::class.java)
        .build()
}
```

```python
# Django — djangorestframework-simplejwt
# settings.py
SIMPLE_JWT = {
    "ACCESS_TOKEN_LIFETIME": timedelta(minutes=15),
    "REFRESH_TOKEN_LIFETIME": timedelta(days=7),
    "ROTATE_REFRESH_TOKENS": True,
    "BLACKLIST_AFTER_ROTATION": True,
    "ALGORITHM": "HS256",
    "AUTH_HEADER_TYPES": ("Bearer",),
}
```

```typescript
// Next.js — Auth.js (NextAuth v5)
// auth.ts
import NextAuth from 'next-auth';
import Credentials from 'next-auth/providers/credentials';

export const { auth, signIn, signOut } = NextAuth({
  providers: [
    Credentials({
      authorize: async (credentials) => {
        // validate and return user or null
      },
    }),
  ],
  session: { strategy: 'jwt', maxAge: 7 * 24 * 60 * 60 },
});
```

### Password Hashing

Never store plaintext passwords. Never use MD5 or SHA-1 for password hashing.

| Algorithm | Use | Work factor |
|-----------|-----|-------------|
| bcrypt | default | cost=12 |
| Argon2id | preferred when available | m=65536, t=3 |
| PBKDF2 | FIPS environments | iterations=600000 |

```kotlin
// Spring Security uses bcrypt by default
@Bean
fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder(12)
```

```python
# Django uses PBKDF2 by default; switch to Argon2 for stronger hashing
# pip install django[argon2]
PASSWORD_HASHERS = [
    "django.contrib.auth.hashers.Argon2PasswordHasher",
    "django.contrib.auth.hashers.PBKDF2PasswordHasher",  # fallback for existing hashes
]
```

## Authorization

**Principle of least privilege:** every endpoint must explicitly declare who
can access it. Deny by default.

```kotlin
// Spring — method-level security
@PreAuthorize("hasRole('ADMIN') or #userId == authentication.name")
fun getUser(userId: String): UserResponse { ... }
```

```python
# Django REST Framework
class UserDetailView(RetrieveUpdateAPIView):
    permission_classes = [IsAuthenticated, IsOwnerOrAdmin]
    queryset = User.objects.all()

class IsOwnerOrAdmin(BasePermission):
    def has_object_permission(self, request, view, obj):
        return request.user.is_staff or obj == request.user
```

Always check authorization at the **object level**, not just the endpoint
level. An authenticated user should not be able to access another user's data
by changing an ID in the URL.

## CORS

```kotlin
// Spring Boot — explicit allowlist; never use "*" in production
@Bean
fun corsConfigurationSource(): CorsConfigurationSource {
    val config = CorsConfiguration().apply {
        allowedOrigins = listOf("https://app.example.com")
        allowedMethods = listOf("GET", "POST", "PUT", "DELETE")
        allowedHeaders = listOf("Authorization", "Content-Type")
        allowCredentials = true
        maxAge = 3600L
    }
    return UrlBasedCorsConfigurationSource().apply {
        registerCorsConfiguration("/api/**", config)
    }
}
```

```python
# Django — django-cors-headers
CORS_ALLOWED_ORIGINS = [
    "https://app.example.com",
]
CORS_ALLOW_CREDENTIALS = True
CORS_ALLOW_ALL_ORIGINS = False  # never True in production
```

## Security Headers

Set these headers on every response. They defend against XSS, clickjacking,
and MIME sniffing.

```text
Strict-Transport-Security: max-age=63072000; includeSubDomains; preload
Content-Security-Policy: default-src 'self'; script-src 'self'; object-src 'none'
X-Frame-Options: DENY
X-Content-Type-Options: nosniff
Referrer-Policy: strict-origin-when-cross-origin
Permissions-Policy: camera=(), microphone=(), geolocation=()
```

```typescript
// Next.js — next.config.ts
const securityHeaders = [
  { key: 'X-Frame-Options', value: 'DENY' },
  { key: 'X-Content-Type-Options', value: 'nosniff' },
  { key: 'Referrer-Policy', value: 'strict-origin-when-cross-origin' },
  {
    key: 'Strict-Transport-Security',
    value: 'max-age=63072000; includeSubDomains; preload',
  },
];

export default {
  headers: async () => [
    { source: '/(.*)', headers: securityHeaders },
  ],
};
```

## Secrets Management

**Never commit secrets to source control.** No exceptions.

```text
✗ DATABASE_URL = "postgres://user:password@prod.example.com/db"  # in source
✓ DATABASE_URL = env("DATABASE_URL")                              # from environment
```

Rules:

- Load secrets from environment variables or a secrets manager (AWS Secrets
  Manager, Vault, GCP Secret Manager)
- Rotate credentials on any suspected exposure
- Use separate credentials per environment (dev, staging, prod)
- Add `.env` to `.gitignore` and never commit it

```python
# Django — django-environ
import environ
env = environ.Env()
environ.Env.read_env()  # reads .env in development only

SECRET_KEY = env("SECRET_KEY")
DATABASE_URL = env.db("DATABASE_URL")
```

## Rate Limiting

Protect authentication endpoints. Brute-force without rate limiting can
compromise any account with a weak password.

```kotlin
// Spring Boot — Bucket4j
@Component
class RateLimitFilter(private val rateLimiter: RateLimiter) : OncePerRequestFilter() {

    override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, chain: FilterChain) {
        if (request.requestURI.startsWith("/api/auth/")) {
            val probe = rateLimiter.tryConsumeAndReturnRemaining(1)
            if (!probe.isConsumed) {
                response.status = HttpStatus.TOO_MANY_REQUESTS.value()
                return
            }
        }
        chain.doFilter(request, response)
    }
}
```

Minimum rate limits for auth endpoints: 5 requests per minute per IP. Stricter
limits (e.g., 3 per 15 minutes) are better for login endpoints.

## SQL Injection Prevention

Never concatenate user input into SQL strings. Always use parameterized queries
or an ORM.

```kotlin
// bad — SQL injection risk
val user = jdbcTemplate.queryForObject("SELECT * FROM users WHERE email = '$email'")

// good — parameterized
val user = jdbcTemplate.queryForObject(
    "SELECT * FROM users WHERE email = ?",
    UserRowMapper(),
    email
)
```

```python
# bad
User.objects.raw(f"SELECT * FROM users WHERE email = '{email}'")

# good — Django ORM handles parameterization automatically
User.objects.filter(email=email)
```

## Logging Security Events

Log authentication events without including sensitive data.

```text
Log:   login success/failure, permission denied, password reset, token refresh
Never: passwords, tokens, PII (full email in prod logs), credit card numbers
```

```kotlin
// Log security events at INFO level; failures at WARN
log.info("Authentication successful for user={}", userId)
log.warn("Authentication failed: invalid credentials for user={}", maskedEmail)
log.warn("Authorization denied: user={} attempted access to resource={}", userId, resourceId)
```
