---
description: Create or resume an opt-in Delivery Ledger for substantial authorized work.
---

# /cc:odd

## Step 0 — CCEP Bootstrap

```bash
npx cc-codeconductor ccep profile odd --output json
npx cc-codeconductor ccep compile --command odd --phase <phase-id> "$ARGUMENTS" --view prompt --output json
```

Pass each subagent only the compiled `prompt` for its phase.

Create a ledger only after authorization and tracked coordination. Read-only and small work do not create state.
