---
description: >-
  Run the database migration workflow for schema/data changes,
  operational sequencing, tests, and review.
---

# Database Migration Workflow

Migration request: $ARGUMENTS

## Step 0 — CCEP Bootstrap

Command: `db-migration` (fixed for this workflow — do not infer from user text)

1. Run: `npx cc-codeconductor ccep profile db-migration --output json` to get the phases and their roles.
2. For each delegated phase, run: `npx cc-codeconductor ccep compile --command db-migration --phase <phase-id> "$ARGUMENTS" --view prompt --output json`
3. After planner/intake JSON is available, run: `npx cc-codeconductor ccep evaluate --command db-migration --input <planner.json> --output json`. If `stop` is true, show questions or risks and wait for human input.
4. Pass each subagent only the compiled `prompt` for its phase — never forward raw `$ARGUMENTS` to planners.
   Canonical delivery order is test-before-implement whenever both phases apply.

---

## Step 0b — OpenSpec quality gates

If `openspec status` reports an active change folder:

1. Run: `npx cc-codeconductor openspec validate --output json`
2. Run: `npx cc-codeconductor openspec analyze --output json`
3. If analyze `stop` is true or any finding is CRITICAL, stop. Do not delegate to implementer.
4. Next command spelling on this runner: `/cc:db-migration`

Run the CLI as `npx cc-codeconductor <same argv>`.

---


## Step 1 — Task Card validation (Task Coach role)

Invoke the `task-coach` subagent via the Task tool.

The Task Card must classify the task as high risk and include affected schema,
model, and migration files; data backfill needs; deployment ordering;
rollback/forward-fix strategy; lock risk; data risk; and verification commands.

**STOP here. Show the completed Task Card and wait for human confirmation.**

---

## Step 2 — Migration Plan (Architect role)

Invoke the `architect` subagent via the Task tool.

Define the schema/data plan, operational sequencing, compatibility strategy,
rollback/forward-fix notes, and test approach.

**STOP here. Show the Technical Plan and wait for explicit human approval.**

---

## Step 3 — Migration Tests (Tester role)

Invoke the `tester` subagent via the Task tool.

Cover migration-sensitive behavior where the stack supports it, including
existing-data edge cases and rollback/forward-fix notes when automated rollback
tests are not practical.

---

## Step 4 — Implementation (Implementer role)

Invoke the `implementer` subagent via the Task tool.

Keep model and migration changes together, avoid unrelated refactors, and
preserve the deployment order specified by architect.

---

## Step 5 — Review (Reviewer role)

Invoke the `reviewer` subagent via the Task tool.

Block on missing migration tests, missing data-risk notes, undocumented
deployment sequencing, or model/migration drift.

## Next

Run `/cc:review` on the diff before merging.
