---
name: council
description: "Multi-agent council for code review and architecture decisions"
version: 0.1.0
---

## Agents
- **Architect** (architect): architecture, design-patterns, code-structure
- **Product** (product): requirements, ux, business-value
- **Delivery** (delivery): delivery, testing, deployment
- **DataOps** (data-ops): data, pipelines, analytics
- **Security Reviewer** (security-reviewer): security, vulnerabilities, compliance, credentials, injection, auth, supply-chain
- **Devil** (devil): review, edge-cases, failure-modes

## Usage
Use the council agents to get multi-perspective analysis on code changes, architecture decisions, and security reviews.
Keep GPT-6.1 Sol at medium reasoning effort. Share a concise evidence summary
and relevant diff with only the necessary council roles. Reuse findings in the
final verdict instead of replaying the full transcript or repeating analysis.
