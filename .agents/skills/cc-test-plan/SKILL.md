---
name: cc-test-plan
description:
  Generate a structured test plan for a feature or module — covers unit,
  integration, contract, and edge cases without writing any implementation code.
---

# Test Plan Workflow

Scope: $ARGUMENTS

Specify what to plan tests for. Examples:

- A feature name: `user authentication`
- A module or file path: `src/orders/OrderService.kt`
- A Task Card title: `Add paginated product listing endpoint`
- A PR or branch: `feature/payment-retry`

If $ARGUMENTS is empty, describe the scope in your next message before invoking
`tester`.

---

## Step 1 — Scope confirmation

Before invoking `tester`, confirm the scope is well-defined.

A valid scope includes:

- The behavior or module under test
- The acceptance criteria or expected behavior (from the Task Card if available)
- Known edge cases or failure modes

If the scope is vague (e.g., "test the whole service"), ask one clarifying
question and wait for the answer.

---

## Step 2 — Test plan generation (tester)

Invoke `tester` in planning mode — produce a test plan document, not test code.
The plan will be used as input when tests are actually written.

tester must produce a Test Plan covering the following layers:

**Unit tests**

- Individual functions or methods in isolation
- One test per behavior, not per method
- Input/output contracts, null handling, type coercion

**Integration tests**

- Interactions between two or more components
- Database read/write cycles (if applicable)
- External service boundaries (mocked or stubbed)

**Contract tests**

- API endpoint contracts: request shape, response shape, status codes
- Event schema contracts (if event-driven components are in scope)

**Edge cases**

- Empty inputs, boundary values, max/min limits
- Concurrent access (if shared state is involved)
- Failure paths: what happens when a dependency is unavailable

**Regression cases**

- Known past bugs that must not recur (include reference if available)

---

## Step 3 — Test Plan format

tester produces the plan in this format:

```markdown
## Test Plan — [Scope Name]

### Scope

[What is being tested and why]

### Unit Tests

| Test ID | Target | Scenario | Expected Result |
| ------- | ------ | -------- | --------------- |
| U-001   | ...    | ...      | ...             |

### Integration Tests

| Test ID | Components | Scenario | Expected Result |
| ------- | ---------- | -------- | --------------- |
| I-001   | ...        | ...      | ...             |

### Contract Tests

| Test ID | Endpoint/Event | Property | Expected Value |
| ------- | -------------- | -------- | -------------- |
| C-001   | ...            | ...      | ...            |

### Edge Cases

| Test ID | Input/Condition | Expected Behavior |
| ------- | --------------- | ----------------- |
| E-001   | ...             | ...               |

### Regression Cases

| Test ID | Reference | Scenario | Must Not Happen |
| ------- | --------- | -------- | --------------- |
| R-001   | ...       | ...      | ...             |

### Coverage Targets

- Minimum unit coverage: [%] (or "all acceptance criteria covered")
- Integration scenarios: [count]
- Contract validations: [count]

### Out of Scope

[What this test plan explicitly does not cover and why]
```

---

## Step 4 — Human review

Show the Test Plan to the human before any tests are written.

The Test Plan is an artifact for review and approval. Writing test code is a
separate action — invoke `/cc:feature` or add a test task to the board to
implement from this plan.

---

## Completion

Deliver the complete Test Plan document. Save it as
`docs/test-plans/[scope-slug].md` if the human requests persistence.

This command produces a plan, not test files. No production code and no test
code is written during this command.
