---
name: security-recon
description: >
  Map owned assets, attack surface, and inventory for systems the requester is authorized to assess. Use for asset discovery, service catalogs, and exposure reviews — never unauthorized scanning.
---

# Authorized Reconnaissance

## Authorization

Use this skill only with **explicit written authorization** for a named
scope (systems, environments, and time window). If authorization is missing,
expired, or the request is for someone else's systems, **stop**.

This skill is **defensive and owner-authorized**. It does not authorize
offensive cyber operations.

## Do

- Inventory hosts, services, and identities the authorization letter names
- Prefer read-only, rate-limited discovery against owned environments
- Record data sources (CMDB, cloud APIs, repo maps) in the Task Card
- Flag shadow IT only to the asset owner

## Do not

- Scan networks, IPs, or tenants outside the authorization window
- Use third-party scanners against production without change control
- Store credentials, tokens, or dumps in the skill output

- Do not produce exploit payloads, malware, or attack procedures.

## How to Use

Load this skill from `/cc-security` when the Task Card domain is `security-recon`.
Example prompts:

- `Inventory the staging VPC we own: services, public endpoints, and IAM principals in scope for Q3.`
- `From this repo, list internet-facing routes and compare them to the authorized asset list.`

## Integration

- Workflow: `/cc-security` (CCEP command `security`)
- Existing OWASP application-security skill remains `security` (not this id)
- High-risk changes still require `security-reviewer`
