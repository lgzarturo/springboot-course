---
name: cc-prototype
description:
  Disposable spike in an isolated worktree. Not a merge candidate.
---

# Prototype Workflow

Prototype request: $ARGUMENTS

## Step 0 — CCEP Bootstrap

Command: `prototype` (fixed for this workflow — do not infer from user text)

1. Run: `npx cc-codeconductor ccep profile prototype --output json` to get the phases and their roles.
2. For each delegated phase, run: `npx cc-codeconductor ccep compile --command prototype --phase <phase-id> "$ARGUMENTS" --view prompt --output json`
3. After planner/intake JSON is available, run: `npx cc-codeconductor ccep evaluate --command prototype --input <planner.json> --output json`. If `stop` is true, show questions or risks and wait for human input.
4. Pass each subagent only the compiled `prompt` for its phase — never forward raw `$ARGUMENTS` to planners.

---

## Step 1 — Bounds (architect)

Invoke `architect`. Define what the spike may touch, time-box, and what must not leak into main. Do not treat this as a production Technical Plan.

**STOP here. Wait for approval of the spike bounds.**

---

## Step 2 — Spike (implementer)

Invoke `implementer` in a Git worktree that is not a merge candidate. Label the Implementation Summary as disposable. Do not open a PR against protected branches.

---

## Completion

Report bounds, worktree path, and what was learned. Recommend `/cc-feature` or `/cc-fix` if the spike should become real work.
