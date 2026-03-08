package com.marketpos.domain.usecase

import com.marketpos.core.util.DateUtils
import com.marketpos.domain.repository.SaleRepository
import javax.inject.Inject
import kotlin.math.ceil
import kotlin.math.max

data class MinStockSuggestion(
    val suggestedMinStock: Int,
    val soldQuantityLast30Days: Int,
    val usedFallback: Boolean = false
)

class SuggestMinStockUseCase @Inject constructor(
    private val saleRepository: SaleRepository
) {
    suspend operator fun invoke(productBarcode: String): Result<MinStockSuggestion> = runCatching {
        val now = DateUtils.now()
        val thirtyDaysMillis = 30L * 24L * 60L * 60L * 1000L
        val soldLast30Days = saleRepository.getSoldQuantity(
            productBarcode = productBarcode,
            fromEpochMillis = now - thirtyDaysMillis,
            toEpochMillis = now
        )
        if (soldLast30Days <= 0) {
            return@runCatching MinStockSuggestion(
                suggestedMinStock = 1,
                soldQuantityLast30Days = 0,
                usedFallback = true
            )
        }

        val suggested = max(1, ceil((soldLast30Days / 30.0) * 7.0).toInt())
        MinStockSuggestion(
            suggestedMinStock = suggested,
            soldQuantityLast30Days = soldLast30Days,
            usedFallback = false
        )
    }
}
