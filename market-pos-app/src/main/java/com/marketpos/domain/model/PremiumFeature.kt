package com.marketpos.domain.model

enum class PremiumFeature(val title: String) {
    SERIAL_SCAN("Seri Ürün Tarama"),
    WEB_NAME_SUGGESTION("İnternet İsim Önerisi"),
    OCR_NAME_SCAN("Ambalajdan Oku"),
    WEB_BARCODE_SEARCH("Web'de Barkod Ara"),
    BARKOD_BANKASI_IMPORT("BarkodBankası İçeri Aktar"),
    BULK_PRICE_UPDATE("Toplu Fiyat Güncelle"),
    LINE_PRICE_OVERRIDE("Sepette Özel Fiyat ve İndirim"),
    REPORTS("Satış Raporları"),
    STOCK_TRACKING("Stok Takibi")
}


