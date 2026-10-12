---
name: security-reverse-engineering
description: >
  Analyze binaries or protocols you own (or have license to inspect) to improve security, compatibility, or incident response. Not for cracking, DRM bypass, or malware creation.
---

# Authorized Reverse Engineering

## Authorization

Use this skill only with **explicit written authorization** for a named
scope (systems, environments, and time window). If authorization is missing,
expired, or the request is for someone else's systems, **stop**.

This skill is **defensive and owner-authorized**. It does not authorize
offensive cyber operations.

## Do

- Confirm license and ownership before inspecting a binary
- Focus on trust boundaries, crypto misuse, and update integrity
- Document findings as hardening tasks with tests where code exists

## Do not

- Bypass license checks, DRM, or access controls you do not own
- Publish unpacking or unpacker tutorials for third-party software
- Produce malware droppers or obfuscators

- Do not produce exploit payloads, malware, or attack procedures.

## How to Use

Load this skill from `/cc-security` when the Task Card domain is `security-reverse-engineering`.
Example prompts:

- `Review our firmware update signer: how is the public key pinned, and what tests prove tamper detection?`
- `Map native JNI entrypoints in our Android app for unsafe string concatenation.`

## Integration

- Workflow: `/cc-security` (CCEP command `security`)
- Existing OWASP application-security skill remains `security` (not this id)
- High-risk changes still require `security-reviewer`
