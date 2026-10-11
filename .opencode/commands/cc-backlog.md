---
description:
  Author BACKLOG.md — wayfinding, grilling, create or append items, validate
  with openspec, and plan OpenSpec change folders for /cc-openspec.
---

# Backlog Authoring Workflow

Objectives: $ARGUMENTS

Create or append valid `BACKLOG.md` items and OpenSpec change docs. Do **not**
deliver implementation — that is `/cc-openspec`.

---

## Step 0 — CCEP Bootstrap

Command: `backlog` (fixed for this workflow — do not infer from user text)

1. Run: `npx cc-codeconductor ccep profile backlog --output json` to get the phases and their roles.
2. For each delegated phase, run: `npx cc-codeconductor ccep compile --command backlog --phase <phase-id> "$ARGUMENTS" --view prompt --output json`
3. After planner/intake JSON is available, run: `npx cc-codeconductor ccep evaluate --command backlog --input <planner.json> --output json`. If `stop` is true, show questions or risks and wait for human input.
4. Pass each subagent only the compiled `prompt` for its phase — never forward raw `$ARGUMENTS` to planners.

---

## Step 1 — Wayfinding (repo-explorer)

If `graphify-out/graph.json` exists, run `graphify query "$ARGUMENTS"`. Invoke
`repo-explorer`. Do not write `BACKLOG.md`. Scope must name real files.

---

## Step 2 — Relentless Grilling (task-coach)

Invoke `task-coach`. Measurable acceptance only. One grilling question at a time.

**STOP here.** Unresolved grilling questions populate `questionsForUser`;
`ccep evaluate --command backlog` halts until a human answers.

---

## Step 3 — Write or append BACKLOG.md

Apply skill `backlog`. Create from template if missing; otherwise append under
`## Items`. Next `BC-NNN` = max existing + 1. Status `READY`. Do not rewrite
`## Global` or `## Archive`. Do not `git add` `BACKLOG.md` or `openspec/`.

---

## Step 4 — Validate (mandatory gate)

Run `npx cc-codeconductor openspec validate`. If invalid, show errors and
canonical structure, fix, re-validate. **STOP** planning until valid.

---

## Step 5 — Plan new items

For each new `BC-id`: `npx cc-codeconductor openspec plan <BC-id>`.

---

## Completion

Report IDs, validate result, change folders. Tell the user to run `/cc-openspec`.
Apply skill `backlog`.
