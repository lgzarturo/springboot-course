---
name: security-blue-team
description: >
  Improve detections, hardening, and response for the estate you defend. Detection-as-code and control design, not offense.
---

# Blue Team Defense

## Authorization

Use this skill only with **explicit written authorization** for a named
scope (systems, environments, and time window). If authorization is missing,
expired, or the request is for someone else's systems, **stop**.

This skill is **defensive and owner-authorized**. It does not authorize
offensive cyber operations.

## Do

- Tune detections for true positives; document exceptions
- Pair every new control with a test or canary
- Share hunt hypotheses with IR, not with the public internet

## Do not

- Disable EDR or logging to 'reduce noise' without a ticket
- Copy attacker malware into production 'for training'

- Do not produce exploit payloads, malware, or attack procedures.

## How to Use

Load this skill from `/cc-security` when the Task Card domain is `security-blue-team`.
Example prompts:

- `We missed a token-replay. Propose a detection plus a unit test for the API gateway.`
- `Harden CI: pin actions, require reviews on workflows that deploy.`

## Integration

- Workflow: `/cc-security` (CCEP command `security`)
- Existing OWASP application-security skill remains `security` (not this id)
- High-risk changes still require `security-reviewer`
