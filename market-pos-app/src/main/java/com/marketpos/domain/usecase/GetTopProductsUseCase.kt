package com.marketpos.domain.usecase

import com.marketpos.core.util.DateUtils
import com.marketpos.domain.model.ProductAggregateReport
import com.marketpos.domain.repository.SaleRepository
import javax.inject.Inject

class GetTopProductsUseCase @Inject constructor(
    private val saleRepository: SaleRepository
) {
    suspend fun topSellingDaily(referenceTime: Long = DateUtils.now()): List<ProductAggregateReport> {
        val range = DateUtils.dayRange(referenceTime)
        return saleRepository.getTopSelling(range.first, range.last)
    }

    suspend fun topProfitDaily(referenceTime: Long = DateUtils.now()): List<ProductAggregateReport> {
        val range = DateUtils.dayRange(referenceTime)
        return saleRepository.getTopProfit(range.first, range.last)
    }
}
