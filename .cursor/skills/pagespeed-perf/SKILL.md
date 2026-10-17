---
id: pagespeed-perf
name: pagespeed-perf
description: >
  Web Performance Engineering — analyzes Core Web Vitals using the PageSpeed
  Insights API (PSI v5) and applies the 80/20 principle: identify the 20% of
  changes that produce 80% of the performance gain. Produces a prioritized
  markdown report with quantified estimates and framework-specific code.
  Trigger: When auditing page speed, analyzing Core Web Vitals, or optimizing
  web performance for any URL.

user-invokable: true
license: MIT
metadata:
  author: codeconductor
  category: performance

compatibility:
  tools: [claude, codex, gemini, agy, opencode]
  stacks:
    languages: []
    frameworks: [astro, nextjs, react, vue, django, spring, wordpress]

risk:
  level: medium
  can_execute_shell: true
  can_modify_files: false
  requires_network: true

inputs:
  - name: url
    type: string
    required: true
    description: "Full URL to audit (must include scheme: https://...)"
  - name: strategy
    type: string
    required: false
    description: "mobile | desktop | both (default: both)"

outputs:
  - name: report
    type: markdown
    description: >
      Prioritized performance report saved as
      {YYYY-MM-DD}_pagespeed-{hostname}-claude.md in the current directory.

quality:
  reviewed_by: codeconductor-core
  version: 1.0.0
paths:
  - "**/*.{html,tsx,jsx,vue}"
---

# PageSpeed Performance Audit — Web Performance Engineering (80/20)

## Role and Purpose

Act as **Senior Web Performance Engineer, Frontend Architect, and Technical
Auditor**.

Always access the real site, measure real metrics via the PageSpeed Insights
API, analyze resources with the greatest impact, and produce a prioritized
Spanish-language report following the 80/20 principle: the **20% of critical
changes that produce 80% of the performance improvement**.

Never give generic recommendations. Every finding must be backed by data
observed from the specific URL being audited.

---

## Step 0 — Pre-flight: API Key and Output Filename

**MANDATORY. Execute before any analysis.**

### 1. Read the API key from the environment

```powershell
$env:PAGESPEED_API_KEY
```

- Value present → store as `{API_KEY}`, use in all PSI calls.
- Empty → proceed without key (CrUX field data unavailable; rate limits apply).

### 2. Define the output filename

```powershell
$date    = (Get-Date -Format "yyyy-MM-dd")
$website = ([System.Uri]"{URL}").Host -replace '[^a-zA-Z0-9]', '-'
$out     = "${date}_pagespeed-${website}-claude.md"
Write-Host $out
```

The final report **must** be written to this file.

---

## Data Collection

### Primary Method — Bun Scripts (always try first)

```powershell
bun --version   # if available, use Primary Method; otherwise use Fallback
$skillDir = "$env:USERPROFILE\.claude\skills\pagespeed-perf\scripts"
bun run "$skillDir\run.ts" --url={URL}
```

`run.ts` calls `psi-collect.ts` and `html-audit.ts` in parallel and returns
structured JSON. Key output fields:

```
output.summary.mobileScore          → Lighthouse mobile score (0-100)
output.summary.desktopScore         → Lighthouse desktop score
output.summary.passesCWV            → boolean — passes real CWV
output.summary.top5Actions          → array ordered by 80/20 score

output.psi.mobile.lab.lcp           → LCP (lab, mobile)
output.psi.mobile.lab.tbt           → TBT
output.psi.mobile.lab.cls           → CLS
output.psi.mobile.lab.fcp           → FCP
output.psi.mobile.lab.ttfb          → TTFB
output.psi.mobile.field             → CrUX data (null if no key / no data)
output.psi.mobile.opportunities     → array ordered by impact80_20 desc
output.psi.mobile.lcpElement        → HTML snippet of the LCP element
output.psi.mobile.thirdParties      → third-party scripts with blockingTime
output.psi.mobile.usedApiKey        → boolean — confirms key was used

output.html.stack                   → detected framework / CMS
output.html.resourceHints           → preloads, preconnects, prefetches
output.html.images                  → lazy, fetchpriority, list
output.html.scripts                 → blocking in <head>, third-party list
output.html.fonts                   → googleFonts, font-display, preloaded
output.html.issues                  → ordered by impact80_20
```

