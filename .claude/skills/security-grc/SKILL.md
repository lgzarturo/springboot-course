---
name: security-grc
description: >
  Map controls to SOC2, ISO 27001, PCI, or internal policy for the org you work for. Evidence and control design — not audit theater.
---

# GRC / Compliance Engineering

## Authorization

Use this skill only with **explicit written authorization** for a named
scope (systems, environments, and time window). If authorization is missing,
expired, or the request is for someone else's systems, **stop**.

This skill is **defensive and owner-authorized**. It does not authorize
offensive cyber operations.

## Do

- Map each control to an owner, evidence path, and test
- Prefer automated evidence (CI, IaC, tickets) over screenshots
- Record exceptions with expiry dates

## Do not

- Fabricate evidence or backdate approvals
- Copy another company's policies without legal review

- Do not produce exploit payloads, malware, or attack procedures.

## How to Use

Load this skill from `/cc-security` when the Task Card domain is `security-grc`.
Example prompts:

- `Map our GitHub branch protection to SOC2 CC6.6 and list evidence.`
- `List gaps: secrets in git, missing MFA on GitHub org owners.`

## Integration

- Workflow: `/cc-security` (CCEP command `security`)
- Existing OWASP application-security skill remains `security` (not this id)
- High-risk changes still require `security-reviewer`
