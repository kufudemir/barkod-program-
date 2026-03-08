# KolayKasa V4 Birleşik Planı
## Özet

Bu plan, `barkod.space` altında çalışan mevcut sistemi tek dokümanda birleştirir. Landing page ayrı plan olmayacak; `V4` planının `Faz 0` bölümüdür.

Ürün çatısı:
1. `KolayKasa` marka adı
2. `barkod.space` domaini
3. `Admin / Back Office`
4. `Web POS`
5. `Mobil POS`
6. `Web satış companion modu`
7. `Firma lisansı`
8. `Ticket / geri bildirim sistemi`
9. `Fiş yazdırma altyapısı`

Bu planın temel amacı:
- bugünkü mobil uygulamayı korumak
- admin paneli yönetim tarafı olarak bırakmak
- web satış tarafını ayrı ürün alanına dönüştürmek
- lisans, ticket, landing ve paketleri profesyonel hale getirmek
- tüm kararları implementer için karar tamam seviyesinde kilitlemek

Bu plan Plan Mode bittikten sonra şu dosyaya yazılacak:
- `c:\Users\Ufuk\Desktop\barkod programı\dokumanlar\uygulama-dokumanlari\gelistirme-fazlari-v4-web-pos-parity.md`

Güncellenecek diğer dosyalar:
- `c:\Users\Ufuk\Desktop\barkod programı\dokumanlar\uygulama-dokumanlari\program özellikleri.md`
- `c:\Users\Ufuk\Desktop\barkod programı\dokumanlar\web-dokumanlari\web-veri-modeli.md`
- `c:\Users\Ufuk\Desktop\barkod programı\dokumanlar\web-dokumanlari\web-api-sozlesmesi.md`
- `c:\Users\Ufuk\Desktop\barkod programı\dokumanlar\uygulama-dokumanlari\changelog.md`

---

## 1. Nihai Ürün Konumu

### Marka
- Ürün adı: `KolayKasa`
- Domain: `barkod.space`

### Kullanım cümlesi
- `KolayKasa, barkod.space altyapısıyla çalışır.`

### Ana değer önerisi
- Telefonda da, web kasada da satış
- Ortak katalog ve firma verisi
- Barkod odaklı hızlı satış
- Küçük işletme / esnaf odaklı kullanım
- Bulutla desteklenen ama mobilde çalışabilir yapı

### Ürün katmanları
1. `Landing`
- pazarlama ve giriş yüzü

2. `Back Office`
- admin panel
- firma, katalog, kullanıcı, lisans, senkron, ticket, sistem yönetimi

3. `Web POS`
- satış arayüzü
- admin panelden ayrı

4. `Mobil Uygulama`
- tam mobil POS
- ayrıca web satış companion modu

---

## 2. Landing Page Kararları

## 2.1 Ana amaç
Landing page şunları yapacak:
1. Ürünü anlatacak
2. APK indirtecek
3. Kullanıcı oluşturmaya yönlendirecek
4. Misafir başlangıç yolunu gösterecek
5. Paketleri sunacak
6. Lisans talebi toplayacak
7. Web POS vizyonunu gösterecek
8. Video ve ekran görüntüsü alanı taşıyacak

## 2.2 Ana CTA
Ana CTA:
- `APK İndir`

Yardımcı CTA’lar:
- `Kullanıcı Oluştur`
- `Misafir Olarak Başla`
- `Giriş Yap`

Landing üzerinde `Web POS Girişi` ana CTA olmayacak.
- üst küçük menü veya giriş alanında olacak

## 2.3 Hero metni
Başlık:
- `KolayKasa ile barkodlu satışa telefondan başlayın`

Alt metin:
- `Market, tekel, büfe ve küçük işletmeler için geliştirilen KolayKasa; barkod okutma, ürün yönetimi, stok takibi, raporlar ve bulut destekli senkron altyapısını tek uygulamada sunar.`

Güven satırı:
- `Android uygulama hazır`
- `Bulut senkron hazır`
- `Web yönetim paneli hazır`
- `Web POS aktif geliştiriliyor`

## 2.4 Bölüm sırası
Landing page bölümleri:

1. `Hero`
2. `Nasıl Çalışır`
3. `Kullanım Senaryoları`
4. `Hazır Özellikler`
5. `Bulut ve Yönetim`
6. `Paketler`
7. `Fiş ve Yazdırma`
8. `Video`
9. `Ekran Görüntüleri`
10. `Sık Sorulan Sorular`
11. `Lisans Başvurusu`
12. `Footer`

