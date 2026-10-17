---
id: astro
name: astro
description: >
  Provides expert knowledge for building Astro 5+ sites with Islands Architecture, Content Collections, TypeScript, and performance-first rendering strategies.

user-invokable: true
license: MIT
metadata:
  author: lgzarturo
  category: frontend

compatibility:
  tools: [claude, codex, gemini, agy, opencode]
  stacks:
    languages: []
    frameworks: []

risk:
  level: low
  can_execute_shell: false
  can_modify_files: true
  requires_network: false

inputs: []

outputs: []

quality:
  reviewed_by: codeconductor-core
  version: 0.1.0
paths:
  - "**/*.astro"
  - "**/astro.config.*"
---



# Astro

## Islands Architecture

Astro renders everything to static HTML by default. JavaScript ships only for
components that explicitly opt in — these are called Islands.

### Hydration Directives

| Directive | When JS loads | Use case |
|-----------|--------------|----------|
| `client:load` | On page load | Interactive above-the-fold UI |
| `client:idle` | When browser is idle | Non-critical interactive widgets |
| `client:visible` | When element enters viewport | Below-the-fold islands |
| `client:media` | When CSS media query matches | Responsive interactive components |
| `client:only` | Client-only, no SSR | Components that require the DOM (e.g., charting libs) |

```astro
---
import Counter from '../components/Counter.tsx';
import HeavyChart from '../components/HeavyChart.tsx';
import MobileNav from '../components/MobileNav.tsx';
---

<!-- Hydrates immediately — user interacts right away -->
<Counter client:load />

<!-- Hydrates when scrolled into view — saves initial JS -->
<HeavyChart client:visible />

<!-- Only on mobile, only when query matches -->
<MobileNav client:media="(max-width: 768px)" />
```

Rules:

- Default to no hydration directive — most UI does not need JavaScript
- `client:load` is the most expensive directive; use it sparingly
- `client:only` skips server rendering entirely — the component receives no
  props from the server; pass all data via props or fetch inside the component
- Do not use `client:load` for components that could use `client:visible`

### Framework Components Inside Astro

```astro
---
import ReactButton from './Button.tsx';   // React island
import VueWidget from './Widget.vue';     // Vue island
---

<!-- Both can coexist on the same page -->
<ReactButton client:idle label="Click me" />
<VueWidget client:visible />
```

Each framework ships its own runtime only when at least one island of that
framework is on the page.

## Content Collections

Content Collections provide type-safe access to Markdown, MDX, and data files.
Define schemas in `src/content/config.ts`.

### Schema Definition

```typescript
// src/content/config.ts
import { defineCollection, z } from 'astro:content';

const blog = defineCollection({
  type: 'content',   // .md or .mdx files
  schema: z.object({
    title: z.string(),
    description: z.string(),
    pubDate: z.coerce.date(),
    updatedDate: z.coerce.date().optional(),
    author: z.string().default('Anonymous'),
    tags: z.array(z.string()).default([]),
    draft: z.boolean().default(false),
    image: z.object({
      src: z.string(),
      alt: z.string(),
    }).optional(),
  }),
});

const docs = defineCollection({
  type: 'content',
  schema: z.object({
    title: z.string(),
    order: z.number(),
    section: z.enum(['guide', 'reference', 'tutorial']),
  }),
});

export const collections = { blog, docs };
```

### Querying Collections

```astro
---
import { getCollection, getEntry } from 'astro:content';

// All published posts, sorted by date
const posts = (await getCollection('blog', ({ data }) => !data.draft))
  .sort((a, b) => b.data.pubDate.valueOf() - a.data.pubDate.valueOf());

// Single entry by slug
const post = await getEntry('blog', 'my-first-post');
const { Content } = await post.render();
---

{posts.map(post => (
  <article>
    <h2><a href={`/blog/${post.slug}`}>{post.data.title}</a></h2>
    <time>{post.data.pubDate.toLocaleDateString()}</time>
  </article>
))}
```

### Dynamic Routes from Collections

```astro
---
// src/pages/blog/[slug].astro
import { getCollection } from 'astro:content';

export async function getStaticPaths() {
  const posts = await getCollection('blog');
  return posts.map(post => ({
    params: { slug: post.slug },
    props: { post },
  }));
}

const { post } = Astro.props;
const { Content, headings } = await post.render();
---

<article>
  <h1>{post.data.title}</h1>
  <Content />
</article>
```

## Rendering Strategies

### SSG (Static Site Generation) — default

Every page is pre-rendered at build time. Best for content that does not change
per request.

```javascript
// astro.config.mjs — no output config needed; SSG is the default
export default defineConfig({
  site: 'https://example.com',
});
```

### SSR (Server-Side Rendering)

Renders pages on each request. Required for: authenticated routes, personalized
content, live data.

```javascript
// astro.config.mjs
import node from '@astrojs/node';

export default defineConfig({
  output: 'server',
  adapter: node({ mode: 'standalone' }),
});
```

### Hybrid Mode

Mix SSG and SSR on a per-page basis. Most pages are static; specific routes opt
into server rendering.

```javascript
// astro.config.mjs
export default defineConfig({
  output: 'hybrid',
  adapter: node({ mode: 'standalone' }),
});
```

```astro
---
// src/pages/dashboard.astro — this page is server-rendered
export const prerender = false;

// src/pages/about.astro — this page is statically generated (hybrid default)
export const prerender = true;
---
```

Use hybrid mode when: most content is static but a few routes need auth or
live data. Do not make everything `output: 'server'` — you lose the performance
benefits of static generation.

## Image Optimization

Use the built-in `<Image>` and `<Picture>` components. Never use raw `<img>`
for local assets — you lose automatic optimization.

```astro
---
import { Image, Picture } from 'astro:assets';
import heroImage from '../assets/hero.png';
---

<!-- Optimized single image -->
<Image
  src={heroImage}
  alt="Hero illustration"
  width={800}
  height={600}
  format="webp"
  quality={80}
/>

<!-- Responsive with multiple formats -->
<Picture
  src={heroImage}
  formats={['avif', 'webp']}
  alt="Hero illustration"
  widths={[400, 800, 1200]}
  sizes="(max-width: 800px) 100vw, 800px"
/>
```

Rules:

- Always provide `alt` — empty string is acceptable only for decorative images
- Prefer `avif` + `webp` fallback for best compression
- Use `widths` + `sizes` on above-the-fold images to serve the right size per
  viewport
- Remote images require explicit `width` and `height` to prevent layout shift

## Project Structure

```text
src/
  assets/          — images and static assets processed by Astro
  components/      — .astro components (and framework islands)
  content/
    blog/          — .md and .mdx files
    config.ts      — collection schemas
  layouts/         — page shell layouts
  pages/           — file-based routing; every file is a route
  styles/          — global CSS
astro.config.mjs
tsconfig.json
```

Colocation rule: put framework island components (`.tsx`, `.vue`) in
`src/components/`. Do not scatter them in `src/pages/`.

## TypeScript Conventions

```json
// tsconfig.json — use the strict Astro preset
{
  "extends": "astro/tsconfigs/strict"
}
```

```astro
---
// Type props explicitly in the frontmatter
interface Props {
  title: string;
  description?: string;
  tags: string[];
}

const { title, description = '', tags } = Astro.props;
---
```

Astro infers prop types from `interface Props` automatically. Do not use `any`.
