# Web API Sözleşmesi

Bu doküman mevcut barkod.space API uçlarını ve V4 ile eklenecek rotaları özetler.

## Mevcut Kimlik ve Oturum API'leri

### POST /api/v1/auth/register
- kullanıcı kaydı açar
- access token döner

### POST /api/v1/auth/login
- kullanıcı giriş yapar
- access token döner

### POST /api/v1/auth/logout
- mevcut erişim tokenini kapatır

### GET /api/v1/auth/me
- geçerli kullanıcı bilgisini döner

### POST /api/v1/auth/consent
- veri kullanım onay sürümünü ve onay zamanını kaydeder

### POST /api/v1/auth/password
- giriş yapmış kullanıcı şifresini değiştirir

### POST /api/v1/auth/password/forgot
- şifre sıfırlama kodu üretir ve e-posta gönderir

### POST /api/v1/auth/password/reset
- kod ile yeni şifre belirler

## Mevcut Premium / Hesap API'leri

### GET /api/v1/auth/premium
- hesaba bağlı lisans/premium durumunu döner

### POST /api/v1/auth/premium/sync
- cihazdaki ücretli durum bilgisini hesaba senkronlar

## Mevcut Firma ve Aktivasyon API'leri

### GET /api/v1/auth/companies
- hesaba bağlı firmaları listeler
- `deviceUid` verilirse cihaz geçmişindeki firmaları da ekleyebilir

### GET /api/v1/device/history
- sadece cihaz geçmişindeki firmaları döner

### POST /api/v1/device/activate
- firma ünvanı veya firma kodu ile cihaz aktivasyonu yapar
- yanıt olarak firma bilgisi ve aktivasyon tokeni döner

### GET /api/v1/auth/companies/{companyCode}/catalog
- seçilen firmaya ait bulut kataloğunu döner

### GET /api/v1/auth/companies/{companyCode}/catalog/changes [✔ IMPLEMENTED]
- seçilen firmaya ait artımlı katalog değişikliklerini döner
- query:
  - `sinceUpdatedAt` (epoch ms, zorunlu degil)
  - `limit` (1..500, varsayilan 200)
- owner ve aktif personel rolleri (manager/cashier) için erişim desteklenir
- yanıt:
  - `changes[]` (`barcode`, `name`, `groupName`, `salePriceKurus`, `costPriceKurus`, `note`, `isActive`, `updatedAt`)
  - `hasMore`
  - `nextCursor`

### GET /api/v1/catalog/products/{barcode}/suggestion
- global katalogda doğrulanmış ürün adını ve ürün grubunu döner

## Mevcut Katalog Senkron API'si

### POST /api/v1/sync/catalog-batch
- Android uygulamanın kuyrukta tuttuğu ürün güncellemelerini toplu olarak işler
- aynı `eventUuid` tekrar işlenmez
- global ürün ve firma fiyatları güncellenir

## Mevcut Açık Web Sayfaları

### GET /
- KolayKasa landing page'i döner

### GET /aydinlatma-metni
- aydınlatma metni sayfasını döner

### GET /veri-kullanimi
- veri kullanımı ve veri toplama açıklamasını döner

## V4 İçin Eklenecek Public Web Rotaları

### GET /paketler
- paket karşılaştırma sayfası

### GET /apk
- son APK ve indirme açıklaması

### GET /kullanici-olustur
- kayıt olma yönlendirme yüzeyi

### GET /misafir-basla
- misafir kullanım açıklaması

### GET /lisans-talebi
- firma lisans talep formu

### POST /lisans-talebi
- açık web formundan lisans talebi kaydı oluşturur
- kayıt başlangıç durumu: `pending_payment`

### GET /pos/login [✔ IMPLEMENTED]
- web POS giriş ekranı (admin panelden bağımsız)

### POST /pos/login [✔ IMPLEMENTED]
- web POS oturumu açar

### POST /pos/logout [✔ IMPLEMENTED]
- web POS oturumunu kapatır

### GET /pos [✔ IMPLEMENTED]
- web POS ana kabuğu (Faz 2A shell)

### POST /pos/scan [✔ IMPLEMENTED]
- barkodu web POS sepetine ekler

### POST /pos/item/{barcode}/increment [✔ IMPLEMENTED]
- sepet satır adetini artırır

### POST /pos/item/{barcode}/decrement [✔ IMPLEMENTED]
- sepet satır adetini azaltır

