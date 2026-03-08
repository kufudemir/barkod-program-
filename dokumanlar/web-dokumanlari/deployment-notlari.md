# Deployment Notları

## Sunucu Varsayımları
- Domain: `barkod.space`
- PHP: `8.3`
- Laravel: `12`
- MySQL: `8.x`

## İlk Kurulum
1. `web-application` içeriğini sunucuya yükleyin.
2. Domain document root’unu `web-application/public` yapın.
3. `.env` dosyasını veritabanı ve SMTP bilgileriyle doldurun.
4. Geçici olarak `APP_SETUP_ENABLED=true` yapın.
5. `https://barkod.space/setup?secret=...` adresini açın.
6. Kurulum ekranından migration ve admin oluşturmayı çalıştırın.
7. İşlem bitince `APP_SETUP_ENABLED=false` yapın.

## Güncelleme
- Web değişikliğinde ilgili patch klasörünü sunucuya aynı yollarla kopyalayın.
- Migration veya cache temizleme gereken patch’lerde setup ekranını kullanın:
  - `https://barkod.space/setup?secret=...`
  - `İşlem Türü` alanından:
    - `Migration + Cache Temizle (Önerilen)` veya
    - `Sadece Migration` veya
    - `Sadece Cache Temizle`
    seçeneklerinden uygun olanı çalıştırın.
- Android yeni sürüm çıktığında:
  - `public/app-updates/android/latest.json`
  - ilgili APK dosyası
  güncel olmalıdır.
- Veri kullanım onayı ve yasal metin güncellemesi içeren sürümlerde:
  - açık web sayfaları (`/`, `/aydinlatma-metni`, `/veri-kullanimi`)
  - ilgili API uçları
  aynı patch altında yüklenmelidir.

## SMTP
Örnek ayarlar:
- `MAIL_MAILER=smtp`
- `MAIL_SCHEME=tls`
- `MAIL_HOST=fr-astral.guzelhosting.com`
- `MAIL_PORT=587`
- `MAIL_USERNAME=admin@barkod.space`
- `MAIL_PASSWORD=...`
- `MAIL_FROM_ADDRESS=admin@barkod.space`

## Lisans Talep ve Banka Bilgisi
Lisans talep formunda gösterilen banka bilgileri `config/license.php` üzerinden okunur.

Opsiyonel `.env` alanları:
- `LICENSE_BANK_ACCOUNT_NAME=...`
- `LICENSE_BANK_IBAN=...`
- `LICENSE_BANK_NAME=...`
- `LICENSE_BANK_DESCRIPTION_HINT=...`

## Sorun Giderme
- `/admin` açılmıyorsa web patch’in eksiksiz yüklendiğini kontrol edin.
- Şifre sıfırlama çalışmıyorsa `storage/logs/laravel.log` dosyasını inceleyin.
- Migration çalışmıyorsa `.env` içindeki `DB_*` alanlarını kontrol edin.
- Terminal erişimi yoksa bakım adımlarını `/setup` ekranından yürütün.
