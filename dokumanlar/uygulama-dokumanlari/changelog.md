# Değişiklik Geçmişi

## 2026-03-08 Dokuman Senkronu (Surum Degisikligi Yok)
- Proje dokumanlari bastan sona tarandi ve durum notlari guncellendi.
- program ozellikleri.md icinde sistem kapsam/surum referanslari ve uygulanan ozellik durumlari senkronlandi.
- test-sonrasi-is-listesi.md V4 tamamlanan fazlara gore kapatilmis/acik maddelerle yeniden duzenlendi.
- eklenebilecek-ozellikler.md mevcut urunle cakismayan gercek sonraki backlog olacak sekilde guncellendi.
- gelistirme-fazlari-v4-web-pos-parity.md kapanis/stabilizasyon turu takibiyle hizali kalacak sekilde dokuman kontrolunden gecirildi.
- yonetici-ozet-tek-ekran.md V4 odaktan cikarilip proje geneli tek ozet formatinda yeniden yazildi.
## 1.03.01
- Faz 13B tamamlandi: mobil satis publish outbox fallback eklendi.
- Mobil POS checkout sonrasi cloud publish akisi guclendirildi:
  - ag hatasi varsa satis verisi outbox'a yazilir
  - worker ile otomatik retry edilir
- Katalog senkron worker'i artik karma event tiplerini ayirir:
  - `PRODUCT_UPSERT`
  - `LOCAL_SALE_PUBLISH`
- APK surumu `1.03.01` olarak guncellendi.

## Web Patch v1.03.01
- Android dagitim dosyalari guncellendi:
  - `public/app-updates/android/latest.json`
  - `public/app-updates/android/marketpos-v1.03.01-debug.apk`
- Web kod degisikligi yok.

## 1.02.01
- Faz 13A baslatildi: web-mobil katalog parity cekirdegi eklendi.
- Android tarafinda buluttan artimli katalog degisikligi cekme ve local merge akisi eklendi.
- Senkron worker artik outbox push sonrasinda cloud degisikliklerini de ceker.
- Yeni ayar anahtarlari:
  - `sync.catalog.cursor`
  - `sync.catalog.cursor.company`
- APK surum bilgisi `1.02.01` olarak guncellendi.

## Web Patch v1.02.01
- Faz 13A web ciktilari eklendi.
- Yeni endpoint:
  - `GET /api/v1/auth/companies/{companyCode}/catalog/changes`
- Tam katalog endpointi role-farkindalikli hale getirildi:
  - owner + aktif personel (manager/cashier) erisimi
  - yanita `cursor` bilgisi eklendi
- Mobil firma erisim kontrolu icin ortak servis eklendi: `MobileCompanyAccessService`.
- Migration yok.

## Web Patch v1.01.02
- Faz 12 tamamlandi: personel ve rol sistemi devreye alindi.
- Yeni tablo: `company_staff_roles` (firma bazli personel rolu).
- Rol modeli aktif edildi:
  - `owner`
  - `manager`
  - `cashier`
- POS auth/middleware role bagli hale getirildi:
  - owner disi personel atamasi ile web POS girisi
  - firma erisiminde role dayali kontrol
- Web POS menu `Cihazlari Yonet` aktif edildi.
- Yeni management endpointleri:
  - `GET /pos/manage/overview`
  - `POST /pos/manage/devices/{device}/toggle`
  - `POST /pos/manage/sessions/{posSession}/close`
  - `POST /pos/manage/staff/upsert`
  - `POST /pos/manage/staff/{staffRole}/delete`
- Web POS role bagli yetki kisitlari eklendi:
  - kasa/sube/oturum yonetimi
  - firma/fis ayarlari
  - gecmis satis fis duzenleme/silme
- Mobil web companion firma erisimi owner + personel rollerini destekler hale getirildi.
- Migration var: `2026_03_08_210000_create_company_staff_roles_table.php`.

