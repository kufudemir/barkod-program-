package com.marketpos.data.repository

import com.marketpos.data.network.BarkodSpaceApiClient
import com.marketpos.data.network.NameSuggestionService
import com.marketpos.domain.model.NameSuggestion
import com.marketpos.domain.repository.NameSuggestionRepository
import javax.inject.Inject
import javax.inject.Singleton
import org.jsoup.Jsoup

@Singleton
class NameSuggestionRepositoryImpl @Inject constructor(
    private val service: NameSuggestionService,
    private val barkodSpaceApiClient: BarkodSpaceApiClient
) : NameSuggestionRepository {

    override suspend fun suggestNames(barcode: String): List<NameSuggestion> {
        val cleanBarcode = barcode.filter(Char::isDigit).ifBlank { barcode.trim() }
        if (cleanBarcode.isBlank()) return emptyList()

        val ownCatalogSuggestions = fromOwnCatalog(cleanBarcode)
        if (ownCatalogSuggestions.isNotEmpty()) {
            return ownCatalogSuggestions
        }

        val barkodBankasiSuggestions = fromBarkodBankasi(cleanBarcode)
        if (barkodBankasiSuggestions.isNotEmpty()) {
            return barkodBankasiSuggestions
        }

        val webUrl = WebNameSuggestionSupport.buildDuckDuckGoUrl(cleanBarcode)
        val html = runCatching { service.rawGet(webUrl).string() }.getOrNull().orEmpty()
        if (html.isBlank()) return emptyList()

        return WebNameSuggestionSupport.extractHits(html, limit = 15)
            .let { hits -> WebNameSuggestionSupport.analyzeHits(cleanBarcode, hits, limit = 5) }
            .map { NameSuggestion(name = it.name, sourceLabel = it.sourceLabel) }
    }

    private suspend fun fromOwnCatalog(barcode: String): List<NameSuggestion> {
        return runCatching {
            val response = barkodSpaceApiClient.fetchGlobalCatalogSuggestion(barcode)
            listOf(
                NameSuggestion(
                    name = response.name,
                    sourceLabel = "barkod.space Katalog"
                )
            )
        }.getOrDefault(emptyList())
    }

    private suspend fun fromBarkodBankasi(barcode: String): List<NameSuggestion> {
        return runCatching {
            val html = service.rawGet("https://barkodbankasi.com/index.php?grup=&search=$barcode").string()
            val document = Jsoup.parse(html)
            document.select("table tbody tr")
                .mapNotNull { row ->
                    val cells = row.select("td")
                    if (cells.size < 2) return@mapNotNull null
                    val rowBarcode = cells[0].text().filter(Char::isDigit)
                    if (rowBarcode.isNotBlank() && rowBarcode != barcode) return@mapNotNull null

                    val name = cells[1].text().trim().trim('\'', '"')
                    if (name.isBlank()) return@mapNotNull null
                    NameSuggestion(name = name, sourceLabel = "BarkodBankası")
                }
                .distinctBy { it.name.uppercase() }
                .take(5)
        }.getOrDefault(emptyList())
    }
}
