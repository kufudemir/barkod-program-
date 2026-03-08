# Phase State

Source summaries:
- `ai_docs/phases.md`
- `ai_docs/backlog.md`
- `ai_docs/context/project_state.md`

## Current snapshot

- The repository contains Android and web runtimes
- The current `ai_docs` state describes the analyzed V4 phases from `0A` through `14` as completed in source documents
- Stabilization or closure work may still be handled later as a separate effort

## Operating rule

- Implement only the active phase or explicitly approved scope
- If a request is outside the active phase, classify it as backlog or ask for confirmation instead of silently building ahead
- Break large work into smaller phase slices when possible
- Keep one concrete sub-phase target per implementation turn

## Backlog handling

Use backlog treatment for:

- future-phase work
- unclear scope
- audits that should not become code changes yet
- stabilization follow-ups not currently approved

Backlog is not implementation.