## 1.01.01
- Faz 11B tamamlandi.
- Ayarlar menusu altina `Destek ve Geri Bildirim` ekrani eklendi.
- Mobil ticket akislari eklendi:
  - gelen kutusu
  - durum filtresi
  - yeni ticket olusturma
  - ticket detayi ve mesajlasma
  - kapali ticketi yeniden acma
- Yeni domain modelleri/repository:
  - `SupportModels`
  - `SupportRepository`
  - `SupportRepositoryImpl`
- Android API istemcisine support endpointleri eklendi:
  - `GET /api/v1/support/inbox`
  - `POST /api/v1/support/tickets`
  - `GET /api/v1/support/tickets/{ticketId}`
  - `POST /api/v1/support/tickets/{ticketId}/reply`
  - `POST /api/v1/support/tickets/{ticketId}/reopen`
- APK surum bilgisi `1.01.01` olarak guncellendi.

## Web Patch v1.01.01
- Faz 11B web ciktilari tamamlandi.
- Web POS icine destek modulu eklendi:
  - topbar menu: `Destek Gelen Kutusu` ve `Yeni Ticket`
  - modal tabanli inbox/detay/yanit/yeniden acma akisi
- Yeni POS support endpointleri eklendi:
  - `GET /pos/support/inbox`
  - `GET /pos/support/tickets/{ticketId}`
  - `POST /pos/support/tickets`
  - `POST /pos/support/tickets/{ticketId}/reply`
  - `POST /pos/support/tickets/{ticketId}/reopen`
- Admin panelde `Ticket Merkezi` resource'u eklendi:
  - listeleme/filtreleme
  - admin yaniti
  - durum guncelleme
  - detay ve duzenleme sayfalari
- Migration yok.

## Web Patch v1.0.84
- Faz 11A tamamlandi: ticket/geri bildirim altyapisi eklendi.
- Yeni migration: `2026_03_08_120000_create_feedback_tables.php`
  - `feedback_reports`
  - `feedback_messages`
  - `feedback_attachments`
- Yeni modeller:
  - `FeedbackReport`
  - `FeedbackMessage`
  - `FeedbackAttachment`
- Yeni API controller:
  - `SupportTicketController`
- Yeni API endpointleri:
  - `GET /api/v1/support/inbox`
  - `POST /api/v1/support/tickets`
  - `GET /api/v1/support/tickets/{ticketId}`
  - `POST /api/v1/support/tickets/{ticketId}/reply`
  - `POST /api/v1/support/tickets/{ticketId}/reopen`
  - `POST /api/v1/support/tickets/{ticketId}/attachments`
- Erişim kuralları:
  - bearer token ile kullanıcı erişimi
  - aynı firmaya bağlı ticket görünürlüğü
  - misafir ticket için `companyCode + deviceUid` zorunluluğu
- Migration var.

## Web Patch v1.0.83-hotfix1
- Checkout sirasindaki `Unknown named parameter $paymentMethod` hatasi giderildi.
- `WebPosService::completeSale(...)` cagrilari positional argument formatina alindi:
  - `PosSaleController`
  - `MobileWebSaleController`
- Migration yok.

## Web Patch v1.0.83
- Faz 10 tamamlandi: firma bazli fis ayarlari eklendi.
- Yeni veri modeli: `receipt_profiles` (firma/sube varsayilan fis profili).
- Web POS topbar menusu altina `Fis Ayarlari` modal akisi eklendi.
- Fis profili alanlari:
  - kagit tipi (`58mm / 80mm / A4`)
  - yazdirma modu (`Tarayici / PDF`)
  - baslik ve alt bilgi satirlari
  - gorunur alan secimleri (firma, vergi, odeme, barkod, tarih, kasa)
- `receipt-show` ekrani profile baglandi:
  - varsayilan kagit tipi profilden okunur
  - secili alanlar profile gore gosterilir/gizlenir
  - baslik ve alt bilgi satirlari fiste gosterilir
