package com.marketpos.data.repository

import com.marketpos.core.util.MoneyUtils
import java.lang.Character.UnicodeScript
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import org.jsoup.Jsoup

internal data class WebSearchHit(
    val rank: Int,
    val title: String,
    val snippet: String,
    val url: String?,
    val priceKurus: Long?
)

internal data class WebAnalyzedSuggestion(
    val name: String,
    val sourceLabel: String,
    val sourceUrl: String?,
    val salePriceKurus: Long?
)

internal object WebNameSuggestionSupport {

    fun buildDuckDuckGoUrl(barcode: String): String {
        val query = URLEncoder.encode("$barcode urun", StandardCharsets.UTF_8.toString())
        return "https://duckduckgo.com/html/?q=$query&kl=tr-tr&ia=web"
    }

    fun extractHits(html: String, limit: Int = 15): List<WebSearchHit> {
        val document = Jsoup.parse(html)
        val resultElements = document.select(".result, .results_links, .result--web").take(limit)

        return resultElements.mapIndexedNotNull { index, element ->
            val titleElement = element.selectFirst("a.result__a, h2 a, a[data-testid=result-title-a]") ?: return@mapIndexedNotNull null
            val title = titleElement.text().trim()
            if (title.isBlank()) return@mapIndexedNotNull null
            val snippet = element.selectFirst(".result__snippet, .result__body, .result__extras__url")?.text().orEmpty().trim()
            val url = titleElement.absUrl("href").ifBlank { titleElement.attr("href").ifBlank { null } }

            WebSearchHit(
                rank = index + 1,
                title = title,
                snippet = snippet,
                url = url,
                priceKurus = parsePrice("$title $snippet")
            )
        }
    }

    fun analyzeHits(
        barcode: String,
        hits: List<WebSearchHit>,
        limit: Int = 4
    ): List<WebAnalyzedSuggestion> {
        if (hits.isEmpty()) return emptyList()

        val rawCandidates = hits.flatMap { hit ->
            extractCandidatePhrases(hit.title, barcode, hit, baseWeight = 20) +
                extractCandidatePhrases(hit.snippet, barcode, hit, baseWeight = 8)
        }

        if (rawCandidates.isEmpty()) return emptyList()

        val tokenFrequency = rawCandidates
            .flatMap { it.tokens }
            .groupingBy { it }
            .eachCount()

        return rawCandidates
            .map { candidate ->
                val tokenScore = candidate.tokens.sumOf { tokenFrequency[it] ?: 0 } * 10
                val positionScore = (16 - candidate.rank).coerceAtLeast(1) * 10
                val score = candidate.baseWeight + positionScore + tokenScore + qualityScore(candidate.name)
                candidate.copy(score = score)
            }
            .groupBy { it.normalizedKey }
            .map { (_, group) ->
                group.maxBy { it.score }
            }
            .sortedByDescending { it.score }
            .map {
                WebAnalyzedSuggestion(
                    name = it.name,
                    sourceLabel = "Web Analiz",
                    sourceUrl = it.url,
                    salePriceKurus = it.priceKurus
                )
            }
            .take(limit)
    }

