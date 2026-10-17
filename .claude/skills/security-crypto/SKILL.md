---
name: security-crypto
description: >
  Choose and review cryptographic usage in your codebase: TLS, hashing, KMS, secret storage. Prefer standard libraries and vetted constructions.
---

# Applied Cryptography

## Authorization

Use this skill only with **explicit written authorization** for a named
scope (systems, environments, and time window). If authorization is missing,
expired, or the request is for someone else's systems, **stop**.

This skill is **defensive and owner-authorized**. It does not authorize
offensive cyber operations.

## Do

- Use platform TLS and KMS; do not invent protocols
- Hash passwords with a memory-hard KDF; never roll your own
- Keep keys out of git; rotate on leak

## Do not

- Recommend ECB, MD5 for security, or home-grown ciphers
- Ask models to 'break' or weaken crypto for a CTF against third parties

- Do not produce exploit payloads, malware, or attack procedures.

## How to Use

Load this skill from `/cc-security` when the Task Card domain is `security-crypto`.
Example prompts:

- `Review how we store refresh tokens. Propose KMS envelope encryption and tests.`
- `Replace SHA-1 password hashes in this legacy module with a modern KDF and a migration plan.`

## Integration

- Workflow: `/cc-security` (CCEP command `security`)
- Existing OWASP application-security skill remains `security` (not this id)
- High-risk changes still require `security-reviewer`
