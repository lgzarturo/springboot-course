---
name: cc-spec-mutation
description: >-
  Spec-locked TDD with a mutation-testing gate — refine the intent
  into an immutable Gherkin contract (SHA-256 frozen), implement under the
  three laws of TDD, pass a judge audit, and merge only if every mutant dies.
---

# Spec-Mutation — Hard Spec → TDD → Judge → Mutation Gate

Scope: $ARGUMENTS

Describe what behavior you want to implement. Include:

- The function, method, or feature to implement
- The expected behavior (inputs, outputs, invariants, edge cases)
- The allowed file scope (production files that may change)
- The test command for the affected suite (e.g. `pytest tests/test_billing.py`)

---

## Step 0 — CCEP Bootstrap

Command: `spec-mutation` (fixed for this workflow — do not infer from user text)

1. Run: `npx cc-codeconductor ccep profile spec-mutation --output json` to get the phases and their roles.
2. For each delegated phase, run: `npx cc-codeconductor ccep compile --command spec-mutation --phase <phase-id> "$ARGUMENTS" --view prompt --output json`
3. After planner/intake JSON is available, run: `npx cc-codeconductor ccep evaluate --command spec-mutation --input <planner.json> --output json`. If `stop` is true, show questions or risks and wait for human input.
4. Pass each subagent only the compiled `prompt` for its phase — never forward raw `$ARGUMENTS` to planners.
   Canonical delivery order is test-before-implement whenever both phases apply.

---

## Step 0b — OpenSpec quality gates

If `openspec status` reports an active change folder:

1. Run: `npx cc-codeconductor openspec validate --output json`
2. Run: `npx cc-codeconductor openspec analyze --output json`
3. If analyze `stop` is true or any finding is CRITICAL, stop. Do not delegate to implementer.
4. Next command spelling on this runner: `/cc-spec-mutation`

Run the CLI as `npx cc-codeconductor <same argv>`.

---

## Contract

The Gherkin specification is the **immutable contract** of the system. No code
merges unless it survives intentional source mutations. The loop is closed:

```
[Human + spec_partner] ──> [gherkin_author] ──> [Test Freeze: SHA-256]
                                                       │
┌──────────────────────────────────────────────────────┘
▼
[tdd_craftsman] <───────────────┐ (surviving mutant)
   (Red-Green-Refactor)         │
        │                       │
        ▼                       │
     [judge] ───────────> [mutation_testing] ───> [Safe Merge]
```

Role mapping onto Conductor Agents (AGENTS.md):

| Workflow role      | Conductor Agent   | Deliverable                                |
| ------------------ | ----------------- | ------------------------------------------ |
| `craftsman_lead`   | `orchestrator`    | Routed Task Cards, per-stage scorecards    |
| `spec_partner`     | `task-coach`      | Spec draft with invariants and boundaries  |
| `gherkin_author`   | `contract-builder`| Strict `.feature` file (Given/When/Then)   |
| `tdd_craftsman`    | `tester` → `implementer` | Failing test, then minimal production code |
| `judge`            | `reviewer`        | Binary verdict (PASS/REJECT) with scoring  |
| `mutation_testing` | `tester` (runner) | Killed/survived mutant report              |

## Stage 1 — Interactive refinement (`spec_partner` / task-coach)

Do not jump to implementation. Apply Socratic questioning to the initial intent:

- Preconditions, postconditions, and edge cases.
- Invariant matrix: what must always be true.
- Scope boundaries: explicit `Scope / Files` and `Scope / Out` for the Task Card.

Stop gate: human confirms the draft before formalization.

## Stage 2 — Hard Spec formalization (`gherkin_author` / contract-builder)

Formalize the agreed draft into `specs/<task>.feature` using strict Gherkin:

- No vague language ("must respond fast" is forbidden — use measurable Then steps).
- Every scenario declares preconditions (`Given`), actions (`When`), and
  observable states (`Then`).
- Once the human approves the `.feature`, freeze it:

```bash
shasum -a 256 specs/<task>.feature tests/ > .codeconductor/tasks/<task_id>.lock
```

From this point `specs/` and `tests/` are **read-only** for implementation
agents. Before every later gate, recompute the hash; any single-byte difference
aborts the pipeline with scorecard 0 (Specification Gaming).

