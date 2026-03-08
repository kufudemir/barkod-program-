package com.marketpos.feature.product

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.marketpos.core.util.DateUtils
import com.marketpos.core.util.MoneyUtils
import com.marketpos.domain.model.AppMode
import com.marketpos.ui.components.PinDialog

private enum class ProtectedAction { EDIT, UPDATE_PRICE_MANUAL, UPDATE_PRICE_PERCENT_INCREASE, UPDATE_PRICE_PERCENT_DECREASE, INCREASE_STOCK }

@Composable
fun ProductDetailScreen(
    viewModel: ProductDetailViewModel,
    onBack: () -> Unit,
    onNavigateEdit: (String) -> Unit,
    onNavigateScan: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbar = remember { SnackbarHostState() }
    var showManualPriceDialog by remember { mutableStateOf(false) }
    var showPercentDialog by remember { mutableStateOf(false) }
    var percentIncrease by remember { mutableStateOf(true) }
    var showStockDialog by remember { mutableStateOf(false) }
    var pendingAction by remember { mutableStateOf<ProtectedAction?>(null) }
    var pendingValue by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is ProductDetailEvent.ShowMessage -> snackbar.showSnackbar(event.message)
                is ProductDetailEvent.NavigateEdit -> onNavigateEdit(event.barcode)
                ProductDetailEvent.NavigateScan -> onNavigateScan()
            }
        }
    }

    Scaffold(snackbarHost = { com.marketpos.ui.components.CenteredSnackbarHost(snackbar) }) { innerPadding ->
        val product = uiState.product
        if (product == null) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val isCriticalStock = product.stockQty <= product.minStockQty

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Ürün Bilgisi", style = MaterialTheme.typography.labelLarge)
                    Text(
                        product.name,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text("Satış Fiyatı", style = MaterialTheme.typography.titleMedium)
                    Text(
                        MoneyUtils.formatKurus(product.salePriceKurus),
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            if (isCriticalStock) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            "KRITIK STOK UYARISI",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Text(
                            "Stok: ${product.stockQty} - Min Stok: ${product.minStockQty}",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }

            Text("Barkod: ${product.barcode}")
            Text("Stok: ${product.stockQty}")
            Text("Min Stok: ${product.minStockQty}")
            product.note?.takeIf { it.isNotBlank() }?.let { Text("Not: $it") }
            Text("Son Güncelleme: ${DateUtils.formatDateTime(product.updatedAt)}")
            Text("Mod: ${if (uiState.mode == AppMode.ADMIN) "ADMIN" else "KASIYER"}")
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text("Sepete Eklenecek Fiyat", style = MaterialTheme.typography.titleMedium)
                    Text(
                        uiState.pendingCartPriceLabel ?: MoneyUtils.formatKurus(product.salePriceKurus),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    if (uiState.hasPendingCartOverride) {
                        Text(
                            "Liste fiyatı: ${MoneyUtils.formatKurus(product.salePriceKurus)}",
                            style = MaterialTheme.typography.bodySmall
                        )
                        OutlinedButton(onClick = viewModel::resetPendingCartPrice, modifier = Modifier.fillMaxWidth()) {
                            Text("Liste Fiyatına Dön")
                        }
                    } else {
                        Text(
                            "Aşağıdaki fiyat işlemleri sadece sepete eklenecek satıra uygulanır.",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            Button(onClick = viewModel::addToCart, modifier = Modifier.fillMaxWidth()) {
                Text("Sepete Ekle")
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(onClick = { showManualPriceDialog = true }, modifier = Modifier.weight(1f)) {
                    Text("Sepet Fiyatı")
                }
                OutlinedButton(
                    onClick = {
                        percentIncrease = true
                        showPercentDialog = true
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Sepet % Artır")
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(
                    onClick = {
                        percentIncrease = false
                        showPercentDialog = true
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Sepet % Azalt")
                }
                OutlinedButton(onClick = { showStockDialog = true }, modifier = Modifier.weight(1f)) {
                    Text("Stok Artır")
                }
            }

            Button(
                onClick = {
                    if (uiState.mode == AppMode.ADMIN) viewModel.requestEdit(null)
                    else pendingAction = ProtectedAction.EDIT
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Ürün Düzenle")
            }

            OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
                Text("Geri")
            }
        }
    }

    if (showManualPriceDialog) {
        var input by remember(uiState.product?.salePriceKurus) {
            mutableStateOf(uiState.pendingCartEffectivePriceKurus?.let(MoneyUtils::formatKurusForInput).orEmpty())
        }
        AlertDialog(
            onDismissRequest = { showManualPriceDialog = false },
            title = { Text("Sepete Özel Fiyat") },
            text = {
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = MoneyUtils.sanitizeTlTypingInput(it) },
                    label = { Text("Yeni Fiyat (TL)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
            },
            dismissButton = { TextButton(onClick = { showManualPriceDialog = false }) { Text("Vazgeç") } },
            confirmButton = {
                Button(onClick = {
                    showManualPriceDialog = false
                    if (uiState.mode == AppMode.ADMIN) {
                        viewModel.updatePriceManual(input, null)
                    } else {
                        pendingAction = ProtectedAction.UPDATE_PRICE_MANUAL
                        pendingValue = input
                    }
                }) { Text("Uygula") }
            }
        )
    }

    if (showPercentDialog) {
        var input by remember { mutableStateOf("10") }
        AlertDialog(
            onDismissRequest = { showPercentDialog = false },
            title = { Text(if (percentIncrease) "Sepete Özel % Artış" else "Sepete Özel % Azalış") },
            text = {
                OutlinedTextField(
                    value = input,
                    onValueChange = {
                        input = it.filter { char -> char.isDigit() || char == ',' || char == '.' }
                    },
                    label = { Text("Yüzde") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
            },
            dismissButton = { TextButton(onClick = { showPercentDialog = false }) { Text("Vazgeç") } },
            confirmButton = {
                Button(onClick = {
                    showPercentDialog = false
                    if (uiState.mode == AppMode.ADMIN) {
                        viewModel.updatePricePercent(input, percentIncrease, null)
                    } else {
                        pendingAction = if (percentIncrease) {
                            ProtectedAction.UPDATE_PRICE_PERCENT_INCREASE
                        } else {
                            ProtectedAction.UPDATE_PRICE_PERCENT_DECREASE
                        }
                        pendingValue = input
                    }
                }) { Text("Uygula") }
            }
        )
    }

    if (showStockDialog) {
        var input by remember { mutableStateOf("1") }
        AlertDialog(
            onDismissRequest = { showStockDialog = false },
            title = { Text("Stok Artır") },
            text = {
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it.filter(Char::isDigit) },
                    label = { Text("Adet") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            },
            dismissButton = { TextButton(onClick = { showStockDialog = false }) { Text("Vazgeç") } },
            confirmButton = {
                Button(onClick = {
                    showStockDialog = false
                    if (uiState.mode == AppMode.ADMIN) {
                        viewModel.increaseStock(input, null)
                    } else {
                        pendingAction = ProtectedAction.INCREASE_STOCK
                        pendingValue = input
                    }
                }) { Text("Uygula") }
            }
        )
    }

    if (pendingAction != null) {
        PinDialog(
            title = "PIN Doğrulama",
            onDismiss = { pendingAction = null },
            onConfirm = { pin ->
                val action = pendingAction
                pendingAction = null
                when (action) {
                    ProtectedAction.EDIT -> viewModel.requestEdit(pin)
                    ProtectedAction.UPDATE_PRICE_MANUAL -> viewModel.updatePriceManual(pendingValue, pin)
                    ProtectedAction.UPDATE_PRICE_PERCENT_INCREASE -> viewModel.updatePricePercent(
                        percentInput = pendingValue,
                        increase = true,
                        pinIfCashier = pin
                    )
                    ProtectedAction.UPDATE_PRICE_PERCENT_DECREASE -> viewModel.updatePricePercent(
                        percentInput = pendingValue,
                        increase = false,
                        pinIfCashier = pin
                    )
                    ProtectedAction.INCREASE_STOCK -> viewModel.increaseStock(pendingValue, pin)
                    null -> Unit
                }
            }
        )
    }
}


