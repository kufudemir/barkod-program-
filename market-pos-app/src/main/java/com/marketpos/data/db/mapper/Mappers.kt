package com.marketpos.data.db.mapper

import com.marketpos.data.db.entity.ProductEntity
import com.marketpos.data.db.entity.SaleEntity
import com.marketpos.data.db.entity.SaleItemEntity
import com.marketpos.data.db.query.ProductAggregateRow
import com.marketpos.data.db.query.SummaryRow
import com.marketpos.domain.model.Product
import com.marketpos.domain.model.ProductAggregateReport
import com.marketpos.domain.model.Sale
import com.marketpos.domain.model.SaleItem
import com.marketpos.domain.model.SummaryReport

fun ProductEntity.toDomain(): Product = Product(
    barcode = barcode,
    name = name,
    groupName = groupName,
    salePriceKurus = salePriceKurus,
    costPriceKurus = costPriceKurus,
    stockQty = stockQty,
    minStockQty = minStockQty,
    note = note,
    createdAt = createdAt,
    updatedAt = updatedAt,
    isActive = isActive
)

fun Product.toEntity(): ProductEntity = ProductEntity(
    barcode = barcode,
    name = name,
    groupName = groupName,
    salePriceKurus = salePriceKurus,
    costPriceKurus = costPriceKurus,
    stockQty = stockQty,
    minStockQty = minStockQty,
    note = note,
    createdAt = createdAt,
    updatedAt = updatedAt,
    isActive = isActive
)

fun SaleEntity.toDomain(): Sale = Sale(
    saleId = saleId,
    createdAt = createdAt,
    totalAmountKurus = totalAmountKurus,
    totalCostKurus = totalCostKurus,
    profitKurus = profitKurus,
    itemCount = itemCount,
    status = status
)

fun SaleItemEntity.toDomain(): SaleItem = SaleItem(
    saleItemId = saleItemId,
    saleId = saleId,
    productBarcode = productBarcode,
    productNameSnapshot = productNameSnapshot,
    unitBaseSalePriceKurusSnapshot = unitBaseSalePriceKurusSnapshot,
    unitSalePriceKurusSnapshot = unitSalePriceKurusSnapshot,
    unitCostPriceKurusSnapshot = unitCostPriceKurusSnapshot,
    quantity = quantity,
    lineTotalKurus = lineTotalKurus,
    lineCostKurus = lineCostKurus
)

fun SummaryRow.toDomain(): SummaryReport = SummaryReport(
    totalAmountKurus = totalAmountKurus ?: 0L,
    totalProfitKurus = totalProfitKurus ?: 0L,
    saleCount = saleCount ?: 0
)

fun ProductAggregateRow.toDomain(): ProductAggregateReport = ProductAggregateReport(
    productBarcode = productBarcode,
    productName = productName,
    totalQuantity = totalQuantity,
    totalProfitKurus = totalProfitKurus
)
