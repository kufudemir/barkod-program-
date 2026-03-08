# KolayKasa Proje Genel Tek Özet

_Güncelleme Tarihi: 2026-03-08_

## 1) Ürün Kimliği
- Ürün adı: **KolayKasa**
- Domain: **barkod.space**
- Ürün tipi: **Tek cihaz odaklı offline-first Mobil POS + Bulut senkron + Web POS + Admin Back Office**
- Hedef kullanıcı: market/tekel/büfe gibi küçük ve orta işletmeler

## 2) Güncel Teknik Durum
- Android sürüm: **1.03.01** (`versionCode: 10301`)
- APK güncelleme manifesti: `web-application/public/app-updates/android/latest.json`
- Mimari: **MVVM + UseCase + Repository + Room + Hilt + Compose**
- Web stack: **Laravel 12 + Filament + MySQL/SQLite + REST API**
- Senkron modeli: **offline-first + outbox + retry + artımlı katalog çekme**

## 3) Canlıda Olan Ana Modüller

### Mobil POS
- Barkod tarama (ML Kit), seri tarama, sepet, satış, stok, rapor
- Ürün ekleme/düzenleme/silme
- İsim öneri sistemi (global katalog + BarkodBankası + web analiz + OCR destekli)
- Hesap/kayıt/şifre sıfırlama, veri kullanım onayı, mod seçimi (Mobil/Web satış)

### Web POS
- Ayrı giriş (`/pos/login`) ve ayrı satış shell'i (`/pos`)
- Şube/Kasa/POS session yapısı
- Çoklu satış sekmesi (`sale_session`), bekleyen satış, geri aç/sil
- Ödeme türleri (nakit/kart/diğer)
- Fiş popup görüntüleme/düzenleme/silme
- Yazdırma/PDF akışları (58mm/80mm/A4)
- Mobil companion ile canlı sepet/satış etkileşimi
- HID barkod okuyucu uyumu

### Admin / Back Office
- Firma, cihaz, global katalog, firma fiyatları
- Senkron merkezi, teknik batch görünümü
- Firma yaşam döngüsü ayarları, sistem sıfırlama
- Lisans talepleri, firma lisansları, paket/feature yönetimi
- Ticket merkezi (yanıt/durum yönetimi)

## 4) Faz Bazlı Genel Tamamlanma Özeti

### V2 (Tamamlandı)
- Operasyon hızı, ürün/stok toplu işlemler, raporlama, ayarlar, stok sayım temeli

### V3 (Büyük ölçüde tamamlandı)
- Hesap/oturum/veri sahipliği
- Firma ve cihaz geçmişi
- Global katalog yönetimi
- Admin senkron merkezi
- Aydınlatma/veri kullanım akışı
- Web panelden satış temelinin açılması

### V4 (Plan kapsamı tamamlandı)
- Landing + paket/lisans modeli
- Web POS’un admin panelden ayrılması
- Şube/kasa/session hiyerarşisi
- Çoklu sekme, companion, HID, bekleyen satış
- Ödeme türleri + fiş altyapısı + fiş profilleri
- Ticket sistemi
- Personel ve rol sistemi
- Senkron parity çekirdeği
- Topbar menüleşme ve hızlı yönetim

## 5) İş Kuralları ve Finans
- Para birimi saklama: **Long (kuruş)**
- Yüzde güncellemede yuvarlama: **her zaman yukarı (ceil) tam TL**
- Satış/stok işlemlerinde veri kaybını önleyen transaction ve queue yaklaşımı

## 6) Operasyon ve Yayınlama
- Sunucu tarafında terminal kısıtı olduğunda patch/ftp + setup ekranı akışıyla deploy
- Android güncellemesi web üstünden `latest.json` ile duyuruluyor
- Web patch klasörleri versiyonlu tutuluyor (`web-application/update/...`)

## 7) Dokümanlarda Açık Görünen Maddeler
Aşağıdaki maddeler dokümanda açık/kararsız olarak işaretli:
- `Çoklu cihaz sekmeleri` (program özellikleri dosyasında açık işaretli)
- `FREE / SILVER / GOLD hak modeli` (program özellikleri dosyasında açık işaretli)

Not: Teknikte bu başlıkların bazı parçaları uygulanmış durumda olabilir; kesin karar için `program özellikleri.md` işaretleri referans alınır.

## 8) Kalan Ana İş Paketi (Stabilizasyon)
1. Uçtan uca regresyon testi (Mobil POS + Web POS + Companion + Senkron)
2. Senkron dayanıklılık testi (failed outbox, retry, tutarlılık)
3. Performans/uzun kullanım testi
4. Release hazırlığı (imzalı build, yayın notu, dağıtım doğrulama)

## 9) Tek Cümle Yönetici Özeti
**KolayKasa, mobil ve web satış kanallarını aynı firma verisi etrafında birleştiren, offline-first çalışabilen ve lisans/ticket/yönetim katmanları hazır bir POS ekosistemi haline gelmiştir; şu an ana odak stabilizasyon ve release kalitesidir.**