### Fallback Method — WebFetch (only if Bun unavailable)

```
# With API key (preferred)
https://www.googleapis.com/pagespeedonline/v5/runPagespeed?url={URL}&strategy=mobile&category=performance&key={API_KEY}
https://www.googleapis.com/pagespeedonline/v5/runPagespeed?url={URL}&strategy=desktop&category=performance&key={API_KEY}

# Without key (rate-limited)
https://www.googleapis.com/pagespeedonline/v5/runPagespeed?url={URL}&strategy=mobile&category=performance
```

Key PSI response paths:

```
lighthouseResult.categories.performance.score
lighthouseResult.audits.largest-contentful-paint
lighthouseResult.audits.total-blocking-time
lighthouseResult.audits.cumulative-layout-shift
lighthouseResult.audits.first-contentful-paint
lighthouseResult.audits.server-response-time
lighthouseResult.audits.largest-contentful-paint-element.details.items[0]
lighthouseResult.audits.render-blocking-resources.details.overallSavingsMs
lighthouseResult.audits.unused-javascript.details.overallSavingsBytes
lighthouseResult.audits.third-party-summary.details.items

loadingExperience.metrics.LARGEST_CONTENTFUL_PAINT_MS.percentile   (CrUX)
loadingExperience.metrics.INTERACTION_TO_NEXT_PAINT.percentile      (CrUX)
loadingExperience.metrics.CUMULATIVE_LAYOUT_SHIFT_SCORE.percentile  (CrUX)
loadingExperience.overall_category   → "FAST" | "AVERAGE" | "SLOW"
```

Also fetch the site HTML via WebFetch and inspect the `<head>` for resource
hints, lazy loading, `fetchpriority`, `font-display`, blocking scripts, and
images without explicit dimensions.

---

## Core Web Vitals — Thresholds

| Metric | Good     | Needs Improvement | Critical  | Score weight |
| ------ | -------- | ----------------- | --------- | ------------ |
| LCP    | ≤ 2.5 s  | 2.5 s – 4.0 s     | > 4.0 s   | 25%          |
| INP    | ≤ 200 ms | 200 ms – 500 ms   | > 500 ms  | 10%          |
| CLS    | ≤ 0.1    | 0.1 – 0.25        | > 0.25    | 15%          |
| FCP    | ≤ 1.8 s  | 1.8 s – 3.0 s     | > 3.0 s   | 10%          |
| TBT    | ≤ 200 ms | 200 ms – 600 ms   | > 600 ms  | 30%          |
| TTFB   | ≤ 800 ms | 800 ms – 1.8 s    | > 1.8 s   | —            |

LCP + TBT represent 55% of the Lighthouse Performance Score.

---

## 80/20 Optimization Matrix

Score = `Impact (1–5) × Ease (1–5)`. Prioritize by descending score.

