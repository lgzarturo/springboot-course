---
id: rust-sqlx
name: rust-sqlx
description: >
  sqlx in Rust: query macros and offline metadata, pool acquisition,
  transactions and migrations.
  Trigger: When touching sqlx queries, pools, transactions, .sqlx metadata or migrations in a Rust package.
---

# Rust sqlx

## Activation

Apply when the owning package declares `sqlx` (normal dependency) and the
change touches queries, pool/transaction code, `.sqlx/` or `migrations/`. For a
dev-only `sqlx`, apply to tests only.

## Inputs

- `sqlx` features (database driver, `migrate`, `offline`, runtime).
- `DATABASE_URL` source, `.sqlx/` directory, `migrations/` directory.

## Procedure

1. **Query macros.** Condition: `query!`/`query_as!` are used. Action: they check
   against `DATABASE_URL` or the `.sqlx` metadata at build time. Any query or
   schema change requires regenerating metadata. Exception: queries built with
   non-macro `query()` are not checked at build. Verify: `cargo sqlx prepare
   --check` (add `--workspace` in a workspace).
2. **Stale metadata** (`sqlx.metadata-stale`). Condition: queries or migrations
   changed. Action: run `cargo sqlx prepare` against a database migrated to the
   current schema and commit `.sqlx/`. Exception: repository builds only online.
   Verify: `cargo sqlx prepare --check` exits 0; CI builds with `DATABASE_URL`
   unset.
3. **Nested acquisition** (`sqlx.nested-acquisition`). Condition: code holds a
   pooled connection or transaction and calls something that acquires another.
   Action: pass the held `&mut` connection/transaction down; never acquire from
   the pool while holding one. Exception: a pool sized above the maximum nesting,
   documented. Verify: a test with `max_connections(1)` that does not time out
   (`acquire_timeout` bounds the wait).
4. **Transactions.** Condition: several statements must succeed together.
   Action: one transaction, explicit `commit()`; drop without commit rolls
   back. Exception: independent idempotent statements. Verify: a test forcing
   an error between statements and asserting no partial write.
5. **Migrations.** Apply `database-migrations` for content; here only: embed
   with `sqlx::migrate!` and run once at startup or deploy, not per request.

## Decisions and exceptions

- Do not edit an applied migration; add a new one (checksums are validated).
- Do not widen pool size to hide a nested-acquisition bug.
- Unknown boundary (is this a data-access layer?): ask or report it; do not guess.

## Evidence

Exit code of `cargo sqlx prepare --check` and of `cargo test -p <package>`
with a database available. Without a database, report macros as unverified.

## References

- https://docs.rs/sqlx/latest/sqlx/macro.query.html (offline mode)
- https://github.com/launchbadge/sqlx/blob/main/sqlx-cli/README.md
- https://docs.rs/sqlx/latest/sqlx/macro.migrate.html
- https://docs.rs/sqlx/latest/sqlx/pool/struct.PoolOptions.html
