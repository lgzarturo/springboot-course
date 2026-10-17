---
id: seo-analytics-injector
name: seo-analytics-injector
description: >
  Verifies and injects Google Tag Manager (GTM), GA4 event triggers, and optimized meta tags.
user-invokable: true
license: MIT
metadata:
  author: lgzarturo
  category: marketing
compatibility:
  tools: [claude, codex, gemini, agy, opencode]
  stacks:
    languages: [typescript, javascript, html]
    frameworks: [astro, nextjs]
---
# SEO Analytics Injector

## Core Principles

1. **Meta tags**: Layouts must contain standard SEO meta tags (`title`, `description`, `robots`, `og:title`, `og:description`, `og:image`, `twitter:card`).
2. **Analytics Integration**: Verify GTM container script is placed inside `<head>` (as high as possible) and the noscript iframe is placed immediately after the opening `<body>` tag.
3. **Lighthouse Compatibility**: Ensure images have `width` and `height`, and interactive components have accessible descriptions.

## GTM / GA4 Snippet Verification

GTM Script must match:
```html
<!-- Google Tag Manager -->
<script>(function(w,d,s,l,i){w[l]=w[l]||[];w[l].push({'gtm.start':
new Date().getTime(),event:'gtm.js'});var f=d.getElementsByTagName(s)[0],
j=d.createElement(s),dl=l!='dataLayer'?'&l='+l:'';j.async=true;j.src=
'https://www.googletagmanager.com/gtm.js?id='+i+dl;f.parentNode.insertBefore(j,f);
})(window,document,'script','dataLayer','GTM-XXXXXX');</script>
<!-- End Google Tag Manager -->
```
 GTM Noscript must match:
```html
<!-- Google Tag Manager (noscript) -->
<noscript><iframe src="https://www.googletagmanager.com/ns.html?id=GTM-XXXXXX"
height="0" width="0" style="display:none;visibility:hidden"></iframe></noscript>
<!-- End Google Tag Manager (noscript) -->
```
