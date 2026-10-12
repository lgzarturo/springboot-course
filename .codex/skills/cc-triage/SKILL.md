---
name: cc-triage
description: Classify a request into type, risk, and the destination CodeConductor command.
---

# triage

Invoke as `$cc-triage`. The user request follows the skill mention.

# Triage Workflow

Triage request: $ARGUMENTS

## Step 0 — CCEP Bootstrap

Command: `triage` (fixed for this workflow — do not infer from user text)

1. Run: `npx cc-codeconductor ccep profile triage --output json` to get the phases and their roles.
2. For each delegated phase, run: `npx cc-codeconductor ccep compile --command triage --phase <phase-id> "$ARGUMENTS" --view prompt --output json`
3. After planner/intake JSON is available, run: `npx cc-codeconductor ccep evaluate --command triage --input <planner.json> --output json`. If `stop` is true, show questions or risks and wait for human input.
4. Pass each subagent only the compiled `prompt` for its phase — never forward raw `$ARGUMENTS` to planners.

---

## Step 1 — Classify (task-coach)

Invoke `task-coach`. Produce: title, type (`feature` | `fix` | `refactor` | `review` | `docs` | `test`), risk, named scope, and the destination command (`feature`, `fix`, `refactor`, `review`, `explore`, …).

Ask one question per unresolved branch. If the human is unavailable, emit a Markdown questionnaire and stop at ConfirmationGate.

**STOP here. Show the classification and wait for confirmation before running the destination workflow.**

---

## Completion

Do not implement. Hand the human a destination slash command and a partial Task Card.
