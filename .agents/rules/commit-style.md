---
trigger: always_on
description: Rules for generating commit messages using Conventional Commits format
---

## Git Commit Messages

### Format

```
<type>(<scope>): <short description>

- <detail 1>
- <detail 2>
```

### Valid Types

`feat` `fix` `docs` `style` `refactor` `test` `chore` `perf` `ci` `build`
`revert`

### Rules

- Language: **neutral English** always
- Header: maximum 69 characters, no trailing period
- Body: concise bullet points, one idea per line
- Footer: only for breaking changes or issues
- No gerunds ("adding", "fixing")
- Use infinitive or imperative ("add", "fix", "update")
