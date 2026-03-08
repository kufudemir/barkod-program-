package com.marketpos.feature.reports

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.marketpos.domain.model.Product

@Composable
fun StockTrackingScreen(
    viewModel: StockTrackingViewModel,
    onNavigateStockCount: () -> Unit,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbar = remember { SnackbarHostState() }
    var editProduct by remember { mutableStateOf<Product?>(null) }
    var pendingStock by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is StockTrackingEvent.ShowMessage -> snackbar.showSnackbar(event.message)
            }
        }
    }

    Scaffold(snackbarHost = { com.marketpos.ui.components.CenteredSnackbarHost(snackbar) }) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("Stok Takibi", style = MaterialTheme.typography.headlineSmall)
            Text(
                "Burada stok miktarı minimum stok seviyesinin 2 katı veya altında olan ürünler listelenir.",
                style = MaterialTheme.typography.bodyMedium
            )
            Button(onClick = onNavigateStockCount, modifier = Modifier.fillMaxWidth()) {
                Text("Stok Sayım Modu")
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StockFilter.entries.forEach { filter ->
                    OutlinedButton(
                        onClick = { viewModel.selectFilter(filter) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(if (uiState.selectedFilter == filter) "${filter.label} *" else filter.label)
                    }
                }
            }

            if (uiState.products.isEmpty()) {
                Text("Takip gerektiren düşük stoklu ürün yok.")
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.products, key = { it.barcode }) { product ->
                        val isCritical = product.stockQty <= product.minStockQty
                        val statusColor = if (isCritical) Color(0xFFB3261E) else Color(0xFF8A5A00)

                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(product.name, style = MaterialTheme.typography.titleMedium)
                                Text("Barkod: ${product.barcode}")
                                Text("Stok: ${product.stockQty}")
                                Text("Minimum stok: ${product.minStockQty}")
                                Text(
                                    if (isCritical) "Durum: Kritik stok" else "Durum: Düşük stok",
                                    color = statusColor
                                )
                                Text(
                                    "Hızlı stok işlemleri",
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = { viewModel.adjustStock(product.barcode, product.stockQty, 5) },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("+5")
                                    }
                                    OutlinedButton(
                                        onClick = { viewModel.adjustStock(product.barcode, product.stockQty, 10) },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("+10")
                                    }
                                    OutlinedButton(
                                        onClick = { viewModel.adjustStock(product.barcode, product.stockQty, 20) },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("+20")
                                    }
                                    OutlinedButton(
                                        onClick = { viewModel.adjustStock(product.barcode, product.stockQty, 50) },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("+50")
                                    }
                                }
                                OutlinedButton(
                                    onClick = {
                                        editProduct = product
                                        pendingStock = product.stockQty.toString()
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Stok Düzenle")
                                }
                            }
                        }
                    }
                }
            }

            OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
                Text("Geri")
            }
        }
    }

    if (editProduct != null) {
        val product = editProduct!!
        AlertDialog(
            onDismissRequest = { editProduct = null },
            title = { Text("Stok Düzenle") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(product.name)
                    OutlinedTextField(
                        value = pendingStock,
                        onValueChange = { pendingStock = it.filter(Char::isDigit) },
                        label = { Text("Yeni stok miktarı") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                val value = pendingStock.toIntOrNull() ?: product.stockQty
                                pendingStock = (value + 5).toString()
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("+5")
                        }
                        OutlinedButton(
                            onClick = {
                                val value = pendingStock.toIntOrNull() ?: product.stockQty
                                pendingStock = (value + 10).toString()
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("+10")
                        }
                        OutlinedButton(
                            onClick = {
                                val value = pendingStock.toIntOrNull() ?: product.stockQty
                                pendingStock = (value + 20).toString()
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("+20")
                        }
                        OutlinedButton(
                            onClick = {
                                val value = pendingStock.toIntOrNull() ?: product.stockQty
                                pendingStock = (value + 50).toString()
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("+50")
                        }
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { editProduct = null }) {
                    Text("Vazgeç")
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        editProduct = null
                        viewModel.updateStock(product.barcode, pendingStock)
                    }
                ) {
                    Text("Kaydet")
                }
            }
        )
    }
}

