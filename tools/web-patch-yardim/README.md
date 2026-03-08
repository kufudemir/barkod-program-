# Web Patch Yardımı

Bu klasör, `web-application/update/` altındaki patch yapısını düzenli tutmak için kullanılır.

## Amaç
- yeni patch hazırlarken aynı klasör standardını korumak
- README ve manifest dosyalarını unutmayı engellemek

## Standart
1. Yeni sürüm:
   - `web-application/update/vX.Y.Z/`
2. Aynı sürümde hotfix:
   - `web-application/update/vX.Y.Z-hotfixN/`
3. Patch içinde mümkünse:
   - `README.md`
   - değişen dosyalar
   - gerekiyorsa `public/app-updates/android/`

## Şablon Kaynakları
- `web-application/update/_sablonlar/PATCH-README-SABLONU.md`
- `web-application/update/_sablonlar/patch-manifest.sablon.json`
