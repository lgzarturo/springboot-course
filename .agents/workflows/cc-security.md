---
name: cc-security
description: >
  Run the authorized defensive security workflow — domain security-* skills,
  authorization gate, risk-based routing, hardening, and review.
---

# Defensive Security Workflow

Security objective: $ARGUMENTS

Provide the following information in $ARGUMENTS:

- What must be reviewed or hardened, and why
- Domain: a `security-*` skill id (recon, vuln-assessment, web, cloud, IR,
  hunting, GRC, …) or `web-app` / `api` / `cloud-config` /
  `dependency-supply-chain` / `secrets`
- Authorization: who authorized this work on this target, and the scope boundary
- Risk classification (`low`, `medium`, or `high`), if known
- Scope: which files, modules, repos, or environments are in and out of bounds

---

## Scope and authorization

This workflow performs **authorized defensive security work only**: threat
modeling, hardening, secure configuration, dependency and supply-chain review,
secret hygiene, and detection coverage.

Refuse outright and offer the defensive equivalent instead (threat model,
hardening, detection coverage, dependency audit):

- Exploit or proof-of-concept development
- Malware authoring, or malware analysis intended for reuse
- Reverse engineering aimed at bypassing a control
- Red-team playbooks
- Any unauthorized-access procedure

Scope is limited to the repository under analysis. Do not scan, probe, or
perform reconnaissance against third-party hosts.

Do not proceed without an authorization statement. Do not produce exploit
payloads, malware, or attack procedures.

---

## Step 0 — CCEP Bootstrap

Command: `security` (fixed for this workflow — do not infer from user text)

1. Run: `npx cc-codeconductor ccep profile security --output json` to get the phases and their roles.
2. For each delegated phase, run: `npx cc-codeconductor ccep compile --command security --phase <phase-id> "$ARGUMENTS" --view prompt --output json`
3. After planner/intake JSON is available, run: `npx cc-codeconductor ccep evaluate --command security --input <planner.json> --output json`. If `stop` is true, show questions or risks and wait for human input.
4. Pass each subagent only the compiled `prompt` for its phase — never forward raw `$ARGUMENTS` to planners.
   Canonical delivery order is test-before-implement whenever both phases apply.

---

## Step 1 — Wayfinding (repo-explorer)

If `graphify-out/graph.json` exists, run `graphify query "$ARGUMENTS"` (and
`graphify path` / `graphify explain` when needed). Then invoke `repo-explorer`
to map modules, conventions, and impact radius. Do not write code in this step.
Record a Repo Map artifact before intake. Load the matching `security-*` skill
for the named domain (see `.agents/skills/security-*/SKILL.md`). Keep the OWASP
`security` skill for application-security reviews.

---

## Step 2 — Task Card validation (task-coach)

Invoke `task-coach` with the security objective above.

task-coach must produce a Task Card that includes:

- Objective: what must be reviewed or hardened, in one sentence
- Domain: which `security-*` skill applies, or `web-app` / `api` /
  `cloud-config` / `dependency-supply-chain` / `secrets`
- Authorization: who authorized this work on this target, and the scope boundary
- Risk classification: `low`, `medium`, or `high`
- Scope: which files or modules are likely affected

If authorization is absent, unclear, or names a system the requester does not
own or operate, **STOP and refuse. Do not route the task.**

Redact secrets, tokens, and credentials from any evidence before it enters the
Task Card. Summarize logs; do not paste env files.

**STOP here. Show the Task Card and wait for human confirmation.**

---

## Step 3 — Route by risk

Read the risk field from the Task Card and follow the corresponding route.

### Low-risk route

Applies when: the weakness is isolated, not exploitable in the current
configuration, and no authentication, secret, or dependency boundary is
involved.

Route: `task-coach` → `tester` → `implementer`

Proceed directly to Step 4 (tests), then Step 5a.

### Medium or high-risk route

Applies when: the work touches authentication, authorization, secrets,
cryptography, trust-boundary input validation, or a dependency upgrade.

Route: `task-coach` → `architect` → `tester` → `implementer` → `reviewer`

Invoke `architect` before implementation. architect must:

- Identify the weakness and the affected trust boundary
- Define the hardening approach and affected files
- Flag any regression risk to adjacent components
- Produce a Technical Plan

On **high** risk, also invoke `security-reviewer` before Reviewer.

**STOP here if high-risk. Show the Technical Plan and wait for human approval
before continuing.**

---

## Step 4 — Tests (tester)

Invoke `tester` for all risk levels.

tester must:

1. Write a regression test that proves the weakness exists and confirm it fails
   before any change (RED)
2. Verify that existing tests still pass
3. Produce a Coverage Summary: test added, case covered

---

## Step 5a — Implementation, low-risk (implementer)

Invoke `implementer` with the Task Card.
Implementer creates a Git Worktree before touching any file; all edits happen inside it.

implementer must:

1. Apply the minimal hardening change — no unrelated changes
2. Run the suite and make the RED regression test pass
3. Produce an Implementation Summary: weakness, change applied, files changed

---

## Step 5b — Implementation, medium/high-risk (implementer)

Invoke `implementer` with the approved Technical Plan and the Task Card.
Implementer creates a Git Worktree before touching any file; all edits happen inside it.

implementer must follow the plan exactly. Any deviation requires a new Technical
Plan approval. After implementation, run the full test suite. No exploit code.

---

## Step 6 — Review (Reviewer role) — medium/high-risk only

Invoke `reviewer` with the diff and Task Card. The Reviewer must apply the OWASP
rules in the `security` skill.

reviewer produces a Review Report with CRITICAL / WARNING / SUGGESTION findings.
If any CRITICAL findings exist, **STOP**. Do not close the task until they are
resolved.

---

## Completion

Report: Task Card, Implementation Summary, regression test added, Review Report
(if applicable). The task is complete only when: the regression test passes, the
full suite passes, and no CRITICAL review findings remain.

## Next

Run `/cc-review` on the diff before merging.
