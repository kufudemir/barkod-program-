package com.marketpos.feature.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marketpos.core.util.DateUtils
import com.marketpos.core.util.MoneyUtils
import com.marketpos.domain.model.DiscountedProductReport
import com.marketpos.domain.model.HourlySalesReport
import com.marketpos.domain.model.Product
import com.marketpos.domain.model.ProductAggregateReport
import com.marketpos.domain.model.Sale
import com.marketpos.domain.model.SaleItem
import com.marketpos.domain.model.SummaryReport
import com.marketpos.domain.repository.ProductRepository
import com.marketpos.domain.repository.SaleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Instant
import java.time.ZoneId
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class ReportRangePreset(val label: String) {
    TODAY("Bugun"),
    THIS_WEEK("Bu Hafta"),
    THIS_MONTH("Bu Ay"),
    CUSTOM("Özel Aralik")
}

data class HourlySalesUi(
    val hourLabel: String,
    val saleCount: Int,
    val totalAmountLabel: String
)

data class DiscountedProductUi(
    val productName: String,
    val quantityLabel: String,
    val discountLabel: String
)

data class SaleHistoryUi(
    val saleId: Long,
    val createdAtLabel: String,
    val itemCountLabel: String,
    val totalLabel: String,
    val profitLabel: String
)

data class SaleDetailItemUi(
    val name: String,
    val quantityLabel: String,
    val unitPriceLabel: String,
    val lineTotalLabel: String
)

data class SaleDetailUi(
    val saleId: Long,
    val createdAtLabel: String,
    val totalLabel: String,
    val profitLabel: String,
    val items: List<SaleDetailItemUi>
)

data class ProductRiskUi(
    val name: String,
    val stockLabel: String,
    val riskLabel: String
)

data class PriceChangeUi(
    val name: String,
    val groupLabel: String?,
    val updatedAtLabel: String,
    val currentPriceLabel: String
)

data class ReportsUiState(
    val selectedRange: ReportRangePreset = ReportRangePreset.TODAY,
    val customStartDateMillis: Long = DateUtils.dayRange().first,
    val customEndDateMillis: Long = DateUtils.dayRange().last,
    val rangeLabel: String = "Bugun",
    val summary: SummaryReport = SummaryReport(0L, 0L, 0),
    val topSelling: List<ProductAggregateReport> = emptyList(),
    val topProfit: List<ProductAggregateReport> = emptyList(),
    val hourlySales: List<HourlySalesUi> = emptyList(),
    val discountedProducts: List<DiscountedProductUi> = emptyList(),
    val stockRiskProducts: List<ProductRiskUi> = emptyList(),
    val priceChanges: List<PriceChangeUi> = emptyList(),
    val saleHistory: List<SaleHistoryUi> = emptyList(),
    val selectedSaleDetail: SaleDetailUi? = null,
    val isLoading: Boolean = true
)

