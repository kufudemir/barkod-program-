package com.marketpos.domain.model

enum class AppThemeMode(val label: String) {
    LIGHT("Açık"),
    DARK("Koyu");

    companion object {
        fun fromStoredValue(value: String?): AppThemeMode {
            return entries.firstOrNull { it.name == value } ?: LIGHT
        }
    }
}