### POST /pos/item/{barcode}/remove [✔ IMPLEMENTED]
- sepet satırını kaldırır

### POST /pos/cart/clear [✔ IMPLEMENTED]
- aktif şirket sepetini temizler

### POST /pos/checkout [✔ IMPLEMENTED]
- web POS satışını tamamlar

### POST /pos/switch-company [✔ IMPLEMENTED]
- aynı kullanıcıdaki aktif firmalar arasında POS şirketini değiştirir

### POST /pos/session/context [✔ IMPLEMENTED]
- seçili firma içinde şube ve kasa bağlamını değiştirir
- gerekirse yeni aktif POS session açar

### POST /pos/session/open [✔ IMPLEMENTED]
- seçili kasa için aktif POS session açar veya mevcut aktif session'ı kullanır

### POST /pos/session/close [✔ IMPLEMENTED]
- seçili kasa için aktif POS session'ı kapatır

### GET /pos/manage/overview [✔ IMPLEMENTED]
- Web POS `Cihazları Yönet` modalı için cihaz/oturum/personel özetini döner

### POST /pos/manage/devices/{device}/toggle [✔ IMPLEMENTED]
- firma cihazını aktif/pasif yapar (owner/manager yetkisi)

### POST /pos/manage/sessions/{posSession}/close [✔ IMPLEMENTED]
- firma içindeki aktif POS oturumunu kapatır (owner/manager yetkisi)

### POST /pos/manage/staff/upsert [✔ IMPLEMENTED]
- personel e-postasına rol atar veya günceller (owner yetkisi)

### POST /pos/manage/staff/{staffRole}/delete [✔ IMPLEMENTED]
- personel rol kaydını kaldırır (owner yetkisi)

### POST /pos/sale-session/create [✔ IMPLEMENTED]
- aktif POS oturumu için yeni satış sekmesi açar
- yeni sekme aktif sekme olarak seçilir

### POST /pos/sale-session/switch [✔ IMPLEMENTED]
- aktif POS oturumu içindeki satış sekmeleri arasında geçiş yapar

### POST /pos/sale-session/close [✔ IMPLEMENTED]
- aktif POS oturumundaki satış sekmesini kapatır
- son aktif sekme kapanırsa otomatik yeni `Web Manuel` sekmesi açılır

### POST /pos/company/profile [✔ IMPLEMENTED]
- web POS menu icinden firma bilgi alanlarini gunceller
- alanlar: firma unvani, yetkili, iletisim, vergi no, vergi dairesi

### GET /pos/search/products [✔ IMPLEMENTED]
- web POS barkod/girdi alanı için ürün öneri listesini döner
- barkod ve ürün adına göre şirket içi aktif fiyat kayıtlarında arama yapar

### GET /pos/receipts [✔ IMPLEMENTED]
- web POS satislari ve fisleri listeler
- bekleyen satislari geri acma/silme aksiyonlarini sunar

### GET /pos/receipts/{webSale} [✔ IMPLEMENTED]
- web POS fis detayini acar
- `paper=58mm|80mm|a4` ile fis onizleme/yazdirma gorunumu degisir
- `output=print|pdf` ve `autoprint=1` parametreleri desteklenir

### GET /pos/receipts/public/{webSale} [✔ IMPLEMENTED]
- signed URL ile kisa sureli public fis goruntuleme
- mobil companion yazdirma/PDF fallback akisi bu rota uzerinden calisir

## V4 Lisans API'leri [✔ IMPLEMENTED]

### POST /api/v1/license/request
- lisans talebi oluşturur
- istenen paket ve iletişim bilgilerini kaydeder
- `requestedPackageCode`: `SILVER` veya `GOLD` (`PRO` gelirse `SILVER` olarak normalize edilir)
- `requesterName` ve `requesterEmail` gönderilmezse, bearer token geçerliyse hesap bilgisinden doldurulur
- başlangıç durumu: `pending_payment`

### GET /api/v1/company/license
- kullanıcının aktif firma lisans özetini döner
- aktif lisans + son lisans + feature çözümlemesi birlikte döner

### POST /api/v1/company/license/refresh
- firma lisans bilgilerini cihaz veya web istemcisine yeniler
- `GET /api/v1/company/license` ile aynı özeti yeniden üretir

## V4 Web POS API'leri

### POST /api/v1/pos/session/open
- web POS oturumu açar