@HiltViewModel
class ReportsViewModel @Inject constructor(
    private val saleRepository: SaleRepository,
    private val productRepository: ProductRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReportsUiState())
    val uiState: StateFlow<ReportsUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val range = selectedRangeToEpoch()
            val summary = saleRepository.getSummary(range.first, range.last)
            val topSelling = saleRepository.getTopSelling(range.first, range.last, 10)
            val topProfit = saleRepository.getTopProfit(range.first, range.last, 10)
            val sales = saleRepository.listSales(range.first, range.last)
            val saleItems = saleRepository.listSaleItemsByRange(range.first, range.last)
            val products = productRepository.listAllActiveOnce()

            _uiState.value = _uiState.value.copy(
                summary = summary,
                topSelling = topSelling,
                topProfit = topProfit,
                hourlySales = buildHourlySales(sales),
                discountedProducts = buildDiscountedProducts(saleItems, products),
                stockRiskProducts = buildStockRisk(products),
                priceChanges = buildPriceChanges(products, range.first, range.last),
                saleHistory = sales.map { it.toHistoryUi() },
                rangeLabel = buildRangeLabel(),
                isLoading = false
            )
        }
    }

    fun selectRange(range: ReportRangePreset) {
        _uiState.value = _uiState.value.copy(selectedRange = range)
        refresh()
    }

    fun updateCustomStartDate(dateMillis: Long) {
        val start = DateUtils.startOfDay(dateMillis)
        val normalizedEnd = maxOf(_uiState.value.customEndDateMillis, start)
        _uiState.value = _uiState.value.copy(
            selectedRange = ReportRangePreset.CUSTOM,
            customStartDateMillis = start,
            customEndDateMillis = DateUtils.endOfDay(normalizedEnd)
        )
        refresh()
    }

    fun updateCustomEndDate(dateMillis: Long) {
        val end = DateUtils.endOfDay(dateMillis)
        val normalizedStart = minOf(_uiState.value.customStartDateMillis, end)
        _uiState.value = _uiState.value.copy(
            selectedRange = ReportRangePreset.CUSTOM,
            customStartDateMillis = DateUtils.startOfDay(normalizedStart),
            customEndDateMillis = end
        )
        refresh()
    }

    fun openSaleDetail(saleId: Long) {
        viewModelScope.launch {
            val sale = saleRepository.listSales(
                selectedRangeToEpoch().first,
                selectedRangeToEpoch().last
            ).firstOrNull { it.saleId == saleId } ?: return@launch
            val items = saleRepository.getSaleItems(saleId)
            _uiState.value = _uiState.value.copy(
                selectedSaleDetail = SaleDetailUi(
                    saleId = sale.saleId,
                    createdAtLabel = DateUtils.formatDateTime(sale.createdAt),
                    totalLabel = MoneyUtils.formatKurus(sale.totalAmountKurus),
                    profitLabel = MoneyUtils.formatKurus(sale.profitKurus),
                    items = items.map { it.toDetailItemUi() }
                )
            )
        }
    }

    fun closeSaleDetail() {
        _uiState.value = _uiState.value.copy(selectedSaleDetail = null)
    }

    private fun selectedRangeToEpoch(): LongRange {
        return when (_uiState.value.selectedRange) {
            ReportRangePreset.TODAY -> DateUtils.dayRange()
            ReportRangePreset.THIS_WEEK -> DateUtils.weekRange()
            ReportRangePreset.THIS_MONTH -> DateUtils.monthRange()
            ReportRangePreset.CUSTOM -> _uiState.value.customStartDateMillis.._uiState.value.customEndDateMillis
        }
    }

    private fun buildRangeLabel(): String {
        return when (_uiState.value.selectedRange) {
            ReportRangePreset.CUSTOM -> {
                "${DateUtils.formatDate(_uiState.value.customStartDateMillis)} - ${DateUtils.formatDate(_uiState.value.customEndDateMillis)}"
            }
            else -> _uiState.value.selectedRange.label
        }
    }

    private fun buildHourlySales(sales: List<Sale>): List<HourlySalesUi> {
        val byHour = sales.groupBy {
            Instant.ofEpochMilli(it.createdAt).atZone(ZoneId.systemDefault()).hour
        }.map { (hour, hourSales) ->
            HourlySalesReport(
                hour = hour,
                saleCount = hourSales.size,
                totalAmountKurus = hourSales.sumOf { it.totalAmountKurus }
            )
        }.sortedBy { it.hour }

        return byHour.map {
            HourlySalesUi(
                hourLabel = "%02d:00".format(it.hour),
                saleCount = it.saleCount,
                totalAmountLabel = MoneyUtils.formatKurus(it.totalAmountKurus)
            )
        }
    }

    private fun buildDiscountedProducts(
        saleItems: List<SaleItem>,
        products: List<Product>
    ): List<DiscountedProductUi> {
        val grouped = saleItems.groupBy { it.productBarcode }
            .mapNotNull { (barcode, items) ->
                val estimatedDiscount = items.sumOf { item ->
                    ((item.unitBaseSalePriceKurusSnapshot - item.unitSalePriceKurusSnapshot).coerceAtLeast(0L)) * item.quantity
                }
                if (estimatedDiscount <= 0L) return@mapNotNull null

                DiscountedProductReport(
                    productBarcode = barcode,
                    productName = items.first().productNameSnapshot,
                    totalQuantity = items.sumOf { it.quantity },
                    estimatedDiscountKurus = estimatedDiscount
                )
            }
            .sortedByDescending { it.estimatedDiscountKurus }
            .take(10)

        return grouped.map {
            DiscountedProductUi(
                productName = it.productName,
                quantityLabel = "${it.totalQuantity} adet",
                discountLabel = MoneyUtils.formatKurus(it.estimatedDiscountKurus)
            )
        }
    }

    private fun buildStockRisk(products: List<Product>): List<ProductRiskUi> {
        return products
            .filter { it.stockQty <= it.minStockQty * 2 }
            .sortedBy {
                when {
                    it.stockQty <= 0 -> -1000
                    else -> it.stockQty - it.minStockQty
                }
            }
            .take(10)
            .map { product ->
                val riskLabel = when {
                    product.stockQty <= 0 -> "Stoksuz"
                    product.stockQty <= product.minStockQty -> "Kritik"
                    else -> "Dusuk"
                }
                ProductRiskUi(
                    name = product.name,
                    stockLabel = "Stok: ${product.stockQty} / Min: ${product.minStockQty}",
                    riskLabel = riskLabel
                )
            }
    }

    private fun buildPriceChanges(products: List<Product>, from: Long, to: Long): List<PriceChangeUi> {
        return products
            .filter { it.updatedAt in from..to }
            .sortedByDescending { it.updatedAt }
            .take(10)
            .map {
                PriceChangeUi(
                    name = it.name,
                    groupLabel = it.groupName,
                    updatedAtLabel = DateUtils.formatDateTime(it.updatedAt),
                    currentPriceLabel = MoneyUtils.formatKurus(it.salePriceKurus)
                )
            }
    }

    private fun Sale.toHistoryUi(): SaleHistoryUi {
        return SaleHistoryUi(
            saleId = saleId,
            createdAtLabel = DateUtils.formatDateTime(createdAt),
            itemCountLabel = "$itemCount ürün",
            totalLabel = MoneyUtils.formatKurus(totalAmountKurus),
            profitLabel = MoneyUtils.formatKurus(profitKurus)
        )
    }

    private fun SaleItem.toDetailItemUi(): SaleDetailItemUi {
        return SaleDetailItemUi(
            name = productNameSnapshot,
            quantityLabel = "$quantity adet",
            unitPriceLabel = MoneyUtils.formatKurus(unitSalePriceKurusSnapshot),
            lineTotalLabel = MoneyUtils.formatKurus(lineTotalKurus)
        )
    }
}

