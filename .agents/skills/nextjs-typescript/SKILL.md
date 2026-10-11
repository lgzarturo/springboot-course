---
id: nextjs-typescript
name: nextjs-typescript
description: >
  Provides expert knowledge for building Next.js 15+ applications with the App Router, TypeScript, Server Components, Server Actions, and TanStack Query.

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
  level: medium
  can_execute_shell: false
  can_modify_files: true
  requires_network: false

inputs: []

outputs: []

quality:
  reviewed_by: codeconductor-core
  version: 0.1.0
---



# Next.js + TypeScript

## Server vs Client Components

The App Router defaults to Server Components. Every component is a Server
Component unless it explicitly opts in to the client.

### Decision Rule

```text
Does the component need any of the following?
  - useState / useReducer
  - useEffect / lifecycle methods
  - Browser APIs (window, document, localStorage)
  - Event listeners (onClick, onChange, onSubmit)
  - Third-party libraries that require the DOM

  YES → Client Component (`"use client"` directive)
  NO  → Server Component (default, no directive needed)
```

Keep the `"use client"` boundary as far down the component tree as possible.
Wrap only the interactive leaf node, not the entire page.

### Server Component (default)

```tsx
// app/users/page.tsx — no directive needed
import { db } from '@/lib/db';

export default async function UsersPage() {
  // Direct database access — no API round-trip needed
  const users = await db.user.findMany({ orderBy: { createdAt: 'desc' } });

  return (
    <ul>
      {users.map(user => (
        <li key={user.id}>{user.email}</li>
      ))}
    </ul>
  );
}
```

### Client Component

```tsx
// components/ui/counter.tsx
'use client';

import { useState } from 'react';

interface Props {
  initialCount?: number;
}

export function Counter({ initialCount = 0 }: Props) {
  const [count, setCount] = useState(initialCount);
  return <button onClick={() => setCount(c => c + 1)}>Count: {count}</button>;
}
```

### Composing Server and Client

```tsx
// app/dashboard/page.tsx — Server Component
import { Counter } from '@/components/ui/counter';  // Client
import { getUser } from '@/lib/queries';            // Server-only function

export default async function DashboardPage() {
  const user = await getUser();  // runs on the server

  return (
    <div>
      <h1>Welcome, {user.name}</h1>
      <Counter initialCount={user.loginCount} />  {/* client island */}
    </div>
  );
}
```

Passing server data to client components as props is correct. The server
renders the Server Component tree first, serializes the props, and sends them
to the client.

## Data Fetching

### Server Component Fetching (recommended for initial data)

```tsx
// app/posts/[id]/page.tsx
interface Props {
  params: Promise<{ id: string }>;
}

export default async function PostPage({ params }: Props) {
  const { id } = await params;
  const post = await fetch(`https://api.example.com/posts/${id}`, {
    next: { revalidate: 3600 },  // ISR: revalidate every hour
  }).then(r => r.json());

  if (!post) notFound();

  return <article>{post.title}</article>;
}
```

Cache strategies:

| `cache` option | Behavior |
|----------------|----------|
| `force-cache` | Cache indefinitely (default for fetch in RSC) |
| `no-store` | Never cache — fresh on every request |
| `next: { revalidate: N }` | ISR — revalidate after N seconds |
| `next: { tags: ['posts'] }` | On-demand revalidation via tag |

### Server Actions (mutations)

Server Actions run on the server. Use them for form submissions and mutations.
Never use them for reads.

```tsx
// app/posts/create/actions.ts
'use server';

import { revalidatePath } from 'next/cache';
import { redirect } from 'next/navigation';
import { z } from 'zod';
import { db } from '@/lib/db';

const CreatePostSchema = z.object({
  title: z.string().min(1).max(200),
  content: z.string().min(1),
});

export async function createPost(formData: FormData) {
  const parsed = CreatePostSchema.safeParse({
    title: formData.get('title'),
    content: formData.get('content'),
  });

  if (!parsed.success) {
    return { error: parsed.error.flatten().fieldErrors };
  }

  await db.post.create({ data: parsed.data });

  revalidatePath('/posts');
  redirect('/posts');
}
```

```tsx
// app/posts/create/page.tsx
import { createPost } from './actions';

export default function CreatePostPage() {
  return (
    <form action={createPost}>
      <input name="title" type="text" required />
      <textarea name="content" required />
      <button type="submit">Create</button>
    </form>
  );
}
```

Rules for Server Actions:

- Always validate input with Zod — do not trust `FormData` values
- Return error objects for validation failures; use `redirect()` for success
- Call `revalidatePath()` or `revalidateTag()` after mutations that affect
  cached data
- Never put secrets or auth logic in Client Components — keep it in actions

### TanStack Query (client-side data)

Use TanStack Query for data that must stay fresh on the client: real-time
feeds, user-specific data after mutations, optimistic updates.

```tsx
// components/posts/post-list.tsx
'use client';

