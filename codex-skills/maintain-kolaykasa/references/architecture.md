# Architecture

Source summaries:
- `ai_docs/architecture.md`
- `ai_docs/project_map.md`

## Product layers

1. Android mobile POS
2. Web back office and admin
3. Web POS at `/pos`
4. Sync and API layer under `/api/v1/...`

## Android shape

- Follow `UI -> ViewModel -> UseCase -> Repository -> Data`
- Keep feature-based packaging
- Preserve offline-first behavior around Room-backed local state
- Treat network sync as queued or retried work rather than assuming permanent connectivity

## Web shape

- Laravel 12 application with Filament admin
- Separate POS auth and POS shell behavior
- Keep business rules concentrated in services rather than spreading them across controllers
- Use Eloquent models for company, device, product, sale, license, and ticket domains

## Critical invariants

- Money is persisted and processed in integer `kurus`
- Sync behavior depends on deduplication, outbox patterns, and safe retry
- Phase discipline is part of the architecture process, not just planning
- Documentation changes are required when APIs or user-visible behavior change

## Change guidance

- Prefer local fixes within the existing layer boundaries
- Avoid moving logic between Android and web layers without strong reason
- Treat schema, service, and sync changes as high-risk and verify related docs