## 2.5 İçerik dürüstlük kuralı
Landing page’de tüm özellikler 3 etiketten biriyle gösterilecek:
- `Hazır`
- `Aktif geliştiriliyor`
- `Yakında`

Aşağıdakiler şu an `Hazır` diye sunulmayacak:
- tam web POS
- çoklu telefon sekmeleri
- gelişmiş fiş yazdırma
- ticket sistemi
- şirket lisans başvuru akışı
- personel rolleri

## 2.6 Paket kartı dili
Paket adları:
- `Ücretsiz`
- `Gümüş`
- `Altın`

Alt başlıkları:
- `Başlangıç`
- `Mobil Pro`
- `Web POS`

---

## 3. Paket ve Lisans Modeli

## 3.1 Lisans bağı
Lisans kullanıcıya veya cihaza değil, `firma`ya bağlanır.

### Anlamı
Bir firma lisansı:
- mobil cihazlarını
- bulut verisini
- web POS erişimini
- bağlı kullanıcılarını
- şube / kasa kullanımını
tek lisans seviyesinden etkiler

## 3.2 Ödeme modeli
İlk sürüm:
- `banka transferi`

### Özellikler
- online ödeme entegrasyonu yok
- banka dekont / referans açıklaması ile işlem
- admin panelden onay

## 3.3 Lisans talep akışı
1. Kullanıcı landing veya uygulama üzerinden talep oluşturur
2. Talep `pending_payment` oluşur
3. Kullanıcıya banka bilgisi gösterilir
4. Kullanıcı ödeme yapar
5. Admin panelde talep incelenir
6. Admin firmaya lisans atar
7. Lisans `active` olur
8. Firma lisansı mobil ve webde görünür

## 3.4 Paket teknik modeli
Tam serbest paket sistemi yapılmayacak.
Seçilen model:
- `özellik matrisi + paket şablonu + firma override`

### Neden
- destek kolaylaşır
- hangi pakette ne açık olduğu net olur
- teknik kaos oluşmaz
- ama admin esnekliği korunur

## 3.5 Paket veri modeli

### `feature_flags`
- id
- key
- title
- description
- scope (`mobile`, `web_pos`, `admin`, `shared`)
- is_core
- created_at
- updated_at

### `license_packages`
- id
- code (`FREE`, `SILVER`, `GOLD`)
- name
- description
- sort_order
- is_active
- created_at
- updated_at

### `license_package_features`
- id
- package_id
- feature_flag_id
- is_enabled
- created_at
- updated_at

### `company_licenses`
- id
- company_id
- package_id
- status (`active`, `suspended`, `expired`, `cancelled`)
- starts_at
- expires_at nullable
- assigned_by_admin_user_id
- source (`manual_bank_transfer`, `manual_admin`)
- note nullable
- created_at
- updated_at

### `company_license_feature_overrides`
- id
- company_license_id
- feature_flag_id
- is_enabled
- reason nullable
- created_at
- updated_at

### `license_requests`
- id
- company_id nullable
- requested_by_mobile_user_id nullable
- requester_name
- requester_email
- requester_phone nullable
- requested_package_code (`SILVER`, `GOLD`)
- status (`pending_payment`, `payment_review`, `approved`, `rejected`, `cancelled`)
- bank_reference_note nullable
- admin_note nullable
- created_at
- updated_at

### `company_license_events`
- id
- company_license_id
- event_type
- payload_json
- created_at

## 3.6 Paket çözümleme kuralı
Bir feature erişimi şu sırayla çözülür:
1. `is_core = true` ise her zaman açık
2. firma override varsa override uygulanır
3. yoksa firmanın aktif paketindeki feature değeri uygulanır

## 3.7 Paket içeriği

### `Ücretsiz`
Açık olacak:
- mobil barkod okutma
- ürün ekleme / düzenleme
- ürün listesi
- temel sepet
- temel satış tamamlama
- misafir kullanım
- temel fiyat güncelleme
- temel arama
- cihaz içi kullanım

Kapalı olacak:
- bulut hesap / firma lisansı gerektiren gelişmiş sync
- raporlar
- stok takibi
- OCR
- gelişmiş isim önerme
- web arama
- BarkodBankası import
- toplu fiyat / stok güncelleme
- satır bazlı fiyat override ve indirim
- web POS
- ticket sistemi
- gelişmiş bulut geri yükleme

