---
name: cc-explore
description:
  Map the repository and recommend the next CodeConductor slash command. Read-only.
---

# Explore Workflow

Explore request: $ARGUMENTS

## Step 0 — CCEP Bootstrap

Command: `explore` (fixed for this workflow — do not infer from user text)

1. Run: `npx cc-codeconductor ccep profile explore --output json` to get the phases and their roles.
2. For each delegated phase, run: `npx cc-codeconductor ccep compile --command explore --phase <phase-id> "$ARGUMENTS" --view prompt --output json`
3. After planner/intake JSON is available, run: `npx cc-codeconductor ccep evaluate --command explore --input <planner.json> --output json`. If `stop` is true, show questions or risks and wait for human input.
4. Pass each subagent only the compiled `prompt` for its phase — never forward raw `$ARGUMENTS` to planners.

---

## Step 1 — Map (repo-explorer)

If `graphify-out/graph.json` exists, run `graphify query "$ARGUMENTS"`. Invoke `repo-explorer`. Produce a Repo Map. Do not write code.

---

## Step 2 — Suggest next command (orchestrator)

Recommend exactly one next slash command (`/cc-feature`, `/cc-fix`, `/cc-refactor`, `/cc-review`, `/cc-backlog`, `/cc-openspec`, `/cc-triage`, …) with a one-sentence rationale. Do not start that workflow.

---

## Completion

Report the Repo Map and the recommended next `/cc-` command.
