package com.marketpos.domain.repository

import com.marketpos.domain.model.NameSuggestion

interface NameSuggestionRepository {
    suspend fun suggestNames(barcode: String): List<NameSuggestion>

    suspend fun suggestName(barcode: String): NameSuggestion? {
        return suggestNames(barcode).firstOrNull()
    }
}
