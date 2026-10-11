---
id: auth-token-inspector
name: auth-token-inspector
description: >
  Audits token storage mechanisms to prevent XSS-based JWT theft.
user-invokable: true
license: MIT
metadata:
  author: lgzarturo
  category: security
compatibility:
  tools: [claude, codex, gemini, agy, opencode]
  stacks:
    languages: [typescript, javascript, python, kotlin, java, php]
paths:
  - "**/*.{ts,tsx,py,kt,php}"
---
# Auth Token Inspector

## Core Principles

1. **Secure Storage**: JWTs or session tokens must NOT be stored in `localStorage` or `sessionStorage` due to vulnerability to Cross-Site Scripting (XSS).
2. **HttpOnly Cookies**: Deliver tokens via `HttpOnly`, `Secure`, and `SameSite=Strict` (or `Lax`) cookies to protect them from client-side JS access.
3. **Short Expiration**: Ensure access tokens have short expiration times, and implement secure, rotation-enabled refresh tokens stored in cookies.

## Storage Comparison

| Storage Mode | Vulnerable to XSS | Vulnerable to CSRF | Recommended |
| :--- | :--- | :--- | :--- |
| **localStorage** | Yes (High Risk) | No | **NO** |
| **sessionStorage** | Yes (High Risk) | No | **NO** |
| **HttpOnly Cookie** | No (Secure) | Yes (Mitigated via SameSite/Tokens) | **YES** |
