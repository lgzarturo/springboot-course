---
name: security-red-team
description: >
  Plan purple-team / adversary-simulation exercises against systems you are contracted to test. High-level objectives and detection gaps only. No live attack procedures or payloads.
---

# Authorized Adversary Simulation (Planning)

## Authorization

Use this skill only with **explicit written authorization** for a named
scope (systems, environments, and time window). If authorization is missing,
expired, or the request is for someone else's systems, **stop**.

This skill is **defensive and owner-authorized**. It does not authorize
offensive cyber operations.

## Do

- Require a rules-of-engagement document before any exercise
- Map ATT&CK techniques to detections you want to validate
- Coordinate with blue team; prefer tabletop if production risk is high

## Do not

- Provide exploit steps, malware, phishing kits, or C2 playbooks
- Run unapproved tests against production
- Help with unauthorized 'red team' against third parties

- Do not produce exploit payloads, malware, or attack procedures.

## How to Use

Load this skill from `/cc-security` when the Task Card domain is `security-red-team`.
Example prompts:

- `Draft ROE and in-scope techniques for an internal purple-team week on staging.`
- `List detections we should have for credential stuffing against our login — no attack scripts.`

## Integration

- Workflow: `/cc-security` (CCEP command `security`)
- Existing OWASP application-security skill remains `security` (not this id)
- High-risk changes still require `security-reviewer`
