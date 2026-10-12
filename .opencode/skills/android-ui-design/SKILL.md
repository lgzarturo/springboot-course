---
name: android-ui-design
description: >-
  Design, implement, review, and audit Android Jetpack Compose UI/UX for phone
  interfaces. Use when a request concerns concrete Compose layout, components,
  interaction, accessibility, or visual behavior. Do not activate for framework
  or dependency presence alone; non-UI Android work is excluded.
---

# Android UI design

Apply this specialized guidance to Android phone interfaces built with Jetpack
Compose. Keep the active `cc-feature`, `cc-fix`, `cc-review`, or `cc-openspec`
workflow and its gates; this skill contributes UI/UX criteria, not a separate
workflow. Use the general `android` skill for architecture, dependency injection,
Media3, performance, testing, and other non-UI Android engineering.

## Intake and boundaries

Identify the user goal, phone surface, affected interaction, supported Android
versions, and observable outcomes. Inspect the existing Compose components,
design tokens, navigation conventions, installed versions, tests, previews, and
available emulator or device tooling before proposing changes.

Preserve the established product visual system, including its components, color,
type, shape, spacing, and motion tokens. Material 3 is advisory guidance, not a
mandate to replace a deliberate product language. Suggest Material 3 changes with
their accessibility or usability benefit and wait for scope approval when they
would alter the visual direction.

The initial scope is phones. Do not invent tablet, foldable, desktop, or wearable
layouts. Still account for phone portrait and landscape orientation, window size
changes, display cutouts, system bars, and the soft keyboard or IME.

## Design mode

Turn the request into a small interaction contract before implementation:

- Define hierarchy, content, spacing, phone layout, scroll and overflow behavior,
  navigation, focus entry and return, gestures, and predictive back behavior.
- Specify applicable default, pressed, focused, disabled, pending or loading,
  empty, success, error or failure, and offline states. State what changes and
  what the user can do next; never imply success before it is known.
- Name the state owner. Prefer state hoisting, a single source of truth, and
  unidirectional data flow (UDF); keep transient visual state local only when no
  external owner needs to observe or control it.
- Design edge-to-edge intentionally with WindowInsets and system bars. Define IME
  resize, pan, focus, dismissal, and restoration behavior for editable surfaces.
- Require at least 48dp touch targets, meaningful semantics, TalkBack labels and
  actions, logical traversal or focus order, sufficient contrast, and usable font
  scaling without clipped or hidden actions.
- Add motion only when it has a purpose such as continuity, spatial orientation,
  feedback, or state change. Define start/end state, interruption, cancellation
  or reversal, repeated or rapid input, and reduced-motion or reduced-animation
  behavior that preserves essential information.

Use existing components and platform/Compose primitives before adding code or a
dependency. When alternatives are requested, compare only scoped options and make
the product-system and Material 3 tradeoffs explicit.

## Implementation mode

Implement the accepted contract without expanding the visual language. Keep
composables focused, pass immutable UI state and event callbacks, and preserve
unidirectional data flow. Hoist state to the lowest common owner that needs to
read or write it. Do not duplicate navigation, loading, or failure truth across
the composition.

Prefer semantic Material/Compose controls over raw pointer input. When a custom
gesture is necessary, preserve accessible actions, minimum touch targets, visual
feedback, cancellation, and interoperability with scrolling. Consume insets once
at the correct boundary and verify that edge-to-edge content and IME transitions
do not obscure controls.

Write tests for observable behavior and state outcomes rather than modifier or
implementation details. Cover the relevant state transition, duplicate or rapid
input, disabled behavior, semantics, navigation/back behavior, and state
restoration. Use previews as design aids, not as proof of runtime interaction.

## Review mode

Review the agreed surface against the interaction contract and existing Review
Report rules. Each finding needs a location, reproduction or evidence, user
impact, and proposed resolution. Treat contract and accessibility failures as
defects; classify unsupported aesthetic preferences as suggestions. Do not create
a new review axis or elevate a Material 3 preference over the product system.

Check state completeness, TalkBack semantics and traversal, 48dp touch targets,
contrast and font scaling, focus/IME behavior, edge-to-edge insets, gestures,
predictive back, orientation changes, state ownership/UDF, motion interruption,
reduced animation, and rapid repeated interaction as applicable.

## Audit mode

A requested audit is read-only. Inspect only the agreed Compose phone surface and
report prioritized, evidence-backed findings; an audit is not permission to edit,
implement, modify, or automatically fix the UI. Keep out-of-scope improvements as
suggestions and create backlog work only when requested through the existing
workflow.

Distinguish code inspection, semantics tests, screenshots, and live emulator or
device interaction because they prove different things. If required visual,
emulator, or device evidence is unavailable, mark that validation pending with
the surface, steps, expected result, and limitation. Do not claim visual
validation or a completed acceptance criterion without executing the check.

## Evidence and delivery

Report the observable outcomes checked, commands or tools used, results, and
limitations. A screenshot cannot prove TalkBack traversal, gesture interruption,
IME recovery, predictive back, or repeated-input behavior. Prefer a real phone or
phone emulator for those checks; otherwise leave precise pending verification.

Primary Android references: [Compose semantics](https://developer.android.com/develop/ui/compose/accessibility/semantics),
[accessibility defaults](https://developer.android.com/develop/ui/compose/accessibility/api-defaults),
[state and hoisting](https://developer.android.com/develop/ui/compose/state),
[window insets](https://developer.android.com/develop/ui/compose/system/insets),
[predictive back](https://developer.android.com/develop/ui/compose/system/predictive-back),
and [Compose accessibility testing](https://developer.android.com/develop/ui/compose/accessibility/testing).
