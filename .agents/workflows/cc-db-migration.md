---
name: cc-db-migration
description:
  Run the database migration workflow for schema/data changes, operational
  sequencing, tests, and review.
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
4. Next command spelling on this runner: `/cc-db-migration`

Run the CLI as `npx cc-codeconductor <same argv>`.

---


## Step 1 — Task Card validation (task-coach)

Invoke `task-coach` with the request above.

The Task Card must classify the task as `high` risk and include:

- tables, collections, models, or migration files in scope
- data backfill or data cleanup requirements
- deployment ordering and compatibility window
- rollback or forward-fix strategy
- lock risk, data risk, and verification commands

**STOP here. Show the Task Card and wait for human confirmation.**

---

## Step 2 — Migration Plan (architect)

Invoke `architect` with the approved Task Card.

architect must define the schema/data plan, operational sequencing,
compatibility strategy, rollback/forward-fix notes, and test approach.

**STOP here. Show the Technical Plan and wait for explicit human approval.**

---

## Step 3 — Migration tests (tester)

Invoke `tester`.

tester must cover migration-sensitive behavior where the stack supports it,
including happy path, existing-data edge cases, and rollback/forward-fix notes
when automated rollback tests are not practical.

---

## Step 4 — Implementation (implementer)

Invoke `implementer` with the approved plan.

implementer must keep model and migration changes together, avoid unrelated
refactors, and preserve the deployment order specified by architect.

---

## Step 5 — Review (reviewer)

Invoke `reviewer`.

reviewer must block on missing migration tests, missing data-risk notes,
undocumented deployment sequencing, or model/migration drift.

---

## Completion

Report migration files changed, model files changed, tests run, lock risk, data
risk, operational sequencing, rollback/forward-fix notes, and residual risk.

## Next

Run `/cc-review` on the diff before merging.
