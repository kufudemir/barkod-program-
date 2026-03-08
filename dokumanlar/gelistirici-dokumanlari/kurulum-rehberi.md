# Kurulum Rehberi

Bu rehber, gelistirme ortamini sifirdan ayaga kaldirmak icin minimum adimlari verir.

## 1) Gereksinimler
- Android Studio + Android SDK 35 + JDK 17
- PHP 8.3+
- Composer 2.x
- Node.js 20+ ve npm
- MySQL 8+
- Git

## 2) Repoyu Hazirla
```bash
git clone <repo-url>
cd barkod-programi-github-ready-clean-20260308-163706
```

## 3) Android Uygulamasi
### Derleme
Linux/macOS:
```bash
./gradlew :app:assembleDebug
```
Windows PowerShell:
```powershell
.\gradlew.bat :app:assembleDebug
```

### Test
```bash
./gradlew :app:testDebugUnitTest
./gradlew :app:connectedDebugAndroidTest
```

## 4) Web Uygulamasi
```bash
cd web-application
composer install
```

Ortam dosyasini olustur:
Linux/macOS:
```bash
cp .env.example .env
```
Windows PowerShell:
```powershell
Copy-Item .env.example .env
```

Uygulama anahtari ve veritabani:
```bash
php artisan key:generate
php artisan migrate
```

Frontend bagimliliklari:
```bash
npm install
npm run build
```

Calistirma:
```bash
php artisan serve
```

## 5) Opsiyonel: Admin Kullanici Olusturma
```bash
php artisan marketpos:create-admin admin@example.com SuperSecret123 --name="Admin"
```

## 6) Hizli Saglik Kontrolu
- `GET /health` => `{"status":"ok",...}`
- `GET /` => landing sayfasi
- `GET /pos/login` => Web POS giris ekrani

## 7) Sik Karsilasilan Sorunlar
- Migration hatasi: `.env` icindeki DB ayarlarini kontrol et.
- Vite build hatasi: `node -v` ve `npm -v` surumlerini kontrol et.
- Android build hatasi: Android SDK 35 ve JDK 17 kurulu oldugundan emin ol.
