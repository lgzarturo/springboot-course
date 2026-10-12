# OpenCode Preset for CodeConductor

## Model Selection Guide

This preset uses the four requested **OpenCode Go** models. Claude (Anthropic)
can be selected as an explicit override.

---

## Model Reference

### Claude Models (Anthropic)

| Model                       | Strength                        | Best For                        |
| --------------------------- | ------------------------------- | ------------------------------- |
| `claude-opus-5`             | Complex reasoning, architecture | Architect, complex design       |
| `claude-sonnet-5`           | Balanced, general purpose       | Implementation, testing         |
| `claude-haiku-4-5-20251001` | Fast, lightweight               | Task Coach, Docs, Repo Explorer |

### OpenCode Go models

Canonical role assignments live in `src/presets/models/roles.yml`. Model IDs
were verified against [OpenCode Go](https://opencode.ai/docs/go/) and the live
catalog on September 25, 2026.

| Model | Best for |
| --- | --- |
| `opencode-go/deepseek-v4.1-flash` | Architect, reviewer, contract-builder, orchestrator (TUI default), planner, goal-planner |
| `opencode-go/mimo-v2.6-flash` | Implementer, tester |
| `opencode-go/glm-5.3-flash` | Security-reviewer, devil, complexity-auditor, task-coach, repo-explorer |
| `opencode-go/muse-spark-1.3-contributor` | Docs |

These assignments are workflow choices, not a provider ranking. Generated
agents leave sampling and reasoning settings at provider defaults rather than
copying Claude-specific `effort` or forcing a shared `temperature` onto different
models. No unverified variants are configured.

Muse Contributor is included at the request of the maintainer. OpenCode documents
that it uses prompts/completions for training, is not ZDR, and has regional
restrictions. Do not send confidential material to that model; override the docs
agent with another verified model when required.

---

## Agent Model Matrix

See `skills/cc-update-preset-models/references/role-map.md` for the complete
cross-preset role matrix.

---

## Agent Modes

| Agent             | Mode     | Description                                       |
| ----------------- | -------- | ------------------------------------------------- |
| **Orchestrator**  | primary  | Main coordinator — Tab to switch to it            |
| **Architect**     | subagent | Invoked by Orchestrator for design work           |
| **Implementer**   | subagent | Invoked by Orchestrator for code implementation   |
| **Tester**        | subagent | Invoked by Orchestrator for test generation       |
| **Reviewer**      | subagent | Invoked by Orchestrator for code review           |
| **Task Coach**    | subagent | Invoked by Orchestrator for intake clarification  |
| **Docs**          | subagent | Invoked by Orchestrator for documentation updates |
| **Repo Explorer** | subagent | Invoked by Orchestrator for codebase exploration  |

---

## Permission System

This preset uses OpenCode's permission system (v1.1.1+) with granular control:

Keep the V1-compatible `permission` dictionary for the `opencode` CLI. V2's
`permissions` rule list is exclusive to `opencode2` and is rejected by OpenCode
V1. Validate generated files with the actual CLI version before migrating formats.

- **Global defaults**: Most operations require approval (`ask`)
- **Read access**: Allowed by default, with sensitive files denied
- **Write/Edit**: Requires approval, with protected paths denied
- **Bash commands**: Read-only git commands allowed, destructive commands denied
- **Agent-specific**: Each agent has tailored permissions matching its role

### Protected Paths

The following paths are denied by default:

- `.env`, `.env.*` — environment secrets
- `secrets/**` — secret files
- `~/.ssh/**`, `~/.aws/**`, `~/.kube/**` — system credentials
- `.git/**`, `.opencode/**`, `.claude/**` — tool configuration

---

## Model Selection by Task Complexity

### Simple Tasks (Q&A, intake, documentation)

**Preset choice:** `glm-5.3-flash` for intake/exploration;
`muse-spark-1.3-contributor` for non-confidential docs.

- Task Coach intake
- Repo Explorer mapping
- Docs updates

### Medium Tasks (Implementation, testing)

**Preset choice:** `mimo-v2.6-flash` for implementation and testing;
`deepseek-v4.1-flash` for reviews.

- Implementer code writing
- Tester test generation
- Reviewer standard reviews

### Complex Tasks (Architecture, security, multi-agent coordination)

**Preset choice:** `deepseek-v4.1-flash` for design/coordination;
`glm-5.3-flash` for defensive security review.

- Architect technical design
- Orchestrator routing decisions
- Reviewer security analysis

---

## Usage in Agent Contracts

Agent templates contain placeholders. Installation resolves the role model
into YAML frontmatter:

```yaml
---
description: ...
model: opencode-go/deepseek-v4.1-flash
mode: subagent
---
```

To override, edit the `model` field in the agent's YAML frontmatter or use the
configuration in `opencode.jsonc`.

---

## Configuration Priority

1. **Agent frontmatter** — highest priority (per-agent override)
2. **opencode.jsonc model override** — applies to specific agents
3. **opencode.jsonc default** — fallback for all agents

---

## Environment Variables

Connect to **OpenCode Go** through `/connect` and use the Go API key. The
`opencode-go/` provider does not use individual DeepSeek, GLM, MiMo, or Meta keys.
Do not store API keys in the preset. Run `opencode models opencode-go` to check
availability before using or overriding a role model.

---

## Selecting Between Claude and OpenCode Go

| Scenario                        | Recommended                                        |
| ------------------------------- | -------------------------------------------------- |
| Complex reasoning, architecture | OpenCode Go (`deepseek-v4.1-flash`) or Claude (`opus`) |
| Fast iteration, simple tasks    | OpenCode Go (`glm-5.3-flash`) or Claude (`haiku`)   |
| Code implementation             | OpenCode Go (`mimo-v2.6-flash`) or Claude (`sonnet`) |
| Non-confidential documentation   | OpenCode Go (`muse-spark-1.3-contributor`) |
| Availability issues             | Switch to alternative from the matrix              |

## Approach

- Think before acting. Read existing files before writing code.
- Be concise in output but thorough in reasoning.
- Prefer editing over rewriting whole files.
- Do not re-read files you have already read unless the file may have changed.
- Skip files over 100KB unless explicitly required.
- Suggest running /cost when a session is running long to monitor cache ratio.
- Recommend starting a new session when switching to an unrelated task.
- Test your code before declaring done.
- No sycophantic openers or closing fluff.
- Keep solutions simple and direct.
- User instructions always override this file.
- When using tools, be precise and minimal with context.
- Prose/docs/code comments: be terse and direct. Prefer concrete nouns over abstract ones. Omit filler phrases ("note that", "please", "as mentioned"). One idea per sentence.

### YAGNI (You Aren't Gonna Need It)

Do not build features, abstractions, or "flexibility" that is not explicitly
requested. If the user asks for a function, write a function — not a class
hierarchy. If they ask for a string, return a string — not a Result type
with 15 error codes. Every line you write must solve a problem that exists
**now**.

### Stdlib-First

Prefer the language's standard library over third-party packages. Before
adding a dependency, ask: "Does `node:fs`, `node:path`, `node:crypto`, or
a built-in module solve this?" If yes, use it. Every external dependency
introduces maintenance burden, supply-chain risk, and version conflicts.

## Context Budget

- If the task type differs from the previous one, execute "/clear" before
  starting.
- Delegate verbose operations to sub-agents.
