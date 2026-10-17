---
name: cc-scorecard
description:
  Evaluate deliverable quality — scorecard, outcome tracking, regression checklist.
---

# Scorecard Evaluation Workflow

Scope: $ARGUMENTS

## Step 0 — CCEP Bootstrap

Command: `scorecard` (fixed for this workflow — do not infer from user text)

1. Run: `npx cc-codeconductor ccep profile scorecard --output json` to get the phases and their roles.
2. For each delegated phase, run: `npx cc-codeconductor ccep compile --command scorecard --phase <phase-id> "$ARGUMENTS" --view prompt --output json`
3. After planner/intake JSON is available, run: `npx cc-codeconductor ccep evaluate --command scorecard --input <planner.json> --output json`. If `stop` is true, show questions or risks and wait for human input.
4. Pass each subagent only the compiled `prompt` for its phase — never forward raw `$ARGUMENTS` to planners.

---

If a change folder exists, run `npx cc-codeconductor openspec analyze --output json` first. `--from-diff` overlays FR/SC coverage onto `acceptance` and TDD evidence onto `tests`.

1. `npx cc-codeconductor scorecard create --task <id> --from-diff`
2. Complete criteria per docs/agent-scorecard.md
3. `scorecard regression` if needed
4. `scorecard record` with verdict and optional cost/tokens
5. `scorecard aggregate`

Apply skill `evaluation`.