### POST /api/v1/pos/session/close
- web POS oturumunu kapatır

### GET /api/v1/pos/session/active
- aktif POS oturumunu döner

### POST /api/v1/pos/session/{id}/scanner/attach
- mobil companion cihazı aktif web POS oturumuna bağlar

### POST /api/v1/pos/session/{id}/scanner/detach
- mobil companion cihazı ayırır

### POST /api/v1/pos/session/{id}/scan
- barkod okuma olayını aktif satış sekmesine işler

### POST /api/v1/pos/session/{id}/items
- sepet satırı ekler

### PATCH /api/v1/pos/session/{id}/items/{itemId}
- sepet satırını günceller

### DELETE /api/v1/pos/session/{id}/items/{itemId}
- sepet satırını kaldırır

### POST /api/v1/pos/session/{id}/hold
- satış sekmesini beklemeye alır

### POST /api/v1/pos/session/{id}/resume
- bekleyen satış sekmesini tekrar açar

### POST /api/v1/pos/session/{id}/checkout
- satışı tamamlar

### POST /api/v1/pos/session/{id}/print
- satış fişi çıktısı oluşturur

## V4 Mobil Web Companion API'leri [✔ IMPLEMENTED]

### GET /api/v1/mobile/web-sale/active [✔ IMPLEMENTED]
- mobil uygulamanın bağlanacağı aktif web satış oturumunu döner

### POST /api/v1/mobile/web-sale/scan [✔ IMPLEMENTED]
- barkodu companion sepetine ekler

### POST /api/v1/mobile/web-sale/item/increment [✔ IMPLEMENTED]
- companion sepet satır adetini artırır

### POST /api/v1/mobile/web-sale/item/decrement [✔ IMPLEMENTED]
- companion sepet satır adetini azaltır

### POST /api/v1/mobile/web-sale/item/remove [✔ IMPLEMENTED]
- companion sepet satırını kaldırır

### POST /api/v1/mobile/web-sale/item/custom-price [✔ IMPLEMENTED]
- companion satırında özel fiyat uygular

### POST /api/v1/mobile/web-sale/item/percent-discount [✔ IMPLEMENTED]
- companion satırında yüzde indirim uygular

### POST /api/v1/mobile/web-sale/item/fixed-discount [✔ IMPLEMENTED]
- companion satırında sabit TL indirim uygular

### POST /api/v1/mobile/web-sale/item/reset-price [✔ IMPLEMENTED]
- companion satırını liste fiyatına sıfırlar

### POST /api/v1/mobile/web-sale/complete [✔ IMPLEMENTED]
- companion modundan satış tamamlar

### POST /api/v1/mobile/web-sale/print [✔ IMPLEMENTED]
- companion modundan yazdirma/PDF baglantisi üretir
- request:
  - `companyCode` (zorunlu)
  - `deviceUid` (zorunlu)
  - `deviceName` (opsiyonel)
  - `saleId` (opsiyonel)
  - `paper` (`58mm`, `80mm`, `a4`, opsiyonel)
  - `output` (`print`, `pdf`, opsiyonel)
- response:
  - `printReady`
  - `saleId`
  - `previewUrl` (signed, autoprint kapali)
  - `printUrl` (signed, autoprint acik)
  - `pdfUrl` (signed, A4 + PDF odakli)
  - `expiresAt`

## V4 Ticket API'leri

### GET /api/v1/support/inbox [✔ IMPLEMENTED]
- kullanıcının ve firmasının ticket özetlerini döner

### POST /api/v1/support/tickets [✔ IMPLEMENTED]
- yeni ticket açar
- misafir ticket için `companyCode` ve `deviceUid` zorunludur

### GET /api/v1/support/tickets/{id} [✔ IMPLEMENTED]
- ticket detay ve mesajlarını döner

### POST /api/v1/support/tickets/{id}/reply [✔ IMPLEMENTED]
- ticket'a mesaj ekler

### POST /api/v1/support/tickets/{id}/reopen [✔ IMPLEMENTED]
- kapalı ticket'ı yeniden açar

### POST /api/v1/support/tickets/{id}/attachments [✔ IMPLEMENTED]
- ticket dosya ekini yükler

## API Dürüstlük Kuralı

Bu dokümanda yazan her V4 uç şu anda sistemde mevcut olmak zorunda değildir.
Aktif olan uçlar implementasyon tamamlandıkça `program özellikleri.md` içinde işaretlenir.
