---
name: security-incident-response
description: >
  Coordinate containment, eradication, and recovery for an authorized incident. Preserve evidence, communicate facts, and avoid destructive actions without approval.
---

# Incident Response

## Authorization

Use this skill only with **explicit written authorization** for a named
scope (systems, environments, and time window). If authorization is missing,
expired, or the request is for someone else's systems, **stop**.

This skill is **defensive and owner-authorized**. It does not authorize
offensive cyber operations.

## Do

- Follow the org IR plan: detect, contain, eradicate, recover, lessons
- Preserve logs and disk images before reboot or wipe
- Redact secrets from tickets and chat
- Track timeline, systems, and decision-makers

## Do not

- Wipe systems before evidence capture unless imminent ransomware spread is approved
- Blame individuals in the working notes; stick to systems and facts
- Reuse attacker tooling from the incident as a 'test'

- Do not produce exploit payloads, malware, or attack procedures.

## How to Use

Load this skill from `/cc-security` when the Task Card domain is `security-incident-response`.
Example prompts:

- `P1: suspected token theft on api-prod. Draft containment and evidence steps for the IR commander.`
- `After containment, write the lessons-learned outline and the regression tests we should add.`

## Integration

- Workflow: `/cc-security` (CCEP command `security`)
- Existing OWASP application-security skill remains `security` (not this id)
- High-risk changes still require `security-reviewer`
