# KolayKasa Proje Kuralları

Bu doküman, projede bugüne kadar belirlenen teknik ve iş kurallarını tek yerde toplar.
Amaç: geliştirme sırasında dağınık kararları tek referansta görmek.

## 1) Kaynak Doküman Kuralı

Bu özet aşağıdaki aktif dokümanlardan derlenmiştir:
- `dokumanlar/uygulama-dokumanlari/program özellikleri.md`
- `dokumanlar/uygulama-dokumanlari/teknik-mimari.md`
- `dokumanlar/uygulama-dokumanlari/room-entity-plani.md`
- `dokumanlar/uygulama-dokumanlari/ekran-akis-diyagrami.md`
- `dokumanlar/uygulama-dokumanlari/codex-gelistirme-plani.md`
- `dokumanlar/uygulama-dokumanlari/gelistirme-fazlari-v3.md`
- `dokumanlar/uygulama-dokumanlari/gelistirme-fazlari-v4-web-pos-parity.md`
- `dokumanlar/web-dokumanlari/web-veri-modeli.md`
- `dokumanlar/web-dokumanlari/web-api-sozlesmesi.md`
- `dokumanlar/web-dokumanlari/aktivasyon-akisi.md`
- `dokumanlar/web-dokumanlari/deployment-notlari.md`
- `web-application/update/README.md`
- `web-application/update/_indeks/patch-indeksi.md`
- `web-application/update/_sablonlar/PATCH-README-SABLONU.md`

Not:
- `dokumanlar/oturum-kayitlari/` arşivdir; teknik kural kaynağı değildir.

## 2) Ürün ve Kapsam Kuralı

- Ürün adı: `KolayKasa`.
- Domain: `barkod.space`.
- Ana katmanlar: `Landing`, `Back Office (Admin)`, `Web POS`, `Mobil POS`.
- Bilerek kapsam dışı kalanlar:
  - resmi mali entegrasyon
  - CRM / sadakat sistemi
  - kampanya motoru
  - otomatik online ödeme altyapısı (ilk sürüm)

## 3) Mimari ve Teknoloji Kuralı

Android tarafı:
- Kotlin + Jetpack Compose
- MVVM + Repository + UseCase
- Room + Hilt
- Coroutines / Flow
- ML Kit Barcode Scanning + ML Kit Text Recognition
- Retrofit/OkHttp tabanlı ağ

Web tarafı:
- Laravel 12 + Filament Admin
- MySQL

Genel:
- Katman zorunluluğu: `UI -> ViewModel -> UseCase -> Repository -> Data Source`.
- Sistem offline-first yaklaşımıyla tasarlanır.
- Kullanıcıya görünen metinlerde Türkçe UTF-8 uyumu korunur.

## 4) Finans ve Hesaplama Kuralı

- Para değerleri `Long kuruş` olarak tutulur.
- Fiyat/satış hesaplarında `Double` kalıcı depolama için kullanılmaz.
- Yüzde değişim sonrası sonuç tam TL’ye **yukarı yuvarlanır**.
- Satış tamamlama akışı tutarlı ve atomik olmalıdır.
- Satış/veri modelinde kurus snapshot alanları kullanılır (sale/item/line/profit).

## 5) Aktivasyon, Oturum ve Senkron Kuralı

- Aktivasyon firma bazlıdır.
- Firma kodu sunucuda üretilir.
- Tek firma için tek aktif cihaz kuralı uygulanır.
- Aynı cihazın firma geçmişi tutulur ve farklı firma denemesinde kullanıcı uyarılır.
- Katalog senkronu toplu işlenir; aynı `eventUuid` tekrar işlenmez (dedup).
- Uygulama tarafında outbox/kuyruk mantığı korunur.
- Bulut katalog geri yükleme seçenekleri sunulur:
  - yerel kataloğa ekle
  - yereli sil ve bulutu yükle

## 6) Barkod, Ürün ve Satış Akışı Kuralı

- Barkod format desteği: EAN-13, EAN-8, UPC-A, UPC-E.
- Ürün bulunamadığında ürün ekleme akışı açılır.
- İsim önerisi best-effort çalışır; başarısız olursa manuel giriş serbesttir.
- OCR destekli ambalaj metni ile ürün adı üretimi desteklenir.
- Sepet/satış tarafında satır bazlı fiyat ve indirim akışları korunur.

## 7) Lisans ve Paket Kuralı (V4)

- Lisans kullanıcıya/cihaza değil `firmaya` bağlıdır.
- İlk ödeme modeli: banka transferi + admin onayı.
- Teknik paket kodları sabittir:
  - `FREE`
  - `SILVER`
  - `GOLD`
- Paket çözümleme sırası sabittir:
  1. `is_core = true` ise her zaman açık
  2. firma override varsa override uygulanır
  3. yoksa aktif paket özelliği uygulanır
- Geçiş kuralı sabittir:
  - `FREE -> FREE`
  - `PRO -> SILVER`
  - `GOLD` web POS katmanı

