---
id: tailwind-responsive-auditor
name: tailwind-responsive-auditor
description: >
  Audits Tailwind CSS usage ensuring mobile-first responsive utilities and clean classes.
user-invokable: true
license: MIT
metadata:
  author: lgzarturo
  category: frontend
compatibility:
  tools: [claude, codex, gemini, agy, opencode]
  stacks:
    languages: [typescript, javascript, html]
    frameworks: [astro, nextjs, react, tailwindcss]
---
# Tailwind Responsive Auditor

## Core Principles

1. **Mobile-First Design**: Always apply styles for mobile first (without breakpoints), then override them for larger screens (`sm:`, `md:`, `lg:`, `xl:`).
2. **Class Deduplication**: Avoid redundant classes (e.g. `w-full w-auto` or `text-red-500 text-blue-500`).
3. **No Arbitrary Values**: Avoid using arbitrary values like `w-[327px]` or `bg-[#f0f0f0]` unless absolutely necessary. Rely on the configured Tailwind theme.

## Audit Checklist

- Do not use desktop-first design (e.g., max-width utilities should be avoided unless specified).
- Ensure that elements have responsive margins, padding, and layout flex/grid direction where needed.
- Alert on duplicate or conflicting utility classes in component files.
