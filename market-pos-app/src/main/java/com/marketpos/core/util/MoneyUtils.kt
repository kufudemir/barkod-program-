package com.marketpos.core.util

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat
import java.util.Locale

object MoneyUtils {

    fun parseTlInputToKurus(input: String): Long? {
        val normalized = normalizeTlInput(input)
        if (normalized.isBlank()) return null
        return runCatching {
            BigDecimal(normalized)
                .multiply(BigDecimal(100))
                .setScale(0, RoundingMode.HALF_UP)
                .longValueExact()
        }.getOrNull()
    }

    fun sanitizeTlTypingInput(input: String): String {
        return input.filter { it.isDigit() || it == ',' || it == '.' }
    }

    fun formatKurus(kurus: Long): String {
        val amount = BigDecimal(kurus).divide(BigDecimal(100))
        return NumberFormat.getCurrencyInstance(Locale("tr", "TR")).format(amount)
    }

    fun formatKurusForInput(kurus: Long): String {
        val amount = BigDecimal(kurus).divide(BigDecimal(100))
        return if (kurus % 100L == 0L) {
            amount.setScale(0, RoundingMode.UNNECESSARY).toPlainString()
        } else {
            amount.setScale(2, RoundingMode.UNNECESSARY).toPlainString().replace(".", ",")
        }
    }

    fun roundUpToWholeTL(priceKurus: Long): Long {
        val hundred = 100L
        val remainder = priceKurus % hundred
        return if (remainder == 0L) priceKurus else priceKurus + (hundred - remainder)
    }

    fun applyPercentChange(priceKurus: Long, percent: Double): Long {
        val result = BigDecimal(priceKurus)
            .multiply(BigDecimal.ONE + (BigDecimal.valueOf(percent).divide(BigDecimal(100))))
            .setScale(0, RoundingMode.HALF_UP)
            .longValueExact()
        return roundUpToWholeTL(result)
    }

    fun percentIncrease(priceKurus: Long, percent: Double): Long {
        return applyPercentChange(priceKurus, kotlin.math.abs(percent))
    }

    fun percentDecrease(priceKurus: Long, percent: Double): Long {
        return applyPercentChange(priceKurus, -kotlin.math.abs(percent))
    }

    private fun normalizeTlInput(input: String): String {
        val cleaned = input
            .trim()
            .replace("₺", "")
            .replace("TL", "", ignoreCase = true)
            .replace(" ", "")
            .replace("\u00A0", "")

        if (cleaned.isBlank()) return ""

        val numeric = cleaned.filter { it.isDigit() || it == ',' || it == '.' || it == '-' }
        if (numeric.isBlank()) return ""
        if (numeric.count { it == '-' } > 1 || (numeric.contains('-') && !numeric.startsWith("-"))) return ""

        return when {
            numeric.contains(',') && numeric.contains('.') -> {
                numeric.replace(".", "").replace(',', '.')
            }
            numeric.count { it == '.' } > 1 -> {
                val lastDot = numeric.lastIndexOf('.')
                buildString {
                    numeric.forEachIndexed { index, char ->
                        if (char != '.' || index == lastDot) append(char)
                    }
                }
            }
            else -> numeric.replace(',', '.')
        }
    }
}
