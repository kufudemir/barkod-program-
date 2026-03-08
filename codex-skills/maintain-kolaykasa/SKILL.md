---
name: maintain-kolaykasa
description: Safely analyze, modify, and document the KolayKasa repository, which contains an Android POS app, a Laravel web/admin app, a web POS, and project governance docs. Use when working in this specific repo on bug fixes, feature changes, refactors, ai_docs synchronization, API or data-model updates, release patch preparation, or when project-specific rules matter such as active-phase discipline, Turkish UI text, kurus-based money handling, sync/outbox constraints, and minimal-change policy.
---

# Maintain KolayKasa

## Overview

Use this skill for tasks inside the KolayKasa repository. Read only the references needed for the current task, then apply the repo guardrails before changing code or docs.

## Quick Start

1. Read `references/operating-rules.md` first.
2. Load the task-specific references:
   - Use `references/repo-map.md` to find the right area quickly.
   - Use `references/architecture.md` for cross-layer or behavioral changes.
   - Use `references/product-scope.md` for feature intent and user-facing behavior.
   - Use `references/phase-state.md` for phase compliance, backlog handling, and current repo state.
   - Use `references/documentation-priority.md` when docs may conflict or must be updated.
   - Use `references/tech-stack.md` for framework and deployment context.
3. Confirm whether the request is analysis-only, implementation, docs sync, or release/patch work.
4. Change the smallest viable surface area. Preserve architecture and unrelated user work.
5. Update docs when behavior, API contracts, release artifacts, or phase state change.

## Workflow

1. Classify the request.
   - Treat tasks that only inspect or summarize as analysis-only.
   - Treat code, schema, API, UI, or business-rule changes as implementation.
   - Treat `ai_docs` or `dokumanlar` updates as docs sync.
   - Treat changes under `web-application/update` or Android update artifacts as release work.
2. Locate the affected runtime.
   - Android app: `market-pos-app`
   - Web/admin/POS/API: `web-application`
   - Governance and project docs: `dokumanlar`, `ai_docs`
3. Load the minimum references needed for the task.
4. Inspect the live code and source docs before editing.
5. Apply the smallest defensible change.
6. Validate with the most relevant checks available for the touched area.
7. Report the outcome using the repo's required closing structure.

## Guardrails

Apply these rules on every task:

- Preserve data integrity first.
- Respect active-phase discipline. Do not silently implement future-phase work.
- Prefer source documentation over assumptions.
- Do not perform unrelated refactors.
- Preserve existing user changes.
- Keep monetary logic in integer kurus.
- Preserve sync/outbox/dedup behavior unless the task explicitly changes it.
- Keep user-facing text Turkish and UTF-8 safe.
- Keep web deployment changes compatible with environments that may not have terminal access.
- Update API and release docs when behavior changes.

## Reference Loading Guide

- Read `references/operating-rules.md` first on every substantial task.
- Read `references/repo-map.md` before broad code search or cross-project edits.
- Read `references/architecture.md` before changing models, services, sync, auth, POS flows, or shared business logic.
- Read `references/product-scope.md` before changing user-visible features or deciding whether something already exists.
- Read `references/phase-state.md` before implementing requests that may be out of scope.
- Read `references/documentation-priority.md` when multiple docs may conflict, or when docs must be synced after changes.
- Read `references/tech-stack.md` when selecting commands, tools, libraries, or deployment steps.

## Task Patterns

### Implement a feature or bug fix

1. Read operating rules, repo map, architecture, and phase state.
2. Identify whether the change belongs to Android, web, API, or docs.
3. Verify relevant source documents before editing.
4. Change the minimum code path.
5. Run focused validation.
6. Update docs if behavior or contracts changed.

### Sync or rebuild `ai_docs`

1. Treat `dokumanlar/` and runtime code as the source material.
2. Keep `ai_docs` concise and operational.
3. Store stable guidance in summaries, not raw archives.
4. Avoid copying volatile conversation history into the core skill references.

### Prepare release or patch work

1. Check patch naming rules.
2. Keep patch contents limited to changed files.
3. Update patch README and index when needed.
4. If Android version changes, update APK distribution artifacts and metadata.

## Output Expectations

When finishing a substantial task, report:

1. `task summary`
2. `affected files`
3. `actions`
4. `phase compliance`
5. `risks`
6. `updated logs`
7. `result`

## Resources

Use the curated reference files in `references/`. They are distilled from `ai_docs` and intended to stay stable enough for repeated use. If the live repo documents evolve, refresh these references instead of bloating `SKILL.md`.
