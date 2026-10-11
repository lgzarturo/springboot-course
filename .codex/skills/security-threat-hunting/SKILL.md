---
name: security-threat-hunting
description: >
  Hunt for adversary techniques in telemetry you are authorized to query. Hypothesis-driven, evidence-first, no live offensive operations.
---

# Threat Hunting

## Authorization

Use this skill only with **explicit written authorization** for a named
scope (systems, environments, and time window). If authorization is missing,
expired, or the request is for someone else's systems, **stop**.

This skill is **defensive and owner-authorized**. It does not authorize
offensive cyber operations.

## Do

- State a hypothesis (technique + data source + expected artifact)
- Query only approved telemetry (SIEM, EDR, cloud logs)
- Promote confirmed hits to IR with timestamps and host identity

## Do not

- Run offensive tooling on production to 'verify' a hunt
- Query personal mailboxes or systems outside the authorization scope

- Do not produce exploit payloads, malware, or attack procedures.

## How to Use

Load this skill from `/cc-security` when the Task Card domain is `security-threat-hunting`.
Example prompts:

- `Hypothesis: stolen refresh tokens in our IdP logs this week. Query patterns and list follow-ups.`
- `Hunt for unusual service-account key creation in GCP for project X (authorized).`

## Integration

- Workflow: `/cc-security` (CCEP command `security`)
- Existing OWASP application-security skill remains `security` (not this id)
- High-risk changes still require `security-reviewer`
