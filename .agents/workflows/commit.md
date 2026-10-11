---
name: commit
description: Generates a commit in English following Conventional Commits based on staged changes
---

// turbo-all

## Steps

1. View staged changes: `git diff --cached --stat`
2. If nothing is staged, suggest `git add .` or specific files
3. Get full diff: `git diff --cached`
4. Check recent style: `git log -n 3 --oneline`
5. Generate message following `.agents/rules/commit-style.md`
6. Show proposal and confirm with the user
7. Execute: `git commit -m "<message>"`
8. Confirm success: `git status`
