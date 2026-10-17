---
name: security-soc-automation
description: >
  Automate detection, enrichment, and response playbooks for a SOC you operate. Keep humans in the loop for destructive actions.
---

# SOC Automation

## Authorization

Use this skill only with **explicit written authorization** for a named
scope (systems, environments, and time window). If authorization is missing,
expired, or the request is for someone else's systems, **stop**.

This skill is **defensive and owner-authorized**. It does not authorize
offensive cyber operations.

## Do

- Automate enrichment (WHOIS, asset owner, hash reputation) not auto-wipe
- Version playbooks; require approval for isolate/disable-user actions
- Test detections with synthetic events in a lab tenant

## Do not

- Auto-disable production users or servers without an approval gate
- Pipe raw secrets from tickets into chat webhooks

- Do not produce exploit payloads, malware, or attack procedures.

## How to Use

Load this skill from `/cc-security` when the Task Card domain is `security-soc-automation`.
Example prompts:

- `Draft a SOAR playbook: phishing report → hash lookup → ticket, no auto-containment.`
- `Turn this Sigma-style idea into a documented detection plus a false-positive test.`

## Integration

- Workflow: `/cc-security` (CCEP command `security`)
- Existing OWASP application-security skill remains `security` (not this id)
- High-risk changes still require `security-reviewer`
