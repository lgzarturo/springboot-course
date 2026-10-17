---
name: testing-tdd
description:
  Guides agents through Red-Green-Refactor with runner-captured evidence.
  Use when running /cc-tdd-cycle, writing tests before implementation, or
  Global TDD required is yes.
---

# Test-Driven Development

## Overview

Red (failing test) → Green (minimal code) → Refactor. Evidence comes from
`npx cc-codeconductor tdd capture`, not handmade JSON.

## When to Use

- `/cc-tdd-cycle`, new behavior, bug fixes, TDD-required OpenSpec items

**NOT** for docs-only changes or when the Task Card forbids tests.

## Process

1. Write the failing test that encodes one acceptance criterion. Run the suite.
   It MUST fail (`suiteFails === true`).
2. Implement the minimum that turns it green. Do not expand scope.
3. Refactor only with a green suite.
4. Capture evidence via the verification runner (`openspec done` on test/implement
   when TDD is required).
5. Cover happy path, edge, and error for each behavior.

Pyramid default: many unit, fewer integration, rare E2E.

## Common Rationalizations

| Rationalization | Reality |
| --- | --- |
| I'll add tests later | Later means never. Red first. |
| This is too small to test | If it can break, it needs a failing test first. |
| I'll write the evidence JSON | Handmade TDD JSON is rejected. |

## Red Flags

- Tests that assert implementation details instead of behavior
- Green without a recorded red
- Skipping error cases

## Verification

- [ ] Suite failed before implement
- [ ] Suite passed after implement
- [ ] Runner evidence exists (not handmade)
- [ ] Optional: `npx cc-codeconductor scorecard suite-run --suite workflow-gates`