- Mobil `POST /api/v1/mobile/web-sale/print` varsayilan kagit tipini firma fis profilinden alir hale getirildi.
- Migration var: `2026_03_06_190000_create_receipt_profiles_table.php`.

## 1.0.71
- Mobil companion ekranından `Son Satislar (Web + Mobil)` kartı kaldırıldı.
- Web satıs companion ekranı sadeleştirildi (ana satış akışına odaklı görünüm).

## Web Patch v1.0.82
- Web POS fiş popup düzenleme ekranına canlı satır tutarı hesaplama eklendi.
- Adet veya birim fiyat değiştiğinde satır tutarı anlık güncellenir hale getirildi.
- Fiş kaydet sonrası popup satırlarının görünmez olması hatası giderildi (`itemsEditable` state korunuyor).
- Android update bildirimi `1.0.71` sürümüne güncellendi (`latest.json` + yeni APK).
- Migration yok.

## 1.0.70
- Mobil companion barkod taramada cift okuma engeli guclendirildi:
  - islem surerken yeni tarama bloklanir
  - global cooldown penceresi eklendi
  - ayni barkod icin anti-tekrar suresi uzatildi
- Mobil companion ekranina Son Satislar (Web + Mobil) listesi eklendi.
- Mobil normal POS checkout sonrasi satisi buluta aktaran arka plan publish akisi eklendi (api/v1/mobile/sales/publish).

## Web Patch v1.0.81
- Fis popup kaydet akisindaki Call to undefined relationship [payments] hatasi giderildi.
- PosReceiptController odeme verisini Eloquent relation yerine dogrudan tablo sorgusuyla yonetir hale getirildi (eski model dagitimlarinda da uyumlu).
- Web POS Bugunku ozet ve Son Satislar listesi register + Mobil POS (register null) satislarini birlikte gosterecek sekilde guncellendi.
- Mobil API yanitina recentSales ve lastSale alanlari eklendi (companion son satis paneli icin).
- Yeni endpoint: POST /api/v1/mobile/sales/publish.
- Migration yok.

## 1.0.69
- Mobil companion yazdirma akisinda URL tabanli acilis eklendi.
- `Yazdirma Tetikle` butonu `Yazdir / PDF` olarak guncellendi.
- Companion, sunucudan gelen fis baglantisini tarayicida acar hale getirildi.
- Tarayici acik degilse paylasma chooser fallback'i eklendi.

## Web Patch v1.0.80
- Mobil companion barkod taramasi sonrasi Web POS canli senkron akisina alindi.
- Yeni endpoint: `GET /pos/sync/state`.
- Mobil cihazdan guncellenen satis sekmesi webde otomatik one getirilir hale getirildi.
- Web POS ana ekrandaki `Son Satislar` kartlari tiklanabilir yapildi ve popup fis detayi eklendi.
- Fis popup icinden:
  - `58mm Yazdir`
  - `80mm Yazdir`
  - `A4 PDF`
  - satis satiri adet/fiyat duzenleme
  - odeme turu duzenleme
  - satis kaydi silme
  islemleri aktif edildi.
- Satis/Fisler (`/pos/receipts`) sayfasi yeni sayfaya gitmeden popup akisina gecirildi.
- Yeni route'lar:
  - `GET /pos/receipts/{webSale}/json`
  - `POST /pos/receipts/{webSale}/update`
  - `POST /pos/receipts/{webSale}/delete`
- `sale_session` guncelleme zaman damgasi cart replace/clear islemlerinde yenilenir hale getirildi (canli sekme odaklama icin).
- Migration yok.

## Web Patch v1.0.79
- Faz 9 tamamlandi: fis yazdirma altyapisi devreye alindi.
- Web POS fis detay ekraninda kagit tipi secimi eklendi (`58mm / 80mm / A4`).
- Web POS fis detay ekranina `Yazdir` ve `PDF Kaydet` aksiyonlari eklendi.
- Fis ekrani query parametreleri aktif edildi:
  - `paper=58mm|80mm|a4`
  - `output=print|pdf`
  - `autoprint=1`