import { useQuery } from '@tanstack/react-query';

interface Post {
  id: string;
  title: string;
}

export function PostList() {
  const { data, isLoading, error } = useQuery<Post[]>({
    queryKey: ['posts'],
    queryFn: () => fetch('/api/posts').then(r => r.json()),
    staleTime: 60_000,   // treat data as fresh for 60 seconds
  });

  if (isLoading) return <p>Loading...</p>;
  if (error) return <p>Failed to load posts.</p>;

  return (
    <ul>
      {data?.map(post => <li key={post.id}>{post.title}</li>)}
    </ul>
  );
}
```

Do not use TanStack Query for data that a Server Component can fetch directly.
The extra client-side fetch is unnecessary when the data can be fetched at
render time on the server.

## Route Handlers

Use route handlers for: webhooks, third-party OAuth callbacks, or endpoints
consumed by non-Next.js clients.

```typescript
// app/api/posts/route.ts
import { NextRequest, NextResponse } from 'next/server';
import { z } from 'zod';
import { db } from '@/lib/db';

const CreatePostBody = z.object({
  title: z.string().min(1),
  content: z.string().min(1),
});

export async function GET() {
  const posts = await db.post.findMany({ orderBy: { createdAt: 'desc' } });
  return NextResponse.json(posts);
}

export async function POST(request: NextRequest) {
  const body = await request.json();
  const parsed = CreatePostBody.safeParse(body);

  if (!parsed.success) {
    return NextResponse.json(
      { error: parsed.error.flatten() },
      { status: 400 }
    );
  }

  const post = await db.post.create({ data: parsed.data });
  return NextResponse.json(post, { status: 201 });
}
```

## Metadata API

```tsx
// app/posts/[id]/page.tsx
import type { Metadata } from 'next';

interface Props {
  params: Promise<{ id: string }>;
}

export async function generateMetadata({ params }: Props): Promise<Metadata> {
  const { id } = await params;
  const post = await fetch(`/api/posts/${id}`).then(r => r.json());

  return {
    title: post.title,
    description: post.excerpt,
    openGraph: {
      title: post.title,
      images: [{ url: post.coverImage }],
    },
  };
}
```

Never hardcode metadata in `<head>` tags — use the Metadata API. It handles
deduplication, inheritance, and streaming correctly.

## Streaming with Suspense

```tsx
// app/dashboard/page.tsx
import { Suspense } from 'react';
import { UserStats } from './user-stats';   // slow data fetch
import { RecentActivity } from './recent'; // fast data fetch

export default function DashboardPage() {
  return (
    <div>
      <RecentActivity />   {/* renders immediately */}
      <Suspense fallback={<p>Loading stats...</p>}>
        <UserStats />      {/* streams in when ready */}
      </Suspense>
    </div>
  );
}
```

Wrap independently slow data sources in their own `<Suspense>` boundaries.
Do not wrap the entire page — that defeats streaming.

## TypeScript Conventions

```typescript
// Use type imports for type-only imports
import type { User } from '@prisma/client';

// Prefer interfaces for object shapes that may be extended
interface UserCardProps {
  user: Pick<User, 'id' | 'name' | 'email'>;
  onSelect?: (id: string) => void;
}

// Use type aliases for unions and computed types
type Status = 'active' | 'inactive' | 'pending';
type UserWithPosts = User & { posts: Post[] };
```

Rules:

- Never use `any` — use `unknown` and narrow with type guards
- Use `satisfies` to validate objects against a type without widening
- Co-locate type definitions with the component or function that uses them;
  export only what other modules need
- Use `next/navigation` hooks (`useRouter`, `usePathname`) not `next/router` —
  the latter is Pages Router only

## Project Structure

```text
app/
  (auth)/                — route group, no URL segment
    login/page.tsx
    register/page.tsx
  (dashboard)/
    dashboard/page.tsx
    posts/
      [id]/page.tsx
      create/
        page.tsx
        actions.ts
  api/
    posts/route.ts
  layout.tsx             — root layout
  not-found.tsx

components/
  ui/                    — generic, reusable components
  posts/                 — domain-specific components

lib/
  db.ts                  — database client
  auth.ts                — auth configuration
  queries.ts             — reusable server-side query functions
```
