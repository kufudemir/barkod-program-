# KolayKasa (MarketPOS)

KolayKasa; Android mobil POS, Laravel tabanli web yonetim paneli ve Web POS katmanini tek repoda birlestiren barkod/satis/raporlama platformudur.

## Projenin Amaci
- Kucuk ve orta olcekli isletmelerin barkod odakli satis operasyonunu hizlandirmak
- Mobilde offline-first calisma ile internet kesintilerinde satisin durmamasini saglamak
- Web tarafinda firma, cihaz, lisans, katalog ve satis akisini merkezden yonetmek
- Mobil ve web taraflarini API ve senkron mekanizmasi ile tek veri modelinde birlestirmek

## Kullanim Senaryolari
1. Mobil POS: Telefonla barkod okut, urun yonet, satis tamamla, rapor al.
2. Web POS: Tarayici uzerinden kasa ac, barkod/HID ile satis yap, fis yazdir.
3. Companion mod: Telefonu Web POS icin barkod okuyucu olarak kullan.
4. Back Office: Admin panelinden firma/cihaz/lisans/senkron/ticket yonetimi yap.

## Hemen Basla
1. Gereksinimler:
   - Android Studio (JDK 17), Android SDK 35
   - PHP 8.3+, Composer 2.x
   - Node.js 20+, npm
   - MySQL 8+
2. Android build kontrolu:
   - `./gradlew :app:assembleDebug` (Linux/macOS)
   - `.\gradlew.bat :app:assembleDebug` (Windows)
3. Web uygulamasini ayaga kaldir:
   - `cd web-application`
   - `composer install`
   - `cp .env.example .env` (Linux/macOS) veya `Copy-Item .env.example .env` (PowerShell)
   - `php artisan key:generate`
   - `php artisan migrate`
   - `npm install`
   - `npm run build`
   - `php artisan serve`

Detayli kurulum:
- [`dokumanlar/gelistirici-dokumanlari/kurulum-rehberi.md`](dokumanlar/gelistirici-dokumanlari/kurulum-rehberi.md)

## Mimari
Sistem 4 ana katmandan olusur:
1. Android Mobil POS
2. Web Back Office (Laravel + Filament)
3. Web POS (`/pos`)
4. API ve senkron katmani (`/api/v1/...`)

Detayli mimari dokumani:
- [`dokumanlar/gelistirici-dokumanlari/mimari.md`](dokumanlar/gelistirici-dokumanlari/mimari.md)

## API Dokumantasyonu
- Base URL: `https://barkod.space`
- Mobil auth: `/api/v1/auth/*`
- Aktivasyon ve cihaz: `/api/v1/device/*`
- Senkron: `/api/v1/sync/*`
- Companion satis: `/api/v1/mobile/web-sale/*`
- Destek: `/api/v1/support/*`

Detayli API referansi:
- [`dokumanlar/gelistirici-dokumanlari/api-dokumani.md`](dokumanlar/gelistirici-dokumanlari/api-dokumani.md)
- Tam API sozlesmesi: [`dokumanlar/web-dokumanlari/web-api-sozlesmesi.md`](dokumanlar/web-dokumanlari/web-api-sozlesmesi.md)

## Ozellikler
Modul bazli ozellik dokumani:
- [`dokumanlar/gelistirici-dokumanlari/ozellikler.md`](dokumanlar/gelistirici-dokumanlari/ozellikler.md)

Detayli urun ozellik listesi:
- [`dokumanlar/uygulama-dokumanlari/program özellikleri.md`](dokumanlar/uygulama-dokumanlari/program%20%C3%B6zellikleri.md)

## Roadmap
- V4 Faz 0-14 tamamlandi
- Acik isler stabilizasyon/release odakli backloga tasindi

Detayli yol haritasi:
- [`dokumanlar/gelistirici-dokumanlari/roadmap.md`](dokumanlar/gelistirici-dokumanlari/roadmap.md)
- Referans faz plani: [`dokumanlar/uygulama-dokumanlari/gelistirme-fazlari-v4-web-pos-parity.md`](dokumanlar/uygulama-dokumanlari/gelistirme-fazlari-v4-web-pos-parity.md)

## Dizin Yapisi
```text
.
|-- market-pos-app/      # Android uygulamasi
|-- web-application/     # Laravel web uygulamasi
|-- dokumanlar/          # Proje ve gelistirici dokumanlari
|-- tools/               # Yardimci script ve araclar
`-- ai_docs/             # AI destekli proje hafizasi
```

## Test Komutlari
- Android unit test: `./gradlew :app:testDebugUnitTest`
- Android instrumentation test: `./gradlew :app:connectedDebugAndroidTest`
- Web test: `cd web-application && php artisan test`

## Diger Dokumanlar
- Genel dokuman indeksi: [`dokumanlar/README.md`](dokumanlar/README.md)
- Proje kurallari: [`dokumanlar/proje-kurallari.md`](dokumanlar/proje-kurallari.md)
- Web veri modeli: [`dokumanlar/web-dokumanlari/web-veri-modeli.md`](dokumanlar/web-dokumanlari/web-veri-modeli.md)
- Teknik stack: [`ai_docs/tech_stack.md`](ai_docs/tech_stack.md)

## Surum Bilgisi
- Android uygulama surumu: `1.03.01`
- Android `versionCode`: `10301`