- Satis/Fis listesine hizli cikti kisayollari eklendi (`58mm Yazdir`, `A4 PDF`).
- Yeni signed rota eklendi: `GET /pos/receipts/public/{webSale}`.
- Mobil companion `POST /api/v1/mobile/web-sale/print` endpointi gercek yazdirma URL'leri doner hale getirildi.
- Mobil companion ekraninda `Yazdir / PDF` aksiyonu tarayicida fis baglantisini acar; tarayici yoksa paylasma fallback'i kullanilir.
- Migration yok.

## Web Patch v1.0.79-hotfix1
- Canli sunucuda 404 veren `/api/v1/mobile/web-sale/*` endpointleri icin route/controller geri yukleme paketi hazirlandi.
- `routes/api.php` ve `MobileWebSaleController` dosyalari patch'e eklendi.
- Migration yok.

## 1.0.68
- Faz 5 tamamlandi.
- Giris sonrasi satis modu secimi eklendi (`Web uzerinden satis` / `Mobil uzerinden satis`).
- Yeni web companion ekrani eklendi:
  - aktif web POS oturumu kontrolu
  - fallback: `Yeniden Dene` / `Mobil POS'a Gec`
  - barkod okutma ile sepete ekleme
  - satir adet artir/azalt/sil
  - satir bazli ozel fiyat, yuzde indirim, sabit TL indirim, fiyat sifirlama
  - satis tamamlama ve yazdirma tetigi
- Web companion API entegrasyonu eklendi.

## Web Patch v1.0.78
- Faz 8 tamamlandi: odeme turleri eklendi (`Nakit / Kart / Diger`).
- Checkout formuna odeme turu secimi eklendi.
- Satis tamamlama akisinda `payment_method` dogrulamasi eklendi.
- Yeni tablo eklendi: `web_sale_payments`.
- Satis tamamlandiginda odeme kaydi olusturulur hale getirildi.
- Son satislar, satis/fis listesi ve fis detayinda odeme turu gorunur hale getirildi.
- Sistem sifirlama aracina `web_sale_payments` temizligi eklendi.
- Migration var.

## Web Patch v1.0.77
- Web POS ana ekrandaki `Bugunku satis / Bugunku urun adedi / Bugunku ciro` kartlari checkout sonrasi anlik guncellenir hale getirildi.
- `Satisi Tamamla` AJAX yanitina `todaySummary` eklendi.
- Sayfa yenilemeden kart metrikleri dogru gorunur.
- Migration yok.

## Web Patch v1.0.76
- Bekleyen satis akisi ana POS ekranina tasindi.
- `Satisi Beklet` ile musterinin sepeti beklemeye alinip yeni aktif sekme ile satisa devam edilebilir hale getirildi.
- Ana ekranda `Bekleyen satislar` listesi eklendi:
  - tek tikla `Geri Ac`
  - tek tikla `Sil`
- Bekleyen satis islemleri AJAX canli akisinda calisir hale getirildi (sayfa yenileme yok).
- JSON payload'a `heldSessions` eklendi (aktif sekme + bekleyen sekmeler birlikte guncellenir).
- Migration yok.

## Web Patch v1.0.75-hotfix10
- V4 Faz 7 tamamlandi: Bekleyen satis / acik fis akisi eklendi.
- Web POS sepet aksiyonlarina `Satisi Beklet` eklendi.
- Bekletilen satis sekmesi `held` durumuna alinip yeni aktif sekme ile calisma devam eder hale getirildi.
- Yeni ekran: `GET /pos/receipts`:
  - bekleyen satis listesi
  - bekleyen satisi geri acma
  - bekleyen satisi silme
  - son satis fisleri listesi
