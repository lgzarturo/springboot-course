---
description: >-
  Run OpenSpec backlog delivery — validate BACKLOG.md, plan TaskCards,
  orchestrate agents by phase, review gate, and update backlog state.
---

# OpenSpec Backlog Workflow

Scope: $ARGUMENTS

Orchestrate FIFO delivery from `BACKLOG.md`. CodeConductor owns planning; agents execute one TaskCard per phase with the installed preset model for each role.

## Model and context budget

Use Claude Sonnet 5.5 for documentation, validation, design, tests,
implementation, and review. Use the lighter role model for simple discovery
and routing. Keep each phase on its assigned role and pass only
the current TaskCard, relevant file paths, acceptance criteria, and a short
summary of the preceding phase. Avoid replaying the full transcript or reading
the same large files in every phase.

If design or implementation is blocked by a specific business rule that Sonnet
cannot resolve from the available evidence, invoke the `architect` subagent once
with `model: opus`. Give it the exact question, relevant constraints,
and affected files. Ask for a brief decision and rationale, then continue the
remaining work on Sonnet 5.5. Do not switch the main session to Opus or use it
for routine documentation, validation, code generation, or review. This is a
routing goal, not a measured 90% usage guarantee.

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


## Planning Principles — tracer bullet and blocking edges

A **tracer bullet** is a vertical slice of work: one `BC-NNN` backlog item that goes discover → design → test → implement → review and produces a demoable result, sized to fit inside a single agent's context window without needing an intermediate `/clear`.

**Quantitative heuristic**: a tracer bullet must fit within a maximum of 5 acceptance criteria and the 5 canonical phases (discover, design, test, implement, review) per context window. If an item needs more, split it into multiple `BC-NNN` items linked by `Depends on` instead of growing one tracer bullet past that budget.

**Equivalence**: one `BC-NNN` item equals one tracer bullet equals one or more TaskCards. `cc openspec plan <BC-id>` generates the TaskCards (discover/design/test/implement/review) that implement that item's tracer bullet — each BC-NNN item corresponds to exactly one tracer bullet.

**Blocking edges**: the `Depends on` field between `BC-NNN` items expresses a blocking dependency — an item cannot be selected in Step 2 until every item in its `Depends on` list is `DONE`. `Depends on` is the sole mechanism for sequencing tracer bullets; there is no separate edge syntax.

---

## Step 0a — Validate (mandatory gate)

Run:

```bash
npx cc-codeconductor openspec validate
```

If validation fails:

