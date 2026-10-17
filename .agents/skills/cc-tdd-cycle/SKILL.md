---
name: cc-tdd-cycle
description: >-
  Run a structured Red-Green-Refactor TDD cycle — write a failing test first,
  implement the minimum code to pass it, then refactor with the suite green.
---

# TDD Cycle — Red → Green → Refactor

Scope: $ARGUMENTS

Describe what behavior you want to implement. Include:

- The function, method, or feature to implement
- The expected behavior (inputs and outputs, or acceptance criteria)
- Any known constraints or edge cases
- Relevant files or modules (if known)

---

## Before you begin — mandatory pre-check

This command enforces strict TDD discipline. The three phases are sequential and
non-negotiable:

1. **RED** — a failing test exists before any implementation code is written
2. **GREEN** — the minimum implementation to make the test pass (no more)
3. **REFACTOR** — clean up the code while keeping all tests green

Do not write implementation code during RED. Do not refactor during GREEN.
Mixing phases invalidates the cycle.

## Verifiable phase gates

RED → GREEN and GREEN → REFACTOR are enforced by `tddCycleStateMachine` in
`domain/loop`. Evidence must be captured with `npx cc-codeconductor tdd capture` (verification
runner) — do not hand-edit JSON under `.codeconductor/evidence/`.

- RED→GREEN requires runner evidence that the suite **failed**.
- GREEN→REFACTOR requires runner evidence that the suite **passed**.

Do not advance phases until that evidence exists.

When delivering a BACKLOG item, `openspec done` on the test or implement card
uses the same runner evidence.

---

## Phase 1 — RED (Tester role)

Adopt the **Tester** role as defined in `CLAUDE.md`.

### 1a — Scope clarification

Before writing any test, confirm:

- What is the unit of behavior being tested? (function, method, endpoint, domain
  rule)
- What are the inputs and expected outputs?
- What are the failure cases?

If the scope is ambiguous, ask one clarifying question and wait for the answer.

### 1b — Write the failing test

Write a test that:

- Targets exactly the behavior described in the scope
- Fails for the right reason (not a compile error, not a missing dependency —
  the logic does not exist yet)
- Has a name that describes the behavior: `given_X_when_Y_then_Z` or equivalent
  in the project's test naming convention
- Covers at minimum: one happy path, one edge case, one failure case

Do not write the implementation. Do not make the test pass by any means other
than the implementation that will follow in Phase 2.

### 1c — Run the test suite and confirm RED

Run the test suite. The new test must fail. Existing tests must pass.

If the new test passes without implementation, the test is wrong. Fix the test
before continuing.

**RED Phase Report:**

```
## RED Phase Report

**Test file**: [path/to/test/file]
**Tests written**:
- [test name] — [what it verifies]

**Suite result**: [X passing, Y failing]
**New test status**: FAIL ✓
**Failure reason**: [quoted error or assertion message]
**Existing tests**: [all passing | N failing — list]
```

**STOP. Show the RED Phase Report. Do not proceed to GREEN without
confirmation.**

---

## Phase 2 — GREEN (Implementer role)

Adopt the **Implementer** role as defined in `CLAUDE.md`.

### 2a — Read the failing test before writing any code

Understand exactly what the test asserts. Write only the code that satisfies
that assertion. Nothing more.

### 2b — Implement the minimum

Rules for GREEN phase:

- Write the smallest amount of code that makes the failing test pass
- Do not add features not tested
- Do not clean up or restructure existing code — that is Refactor's job
- Do not add new tests — that is another RED cycle
- Hardcoding a return value is acceptable if it makes the test pass (the
  Refactor phase will generalize it)

### 2c — Run the test suite and confirm GREEN

Run the test suite. The new test must pass. All previously passing tests must
still pass.

If any previously passing test now fails, you introduced a regression. Fix it
before continuing.

**GREEN Phase Report:**

```
## GREEN Phase Report

**Files changed**:
- [path/to/file] — [what was added, one sentence]

**Implementation approach**: [one sentence — what the code does]
**Suite result**: [X passing, Y failing]
**New test status**: PASS ✓
**Regressions**: [none | list failing tests]
```

**STOP. Show the GREEN Phase Report. Do not proceed to REFACTOR without
confirmation.**

---

## Phase 3 — REFACTOR (Implementer role, then Reviewer role)

Adopt the **Implementer** role as defined in `CLAUDE.md`.

### 3a — Assess what needs cleaning

Before touching any code, identify:

- Duplication introduced during GREEN
- Names that do not clearly express intent
- Abstractions that belong in a separate function or module
- Logic that is hardcoded and should be generalized

Do not invent improvements. Only address what is directly in the implementation
written in Phase 2.

### 3b — Refactor

Rules for REFACTOR phase:

- All tests must remain GREEN throughout — run the suite after each change
- Do not add new behavior
- Do not add new tests (if you discover untested behavior, note it for a new RED
  cycle)
- Do not change function signatures unless the original was clearly wrong

### 3c — Run the test suite and confirm GREEN after refactor

The full suite must pass. If any test fails during refactor, undo the last
change and investigate.

### 3d — Review (Reviewer role)

Adopt the **Reviewer** role as defined in `CLAUDE.md`.

Review only the refactored code against these axes:

| Axis          | What to check                                                   |
| ------------- | --------------------------------------------------------------- |
| Scope         | Did the refactor change any behavior?                           |
| Correctness   | Does the logic still satisfy the original test intent?          |
| Architecture  | Does the code follow existing project patterns?                 |
| Test coverage | Are all written tests still meaningful (not trivially passing)? |

Produce a Review Report. CRITICAL findings block completion.

**REFACTOR Phase Report:**

```
## REFACTOR Phase Report

**Changes made**:
- [path/to/file] — [what was cleaned up]

**Suite result**: [X passing, Y failing]
**Behavioral changes**: none (refactor only)

### Review findings
**CRITICAL**: (none) | [list]
**WARNING**: (none) | [list]
**SUGGESTION**: (none) | [list]

**Verdict**: approved | approved with warnings | blocked
```

---

## Completion

The TDD cycle is complete when:

- RED: at least one failing test was written and confirmed failing
- GREEN: the minimum implementation makes the test pass
- REFACTOR: the code is clean, all tests pass, no CRITICAL review findings

**Final Summary:**

```
## TDD Cycle Summary

**Behavior implemented**: [one sentence]
**Tests written**: [count] — [list test names]
**Files changed**: [list]
**Suite result**: [X passing, Y failing]
**Cycle status**: complete | blocked (reason)
```

If the behavior requires additional test cases, start a new `/cc:tdd-cycle` with
the next scenario. One cycle = one behavior.
