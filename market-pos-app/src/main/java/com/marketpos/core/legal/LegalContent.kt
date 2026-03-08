package com.marketpos.core.legal

object LegalContent {
    const val CURRENT_VERSION = "2026.03"

    const val CONSENT_LABEL = "Aydınlatma metnini ve veri kullanım açıklamasını okudum, kabul ediyorum."

    const val SHORT_NOTICE = "Bu uygulama; firma adı, cihaz bilgisi, barkod, ürün adı, fiyat, ürün grubu ve senkron teknik durumlarını toplar. Amaç; katalog senkronu, veri kurtarma, premium yönetimi ve ürün öneri sistemini geliştirmektir."

    const val DISCLOSURE_TITLE = "Aydınlatma Metni"
    const val DISCLOSURE_TEXT = """
Bu uygulama tek cihazda çalışacak şekilde tasarlanmıştır ancak barkod.space altyapısı açık olduğunda bazı veriler merkeze gönderilir.

Toplanan başlıca veriler:
- Firma adı ve firma kodu
- Cihaz kimliği ve cihaz adı
- Barkod, ürün adı, ürün grubu, alış ve satış fiyatı
- Senkron durumu, son hata ve teknik gönderim kayıtları
- Kayıtlı kullanıcı kullanıyorsanız ad, e-posta ve hesap oturum bilgileri

Bu veriler şu amaçlarla kullanılır:
- Aynı firma için cihaz ve katalog ilişkisinin kurulması
- Ürün verilerinin bulutta saklanması ve geri yüklenmesi
- Global barkod kataloğunun geliştirilmesi
- Premium haklarının hesapla ilişkilendirilmesi
- Teknik hata takibi ve senkron sorunlarının çözülmesi

Bu aşamada ödeme kartı, rehber, konum veya kişisel mesaj içerikleri toplanmaz.

Verileriniz size karşı kullanılmak için değil; ürün eşleştirme, katalog geri yükleme ve sistem kalitesini artırmak için işlenir.
"""

    const val DATA_USAGE_TITLE = "Veri Kullanımı"
    const val DATA_USAGE_TEXT = """
Veri kullanımı ilkeleri:

1. Ürün verileri
Girilen barkodlar, ürün adları, ürün grupları ve fiyat bilgileri barkod.space üzerinde saklanabilir. Böylece aynı hesabın veya aynı firmanın verileri daha sonra geri yüklenebilir.

2. Firma ve cihaz ilişkisi
Bu cihazla hangi firmaların aktive edildiği tutulur. Amaç; yanlış firma aktivasyonlarını azaltmak ve eski firmalara geri dönüşü kolaylaştırmaktır.

3. Hesap ve premium ilişkisi
Kayıtlı kullanıcı hesabı kullanıyorsanız premium durumu ve hesap bilgileri cihaz değişiminde geri yüklenebilmesi için hesapla ilişkilendirilebilir.

4. Teknik kayıtlar
Senkron hataları, son başarılı gönderim zamanı ve cihaz bazlı teknik kayıtlar sistemin sağlığını izlemek için tutulur.

5. Saklama ve temizlik
Misafir kaynaklı ve uzun süre hareketsiz kalan sahipsiz firma kayıtları yönetici tarafından otomatik temizlenebilir. Kayıtlı kullanıcıya bağlı firmalar korunur.

Bu metin uygulamanın mevcut sürümündeki veri işleme çerçevesini açıklar. İçerik veya kapsam değişirse onay sürümü güncellenir.
"""
}