## Stage 3 — TDD under the three laws (`tdd_craftsman`)

Delegates to the `/cc-tdd-cycle` state machine (`tddCycleStateMachine` in
`domain/loop`). Evidence must be captured with `npx cc-codeconductor tdd capture` — do
not hand-edit JSON under `.codeconductor/evidence/`.

1. **Law 1 (RED):** no production code except to make a failing test pass. A
   compile error from a missing interface counts as a failure.
2. **Law 2:** write exactly one failing assertion or scenario at a time.
3. **Law 3 (GREEN):** write only the minimal production code to pass. No
   speculative code, no preventive heuristics, stdlib-first.

Hard rule: any write attempt against `specs/` or `tests/` during GREEN/REFACTOR
is a harness violation — stop execution and report.

## Stage 4 — Judge audit (`judge` / reviewer)

Deterministic gates before spending compute on mutation:

- Clean compile / diagnostics exit code 0.
- Traceability: every Gherkin step maps to an implemented test step.
- Scope Gaming audit: `git diff --name-only` must match the Task Card
  `Scope / Files` exactly. Relaxed types, weakened assertions, or out-of-scope
  edits → REJECT with findings.

Verdict is binary: PASS continues to the mutation gate; REJECT returns to the
`implement` phase with the findings attached.

## Stage 5 — Mutation gate (`mutation_testing`)

Run the deterministic AST mutator shipped with this preset:

```bash
python3 presets/shared/mutation_runner.py \
  --target <production_file.py> \
  --test-command "<test command>" \
  --spec-folder specs
```

The runner applies deterministic operator mutations (`>` → `<=`, `==` → `!=`,
`is` → `is not`, …) one at a time, re-runs the suite per mutant, and restores
the original source unconditionally (`finally` rollback).

- **Mutant killed (tests fail):** the suite detects the corruption. Continue.
- **Mutant survived (tests pass):** the tests are blind to this branch. The
  runner writes `specs/handover.md` + appends to
  `specs/implementation-summary.md` and exits with code **2**.

Non-Python stacks: substitute Stryker (JS/TS), PITest (JVM), or Mutmut
(Python full-suite) with the same contract — 100% kill rate or hands-off.

### Hands-off protocol (exit code 2)

1. Do NOT modify production code to "fix" a surviving mutant.
2. Route back to `tdd_craftsman` with `specs/handover.md` as input: write the
   missing failing test (Law 1 & 2) that asserts the mutated branch.
3. Re-run stages 3–5.

### Circuit breaker (max 3 loops)

The orchestrator keeps a persistent counter per Task Card. If the
`tdd_craftsman ↔ mutation_testing` loop does not reach a 100% kill rate after
**3 iterations**:

- Cancel active subagents (stop token/context spend).
- `git checkout -- <scope>` rollback to the last clean state.
- Scorecard: `STATUS = BLOCKED`; escalate to a human operator. The branch stays
  frozen until human arbitration.

## Guardrails (harness-enforced, not prompt-enforced)

- **Test Freezing:** SHA-256 of `specs/` + `tests/` stored in
  `.codeconductor/tasks/<task_id>.lock`; hash mismatch aborts the pipeline.
- **Scope Guardian:** `fs_write`/`fs_patch` paths are validated against the Task
  Card `Scope / Files`; path escapes (`../`) and critical files (`**/.env*`,
  `**/credentials*`, infrastructure roots) are denied per `policy.yml`.
- **RBAC per role:** `gherkin_author` writes only `specs/`; `tdd_craftsman`
  reads specs/tests and writes only scoped `src/`; `judge` and
  `mutation_testing` are read-only except the runner's rolled-back patch.
- **Worktree isolation:** run the whole flow in a dedicated `git worktree`;
  protected branches (`main`, `master`, `develop`) are never touched.

## Completion criteria

- [ ] `.feature` approved and frozen (SHA-256 lock file exists and matches).
- [ ] RED → GREEN → REFACTOR evidence captured per phase.
- [ ] Judge verdict PASS (compile clean, traceability complete, scope clean).
- [ ] Mutation runner exits 0 with `total_mutants_killed == total_points`.
- [ ] Scorecard records the kill rate and iteration count (≤ 3).

## Next

Run `/cc-review` on the diff before merging.
