---
name: cc-pipeline
description: Run the full 8-phase multi-agent workflow loop (Intake, Structure, Design, Test, Implement, Validate, Council, Compact).
---

# Multi-Agent Workflow Pipeline Loop

Scope: $ARGUMENTS

This workflow orchestrates the complete 8-phase development loop, combining Ponytail minimalism, DDD→SDD→TDD pipeline, and autonomous governance.

---

## Phase 1 — Intake (task-coach)

The `task-coach` agent collects the raw request, refines it with the user, and validates that all fields of the Task Card are filled out (title, type, risk, scope, context, acceptance criteria, constraints).

---

## Phase 2 — Structure (prompt-structurer)

Prunes the Task Card of redundant context, wraps parameters in XML tags (`<context>`, `<constraints>`, `<task>`, `<scope>`), and retrieves lightweight AST signatures instead of complete files to minimize token footprints.

---

## Phase 3 — Design (architect)

The `architect` reads AST signatures, creates the Technical Plan, and defines the Edge Case Matrix detailing potential failure modes and boundary conditions.

> [!IMPORTANT]
> **STOP GATE (SDD SPECIFICATION GATE):** Pauses execution and requests manual operator approval before writing any code or tests.

---

## Phase 4 — Test (adversarial-tester)

The `adversarial-tester` writes the TDD RED tests verifying happy paths, edge cases, and parameters. The test suite must be run to confirm it fails for the correct reasons.

---

## Phase 5 — Implement (implementer)

The `implementer` writes the minimal code to satisfy the tests. The implementer must edit only files within the approved plan scope. If tests fail, run implement-test loop iterations (max 3) before escalating.

---

## Phase 6 — Validate (tester)

Runs validation checks:
1. **Mutation Testing:** Automatically mutates operators and bounds to verify test assertions are strict (minimum score: 80%).
2. **Diff Scope Audit:** Scans modified files to prevent out-of-scope code additions or speculative implementations.

---

## Phase 7 — Council Verdict (council)

Reviewer agents audit the diff on 6 axes: Architecture, Security, Product, Delivery, Compliance, and Devil.

- **Veto check:** Security and Compliance reviewers can apply overriding vetoes (`securityVeto` / `complianceVeto`) to block a change.
- **Confidence check:** Escalates to human operator if individual confidence < 0.6 or average confidence < 0.7.

> [!IMPORTANT]
> **STOP GATE (VERDICT/MERGE GATE):** Pauses execution after Council consensus to request final manual confirmation before compaction and delivery.

---

## Phase 8 — Compact (compaction-hook)

Prunes RED/GREEN iteration files and writes a persistent summary pointer to `.codeconductor/memory.md` to update the repository memory without exceeding the 40KB file boundary.
