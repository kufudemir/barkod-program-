# KolayKasa Özellikleri

## Doküman Senkron Notu (2026-03-08)
- Proje dökümanları baştan sona tarandı ve bu dosya güncellendi.
- Uygulama sürüm referansı: 1.03.01 (market-pos-app/build.gradle.kts).
- Web API controller sayısı: 22 (web-application/app/Http/Controllers/Api).
- Web migration dosya sayısı: 31 (web-application/database/migrations).
- Android feature modül sayısı: 8 (market-pos-app/src/main/java/com/marketpos/feature).
- Bu güncellemede özellik işaretlerinde ([✔ IMPLEMENTED]) varsayımsal değişiklik yapılmadı; açık maddeler korunmuştur.

## Genel Sistem
- Tek cihaz, offline-first çalışma [✔ IMPLEMENTED]
- Room veritabanı [✔ IMPLEMENTED]
- MVVM + Repository + UseCase yapısı [✔ IMPLEMENTED]
- Hilt ile bağımlılık yönetimi [✔ IMPLEMENTED]
- Jetpack Compose arayüz [✔ IMPLEMENTED]
- Açık / koyu tema [✔ IMPLEMENTED]
- barkod.space ile merkez web senkron altyapısı [✔ IMPLEMENTED]
- Website üzerinden APK sürüm kontrolü ve güncelleme bildirimi [✔ IMPLEMENTED]
- KolayKasa marka dönüşümü [✔ IMPLEMENTED]

## Oturum ve Hesap
- Misafir kullanım [✔ IMPLEMENTED]
- E-posta / şifre ile kayıt olma [✔ IMPLEMENTED]
- E-posta / şifre ile giriş yapma [✔ IMPLEMENTED]
- Oturumu kapatma [✔ IMPLEMENTED]
- Şifre değiştirme [✔ IMPLEMENTED]
- E-posta kodu ile şifre sıfırlama [✔ IMPLEMENTED]
- Şifre sıfırlama sonrası onay ekranı [✔ IMPLEMENTED]
- Hesaba bağlı premium senkronu [✔ IMPLEMENTED]
- Hesaba bağlı firma geçmişi [✔ IMPLEMENTED]
- Cihaz geçmişindeki firmaları listeleme [✔ IMPLEMENTED]
- Veri kullanım onayı sürüm takibi [✔ IMPLEMENTED]
- Hesaba bağlı veri kullanım onayı senkronu [✔ IMPLEMENTED]
- Giriş sonrası satış modu seçimi [✔ IMPLEMENTED]

## Aktivasyon ve Senkron
- Firma ünvanı ile aktivasyon [✔ IMPLEMENTED]
- Firma kodunun sunucuda otomatik üretilmesi [✔ IMPLEMENTED]
- Tek firma için tek aktif cihaz kuralı [✔ IMPLEMENTED]
- Ürün değişikliklerini kuyruklayarak web'e gönderme [✔ IMPLEMENTED]
- Mobil satış publish fallback kuyruğu ve otomatik retry [✔ IMPLEMENTED]
- Elle `Şimdi Senkronize Et` işlemi [✔ IMPLEMENTED]
- Bulut katalog geri yükleme [✔ IMPLEMENTED]
- Yerel kataloğa ekle / yereli sil ve bulutu yükle seçenekleri [✔ IMPLEMENTED]
- Buluttan artımlı katalog değişikliği çekme ve local merge [✔ IMPLEMENTED]
- Web ve mobil tam parity [✔ IMPLEMENTED]

## Barkod ve Tarama
- ML Kit ile barkod okuma [✔ IMPLEMENTED]
- EAN-13, EAN-8, UPC-A, UPC-E desteği [✔ IMPLEMENTED]
- Flaş aç / kapat [✔ IMPLEMENTED]
- Tarama kutusu boyutu ayarı [✔ IMPLEMENTED]
- Seri tarama [✔ IMPLEMENTED]
- Seri taramada bekleme süresi ayarı [✔ IMPLEMENTED]
- Seri taramada son ürün ve mini sepet özeti [✔ IMPLEMENTED]
- HID barkod okuyucu web desteği [✔ IMPLEMENTED]

