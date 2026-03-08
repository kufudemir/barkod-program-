<?php

return [
    'version' => '2026.03',
    'disclosure' => [
        'title' => 'Aydınlatma Metni',
        'summary' => 'Bu uygulama; firma adı, cihaz bilgisi, barkod, ürün adı, fiyat, ürün grubu ve senkron teknik durumlarını toplar.',
        'body' => [
            'Bu uygulama tek cihazda çalışacak şekilde tasarlanmıştır ancak barkod.space altyapısı açık olduğunda bazı veriler merkeze gönderilir.',
            'Toplanan başlıca veriler: firma adı ve firma kodu, cihaz kimliği ve cihaz adı, barkod, ürün adı, ürün grubu, alış ve satış fiyatı, senkron durumu ve teknik gönderim kayıtları, kayıtlı kullanıcı kullanıyorsanız ad, e-posta ve hesap oturum bilgileridir.',
            'Bu veriler; aynı firma için cihaz ve katalog ilişkisinin kurulması, ürün verilerinin bulutta saklanması ve geri yüklenmesi, global barkod kataloğunun geliştirilmesi, premium haklarının hesapla ilişkilendirilmesi ve teknik hata takibi için kullanılır.',
            'Bu aşamada ödeme kartı, rehber, konum veya kişisel mesaj içerikleri toplanmaz.',
            'Verileriniz size karşı kullanılmak için değil; ürün eşleştirme, katalog geri yükleme ve sistem kalitesini artırmak için işlenir.',
        ],
    ],
    'data_usage' => [
        'title' => 'Veri Kullanımı',
        'summary' => 'Barkod ve ürün verileri katalog geri yükleme, senkron ve ürün öneri sistemini geliştirmek için kullanılır.',
        'body' => [
            'Ürün verileri: girilen barkodlar, ürün adları, ürün grupları ve fiyat bilgileri barkod.space üzerinde saklanabilir. Böylece aynı hesabın veya aynı firmanın verileri daha sonra geri yüklenebilir.',
            'Firma ve cihaz ilişkisi: bu cihazla hangi firmaların aktive edildiği tutulur. Amaç; yanlış firma aktivasyonlarını azaltmak ve eski firmalara geri dönüşü kolaylaştırmaktır.',
            'Hesap ve premium ilişkisi: kayıtlı kullanıcı hesabı kullanıyorsanız premium durumu ve hesap bilgileri cihaz değişiminde geri yüklenebilmesi için hesapla ilişkilendirilebilir.',
            'Teknik kayıtlar: senkron hataları, son başarılı gönderim zamanı ve cihaz bazlı teknik kayıtlar sistemin sağlığını izlemek için tutulur.',
            'Saklama ve temizlik: misafir kaynaklı ve uzun süre hareketsiz kalan sahipsiz firma kayıtları yönetici tarafından otomatik temizlenebilir. Kayıtlı kullanıcıya bağlı firmalar korunur.',
            'Bu metin uygulamanın mevcut sürümündeki veri işleme çerçevesini açıklar. İçerik veya kapsam değişirse onay sürümü güncellenir.',
        ],
    ],
];
