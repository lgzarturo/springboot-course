---
name: evaluation
description:
  Guides agents through scorecards, outcomes, model profiles, and eval suites.
  Use when running /cc-scorecard or /cc:scorecard, measuring a deliverable, or
  checking workflow gates with suite-run.
---

# Evaluation

## Overview

A scorecard measures the deliverable against eight weighted criteria. Spec
quality checklists are reviewer-owned. "Seems right" is not a verdict.

Pass threshold: weighted score >= 2.0 and no criterion at 0.

## When to Use

- After implement/review, before `openspec archive`
- Comparing models or prompt versions
- Proving the workflow tools still work (`suite-run`)

**NOT** for rewriting specs (reviewer checklist) or for implementing code.

## Process

The CLI is always `npx cc-codeconductor`.

```text
scorecard create --task BC-001 --from-diff
scorecard record --task BC-001 --verdict PASS --score 2.5
scorecard list | aggregate | models | regression | matrix | compare-models
scorecard prompt-diff 0.4.0 0.5.0 --agent architect
scorecard experiment start --suite harness-v1
scorecard suite-run --suite workflow-gates
scorecard suite-run --suite hook-guardrails
scorecard suite-run --suite scorecard-signals
```

`openspec analyze` can auto-suggest `acceptance` / `tests` on `--from-diff`.
Archive needs PASS when review is required.

Outcomes append to `.codeconductor/evaluation/outcomes.jsonl`.

## Common Rationalizations

| Rationalization | Reality |
| --- | --- |
| I'll fill the scorecard by hand without a diff | Use `--from-diff` and runner evidence. |
| Suites are optional toys | `suite-run` is the workflow tool that proves gates. |
| Handmade TDD JSON is fine | The runner rejects it. |

## Red Flags

- PASS with a criterion at 0
- Archive without a recorded scorecard when review is required
- Declaring the workflow ready without `suite-run` or `scorecard record`

## Verification

- [ ] Scorecard created from diff (or explicit scores)
- [ ] Verdict PASS / REVISE / REJECT recorded
- [ ] For process changes: `scorecard suite-run --suite hook-guardrails` (and
      `workflow-gates` / `scorecard-signals` when those gates changed)
