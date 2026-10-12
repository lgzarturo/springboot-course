---
name: cc-api-contract
description:
  Run the API contract workflow for request/response shape changes,
  compatibility constraints, contract tests, documentation, and review.
---

# API Contract Workflow

API contract request: $ARGUMENTS

## Step 1 — Task Card validation (task-coach)

Invoke `task-coach` with the request above.

The Task Card must classify the task as `feature` or `refactor` with `high`
risk unless the human provides a narrower validated risk. It must include:

- affected endpoint, command, or public interface
- request and response examples
- backward compatibility and versioning impact
- consumers that must remain compatible
- contract tests and documentation acceptance criteria

**STOP here. Show the Task Card and wait for human confirmation.**

---

## Step 2 — Technical Plan (architect)

Invoke `architect` with the approved Task Card.

architect must define the API contract, compatibility strategy, validation
rules, docs/OpenAPI impact, and migration path for consumers.

**STOP here. Show the Technical Plan and wait for explicit human approval.**

---

## Step 3 — Implementation (implementer)

Invoke `implementer` with the approved plan.

implementer must apply the minimal diff, preserve compatible behavior unless a
breaking change was explicitly approved, and update only the files named in the
plan.

---

## Step 4 — Contract tests (tester)

Invoke `tester`.

tester must add or update contract tests covering request shape, response shape,
status/error behavior, and backward compatibility constraints.

---

## Step 5 — Review (reviewer)

Invoke `reviewer`.

reviewer must block on missing contract tests, undocumented breaking changes,
or docs/OpenAPI drift.

---

## Completion

Report the final Task Card, Technical Plan, implementation summary, test report,
review report, docs updated, compatibility impact, and residual risks.
