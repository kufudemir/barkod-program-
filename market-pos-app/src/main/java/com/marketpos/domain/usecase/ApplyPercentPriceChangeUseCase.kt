package com.marketpos.domain.usecase

import com.marketpos.core.util.MoneyUtils
import javax.inject.Inject

class ApplyPercentPriceChangeUseCase @Inject constructor() {
    operator fun invoke(priceKurus: Long, percent: Double): Long {
        return MoneyUtils.applyPercentChange(priceKurus, percent)
    }
}