### `Gümüş`
Açık olacak:
- Ücretsiz içindeki her şey
- kullanıcı hesabı
- bulut senkron
- firma bazlı bulut veri
- bulut katalog geri yükleme
- OCR
- web isim önerme
- web barkod arama
- BarkodBankası import
- raporlar
- stok takibi
- toplu fiyat güncelleme
- toplu stok güncelleme
- satır bazlı özel fiyat / % indirim / TL indirim
- ticket sistemi
- premium kurtarma / hesap kurtarma

Kapalı olacak:
- web POS
- çoklu cihaz sekmeleri
- HID barkod okuyucu web desteği
- bekleyen satış web
- ödeme türleri web
- fiş profilleri

### `Altın`
Açık olacak:
- Gümüş içindeki her şey
- web POS
- şube / register / POS session
- çoklu cihaz sekmeleri
- telefon companion modu
- HID barkod okuyucu desteği
- bekleyen satış
- ödeme türleri
- fiş yazdırma
- firma fiş profilleri
- gelişmiş işletme akışları

## 3.8 Geçiş kuralı
Mevcut sistemdeki `FREE/PRO` mantığı şu şekilde map edilecek:
- mevcut `FREE` -> `FREE`
- mevcut `PRO` -> `SILVER`

`GOLD` yeni web POS paketi olarak boş başlayacak.

---

## 4. Mobil Uygulama Davranışı

## 4.1 Giriş sonrası mod seçimi
Her girişten sonra kullanıcıya sorulacak:
- `Web üzerinden satış`
- `Mobil üzerinden satış`

Bu karar sabit.

## 4.2 Mobil üzerinden satış
Bugünkü mobil POS davranışı korunur:
- barkod okutma
- seri tarama
- ürün ekleme / düzenleme
- sepet
- satış
- stok
- rapor
- premium
- senkron

## 4.3 Web üzerinden satış
Telefon sadece barkod okuyucu olmaz.
Aktif web POS oturumuna bağlanmış yardımcı POS olur.

İzin verilen işlemler:
- barkod okutma
- sepete ürün ekleme
- adet + / -
- satır silme
- özel fiyat
- yüzde indirim
- TL indirim
- satış tamamlama
- yazdırma tetikleme

## 4.4 Aktif web oturumu yoksa
Gösterilecek ekran:
- `Web üzerinde oturumunuz bulunmuyor`
- butonlar:
  - `Yeniden Dene`
  - `Mobil POS’a Geç`

## 4.5 Çoklu telefon modeli
Aynı web POS’a birden fazla telefon bağlanabilir.
Ama:
- her telefonun kendi sepeti vardır

Bu nedenle “ortak tek sepet” yapılmayacak.

---

## 5. Web POS Mimari Kararı

## 5.1 Admin panelden ayrılacak
Alanlar:
- `/admin`
- `/pos/login`
- `/pos`

`/pos` kendi auth, layout ve middleware katmanına sahip olacak.

## 5.2 Veri hiyerarşisi
- Firma
- Şube
- Register / Kasa
- POS Session

Yeni tablolar:
### `branches`
- id
- company_id
- name
- code
- status
- created_at
- updated_at

### `registers`
- id
- branch_id
- name
- code
- status
- created_at
- updated_at

### `pos_sessions`
- id
- register_id
- opened_by_mobile_user_id
- status (`active`, `closed`)
- opened_at
- closed_at
- last_activity_at

## 5.3 Çoklu sepet modeli
### `sale_sessions`
- id
- pos_session_id
- source_device_uid nullable
- source_label
- created_by_mobile_user_id nullable
- status (`active`, `held`, `completed`, `cancelled`)
- created_at
- updated_at

### `sale_session_items`
- id
- sale_session_id
- barcode
- product_name_snapshot
- group_name_snapshot
- quantity
- base_sale_price_kurus
- applied_sale_price_kurus
- cost_price_kurus
- pricing_mode (`list`, `custom`, `percent_discount`, `fixed_discount`)
- pricing_meta_json
- line_total_kurus
- line_profit_kurus
- created_at
- updated_at

## 5.4 Web POS görünüm standardı
Web POS admin panel gibi görünmeyecek.
Satış odaklı, geniş aralıklı, sade olacak.

### Layout
1. `Üst bar`
- firma
- şube
- register
- kullanıcı
- bağlantı / senkron
- lisans seviyesi
- küçük kullanıcı menüsü

