package com.marketpos.core.util

import org.junit.Assert.assertEquals
import org.junit.Test

class MoneyUtilsTest {

    @Test
    fun `15 tl yüzde 10 artis 17 tl olmali`() {
        val result = MoneyUtils.percentIncrease(1500, 10.0)
        assertEquals(1700, result)
    }

    @Test
    fun `yuvarlama her zaman yukari olmali`() {
        assertEquals(1700, MoneyUtils.roundUpToWholeTL(1601))
        assertEquals(1700, MoneyUtils.roundUpToWholeTL(1650))
        assertEquals(1700, MoneyUtils.roundUpToWholeTL(1699))
        assertEquals(1600, MoneyUtils.roundUpToWholeTL(1600))
    }
}
