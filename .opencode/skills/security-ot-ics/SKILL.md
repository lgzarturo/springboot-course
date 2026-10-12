---
name: security-ot-ics
description: >
  Advise on defensive security for industrial control systems you operate. Safety first. No process-disruption guidance.
---

# OT / ICS Security

## Authorization

Use this skill only with **explicit written authorization** for a named
scope (systems, environments, and time window). If authorization is missing,
expired, or the request is for someone else's systems, **stop**.

This skill is **defensive and owner-authorized**. It does not authorize
offensive cyber operations.

## Do

- Separate OT networks; no direct internet to PLCs
- Change default credentials; document who can stop a line
- Patch with maintenance windows; test on a replica

## Do not

- Suggest actions that could halt unsafe physical processes
- Scan or 'test' live safety systems without OT engineering approval

- Do not produce exploit payloads, malware, or attack procedures.

## How to Use

Load this skill from `/cc-security` when the Task Card domain is `security-ot-ics`.
Example prompts:

- `Review the jump-host design between IT and the plant VLAN.`
- `Draft a change window checklist for a PLC firmware update.`

## Integration

- Workflow: `/cc-security` (CCEP command `security`)
- Existing OWASP application-security skill remains `security` (not this id)
- High-risk changes still require `security-reviewer`
