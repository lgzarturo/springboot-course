---
description: >-
  Re-explain the last deliverable in Task Card vocabulary when it did not land.
---

# Clarify Workflow

Clarify request: $ARGUMENTS

## Step 0 — CCEP Bootstrap

Command: `clarify` (fixed for this workflow — do not infer from user text)

1. Run: `npx cc-codeconductor ccep profile clarify --output json` to get the phases and their roles.
2. For each delegated phase, run: `npx cc-codeconductor ccep compile --command clarify --phase <phase-id> "$ARGUMENTS" --view prompt --output json`
3. After planner/intake JSON is available, run: `npx cc-codeconductor ccep evaluate --command clarify --input <planner.json> --output json`. If `stop` is true, show questions or risks and wait for human input.
4. Pass each subagent only the compiled `prompt` for its phase — never forward raw `$ARGUMENTS` to planners.

---

## Step 1 — Re-explain (task-coach)

Invoke `task-coach` / `orchestrator`. Restate the last Task Card, plan, or review in the project's vocabulary. Do not add new scope. If questions remain, one question per branch.

**STOP if ConfirmationGate reports questions.**

---

## Completion

A plain-language restatement plus any remaining questions. Then resume the previous workflow.