2. `Sekme satırı`
- `Web Manuel`
- `Telefon 1`
- `Telefon 2`
- `Telefon 3`

3. `Sol ana alan`
- barkod input
- ürün arama
- son taranan ürün
- sepet satırları

4. `Sağ ödeme paneli`
- ara toplam
- indirim
- genel toplam
- ödeme türü
- satışı tamamla
- beklet
- fiş yazdır

### UI kuralları
- teknik string gösterilmez
- cihaz UID gösterilmez
- input alanları pasif halde de görünür
- sıkışık kart düzeni kullanılmaz
- satır aralıkları geniş olur

## 5.5 Barkod okuyucu desteği
HID barkod okuyucu ilk sürümde doğal çalışmalı.
Strateji:
- sürekli fokus alan barkod input
- Enter ile otomatik ekleme
- tekrar okutma ile adet artışı
- scanner ready göstergesi

---

## 6. Ödeme ve Fiş Stratejisi

## 6.1 İlk ödeme türleri
- `cash`
- `card`
- `other`

### `sale_payments`
- id
- sale_id
- method
- amount_kurus
- created_at

## 6.2 Fiş yazdırma ilk sürüm
İlk sürüm hedefi:
- tüm yazıcı markalarına en az bir çıktı yolu vermek

### Web
- tarayıcıdan yazdır
- PDF yazdır / kaydet
- 58mm termal şablon
- 80mm termal şablon
- A4 şablon

### Mobil
- web üzerinden yazdır
- Android paylaş / yazdır fallback

### `receipt_profiles`
- id
- company_id
- branch_id nullable
- name
- paper_size (`58mm`, `80mm`, `a4`)
- header_json
- footer_json
- visible_fields_json
- field_order_json
- print_mode (`browser`, `pdf`)
- is_default
- created_at
- updated_at

## 6.3 İlk sürüm kapsam dışı
- resmi mali belge
- ESC/POS ham çıktı
- sürükle-bırak fiş editörü
- tüm yazıcılara native raw profil

---

## 7. Ticket / Geri Bildirim Sistemi

## 7.1 Amaç
Kullanıcı:
- hata bildirsin
- özellik istesin
- genel geri bildirim versin

Admin:
- admin panelden tüm ticket’ları yönetsin
- yanıt versin
- ticket durumunu güncellesin

## 7.2 Kaynaklar
İlk sürüm ticket kaynakları:
- mobil uygulama
- web POS

Landing page’den public ticket açılmayacak.

## 7.3 Türler
- `bug`
- `feature_request`
- `general`

## 7.4 Durumlar
- `new`
- `reviewing`
- `answered`
- `closed`

## 7.5 Görünürlük
Ticket ve yanıtlar:
- aynı firmadaki tüm kullanıcılar tarafından görülebilir

## 7.6 Misafir desteği
Misafir kullanıcı ticket açabilir.
Ama:
- cihaz + firma bağlamında tutulur
- sınırlı modeldir

## 7.7 Reopen
Kullanıcı kapatılmış ticket’ı yeniden açabilir.

## 7.8 Veri modeli
### `feedback_reports`
- id
- type
- source (`mobile`, `web_pos`)
- company_id nullable
- mobile_user_id nullable
- device_uid nullable
- app_version nullable
- web_url nullable
- title
- description
- status
- created_at
- updated_at

### `feedback_messages`
- id
- feedback_report_id
- author_type (`user`, `admin`)
- author_id nullable
- message
- is_internal_note
- created_at

### `feedback_attachments`
- id
- feedback_report_id
- feedback_message_id nullable
- file_path
- mime_type
- created_at

## 7.9 Kullanıcı arayüzleri
### Mobil
- `Destek ve Geri Bildirim`
- `Gelen Kutusu`
- `Yeni Ticket`

### Web POS
- üst menüde:
  - `Destek`
  - `Gelen Kutusu`
  - `Yeni Ticket`

### Bildirim
- yeni admin cevabı varsa rozet görünür

---

## 8. Public API / Interface / Type Değişiklikleri

## 8.1 Landing / lisans
### Web public routes
- `/`
- `/paketler`
- `/apk`
- `/kullanici-olustur`
- `/misafir-basla`
- `/lisans-talebi`

### API
1. `POST /api/v1/license/request`
2. `GET /api/v1/company/license`
3. `POST /api/v1/company/license/refresh`

