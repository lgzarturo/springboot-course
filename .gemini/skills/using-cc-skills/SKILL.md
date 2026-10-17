---
name: using-cc-skills
description:
  Maps incoming work to the CodeConductor slash command and workflow skill.
  Use when starting a session or deciding which /cc-* command applies.
---

# Using CodeConductor skills

## Overview

Pick one slash command. Follow its skill. Invoke CLI for gates. Do not invent
a parallel process.

## When to Use

- Start of a session, ambiguous request, or "which /cc should I run?"

## Process

| Intent | Command | Skill |
| --- | --- | --- |
| New backlog item | `/cc-backlog` | `backlog` |
| Deliver a BC-xxx item | `/cc-openspec` | `openspec` |
| New feature | `/cc-feature` | `openspec` + `testing-tdd` |
| Bug fix | `/cc-fix` | `testing-tdd` |
| Review a diff | `/cc-review` | `evaluation` |
| TDD cycle only | `/cc-tdd-cycle` | `testing-tdd` |
| Scorecard / suites | `/cc-scorecard` | `evaluation` |

Then run the matching CLI (`openspec validate`, `scorecard create --from-diff`,
`hook pre-tool`, `scorecard suite-run`).

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
applicable while preserving the current workflow gates. Kotlin, Compose, framework,
or dependency presence alone does not activate it; non-UI Android work stays with
the general `android` skill. Audit-only requests remain read-only, and unavailable
visual or emulator checks remain pending rather than claimed as complete.

## Common Rationalizations

| Rationalization | Reality |
| --- | --- |
| I'll skip the slash and just code | Skipping the workflow is a defect. |

## Red Flags

- Two slash commands in parallel that mutate the same files
- Implementing before `openspec analyze` when a change folder is active

## Verification

- [ ] One command selected and shown to the user
- [ ] Matching skill loaded before edits
