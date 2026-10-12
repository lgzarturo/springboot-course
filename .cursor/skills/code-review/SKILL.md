---
id: code-review
name: code-review
description: >
  Provides a structured framework for conducting thorough code reviews covering correctness, security, architecture alignment, performance, and technical debt.

user-invokable: true
license: MIT
metadata:
  author: lgzarturo
  category: review

compatibility:
  tools: [claude, codex, gemini, agy, opencode]
  stacks:
    languages: []
    frameworks: []

risk:
  level: low
  can_execute_shell: false
  can_modify_files: true
  requires_network: false

inputs: []

outputs: []

quality:
  reviewed_by: codeconductor-core
  version: 0.1.0
---



# Code Review

## Review Axes

Every finding must reference exactly one axis. This prevents vague feedback and
makes it actionable.

| Axis | What to check |
|------|--------------|
| **Correctness** | Does the logic handle all cases in the acceptance criteria? |
| **Security** | Injection vectors, secret exposure, auth bypasses, insecure defaults |
| **Architecture** | Does the code follow existing patterns and module boundaries? |
| **Performance** | N+1 queries, unnecessary allocations, blocking I/O in hot paths |
| **Error handling** | Are failure cases handled explicitly and safely? |
| **Test coverage** | Do tests verify the acceptance criteria, not just happy paths? |
| **Scope** | Are there changes outside the stated task boundary? |
| **Technical debt** | Does the implementation introduce debt without acknowledging it? |

## Finding Categories

### CRITICAL — must be resolved before merge

- Logic failures that violate acceptance criteria
- Security vulnerabilities (injection, auth bypass, secret exposure)
- Data loss or corruption risk
- Breaking changes to public API or shared contracts
- Missing error handling that causes silent failures

### WARNING — should be resolved before merge

- Missing edge case coverage in tests
- Scope creep (changes outside the task boundary)
- Pattern inconsistency that will confuse future maintainers
- Performance issue that will degrade under load
- Missing input validation at system boundaries

### SUGGESTION — optional improvement

- Naming clarity improvements
- Extracting a reusable abstraction
- Test readability
- Documentation gaps on non-obvious behavior

## Review Report Format

```markdown
## Review Report

**Task**: [objective from task card]
**Verdict**: [approved | approved with warnings | blocked]

---

### CRITICAL

- [ ] [C1] `path/to/file:line` — [description]
  Axis: [axis] | Evidence: `[quoted code]` | Required action: [what must change]

_(none)_ if no critical findings

### WARNING

- [ ] [W1] `path/to/file:line` — [description]
  Axis: [axis] | Recommended action: [what should change]

_(none)_ if no warning findings

### SUGGESTION

- [ ] [S1] — [description] | Rationale: [brief reason]

_(none)_ if no suggestions

### Summary

- Critical: [count] | Warning: [count] | Suggestion: [count]
- **Verdict justification**: [one sentence]
```

**Verdict rules:**

- `blocked` — any CRITICAL finding present
- `approved with warnings` — no CRITICAL, at least one WARNING
- `approved` — no CRITICAL, no WARNING

## What to Check — by Axis

### Correctness

- Does every acceptance criterion have a corresponding code path?
- Are null/empty/zero values handled explicitly?
- Are boundary conditions checked (off-by-one, empty collections, max values)?
- Do conditional branches cover all cases (exhaustive when/switch)?

### Security

```text
Injection: SQL, command, LDAP, XML — is input sanitized or parameterized?
Auth: is the endpoint protected? are role checks correct?
Secrets: no hardcoded credentials, tokens, or keys in source
Headers: are security headers set (CSP, HSTS, X-Frame-Options)?
CORS: is the origin whitelist explicit, not wildcard?
Dependencies: are new dependencies from trusted sources?
```

### Performance

```text
N+1 queries: does a loop call the database per iteration?
Eager loading: are JOINs or includes used where needed?
Pagination: are all list endpoints paginated?
Caching: is expensive computation cached at an appropriate layer?
Index: do new query filters have corresponding DB indexes?
```

### Error Handling

```text
Are exceptions caught at the right layer (not swallowed silently)?
Is the error response format consistent with the rest of the API?
Is the original exception logged before translating to a user-facing error?
Are transient failures retried with backoff, or propagated immediately?
```

## What NOT to Flag

**Personal style preferences.** If the code follows the project's established
conventions, do not flag it because you would have written it differently.

**Trivial naming.** Variable names are a SUGGESTION at most, never a WARNING.
Do not block a PR over naming unless the name is genuinely misleading.

**Tests for trivial code.** Do not demand tests for getters, setters, or data
class construction. Test behavior, not boilerplate.

**Refactors not in scope.** If you notice an improvement opportunity outside
the task boundary, log it as a SUGGESTION. Do not block the PR for work that
was not requested.

**Framework defaults.** Do not second-guess framework conventions (Spring
dependency injection, Next.js file-based routing). Trust the framework.

## Exact Diff Recommendations

When a finding requires a code change, provide the exact replacement:

```text
# Current code (path/to/file:42)
val user = userRepository.findById(id)
return user.name   // NPE if not found

# Recommended fix
val user = userRepository.findById(id)
  ?: throw NotFoundException("User $id not found")
return user.name
```

Vague instructions ("handle the null case") force the author to guess your
intent. Exact diffs remove ambiguity and speed up the review cycle.

## Prioritization Order

When multiple findings exist, address them in this order:

1. Security — security issues can silently compromise users or data
2. Correctness — broken behavior defeats the purpose of the change
3. Error handling — silent failures are hard to debug in production
4. Test coverage — gaps mean regressions will be invisible
5. Performance — degrade gradually; fix before it causes incidents
6. Architecture — inconsistency compounds over time
7. Technical debt — acknowledge and track; do not always block for it
8. Suggestions — apply at author's discretion
