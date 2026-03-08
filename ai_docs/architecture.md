# Mimari Özeti

Kaynaklar:
- `dokumanlar/uygulama-dokumanlari/teknik-mimari.md`
- `dokumanlar/uygulama-dokumanlari/room-entity-plani.md`
- `dokumanlar/web-dokumanlari/web-veri-modeli.md`
- `dokumanlar/web-dokumanlari/web-api-sozlesmesi.md`
- `dokumanlar/uygulama-dokumanlari/gelistirme-fazlari-v4-web-pos-parity.md`

## Ürün Katmanları
1. Mobil POS (Android)
2. Web Back Office (Admin / Filament)
3. Web POS (`/pos`)
4. Senkron/API katmanı (`/api/v1/...`)

## Android Mimari
- Katman: `UI -> ViewModel -> UseCase -> Repository -> Data`
- Feature bazlı paketleme (`feature/*`)
- Offline-first Room merkezli çalışma
- Ağ bağlantısı olduğunda kuyruk/senkron akışı

## Web Mimari
- Laravel 12 + Filament admin
- Ayrı POS auth ve POS shell yapısı
- Servis katmanı üzerinden iş kuralları (lisans/senkron/POS)
- Eloquent modelleri ile firma, cihaz, ürün, satış, lisans, ticket yönetimi

## Kritik Kurallar
- Finans: kalıcı para modeli `kuruş` tabanlı
- Senkron: dedup + outbox + güvenli tekrar deneme
- Faz disiplini: aktif faz dışına implementasyon yapılmaz
- Doküman senkronu: API/özellik/faz değişiminde döküman güncellenir
