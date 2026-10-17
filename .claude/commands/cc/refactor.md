---
description: >-
  Run the refactor workflow — mandatory architectural justification,
  test verification, risk-based implementation, and scope enforcement.
---

# Refactor Workflow

Refactor description: $ARGUMENTS

Describe what you want to refactor and why. Include:

- The current structure or pattern being changed
- The target structure or pattern
- The motivation (performance, readability, architectural alignment, etc.)
- Known risk areas or dependencies

---

## Prerequisite — Test coverage check

Before any role is adopted, verify that the code being refactored has adequate
test coverage.

A refactor without tests is not a refactor — it is a rewrite with unknown
behavioral consequences.

If coverage is insufficient:

1. **STOP**. Report the coverage gap.
2. Suggest running `/test-plan` first to establish coverage.
3. Do not proceed with the refactor until coverage is confirmed.

---

## Step 0 — CCEP Bootstrap

Command: `refactor` (fixed for this workflow — do not infer from user text)

1. Run: `npx cc-codeconductor ccep profile refactor --output json` to get the phases and their roles.
2. For each delegated phase, run: `npx cc-codeconductor ccep compile --command refactor --phase <phase-id> "$ARGUMENTS" --view prompt --output json`
3. After planner/intake JSON is available, run: `npx cc-codeconductor ccep evaluate --command refactor --input <planner.json> --output json`. If `stop` is true, show questions or risks and wait for human input.
4. Pass each subagent only the compiled `prompt` for its phase — never forward raw `$ARGUMENTS` to planners.

---

## Step 1 — Architectural justification (Architect role)

Adopt the **Architect** role as defined in `CLAUDE.md`. Always invoke this step
first, regardless of risk level. A refactor without a written justification is
scope creep in disguise.

Produce a Refactor Plan that includes:

- Statement of the problem with the current structure
- Proposed target structure and rationale
- Affected files and module boundaries
- Risk level: `low`, `medium`, or `high`
- Behavioral invariants that must not change
- Open questions requiring human input

**Scope creep warning:** If during planning you identify unrelated improvements,
list them separately as "Out of scope." They are not part of this refactor.

**STOP here. Show the Refactor Plan and wait for explicit human approval. Do not
proceed without written approval of the plan.**

---

## Step 1.5 — Blast Radius Assessment (Orchestrator role)

After Architect approval, assess the scope of the refactor using impact analysis.
Large refactors require staged sequencing to keep the tree green between batches.

### When to apply Blast Radius Assessment

Always run this step. It determines the route for implementation:

- **Narrow refactor** (blast radius within limits) → proceed to Step 2 (normal routing by risk)
- **Wide refactor** (blast radius exceeds limits) → proceed to expand-migrate-contract sequence

### Measuring blast radius

Run impact analysis on the files listed in the approved Refactor Plan:

```bash
cc impact --files <file1> <file2> ... <fileN>
```

The report returns three metrics:

1. **affectedComponents** (array of strings) — distinct components/modules that depend on the refactored files
2. **brokenContracts** (array of strings) — public interfaces or contracts that change
3. **affectedFlows** (array of strings) — end-to-end user flows or business processes impacted

### Threshold decision

**Hardcoded heuristic thresholds** (adjust manually for your codebase size):

```
Wide refactor triggered if ANY condition is true:
  - affectedComponents.length >= 6
  - brokenContracts.length >= 3
  - affectedFlows.length >= 2
```

If blast radius exceeds these thresholds, the refactor is classified as **wide**
and requires staged batching. Otherwise, it is **narrow** and proceeds via
normal risk-based routing.

**Note:** These thresholds are heuristics meant for medium-sized codebases
(5–20K lines). Adjust based on your repository's complexity and component
density. A high-level import count alone does not guarantee impact.

### Route: Narrow refactor

If blast radius is within limits:

1. Show the impact report: affectedComponents count, brokenContracts, affectedFlows.
2. Proceed to Step 2 (Route by risk) using the risk level from the approved Refactor Plan.

### Route: Wide refactor — expand-migrate-contract sequence

If blast radius exceeds limits, the refactor must be delivered in sequenced
batches to maintain tree-green guarantees between phases.

**Process:**

1. **Expand phase** (batch 1): Introduce the new interface alongside the old one
   without removing the old interface. This allows consumers to adopt
   incrementally. Tests for both old and new paths must pass in this batch.
   
   Example: If refactoring a component from function-based to class-based:
   ```typescript
   // OLD: function-based API (keep during expand)
   export function transformData(input: Data): Output {
     return Transform.apply(input);
   }
   
   // NEW: class-based API (add during expand)
   export class DataTransformer {
     apply(input: Data): Output {
       return Transform.apply(input);
     }
   }
   
   // Both exported; both tested; old not yet removed.
   ```

