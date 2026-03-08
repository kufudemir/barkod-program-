# API Dokumani

Bu dokuman, en sik kullanilan API gruplarini hizli referans olarak ozetler.
Detayli ve tam liste icin `dokumanlar/web-dokumanlari/web-api-sozlesmesi.md` dosyasini kullan.

## Base URL
- `https://barkod.space`

## Kimlik Dogrulama
1. Mobil kullanici tokeni (login/register sonrasi):
   - Header: `Authorization: Bearer <accessToken>`
   - Kaynak: `POST /api/v1/auth/login` veya `POST /api/v1/auth/register`
2. Cihaz aktivasyon tokeni:
   - Header: `Authorization: Bearer <activationToken>`
   - Kaynak: `POST /api/v1/device/activate`
   - Kullanim: katalog batch senkronu (`POST /api/v1/sync/catalog-batch`)

## Endpoint Gruplari

### Auth ve Hesap
- `POST /api/v1/auth/register`
- `POST /api/v1/auth/login`
- `POST /api/v1/auth/logout`
- `GET /api/v1/auth/me`
- `POST /api/v1/auth/password`
- `POST /api/v1/auth/password/forgot`
- `POST /api/v1/auth/password/reset`
- `GET /api/v1/auth/premium`
- `POST /api/v1/auth/premium/sync`
- `POST /api/v1/auth/consent`

### Firma, Aktivasyon, Lisans
- `GET /api/v1/auth/companies`
- `GET /api/v1/device/history`
- `POST /api/v1/device/activate`
- `POST /api/v1/license/request`
- `GET /api/v1/company/license`
- `POST /api/v1/company/license/refresh`

### Katalog ve Senkron
- `GET /api/v1/catalog/products/{barcode}/suggestion`
- `GET /api/v1/auth/companies/{companyCode}/catalog`
- `GET /api/v1/auth/companies/{companyCode}/catalog/changes`
- `POST /api/v1/sync/catalog-batch`

### Mobil Web Companion Satis
- `GET /api/v1/mobile/web-sale/active`
- `POST /api/v1/mobile/web-sale/scan`
- `POST /api/v1/mobile/web-sale/item/increment`
- `POST /api/v1/mobile/web-sale/item/decrement`
- `POST /api/v1/mobile/web-sale/item/remove`
- `POST /api/v1/mobile/web-sale/item/custom-price`
- `POST /api/v1/mobile/web-sale/item/percent-discount`
- `POST /api/v1/mobile/web-sale/item/fixed-discount`
- `POST /api/v1/mobile/web-sale/item/reset-price`
- `POST /api/v1/mobile/web-sale/complete`
- `POST /api/v1/mobile/web-sale/print`
- `POST /api/v1/mobile/sales/publish`

### Destek / Ticket
- `GET /api/v1/support/inbox`
- `POST /api/v1/support/tickets`
- `GET /api/v1/support/tickets/{ticketId}`
- `POST /api/v1/support/tickets/{ticketId}/reply`
- `POST /api/v1/support/tickets/{ticketId}/reopen`
- `POST /api/v1/support/tickets/{ticketId}/attachments`

## Ornek Istekler

### Login
```http
POST /api/v1/auth/login
Content-Type: application/json

{
  "email": "demo@example.com",
  "password": "secret123",
  "deviceUid": "android-123",
  "deviceName": "Pixel 7"
}
```

### Mevcut Kullanici Bilgisi
```http
GET /api/v1/auth/me
Authorization: Bearer <accessToken>
```

### Cihaz Aktivasyonu
```http
POST /api/v1/device/activate
Content-Type: application/json

{
  "companyName": "Demo Market",
  "deviceUid": "android-123",
  "deviceName": "Pixel 7"
}
```

### Katalog Batch Senkronu
```http
POST /api/v1/sync/catalog-batch
Authorization: Bearer <activationToken>
Content-Type: application/json

{
  "batchUuid": "...",
  "events": []
}
```

## Hata Kodlari (Genel)
- `401`: token gecersiz veya yok
- `403`: kullanici/cihaz pasif veya yetkisiz
- `422`: validation hatasi

## Referanslar
- `web-application/routes/api.php`
- `dokumanlar/web-dokumanlari/web-api-sozlesmesi.md`
