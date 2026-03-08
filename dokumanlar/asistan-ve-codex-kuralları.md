# Asistan ve Codex Calisma Kurallari

Bu dosya, proje gelistirme sirasinda Codex'in uymasi gereken operasyonel calisma kurallarini tanimlar.

## 1) Kapsam ve Kaynak

1. Karar kaynagi aktif dokumanlardir.
2. Arsiv niteligindeki `dokumanlar/oturum-kayitlari/` teknik kural kaynagi degildir.
3. Celiski halinde oncelik:
   - `program ozellikleri.md`
   - `teknik-mimari.md` ve `room-entity-plani.md`
   - `web-veri-modeli.md` ve `web-api-sozlesmesi.md`
   - `gelistirme-fazlari-v4-web-pos-parity.md`

## 2) Is Analizi ve Planlama

1. Her yeni istekte once hedef netlestirilir.
2. Buyuk isler alt fazlara bolunur.
3. Tek turda tek alt faz hedeflenir.
4. Scope disina cikilmaz; istenmeyen ozellik eklenmez.

## 3) Kod Degisikligi Disiplini

1. Dosya degistirmeden once ne degisecegi belirtilir.
2. Ilgisiz dosyalara mudahale edilmez.
3. Mevcut kullanici degisiklikleri izinsiz geri alinmaz.
4. Destructive komutlar kullanilmaz.

## 4) Surum, Patch ve Dagitim

1. Kod degisikliginde uygun patch/surum guncellemesi yapilir.
2. Sadece dokuman/plan degisikliklerinde surum artisi zorunlu degildir.
3. Patch adlandirma:
   - `vX.Y.Z`
   - `vX.Y.Z-hotfixN`
4. Patch klasorune sadece degisen dosyalar konur.
5. Patch `README.md` yazilir.
6. Patch indeksi guncellenir (`web-application/update/_indeks/patch-indeksi.md`).
7. Android surumu degisti ise patch icinde `latest.json` ve APK dagitim dosyasi bulunur.

## 5) Dogrulama Disiplini

1. Mumkun olan teknik kontroller calistirilir:
   - syntax
   - build
   - route list
   - migration kontrolu
   - test
2. Calistirilmayan kontrol varsa sebebi rapora yazilir.

## 6) Is Sonu Rapor Formati (Zorunlu)

Her tur sonunda asagidaki basliklarla rapor verilir:

1. Faz Durumu
2. Yapilanlar
3. Guncellenen Dokumanlar
4. Surum
5. APK (varsa)
6. Web Guncelleme Dosyalari (varsa)
7. Web Patch
8. Onemli Notlar
9. Yapman Gerekenler
10. Dogrulama

## 7) Is Kurallarina Uyum

1. Finans kurallari degistirilmez (kurus bazli model, yuvarlama mantigi).
2. Aktivasyon ve senkron kurallari korunur (tek aktif cihaz, dedup, outbox).
3. API degisirse `web-api-sozlesmesi.md` guncellenir.
4. Ozellik tamamlanmadan `IMPLEMENTED` olarak isaretlenmez.
5. UI metinlerinde UTF-8/Turkce gorunurluk korunur.

## 8) Terminalsiz Sunucu Kurali

1. Sunucuda terminal olmayabilecegi varsayimi esas alinir.
2. Web deploy adimlari terminal komutuna bagimli birakilmaz.
3. Migrate/cache gibi bakim adimlari icin web arayuzu alternatifi sunulur.
4. Is sonu raporda iki yol acik yazilir:
   - terminal varsa komutlu yol
   - terminal yoksa panel/ekran uzerinden yol
