# KolayKasa V4 Web POS Parity Planı

## Özet

Bu plan, `barkod.space` altında çalışan sistemi tek yol haritasında toplar. Landing page ayrı plan değildir; bu dokümanın `Faz 0` bölümüdür.

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

Bu planın amacı:
- bugünkü mobil uygulamayı korumak
- admin paneli yönetim tarafı olarak bırakmak
- web satış tarafını ayrı ürün alanına dönüştürmek
- lisans, ticket, landing ve paketleri profesyonel hale getirmek
- tüm kararları implementasyon için karar tamam seviyesinde kilitlemek

Durum notu:
- Faz 0A: tamamlandı
- Faz 0B: tamamlandı
- Faz 1A: tamamlandı
- Faz 1B: tamamlandı
- Faz 1C: tamamlandı
- Faz 2A: tamamlandı
- Faz 2B: tamamlandı
- Faz 3: tamamlandı
- Faz 4: tamamlandı
- Faz 5: tamamlandı
- Faz 6: tamamlandı
- Faz 7: tamamlandı
- Faz 8: tamamlandı
- Faz 9: tamamlandı
- Faz 10: tamamlandı
- Faz 11A: tamamlandı
- Faz 11B: tamamlandı
- Faz 12: tamamlandı
- Faz 13A: tamamlandı
- Faz 13B: tamamlandı
- Faz 13: tamamlandı
- Faz 14: tamamlandı

## Dokuman Bakim Notu (2026-03-08)
- Faz durumlari ve kapanis/stabilizasyon bolumu proje genel taramasi sonrasi tekrar dogrulandi.
- Bu dokuman V4 ana referansi olarak guncel tutulur.

## Nihai Ürün Konumu

### Marka
- Ürün adı: `KolayKasa`
- Domain: `barkod.space`
- Kullanım cümlesi: `KolayKasa, barkod.space altyapısıyla çalışır.`

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

## Faz 0 - Landing Page ve Ticari Sunum

### Amaç
Landing page şunları yapacak:
1. ürünü anlatacak
2. APK indirtecek
3. kullanıcı oluşturmaya yönlendirecek
4. misafir başlangıç yolunu gösterecek
5. paketleri sunacak
6. lisans talebi toplayacak
7. web POS vizyonunu gösterecek
8. video ve ekran görüntüsü alanı taşıyacak

### Ana CTA
- `APK İndir`

### Yardımcı CTA'lar
- `Kullanıcı Oluştur`
- `Misafir Olarak Başla`
- `Giriş Yap`

### Hero metni
- Başlık: `KolayKasa ile barkodlu satışa telefondan başlayın`
- Alt metin: `Market, tekel, büfe ve küçük işletmeler için geliştirilen KolayKasa; barkod okutma, ürün yönetimi, stok takibi, raporlar ve bulut destekli senkron altyapısını tek uygulamada sunar.`
- Güven satırı:
  - `Android uygulama hazır`
  - `Bulut senkron hazır`
  - `Web yönetim paneli hazır`
  - `Web POS aktif geliştiriliyor`

### Bölüm sırası
1. Hero
2. Nasıl Çalışır
3. Kullanım Senaryoları
4. Hazır Özellikler
5. Bulut ve Yönetim
6. Paketler
7. Fiş ve Yazdırma
8. Video
9. Ekran Görüntüleri
10. Sık Sorulan Sorular
11. Lisans Başvurusu
12. Footer

### İçerik dürüstlük kuralı
Landing page'de tüm özellikler şu etiketlerden biriyle gösterilecek:
- `Hazır`
- `Aktif geliştiriliyor`
- `Yakında`

### Paket kartı dili
- `Ücretsiz` - `Başlangıç`
- `Gümüş` - `Mobil Pro`
- `Altın` - `Web POS`

## Faz 1 - Firma Lisansı ve Paket Yönetimi

### Lisans bağı
Lisans kullanıcıya veya cihaza değil, `firma`ya bağlanır.

