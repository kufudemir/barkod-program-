# Proje Yönetim Kuralları (AI Operasyon)

Bu dosya, `ai_docs` harici proje dokümanlarının taranmış halinden türetilen aktif çalışma kurallarını içerir.

## Kaynak Kural Dokümanları
- `dokumanlar/proje-kurallari.md`
- `dokumanlar/asistan-ve-codex-kuralları.md`
- `dokumanlar/uygulama-dokumanlari/program özellikleri.md`
- `dokumanlar/uygulama-dokumanlari/teknik-mimari.md`
- `dokumanlar/uygulama-dokumanlari/gelistirme-fazlari-v4-web-pos-parity.md`
- `dokumanlar/web-dokumanlari/web-api-sozlesmesi.md`
- `dokumanlar/web-dokumanlari/web-veri-modeli.md`
- `web-application/update/README.md`
- `web-application/update/_indeks/patch-indeksi.md`

## Kural Önceliği
1. Veri kaybını önleme
2. Aktif faz kuralı
3. Kaynak doküman önceliği (özellik/mimari/API/faz)
4. Mevcut kodu koruma
5. Minimum değişiklik ilkesi
6. Dokümantasyon senkronu

## Doküman Öncelik Sırası
1. `program özellikleri.md`
2. `teknik-mimari.md` + `room-entity-plani.md`
3. `web-veri-modeli.md` + `web-api-sozlesmesi.md`
4. `gelistirme-fazlari-v4-web-pos-parity.md`
5. diğer faz/changelog/patch dökümanları

## Faz Disiplini
- Sadece aktif faz implement edilir.
- Gelecek faz talepleri doğrudan kodlanmaz, backlog’a yazılır.
- Ağır işler alt fazlara bölünür (0A/0B, 1A/1B/1C vb.).
- Bir turda tek alt faz hedeflenir.

## Güvenli Değişiklik İlkesi
- İlgisiz refactor yapılmaz.
- Mimariyi bozacak taşıma/yeniden yapılandırma yapılmaz.
- Mevcut kullanıcı değişiklikleri izinsiz geri alınmaz.
- Destructive komutlar kullanılmaz.

## Finans ve İş Kuralları Koruması
- Para modeli `Long kuruş` olarak korunur.
- Yuvarlama/hesaplama kuralı doküman dışına çıkmaz.
- Aktivasyon/senkron kuralları (tek aktif cihaz, dedup, outbox) korunur.

## Sürüm / Patch / Dağıtım Disiplini
- Patch adlandırma:
  - `vX.Y.Z`
  - `vX.Y.Z-hotfixN`
- Patch içinde sadece değişen dosyalar bulunur.
- Patch README zorunludur.
- Patch indeksi güncellenir.
- Android sürüm değiştiyse `latest.json` + APK dağıtım dosyası güncellenir.

## Terminalsiz Sunucu Kuralı
- Sunucuda terminal olmayabilir varsayımıyla ilerlenir.
- Deploy adımları terminale bağımlı bırakılmaz.
- Migrate/cache için panel tabanlı alternatif yol da rapora yazılır.

## API / Doküman Senkronu
- Yeni endpoint veya davranış değişiminde API dokümanı güncellenir.
- Özellik tamamlanmadan `[✔ IMPLEMENTED]` işaretlemesi yapılmaz.
- Kullanıcıya görünen metinlerde UTF-8/Türkçe uyumu korunur.

## İş Sonu Rapor Şablonu (Zorunlu)
1. task summary
2. affected files
3. actions
4. phase compliance
5. risks
6. updated logs
7. result
