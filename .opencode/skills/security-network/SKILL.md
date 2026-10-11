---
name: security-network
description: >
  Harden network architecture, segmentation, TLS, and allowlists for systems you operate. Defensive design and review only.
---

# Network Security

## Authorization

Use this skill only with **explicit written authorization** for a named
scope (systems, environments, and time window). If authorization is missing,
expired, or the request is for someone else's systems, **stop**.

This skill is **defensive and owner-authorized**. It does not authorize
offensive cyber operations.

## Do

- Prefer deny-by-default, least privilege, and explicit allowlists
- Review security groups, NACLs, and mesh policies against the threat model
- Require TLS with modern ciphers; flag plaintext admin paths

## Do not

- Provide packet-crafting or MITM instructions against third-party networks
- Open 0.0.0.0/0 'just for debugging' in production

- Do not produce exploit payloads, malware, or attack procedures.

## How to Use

Load this skill from `/cc-security` when the Task Card domain is `security-network`.
Example prompts:

- `Review terraform for the prod VPC: public subnets, SG ingress, and admin jump box.`
- `Propose segmentation between PCI and non-PCI subnets with testable terraform assertions.`

## Integration

- Workflow: `/cc-security` (CCEP command `security`)
- Existing OWASP application-security skill remains `security` (not this id)
- High-risk changes still require `security-reviewer`