### İlk ödeme modeli
- `Banka transferi`
- online ödeme entegrasyonu yok
- banka dekont / referans açıklaması ile işlem
- admin panelden onay

### Lisans talep akışı
1. kullanıcı landing veya uygulama üzerinden talep oluşturur
2. talep `pending_payment` oluşur
3. kullanıcıya banka bilgisi gösterilir
4. kullanıcı ödeme yapar
5. admin panelde talep incelenir
6. admin firmaya lisans atar
7. lisans `active` olur
8. firma lisansı mobil ve webde görünür

### Paket teknik modeli
Tam serbest paket sistemi yapılmayacak.

Seçilen model:
- `özellik matrisi + paket şablonu + firma override`

### Paket çözümleme kuralı
Bir feature erişimi şu sırayla çözülür:
1. `is_core = true` ise her zaman açık
2. firma override varsa override uygulanır
3. yoksa firmanın aktif paketindeki feature değeri uygulanır

### Paketler
#### `Ücretsiz`
Açık:
- mobil barkod okutma
- ürün ekleme / düzenleme
- ürün listesi
- temel sepet
- temel satış tamamlama
- misafir kullanım
- temel fiyat güncelleme
- temel arama
- cihaz içi kullanım

Kapalı:
- gelişmiş bulut senkron ve hesap kurtarma
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

#### `Gümüş`
Açık:
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
- satır bazlı özel fiyat / yüzde indirim / TL indirim
- ticket sistemi
- premium kurtarma / hesap kurtarma

Kapalı:
- web POS
- çoklu cihaz sekmeleri
- HID barkod okuyucu web desteği
- bekleyen satış web
- ödeme türleri web
- fiş profilleri

#### `Altın`
Açık:
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

### Geçiş kuralı
Mevcut sistemdeki `FREE / PRO` mantığı şu şekilde map edilir:
- mevcut `FREE` -> `FREE`
- mevcut `PRO` -> `SILVER`
- `GOLD` yeni web POS paketi olarak açılır

## Faz 2 - Web POS'u Admin Panelden Ayırma

### Hedef
Satış arayüzünü back office'ten ayırmak.

### Route yapısı
- `/admin`
- `/pos/login`
- `/pos`

### Çıktılar
- ayrı auth
- ayrı layout
- sade POS shell

## Faz 3 - Şube, Register ve POS Session

### Hedef
Gerçek kasa hiyerarşisini kurmak.

### Hiyerarşi
- Firma
- Şube
- Register / Kasa
- POS Session

## Faz 4 - Çoklu Cihaz ve Satış Sekmeleri

### Hedef
Aynı web kasada çoklu telefon, ama her telefon için ayrı sepet.

### Kural
- her bağlı telefon kendi `sale_session` üzerinde çalışır
- ortak tek sepet yapılmaz

## Faz 5 - Mobil Mod Seçimi ve Web Satış Companion Modu (Tamamlandı)

### Giriş sonrası mod seçimi
- `Web üzerinden satış`
- `Mobil üzerinden satış`

### Web üzerinden satış modunda telefonun yapabilecekleri
- barkod okutma
- sepete ürün ekleme
- adet + / -
- satır silme
- özel fiyat
- yüzde indirim
- TL indirim
- satış tamamlama
- yazdırma tetikleme

### Aktif web oturumu yoksa
Gösterilecek ekran:
- `Web üzerinde oturumunuz bulunmuyor`
- `Yeniden Dene`
- `Mobil POS'a Geç`

### Faz 5 Cikti Ozeti
- giris sonrasi satis modu secimi eklendi (`Web uzerinden satis` / `Mobil uzerinden satis`)
- web companion ekrani eklendi (aktif web POS oturumu kontrolu)
- companion fallback eklendi (`Yeniden Dene` / `Mobil POS'a Gec`)
- companion sepet islemleri eklendi:
  - barkod okut ve sepete ekle
  - adet artir / azalt
  - satir sil
  - ozel fiyat
  - yuzde indirim
  - sabit TL indirim
  - fiyat sifirlama
  - satis tamamlama

