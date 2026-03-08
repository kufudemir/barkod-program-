# Konvansiyonlar

## Dil ve Metin
- Kullanıcıya görünen metinlerde Türkçe ve UTF-8 uyumu korunur.

## Sürüm / Patch
- Patch adları:
  - `vX.Y.Z`
  - `vX.Y.Z-hotfixN`
- Patch klasöründe sadece değişen dosyalar bulunur.
- Patch README zorunludur.

## Dağıtım
- Android sürüm güncellemesinde `public/app-updates/android/latest.json` ve yeni APK güncellenir.
- Sunucu terminali olmayan senaryolar için panel tabanlı alternatif adımlar dokümana yazılır.

## Faz Disiplini
- Büyük işler alt fazlara bölünür.
- Tek turda tek alt faz hedeflenir.
- Her tur sonunda kısa doğrulama raporu verilir.
