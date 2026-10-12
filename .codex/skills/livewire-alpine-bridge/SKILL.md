---
id: livewire-alpine-bridge
name: livewire-alpine-bridge
description: >
  Schedules reactive frontend states cleanly using entangle directives in PHP and JS.
user-invokable: true
license: MIT
metadata:
  author: lgzarturo
  category: frontend
compatibility:
  tools: [claude, codex, gemini, agy, opencode]
  stacks:
    languages: [php, javascript]
    frameworks: [laravel, livewire, alpinejs]
---
# Livewire Alpine Bridge

## Core Principles

1. **State Entanglement**: Synchronize states between Livewire properties and Alpine.js states using the `@entangle` directive.
2. **Minimize Network Roundtrips**: Handle purely UI-related reactive interactions (modals, dropdowns, tab switches) strictly in Alpine.js without sending requests to the server.
3. **Lazy Syncing**: Use `.live` modifier selectively. Rely on `.defer` or deferred synchronization for inputs to prevent heavy server load.

## Implementation Pattern

```html
<div x-data="{ open: @entangle('showModal') }">
    <button @click="open = true">Open Modal</button>
    
    <div x-show="open" @click.away="open = false">
        Modal Content
    </div>
</div>
```