## Faz 6 - HID Barkod Okuyucu (Tamamlandı)

### Hedef
USB / kablolu barkod okuyucuyu web POS'ta doğal kullanmak.

### Strateji
- sürekli fokus alan barkod input
- Enter ile otomatik ekleme
- tekrar okutma ile adet artışı
- `Tarayıcı hazır` göstergesi

### Faz 6 Cikti Ozeti
- Web POS satis ekranina `Tarayici hazir` durum gosterge paneli eklendi.
- Barkod input icin surekli odak kilidi eklendi (aktif POS oturumunda otomatik fokus geri alma).
- Menu/modal acikken odak kilidi duraklatildi, kapaninca otomatik devam eder hale getirildi.
- Odak barkod kutusunda degilken HID tarayicidan gelen tuslar gecici buffer'da toplanir hale getirildi.
- HID tarayici Enter gonderdiginde barkod otomatik olarak sepete eklenir hale getirildi.
- Barkod okuma sirasinda durum metni `Tarama isleniyor...` olarak anlik guncellenir.

## Faz 7 - Bekleyen Satış / Açık Fiş (Tamamlandı)

### Hedef
Satış beklet / geri aç akışını tamamlamak.

### Faz 7 Çıktı Zorunluluğu (Atlanmayacak)
- Web POS menüsündeki `Satışları Fişle Gör` girdisi bu fazda aktif edilir
- bekleyen/açık fiş listesi menüden erişilebilir olur
- satış detayı fiş görünümü ile açılabilir

### Faz 7 Cikti Ozeti
- Web POS ekranina `Satisi Beklet` aksiyonu eklendi.
- Bekletilen satis sekmesi `held` durumuna gecirilip otomatik yeni aktif sekmeye donulur hale getirildi.
- Bekleyen satislar ana POS ekraninda listelenir ve tek tikla geri acilir hale getirildi.
- `/pos/receipts` ekrani eklendi:
  - bekleyen satislar listesi
  - `Geri Ac` aksiyonu
  - `Sil` aksiyonu
  - tamamlanan satis fisleri listesi
- `Satışları Fişle Gör` topbar menusu aktif edildi.
- `/pos/receipts/{webSale}` fis detay ekrani eklendi ve yazdir butonu acildi.

## Faz 8 - Ödeme Türleri (Tamamlandı)

### İlk sürüm
- `nakit`
- `kart`
- `diğer`

### Faz 8 Cikti Ozeti
- Web POS checkout ekranina odeme turu secimi eklendi (`Nakit / Kart / Diger`).
- Checkout isteginde secilen odeme turu dogrulanir hale getirildi.
- `web_sale_payments` tablosu eklendi ve satis tamamlandiginda odeme kaydi olusturulur hale getirildi.
- Son satislar ve fis ekranlarinda odeme turu gorunur hale getirildi.

## Faz 9 - Fiş Yazdırma Altyapısı (Tamamlandı)

### İlk sürüm
Web:
- tarayıcıdan yazdır
- PDF yazdır / kaydet
- 58mm termal şablon
- 80mm termal şablon
- A4 şablon

Mobil:
- web üzerinden yazdır
- Android paylaş / yazdır fallback

### İlk sürüm kapsam dışı
- resmi mali belge
- ESC/POS ham çıktı
- sürükle-bırak fiş editörü
- tüm yazıcılara native raw profil

### Faz 9 Cikti Ozeti
- Web POS fis ekranina kagit tipi secimi eklendi (`58mm / 80mm / A4`).
- Web POS fis ekraninda `Yazdir` ve `PDF Kaydet` aksiyonlari aktif edildi.
- `paper`, `output` ve `autoprint` query parametreleri ile fis gorunumu kontrol edilir hale getirildi.
- Mobil companion `POST /api/v1/mobile/web-sale/print` endpointi signed fis URL uretecek sekilde gelistirildi.
- Signed public fis rotasi eklendi: `GET /pos/receipts/public/{webSale}`.
- Mobil companion tarafinda yazdirma tetigi artik dis baglanti acarak web yazdirma/PDF akisina gecis yapar hale getirildi.

