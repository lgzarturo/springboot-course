---
name: security-mobile
description: >
  Harden iOS/Android apps you ship: storage, TLS pinning policy, IPC, and backend trust. You still enforce authz on the server.
---

# Mobile App Security

## Authorization

Use this skill only with **explicit written authorization** for a named
scope (systems, environments, and time window). If authorization is missing,
expired, or the request is for someone else's systems, **stop**.

This skill is **defensive and owner-authorized**. It does not authorize
offensive cyber operations.

## Do

- Assume the client is hostile; enforce authz on APIs
- Review local storage, backup flags, and WebView URLs
- Pin or constrain TLS per org policy; never ship debug trust-all

## Do not

- Provide IPA/APK cracking or cert-unpinning recipes for others' apps
- Store long-lived tokens in world-readable prefs

- Do not produce exploit payloads, malware, or attack procedures.

## How to Use

Load this skill from `/cc-security` when the Task Card domain is `security-mobile`.
Example prompts:

- `Review Android SharedPreferences usage for tokens in this module.`
- `Check that certificate pinning failures fail closed in release builds.`

## Integration

- Workflow: `/cc-security` (CCEP command `security`)
- Existing OWASP application-security skill remains `security` (not this id)
- High-risk changes still require `security-reviewer`
