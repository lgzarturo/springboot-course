---
id: spring-auth-auditor
name: spring-auth-auditor
description: >
  Secures Spring Security filters, JWT validations, OAuth2 setups, and CORS/CSRF headers.
user-invokable: true
license: MIT
metadata:
  author: lgzarturo
  category: security
compatibility:
  tools: [claude, codex, gemini, agy, opencode]
  stacks:
    languages: [kotlin, java]
    frameworks: [spring-boot, spring-security]
paths:
  - "**/*.kt"
  - "**/Security*.kt"
---
# Spring Auth Auditor

## Core Principles

1. **Explicit Security Filters**: Define clear SecurityFilterChain configurations. Do not use default broad rules.
2. **CORS & CSRF**: Always specify explicit CORS origins and keep CSRF protection enabled unless stateless API endpoints with JWT are explicitly verified.
3. **No Mutations in Production**: Configurations must not mutate settings based on dev/prod environments dynamically in an unsafe manner.

## Verification Checklist

- Validate JWT signature, issuer, and expiration time inside `OncePerRequestFilter`.
- Do not store JWT tokens in client-accessible localStorage. Promote Cookie-based Auth with `HttpOnly` flags.
- Ensure that endpoints matching `/api/public/**` or similar exclusions are intentionally public.
