---
name: web-design-engineering
description: >-
  Design, implement, and review web interface layout, component states, feedback,
  and motion within the current CodeConductor workflow. Use for concrete web UI
  work or requested UI audits, not backend-only tasks or native mobile apps.
---

# Web design engineering

Apply this guidance to the web surface in scope. A React or Tailwind dependency
alone is not a trigger. Keep the existing feature, fix, review, or OpenSpec flow;
this skill adds contextual criteria, not commands, agents, gates, or score weights.

## Intake and discovery

Translate vague requests into observable behavior: “responsive feedback” might
mean showing a pending state immediately after submit while preventing duplicate
submissions. Identify the user objective, affected surface, relevant states, and
acceptance checks in the Task Card. Ask only about ambiguities that affect scope.

Inspect existing components, design tokens, documented visual decisions, installed
dependencies and versions, and available browser, component-preview, and test tools.
Reuse the product's visual language and accessible components. Consult official
documentation for the installed version when an API is uncertain. Prefer existing
capabilities and browser primitives; a small transition does not justify a library.

## Design and implementation

Record relevant decisions in the Technical Plan or OpenSpec design.md:

- For static UI: hierarchy, readable content, spacing, responsive layout, overflow,
  semantics, contrast, and focus visibility. Do not invent motion to fill a checklist.
- For interactive components: default, pending, success, failure, disabled, and
  empty states as applicable; pointer, touch, keyboard, focus entry and return.
  Acknowledge input promptly without implying success before it is known.
- If motion serves the task, name its purpose and weigh frequency, waiting cost,
  and context. Specify start/end states, exit, rapid repeated input, cancellation
  or reversal, and reduced-motion behavior that preserves essential information.
  Use continuity or direct manipulation when it helps users understand the action.
- Reuse motion tokens. Choose properties and tools appropriate to the component;
  inspect layout/paint cost when relevant instead of assuming smoothness from code.
  No duration, easing curve, animation technique, or visual style is universally
  forbidden. Explain tradeoffs against the contract and product conventions.
- When alternatives are explicitly requested, compare scoped options and their
  tradeoffs before choosing. Do not build a variant selector or prototype framework.

Agree checks before implementation. Preserve the workflow's test-before-implement
order and use its evidence mechanism when required. Test observable state changes
and interaction outcomes, not just CSS strings. Implement only the accepted scope.
Tailwind skills own responsive conventions; framework skills own implementation;
PageSpeed owns performance measurement; evaluation owns the scorecard.

## Review and delivery

Use the existing Review Report and its severity/verdict rules. Map contract failures
to Spec Axis and technical issues to existing subchecks such as Correctness,
Architecture alignment, Performance, or Test coverage. Preserve Standards Axis;
do not add a design axis or rerank the two axes. Ground each finding in a location,
reproduction or evidence, user impact, and a proposed resolution. Aesthetic
preferences alone are suggestions, not blocking defects. Respect documented
product choices unless evidence demonstrates a contract or technical failure.

For a requested audit, inspect only the agreed surface and prioritize findings by
impact and evidence. Report out-of-scope opportunities as suggestions. Create
backlog items through the existing cc-backlog workflow only when requested; deliver
those items later through OpenSpec. An audit is not permission to implement fixes.

Report checks actually performed, their results, and limitations. Code inspection,
DOM tests, screenshots, and live interaction checks establish different evidence.
A screenshot cannot prove interruption or keyboard behavior. If a required visual
or interaction check cannot run, mark it pending with the surface, steps, expected
result, and missing tool/environment. Do not claim visual validation from code or
mark the unmet acceptance criterion complete.

## Example acceptance

For a dismissible web dialog using existing tokens: opening from the keyboard
places focus inside; Escape during entry closes it without reopening on a stale
completion callback and returns focus to its trigger. With reduced motion enabled,
the same actions work without the spatial transition. Check rapid open/close in
the browser and record the result; if unavailable, keep that check pending.
For a static pricing page, verify content hierarchy and no horizontal overflow at
the agreed viewports; no animation is required.

## Provenance and boundaries

Original CodeConductor adaptation informed by Emil Kowalski's
[design engineering](https://github.com/emilkowalski/skills/blob/main/skills/emil-design-eng/SKILL.md),
[animation construction](https://github.com/emilkowalski/skills/blob/main/skills/animate/SKILL.md),
and [interaction principles](https://github.com/emilkowalski/skills/blob/main/skills/apple-design/SKILL.md).
Additional references: [review](https://github.com/emilkowalski/skills/blob/main/skills/review-animations/SKILL.md),
[audits](https://github.com/emilkowalski/skills/blob/main/skills/improve-animations/SKILL.md),
[opportunities](https://github.com/emilkowalski/skills/blob/main/skills/find-animation-opportunities/SKILL.md),
[vocabulary](https://github.com/emilkowalski/skills/blob/main/skills/animation-vocabulary/SKILL.md),
[library selection](https://github.com/emilkowalski/skills/blob/main/skills/pick-ui-library/SKILL.md),
[component guidance](https://github.com/emilkowalski/skills/blob/main/skills/ask-sonner/SKILL.md),
and [alternatives](https://github.com/emilkowalski/skills/blob/main/skills/prototype/SKILL.md).
These are provenance, not runtime prerequisites. This skill does not install or
synchronize upstream skills, adopt promotional responses or mandatory author
formats, prescribe an Apple identity, or cover Expo/Swift native development.
