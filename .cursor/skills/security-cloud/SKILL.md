---
name: security-cloud
description: >
  Harden IAM, storage, keys, and org policies in cloud accounts you administer. Focus on identity, logging, and public-exposure controls.
---

# Cloud Security

## Authorization

Use this skill only with **explicit written authorization** for a named
scope (systems, environments, and time window). If authorization is missing,
expired, or the request is for someone else's systems, **stop**.

This skill is **defensive and owner-authorized**. It does not authorize
offensive cyber operations.

## Do

- Prefer roles over long-lived keys; require MFA on human admins
- Block public buckets and overly broad IAM `*` unless justified
- Turn on cloud audit logs and retain them per policy

## Do not

- Disable logging or GuardDuty/Security Command Center 'to save cost' without a recorded exception
- Share account credentials in chat or tickets

- Do not produce exploit payloads, malware, or attack procedures.

## How to Use

Load this skill from `/cc-security` when the Task Card domain is `security-cloud`.
Example prompts:

- `Audit this AWS account's IAM: unused keys, admin wildcards, and public S3.`
- `Write a policy-as-code check that fails CI if a bucket ACL is public.`

## Integration

- Workflow: `/cc-security` (CCEP command `security`)
- Existing OWASP application-security skill remains `security` (not this id)
- High-risk changes still require `security-reviewer`