1. List all errors and recommendations from the CLI output.
2. Show the canonical BACKLOG.md structure (## Global, ## Items, ### BC-001 | Title, Priority, Status, Type, Depends on, Description, Scope, Acceptance).
3. **STOP.** Do not scan, plan, or execute until the user fixes BACKLOG.md.

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
4. Next command spelling on this runner: `/cc:openspec`

Run the CLI as `npx cc-codeconductor <same argv>`.

---


## Step 1 — Scan

Run:

```bash
npx cc-codeconductor openspec scan
```

Report: new items, modified items, closed/archived items, and whether the file changed.

---

## Step 2 — Select item

If `$ARGUMENTS` contains a `BC-xxx` ID, use that item. Otherwise run:

```bash
npx cc-codeconductor openspec status
```

Pick the next `READY` item with satisfied dependencies (FIFO by priority). Skip items in `DONE` or `Archive`.

**STOP if no eligible item.** Report backlog status and wait for the user.

---

## Step 3 — Plan

Run:

```bash
npx cc-codeconductor openspec plan <BC-id>
```

Show:

- Generated TaskCards (discover → design → test → implement → review when TDD required)
- Path to `openspec/changes/<slug>/` (proposal, design, tasks, specs)

Update BACKLOG item status to `PLANNED` (CLI does this automatically).

`plan` is planning-only: it writes TaskCards plus proposal/design/tasks/specs
and stops. Before Step 4, review the plan in this order — proposal → delta
specs → tasks — and confirm: intent is right, no scope creep, every FR is
testable with a scenario that exercises it, edge/error cases are covered, and
each task traces to an FR/SC. Fix the Markdown directly or ask for revisions.
Do not `start` any card until the plan reads correctly.

---

## Drive card status (CLI)

Do not edit `.codeconductor/openspec-state.json` by hand.

```bash
npx cc-codeconductor openspec start <cardId>
npx cc-codeconductor openspec done <cardId>
npx cc-codeconductor openspec block <cardId> --reason "waiting on design"
npx cc-codeconductor openspec sync <itemId>
npx cc-codeconductor openspec verify <itemId>
npx cc-codeconductor openspec archive <itemId>
```

`start` moves the card `pending → doing` and the item `PLANNED → IN_PROGRESS`. `done` marks the card complete, updates Progress, and moves the item to `REVIEW` when every card is done and review is required. `sync` merges delta specs into `openspec/specs/` without closing the item (optional before archive). `verify` is the advisory pre-archive checklist: exit 0 with `archiveReady` plus Completeness/Correctness/Coherence issues. `archive` requires all cards done (and review evidence when Global review is required), re-checks planning artifacts and analyze CRITICALs, fails on unchecked `tasks.md` boxes (override: `--allow-unchecked`), ticks the BACKLOG acceptance criteria, then moves `openspec/changes/<slug>` to `archive/`.

---

## Step 4 — Model matrix and execute loop

Before executing phases, show resolved models:

```bash
npx cc-codeconductor scorecard models
```

For each pending TaskCard, run:

```bash
npx cc-codeconductor openspec next
```

Invoke the agent named on the card as a subagent with **isolated** context (subagents already start fresh; do not `/clear` the main session between phases):

| Phase | Agent | Role |
|-------|-------|------|
| discover | repo-explorer | Map repo and impact |
| design | architect | Technical plan |
| test | tester | TDD tests (before implement if TDD required) |
| implement | implementer | Minimal diff in worktree |
| review | reviewer | Acceptance + scope gate |

Use the model configured in the installed preset for each agent role.

After each phase:

- Mark progress in deliverable summary.
- Run `npx cc-codeconductor openspec done <cardId>` when the phase completes. Do not edit openspec-state.json by hand.

Implementer: create a Git worktree before editing (`git worktree add ../<branch>-session <branch>`).

Discover is read-only: `repo-explorer` never writes code. `openspec done` ticks the boxes tagged
with that card and keeps your edits; the implementer ticks any other `tasks.md` box
it adds (`- [ ]` → `- [x]`) — only `x`/`X` counts as done. If implementation reveals a design problem, pause and reconcile the
planning artifacts first (any direction: a later artifact may force revising an
earlier one). Planning artifacts only in that step — never code — and confirm
each edit. If the item's intent changed rather than its details, open a fresh
item with `/cc:backlog` instead of warping this one.

---

## Step 5 — Review gate

Run regression when BACKLOG Global `Review required: yes`:

```bash
npx cc-codeconductor scorecard regression
```

The `reviewer` agent must verify:

- All acceptance criteria from BACKLOG.md
- Tests pass
- Scope matches the backlog item
- No architectural violations

If **rejected**: set item status `IN_PROGRESS`, record findings in state, **STOP** and report to user.

If **approved**: proceed to Step 6.

---

## Step 6 — Scorecard and update backlog

First run the advisory pre-archive check (`npx cc-codeconductor openspec verify <BC-id> --output json`): confirm `archiveReady` and clear any Completeness/Correctness/Coherence issues. Optionally run `openspec sync <BC-id>` to merge delta specs into `openspec/specs/` without closing the item. Then:

1. `npx cc-codeconductor scorecard create --task <BC-id> --from-diff`
2. Complete criteria; `scorecard record` with verdict and optional cost/tokens
3. Set item `Progress: 100%`, `Status: DONE` if PASS
4. Move item to `## Archive` in BACKLOG.md if DONE
5. Run `npx cc-codeconductor openspec scan`

Report completion: Task Cards executed, scorecard verdict, change folder path, files changed.

---

## Skill

Apply `.claude/skills/openspec/SKILL.md` for backlog format rules and state transitions.
