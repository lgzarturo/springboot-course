# 📝 Guía Rápida de Commits

> **Referencia rápida para commits con Conventional Commits**
>
> Para documentación completa, consulta [WORKFLOW.md](WORKFLOW.md)

---

## Estructura Básica

```
<tipo>(alcance): <descripción>

[cuerpo opcional]

[footer opcional]
```

---

## Tipos de Commit

| Tipo       | Cuándo Usarlo                             | Versión |
|------------|-------------------------------------------|---------|
| `feat`     | Nueva funcionalidad                       | 0.X.0   |
| `fix`      | Corrección de bugs                        | 0.0.X   |
| `docs`     | Solo documentación                        | -       |
| `style`    | Formato, espacios (sin cambios de código) | -       |
| `refactor` | Refactorización                           | -       |
| `perf`     | Mejoras de rendimiento                    | 0.0.X   |
| `test`     | Tests                                     | -       |
| `build`    | Sistema de build, dependencias            | -       |
| `ci`       | CI/CD                                     | -       |
| `chore`    | Mantenimiento                             | -       |

---

## Ejemplos Rápidos

### ✅ Buenos Commits

```bash
feat(reservas): agregar endpoint para crear reservas
fix(pagos): corregir cálculo de impuestos
docs(readme): actualizar sección de instalación
test(controller): agregar tests para PingController
refactor(service): extraer lógica de validación
perf(query): optimizar consulta de disponibilidad
```

### Con Cuerpo

```bash
git commit -m "feat(api): implementar paginación

- Agregar parámetros page, size y sort
- Actualizar DTOs con metadata de paginación
- Documentar endpoints en OpenAPI

Closes #42"
```

### Breaking Change

```bash
git commit -m "feat(auth)!: cambiar estructura de respuesta

BREAKING CHANGE: El token ahora se retorna en un objeto
con accessToken y refreshToken en lugar de string simple"
```

---

## Alcances Comunes

- `api` - Cambios en API REST
- `domain` - Lógica de dominio/negocio
- `service` - Servicios
- `controller` - Controladores
- `repository` - Repositorios
- `model` - Modelos/DTOs
- `config` - Configuración
- `test` - Tests
- `docs` - Documentación
- Nombres de módulos específicos: `reservas`, `pagos`, `habitaciones`, etc.

---

## Comandos Útiles

```bash
# Ver cambios
git status
git diff

# Commit interactivo
git add -p

# Ver último commit
git log -1

# Modificar último commit (antes de push)
git commit --amend

# Ver historial bonito
git log --oneline --graph --decorate
```

---

## Checklist Antes de Commit

- [ ] ✅ Tests pasan: `.\gradlew.bat test`
- [ ] ✅ Linter sin errores: `.\gradlew.bat ktlintCheck`
- [ ] ✅ Código revisado: `git diff`
- [ ] ✅ Mensaje sigue Conventional Commits
- [ ] ✅ Descripción clara y específica
- [ ] ✅ Referencia a issue si aplica

---

## Errores Comunes

### ❌ Evitar

```bash
git commit -m "cambios"
git commit -m "fix"
git commit -m "WIP"
git commit -m "Actualización"
git commit -m "agregar función"  # Falta tipo
```

### ✅ Correcto

```bash
git commit -m "feat(pagos): agregar validación de tarjeta"
git commit -m "fix(auth): corregir validación de token expirado"
git commit -m "docs(api): actualizar ejemplos de endpoints"
git commit -m "test(service): agregar casos de error"
```

---

## Referencias a Issues

```bash
# Cierra el issue automáticamente
Closes #123
Fixes #456

# Referencia sin cerrar
Refs #789
See #101
```

---

## Plantilla de Commit

Configura la plantilla para ayudarte:

```bash
git config commit.template .gitmessage
```

Luego solo haz `git commit` (sin `-m`) y se abrirá tu editor con la plantilla.

---

## Flujo Rápido

```bash
# 1. Crear rama
git checkout -b feature/mi-feature

# 2. Hacer cambios
# ... editar archivos ...

# 3. Agregar y commit
git add .
git commit -m "feat(modulo): descripción clara"

# 4. Verificar tests
.\gradlew.bat test

# 5. Push
git push -u origin feature/mi-feature

# 6. Crear Pull Request en GitHub
```

---

## Recursos

- **Documentación completa**: [WORKFLOW.md](WORKFLOW.md)
- **Especificación**: https://www.conventionalcommits.org/
- **Semantic Versioning**: https://semver.org/

---

## Ayuda

¿Dudas? Abre un Issue con la etiqueta `question`

**¡Happy Coding! 🚀**
