# KolayKasa (MarketPOS)

KolayKasa; Android tabanli mobil POS, Laravel tabanli web yonetim paneli ve Web POS katmanini tek repoda birlestiren barkod/satis/raporlama platformudur.

## Kisa Ozet
- Monorepo yapisi: Android + Web + dokumantasyon + yardimci araclar
- Offline-first mobil calisma, baglanti geldiginde kuyruk tabanli senkron
- Web tarafinda admin paneli, lisans yonetimi, cihaz/firma yonetimi ve Web POS
- Mobil ve web arasinda API tabanli aktivasyon, katalog ve satis publish akisleri

## Ana Moduller
- `market-pos-app/`: Android uygulamasi (Kotlin, Jetpack Compose, Hilt, Room, WorkManager, ML Kit)
- `web-application/`: barkod.space web uygulamasi (Laravel 12, Filament 5, Blade)
- `dokumanlar/`: urun/mimari/API/faz ve test dokumantasyonu
- `tools/`: lisans uretimi, web kurulum yardimcilari, arastirma ve patch yardim dosyalari
- `ai_docs/`: AI destekli proje haritasi, faz hafizasi ve kural ozetleri

## One Cikan Ozellikler
### Mobil POS (Android)
- Barkod okuma (EAN-13, EAN-8, UPC-A, UPC-E)
- OCR destekli urun adi onerisi ve manuel urun yonetimi
- Sepet, satis, indirim, bekleyen satis, stok ve rapor ekranlari
- Aktivasyon, hesap/oturum, premium/lisans ve destek talebi akislari
- Web companion modu ile web satis sepetine telefondan barkod okutma

### Web Uygulamasi (Laravel)
- Acik landing sayfalari (`/`, `/paketler`, `/apk`, `/lisans-talebi`)
- Filament admin panelinde firma/cihaz/lisans/katalog/senkron yonetimi
- Ayri kimlik yapisina sahip Web POS (`/pos/login`, `/pos`)
- Fis goruntuleme/yazdirma ve ticket/destek sistemi
- Mobil uygulama icin `/api/v1/...` REST endpointleri

## Teknoloji Yigini
- Android: Kotlin, Jetpack Compose, MVVM, Hilt, Room, Coroutines/Flow, Retrofit/OkHttp, WorkManager, CameraX, ML Kit
- Web: PHP 8.2+, Laravel 12, Filament 5, Blade, Vite, Tailwind, MySQL
- Genel: REST API, dedup + outbox senkron modeli, kurus bazli finansal veri modeli

## Dizin Yapisi
```text
.
|-- market-pos-app/
|   |-- src/main/java/com/marketpos/{core,data,domain,feature,navigation,ui}
|   `-- src/{test,androidTest}
|-- web-application/
|   |-- app/{Http,Models,Services,Filament}
|   |-- routes/{web.php,api.php}
|   |-- database/migrations
|   `-- resources/views
|-- dokumanlar/
|   |-- uygulama-dokumanlari/
|   `-- web-dokumanlari/
|-- tools/
|   |-- license-generator/
|   |-- web-kurulum/
|   `-- web-patch-yardim/
`-- ai_docs/
```

## Kurulum
### Gereksinimler
- Android Studio (JDK 17), Android SDK 35
- PHP 8.3, Composer 2.x
- Node.js 20+ ve npm
- MySQL 8+

### 1) Android uygulamasi
```bash
./gradlew :app:assembleDebug
```
Windows:
```powershell
.\gradlew.bat :app:assembleDebug
```

### 2) Web uygulamasi
```bash
cd web-application
composer install
cp .env.example .env
php artisan key:generate
php artisan migrate
npm install
npm run build
php artisan serve
```

## Calistirma ve Test
- Android unit test:
```bash
./gradlew :app:testDebugUnitTest
```
- Android instrumentation test (cihaz/emulator gerekli):
```bash
./gradlew :app:connectedDebugAndroidTest
```
- Laravel test:
```bash
cd web-application
php artisan test
```

## Onemli Notlar
- Repo, "GitHub-ready clean backup" mantigiyla duzenlenmistir.
- Bilincli olarak dislananlar:
  - `**/*.apk`, `**/*.aab`
  - `**/vendor/`
  - `web-application/.env`
  - `web-application/update/`
  - `web-application/public/app-updates/android/`
- Finansal alanlarda kalici para modeli `Long kurus` ilkesine gore tasarlanmistir.

## Dokumantasyon Giris Noktalari
- Genel dokuman indeksi: [`dokumanlar/README.md`](dokumanlar/README.md)
- Proje kurallari: [`dokumanlar/proje-kurallari.md`](dokumanlar/proje-kurallari.md)
- Uygulama ozellikleri: [`dokumanlar/uygulama-dokumanlari/program özellikleri.md`](dokumanlar/uygulama-dokumanlari/program%20%C3%B6zellikleri.md)
- Teknik mimari: [`dokumanlar/uygulama-dokumanlari/teknik-mimari.md`](dokumanlar/uygulama-dokumanlari/teknik-mimari.md)
- Web API sozlesmesi: [`dokumanlar/web-dokumanlari/web-api-sozlesmesi.md`](dokumanlar/web-dokumanlari/web-api-sozlesmesi.md)
- Web veri modeli: [`dokumanlar/web-dokumanlari/web-veri-modeli.md`](dokumanlar/web-dokumanlari/web-veri-modeli.md)
- AI proje haritasi: [`ai_docs/project_map.md`](ai_docs/project_map.md)

## Surum Bilgisi
- Android uygulama surumu: `1.03.01`
- Android `versionCode`: `10301`
