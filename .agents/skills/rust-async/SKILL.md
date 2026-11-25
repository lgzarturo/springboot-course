---
id: rust-async
name: rust-async
description: >
  Rust async with tokio or async-std: guards across await, detached tasks,
  blocking work, cancellation and shutdown.
  Trigger: When writing or reviewing async Rust in a package that declares tokio or async-std.
---

# Rust async

## Activation

Apply when the owning package declares `tokio` or `async-std` as a normal
dependency and the change touches `async fn`, `.await`, `spawn` or channels.

## Inputs

- Declared runtime, its features (`rt`, `rt-multi-thread`, `sync`, `time`).
- Existing shutdown path (`CancellationToken`, signal handler, `JoinSet`).

## Procedure

1. **Guards across await** (`rust.guard-across-await`). Condition: a
   `std::sync::MutexGuard`/`RwLock` guard or `RefCell` borrow is live at an
   `.await`. Action: scope the guard in a block that ends before the await, or
   copy out the data. Use `tokio::sync::Mutex` only when the lock must be held
   across the await. Exception: none for std guards (the future becomes `!Send`
   or can deadlock). Verify: `cargo check`; a `Send` bound compile error on
   `spawn` is the signal.
2. **Detached tasks** (`rust.detached-task`). Condition: `spawn` result is
   dropped. Action: dropping a `JoinHandle` detaches the task; keep the handle
   or use a `JoinSet`, and decide who observes panics and errors. Exception:
   fire-and-forget with logged failure and no shutdown dependency. Verify: each
   `spawn` has an owner that is awaited or aborted at shutdown.
3. **Blocking work.** Condition: CPU-heavy or blocking I/O inside async code.
   Action: `tokio::task::spawn_blocking`. Exception: work shorter than a
   scheduling quantum. Verify: no `std::thread::sleep` or blocking
   `std::fs`/`std::net` call in an `async fn`.
4. **Cancellation.** Condition: a future may be dropped at any `.await`. Action:
   keep state consistent at every await; use cancel-safe operations in
   `select!`. `abort()` does not stop a `spawn_blocking` task that already
   started. Exception: none. Verify: a test that drops the future mid-operation.
5. **Shutdown.** Condition: the service owns tasks. Action: signal, then await
   handles with a timeout. Started `spawn_blocking` tasks run to completion, so
   bound their runtime. Verify: a test or run that shuts down without hanging.

## Decisions and exceptions

- Do not change runtime flavor or add features without a Task Card line.
- `async-std` and `tokio` APIs differ; follow the declared runtime only.
- Unbounded channels need a stated reason; default to bounded.

## Evidence

`cargo test -p <package>` with the exit code, plus the named test that drops
or aborts the future. Report unverified cancellation paths as unverified.

## References

- https://docs.rs/tokio/latest/tokio/task/fn.spawn_blocking.html
- https://docs.rs/tokio/latest/tokio/task/struct.JoinHandle.html
- https://docs.rs/tokio/latest/tokio/sync/struct.Mutex.html
- https://tokio.rs/tokio/topics/shutdown
