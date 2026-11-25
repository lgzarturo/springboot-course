---
id: database-migrations
name: database-migrations
description: >
  Safe schema and data migrations: ordering, immutability, locks, backfills,
  persisted enums and rollback.
  Trigger: When creating or editing files under migrations/, db/migrate or any .sql file.
---

# Database migrations

## Activation

Apply to any file under `migrations/` or `db/migrate/` and to `.sql` files,
in any language or ORM. Migration paths are high risk; do not lower it.

## Inputs

- The migration tool and its ordering/checksum rules.
- Target engine and version; table size and traffic when known.
- Code that reads or writes the changed columns.

## Procedure

1. **Immutability.** Condition: a migration was already applied anywhere
   shared. Action: add a new migration; never edit the old one. Exception:
   unmerged local migration. Verify: `git diff` shows only new files in
   `migrations/`.
2. **Unique on existing data** (`migration.unique-existing-data`). Condition:
   adding a unique index or constraint. Action: first query for duplicates,
   deduplicate or fail with a clear message, then add the constraint. Exception:
   table is provably empty in every environment. Verify: a test migrating a
   fixture with duplicates.
3. **Persisted enums** (`data.persisted-enum`). Condition: an enum is stored.
   Action: store a stable code, not an ordinal or a name that may be
   renamed; additions are additive, removals need a data migration. Exception:
   none for stored values. Verify: a test reading a row written by the previous
   version.
4. **Locks and backfills.** Condition: large table. Action: split into add
   nullable column, batched backfill, then constraint; use the engine's
   non-blocking index option where available. Exception: small tables. Verify:
   state the lock taken and the expected duration in the Evidence.
5. **Rollback.** Condition: every migration. Action: write the down step or
   state that the change is forward-only and why. Verify: apply, roll back
   (or restore) and re-apply on a scratch database.
6. **Code compatibility.** Condition: a column is dropped or renamed. Action:
   deploy code that stops using it first (expand/contract). Verify: grep shows
   no remaining reader.

## Decisions and exceptions

- Mixing schema change and large data change in one migration: split.
- Destructive statements (`DROP`, `TRUNCATE`) need explicit Task Card approval.

## Evidence

The command that applied the migration on a scratch database, its exit code,
and the rollback result. Without a database, report the migration as unrun.

## References

- https://martinfowler.com/articles/evodb.html
- https://www.postgresql.org/docs/current/sql-altertable.html
- https://docs.rs/sqlx/latest/sqlx/macro.migrate.html