- Yeni ekran: `GET /pos/receipts/{webSale}` fis detay gorunumu + yazdir butonu.
- Topbar menude `Satislari Fisle Gor` aktif edildi.
- Yeni route'lar:
  - POST `/pos/sale-session/hold`
  - GET `/pos/receipts`
  - GET `/pos/receipts/{webSale}`
  - POST `/pos/held/{saleSession}/resume`
  - POST `/pos/held/{saleSession}/discard`
- Migration yok.

## Web Patch v1.0.75-hotfix9
- V4 Faz 6 tamamlandi: Web POS icin HID barkod okuyucu deneyimi guclendirildi.
- Satis ekranina `Tarayici hazir` durum gostergesi eklendi (hazir/isleniyor/beklemede/pasif).
- Aktif POS oturumunda barkod input icin surekli odak kilidi eklendi.
- Menu ve modal acikken odak kilidi otomatik duraklatilip kapaninca geri acilir hale getirildi.
- Barkod kutusu odakta olmasa da HID okuyucudan gelen tuslar buffer'da toplanip Enter ile otomatik sepete eklenir hale getirildi.
- Migration yok.

## Web Patch v1.0.75-hotfix8
- Mobil companion API'leri eklendi:
  - GET `/api/v1/mobile/web-sale/active`
  - POST `/api/v1/mobile/web-sale/scan`
  - POST `/api/v1/mobile/web-sale/item/increment`
  - POST `/api/v1/mobile/web-sale/item/decrement`
  - POST `/api/v1/mobile/web-sale/item/remove`
  - POST `/api/v1/mobile/web-sale/item/custom-price`
  - POST `/api/v1/mobile/web-sale/item/percent-discount`
  - POST `/api/v1/mobile/web-sale/item/fixed-discount`
  - POST `/api/v1/mobile/web-sale/item/reset-price`
  - POST `/api/v1/mobile/web-sale/complete`
  - POST `/api/v1/mobile/web-sale/print`
- Companion istekleri bearer token + companyCode + deviceUid ile dogrulanir hale getirildi.
- Cihaza ozel `sale_session` otomatik bulunur/olusturulur.
- Migration yok

## Web Patch v1.0.75-hotfix7
- Web POS topbar yenilendi: cikis butonu yerine menu butonu eklendi.
- Firma degistir, sube/kasa secimi ve POS oturumu ac/kapat kontrolleri topbar menu altina tasindi.
- Satis ekranindaki ust kontrol blogu kaldirildi (ekran sadeleştirildi).
- Satis sekmesi ac/gec/kapat AJAX canli akisinda korunur.
- Sayfa ici bildirim satiri yerine sag ust toast sistemine gecildi (2 sn auto close + manuel kapatma).
- Firma Bilgileri popup'i eklendi ve POST /pos/company/profile ile kaydetme acildi.
- Menuye Cihazlari Yonet ve Satislari Fisle Gor (yakinda) girdileri eklendi.
- Migration yok.

## Web Patch v1.0.75-hotfix6
- Acil duzeltme: /pos sayfasindaki Blade parse hatasi giderildi (@json([...]) yerine guvenli payload encode kullanimi).
- Hata: Unclosed '[' on line 264 does not match ')' cozuldu.
- Migration yok.

## Web Patch v1.0.75-hotfix5
- Satis sekmesi ac/gec/kapat islemleri AJAX canli akisina alindi (sayfa yenileme yok).
- Sekme satiri canli render edilirken kapatma butonu sekme pill'i icinde birlestirildi.
- Sayfa ustundeki sabit bildirim alani kaldirildi; sag ustte toast bildirim sistemi eklendi (2 sn auto close + manuel kapatma).
- Migration yok.

## Web Patch v1.0.75-hotfix4
- Checkout sonrasi kapanan satis sekmesi Web POS ekraninda canli olarak kaldirilir (sekme bari AJAX ile anlik yenilenir).
- POS satis endpointleri aktif sekme listesini JSON payload icinde doner hale getirildi.
- Migration yok.