## 8) Landing ve Dürüstlük Kuralı (V4 Faz 0)

- Ana CTA: `APK İndir`.
- Yardımcı CTA:
  - `Kullanıcı Oluştur`
  - `Misafir Olarak Başla`
  - `Giriş Yap`
- Landing etiketleri sadece şu üçlü ile gösterilir:
  - `Hazır`
  - `Aktif geliştiriliyor`
  - `Yakında`
- Hazır olmayan özellikler “Hazır” gibi sunulmaz.

## 9) Web POS Ayrımı Kuralı (V4)

- Web POS admin panelden ayrı route/auth yapısında ilerler:
  - `/admin`
  - `/pos/login`
  - `/pos`
- Çoklu cihaz modelinde her telefon ayrı sepet (`sale_session`) kullanır.
- Ortak tek sepet kuralı yoktur.
- Aktif web oturumu yoksa mobilde fallback ekranı gösterilir.

## 10) Patch, Sürüm ve Dağıtım Kuralı

- Web patch klasör standardı:
  - normal: `vX.Y.Z`
  - hotfix: `vX.Y.Z-hotfixN`
- Patch içine yalnız değişen dosyalar konur.
- Android sürümü değiştiyse patch içinde zorunlu:
  - `public/app-updates/android/latest.json`
  - yeni APK dosyası
- Her patch için `README.md` tutulur ve şu bilgiler yazılır:
  - migration var/yok
  - setup gerekli/gerekmiyor
  - APK güncellemesi var/yok
- Patch listesi `web-application/update/_indeks/patch-indeksi.md` içinde güncel tutulur.
- Sunucu deployment’ında patch dosyaları aynı relatif yollarla yüklenir.

## 11) Doküman ve Test Yönetimi Kuralı

- Özellik takibi `program özellikleri.md` dosyasında yapılır.
- Tamamlanan maddeler `[✔ IMPLEMENTED]` ile işaretlenir.
- Test matrisi `kapsamli-test-listesi.md` üzerinden yürütülür.
- Risk/iyileştirme izleme:
  - `tarama-sonuclari.md`
  - `test-sonrasi-is-listesi.md`
- Web API kapsamı için `web-api-sozlesmesi.md` referanstır.

## 12) V4 Çalışma Disiplini (Orta-Faz / Timebox)

- Ağır fazlar alt fazlara bölünerek ilerletilir (`0A/0B`, `1A/1B/1C` vb.).
- Her tur tek alt faz hedefiyle yürütülür.
- Her tur sonunda zorunlu rapor verilir:
  - ne eklendi
  - ne eksik kaldı
  - test/check sonucu
  - bir sonraki tur hedefi

## 13) Is Sonrasi Raporlama Kurali

Her kod/guncelleme turu sonunda rapor zorunludur.
Rapor, asagidaki sira ve basliklarla verilir:

1. `Faz Durumu`
- Ornek: `Faz 5 tamamlandi` veya `Faz 1A kismen tamamlandi`.

2. `Yapilanlar`
- Islenen ekranlar, akislar, API uclari, migration ve davranis degisiklikleri.

3. `Guncellenen Dokumanlar`
- Degisen dokuman dosyalari acik liste halinde yazilir.

4. `Surum`
- Uygulama/web surumu degistiyse yeni surum belirtilir.
- Sadece dokuman veya planlama degisikliginde surum artisi zorunlu degildir.

5. `APK` (varsa)
- Uretilen APK dosya yolu verilir.

6. `Web Guncelleme Dosyalari` (varsa)
- `latest.json`
- APK dagitim dosyasi
- gerekli diger dagitim dosyalari

7. `Web Patch`
- Olusturulan patch klasoru acik yazilir (`vX.Y.Z` veya `vX.Y.Z-hotfixN`).

8. `Onemli Notlar`
- Migration, setup, cache temizleme gibi kritik adimlar acikca belirtilir.

9. `Yapman Gerekenler`
- Sunucuda uygulanacak adimlar sirasiyla ve net komut/aksiyon olarak yazilir.

10. `Dogrulama`
- Derleme/syntax/route/test sonuc ozeti verilir.
- Calistirilamayan test varsa nedeni mutlaka belirtilir.

## 14) Asistan Calisma Tarzi Kurali

Bu bolum, gelistirme sirasinda izlenecek calisma davranisini tanimlar.

1. Istek anlama
- Her yeni istekte once hedef netlestirilir, sonra ilk adim belirtilir.
- Belirsizlik varsa varsayim acik yazilir.

2. Parcali ilerleme
- Buyuk isler tek parca degil, faz/alt faz olarak ilerletilir.
- Her turda tek net hedefle calisilir.

3. Ara bilgilendirme
- Islem devam ederken duzenli ara durum bilgisi verilir.
- Uzun sureli islerde "hangi asamadayim" net yazilir.

