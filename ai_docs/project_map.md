# Proje Haritası

## Kök Dizin
- `market-pos-app/`: Android uygulama (Kotlin/Compose)
- `web-application/`: Laravel web uygulaması (Admin + Web POS + API)
- `dokumanlar/`: Ana proje dokümantasyonları
- `tools/`: Yardımcı script/araç klasörleri

## Android (`market-pos-app`)
- `src/main/java/com/marketpos/core`: çekirdek yardımcı modüller (sync, premium, legal, device, cart)
- `src/main/java/com/marketpos/data`: db/network/repository katmanları
- `src/main/java/com/marketpos/domain`: model/repository/usecase sözleşmeleri
- `src/main/java/com/marketpos/feature`: ekran bazlı modüller (scan, sale, product, reports, settings, auth, activation, companion)
- `src/main/java/com/marketpos/navigation`: uygulama yönlendirme
- `src/main/java/com/marketpos/ui`: tema ve ortak UI bileşenleri

## Web (`web-application`)
- `app/Models`: iş alanı modelleri (firma, lisans, senkron, POS, ticket)
- `app/Services`: domain servisleri (sync, lisans çözümleme, POS oturum/sepet)
- `app/Http/Controllers`: web ve API controllerları
- `app/Filament`: admin panel sayfaları, kaynaklar, widgetlar
- `routes/web.php`: web/public + POS rotaları
- `routes/api.php`: mobil ve servis API rotaları
- `database/migrations`: veri modeli evrimi
- `public/app-updates/android`: APK update dağıtım dosyaları
- `update/`: patch dağıtım paketleri

## Dokümantasyon (`dokumanlar`)
- `uygulama-dokumanlari/`: mobil ve genel ürün planları/fazlar
- `web-dokumanlari/`: web veri modeli, API sözleşmesi, deploy notları
- `proje-kurallari.md`: birleşik proje kuralları
- `asistan-ve-codex-kuralları.md`: asistan çalışma disiplini