## Web Patch v1.0.75-hotfix3
- Web POS urun arama iyilestirildi: barkod disinda urun adi, grup ve not metni ile de oneri donebilir.
- Barkod alaninin placeholder metni Barkod veya urun adi yazin olarak guncellendi.
- Arama esik kontrolu sadeleştirildi; isimle arama davranisi netlestirildi.
- Migration yok.

## Web Patch v1.0.75-hotfix2
- Kritik bug duzeltildi: bir sekmeden satis tamamlaninca diger aktif sekmenin sepeti sifirlanmiyor.
- Web POS barkod alanina urun arama/oneri dropdown eklendi.
- Yeni route: GET /pos/search/products.
- Son Satislar paneli checkout sonrasi canli guncellemeye devam edecek sekilde korundu.
- Migration yok.

## Web Patch v1.0.75-hotfix1
- Web POS satis sekmelerine sekme kapatma aksiyonu eklendi.
- Yeni route: POST /pos/sale-session/close.
- Satis tamamlandiktan sonra Son Satislar paneli AJAX ile anlik guncellenir hale getirildi.
- Checkout sonrasi sekme gecisi optimize edildi: var olan aktif sekme varsa ona donulur, yoksa yeni sekme acilir.
- Migration yok.

## Web Patch v1.0.75
- V4 Faz 4 tamamlandi: Web POS icin coklu satis sekmesi altyapisi eklendi.
- Yeni tablolar: sale_sessions, sale_session_items.
- POS sepeti session yerine veritabaninda sale_session bazli tutulur hale getirildi.
- /pos ekranina satis sekmeleri eklendi (sekme degistir + yeni sekme ac).
- Satis tamamlama sonrasi aktif sekme tamamlandi durumuna cekilir ve ayni kaynaktan yeni aktif sekme olusur.
- Yeni route'lar:
  - POST /pos/sale-session/create
  - POST /pos/sale-session/switch
- Migration var.

## Web Patch v1.0.74
- V4 Faz 3 tamamlandi: Branch / Register / POS Session veri modeli eklendi.
- Yeni tablolar: branches, registers, pos_sessions.
- web_sales kayitlarina branch/register/pos_session/mobile_user baglantisi eklendi.
- /pos ekranina sube-kasa secimi ve POS oturumu ac/kapat akisi eklendi.
- POS islemleri aktif oturum zorunlu olacak sekilde guncellendi.
- Sepet session anahtari POS oturum bazli hale getirildi.
- Migration var.

## Web Patch v1.0.73-hotfix6
- Web POS sepet islemleri (urun ekle, adet +/-, satir sil, sepet temizle, satis tamamla) AJAX akisina alindi.
- Islem sonrasi sayfa yenileme kaldirildi; sepet satirlari ve ozet anlik guncellenir.
- POS satis endpointleri JSON yanit destekli hale getirildi.
- Migration yok.

## Web Patch v1.0.73-hotfix5
- POS login loop için session/proxy normalizasyonu eklendi (APP_URL schema, HTTPS/proxy, session path/domain).
- POS middleware login'e dönerken sebep kodu üretir hale getirildi (teşhis için).
- Login ekranında sebep mesajı ve session yazma doğrulaması eklendi.
- Migration yok.

## Web Patch v1.0.73-hotfix4
- POS login loop için session yazma sırası düzeltildi: regenerate -> put(pos.auth) -> save.
- /pos/login için hedefli CSRF istisnası korunarak paylaşımlı hosting uyumu artırıldı.
- Migration yok.

## Web Patch v1.0.73-hotfix3
- /pos/login 419 Page Expired problemi için sadece POST /pos/login endpoint'i CSRF doğrulama istisnasına alındı.
- POS içindeki diğer satış endpointleri CSRF korumasında bırakıldı.
- Migration yok.

