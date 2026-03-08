# Mimari

Bu dokuman KolayKasa sisteminin yuksek seviyeli mimarisini ozetler.

## 1) Bilesenler
1. Android Mobil POS (`market-pos-app`)
2. Web Back Office (`web-application` + Filament)
3. Web POS (`/pos`)
4. API katmani (`/api/v1/...`)
5. MySQL veri tabani

## 2) Yazi Diyagrami
```text
[Android Mobil POS]
   |  (REST API + token)
   v
[Laravel API Katmani] ----> [MySQL]
   ^
   |  (Blade/Filament + service katmani)
[Web POS ve Admin Panel]
```

## 3) Android Katmanlari
`UI -> ViewModel -> UseCase -> Repository -> Data Source`

- UI: Compose ekranlari (`feature/*`)
- ViewModel: ekran state ve event yonetimi
- UseCase: is kurallari
- Repository: domain sozlesmesi + veri kaynagi secimi
- Data Source: Room, Retrofit, WorkManager, ML Kit

Temel prensipler:
- Offline-first calisma
- Outbox kuyrugu ile senkron
- Finansal degerleri `Long` kurus olarak saklama

## 4) Web Katmanlari
`Route -> Controller -> Service -> Model -> DB`

- Route: `routes/web.php`, `routes/api.php`
- Controller: request/response ve validation
- Service: lisans, POS oturum, senkron ve is kurallari
- Model: Eloquent veri modeli
- DB: migration ile yonetilen tablo yapisi

## 5) Ana Veri Alanlari
- Firma/cihaz: `companies`, `devices`, `device_company_histories`
- Katalog/senkron: `global_products`, `company_product_offers`, `sync_batches`, `sync_event_dedups`
- Satis/POS: `pos_sessions`, `sale_sessions`, `web_sales`, `web_sale_items`, `web_sale_payments`
- Lisans: `license_packages`, `company_licenses`, `company_license_feature_overrides`, `license_requests`
- Ticket: `feedback_reports`, `feedback_messages`, `feedback_attachments`

## 6) Senkron Akisi (Ozet)
1. Mobilde urun degisikligi outbox'a yazilir.
2. Internet varsa batch olarak `POST /api/v1/sync/catalog-batch` ile gonderilir.
3. Web tarafi dedup (event UUID) ile idempotent isler.
4. Mobil taraf degisiklikleri `catalog/changes` ile artimli ceker.
5. Merge kurallari ile local katalog guncellenir.

## 7) Guvenlik ve Yetki
- Mobil API: bearer token (kullanici tokeni)
- Cihaz bazli senkron: aktivasyon tokeni
- Web POS: ayri login + middleware
- Rol modeli: owner / manager / cashier

## 8) Referans Dokumanlar
- `dokumanlar/uygulama-dokumanlari/teknik-mimari.md`
- `dokumanlar/web-dokumanlari/web-veri-modeli.md`
- `dokumanlar/web-dokumanlari/web-api-sozlesmesi.md`
- `ai_docs/architecture.md`
