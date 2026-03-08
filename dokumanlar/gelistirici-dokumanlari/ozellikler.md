# Ozellikler

Bu dokuman, gelistirici seviyesinde modul bazli ozellik kapsamini ozetler.
Detayli ve satir bazli durum icin `program özellikleri.md` dosyasina bak.

## 1) Mobil POS (Android)
- Barkod okuma: EAN-13, EAN-8, UPC-A, UPC-E
- OCR destekli urun adi onerisi
- Urun ekle/duzenle/sil
- Sepet ve satis akisi
- Satir bazli fiyat ve indirim islemleri
- Stok takip ve stok sayim ekranlari
- Rapor ekranlari
- Hesap, aktivasyon, premium ve destek ekranlari

## 2) Web Back Office
- Firma, cihaz ve mobil kullanici yonetimi
- Lisans paketleri ve firma lisansi yonetimi
- Feature flag / override yonetimi
- Senkron merkezi ve teknik kayit goruntuleme
- Ticket merkezi

## 3) Web POS
- Ayri login ve ayri oturum alani (`/pos`)
- Barkod ile satis ve urun arama
- Satis sekmeleri (`sale_session`) ve bekleyen satis
- Odeme turleri ve fis goruntuleme/yazdirma
- Cihaz/oturum/personel hizli yonetim modallari

## 4) Mobil-Web Companion
- Telefonla web sepetine barkod okutma
- Satir adet/fiyat/indirim guncelleme
- Companion modunda satis tamamlama ve yazdirma tetigi

## 5) API ve Senkron
- Auth, aktivasyon, lisans, katalog ve destek endpointleri
- Katalog batch push + degisiklik bazli pull senkronu
- Event dedup ve retry mantigi

## 6) Ticket Sistemi
- Mobil ve Web POS tarafindan ticket olusturma
- Firma bazli ortak gelen kutusu
- Admin panelden yanit ve durum yonetimi

## 7) Not
Bu ozet, 2026-03-08 tarihinde mevcut kod ve dokuman taramasina dayanir.
Asil kaynak:
- `dokumanlar/uygulama-dokumanlari/program özellikleri.md`
