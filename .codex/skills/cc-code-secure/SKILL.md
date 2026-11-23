---
name: cc-code-secure
description: Scan a diff for dangerous patterns, review it without session context, and suggest data-flow tests before review.
---

# code-secure

Invoke as `$cc-code-secure`. The user request follows the skill mention.

# Code Secure

Target: $ARGUMENTS

Accepted formats:

- A branch name: `feature/my-branch`
- A file or set of files: `src/api/UserController.kt`
- A pull request reference: `PR #42`
- Empty — the working diff (`git diff HEAD`)

This workflow is read-only. It does not edit code, install tools, write CI or
hooks, or start `$cc-review`. It produces a Code Secure Brief that `$cc-review`
can consume. `$cc-security` remains the authorized hardening workflow and can
design and implement a fix.

---

## Scope

Review the local diff only. Refuse exploit development, proof-of-concept
payloads, malware, and any scan of a third-party host. Do not explain how to
evade a check.

---

## Step 0 — CCEP Bootstrap

Command: `code-secure` (fixed for this workflow — do not infer from user text)

1. Run: `npx cc-codeconductor ccep profile code-secure --output json` to get the phases and their roles.
2. For each delegated phase, run: `npx cc-codeconductor ccep compile --command code-secure --phase <phase-id> "$ARGUMENTS" --view prompt --output json`
3. After planner/intake JSON is available, run: `npx cc-codeconductor ccep evaluate --command code-secure --input <planner.json> --output json`. If `stop` is true, show questions or risks and wait for human input.
4. Pass each subagent only the compiled `prompt` for its phase — never forward raw `$ARGUMENTS` to planners.

---

## Step 1 — Pattern advisory

Run:

`npx cc-codeconductor code-secure scan --json`

Empty $ARGUMENTS scans `git diff HEAD`. When $ARGUMENTS names paths, pass them
after `scan`. Piped stdin is used when it is present.

Every hit is `advisory`. The regex does not follow data flow and is easy to
miss a real bug. The command exits 0 even when hits exist. Do not add these
patterns to the pre-tool hook: that hook denies commands, and these signatures
must not become a block.

Record the `tools` array from the JSON:

- Betterleaks, or Gitleaks when Betterleaks is absent, may scan the diff
  offline. Neither blocks the commit from this plugin. If the binary is
  missing, say the pre-commit control is not installed.
- TruffleHog belongs in CI as `trufflehog --only-verified`, plus a weekly
  history sweep. Do not run it here. It calls issuer APIs, and this role has
  no network.
- Semgrep is not launched, because registry packs would be downloaded. When
  the binary exists, the blocking SAST command is community `p/java`,
  `p/kotlin`, `p/php`, `p/django`, `p/nextjs`, `p/owasp-top-ten`, and
  `p/spring`. 181 of 241 `p/java` rules and 63 of 73 `p/kotlin` rules are Pro
  and need login. elttam community rules add Spring, Jackson, and XStream. A
  community-only run is not full coverage.

---

## Step 2 — Diff review without session context

Collect the diff the same way `$cc-review` does (`git diff HEAD`, or
`git diff main...$ARGUMENTS` for a branch).

Adopt the `security-reviewer` role as defined in `AGENTS.md`. When this runner
exposes a `security-review` subagent, use that subagent for this step instead.
Pass only the diff and the scan JSON. Do not pass the chat, the session
objective, or the Task Card.

The reviewer looks for high-severity issues that must be fixed before review,
and suggests static-analysis hardening (a Semgrep pack, a Betterleaks or
Gitleaks pre-commit, a TruffleHog CI job). It does not edit files.

A regex hit becomes must-fix only when this pass, or an external tool in the
scan JSON, confirms it.

---

## Step 3 — Data-flow tests

Adopt the `tester` role as defined in `AGENTS.md`. This step receives the session
objective, the modified files, and the tests that already cover those files.

Check whether a test follows the data from the entry point to the sink. Suggest
cases that name the input, the trust boundary, and the assertion. Do not write
the tests. Do not suggest a synthetic test: an assertion that is always true,
or a mock that never walks the flow, does not count.

---

## Step 4 — Code Secure Brief

```markdown
## Code Secure Brief

### Layer 1 — Advisory patterns
- [file:line] id — tool that should own this class

### External tools
- betterleaks / gitleaks / semgrep / trufflehog — present, executed, detail

### Layer 2 — Must-fix
- [file:line] confirmed by diff review or an external tool

### Linter hardening
- suggestion, with the tool and the pack or hook

### Layer 3 — Data-flow tests
- existing coverage, or a suggested case (input, boundary, assertion)
```

When the must-fix list has an item, stop. Do not recommend `$cc-review` until
those items are corrected. When it is empty, the next command is `$cc-review`
and the brief is the context. Do not start that command from this workflow.