## Faz 10 - Firma Fiş Ayarları (Tamamlandı)

### Hedef
Firma bazlı fiş profili yönetimi.

### Faz 10 Cikti Ozeti
- `receipt_profiles` veri modeli eklendi (firma + sube bazli varsayilan profil).
- Web POS topbar menusu altina `Fis Ayarlari` modal akisi eklendi.
- Fis profili alanlari:
  - kagit tipi (`58mm / 80mm / A4`)
  - yazdirma modu (`Tarayici Yazdirma / PDF Oncelikli`)
  - baslik/alt bilgi satirlari
  - gorunur alanlar (firma, vergi, odeme, barkod, tarih, kasa)
- Fis goruntuleme ve yazdirma ekrani profile baglandi:
  - varsayilan kagit tipi profilden okunur
  - secili alanlar profile gore gizlenir/gosterilir
  - baslik/alt bilgi satirlari fis uzerinde render edilir
- Mobil `web-sale/print` endpointi varsayilan kagit tipini firma fis profilinden alir hale getirildi.

## Faz 11 - Ticket Sistemi

### Amaç
Kullanıcı:
- hata bildirsin
- özellik istesin
- genel geri bildirim versin

Admin:
- admin panelden tüm ticket'ları yönetsin
- yanıt versin
- ticket durumunu güncellesin

### İlk sürüm kaynakları
- mobil uygulama
- web POS

### Türler
- `bug`
- `feature_request`
- `general`

### Durumlar
- `new`
- `reviewing`
- `answered`
- `closed`

### Görünürlük
Ticket ve yanıtlar aynı firmadaki tüm kullanıcılar tarafından görülebilir.

### Faz 11A Cikti Ozeti (Tamamlandı)
- Ticket veri modeli eklendi:
  - `feedback_reports`
  - `feedback_messages`
  - `feedback_attachments`
- Mobil/web API tabanı eklendi:
  - `GET /api/v1/support/inbox`
  - `POST /api/v1/support/tickets`
  - `GET /api/v1/support/tickets/{ticketId}`
  - `POST /api/v1/support/tickets/{ticketId}/reply`
  - `POST /api/v1/support/tickets/{ticketId}/reopen`
  - `POST /api/v1/support/tickets/{ticketId}/attachments`
- Yetki kuralı:
  - bearer token ile aktif mobil kullanıcı doğrulanır
  - ticket erişimi kullanıcı veya aynı firmaya bağlı sahiplik ile sınırlandırılır
- Misafir ticket kuralı:
  - token yoksa ticket açmada `companyCode + deviceUid` zorunlu

### Faz 11B Cikti Ozeti (Tamamlandı)
- Web POS topbar menu altina `Destek Gelen Kutusu` ve `Yeni Ticket` akislari eklendi.
- Web POS'ta ticket modal modulu eklendi:
  - inbox listeleme ve durum filtresi
  - ticket detay/mesaj akisi
  - yeni ticket olusturma
  - yanit gonderme
  - kapali ticketi yeniden acma
- POS session guvenligi ile calisan web endpointleri eklendi:
  - `GET /pos/support/inbox`
  - `GET /pos/support/tickets/{ticketId}`
  - `POST /pos/support/tickets`
  - `POST /pos/support/tickets/{ticketId}/reply`
  - `POST /pos/support/tickets/{ticketId}/reopen`
- Admin panelde `Ticket Merkezi` kaynagi eklendi:
  - ticket listeleme/filtreleme
  - admin yaniti ekleme
  - durum guncelleme (kapat/yeniden ac)
  - detay/inceleme sayfalari
