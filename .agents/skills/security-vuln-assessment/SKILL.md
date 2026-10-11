---
name: security-vuln-assessment
description: >
  Plan and report authorized vulnerability assessments on owned systems. Prioritize evidence, severity, and remediations. Do not deliver exploit payloads.
---

# Vulnerability Assessment

## Authorization

Use this skill only with **explicit written authorization** for a named
scope (systems, environments, and time window). If authorization is missing,
expired, or the request is for someone else's systems, **stop**.

This skill is **defensive and owner-authorized**. It does not authorize
offensive cyber operations.

## Do

- Use scanner findings plus code review as evidence, not as a scoreboard
- Map issues to OWASP / CWE and to a concrete fix in the repo
- Require a reproduction that is safe (config, unit test, or staging)
- Track false positives explicitly

## Do not

- Ship exploit PoCs, weaponized payloads, or live attack scripts
- Scan production without a documented freeze and rollback
- Mark a finding closed without a failing-then-passing test when code changes

- Do not produce exploit payloads, malware, or attack procedures.

## How to Use

Load this skill from `/cc-security` when the Task Card domain is `security-vuln-assessment`.
Example prompts:

- `Assess the checkout API in staging (ticket SEC-214). Report exploitable authz gaps as remediations, not exploits.`
- `Triage SAST results on src/auth and turn confirmed issues into failing tests plus patches.`

## Integration

- Workflow: `/cc-security` (CCEP command `security`)
- Existing OWASP application-security skill remains `security` (not this id)
- High-risk changes still require `security-reviewer`
