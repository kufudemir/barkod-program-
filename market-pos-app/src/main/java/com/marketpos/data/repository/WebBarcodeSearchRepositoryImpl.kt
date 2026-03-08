package com.marketpos.data.repository

import com.marketpos.core.util.MoneyUtils
import com.marketpos.data.network.NameSuggestionService
import com.marketpos.domain.model.WebBarcodeSearchResult
import com.marketpos.domain.repository.WebBarcodeSearchRepository
import javax.inject.Inject
import javax.inject.Singleton
import org.jsoup.Jsoup

@Singleton
class WebBarcodeSearchRepositoryImpl @Inject constructor(
    private val service: NameSuggestionService
) : WebBarcodeSearchRepository {

    override suspend fun searchByBarcode(barcode: String): Result<List<WebBarcodeSearchResult>> = runCatching {
        val cleanBarcode = barcode.filter(Char::isDigit)
        require(cleanBarcode.isNotBlank()) { "Geçerli barkod girin" }

        val barkodBankasiResults = fromBarkodBankası(cleanBarcode)
        if (barkodBankasiResults.isNotEmpty()) {
            return@runCatching barkodBankasiResults
        }

        val webUrl = WebNameSuggestionSupport.buildDuckDuckGoUrl(cleanBarcode)
        val html = service.rawGet(webUrl).string()
        val hits = WebNameSuggestionSupport.extractHits(html, limit = 15)

        WebNameSuggestionSupport.analyzeHits(cleanBarcode, hits, limit = 5).map {
            WebBarcodeSearchResult(
                barcode = cleanBarcode,
                name = it.name,
                salePriceKurus = it.salePriceKurus,
                sourceLabel = it.sourceLabel,
                sourceUrl = it.sourceUrl ?: webUrl
            )
        }
    }

    private suspend fun fromBarkodBankası(barcode: String): List<WebBarcodeSearchResult> {
        return runCatching {
            val html = service.rawGet("https://barkodbankasi.com/index.php?grup=&search=$barcode").string()
            val document = Jsoup.parse(html)
            document.select("table tbody tr").mapNotNull { row ->
                val cells = row.select("td")
                if (cells.size < 6) return@mapNotNull null

                val itemBarcode = cells[0].text().filter(Char::isDigit).ifBlank { barcode }
                val name = cells[1].text().trim().trim('\'', '"')
                if (name.isBlank()) return@mapNotNull null
                val price = MoneyUtils.parseTlInputToKurus(
                    cells[4].text().replace("TL", "", ignoreCase = true).trim()
                )

                WebBarcodeSearchResult(
                    barcode = itemBarcode,
                    name = name,
                    salePriceKurus = price,
                    sourceLabel = "BarkodBankası",
                    sourceUrl = "https://barkodbankasi.com/index.php?grup=&search=$barcode"
                )
            }
        }.getOrDefault(emptyList())
    }
}

