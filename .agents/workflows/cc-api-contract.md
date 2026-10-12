---
name: cc-api-contract
description:
  Run the API contract workflow for request/response shape changes,
  compatibility constraints, contract tests, documentation, and review.
---

# API Contract Workflow

API contract request: $ARGUMENTS

## Step 0 — CCEP Bootstrap

Command: `api-contract` (fixed for this workflow — do not infer from user text)

1. Run: `npx cc-codeconductor ccep profile api-contract --output json` to get the phases and their roles.
2. For each delegated phase, run: `npx cc-codeconductor ccep compile --command api-contract --phase <phase-id> "$ARGUMENTS" --view prompt --output json`
3. After planner/intake JSON is available, run: `npx cc-codeconductor ccep evaluate --command api-contract --input <planner.json> --output json`. If `stop` is true, show questions or risks and wait for human input.
4. Pass each subagent only the compiled `prompt` for its phase — never forward raw `$ARGUMENTS` to planners.

---

## Step 0b — OpenSpec quality gates

If `openspec status` reports an active change folder:

1. Run: `npx cc-codeconductor openspec validate --output json`
2. Run: `npx cc-codeconductor openspec analyze --output json`
3. If analyze `stop` is true or any finding is CRITICAL, stop. Do not delegate to implementer.
4. Next command spelling on this runner: `/cc-api-contract`

Run the CLI as `npx cc-codeconductor <same argv>`.

---


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

## Next

Run `/cc-review` on the diff before merging.
