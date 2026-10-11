---
name: cc-iterative
description: Run the advanced iterative workflow — Wayfinding AST discovery, Relentless Grilling, contract and spec design, TDD, council review, and docs.
---

# iterative

Invoke as `$cc-iterative`. The user request follows the skill mention.

# Advanced Iterative Workflow

Iterative request: $ARGUMENTS

---

## Step 0 — CCEP Bootstrap

Command: `iterative` (fixed for this workflow — do not infer from user text)

1. Run: `npx cc-codeconductor ccep profile iterative --output json` to get the phases and their roles.
2. For each delegated phase, run: `npx cc-codeconductor ccep compile --command iterative --phase <phase-id> "$ARGUMENTS" --view prompt --output json`
3. After planner/intake JSON is available, run: `npx cc-codeconductor ccep evaluate --command iterative --input <planner.json> --output json`. If `stop` is true, show questions or risks and wait for human input.
4. Pass each subagent only the compiled `prompt` for its phase — never forward raw `$ARGUMENTS` to planners.
   Canonical delivery order is test-before-implement whenever both phases apply.

---

## Step 0b — OpenSpec quality gates

If `openspec status` reports an active change folder:

1. Run: `npx cc-codeconductor openspec validate --output json`
2. Run: `npx cc-codeconductor openspec analyze --output json`
3. If analyze `stop` is true or any finding is CRITICAL, stop. Do not delegate to implementer.
4. Next command spelling on this runner: `$cc-iterative`

Run the CLI as `npx cc-codeconductor <same argv>`.

---


## Step 1 — Wayfinding (repo-explorer)

If `graphify-out/graph.json` exists, run `graphify query "$ARGUMENTS"` (and
`graphify path` / `graphify explain` when two modules or one concept need a
scoped subgraph). Then adopt the `repo-explorer` role as defined in `AGENTS.md`
with that graph output.

repo-explorer maps directory trees, conventions, god nodes, and affected
modules. Do not write code in this step. Write a short phase handoff (scope,
files, open questions) before the next phase — do not chain agents on unspoken
context.

---

## Step 2 — Relentless Grilling & Task Card (task-coach)

Adopt the `task-coach` role as defined in `AGENTS.md`.

Produce a complete Task Card. The Task Card is ready when it contains: title,
type, risk classification, scope, context, acceptance criteria, and
constraints, and every assumption behind it has survived one grilling
question (Grilling protocol).

If any field is missing or ambiguous, ask one clarifying question at a time and
wait for the answer. Do not proceed with an incomplete Task Card.

**STOP here.** Unresolved grilling questions or missing fields populate
`questionsForUser` in the CCEP-1 `planner-output`; the `ConfirmationGate`
(`ccep evaluate`) reads that field and halts the workflow until a human
answers. Show the completed Task Card and wait for that confirmation before
continuing.

---

## Step 3 — Contract & Technical Plan (contract-builder & architect)

Adopt the `contract-builder` role as defined in `AGENTS.md`, then the `architect` role as defined in `AGENTS.md`.

1. Define API contracts, JSON Schemas, or TypeScript interfaces.
2. Formulate the Technical Plan (chosen approach, affected files, trade-offs).
3. Apply YAGNI, Simplicity First, and Stdlib-First.

**STOP here. Show the Technical Plan and wait for explicit human approval. Do
not proceed to tests until the plan is approved.**

---

## Step 4 — Test coverage (Tester role)

Adopt the `tester` role as defined in `AGENTS.md`.

1. Write or extend failing tests for the new behavior before implementation (RED).
2. Ensure all acceptance criteria from the Task Card have at least one test.
3. Produce a Test Report: test files added or modified, cases covered.

---

## Step 5 — Implementation (Implementer role)

Adopt the `implementer` role as defined in `AGENTS.md`.

Use the approved Technical Plan, contracts, and the failing tests.
Implementer creates a Git Worktree before touching any file; all edits happen
inside it.

1. Read the Technical Plan before touching any file.
2. Apply the minimal diff — only what the plan specifies.
3. Run the project test suite and make the previously written failing tests pass.
4. If tests fail, run up to 3 repair cycles (`implementer` → `tester`).
5. Produce an Implementation Summary: what changed, which files, how to verify
   locally.

---

## Step 6 — Multi-Perspective Council Review

Invoke the `council` skill on the generated diff (`council-review` phase).

The council evaluates Architecture, Security, Product, Delivery, DataOps, and
Devil. `security-reviewer` may set `securityVeto: true`; that REJECTED verdict
overrides majority consensus.

If ANY agent votes CRITICAL:
- The Review Report status is **BLOCKED**.
- Return to Step 5 with the feedback.

If APPROVED (no CRITICAL findings), continue.

---

## Step 7 — Documentation (Docs role)

Adopt the `docs` role as defined in `AGENTS.md` when a public API, public module,
or user-visible behavior changed.

Update: README (if applicable), OpenAPI spec (if applicable), CHANGELOG
(always when implementation changed), ADR (if an architectural decision was
made).

If `graphify-out/` is in use, run `graphify update .` (AST-only).

---

## Completion

The iterative workflow is complete only when: all tests pass, the council
approves the spec and the diff, no CRITICAL findings remain, and documentation
and the knowledge graph reflect the implemented behavior.
