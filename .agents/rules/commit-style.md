---
trigger: always_on
description: Reglas para generar mensajes de commit con formato Conventional Commits
---

## Git Commit Messages

### Formato

```
<tipo>(<scope>): <descripcion corta>

- <detalle 1>
- <detalle 2>
```

### Tipos validos

`feat` `fix` `docs` `style` `refactor` `test` `chore` `perf` `ci` `build`
`revert`

### Reglas

- Idioma: **español neutro** siempre
- Encabezado: maximo 69 caracteres, sin punto final
- Cuerpo: viñetas concisas, una idea por linea
- Footer: solo para breaking changes o issues
- Sin gerundios ("agregando", "corrigiendo")
- Usar infinitivo o imperativo ("agregar", "corregir", "actualizar")