## 8.2 Web POS
### Routes
- `/pos/login`
- `/pos`
- `/pos/session/{id}`
- `/pos/held`
- `/pos/history`

### API
4. `POST /api/v1/pos/session/open`
5. `POST /api/v1/pos/session/close`
6. `GET /api/v1/pos/session/active`
7. `POST /api/v1/pos/session/{id}/scanner/attach`
8. `POST /api/v1/pos/session/{id}/scanner/detach`
9. `POST /api/v1/pos/session/{id}/scan`
10. `POST /api/v1/pos/session/{id}/items`
11. `PATCH /api/v1/pos/session/{id}/items/{itemId}`
12. `DELETE /api/v1/pos/session/{id}/items/{itemId}`
13. `POST /api/v1/pos/session/{id}/hold`
14. `POST /api/v1/pos/session/{id}/resume`
15. `POST /api/v1/pos/session/{id}/checkout`
16. `POST /api/v1/pos/session/{id}/print`

## 8.3 Mobil web companion
17. `GET /api/v1/mobile/web-sale/active`
18. `POST /api/v1/mobile/web-sale/complete`
19. `POST /api/v1/mobile/web-sale/print`

## 8.4 Ticket sistemi
20. `GET /api/v1/support/inbox`
21. `POST /api/v1/support/tickets`
22. `GET /api/v1/support/tickets/{id}`
23. `POST /api/v1/support/tickets/{id}/reply`
24. `POST /api/v1/support/tickets/{id}/reopen`
25. `POST /api/v1/support/tickets/{id}/attachments`

## 8.5 Android yeni tipler
- `AppSaleMode`
  - `WEB_SATIS`
  - `MOBIL_SATIS`
- `ActiveWebPosSessionState`
- `CompanionSaleSession`
- `CompanionCartItem`
- `ReceiptProfileSummary`
- `CompanyLicenseSummary`
- `LicenseRequestDraft`
- `SupportTicketSummary`
- `SupportMessage`
- `SupportInboxBadge`

---

## 9. Fazlar

## Faz 0 — Landing Page ve Ticari Sunum
### Hedef
KolayKasa için gerçek ürün vitrini oluşturmak.

### Çıktılar
- hero
- nasıl çalışır
- kullanım senaryoları
- hazır özellikler
- bulut ve yönetim
- paketler
- fiş ve yazdırma bölümü
- video alanı
- ekran görüntüleri
- SSS
- lisans başvuru alanı
- footer

### Başarı kriteri
- kullanıcı ürünü ilk 10 saniyede anlar
- APK indirme ve kullanıcı oluşturma net görünür
- hazır olmayan şeyler dürüst etiketlenir

---

## Faz 1 — Firma Lisansı ve Paket Yönetimi
### Hedef
Firma lisansı + banka transferi + admin yönetimli paket sistemi

### Çıktılar
- lisans talepleri
- firma lisansları
- feature flag tabanı
- paket şablonları
- firma override

---

## Faz 2 — Web POS’u Admin Panelden Ayırma
### Hedef
Satış arayüzünü back office’ten ayırmak

### Çıktılar
- `/pos/login`
- `/pos`
- ayrı auth
- ayrı layout
- sade POS shell

---

## Faz 3 — Şube / Register / POS Session
### Hedef
Gerçek kasa hiyerarşisi

### Çıktılar
- branches
- registers
- pos_sessions

---

## Faz 4 — Çoklu Cihaz ve Satış Sekmeleri
### Hedef
Aynı web kasada çoklu telefon, ayrı sepet

### Çıktılar
- sale_sessions
- sale_session_items
- sekmeli kasa görünümü

---

## Faz 5 — Mobil Mod Seçimi ve Web Satış Modu
### Hedef
Mobil uygulamayı iki modlu hale getirmek

### Çıktılar
- mod seçimi ekranı
- web companion ekranı
- aktif web POS yoksa fallback ekranı

---

## Faz 6 — HID Barkod Okuyucu
### Hedef
USB/kablolu barkod okuyucuyu web POS’ta doğal kullanmak

---

## Faz 7 — Bekleyen Satış / Açık Fiş
### Hedef
Satış beklet / geri aç akışı

---

## Faz 8 — Ödeme Türleri
### Hedef
Nakit / kart / diğer

---

## Faz 9 — Fiş Yazdırma Altyapısı
### Hedef
58mm / 80mm / A4 + web yazdırma + mobil paylaş/yazdır

---