2. **Migrate batches** (batches 2–N): Each batch moves a logical subset of
   consumers from the old interface to the new one, running tests to confirm
   no regressions. If tests fail in any migrate batch, that batch is BLOCKED
   (via `Depends on` in BACKLOG.md) and subsequent batches do not start until
   the regression is fixed.
   
   Example: Migrate Service-A consumers in batch 2, Service-B consumers in
   batch 3, etc. Each batch is a separate `BC-NNN` item in BACKLOG.md.

3. **Contract phase** (final batch): Remove the old interface entirely after all
   consumers are migrated. This is the point where the contract actually changes.

**Sequencing batches via BACKLOG.md:**

Create multiple `BC-NNN` items, one per batch (expand, then one per migrate, then
contract). Use the `Depends on` field to enforce ordering:

```
### BC-001 | Expand Phase
Status: READY
Depends on: (none)
Description: Introduce new interface alongside old...

### BC-002 | Migrate Batch 1 (Service-A consumers)
Status: READY (but not eligible until BC-001 done)
Depends on: BC-001
Description: Move Service-A from old to new interface...

### BC-003 | Migrate Batch 2 (Service-B consumers)
Status: READY (but not eligible until BC-002 done)
Depends on: BC-002
Description: Move Service-B from old to new interface...

### BC-004 | Contract Phase (Remove old interface)
Status: READY (but not eligible until BC-003 done)
Depends on: BC-003
Description: Delete old interface and consolidate tests...
```

**BLOCKED policy on test failures:**

If any migrate batch's test suite fails after implementation, that batch item
is marked BLOCKED (and stays BLOCKED in BACKLOG.md until the regression is
fixed). Subsequent batches in the `Depends on` chain do not start — the
orchestrator skips them and reports the blocker to the user.

This ensures the tree remains green and no downstream consumer migration builds
on unstable code.

**STOP here. Show the Blast Radius Assessment report and the recommended route
(narrow or wide). If wide, show the proposed batch structure and wait for
user approval before proceeding.**

---

## Step 2 — Route by risk

Read the risk field from the Refactor Plan and follow the corresponding route.

### Low-risk route

Applies when: the refactor is purely internal, no public interfaces change, full
test coverage exists for the affected code, and behavioral impact is isolated to
the refactored module.

Route: Architect (done) → Implementer

Proceed to Step 3a.

### Medium or high-risk route

Applies when: module boundaries change, shared interfaces are affected,
performance characteristics may change, or the refactor touches more than two
files with behavioral impact.

Route: Architect (done) → Implementer → Reviewer

Proceed to Step 3b.

---

## Step 3a — Implementation, low-risk (Implementer role)

Adopt the **Implementer** role as defined in `CLAUDE.md`. Use the approved
Refactor Plan.
Implementer creates a Git Worktree before touching any file; all edits happen inside it.

1. Read the Refactor Plan before opening any file.
2. Apply only the changes specified in the plan.
3. Run the full test suite before and after — both runs must pass.
4. Produce an Implementation Summary: what changed, what did not change, test
   results before and after.

Any deviation from the plan — including "obvious improvements" encountered
during implementation — must be flagged and held for a separate task.

---

## Step 3b — Implementation, medium/high-risk (Implementer role)

Same rules as Step 3a. Additionally:

- Document any unexpected complexity discovered during implementation.
- Pause and report if complexity changes the risk assessment.
- If new risks are found, **STOP** and report before continuing.

---

## Step 4 — Test suite verification

For all risk levels, confirm:

- All tests that existed before the refactor still pass.
- No test was deleted or commented out to make the suite pass.
- Behavior documented in the Task Card remains unchanged.

If any test fails that was passing before, the refactor has introduced a
regression. **STOP and report.**

---

## Step 5 — Code review (Reviewer role) — medium/high-risk only

Adopt the **Reviewer** role as defined in `CLAUDE.md`. Use the diff and Refactor
Plan.

Verify:

- The implementation matches the approved plan.
- No behavior was changed beyond the plan's scope.
- No unrelated files were modified.

Review Report must include CRITICAL / WARNING / SUGGESTION findings. CRITICAL
findings block completion.

---

## Completion

Report: Refactor Plan (approved), Blast Radius Assessment (if applicable), Implementation Summary, test results before
and after, Review Report (if applicable).

The refactor is complete only when: all pre-existing tests still pass, the
implementation matches the approved plan exactly, and no CRITICAL review
findings remain.

If using expand-migrate-contract: completion occurs only after all batches
(expand, migrate, contract) are DONE and no BLOCKED items remain in the
sequencing chain.

## Next

Run `/cc:review` on the diff before merging.
