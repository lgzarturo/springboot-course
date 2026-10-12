---
name: cc-refactor
description:
  Run the refactor workflow — mandatory architectural justification, test
  verification, risk-based implementation, and scope enforcement.
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

Before any agent is invoked, verify that the code being refactored has adequate
test coverage.

A refactor without tests is not a refactor — it is a rewrite with unknown
behavioral consequences.

If coverage is insufficient:

1. **STOP**. Report the coverage gap to the human.
2. Suggest invoking `/cc:test-plan` first to establish coverage.
3. Do not proceed with the refactor until coverage is confirmed.

---

## Step 1 — Architectural justification (architect)

Always invoke `architect` first, regardless of risk level. A refactor without a
written justification is scope creep in disguise.

architect must produce a Refactor Plan that includes:

- Statement of the problem with the current structure
- Proposed target structure and rationale
- Affected files and module boundaries
- Risk level: `low`, `medium`, or `high`
- Behavioral invariants that must not change
- Open questions requiring human input

**Scope creep warning:** If during planning architect identifies unrelated
improvements, they must be listed separately as "Out of scope." They are not
part of this refactor.

**STOP here. Show the Refactor Plan and wait for explicit human approval. Do not
proceed without written approval of the plan.**

---

## Step 2 — Route by risk

Read the risk field from the Refactor Plan and follow the corresponding route.

### Low-risk route

Applies when: the refactor is purely internal, no public interfaces change, full
test coverage exists for the affected code, and behavioral impact is isolated to
the refactored module.

Route: `architect` (done) → `implementer`

Proceed to Step 3a.

### Medium or high-risk route

Applies when: module boundaries change, shared interfaces are affected,
performance characteristics may change, or the refactor touches more than two
files with behavioral impact.

Route: `architect` (done) → `implementer` → `reviewer`

Proceed to Step 3b.

---

## Step 3a — Implementation, low-risk (implementer)

Invoke `implementer` with the approved Refactor Plan.
Implementer creates a Git Worktree before touching any file; all edits happen inside it.

implementer must:

1. Read the Refactor Plan before opening any file
2. Apply only the changes specified in the plan
3. Run the full test suite before and after — both runs must pass
4. Produce an Implementation Summary: what changed, what did not change, test
   results before and after

Any deviation from the plan — including "obvious improvements" encountered
during implementation — must be flagged and held for a separate task.

---

## Step 3b — Implementation, medium/high-risk (implementer)

Same rules as 3a. Additionally:

- implementer must document any unexpected complexity discovered during
  implementation and pause if the complexity changes the risk assessment
- If new risks are found, **STOP** and report to the human before continuing

---

## Step 4 — Test suite verification

For all risk levels, confirm:

- All tests that existed before the refactor still pass
- No test was deleted or commented out to make the suite pass
- Behavior documented in the Task Card remains unchanged

If any test fails that was passing before, the refactor has introduced a
regression. **STOP and report.**

---

## Step 5 — Code review (reviewer) — medium/high-risk only

Invoke `reviewer` with the diff and Refactor Plan.

reviewer must verify:

- The implementation matches the approved plan
- No behavior was changed beyond the plan's scope
- No unrelated files were modified

Review Report must include CRITICAL / WARNING / SUGGESTION findings. CRITICAL
findings block completion.

---

## Completion

Report: Refactor Plan (approved), Implementation Summary, test results before
and after, Review Report (if applicable).

The refactor is complete only when: all pre-existing tests still pass, the
implementation matches the approved plan exactly, and no CRITICAL review
findings remain.
