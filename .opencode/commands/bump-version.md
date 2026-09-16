---
description: Detecta bump semántico (major/minor/patch) desde commits o según instrucción del usuario y actualiza versión
---

Detecta el tipo de proyecto:
- package.json → Next.js / Astro / TypeScript
- pyproject.toml → Python + UV (Django o FastAPI)
- build.gradle.kts o pom.xml → Spring Boot + Kotlin

Analiza commits desde el último tag para inferir el bump (feat = minor, fix = patch, BREAKING CHANGE = major).
Si el usuario indica explícitamente "major", "minor" o "patch", respeta eso.
Actualiza la versión en el archivo correspondiente.
Crea un commit con mensaje "chore: bump version to vX.Y.Z"
Crea un tag anotado: git tag -a vX.Y.Z -m "Release vX.Y.Z"
Pregúntame antes de ejecutar cualquier git command.
