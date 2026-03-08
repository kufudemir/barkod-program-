package com.marketpos.feature.reports

import android.app.DatePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.marketpos.core.util.DateUtils
import com.marketpos.core.util.MoneyUtils
import java.util.Calendar

enum class SalesReportSection(val routeKey: String, val label: String) {
    SUMMARY("summary", "Satış Raporları"),
    RECENT_SALES("recent_sales", "Son Satışlar"),
    TOP_PRODUCTS("top_products", "En Çok Satılanlar");

    companion object {
        fun fromRouteKey(value: String?): SalesReportSection {
            return entries.firstOrNull { it.routeKey == value } ?: SUMMARY
        }
    }
}

@Composable
fun ReportsScreen(
    viewModel: ReportsViewModel,
    initialSection: String?,
    onOpenStockTracking: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    var selectedSection by rememberSaveable(initialSection) {
        mutableStateOf(SalesReportSection.fromRouteKey(initialSection))
    }

    fun showDatePicker(initialMillis: Long, onPicked: (Long) -> Unit) {
        val calendar = Calendar.getInstance().apply { timeInMillis = initialMillis }
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val picked = Calendar.getInstance().apply {
                    set(Calendar.YEAR, year)
                    set(Calendar.MONTH, month)
                    set(Calendar.DAY_OF_MONTH, dayOfMonth)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis
                onPicked(picked)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { innerPadding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Text(
                    text = "Raporlar",
                    style = MaterialTheme.typography.headlineSmall
                )
            }
            item {
                ReportCard {
                    Text("Alt Menü", style = MaterialTheme.typography.titleMedium)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SalesReportSection.entries.forEach { section ->
                            ReportChoiceButton(
                                selected = selectedSection == section,
                                onClick = { selectedSection = section },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(section.label)
                            }
                        }
                    }
                }
            }
            item {
                ReportCard {
                    Text("Dönem Seç", style = MaterialTheme.typography.titleMedium)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ReportRangePreset.entries.forEach { range ->
                            ReportChoiceButton(
                                selected = uiState.selectedRange == range,
                                onClick = { viewModel.selectRange(range) },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(range.label)
                            }
                        }
                    }
                    if (uiState.selectedRange == ReportRangePreset.CUSTOM) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    showDatePicker(uiState.customStartDateMillis) { viewModel.updateCustomStartDate(it) }
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Başlangıç: ${DateUtils.formatDate(uiState.customStartDateMillis)}")
                            }
                            OutlinedButton(
                                onClick = {
                                    showDatePicker(uiState.customEndDateMillis) { viewModel.updateCustomEndDate(it) }
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Bitiş: ${DateUtils.formatDate(uiState.customEndDateMillis)}")
                            }
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(onClick = viewModel::refresh, modifier = Modifier.weight(1f)) {
                            Text("Yenile")
                        }
                        OutlinedButton(onClick = onOpenStockTracking, modifier = Modifier.weight(1f)) {
                            Text("Stok Takibi")
                        }
                    }
                }
            }

            when (selectedSection) {
                SalesReportSection.SUMMARY -> {
                    item {
                        ReportCard {
                            Text("Seçili Dönem: ${uiState.rangeLabel}", style = MaterialTheme.typography.titleMedium)
                            Text(
                                "Ciro: ${MoneyUtils.formatKurus(uiState.summary.totalAmountKurus)}",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text("Kar: ${MoneyUtils.formatKurus(uiState.summary.totalProfitKurus)}")
                            Text("Satış Adedi: ${uiState.summary.saleCount}")
                        }
                    }

                    item { SectionTitle("Saat Bazlı Satış") }
                    items(uiState.hourlySales, key = { it.hourLabel }) { item ->
                        ReportCard {
                            Text(item.hourLabel, fontWeight = FontWeight.SemiBold)
                            Text("${item.saleCount} satış")
                            Text(item.totalAmountLabel)
                        }
                    }

                    item { SectionTitle("Fiyat Değişim Özeti") }
                    items(uiState.priceChanges, key = { it.name + it.updatedAtLabel }) { item ->
                        ReportCard {
                            Text(item.name, fontWeight = FontWeight.SemiBold)
                            item.groupLabel?.let { Text("Grup: $it") }
                            Text("Güncelleme: ${item.updatedAtLabel}")
                            Text("Mevcut fiyat: ${item.currentPriceLabel}")
                        }
                    }
                }

                SalesReportSection.RECENT_SALES -> {
                    item {
                        Text(
                            "Son Satışlar",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                    items(uiState.saleHistory, key = { it.saleId }) { item ->
                        ReportCard(
                            modifier = Modifier,
                            onClick = { viewModel.openSaleDetail(item.saleId) }
                        ) {
                            Text("Satış #${item.saleId}", fontWeight = FontWeight.SemiBold)
                            Text(item.createdAtLabel)
                            Text(item.itemCountLabel)
                            Text("Toplam: ${item.totalLabel}")
                            Text("Kar: ${item.profitLabel}")
                        }
                    }
                }

                SalesReportSection.TOP_PRODUCTS -> {
                    item { SectionTitle("En Çok Satan Ürünler") }
                    items(uiState.topSelling, key = { "s-${it.productBarcode}" }) { item ->
                        ReportCard {
                            Text(item.productName, fontWeight = FontWeight.SemiBold)
                            Text("${item.totalQuantity} adet")
                        }
                    }

                    item { SectionTitle("En Çok Kazandıran Ürünler") }
                    items(uiState.topProfit, key = { "p-${it.productBarcode}" }) { item ->
                        ReportCard {
                            Text(item.productName, fontWeight = FontWeight.SemiBold)
                            Text(MoneyUtils.formatKurus(item.totalProfitKurus))
                        }
                    }

                    if (uiState.discountedProducts.isNotEmpty()) {
                        item { SectionTitle("İndirim Uygulanan Ürünler") }
                        items(uiState.discountedProducts, key = { it.productName }) { item ->
                            ReportCard {
                                Text(item.productName, fontWeight = FontWeight.SemiBold)
                                Text(item.quantityLabel)
                                Text("Tahmini indirim: ${item.discountLabel}")
                            }
                        }
                    }
                }
            }

            item {
                OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
                    Text("Geri")
                }
            }
        }
    }

    uiState.selectedSaleDetail?.let { detail ->
        SaleReceiptDialog(detail = detail, onDismiss = viewModel::closeSaleDetail)
    }
}

@Composable
private fun SaleReceiptDialog(
    detail: SaleDetailUi,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Satış Fişi #${detail.saleId}") },
        text = {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = detail.createdAtLabel,
                        style = MaterialTheme.typography.bodySmall
                    )
                    HorizontalDivider()
                    detail.items.forEach { item ->
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(item.name, fontWeight = FontWeight.SemiBold)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    "${item.quantityLabel} x ${item.unitPriceLabel}",
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    item.lineTotalLabel,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    HorizontalDivider()
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("TOPLAM", fontWeight = FontWeight.Bold)
                        Text(detail.totalLabel, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("KAR")
                        Text(detail.profitLabel, fontFamily = FontFamily.Monospace)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Kapat")
            }
        }
    )
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, style = MaterialTheme.typography.titleMedium)
}

@Composable
private fun ReportCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            content = content
        )
    }
}

@Composable
private fun ReportChoiceButton(
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable androidx.compose.foundation.layout.RowScope.() -> Unit
) {
    if (selected) {
        Button(onClick = onClick, modifier = modifier, content = content)
    } else {
        FilledTonalButton(onClick = onClick, modifier = modifier, content = content)
    }
}


