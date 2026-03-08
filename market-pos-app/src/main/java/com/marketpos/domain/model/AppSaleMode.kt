package com.marketpos.domain.model

enum class AppSaleMode(val label: String) {
    MOBILE_SALES("Mobil uzerinden satis"),
    WEB_SALES("Web uzerinden satis");

    companion object {
        fun fromStoredValue(value: String?): AppSaleMode {
            return runCatching { valueOf(value.orEmpty()) }.getOrDefault(MOBILE_SALES)
        }
    }
}
