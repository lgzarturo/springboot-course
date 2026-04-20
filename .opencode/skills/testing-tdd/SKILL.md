---
name: testing-tdd
description: Genera y ejecuta tests siguiendo TDD (Red-Green-Refactor) + tests unitarios, integración y e2e
license: MIT
---

## Reglas obligatorias (TDD + Testing)
- Siempre sigue el ciclo TDD: 1. Red, 2. Green, 3. Refactor.
- Usa fixtures/factories y mocks reales.
- Tests independientes, rápidos y deterministas.

## Reglas por stack
**Spring Boot + Kotlin** → JUnit 5 + Kotest + Testcontainers
**Python** → pytest + factories
**Next.js / Astro** → Vitest + React Testing Library + Playwright

## Cuándo usarme
- "Escribe los tests TDD para esta función"
- "Añade test de integración para el endpoint /orders"
