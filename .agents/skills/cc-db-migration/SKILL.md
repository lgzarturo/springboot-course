---
name: cc-db-migration
description:
  Run the database migration workflow for schema/data changes, operational
  sequencing, tests, and review.
---

# Database Migration Workflow

Migration request: $ARGUMENTS

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

## Step 3 — Implementation (implementer)

Invoke `implementer` with the approved plan.

implementer must keep model and migration changes together, avoid unrelated
refactors, and preserve the deployment order specified by architect.

---

## Step 4 — Migration tests (tester)

Invoke `tester`.

tester must cover migration-sensitive behavior where the stack supports it,
including happy path, existing-data edge cases, and rollback/forward-fix notes
when automated rollback tests are not practical.

---

## Step 5 — Review (reviewer)

Invoke `reviewer`.

reviewer must block on missing migration tests, missing data-risk notes,
undocumented deployment sequencing, or model/migration drift.

---

## Completion

Report migration files changed, model files changed, tests run, lock risk, data
risk, operational sequencing, rollback/forward-fix notes, and residual risk.
