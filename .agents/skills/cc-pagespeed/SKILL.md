---
name: cc-pagespeed
description: >-
  Run the PageSpeed performance audit — 80/20 analysis of Core Web Vitals using
  the PageSpeed Insights API.
---

# PageSpeed Performance Audit

Audit URL: $ARGUMENTS

---

## Step 1 — Pre-flight (pagespeed-perf skill)

Invoke `pagespeed-perf` skill.

Read `PAGESPEED_API_KEY` from the environment. Compute the output filename:
`{YYYY-MM-DD}_pagespeed-{hostname}-claude.md`.

Log whether the API key was found. Proceed regardless — lab data is available
without a key, but CrUX field data requires it.

---

## Step 2 — Collect

Call the PageSpeed Insights API for the URL provided above.

- Strategy: `both` (mobile + desktop) unless the user specified otherwise.
- Call PSI directly via WebFetch and fetch the HTML source for manual
  resource-hint analysis.

---

## Step 3 — Analyze

Extract from the PSI response:

- Core Web Vitals: LCP, INP, CLS, FCP, TBT, TTFB
- LCP element (type, selector, HTML snippet)
- Third-party scripts ordered by blocking time
- Render-blocking resources
- Unused JavaScript and CSS (savings in bytes and ms)

From the HTML source:

- Resource hints (`<link rel="preload|preconnect|prefetch">`)
- Images: lazy loading, `fetchpriority`, format, dimensions
- Fonts: `font-display`, preloaded subsets
- Blocking scripts in `<head>` (no `async`/`defer`)

---

## Step 4 — Prioritize

Score each identified optimization using the 80/20 matrix:
`Score = Impact × Ease` (each 1–5, max 25).

Order findings by descending score. Mark optimizations with Score ≥ 20 as
**Critical 20%**.

---

## Step 5 — Report

Write the full performance report to
`{YYYY-MM-DD}_pagespeed-{hostname}-claude.md` in the current directory.

Required sections:
1. Resumen Ejecutivo (2–3 paragraphs)
2. Métricas Actuales (lab vs field table)
3. Recursos con Mayor Impacto
4. Plan de Acción Priorizado (80/20 table)
5. Solución Técnica Detallada (per finding: problem, evidence, code, expected gain)
6. Quick Wins / High Impact Changes / Roadmap

End every report with:
```
---
*Informe generado el {YYYY-MM-DD} · Herramienta: Claude Code + pagespeed-perf skill*
*API Key usada: {Sí / No} · Datos CrUX: {Disponibles / No disponibles}*
```

---

## Requirements

**`PAGESPEED_API_KEY`** — Optional but strongly recommended.

- With key: real CrUX field data + 25,000 requests/day quota.
- Without key: lab data only (Lighthouse); shared rate limit applies.

Set in your shell:
```powershell
$env:PAGESPEED_API_KEY = "your-api-key"   # Windows PowerShell
export PAGESPEED_API_KEY="your-api-key"    # macOS / Linux
```

Get a free key: <https://developers.google.com/speed/docs/insights/v5/get-started>
