---
description: >-
  Author BACKLOG.md — wayfinding, grilling, create or append items,
  validate with openspec, and plan OpenSpec change folders for /cc:openspec.
---

# Backlog Authoring Workflow

Objectives: $ARGUMENTS

Create or append valid `BACKLOG.md` items and OpenSpec change docs. Do **not**
deliver implementation — that is `/cc:openspec`.

---

## Step 0 — CCEP Bootstrap

Command: `backlog` (fixed for this workflow — do not infer from user text)

1. Run: `npx cc-codeconductor ccep profile backlog --output json` to get the phases and their roles.
2. For each delegated phase, run: `npx cc-codeconductor ccep compile --command backlog --phase <phase-id> "$ARGUMENTS" --view prompt --output json`
3. After planner/intake JSON is available, run: `npx cc-codeconductor ccep evaluate --command backlog --input <planner.json> --output json`. If `stop` is true, show questions or risks and wait for human input.
4. Pass each subagent only the compiled `prompt` for its phase — never forward raw `$ARGUMENTS` to planners.

---

## Step 1 — Wayfinding (Repo Explorer role)

If `graphify-out/graph.json` exists, run `graphify query "$ARGUMENTS"` (and
`graphify path` / `graphify explain` when needed). Then adopt the **Repo
Explorer** role. Do not write `BACKLOG.md` in this step. Record a Repo Map so
Scope names real files or modules.

---

## Step 2 — Relentless Grilling (Task Coach role)

Adopt the **Task Coach** role. Produce complete backlog item fields: title,
type, risk, scope, description, out of scope, measurable acceptance criteria.

Every assumption must survive one grilling question. Reject vague acceptance
("improve UX", "fix bugs").

If any field is missing or ambiguous, ask one clarifying question at a time.

**STOP here.** Unresolved grilling questions populate `questionsForUser` in the
CCEP-1 `planner-output`; `ccep evaluate --command backlog` halts until a human
answers. Do not write `BACKLOG.md` until that confirmation.

---

## Step 3 — Write or append BACKLOG.md (Docs role)

Apply skill `backlog`.

- If `BACKLOG.md` is absent, create it from the canonical template. Set Global
  `Product` from `package.json` `name` when present.
- If it exists, append new `### BC-xxx` items under `## Items`. Do not rewrite
  `## Global` or `## Archive`.
- Next ID = max `BC-NNN` across Items and Archive, plus one.
- New items: Status `READY`, Progress `0%`, measurable Acceptance checklist.

Do **not** `git add` `BACKLOG.md` or `openspec/`.

---

## Step 4 — Validate (mandatory gate)

Run:

```bash
npx cc-codeconductor openspec validate
```

If validation fails:

1. List all errors and recommendations from the CLI output.
2. Show the canonical BACKLOG.md structure (## Global, ## Items, ### BC-001 | Title, Priority, Status, Type, Depends on, Description, Scope, Acceptance).
3. Fix the file and re-run validate. **STOP** planning until valid.

---

## Step 5 — Plan new items

For each **new** `BC-xxx` from this run:

```bash
npx cc-codeconductor openspec plan <BC-id>
```

Show TaskCards and `openspec/changes/<slug>/` (proposal, design, tasks, specs).

---

## Completion

Report: item IDs added, validate result, change folder paths.

Tell the user to run `/cc:openspec` (optionally `/cc:openspec BC-xxx`) to
deliver. Do not start delivery in this workflow.

Apply skill `backlog` for create/append rules. Apply skill `openspec` only for
format and state-machine reference.