    private fun extractCandidatePhrases(
        rawText: String,
        barcode: String,
        hit: WebSearchHit,
        baseWeight: Int
    ): List<InternalCandidate> {
        val cleaned = cleanHtml(rawText)
            .replace(barcode, " ")
            .replace(Regex("\\s+"), " ")
            .trim()

        if (cleaned.isBlank()) return emptyList()

        return cleaned
            .split(" - ", " | ", " / ", " : ", " • ", " — ")
            .map { it.trim() }
            .flatMap { segment ->
                val normalized = segment
                    .replace(Regex("\\b([a-zA-Z])range\\b", RegexOption.IGNORE_CASE), "$1 range")
                    .replace(Regex("\\bdren\\b", RegexOption.IGNORE_CASE), "d")
                    .replace(Regex("\\b(urun|fiyat|fiyati|fiyatlari|barkod|kodu|ara|search|sonuc|sonuclari|sikayetleri|sikayet|detayi|detay|grubu)\\b", RegexOption.IGNORE_CASE), " ")
                    .replace(Regex("\\b(yacevap|bat|british american tobacco|tekel)\\b", RegexOption.IGNORE_CASE), " ")
                    .replace(Regex("\\s+"), " ")
                    .trim(' ', '-', '|', '/', ':', ';', '.', ',', '"', '\'')

                val tokens = tokenize(normalized)
                val phrase = tokens.joinToString(" ").trim()

                buildList {
                    if (isPlausibleName(phrase)) {
                        add(
                            InternalCandidate(
                                name = phrase.uppercase(),
                                normalizedKey = tokens.joinToString(" "),
                                tokens = tokens,
                                rank = hit.rank,
                                url = hit.url,
                                priceKurus = hit.priceKurus,
                                baseWeight = baseWeight,
                                score = 0
                            )
                        )
                    }
                }
            }
    }

    private fun tokenize(text: String): List<String> {
        return text
            .split(Regex("[^\\p{L}\\p{N}]+"))
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .map { it.lowercase() }
            .filter { token ->
                token.isNotBlank() &&
                    token !in blockedTokens &&
                    (token.length > 1 || token == "d") &&
                    token.count(Char::isLetter) >= 1 &&
                    isMostlyLatin(token)
            }
    }

    private fun isPlausibleName(name: String): Boolean {
        if (name.length !in 4..80) return false
        if (name.count(Char::isLetter) < 3) return false
        val lowered = name.lowercase()
        if (blockedPhrases.any { lowered.contains(it) }) return false
        return true
    }

    private fun isMostlyLatin(token: String): Boolean {
        val letters = token.filter(Char::isLetter)
        if (letters.isEmpty()) return false
        val latinLetters = letters.count { UnicodeScript.of(it.code) == UnicodeScript.LATIN }
        return latinLetters * 2 >= letters.length
    }

    private fun qualityScore(name: String): Int {
        val lowered = name.lowercase()
        var score = 0
        score += name.count(Char::isLetter) * 2
        score -= name.count(Char::isDigit) * 6
        if (name.length in 6..40) score += 25
        if (lowered.contains("kent") || lowered.contains("marlboro") || lowered.contains("parliament")) score += 15
        if (lowered.contains("range") || lowered.contains("blue") || lowered.contains("red") || lowered.contains("white")) score += 10
        if (lowered.contains("gluten") || lowered.contains("canli kurt") || lowered.contains("nerenin")) score -= 40
        return score
    }

    private fun parsePrice(text: String): Long? {
        val match = Regex("""(\d+[.,]\d{1,2})\s*TL""", RegexOption.IGNORE_CASE)
            .find(text)
            ?.groupValues
            ?.getOrNull(1)
            ?: return null
        return MoneyUtils.parseTlInputToKurus(match)
    }

    private fun cleanHtml(raw: String): String {
        return Jsoup.parse(raw)
            .text()
            .replace('\u00A0', ' ')
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    private data class InternalCandidate(
        val name: String,
        val normalizedKey: String,
        val tokens: List<String>,
        val rank: Int,
        val url: String?,
        val priceKurus: Long?,
        val baseWeight: Int,
        val score: Int
    )

    private val blockedTokens = setOf(
        "urun", "fiyat", "fiyati", "fiyatlari", "barkod", "ara", "search",
        "sonuc", "sonuclari", "kodu", "sikayet", "sikayetleri", "detay", "detayi",
        "grubu", "yacevap", "gluten", "var", "mi", "canli", "kurt", "cikti"
    )

    private val blockedPhrases = listOf(
        "barkod bankasi",
        "openfoodfacts",
        "duckduckgo",
        "google",
        "yacevap",
        "nerenin kodu",
        "tekel grubu",
        "sikayet"
    )
}