## Web Patch v1.0.73-hotfix2
- /pos/login 419 Page Expired problemi için POS login ekranındaki manuel CSRF token yenilemesi kaldırıldı.
- POS içindeki tüm form action URL'leri göreli route yapısına alındı (http/https şema uyumsuzluğuna dayanıklı).
- Migration yok.

## Web Patch v1.0.73-hotfix1
- /pos/login 419 Page Expired problemi için POS rotalarına no-cache middleware eklendi.
- POS login sayfasında CSRF token her açılışta yenilenir hale getirildi.
- Amaç: cache/CDN kaynaklı eski token kullanımını engellemek.
## Web Patch v1.0.73
- V4 Faz 2B tamamlandı.
- Web POS shell ekranı satış çekirdeğine bağlandı.
- Barkod okutma/yazma ile sepete ürün ekleme aktif edildi.
- Sepette adet artırma, adet azaltma ve satır silme aktif edildi.
- Satışı tamamlama akışı (POST /pos/checkout) aktif edildi.
- Sepet temizleme akışı (POST /pos/cart/clear) eklendi.
- Firma değiştirme akışı (POST /pos/switch-company) eklendi.
- POS sepeti session tabanlı servis ile yönetilir hale getirildi.
## Web Patch v1.0.72
- V4 Faz 2A tamamlandı.
- /pos/login, /pos, /pos/logout route'ları eklendi.
- Admin panelden bağımsız POS oturum yapısı eklendi (pos.auth middleware).
- Ayrı Web POS giriş ekranı eklendi.
- Ayrı Web POS shell ekranı eklendi (Faz 2B için hazır kabuk).
- Eski admin içi WebPos sayfası menüden gizlendi.
## Web Patch v1.0.71
- V4 Faz 1C tamamlandı.
- Açık web lisans talep formu gerçek POST akışına geçirildi.
- Lisans talepleri artık veritabanına kayıt oluyor (pending_payment başlangıç durumu).
- Admin panelde Lisans Talepleri yönetimi eklendi.
- Talep durum geçişleri eklendi:
  - pending_payment -> payment_review
  - payment_review -> approved/rejected/cancelled
- Talep onayında firma lisansı otomatik atanır ve önceki aktif lisanslar askıya alınır.
- Lisans API uçları eklendi:
  - POST /api/v1/license/request
  - GET /api/v1/company/license
  - POST /api/v1/company/license/refresh
- Banka transfer bilgileri için config/license.php eklendi.
## Web Patch v1.0.70
- V4 Faz 1B tamamlandı.
- Admin panelde paket şablonu yönetimi eklendi (Paket Şablonları).
- Admin panelde feature flag yönetimi eklendi (Feature Flagler).
- Admin panelde paket-feature matrisi yönetimi eklendi (Paket Özellik Matrisi).
- Admin panelde firma lisansı yönetimi eklendi (Firma Lisansları):
  - lisans atama
  - aktif etme / askıya alma / süresi doldu / iptal durum aksiyonları
- Admin panelde firma bazlı feature override yönetimi eklendi.
- Lisans durum değişimleri için company_license_events kayıtları admin aksiyonlarında loglanır hale getirildi.
## Web Patch v1.0.69-hotfix1
- Setup ekranı terminalsiz sunucu kullanımı için geliştirildi.
- İşlem türü seçimi eklendi: Migration + Cache Temizle, Sadece Migration, Sadece Cache Temizle.
- Kurulum/bakım mesajları UTF-8 uyumlu hale getirildi.
- Deployment notlarına terminalsiz bakım akışı eklendi.
## Web Patch v1.0.69
- V4 Faz 0B kapsamında landing vitrini genişletildi.
- Ana sayfaya SSS bölümü ve yasal/footer linkleri eklendi.
- Paket kartları Ücretsiz / Gümüş / Altın diliyle güncellendi.
- Hazır / Aktif geliştiriliyor / Yakında etiketleri controller doğrulamasıyla sabitlendi.
- Lisans talep sayfasına kullanıcıya gösterilen form görünümü eklendi (Faz 1C öncesi UI).
## 1.0.67
- Veri kullanım onayı ve aydınlatma akışı eklendi.
- Web ana sayfası, aydınlatma metni ve veri kullanımı sayfaları açıldı.
- Kullanıcı veri kullanım onayı senkronu eklendi.

