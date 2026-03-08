package com.marketpos.domain.usecase

import com.marketpos.core.util.MoneyUtils
import java.math.BigDecimal
import java.math.RoundingMode
import javax.inject.Inject

class CalculateTargetMarginSalePriceUseCase @Inject constructor() {
    operator fun invoke(costPriceKurus: Long, marginPercent: Double): Result<Long> = runCatching {
        require(costPriceKurus >= 0L) { "Alış fiyatı geçersiz" }
        require(marginPercent > 0.0 && marginPercent < 100.0) { "Kâr marjı 0 ile 100 arasında olmalıdır" }

        val multiplier = BigDecimal.ONE.subtract(BigDecimal.valueOf(marginPercent).divide(BigDecimal(100), 6, RoundingMode.HALF_UP))
        val salePriceRaw = BigDecimal(costPriceKurus)
            .divide(multiplier, 0, RoundingMode.CEILING)
            .longValueExact()

        MoneyUtils.roundUpToWholeTL(salePriceRaw)
    }
}


