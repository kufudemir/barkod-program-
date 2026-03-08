# Repo Map

Source summary:
- `ai_docs/project_map.md`

## Top level

- `market-pos-app/`: Android application
- `web-application/`: Laravel web application, admin panel, web POS, and API
- `dokumanlar/`: source project documentation
- `ai_docs/`: operational AI summaries and context memory
- `tools/`: helper scripts and utilities

## Android hotspots

- `market-pos-app/src/main/java/com/marketpos/core`: core helpers such as sync, premium, legal, device, cart
- `market-pos-app/src/main/java/com/marketpos/data`: db, network, and repository implementations
- `market-pos-app/src/main/java/com/marketpos/domain`: contracts, models, and use cases
- `market-pos-app/src/main/java/com/marketpos/feature`: screen- and feature-based modules
- `market-pos-app/src/main/java/com/marketpos/navigation`: app navigation
- `market-pos-app/src/main/java/com/marketpos/ui`: theme and shared UI

## Web hotspots

- `web-application/app/Models`: domain models for company, license, sync, POS, and tickets
- `web-application/app/Services`: business logic and orchestration
- `web-application/app/Http/Controllers`: web and API controllers
- `web-application/app/Filament`: admin resources, pages, widgets
- `web-application/routes/web.php`: public, admin, and POS routes
- `web-application/routes/api.php`: mobile and service APIs
- `web-application/database/migrations`: schema evolution
- `web-application/public/app-updates/android`: Android distribution metadata and artifacts
- `web-application/update/`: patch distribution packages

## Documentation hotspots

- `dokumanlar/uygulama-dokumanlari/`: product plans, mobile docs, phase docs
- `dokumanlar/web-dokumanlari/`: web data model, API contract, deploy notes
- `dokumanlar/proje-kurallari.md`: consolidated project rules
- `dokumanlar/asistan-ve-codex-kurallari.md`: assistant workflow constraints
