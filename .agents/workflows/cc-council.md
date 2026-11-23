---
name: cc-council
description: Run the full Council-Driven Development workflow — SDD spec creation, TDD enforcement, surgical implementation, and multi-perspective review.
---

<!-- Source of truth for the council workflow command. The cursor, claude and opencode copies are generated from this template by scripts/inject-ccep-bootstrap.ts; gemini and codex derive from the cursor copy via scripts/render-agent-commands.ts. Edit here, then regenerate. -->

# Council-Driven Workflow

Task request: $ARGUMENTS

## Step 0 — CCEP Bootstrap

Command: `council` (fixed for this workflow — do not infer from user text)
command: council

1. Run: `npx cc-codeconductor ccep profile council --output json` to get the phases and their roles.
2. For each delegated phase, run: `npx cc-codeconductor ccep compile --command council --phase <phase-id> "$ARGUMENTS" --view prompt --output json`
3. After planner/intake JSON is available, run: `npx cc-codeconductor ccep evaluate --command council --input <planner.json> --output json`. If `stop` is true, show questions or risks and wait for human input.
4. Pass each subagent only the compiled `prompt` for its phase — never forward raw `$ARGUMENTS` to planners.

---

## Step 1 — Wayfinding (repo-explorer)

If `graphify-out/graph.json` exists, run `graphify query "$ARGUMENTS"` (and
`graphify path` / `graphify explain` when needed). Then invoke `repo-explorer`
to map impact radius before deliberation. Do not write code in this step.

---

## Step 2 — Deliberation & Specification (SDD)

Invoke the `council` skill to analyze the request before writing any code. The council must act as a steering committee involving `task-coach` (Product), `architect`, and `devil`.

The council must:
1. Clarify the prompt and define the absolute minimum scope (Simplicity Gate).
2. Explicitly document all assumptions and run the Grilling protocol on each one (Think Before Coding).
3. Draft a Task Card & Technical Plan (The Specification).

**STOP here.** Unresolved grilling questions populate `questionsForUser` in the
CCEP-1 `planner-output`; `ccep evaluate` (ConfirmationGate) halts until a human
answers. Show the agreed Task Card & Technical Plan and wait for confirmation
before continuing.

---

## Step 3 — Test Definition (TDD)

Invoke `tester` with the approved Task Card & Technical Plan.

tester must:
1. Write failing tests based on the Acceptance Criteria defined in the Task Card.
2. Confirm the tests fail as expected (Red state).

**Goal-Driven Execution (Karpathy)**: Do not proceed until verifiable tests are written and fail for the correct reasons.

---

## Step 4 — Surgical Implementation

Invoke `implementer` with the failing tests and the Technical Plan.

implementer must:
1. Write the minimal code required to pass the tests.
2. Touch ONLY the files specified in the Technical Plan (Surgical Changes).
3. NOT refactor adjacent code, change existing styles, or build speculative features.
4. Run the tests. Loop `implementer` -> `tester` until all tests pass (Green state), at most 3 iterations -- then stop and report the failing tests instead of looping.

---

## Step 5 — Multi-Perspective Council Review

Invoke the `council` skill on the generated diff to perform the final review phase.

The council will evaluate the diff against the 6 axes (Architecture, Security, Product, Delivery, DataOps, Devil).

If ANY agent votes CRITICAL (especially due to over-engineering, scope creep, or missing the verifiable goals):
- The Review Report status is **BLOCKED**.
- Return to Step 4 with the feedback (at most 3 review rounds in total -- then deliver the BLOCKED verdict with the unresolved findings instead of looping).

If APPROVED (no CRITICAL findings):
- Deliver the final Council Verdict and the diff summary.

---

## Completion

Deliver the complete Council Verdict. The feature is only complete when tests pass and the council explicitly approves the implementation according to the specification.

## Next

Approved: merge. Blocked: return the findings to `/cc-fix` or `/cc-feature`.