## Ürün Yönetimi
- Ürün ekleme [✔ IMPLEMENTED]
- Ürün düzenleme [✔ IMPLEMENTED]
- Ürün silme [✔ IMPLEMENTED]
- Barkod alanını elle değiştirme [✔ IMPLEMENTED]
- Not alanı [✔ IMPLEMENTED]
- Ürün grubu [✔ IMPLEMENTED]
- Satış fiyatı, alış fiyatı, stok, minimum stok [✔ IMPLEMENTED]
- Hedef kâr marjı yardımı [✔ IMPLEMENTED]
- Son 30 güne göre minimum stok önerisi [✔ IMPLEMENTED]
- Ürün listesi arama ve filtreleme [✔ IMPLEMENTED]
- Hızlı fiyat ve stok düzenleme [✔ IMPLEMENTED]
- Global katalogtan doğrulanmış isim ve grup çekme [✔ IMPLEMENTED]

## İsim Önerme
- barkod.space global katalog önerisini önceliklendirme [✔ IMPLEMENTED]
- BarkodBankası öncelikli isim önerme [✔ IMPLEMENTED]
- Web sonuç analizi ile yedek öneri [✔ IMPLEMENTED]
- 5 adet alternatif öneri [✔ IMPLEMENTED]
- Ambalajdan OCR ile metin alma [✔ IMPLEMENTED]
- OCR kelime seçimi ile özel ürün adı oluşturma [✔ IMPLEMENTED]

## Sepet ve Satış
- Sepete ekleme [✔ IMPLEMENTED]
- Adet artırma / azaltma [✔ IMPLEMENTED]
- Sepetten çıkarma [✔ IMPLEMENTED]
- Sepeti bekletme ve geri getirme [✔ IMPLEMENTED]
- Satış tamamlama [✔ IMPLEMENTED]
- Satır bazlı özel fiyat [✔ IMPLEMENTED]
- Yüzde indirim [✔ IMPLEMENTED]
- Sabit TL indirim [✔ IMPLEMENTED]
- Satışta tam TL yukarı yuvarlama [✔ IMPLEMENTED]
- Web POS [✔ IMPLEMENTED]
- Çoklu cihaz sekmeleri [ ]
- Web POS satış sekmeleri (sale_session) [✔ IMPLEMENTED]
- Bekleyen satış / açık fiş [✔ IMPLEMENTED]
- Ödeme türleri [✔ IMPLEMENTED]
- Fiş yazdırma [✔ IMPLEMENTED]

## Stok ve Rapor
- Kritik stok görünümü [✔ IMPLEMENTED]
- Stok takibi ekranı [✔ IMPLEMENTED]
- Hızlı stok artır / azalt [✔ IMPLEMENTED]
- Toplu stok güncelleme [✔ IMPLEMENTED]
- Stok sayım modu [✔ IMPLEMENTED]
- Sayım fark raporu [✔ IMPLEMENTED]
- Satış raporları [✔ IMPLEMENTED]
- Özel tarih aralığı [✔ IMPLEMENTED]
- Saat bazlı satış [✔ IMPLEMENTED]
- Satış geçmişi ve satış detayı [✔ IMPLEMENTED]

## Lisans ve Paketler
- Firma bazlı lisans modeli [✔ IMPLEMENTED]
- Ücretsiz / Gümüş / Altın paketleri [✔ IMPLEMENTED]
- Banka transferi ile lisans talebi [✔ IMPLEMENTED]
- Admin panelden paket şablon yönetimi [✔ IMPLEMENTED]
- Admin panelden firma lisansı atama [✔ IMPLEMENTED]
- Firma bazlı feature override [✔ IMPLEMENTED]

## Premium ve Hak Yönetimi
- Lisans kodu ile premium açma [✔ IMPLEMENTED]
- Deneme süresi [✔ IMPLEMENTED]
- Premium ekranı [✔ IMPLEMENTED]
- Hesap bazlı premium kurtarma [✔ IMPLEMENTED]
- FREE / SILVER / GOLD hak modeli [ ]