| Rank | Optimization                      | Primary metric | Impact | Ease | Score |
| ---- | --------------------------------- | -------------- | ------ | ---- | ----- |
| 1    | Preload LCP element               | LCP            | 5      | 5    | **25** |
| 1    | `fetchpriority="high"` on LCP img | LCP            | 5      | 5    | **25** |
| 1    | Gzip / Brotli compression         | FCP, LCP, TBT  | 5      | 5    | **25** |
| 4    | WebP + explicit dimensions        | LCP, CLS       | 5      | 4    | **20** |
| 4    | Lazy loading offscreen images     | LCP, TBT       | 4      | 5    | **20** |
| 4    | `font-display: swap`              | FCP            | 4      | 5    | **20** |
| 4    | `preconnect` to critical origins  | FCP, LCP, TTFB | 4      | 5    | **20** |
| 4    | Defer / facade third-party scripts| TBT, INP       | 5      | 4    | **20** |
| 9    | Remove render-blocking resources  | FCP, LCP       | 5      | 3    | **15** |
| 9    | Long Cache-Control for assets     | repeat visits  | 3      | 5    | **15** |
| 11   | Remove unused JavaScript          | TBT, INP       | 4      | 3    | **12** |
| 12   | Remove unused CSS                 | FCP, TBT       | 3      | 3    | **9**  |
| 13   | CDN for static assets             | TTFB, LCP      | 4      | 2    | **8**  |
| 14   | Code splitting                    | TBT, INP       | 4      | 2    | **8**  |

**Critical 20% (Score ≥ 20)** — address these first:
1. Preload LCP element + `fetchpriority="high"`
2. Brotli/Gzip compression at the server
3. WebP images with explicit `width`/`height`
4. Lazy loading of offscreen images
5. `font-display: swap` for all fonts
6. `preconnect` to all critical origins
7. Defer or facade all third-party scripts

---

## Expected Impact by Optimization

| Optimization           | Metric   | Expected gain         |
| ---------------------- | -------- | --------------------- |
| Preload + fetchpriority| LCP      | −0.5 s to −2.0 s     |
| Brotli compression     | FCP, LCP | −0.3 s to −1.0 s     |
| WebP + dimensions      | LCP, CLS | −0.3 s to −1.2 s     |
| Lazy loading           | LCP      | −0.2 s to −0.8 s     |
| font-display: swap     | FCP      | −0.2 s to −0.8 s     |
| preconnect             | FCP, LCP | −0.1 s to −0.4 s each|
| Defer third parties    | TBT, INP | −50 ms to −300 ms    |

---

## Report Format (mandatory structure)

Save to `{YYYY-MM-DD}_pagespeed-{hostname}-claude.md`:

```markdown
# Auditoría de Rendimiento Web — {URL}
Fecha: {YYYY-MM-DD} | Estrategia: Móvil + Escritorio | Archivo: {filename}.md

## Resumen Ejecutivo
[2-3 párrafos: estado actual, score, bottleneck principal, potencial de mejora]

## Métricas Actuales
[Tabla: Lab (Lighthouse) vs Campo (CrUX)]

## Recursos con Mayor Impacto
[Tabla: URL, Tipo, Tamaño, Tiempo, Impacto, Razón]

## Plan de Acción Priorizado (80/20)
[Tabla ordenada por Score 80/20 descendente]

## Solución Técnica Detallada
[Por hallazgo: Problema, Evidencia, Implementación, Código, Impacto Esperado]

## Quick Wins (< 1 day)
## High Impact Changes
## Roadmap
  - Fase 1 — Inmediato (Semana 1)
  - Fase 2 — Corto Plazo (2–4 semanas)
  - Fase 3 — Mediano Plazo (1–3 meses)

---
*Informe generado el {YYYY-MM-DD} · Herramienta: Claude Code + pagespeed-perf skill*
*API Key usada: {Sí / No} · Datos CrUX: {Disponibles / No disponibles}*
```

---

## Quality Rules — Never Violate

1. **No data = no recommendation.** If PSI fails, try WebFetch directly.
2. **Always quantify.** "Reduces LCP from 4.2 s to ~3.0 s" — never "would improve LCP".
3. **Explicit evidence.** Cite the observed metric value for every finding.
4. **Site-specific code.** Adapt templates to the detected framework (Next.js,
   Astro, Django, etc.). Do not paste generic snippets unchanged.
5. **80/20 order.** Always sort the action plan by descending score.
6. **Separate field vs lab data.** CrUX = real user experience. Lighthouse = controlled lab.
7. **Identify the framework.** Detect Next.js, Astro, Vue, WordPress, etc., and
   provide framework-specific code samples.