- Mobil uygulamada `Destek ve Geri Bildirim` ekrani eklendi:
  - ayarlardan erisim
  - gelen kutusu + durum filtresi
  - yeni ticket olusturma
  - ticket mesajlasma ve yeniden acma

## Faz 12 - Personel ve Rol Sistemi

### Hedef
Owner / manager / cashier ayrımı.

### Faz 12 Çıktı Zorunluluğu (Atlanmayacak)
- Web POS menüsündeki `Cihazları Yönet` girdisi bu fazda aktif edilir
- cihaz/oturum bazlı yetki ve görünürlük kuralları rol modeline bağlanır

### Faz 12 Cikti Ozeti (Tamamlandi)
- `company_staff_roles` tablosu eklendi (firma bazli personel rol modeli).
- Rol modeli aktif edildi: `owner / manager / cashier`.
- POS auth ve middleware rol farkindalikli hale getirildi:
  - owner disi personel atamalariyla web POS girisi acildi
  - firma erisiminde rol kontrolu zorunlu hale getirildi
- Web POS role bagli yetki kurallari eklendi:
  - kasa/sube/oturum yonetimi
  - firma ve fis ayarlari
  - gecmis satis fis duzenleme/silme
- Web POS topbar menusu altindaki `Cihazlari Yonet` menusu aktif edildi.
- `Cihaz ve Oturum Yonetimi` modal akisi eklendi:
  - bagli cihaz listesi + aktif/pasif degistirme
  - POS oturum listesi + yetkili rolde oturum kapatma
  - personel rol listesi
  - owner rolunde personel ekle/guncelle/sil
- Mobil web companion tarafinda firma erisim kontrolu personel rollerini destekler hale getirildi.

## Faz 13 - Senkron ve Veri Birleştirme

### Hedef
Web ve mobil tam parity.

### Faz 13A Cikti Ozeti (Tamamlandi)
- Mobil kullanici tokeni ile erisilen yeni artimli katalog endpointi eklendi:
  - `GET /api/v1/auth/companies/{companyCode}/catalog/changes`
- Firma erisim kontrolu owner + aktif personel rolunu (manager/cashier) kapsayacak sekilde birlestirildi.
- Tam katalog endpointi role/erisim kurallarina alinip `cursor` bilgisi dondurur hale getirildi.
- Mobil senkron cekirdegi gelistirildi:
  - outbox push sonrasi buluttan katalog degisiklikleri cekilir
  - degisiklikler local Room urun tablosuna cakismaz sekilde merge edilir
  - `updatedAt` bazli cursor ile artimli cekim yapilir
  - eski kayitlarin ustune daha yeni bulut kayitlari yazilir, daha yeni lokal kayit korunur
- Yeni ayar anahtarlari eklendi:
  - `sync.catalog.cursor`
  - `sync.catalog.cursor.company`

### Faz 13B Cikti Ozeti (Tamamlandi)
- Mobil POS satislarinin buluta aktarimi guvenli outbox fallback modeliyle guclendirildi.
- Mobil satis publish akisi:
  - internet varsa anlik publish
  - internet/yol hatasi varsa `LOCAL_SALE_PUBLISH` eventi olarak outbox'a alinma
  - worker ile arka planda yeniden deneme
- Senkron worker outbox event tiplerini ayirir hale getirildi:
  - `PRODUCT_UPSERT` -> toplu katalog batch
  - `LOCAL_SALE_PUBLISH` -> tekil satis publish API
- Basarisiz eventler event bazli hata mesaji ile `FAILED` durumuna dusurulur.

## Faz 14 - Web POS Üst Menüleşme ve Hızlı Yönetim (Tamamlandı)

### Hedef
Satış ekranındaki üst kontrol alanını sadeleştirip yönetim işlemlerini topbar menüsüne taşımak.

### Çıktılar
- satış ekranındaki üst kontrol bloğu topbar menüsüne taşınır
- topbar menüsünde firma, şube/kasa, POS oturumu ve çıkış yönetimi bulunur
- menü altında yol haritası girişleri görünür:
  - `Cihazları Yönet`
  - `Satışları Fişle Gör`
