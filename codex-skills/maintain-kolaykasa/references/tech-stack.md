# Tech Stack

Source summary:
- `ai_docs/tech_stack.md`

## Android

- Kotlin
- Jetpack Compose
- MVVM
- Hilt
- Room
- Coroutines and Flow
- Retrofit and OkHttp
- WorkManager
- CameraX
- ML Kit barcode scanning
- ML Kit text recognition

## Web

- PHP 8.2+
- Laravel 12
- Filament 5
- MySQL
- Blade and Livewire-driven admin and POS screens

## Integration

- REST APIs under `/api/v1/...`
- Activation and token-based device/company binding
- Mobile-to-web catalog and sales publishing flows

## Deployment

- Patch-based web deployment under `web-application/update/*`
- Android APK distribution metadata under `public/app-updates/android/latest.json`
- Hotfix naming model `vX.Y.Z-hotfixN`
