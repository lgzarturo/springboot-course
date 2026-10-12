---
name: security-web
description: >
  Review and harden web apps (authz, XSS, CSRF, SSRF, injection) on code you maintain. Complements the OWASP `security` skill. No exploit kits.
---

# Web Application Security

## Authorization

Use this skill only with **explicit written authorization** for a named
scope (systems, environments, and time window). If authorization is missing,
expired, or the request is for someone else's systems, **stop**.

This skill is **defensive and owner-authorized**. It does not authorize
offensive cyber operations.

## Do

- Enforce authorization on every handler; never rely on the UI
- Parameterize queries; reject unknown fields
- Treat cookies, tokens, and redirects as untrusted until validated
- Add tests for authz, CSRF, and open redirects when you change those paths

## Do not

- Provide XSS or SQLi payloads for use against systems you do not own
- Disable CSRF or SameSite 'to make local dev easier' in production configs

- Do not produce exploit payloads, malware, or attack procedures.

## How to Use

Load this skill from `/cc-security` when the Task Card domain is `security-web`.
Example prompts:

- `Review our Next.js Server Actions for IDOR and CSRF. Propose tests, not exploits.`
- `Harden the file-upload endpoint: type, size, path containment, and tests.`

## Integration

- Workflow: `/cc-security` (CCEP command `security`)
- Existing OWASP application-security skill remains `security` (not this id)
- High-risk changes still require `security-reviewer`
