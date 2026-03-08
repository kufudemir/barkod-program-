package com.marketpos.domain.usecase

import com.marketpos.core.util.DateUtils
import com.marketpos.domain.model.SummaryReport
import com.marketpos.domain.repository.SaleRepository
import javax.inject.Inject

class GetDailySummaryUseCase @Inject constructor(
    private val saleRepository: SaleRepository
) {
    suspend operator fun invoke(referenceTime: Long = DateUtils.now()): SummaryReport {
        val range = DateUtils.dayRange(referenceTime)
        return saleRepository.getSummary(range.first, range.last)
    }
}