## Web Patch v1.0.67-hotfix1
- Web POS prototipi eklendi.
- Firma seçerek barkodla satış, web sepeti ve satış tamamlama akışı oluşturuldu.
- Web satış kayıtları ve satış detayı görünümü eklendi.
- Sistem sıfırlama aracı web satış verilerini de temizleyecek şekilde genişletildi.

## Web Patch v1.0.67-hotfix2
- Web POS Blade ayrıştırma hatası giderildi.
- Satışı tamamlama butonu güvenli koşullu render yapısına çevrildi.

## 1.0.66
- Global katalog yönetimi geliştirildi.
- Canonical ürün adı ve ürün grubu düzenleme eklendi.
- Aday isimlerden `Bu ismi kullan` işlemi eklendi.
- Global ürün detayında son 20 firma fiyatı görünümü eklendi.
- barkod.space katalog verisi isim önerisinde en yüksek önceliğe alındı.

## Web Patch v1.0.66-hotfix1 - hotfix8
- Senkron Merkezi, Firma Yaşam Döngüsü ve Sistem Sıfırlama sayfaları sadeleştirildi.
- Türkçe karakter ve görünüm sorunları temizlendi.
- Input ve textarea görünürlüğü artırıldı.
- BOM kaynaklı PHP namespace hataları giderildi.

## 1.0.65
- Android ve web tarafında kapsamlı Türkçe karakter temizliği yapıldı.
- Son kullanıcıya ve admine görünen metinler UTF-8 olarak düzeltildi.
- Dokümanlar temiz Türkçe ile yeniden yazıldı.

## 1.0.64
- Türkçe metin temizliği için ilk geniş düzeltme turu yapıldı.
- Website tabanlı APK güncelleme altyapısı korunarak yeni sürüm yayınlandı.

## 1.0.63
- Cihaz / firma geçmişi modeli eklendi.
- Firma yaşam döngüsü ve etkin olmayan firma temizliği eklendi.

## 1.0.62
- Alt kısım snackbar bildirimleri merkez popup yapısına çevrildi.

## 1.0.61
- Website tabanlı APK güncelleme sistemi eklendi.
- Web panelde sistem sıfırlama aracı eklendi.

## 1.0.60
- Kayıtlı kullanıcı ile önceki firma ilişkisi ve geri yükleme akışı düzeltildi.

## 1.0.59
- Firma geçmişi ve bulut katalog geri yükleme eklendi.
- Şifre sıfırlama başarı ekranı dialog yapısına çevrildi.

## 1.0.58
- SMTP yapılandırması için mail hotfix'i yapıldı.

## 1.0.57
- `Şifremi Unuttum` akışı eklendi.
- Web admin paneline mobil kullanıcılar bölümü eklendi.

## 1.0.56
- Ayarlar ekranı alt menü mantığıyla sadeleştirildi.
- ModeSelection açılış akışı kaldırıldı.
- Kayıtlı kullanıcı için şifre değiştirme ve hesaba bağlı premium senkronu eklendi.

## 1.0.55
- Aktivasyon ekranında farklı firma açma akışı düzeltildi.
- Kayıt ekranında şifre gereksinimi görünür hale getirildi.

## 1.0.54
- Kullanıcı hesabı, oturum seçimi ve kayıtlı kullanıcı temel akışı eklendi.

## 1.0.53 ve öncesi
- Barkod okuma, ürün yönetimi, sepet, satış, rapor, stok, premium ve web senkron altyapısının ilk sürümleri geliştirildi.


































