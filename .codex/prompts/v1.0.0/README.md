# Agent Contracts v1.0.0

First stable, distributable contract set. Copied from `v0.5.0` and aligned with
the CodeConductor Execution Protocol (CCEP-1) and the current workflow profiles.

## Contracts

| Contract | Role in the workflow |
| -------- | -------------------- |
| `orchestrator.md` | Validates Task Cards, selects the route, monitors completion. |
| `task-coach.md` | Turns vague requests into complete, routable Task Cards (intake). |
| `planner.md` | CCEP-1 intake role — serializes intent into `planner-output`. |
| `goal-planner.md` | Turns an objective string into a YAML task graph. |
| `repo-explorer.md` | Read-only repo map, conventions, impact radius. |
| `architect.md` | Technical Plan, ADRs, module boundaries. |
| `contract-builder.md` | API contracts / data shapes before implementation (DDD→SDD→TDD). |
| `tester.md` | Failing-first tests that verify acceptance criteria. |
| `implementer.md` | Minimal-diff implementation of the approved plan. |
| `complexity-auditor.md` | Bloat / dependency / cyclomatic audit (feeds `cc-gain`). |
| `reviewer.md` | Correctness, scope, security, and architecture review. |
| `security-reviewer.md` | Dedicated high-risk security review with veto authority. |
| `docs.md` | README / OpenAPI / ADR / CHANGELOG sync. |
| `devil.md` | Devil's advocate for the `council` (adversarial) workflow. |

## What changed from v0.5.0

- **CCEP-1 alignment** — every contract now carries a "CCEP-1 structured output"
  section mapping its Markdown Deliverable to the runtime JSON schema
  (`planner-output`, `technical-plan`, `implementer-output`, `review-report`,
  `council-verdict`, `agent-output`).
- **New `devil.md`** — the devil's advocate role required by the `council`
  workflow profile (previously referenced without a contract).
- **New `planner.md`** — the CCEP-1 intake role carried forward from the v0.6.0
  draft and finalized.
- **Orchestrator** — added the `council` workflow, the `devil` routing row, a
  workflow-commands table, and a CCEP-1 confirmation-gate section.
- Intake/design/test/review contracts stop on unresolved branches, wayfinding
  names the next `/cc-` command, TDD forbids green-by-weakening, and `docs` may
  write session handoffs only to `.codeconductor/sessions/handoff.md` (gitignored,
  secrets redacted).
- Version markers and scorecard `contract_version` bumped to `v1.0.0`.

## Versioning

Contracts are append-only. Versions `v0.1.0`–`v0.4.0` are deprecated (see each
directory's `DEPRECATED.md` and `docs/prompt-versioning.md`). Pin the version via
`contract_version` in `codeconductor.config.jsonc`.
