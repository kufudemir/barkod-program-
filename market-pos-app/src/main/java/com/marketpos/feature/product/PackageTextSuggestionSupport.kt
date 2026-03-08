package com.marketpos.feature.product

internal object PackageTextSuggestionSupport {

    fun extractCandidates(
        rawText: String,
        barcode: String?,
        limit: Int = 5
    ): List<String> {
        val lines = rawText.lineSequence().toList()
        return extractCandidates(lines, barcode, limit)
    }

    fun extractCandidates(
        lines: List<String>,
        barcode: String?,
        limit: Int = 5
    ): List<String> {
        if (lines.isEmpty()) return emptyList()

        val cleanedLines = lines
            .asSequence()
            .map { cleanLine(it, barcode) }
            .filter { it.isNotBlank() }
            .toList()

        if (cleanedLines.isEmpty()) return emptyList()

        val rawCandidates = buildList {
            cleanedLines.forEachIndexed { index, line ->
                addCandidate(line, index)
                if (index < cleanedLines.lastIndex) {
                    addCandidate("$line ${cleanedLines[index + 1]}", index, mergeBoost = 18)
                }
                if (index < cleanedLines.lastIndex - 1) {
                    addCandidate("$line ${cleanedLines[index + 1]} ${cleanedLines[index + 2]}", index, mergeBoost = 26)
                }
            }
        }

        if (rawCandidates.isEmpty()) return emptyList()

        val tokenFrequency = rawCandidates
            .flatMap { it.tokens }
            .groupingBy { it }
            .eachCount()

        return rawCandidates
            .map { candidate ->
                val repeatedTokenScore = candidate.tokens.sumOf { tokenFrequency[it] ?: 0 } * 8
                val positionScore = (12 - candidate.index).coerceAtLeast(1) * 6
                val orderedTokens = orderTokens(candidate.tokens)
                val orderedName = orderedTokens.joinToString(" ")
                val score = candidate.baseScore + repeatedTokenScore + positionScore + qualityScore(orderedName)
                candidate.copy(
                    name = orderedName,
                    normalizedKey = orderedTokens.joinToString(" "),
                    tokens = orderedTokens,
                    score = score
                )
            }
            .groupBy { it.normalizedKey }
            .map { (_, group) -> group.maxBy { it.score } }
            .sortedByDescending { it.score }
            .map { it.name }
            .distinct()
            .take(limit)
    }

    fun extractSelectableWords(
        lines: List<String>,
        barcode: String?,
        limit: Int = 28
    ): List<String> {
        if (lines.isEmpty()) return emptyList()

        return lines
            .asSequence()
            .map { cleanLine(it, barcode) }
            .flatMap { normalizeCandidate(it).split(Regex("[^\\p{L}\\p{N}]+")).asSequence() }
            .map { it.trim().uppercase() }
            .filter { it.isNotBlank() }
            .filterNot { blockedTokens.contains(it) }
            .filter { token ->
                token.length > 1 || token == "D"
            }
            .distinct()
            .take(limit)
            .toList()
    }

    fun cleanupCustomName(value: String): String {
        val tokens = tokenize(normalizeCandidate(value))
        return orderTokens(tokens)
            .joinToString(" ")
            .trim()
    }

    private fun MutableList<Candidate>.addCandidate(
        rawLine: String,
        index: Int,
        mergeBoost: Int = 0
    ) {
        val normalized = normalizeCandidate(rawLine)
        if (!isPlausibleCandidate(normalized)) return
        val tokens = tokenize(normalized)
        if (tokens.isEmpty()) return

        add(
            Candidate(
                name = tokens.joinToString(" "),
                normalizedKey = tokens.joinToString(" "),
                tokens = tokens,
                index = index,
                baseScore = 30 + mergeBoost,
                score = 0
            )
        )
    }

