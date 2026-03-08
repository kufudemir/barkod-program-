package com.marketpos.feature.product

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.marketpos.core.util.DateUtils
import com.marketpos.core.util.MoneyUtils
import com.marketpos.domain.model.AppMode
import com.marketpos.domain.model.Product
import com.marketpos.ui.components.PinDialog

@Composable
fun ProductListScreen(
    viewModel: ProductListViewModel,
    onBack: () -> Unit,
    onOpenEdit: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbar = remember { SnackbarHostState() }
    var priceDialogProduct by remember { mutableStateOf<Product?>(null) }
    var stockDialogProduct by remember { mutableStateOf<Product?>(null) }
    var deleteDialogProduct by remember { mutableStateOf<Product?>(null) }
    var pendingPrice by remember { mutableStateOf("") }
    var pendingStock by remember { mutableStateOf("") }
    var askPinForBarcode by remember { mutableStateOf<String?>(null) }
    var askPinForStockBarcode by remember { mutableStateOf<String?>(null) }
    var askPinForDeleteBarcode by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is ProductListEvent.ShowMessage -> snackbar.showSnackbar(event.message)
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
            Text("Ürün Listesi", style = MaterialTheme.typography.headlineSmall)
            Text("Mod: ${if (uiState.mode == AppMode.ADMIN) "ADMIN" else "KASIYER"}")
            OutlinedTextField(
                value = uiState.query,
                onValueChange = viewModel::updateQuery,
                label = { Text("Ürün ara (isim/barkod)") },
                modifier = Modifier.fillMaxWidth()
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(onClick = viewModel::toggleCriticalOnly, modifier = Modifier.weight(1f)) {
                    Text(if (uiState.criticalOnly) "Kritik: Açık" else "Kritik Stok")
                }
                OutlinedButton(onClick = viewModel::toggleOutOfStockOnly, modifier = Modifier.weight(1f)) {
                    Text(if (uiState.outOfStockOnly) "Stoksuz: Açık" else "Stoksuzlari Göster")
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(onClick = viewModel::toggleRecentOnly, modifier = Modifier.weight(1f)) {
                    Text(if (uiState.recentOnly) "Son Duzenlenen: Açık" else "Son Duzenlenenler")
                }
                OutlinedButton(
                    onClick = {
                        val next = when (uiState.sortOption) {
                            ProductSortOption.NAME -> ProductSortOption.PRICE_DESC
                            ProductSortOption.PRICE_DESC -> ProductSortOption.STOCK_ASC
                            ProductSortOption.STOCK_ASC -> ProductSortOption.UPDATED_DESC
                            ProductSortOption.UPDATED_DESC -> ProductSortOption.NAME
                        }
                        viewModel.updateSortOption(next)
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Sirala: ${uiState.sortOption.label}")
                }
            }

            LazyColumn(modifier = Modifier.weight(1f)) {
                items(uiState.products, key = { it.barcode }) { product ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Text(product.name)
                        product.groupName?.takeIf { it.isNotBlank() }?.let {
                            Text("Grup: $it")
                        }
                        Text("Barkod: ${product.barcode}")
                        Text("Fiyat: ${MoneyUtils.formatKurus(product.salePriceKurus)}")
                        Text("Stok: ${product.stockQty}")
                        Text("Son güncelleme: ${DateUtils.formatDateTime(product.updatedAt)}")
                        if (product.stockQty <= product.minStockQty) {
                            Text("Kritik stok", color = MaterialTheme.colorScheme.error)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(onClick = {
                                priceDialogProduct = product
                                pendingPrice = MoneyUtils.formatKurusForInput(product.salePriceKurus)
                            }) { Text("Fiyat Değiştir") }
                            OutlinedButton(onClick = {
                                stockDialogProduct = product
                                pendingStock = product.stockQty.toString()
                            }) { Text("Stok Değiştir") }
                            OutlinedButton(onClick = { onOpenEdit(product.barcode) }) { Text("Duzenle") }
                            OutlinedButton(onClick = { deleteDialogProduct = product }) { Text("Sil") }
                        }
                    }
                }
            }

            Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
                Text("Geri")
            }
        }
    }

    if (priceDialogProduct != null) {
        val product = priceDialogProduct!!
        AlertDialog(
            onDismissRequest = { priceDialogProduct = null },
            title = { Text("Fiyat Güncelle") },
            text = {
                OutlinedTextField(
                    value = pendingPrice,
                    onValueChange = { pendingPrice = MoneyUtils.sanitizeTlTypingInput(it) },
                    label = { Text("Yeni fiyat (TL)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
            },
            dismissButton = {
                TextButton(onClick = { priceDialogProduct = null }) { Text("Vazgeç") }
            },
            confirmButton = {
                Button(onClick = {
                    priceDialogProduct = null
                    if (uiState.mode == AppMode.ADMIN) {
                        viewModel.updatePrice(product.barcode, pendingPrice, null)
                    } else {
                        askPinForBarcode = product.barcode
                    }
                }) { Text("Kaydet") }
            }
        )
    }

    if (askPinForBarcode != null) {
        val barcode = askPinForBarcode!!
        PinDialog(
            title = "PIN Gerekli",
            onDismiss = { askPinForBarcode = null },
            onConfirm = { pin ->
                askPinForBarcode = null
                viewModel.updatePrice(barcode, pendingPrice, pin)
            }
        )
    }

    if (stockDialogProduct != null) {
        val product = stockDialogProduct!!
        AlertDialog(
            onDismissRequest = { stockDialogProduct = null },
            title = { Text("Stok Güncelle") },
            text = {
                OutlinedTextField(
                    value = pendingStock,
                    onValueChange = { pendingStock = it.filter(Char::isDigit) },
                    label = { Text("Yeni stok") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            },
            dismissButton = {
                TextButton(onClick = { stockDialogProduct = null }) { Text("Vazgeç") }
            },
            confirmButton = {
                Button(onClick = {
                    stockDialogProduct = null
                    if (uiState.mode == AppMode.ADMIN) {
                        viewModel.updateStock(product.barcode, pendingStock, null)
                    } else {
                        askPinForStockBarcode = product.barcode
                    }
                }) { Text("Kaydet") }
            }
        )
    }

    if (askPinForStockBarcode != null) {
        val barcode = askPinForStockBarcode!!
        PinDialog(
            title = "Stok PIN",
            onDismiss = { askPinForStockBarcode = null },
            onConfirm = { pin ->
                askPinForStockBarcode = null
                viewModel.updateStock(barcode, pendingStock, pin)
            }
        )
    }

    if (deleteDialogProduct != null) {
        val product = deleteDialogProduct!!
        AlertDialog(
            onDismissRequest = { deleteDialogProduct = null },
            title = { Text("Ürün Sil") },
            text = { Text("${product.name} ürününü silmek istiyor musunuz?") },
            dismissButton = {
                TextButton(onClick = { deleteDialogProduct = null }) { Text("Vazgeç") }
            },
            confirmButton = {
                Button(onClick = {
                    deleteDialogProduct = null
                    if (uiState.mode == AppMode.ADMIN) {
                        viewModel.deleteProduct(product.barcode, null)
                    } else {
                        askPinForDeleteBarcode = product.barcode
                    }
                }) { Text("Sil") }
            }
        )
    }

    if (askPinForDeleteBarcode != null) {
        val barcode = askPinForDeleteBarcode!!
        PinDialog(
            title = "Silme PIN",
            onDismiss = { askPinForDeleteBarcode = null },
            onConfirm = { pin ->
                askPinForDeleteBarcode = null
                viewModel.deleteProduct(barcode, pin)
            }
        )
    }
}


