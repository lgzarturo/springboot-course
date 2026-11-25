---
name: cc-council
description: "Multi-agent council for code review and architecture decisions"
---

# Council-Driven Workflow

Task request: $ARGUMENTS

## Step 1 — Deliberation & Specification (SDD)

Invoke the `council` skill to analyze the request before writing any code. The council must act as a steering committee involving `task-coach` (Product), `architect`, and `devil`.

The council must:
1. Clarify the prompt and define the absolute minimum scope (Simplicity Gate).
2. Explicitly document all assumptions and resolve ambiguities (Think Before Coding).
3. Draft a Task Card & Technical Plan (The Specification).

**STOP here. Show the agreed Task Card & Technical Plan and wait for human confirmation before continuing.**

---

## Step 2 — Test Definition (TDD)

Invoke `tester` with the approved Task Card & Technical Plan.

tester must:
1. Write failing tests based on the Acceptance Criteria defined in the Task Card.
2. Confirm the tests fail as expected (Red state).

**Goal-Driven Execution (Karpathy)**: Do not proceed until verifiable tests are written and fail for the correct reasons.

---

## Step 3 — Surgical Implementation

Invoke `implementer` with the failing tests and the Technical Plan.

implementer must:
1. Write the minimal code required to pass the tests.
2. Touch ONLY the files specified in the Technical Plan (Surgical Changes).
3. NOT refactor adjacent code, change existing styles, or build speculative features.
4. Run the tests. Loop `implementer` -> `tester` until all tests pass (Green state).

---

## Step 4 — Multi-Perspective Council Review

Invoke the `council` skill on the generated diff to perform the final review phase.

The council will evaluate the diff against the 6 axes (Architecture, Security, Product, Delivery, DataOps, Devil).

If ANY agent votes CRITICAL (especially due to over-engineering, scope creep, or missing the verifiable goals):
- The Review Report status is **BLOCKED**.
- Return to Step 3 with the feedback.

If APPROVED (no CRITICAL findings):
- Deliver the final Council Verdict and the diff summary.

---

## Completion

Deliver the complete Council Verdict. The feature is only complete when tests pass and the council explicitly approves the implementation according to the specification.
