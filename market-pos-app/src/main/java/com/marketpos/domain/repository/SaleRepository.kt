package com.marketpos.domain.repository

import com.marketpos.domain.model.CartItem
import com.marketpos.domain.model.ProductAggregateReport
import com.marketpos.domain.model.Sale
import com.marketpos.domain.model.SaleItem
import com.marketpos.domain.model.SummaryReport

interface SaleRepository {
    suspend fun createSale(cartItems: List<CartItem>): Result<Long>
    suspend fun getSummary(fromEpochMillis: Long, toEpochMillis: Long): SummaryReport
    suspend fun getTopSelling(fromEpochMillis: Long, toEpochMillis: Long, limit: Int = 10): List<ProductAggregateReport>
    suspend fun getTopProfit(fromEpochMillis: Long, toEpochMillis: Long, limit: Int = 10): List<ProductAggregateReport>
    suspend fun getSoldQuantity(productBarcode: String, fromEpochMillis: Long, toEpochMillis: Long): Int
    suspend fun listSales(fromEpochMillis: Long, toEpochMillis: Long): List<Sale>
    suspend fun listSaleItemsByRange(fromEpochMillis: Long, toEpochMillis: Long): List<SaleItem>
    suspend fun getSaleById(saleId: Long): Sale?
    suspend fun getSaleItems(saleId: Long): List<SaleItem>
    suspend fun getTotalSaleCount(): Int
}
