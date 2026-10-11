---
name: cc-feature
description:
  Run the full feature workflow — task validation, technical design,
  implementation, testing, review, and documentation.
---

# Feature Workflow

Feature request: $ARGUMENTS

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


## Step 1 — Task Card validation (task-coach)

Invoke `task-coach` with the feature description above.

task-coach must produce a complete Task Card before any other agent runs. The
Task Card is ready when it contains: title, type, risk classification, scope,
context, acceptance criteria, and constraints.

If any field is missing or ambiguous, task-coach must ask one clarifying
question at a time and wait for the answer. Do not proceed with an incomplete
Task Card.

**STOP here. Show the completed Task Card and wait for human confirmation before
continuing.**

---

## Step 2 — Technical Plan (architect)

Invoke `architect` with the validated Task Card from Step 1.

architect must produce a Technical Plan that covers:

- Chosen approach and rationale
- Affected files and modules
- Data model changes (if any)
- API contract changes (if any)
- Identified risks and mitigations
- Open questions that require a human decision

**STOP here. Show the Technical Plan and wait for explicit human approval. Do
not invoke implementer until the plan is approved.**

---

## Step 3 — Test coverage (tester)

Invoke `tester` with the approved Technical Plan and the Task Card.

tester must:

1. Write failing tests to cover the new behavior (RED)
2. Ensure all acceptance criteria from the Task Card have at least one test
3. Confirm the new tests fail for the right reason before implementation
4. Produce a Coverage Summary: test files added or modified, cases covered

---

## Step 4 — Implementation (implementer)

Invoke `implementer` with the approved Technical Plan, the Task Card, and the
failing tests from Step 3.
Implementer creates a Git Worktree before touching any file; all edits happen inside it.

implementer must:

1. Read the Technical Plan before touching any file
2. Apply the minimal diff to make the failing tests pass — only what the plan specifies
3. Run the project test suite after implementation
4. Produce an Implementation Summary: what changed, which files, how to verify
   locally

---

## Step 5 — Code review (reviewer)

Invoke `reviewer` with the complete diff and the Task Card.

reviewer must produce a Review Report with findings categorized as:

- CRITICAL — must be fixed before merge
- WARNING — must be resolved before merge
- SUGGESTION — optional improvement

If any CRITICAL findings exist, **STOP and report them**. Do not proceed until
they are resolved and re-reviewed.

---

## Step 6 — Documentation (docs)

Invoke `docs` only if any of the following changed:

- A public API endpoint was added or modified
- A public interface or module was introduced
- Behavior visible to end users changed

docs must update: README (if applicable), OpenAPI spec (if applicable),
CHANGELOG (always), ADR (if an architectural decision was made).

---

## Completion

Report the following to the human:

- Task Card (final)
- Technical Plan (approved)
- Implementation Summary
- Coverage Summary
- Review Report (all findings resolved)
- List of documentation files updated (if any)

The feature is complete only when: all tests pass, no CRITICAL review findings
remain, and documentation reflects the implemented behavior.