    private fun cleanLine(line: String, barcode: String?): String {
        return line
            .replace(barcode.orEmpty(), " ", ignoreCase = true)
            .replace(Regex("\\b\\d{6,14}\\b"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    private fun normalizeCandidate(value: String): String {
        return value
            .uppercase()
            .replace("&", " ")
            .replace("’", "'")
            .replace(Regex("\\bDRANGE\\b"), "D RANGE")
            .replace(Regex("\\bDREN\\b"), "D")
            .replace(Regex("[^\\p{L}\\p{N} +./-]"), " ")
            .replace(Regex("\\s+"), " ")
            .trim(' ', '-', '/', '.')
    }

    private fun tokenize(value: String): List<String> {
        return value
            .split(Regex("[^\\p{L}\\p{N}]+"))
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .filterNot { blockedTokens.contains(it) }
            .filter { token ->
                token.length > 1 || token == "D"
            }
    }

    private fun isPlausibleCandidate(value: String): Boolean {
        if (value.length !in 4..48) return false
        if (value.count(Char::isLetter) < 3) return false
        if (blockedPhrases.any { value.contains(it) }) return false
        if (value.startsWith("HTTP")) return false
        return true
    }

    private fun qualityScore(value: String): Int {
        var score = 0
        val tokenCount = value.split(" ").size
        if (tokenCount in 2..5) score += 24
        if (value.length in 6..28) score += 16
        score += value.count(Char::isLetter)
        score -= value.count(Char::isDigit) * 6
        if (productTypeTokens.any { value.contains(it) }) score += 12
        if (value.contains("TOTAL CARE") || value.contains("TAM KORUMA") || value.contains("BEYAZLATICI")) score += 10
        if (value.contains("BLUE") || value.contains("RED") || value.contains("WHITE")) score += 8
        if (brandTokens.any { value.contains(it) }) score += 14
        if (value.contains("KLINIK") || value.contains("FORMUL") || value.contains("PROFESYONEL")) score -= 4
        return score
    }

    private fun orderTokens(tokens: List<String>): List<String> {
        if (tokens.isEmpty()) return emptyList()
        return tokens.sortedWith(
            compareByDescending<String> { tokenPriority(it) }
                .thenBy { tokens.indexOf(it) }
        ).distinct()
    }

    private fun tokenPriority(token: String): Int {
        return when {
            token in brandTokens -> 300
            token in productTypeTokens -> 220
            token in variantTokens -> 180
            else -> 100
        }
    }

    private data class Candidate(
        val name: String,
        val normalizedKey: String,
        val tokens: List<String>,
        val index: Int,
        val baseScore: Int,
        val score: Int
    )

    private val blockedTokens = setOf(
        "SIGARA", "ZARARLIDIR", "OLDURUR", "UYARI", "ICINDEKILER", "ICINDEKILERI",
        "TUKETIM", "SON", "TARIHI", "NET", "MIKTAR", "GRAM", "ML", "ADET", "PAKET",
        "TARIM", "BAKANLIGI", "TURKIYE", "URETIM", "ITHALATCI", "SATILAMAZ", "YAS",
        "WWW", "COM", "ORG", "GLUTEN", "VAR", "MI", "KODU", "BARKOD",
        "ORJINAL", "UZMAN", "KLINIK", "FORMUL", "GELISMIS", "PROFESYONEL"
    )

    private val blockedPhrases = listOf(
        "SIGARA ICMEK",
        "18 YAS",
        "SON TUKETIM",
        "ICINDEKILER",
        "WWW ",
        " BARKOD",
        " TARIM VE ORMAN",
        " UYARI "
    )

    private val brandTokens = setOf(
        "SENSODYNE", "COLGATE", "IPANA", "KENT", "MARLBORO", "PARLIAMENT", "ELSEVE", "HEAD", "SHOULDERS"
    )

    private val productTypeTokens = setOf(
        "DIS", "MACUNU", "SAMPUAN", "KREM", "SUT", "PEYNIR", "SIGARA", "ICECEK", "KAHVE"
    )

    private val variantTokens = setOf(
        "BLUE", "RED", "WHITE", "TOTAL", "CARE", "TAM", "KORUMA", "BEYAZLATICI", "FERAH", "NEFES"
    )
}