- ilgili fazlar açıldığında kullanıcı menüden direkt erişebilir

## Test Senaryoları

### Landing
1. Ana sayfa açılır
2. `APK İndir` çalışır
3. `Kullanıcı Oluştur` görünür
4. `Misafir Olarak Başla` görünür
5. Paket kartları görünür
6. `Altın` paketi `Yakında` olarak işaretlenir
7. Lisans başvuru formu açılır
8. Footer yasal linkleri çalışır

### Lisans
9. Kullanıcı lisans talebi bırakır
10. Talep `pending_payment` oluşur
11. Admin panelde görünür
12. Admin firmaya lisans atar
13. Firma lisans seviyesi mobil ve webde görünür
14. Override ile tek bir özellik açılıp kapanabilir

### Mobil mod seçimi
15. Giriş sonrası mod seçimi açılır
16. `Mobil üzerinden satış` mevcut akışı açar
17. `Web üzerinden satış` companion akışı açar

### Web POS
18. Kullanıcı `/pos/login` ile giriş yapar
19. Firma / şube / register seçer
20. Barkod input HID okuyucudan veri alır
21. Manuel barkod girişi çalışır
22. Satış tamamlanır

### Mobil companion
23. Aynı hesapla telefon web moduna girer
24. Aktif web POS varsa bağlanır
25. Barkod okutur
26. Kendi sekmesine ürün düşer
27. Adet / fiyat / indirim çalışır
28. Satış tamamlanır

### Ticket
29. Mobilde yeni ticket açılır
30. Web POS'ta yeni ticket açılır
31. Admin panelde görünür
32. Admin cevap verir
33. Mobil gelen kutusunda rozet görünür
34. Web POS gelen kutusunda rozet görünür
35. Aynı firmadaki ikinci kullanıcı cevabı görebilir
36. Ticket yeniden açılabilir

## Açık Varsayımlar
1. Ürün adı `KolayKasa`
2. Domain `barkod.space`
3. Landing ana CTA `APK İndir`
4. Paketler `Ücretsiz / Gümüş / Altın`
5. Lisans firma bazlıdır
6. İlk ödeme modeli banka transferidir
7. Lisans akışı talep + bekleme + admin onayıdır
8. Paket sistemi admin panelden yönetilebilir ama tam serbest değildir
9. Teknik paket kodları sabit kalır: `FREE`, `SILVER`, `GOLD`
10. Mobil uygulama iki modludur
11. Telefon web modunda tam yardımcı POS olur
12. Aynı web kasaya çoklu telefon bağlanabilir
13. Her telefon ayrı sepet kullanır
14. Aktif web oturumu yoksa kullanıcı `Yeniden Dene / Mobil POS'a Geç` ekranını görür
15. Teknik veri otoritesi sunucudur
16. İlk ödeme türleri `nakit / kart / diğer`
17. İlk fiş sistemi resmi mali belge değildir
18. İlk yazdırma stratejisi `tarayıcı yazdırma + mobil paylaş/yazdır`
19. Ticket sistemi ilk sürümde yalnızca mobil ve web POS içinden açılır
20. Ticket yanıtları aynı firmadaki tüm kullanıcılar tarafından görülebilir

## V4 Kapanış/Stabilizasyon Turu (Planlanan)

Bu bölüm V4 geliştirmesi tamamlandıktan sonra yapılacak kapanış işlerini tanımlar.

1. Uçtan uca regresyon testi
- Mobil POS
- Web POS
- Companion
- Senkron akışları

2. Senkron dayanıklılık doğrulaması
- Failed outbox kayıtlarının gözlenmesi
- Retry davranışının doğrulanması
- Hata sonrası veri tutarlılığı kontrolü

3. Release hazırlığı
- İmzalı APK/AAB üretimi
- Yayın notlarının son hali
- Son patch ve dağıtım doğrulaması

