---
description: >-
  Run the API contract workflow for public interface changes,
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
4. Next command spelling on this runner: `/cc:api-contract`

Run the CLI as `npx cc-codeconductor <same argv>`.

---


## Step 1 — Task Card validation (Task Coach role)

Invoke the `task-coach` subagent via the Task tool.

The Task Card must classify the task as high risk by default and include the
affected public interface, request/response examples, compatibility notes,
versioning impact, consumer impact, contract tests, and docs acceptance
criteria.

**STOP here. Show the completed Task Card and wait for human confirmation.**

---

## Step 2 — Technical Plan (Architect role)

Invoke the `architect` subagent via the Task tool.

Define the API contract, validation strategy, compatibility behavior, docs or
OpenAPI changes, and reviewer blocking conditions.

**STOP here. Show the Technical Plan and wait for explicit human approval.**

---

## Step 3 — Implementation (Implementer role)

Invoke the `implementer` subagent via the Task tool.

Apply the minimal diff, preserve compatible behavior unless explicitly approved
as breaking, and update only the planned files.

---

## Step 4 — Contract Tests (Tester role)

Invoke the `tester` subagent via the Task tool.

Add or update tests for request shape, response shape, status/error behavior,
and backward compatibility.

---

## Step 5 — Review (Reviewer role)

Invoke the `reviewer` subagent via the Task tool.

Block on missing contract tests, undocumented breaking changes, or docs/OpenAPI
drift.

## Next

Run `/cc:review` on the diff before merging.
