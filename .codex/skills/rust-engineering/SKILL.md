---
id: rust-engineering
name: rust-engineering
description: >
  Rust engineering baseline: Cargo workspaces and features, error types,
  unsafe, public API, lints and verification.
  Trigger: When editing .rs files, Cargo.toml or Cargo.lock in a Rust package.
---

# Rust engineering

## Activation

Apply to any `.rs`, `Cargo.toml` or `Cargo.lock` change. Rules are defaults;
a repository rule with the same id wins over this skill.

## Inputs

- Nearest `Cargo.toml` (package, `[features]`, `[lints]`) and workspace root.
- `rust-toolchain.toml`, `clippy.toml`, `rustfmt.toml` when present.
- The repository's existing error type and module layout.

## Procedure

1. **Locate the package.** Condition: a change spans crates. Action: resolve the
   owning package per file and run checks with `-p <package>`. Exception: a
   workspace-wide refactor named in the Task Card. Verify: `cargo metadata
   --no-deps --format-version 1` lists the package.
2. **Errors.** Condition: a function can fail. Action: return `Result` with the
   crate's existing error type; add context where the error crosses a module
   boundary. Exception: tests and examples may `unwrap`/`expect`. Verify:
   `cargo clippy -- -D clippy::unwrap_used` on touched non-test code, or a grep
   of the diff.
3. **Public API.** Condition: a `pub` item changes. Action: treat as a breaking
   change unless it only adds; update docs and CHANGELOG. Exception: crate is
   `publish = false` and private to the workspace. Verify: `cargo doc --no-deps`
   and, for published crates, `cargo semver-checks` if installed.
4. **unsafe.** Condition: `unsafe` is needed. Action: smallest block, a `// SAFETY:`
   comment stating the invariant, a safe wrapper. Exception: none for new
   `unsafe` without an owner approval in the Task Card. Verify: Miri when the
   toolchain has it, else a test of the invariant.
5. **Features** (`cargo.feature-unification`). Condition: a dependency or
   feature changes. Action: remember Cargo unifies features across the build;
   never rely on a feature being off. Features must be additive. Exception:
   mutually exclusive features guarded by `compile_error!`. Verify: `cargo check
   -p <package>` and `cargo check --workspace --all-targets`.
6. **Lockfile.** Condition: a dependency changes. Action: commit `Cargo.lock` for
   binaries; justify each new dependency. Exception: libraries that gitignore
   the lockfile. Verify: `cargo check --locked`.

## Decisions and exceptions

- Match the repository's formatting, naming and error style; do not import
  personal preferences. Edition and MSRV come from `Cargo.toml`, not this skill.
- Do not add `anyhow`/`thiserror` unless already used or approved.
- Dependency added "just in case": reject; prefer `std`.

## Evidence

Report each command run with its exit code, per package: `cargo fmt --check`,
`cargo clippy --all-targets -- -D warnings`, `cargo test -p <package>`. A
check that was not run is reported as not run, never as passed.

## References

- Cargo features: https://doc.rust-lang.org/cargo/reference/features.html
- Cargo workspaces: https://doc.rust-lang.org/cargo/reference/workspaces.html
- Rust API Guidelines: https://rust-lang.github.io/api-guidelines/
- Unsafe code guidelines: https://doc.rust-lang.org/nomicon/