4. Dosya degisikligi disiplini
- Dosya editlemeden once neyin degisecegi belirtilir.
- Istek disi ozellik eklenmez.
- Ilgisiz dosyalara mudahale edilmez.

5. Guvenli teknik davranis
- Destructive komut kullanilmaz (`reset --hard`, toplu geri alma vb.).
- Mevcut kullanici degisiklikleri izinsiz geri alinmaz.
- Hata veya beklenmeyen durum varsa durdurup net rapor verilir.

6. Surum ve patch disiplini
- Kod degisikliklerinde surum/patch kurali uygulanir.
- Hotfix gerekli ise `vX.Y.Z-hotfixN` modeli kullanilir.
- Sadece planlama/dokuman guncellemesinde surum artisi zorunlu degildir.

7. Test ve dogrulama
- Mumkun olan syntax/build/route/migration kontrolleri calistirilir.
- Calistirilmayan kontrol varsa nedeni rapora eklenir.

8. Is sonu teslim formati
- 13. bolumdeki rapor formati zorunlu olarak kullanilir.
- Rapor sonunda bir sonraki net adim onerilir.

## 15) Codex Tarafindan Olusturulan Kurallar

Bu bolum, tum aktif markdown dokumanlarinin taranmasindan sonra cikarilan
uygulama/disiplin kurallarini toplar.

1. Kural oncelik sirasi
- Bir celiski olursa su oncelik uygulanir:
  - `program ozellikleri.md`
  - `teknik-mimari.md` + `room-entity-plani.md`
  - `web-veri-modeli.md` + `web-api-sozlesmesi.md`
  - `gelistirme-fazlari-v4-web-pos-parity.md`
  - diger tarihsel faz/changelog dosyalari

2. Planlanan ile aktif olanin ayrimi
- Dokumanda "eklenecek" / "V4 icin" yazan alanlar aktif kabul edilmez.
- Aktiflik sadece implementasyon ve testten sonra raporda yazilir.

3. Ozellik isaretleme disiplini
- `program ozellikleri.md` dosyasinda sadece gercekten biten maddeler
  `[✔ IMPLEMENTED]` olarak isaretlenir.
- Kismen biten madde implement edildi gibi isaretlenmez.

4. Dokuman tutarliligi
- Her kod turunda ilgili dokumanlar senkron guncellenir:
  - `changelog.md`
  - gerekiyorsa faz dosyasi
  - gerekiyorsa `web-api-sozlesmesi.md` / `web-veri-modeli.md`

5. Patch paket disiplininin zorunlulugu
- Web kod degisikliginde patch klasoru zorunludur.
- Patchte sadece degisen dosyalar bulunur.
- Patch `README.md` zorunlu kabul edilir.
- `_indeks/patch-indeksi.md` guncel tutulur.

6. Hotfix adlandirma disiplini
- Ayni surumde duzeltme gerekiyorsa mutlaka:
  - `vX.Y.Z-hotfixN`
- Normal patchle hotfix karistirilmaz.

7. Surum artirma disiplini
- Kod degisikligi varsa surum/patch artirilir.
- Sadece planlama veya dokuman duzenlemesinde surum artisi zorunlu degildir.

8. Deployment adimlarini yazma zorunlulugu
- Her web patch raporunda sunucuya uygulanacak adimlar acik yazilir.
- Migration/setup gerekiyorsa atlanmadan belirtilir.

9. Dogrulama kaniti zorunlulugu
- Mumkun olan kontroller raporda yer alir:
  - syntax
  - route list
  - migration/baglanti kontrolu
  - build/test
- Calistirilmayan kontrol varsa neden rapora yazilir.

10. Finans kurali ihlal edilemez
- Para alanlari kurus bazli long modelinde kalir.
- Yuvarlama kurali ve fiyat hesap mantigi dokuman disina cikmaz.

11. Aktivasyon/senkron kurali ihlal edilemez
- Tek firma tek aktif cihaz, dedup, outbox ve geri yukleme akislari korunur.
- Aktivasyon davranisi dokumandaki akisa uygun kalir.

12. API sozlesme disiplini
- Yeni endpoint eklenince API dokumani guncellenir.
- Endpoint kaldirilirsa etkisi raporda acikca belirtilir.

13. UI metin ve encoding disiplini
- Kullaniciya gorunen metinler UTF-8 uyumlu kalir.
- Bozuk karakter gorulurse duzeltme patchi ayrica acilir.

14. Faz-timebox disiplini
- Agir isler alt faza bolunur.
- Tek turda tek alt faz hedefi korunur.
- Tur sonu rapor sabittir (13. bolum formati).

15. Arsiv dosyalari kaynak kural degildir
- `dokumanlar/oturum-kayitlari/` tarihsel kayittir.
- Teknik karar kaynagi olarak sadece aktif dokumanlar kullanilir.

---

Bu dosya “kural özeti”dir.
Detay teknik alan, endpoint, tablo ve faz içerikleri için kaynak dokümanlara gidilir.
