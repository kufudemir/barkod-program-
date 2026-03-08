# Operating Rules

Source summaries:
- `ai_docs/rules.md`
- `ai_docs/conventions.md`

Apply these rules before making changes:

1. Protect against data loss first.
2. Respect active-phase discipline.
3. Prefer project source documents over memory or inference.
4. Preserve the current architecture unless the task explicitly requires a structural change.
5. Make minimal, local edits.
6. Keep docs synchronized with behavior changes.

## Mandatory constraints

- Do not implement future-phase work silently. Move it to backlog if needed.
- Do not perform unrelated refactors.
- Do not revert user changes unless explicitly asked.
- Keep money handling in integer `kurus`.
- Preserve activation, dedup, outbox, and retry behavior unless the task is specifically about those flows.
- Keep user-visible text Turkish and UTF-8 safe.
- Assume some server environments may lack terminal access; document panel-based alternatives when relevant.

## Release and patch rules

- Use patch names in `vX.Y.Z` or `vX.Y.Z-hotfixN` format.
- Keep patch packages limited to changed files.
- Maintain patch README files.
- Update patch indexes when required.
- If Android versioning changes, update APK distribution metadata such as `public/app-updates/android/latest.json`.

## Required closing report

For substantial tasks, close with:

1. `task summary`
2. `affected files`
3. `actions`
4. `phase compliance`
5. `risks`
6. `updated logs`
7. `result`
