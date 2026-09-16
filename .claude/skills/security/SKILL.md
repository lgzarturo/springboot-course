---
name: security
description: Implementa y revisa seguridad OWASP Top 10
license: MIT
---

## Reglas obligatorias
- Valida input, rate limiting, CORS estricto, headers de seguridad.
- Sigue OWASP Top 10 2025.

## Reglas por stack
**Spring Boot** → Spring Security 6 + JWT
**Python** → django-allauth / FastAPI Users
**Next.js / Astro** → NextAuth v5 / Auth.js

## Cuándo usarme
- "Añade autenticación JWT segura"
- "Revisa este código por vulnerabilidades"
