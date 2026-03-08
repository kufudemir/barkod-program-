# Market POS

Proje yapısı sadeleştirildi.

## Ana Klasörler
- `market-pos-app/`: Android uygulaması
- `web-application/`: barkod.space web uygulaması ve patch klasörleri
- `dokumanlar/`: uygulama ve web dokümanları
- `tools/`: kurulum, araştırma, tasarım ve yardımcı araçlar

## Dokümanlar
- [Uygulama Dokümanları](dokumanlar/uygulama-dokumanlari)
- [Web Dokümanları](dokumanlar/web-dokumanlari)

## Yardımcı Klasörler
- `tools/web-kurulum/`: Composer ve yerel PHP yardımcı dosyaları
- `tools/arastirma/barkodbankasi-ornekleri/`: HTML araştırma çıktıları
- `tools/tasarim/`: görsel kaynaklar
- `tools/license-generator/`: lisans üretici araçları

## Yerel Geliştirme Dosyaları
- `.vscode/`: ortak VS Code ayarları
- `.idea/`: Android Studio proje ayarları
- `.gradle/`, `.kotlin/`: yerel cache klasörleri

Bu klasörlerin yerel ve geçici parçaları `.gitignore` ile ayrılmıştır.

## Build İçin Kökte Kalan Dosyalar
- `settings.gradle.kts`
- `build.gradle.kts`
- `gradle.properties`
- `gradlew`
- `gradlew.bat`
- `local.properties`
