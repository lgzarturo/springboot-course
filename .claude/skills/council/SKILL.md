---
name: council
description: "Multi-agent council for code review and architecture decisions"
---

# Council Skill

## Description
Multi-agent council for code review and architecture decisions

## Version
0.1.0

## Agents
- **Architect** (architect): architecture, design-patterns, code-structure
- **Product** (product): requirements, ux, business-value
- **Delivery** (delivery): delivery, testing, deployment
- **DataOps** (data-ops): data, pipelines, analytics
- **Security Reviewer** (security-reviewer): security, vulnerabilities, compliance, credentials, injection, auth, supply-chain
- **Devil** (devil): review, edge-cases, failure-modes

## Usage
Use the council agents to get multi-perspective analysis on code changes, architecture decisions, and security reviews.
Use Sonnet 5.5 for the routine council pass. Share one concise evidence summary and
the relevant diff with the necessary roles. Invoke council-architect with
`model: opus` only for
one unresolved business-logic question; return its short decision to Sonnet for
the verdict. Do not run every council role on Opus or repeat full context.

## Context
v1
