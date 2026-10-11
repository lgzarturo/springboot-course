---
name: security-log-analysis
description: >
  Query and interpret security logs you are authorized to read. Build detections and investigations without leaking PII.
---

# Security Log Analysis

## Authorization

Use this skill only with **explicit written authorization** for a named
scope (systems, environments, and time window). If authorization is missing,
expired, or the request is for someone else's systems, **stop**.

This skill is **defensive and owner-authorized**. It does not authorize
offensive cyber operations.

## Do

- Minimize PII in saved queries and screenshots
- Time-box queries; record the query and the index in the ticket
- Correlate identity, source IP, and resource — not just a single field

## Do not

- Export full production logs to a laptop or public gist
- Query other customers' tenants in a multi-tenant SaaS

- Do not produce exploit payloads, malware, or attack procedures.

## How to Use

Load this skill from `/cc-security` when the Task Card domain is `security-log-analysis`.
Example prompts:

- `Find failed logins then a success from a new country for our IdP (last 24h).`
- `Explain this CloudTrail burst: is it deploy automation or a key leak?`

## Integration

- Workflow: `/cc-security` (CCEP command `security`)
- Existing OWASP application-security skill remains `security` (not this id)
- High-risk changes still require `security-reviewer`
