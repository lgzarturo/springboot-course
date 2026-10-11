---
name: security-ai-llm
description: >
  Harden LLM and agent integrations you operate: prompt injection, tool allowlists, secret leakage, and supply-chain of models. Defensive only.
---

# AI / LLM Security

## Authorization

Use this skill only with **explicit written authorization** for a named
scope (systems, environments, and time window). If authorization is missing,
expired, or the request is for someone else's systems, **stop**.

This skill is **defensive and owner-authorized**. It does not authorize
offensive cyber operations.

## Do

- Treat model output as untrusted; never exec it without a schema
- Allowlist tools and paths; contain file writes
- Keep secrets out of prompts and logs

## Do not

- Help jailbreak third-party products or steal model weights
- Log raw user PII to prompt-debug stores

- Do not produce exploit payloads, malware, or attack procedures.

## How to Use

Load this skill from `/cc-security` when the Task Card domain is `security-ai-llm`.
Example prompts:

- `Review our agent tool runner for path traversal and unbounded shell.`
- `Add tests that a prompt cannot make the bot dump .env.`

## Integration

- Workflow: `/cc-security` (CCEP command `security`)
- Existing OWASP application-security skill remains `security` (not this id)
- High-risk changes still require `security-reviewer`
