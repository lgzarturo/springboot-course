---
description: >-
  Compact the session into gitignored `.codeconductor/sessions/handoff.md` (redact secrets).
---

# Handoff Workflow

Handoff request: $ARGUMENTS

## Step 0 — CCEP Bootstrap

Command: `handoff` (fixed for this workflow — do not infer from user text)

1. Run: `npx cc-codeconductor ccep profile handoff --output json` to get the phases and their roles.
2. For each delegated phase, run: `npx cc-codeconductor ccep compile --command handoff --phase <phase-id> "$ARGUMENTS" --view prompt --output json`
3. After planner/intake JSON is available, run: `npx cc-codeconductor ccep evaluate --command handoff --input <planner.json> --output json`. If `stop` is true, show questions or risks and wait for human input.
4. Pass each subagent only the compiled `prompt` for its phase — never forward raw `$ARGUMENTS` to planners.

---

## Step 1 — Compact (docs)

Invoke `docs`. Write **only** `.codeconductor/sessions/handoff.md` (gitignored).
Do not write `.codeconductor/handoff.md` or any tracked path.

Before writing, redact secrets, tokens, API keys, passwords, connection strings,
`.env` contents, and log/stack-trace lines that embed those values. Summarize
errors instead of pasting dumps. If unresolved credential material remains, stop
and wait for a human (CCEP `stopOnHighRisk`).

Include: goal, Task Card status (no secret fields), files touched, test
pass/fail (not log dumps), open questions, the next `/cc:` command, and the
recommended `context_scope` (`isolated` | `continuation` | `full`) for the next
session — derived from the Task Card status above, not invented, with a
one-sentence justification.

Link the Delivery Ledger and verification evidence when they exist; do not
repeat the original request or transcript. Do not edit source or tests.

If a Delivery Ledger exists, run `npx cc-codeconductor odd handoff --id <ledger-id> --output json`
and use its `handoff` envelope as the source for these fields. Secret redaction
still applies.

---

## Completion

Report the handoff path. Another session should be able to continue from that file alone.
