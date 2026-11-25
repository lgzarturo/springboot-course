---
name: cc-openspec
description:
  Run OpenSpec backlog delivery — validate BACKLOG.md, plan TaskCards, orchestrate
  agents by phase, review gate, and update backlog state.
---

# OpenSpec Backlog Workflow

Scope: $ARGUMENTS

---

## Web interface scope

When the task concerns web layout, component states, feedback, motion, or a
requested UI audit, apply skill `web-design-engineering` from the installed skill
library. Use it during intake/discovery, design, test/implement, and review as
applicable; keep this workflow’s gates and TDD order. Framework presence alone
does not activate it; backend-only and native mobile work are excluded. Record
required visual checks as pending when unavailable. Keep findings in the existing
Review Report: contract failures in Spec Axis, technical issues in existing
subchecks, and aesthetic preferences as suggestions; do not add an axis.

## Android phone interface scope

When the task concerns concrete Jetpack Compose phone layout, component states,
interaction, accessibility, visual behavior, or a requested UI audit, apply skill
`android-ui-design`. Use it during design, test/implementation, and review as
applicable while keeping this workflow's gates and TDD order. Kotlin, Compose,
framework, or dependency presence alone does not activate it; non-UI Android work
stays with the general `android` skill. Audit-only requests remain read-only, and
unavailable visual or emulator checks remain pending rather than claimed complete.


## Step 0a — Validate (mandatory gate)

Run `npx cc-codeconductor openspec validate`. If invalid, show errors and recommendations, then **STOP**.

---

## Step 0 — CCEP Bootstrap

Command: `openspec` (fixed for this workflow — do not infer from user text)

1. Run: `npx cc-codeconductor ccep profile openspec --output json` to get the phases and their roles.
2. For each delegated phase, run: `npx cc-codeconductor ccep compile --command openspec --phase <phase-id> "$ARGUMENTS" --view prompt --output json`
3. After planner/intake JSON is available, run: `npx cc-codeconductor ccep evaluate --command openspec --input <planner.json> --output json`. If `stop` is true, show questions or risks and wait for human input.
4. Pass each subagent only the compiled `prompt` for its phase — never forward raw `$ARGUMENTS` to planners.
   Canonical delivery order is test-before-implement whenever both phases apply.

---

## Step 0b — OpenSpec quality gates

If `openspec status` reports an active change folder:

1. Run: `npx cc-codeconductor openspec validate --output json`
2. Run: `npx cc-codeconductor openspec analyze --output json`
3. If analyze `stop` is true or any finding is CRITICAL, stop. Do not delegate to implementer.
4. Next command spelling on this runner: `/cc-openspec`

Run the CLI as `npx cc-codeconductor <same argv>`.

---


## Step 1 — Scan

Run `npx cc-codeconductor openspec scan`. Report new, modified, and closed items.

---

## Step 2 — Select item

Use `$ARGUMENTS` BC-id or `npx cc-codeconductor openspec status` for next READY item. **STOP** if none.

---

## Step 3 — Plan

Run `npx cc-codeconductor openspec plan <BC-id>`. Show TaskCards and `openspec/changes/` path.

Drive status with CLI (do not edit openspec-state.json): `openspec start <cardId>`, `openspec done <cardId>`, `openspec block <cardId> --reason "…"`, `openspec archive <itemId>`.

---

## Step 4 — Execute loop

For each pending card: `npx cc-codeconductor openspec next`, then invoke the listed agent:

- discover → `repo-explorer`
- design → `architect`
- test → `tester`
- implement → `implementer`
- review → `reviewer`

Use isolated context. Implementer uses a git worktree.

---

## Step 5 — Review gate

Reviewer approves or rejects against acceptance criteria. Reject → `IN_PROGRESS`, **STOP**.

---

## Step 6 — Update

Mark DONE with `openspec archive <itemId>` (all cards must be done), then run `openspec scan`.

Apply skill `openspec` for format and state rules.
