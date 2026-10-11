---
name: backlog
description: >
  Guides agents through authoring BACKLOG.md and OpenSpec change folders.
  Use when running /cc-backlog or /cc:backlog, creating or appending backlog
  items, or preparing work for /cc-openspec. Delivery is skill openspec.
---

# Backlog authoring

## Overview

Create or append `BACKLOG.md`, then `openspec validate` / `plan`. Do not deliver
the item here.

## When to Use

- `/cc-backlog`, first backlog in a repo, or appending `### BC-xxx` items

**NOT** for executing an item (`openspec`) or for scorecards (`evaluation`).

## Process

1. If `graphify-out/graph.json` exists, `graphify query "<objectives>"`. Then
   `repo-explorer`. Scope names real files.
2. Invoke `task-coach`. One grilling question per assumption. Reject vague
   acceptance ("improve UX"). At most 3 `[NEEDS CLARIFICATION]`.
3. `ccep evaluate --command backlog`. If `stop`, wait for the human.
4. Create `BACKLOG.md` from `presets/templates/BACKLOG.md` or append under
   `## Items`. Do not rewrite `## Global` or `## Archive`.
5. Next ID = max numeric suffix in Items + Archive + 1, zero-padded (`BC-013`).
6. `npx cc-codeconductor openspec validate`. Fix until valid.
7. `openspec plan BC-xxx` for each **new** item this run. Then tell the user
   to run `/cc-openspec`.

Required sections: `## Global`, `## Items`, `## Archive`. Each item:
`### BC-001 | Title` with Priority, Status (`READY` after grilling), Type,
Depends on, Description, Scope, Out of scope, Acceptance.

Local artifacts (`BACKLOG.md`, `openspec/`, `.codeconductor/openspec-state.json`)
are gitignored in consumer projects. Do not `git add` them.

## Common Rationalizations

| Rationalization | Reality |
| --- | --- |
| This fix is small; skip the Task Card | Every item needs measurable acceptance. |
| I'll validate later | Do not plan until `openspec validate` passes. |
| Archive can be rewritten | Archive is history. Never rewrite or re-execute. |

## Red Flags

- Acceptance that cannot fail a check
- Editing `openspec-state.json` by hand
- Planning an invalid backlog

## Verification

- [ ] `openspec validate` exit 0
- [ ] New items have `FR`/`SC`-ready measurable acceptance
- [ ] User pointed at `/cc-openspec` for delivery
