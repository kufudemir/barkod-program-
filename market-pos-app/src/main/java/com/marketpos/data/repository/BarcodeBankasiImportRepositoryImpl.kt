package com.marketpos.data.repository

import androidx.core.text.HtmlCompat
import com.marketpos.core.util.MoneyUtils
import com.marketpos.data.network.NameSuggestionService
import com.marketpos.domain.model.BarcodeBankasiGroup
import com.marketpos.domain.model.BarcodeBankasiImportItem
import com.marketpos.domain.model.BarcodeBankasiPreviewResult
import com.marketpos.domain.repository.BarcodeBankasiImportRepository
import javax.inject.Inject
import javax.inject.Singleton
import okhttp3.HttpUrl.Companion.toHttpUrl

@Singleton
class BarcodeBankasiImportRepositoryImpl @Inject constructor(
    private val service: NameSuggestionService
) : BarcodeBankasiImportRepository {

    override suspend fun fetchGroups(): Result<List<BarcodeBankasiGroup>> = runCatching {
        val html = service.rawGet("https://barkodbankasi.com/").string()
        val groups = parseGroups(html)
        buildList {
            add(BarcodeBankasiGroup(value = "", label = "Tüm Gruplar"))
            addAll(groups)
        }
    }

    override suspend fun fetchPreview(
        query: String?,
        group: String?,
        startPage: Int,
        requestedItemCount: Int
    ): Result<BarcodeBankasiPreviewResult> = runCatching {
        val normalizedQuery = query?.trim().orEmpty()
        val normalizedGroup = group?.trim().orEmpty()
        require(normalizedQuery.isNotBlank() || normalizedGroup.isNotBlank()) {
            "En az grup veya arama metni seçilmelidir"
        }
        require(startPage > 0) { "Başlangıç sayfası 1 veya büyük olmalı" }
        require(requestedItemCount in 1..1000) { "Bir seferde en fazla 1000 ürün çekebilirsiniz" }

        val items = mutableListOf<BarcodeBankasiImportItem>()
        var fetchedPages = 0
        var page = startPage
        var totalAvailable: Int? = null
        val seenBarcodes = linkedSetOf<String>()

        while (items.size < requestedItemCount) {
            val html = service.rawGet(buildUrl(normalizedQuery, normalizedGroup, page)).string()
            totalAvailable = totalAvailable ?: parseTotalAvailable(html)
            val rows = parseItems(html, page)
            if (rows.isEmpty()) break

            rows.forEach { item ->
                if (items.size >= requestedItemCount) return@forEach
                if (seenBarcodes.add(item.barcode)) {
                    items += item
                }
            }

            fetchedPages++
            page++
        }

        BarcodeBankasiPreviewResult(
            query = normalizedQuery.ifBlank { normalizedGroup },
            startPage = startPage,
            requestedItemCount = requestedItemCount,
            fetchedPages = fetchedPages,
            totalAvailable = totalAvailable,
            items = items.take(requestedItemCount),
            skippedItems = 0
        )
    }

    private fun buildUrl(query: String, group: String, page: Int): String {
        return "https://barkodbankasi.com/index.php".toHttpUrl().newBuilder()
            .addQueryParameter("grup", group)
            .addQueryParameter("search", query)
            .addQueryParameter("page", page.toString())
            .build()
            .toString()
    }

    private fun parseGroups(html: String): List<BarcodeBankasiGroup> {
        return Regex(
            pattern = """<option value="([^"]*)"[^>]*>(.*?)</option>""",
            options = setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL)
        ).findAll(html).mapNotNull { match ->
            val value = cleanCell(match.groupValues[1])
            val label = cleanCell(match.groupValues[2])
            if (value.isBlank()) return@mapNotNull null
            BarcodeBankasiGroup(value = value, label = label)
        }.distinctBy { it.value }.toList()
    }

    private fun parseTotalAvailable(html: String): Int? {
        return Regex("""Toplam:\s*(\d+)""", RegexOption.IGNORE_CASE)
            .find(html)
            ?.groupValues
            ?.getOrNull(1)
            ?.toIntOrNull()
    }

    private fun parseItems(html: String, page: Int): List<BarcodeBankasiImportItem> {
        val tbody = Regex(
            pattern = """<tbody[^>]*>(.*?)</tbody>""",
            options = setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL)
        ).find(html)?.groupValues?.getOrNull(1).orEmpty()

        if (tbody.isBlank()) return emptyList()

        return Regex(
            pattern = """<tr[^>]*>(.*?)</tr>""",
            options = setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL)
        ).findAll(tbody).mapNotNull { rowMatch ->
            val cells = Regex(
                pattern = """<td[^>]*>(.*?)</td>""",
                options = setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL)
            ).findAll(rowMatch.groupValues[1]).map { cleanCell(it.groupValues[1]) }.toList()

            if (cells.size < 6) return@mapNotNull null

            val barcode = cells[0].filter(Char::isDigit)
            val name = cells[1].trim().trim('\'', '"')
            val salePriceKurus = cells[4]
                .replace("TL", "", ignoreCase = true)
                .trim()
                .let { MoneyUtils.parseTlInputToKurus(it) }
                ?: return@mapNotNull null

            if (barcode.isBlank() || name.isBlank()) return@mapNotNull null

            BarcodeBankasiImportItem(
                barcode = barcode,
                name = name,
                salePriceKurus = salePriceKurus,
                sourcePage = page,
                lastChangedAt = cells[5].ifBlank { null }
            )
        }.toList()
    }

    private fun cleanCell(raw: String): String {
        val noTags = raw.replace(Regex("<[^>]*>"), " ")
        return HtmlCompat.fromHtml(noTags, HtmlCompat.FROM_HTML_MODE_LEGACY)
            .toString()
            .replace('\u00A0', ' ')
            .replace(Regex("\\s+"), " ")
            .trim()
    }
}