## Web Yönetim Paneli
- Admin panel giriş sistemi [✔ IMPLEMENTED]
- Açık web ana sayfası [✔ IMPLEMENTED]
- Aydınlatma metni sayfası [✔ IMPLEMENTED]
- Veri kullanımı sayfası [✔ IMPLEMENTED]
- Firmalar [✔ IMPLEMENTED]
- Cihazlar [✔ IMPLEMENTED]
- Global katalog [✔ IMPLEMENTED]
- Canonical ürün adı düzenleme [✔ IMPLEMENTED]
- Global ürün grubu düzenleme [✔ IMPLEMENTED]
- Aday isimlerden `Bu ismi kullan` işlemi [✔ IMPLEMENTED]
- Global ürün detayında son 20 firma fiyatı [✔ IMPLEMENTED]
- Firma fiyatları [✔ IMPLEMENTED]
- Senkron merkezi [✔ IMPLEMENTED]
- Firma / cihaz bazlı senkron özeti [✔ IMPLEMENTED]
- Son hata, son başarılı senkron ve bekleyen durum görünümü [✔ IMPLEMENTED]
- Teknik senkron kayıtları [✔ IMPLEMENTED]
- Mobil kullanıcılar [✔ IMPLEMENTED]
- Firma yaşam döngüsü ayarları [✔ IMPLEMENTED]
- Sistem sıfırlama aracı [✔ IMPLEMENTED]
- Landing page ürün vitrini [✔ IMPLEMENTED]
- Lisans talepleri [✔ IMPLEMENTED]
- Firma lisansları [✔ IMPLEMENTED]
- Paket yönetimi [✔ IMPLEMENTED]
- Feature flag yönetimi [✔ IMPLEMENTED]
- Ticket yönetimi [✔ IMPLEMENTED]

## Web POS ve Companion
- Ayrı `/pos` alanı [✔ IMPLEMENTED]
- Şube / register / POS session [✔ IMPLEMENTED]
- Web POS satış sekmeleri (sekme bazlı sepet) [✔ IMPLEMENTED]
- Web POS barkod alanında ürün arama öneri dropdown [✔ IMPLEMENTED]
- Web POS topbar menü (firma/kasa/oturum yönetimi) [✔ IMPLEMENTED]
- Web POS role modeli (owner / manager / kasiyer) [✔ IMPLEMENTED]
- Rol bazlı görünürlük ve yetki kısıtları [✔ IMPLEMENTED]
- Web POS `Cihazları Yönet` menü erişimi [✔ IMPLEMENTED]
- Cihaz listesi ve aktif/pasif yönetimi (Web POS) [✔ IMPLEMENTED]
- POS oturum listesi ve yetkili rolde oturum kapatma [✔ IMPLEMENTED]
- Owner rolünde personel ekleme/güncelleme/silme [✔ IMPLEMENTED]
- Web POS ana ekranda bekleyen satışları listeleme ve tek tıkla geri açma [✔ IMPLEMENTED]
- Web POS `Satışları Fişle Gör` menü erişimi [✔ IMPLEMENTED]
- Web POS sağ üst toast bildirim akışı [✔ IMPLEMENTED]
- Telefon companion modu [✔ IMPLEMENTED]
- HID barkod okuyucu desteği [✔ IMPLEMENTED]
- Webden fiş yazdırma [✔ IMPLEMENTED]

## Destek ve Ticket Sistemi
- Mobil içinden ticket açma [✔ IMPLEMENTED]
- Web POS içinden ticket açma [✔ IMPLEMENTED]
- Firma içi ortak gelen kutusu [✔ IMPLEMENTED]
- Admin yanıtı ve durum yönetimi [✔ IMPLEMENTED]
- Ticket yeniden açma [✔ IMPLEMENTED]

## Bilerek İlk Aşamada Eklenmeyenler
- Resmi mali entegrasyon
- Sadakat sistemi
- CRM
- Kampanya motoru
- Otomatik online ödeme altyapısı



