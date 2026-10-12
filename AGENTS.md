# CodeConductor — Agent Instructions

This project uses CodeConductor for structured AI-assisted engineering
workflows.

## Behavioral Discipline

These principles apply to **all agents** in every workflow. They reduce common
LLM coding mistakes and bias toward caution over speed.

1. **Think Before Coding** — State assumptions explicitly. If uncertain, ask. If
   multiple interpretations exist, present them — don't pick silently. If a
   simpler approach exists, say so.
2. **Simplicity First** — Minimum code that solves the problem. No features
   beyond what was asked. No abstractions for single-use code. No speculative
   "flexibility." Ask: "Would a senior engineer say this is overcomplicated?"
3. **Surgical Changes** — Touch only what you must. Don't "improve" adjacent
   code. Match existing style. Remove only what YOUR changes made unused. Every
   changed line must trace directly to the user's request.
4. **Goal-Driven Execution** — Transform tasks into verifiable goals with
   success criteria. For multi-step tasks, state a plan with verification
   checks. Loop until verified.

<!-- CODECONDUCTOR:BEGIN managed -->

# CodeConductor — Muse Preset

This file configures CodeConductor for **Muse** (Meta's coding-agent CLI,
binary `muse`). It lives at the project root (`AGENTS.md`) so Muse's
context-file loader picks it up automatically.

## Behavioral Discipline

1. **Think Before Coding** — state assumptions; ask if uncertain; present
   alternatives instead of picking silently.
2. **Simplicity First** — minimum code that solves the problem; no
   speculative abstractions.
3. **Surgical Changes** — touch only what the task requires; match existing
   style; remove only what your own change made unused.
4. **Goal-Driven Execution** — turn the task into a verifiable goal with a
   success check; loop until it passes.

## Commands

CodeConductor ships its workflows as Muse skills under `.agents/skills/`,
invoked as `/cc:<name>` (e.g. `/cc:feature`, `/cc:review`). Run `/cc:ask
"<problem>"` when unsure which one applies — it recommends exactly one and
stops; it does not start the workflow.

Skills live under `.agents/skills/` (Muse discovers skills there natively,
per its own docs). Read `.agents/skills/using-cc-skills/SKILL.md` first —
it maps intent to the right slash command.

Where a step says "adopt the `X` role", read the matching skill under
`.agents/skills/` and act as that role for the rest of the step.

## What never changes

- Do not invoke the Implementer without an accepted Technical Plan.
- Do not skip the Reviewer step for medium- or high-risk changes.
- Do not store secrets in any file loaded by Muse.

## Receipt integrity

- For any implementation, test, review, handoff, or delivery decision, capture or verify the current RDD receipt with `npx cc-codeconductor rdd`.
- A receipt is valid only for its exact candidate. If code, tests, contracts, or runner configuration changed, repeat the affected verification.
- TDD and Mutation Testing retain their existing gates; RDD verifies that their observed evidence still belongs to the current candidate.

<!-- CODECONDUCTOR:END managed -->

---

## Project-Specific Notes

This section is manually maintained. Add project-specific conventions,
exceptions, or context here.

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

## Context Budget

- If the task type differs from the previous one, execute "/clear" before
  starting.
- Delegate verbose operations to sub-agents.

## graphify

This project has a knowledge graph at graphify-out/ with god nodes, community structure, and cross-file relationships.

When the user types `/graphify`, invoke the `skill` tool with `skill: "graphify"` before doing anything else.

Rules:
- For codebase questions, first run `graphify query "<question>"` when graphify-out/graph.json exists. Use `graphify path "<A>" "<B>"` for relationships and `graphify explain "<concept>"` for focused concepts. These return a scoped subgraph, usually much smaller than GRAPH_REPORT.md or raw grep output.
- Dirty graphify-out/ files are expected after hooks or incremental updates; dirty graph files are not a reason to skip graphify. Only skip graphify if the task is about stale or incorrect graph output, or the user explicitly says not to use it.
- If graphify-out/wiki/index.md exists, use it for broad navigation instead of raw source browsing.
- Read graphify-out/GRAPH_REPORT.md only for broad architecture review or when query/path/explain do not surface enough context.
- After modifying code, run `graphify update .` to keep the graph current (AST-only, no API cost).
