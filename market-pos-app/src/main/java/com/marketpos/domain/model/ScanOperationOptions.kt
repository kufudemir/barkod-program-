package com.marketpos.domain.model

enum class SerialScanCooldownOption(
    val millis: Long,
    val label: String
) {
    FAST(800L, "0.8 sn"),
    NORMAL(1_200L, "1.2 sn"),
    SAFE(1_800L, "1.8 sn"),
    SLOW(2_500L, "2.5 sn");

    companion object {
        fun fromStoredValue(value: String?): SerialScanCooldownOption {
            return entries.firstOrNull { it.name == value } ?: SAFE
        }
    }
}

enum class ScanBoxSizeOption(
    val widthFraction: Float,
    val heightFraction: Float,
    val label: String
) {
    SMALL(0.58f, 0.16f, "Kucuk"),
    MEDIUM(0.72f, 0.22f, "Orta"),
    LARGE(0.88f, 0.28f, "Buyuk");

    companion object {
        fun fromStoredValue(value: String?): ScanBoxSizeOption {
            return entries.firstOrNull { it.name == value } ?: MEDIUM
        }
    }
}