## Faz 10 — Firma Fiş Ayarları
### Hedef
Firma bazlı fiş profili yönetimi

---

## Faz 11 — Ticket Sistemi
### Hedef
Mobil ve web POS içinden geri bildirim / hata / özellik isteği toplamak

---

## Faz 12 — Personel ve Rol Sistemi
### Hedef
Owner / manager / cashier ayrımı

---

## Faz 13 — Senkron ve Veri Birleştirme
### Hedef
Web ve mobil tam parity

---

## 10. Test Senaryoları

## Landing
1. Ana sayfa açılır
2. `APK İndir` çalışır
3. `Kullanıcı Oluştur` görünür
4. `Misafir Olarak Başla` görünür
5. Paket kartları görünür
6. `Altın` paketi `Yakında` olarak işaretlenir
7. Lisans başvuru formu açılır
8. Footer yasal linkleri çalışır

## Lisans
9. Kullanıcı lisans talebi bırakır
10. Talep `pending_payment` oluşur
11. Admin panelde görünür
12. Admin firmaya lisans atar
13. Firma lisans seviyesi mobil ve webde görünür
14. Override ile tek bir özellik açılıp kapanabilir

## Mobil mod seçimi
15. Giriş sonrası mod seçimi açılır
16. `Mobil üzerinden satış` mevcut akışı açar
17. `Web üzerinden satış` web companion akışı açar

## Web POS
18. Kullanıcı `/pos/login` ile giriş yapar
19. Firma / şube / register seçer
20. Barkod input HID okuyucudan veri alır
21. Manuel barkod girişi çalışır
22. Satış tamamlanır

## Mobil companion
23. Aynı hesapla telefon web moduna girer
24. Aktif web POS varsa bağlanır
25. Barkod okutur
26. Kendi sekmesine ürün düşer
27. Adet / fiyat / indirim çalışır
28. Satış tamamlanır

## Çoklu cihaz
29. İki telefon aynı web POS’a bağlanır
30. Her telefonun ayrı sepet sekmesi vardır
31. Cihazlar birbirinin sepetini bozmaz

## Bekleyen satış
32. Satış bekletilir
33. Tekrar açılır
34. Web ve mobil aynı fişe dönebilir

## Yazdırma
35. 58mm fiş görünümü doğru
36. 80mm fiş görünümü doğru
37. A4 görünümü doğru
38. Web yazdırma açılır
39. Mobil paylaş/yazdır fallback çalışır

## Ticket
40. Mobilde yeni ticket açılır
41. Web POS’ta yeni ticket açılır
42. Admin panelde görünür
43. Admin cevap verir
44. Mobil gelen kutusunda rozet görünür
45. Web POS gelen kutusunda rozet görünür
46. Aynı firmadaki ikinci kullanıcı cevabı görebilir
47. Ticket yeniden açılabilir

---

## 11. Açık Varsayımlar ve Seçilen Varsayılanlar

1. Ürün adı `KolayKasa`
2. Domain `barkod.space`
3. Landing ana CTA `APK İndir`
4. Paketler `Ücretsiz / Gümüş / Altın`
5. Lisans firma bazlıdır
6. İlk ödeme modeli banka transferidir
7. Lisans akışı talep + bekleme + admin onayıdır
8. Paket sistemi admin panelden yönetilebilir ama tam serbest değil, feature matrix + package template + company override modelindedir
9. Teknik paket kodları değişmez:
- `FREE`
- `SILVER`
- `GOLD`
10. Mobil uygulama iki modludur
11. Telefon web modunda tam yardımcı POS olur
12. Aynı web kasaya çoklu telefon bağlanabilir
13. Her telefon ayrı sepet kullanır
14. Aktif web oturumu yoksa kullanıcı `Yeniden Dene / Mobil POS’a Geç` ekranını görür
15. Teknik veri otoritesi sunucudur
16. İlk ödeme türleri `nakit / kart / diğer`
17. İlk fiş sistemi resmi mali belge değildir
18. İlk yazdırma stratejisi `tarayıcı yazdırma + mobil paylaş/yazdır`
19. Ticket sistemi ilk sürümde yalnızca mobil ve web POS içinden açılır
20. Ticket yanıtları aynı firmadaki tüm kullanıcılar tarafından görülebilir
21. Misafir ticket açabilir ama sınırlı bağlamda tutulur
22. Admin panel ayrı, web POS ayrı arayüz olmaya devam eder

