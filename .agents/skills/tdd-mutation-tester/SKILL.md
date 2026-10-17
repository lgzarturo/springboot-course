---
id: tdd-mutation-tester
name: tdd-mutation-tester
description: >
  Performs simple mutation testing to verify test coverage and assertions.
user-invokable: true
license: MIT
metadata:
  author: lgzarturo
  category: testing
compatibility:
  tools: [claude, codex, gemini, agy, opencode]
  stacks:
    languages: [typescript, javascript, python, kotlin, java, php]
---
# TDD Mutation Tester

## Core Principles

1. **Verify Assertion Quality**: Coverage percentage can be misleading. Tests must fail when business logic changes.
2. **Mutation Process**:
   - Locate the core logic implemented by the `implementer`.
   - Temporarily mutate a logical operator (e.g. swap `>` for `<`, `==` for `!=`, `+` for `-`, or invert a boolean).
   - Run the test suite.
   - **Expected Result**: At least one test MUST fail.
   - **Clean Up**: Revert the mutated symbol immediately after running the test.
3. **Scorecard**: If the test suite passes even with mutated business logic, report a "0 in Real Coverage" on the scorecard and return the task for better test assertions.
