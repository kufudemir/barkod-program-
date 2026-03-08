package com.marketpos.feature.sale

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.marketpos.core.util.DateUtils
import com.marketpos.core.util.MoneyUtils
import com.marketpos.domain.model.CartItem
import com.marketpos.domain.model.AppMode
import com.marketpos.domain.model.HeldCart
import com.marketpos.domain.model.PremiumFeature
import com.marketpos.ui.components.PinDialog

@Composable
fun CartScreen(
    viewModel: CartViewModel,
    onNavigateScan: () -> Unit,
    onNavigatePremium: (PremiumFeature) -> Unit,
    onNavigateSaleSuccess: (Long) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbar = remember { SnackbarHostState() }
    var quantityDialogItem by remember { mutableStateOf<CartItem?>(null) }
    var pendingQuantity by remember { mutableStateOf("") }
    var priceDialogItem by remember { mutableStateOf<CartItem?>(null) }
    var pendingCustomPrice by remember { mutableStateOf("") }
    var askPinForPriceBarcode by remember { mutableStateOf<String?>(null) }
    var percentDiscountItem by remember { mutableStateOf<CartItem?>(null) }
    var pendingPercentDiscount by remember { mutableStateOf("") }
    var askPinForPercentBarcode by remember { mutableStateOf<String?>(null) }
    var fixedDiscountItem by remember { mutableStateOf<CartItem?>(null) }
    var pendingFixedDiscount by remember { mutableStateOf("") }
    var askPinForFixedBarcode by remember { mutableStateOf<String?>(null) }
    var showParkDialog by remember { mutableStateOf(false) }
    var pendingParkLabel by remember { mutableStateOf("") }
    var showHeldCartsDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is CartEvent.NavigateSaleSuccess -> onNavigateSaleSuccess(event.saleId)
                CartEvent.NavigateScan -> onNavigateScan()
                is CartEvent.ShowMessage -> snackbar.showSnackbar(event.message)
            }
        }
    }

    Scaffold(snackbarHost = { com.marketpos.ui.components.CenteredSnackbarHost(snackbar) }) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Sepet", style = MaterialTheme.typography.headlineSmall)
            Text("Mod: ${if (uiState.mode == AppMode.ADMIN) "ADMIN" else "KASIYER"}")

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Toplam Tutar", style = MaterialTheme.typography.titleMedium)
                    Text(
                        MoneyUtils.formatKurus(uiState.totalAmountKurus),
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Toplam Ürün Adedi: ${uiState.totalItemCount}")
                        Text("Satır: ${uiState.items.size}")
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                pendingParkLabel = ""
                                showParkDialog = true
                            },
                            modifier = Modifier.weight(1f),
                            enabled = uiState.items.isNotEmpty()
                        ) {
                            Text("Sepeti Beklet")
                        }
                        OutlinedButton(
                            onClick = { showHeldCartsDialog = true },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Bekleyenler (${uiState.heldCartCount})")
                        }
                    }
                }
            }

            if (uiState.items.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("Sepette ürün yok", style = MaterialTheme.typography.titleMedium)
                            Text("Barkod tarayarak veya seri ürün tarama modundan ürün ekleyin.")
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(uiState.items, key = { it.barcode }) { item ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(item.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                                Text("Barkod: ${item.barcode}", style = MaterialTheme.typography.bodySmall)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("${item.quantity} x ${MoneyUtils.formatKurus(item.salePriceKurus)}")
                                    Text(
                                        MoneyUtils.formatKurus(item.lineTotalKurus),
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                if (item.hasCustomPrice) {
                                    Text(
                                        "Özel fiyat aktif. Liste fiyatı: ${MoneyUtils.formatKurus(item.baseSalePriceKurus)}",
                                        color = MaterialTheme.colorScheme.primary,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedButton(onClick = { viewModel.decrease(item.barcode) }, modifier = Modifier.weight(1f)) {
                                        Text("-")
                                    }
                                    OutlinedButton(onClick = { viewModel.increase(item.barcode) }, modifier = Modifier.weight(1f)) {
                                        Text("+")
                                    }
                                    OutlinedButton(onClick = { viewModel.increaseBy(item.barcode, 5) }, modifier = Modifier.weight(1.2f)) {
                                        Text("+5")
                                    }
                                    OutlinedButton(onClick = { viewModel.increaseBy(item.barcode, 10) }, modifier = Modifier.weight(1.4f)) {
                                        Text("+10")
                                    }
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = {
                                            quantityDialogItem = item
                                            pendingQuantity = item.quantity.toString()
                                        },
                                        modifier = Modifier.weight(2f)
                                    ) {
                                        Text("Adet Gir")
                                    }
                                    OutlinedButton(
                                        onClick = {
                                            if (uiState.canUsePremiumPricing) {
                                                priceDialogItem = item
                                                pendingCustomPrice = MoneyUtils.formatKurusForInput(item.salePriceKurus)
                                            } else {
                                                onNavigatePremium(PremiumFeature.LINE_PRICE_OVERRIDE)
                                            }
                                        },
                                        modifier = Modifier.weight(2f)
                                    ) {
                                        Text(if (uiState.canUsePremiumPricing) "Fiyat Değiştir" else "Fiyat Değiştir [PRO]")
                                    }
                                    OutlinedButton(onClick = { viewModel.remove(item.barcode) }, modifier = Modifier.weight(2f)) {
                                        Text("Çıkar")
                                    }
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = {
                                            if (uiState.canUsePremiumPricing) {
                                                percentDiscountItem = item
                                                pendingPercentDiscount = ""
                                            } else {
                                                onNavigatePremium(PremiumFeature.LINE_PRICE_OVERRIDE)
                                            }
                                        },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(if (uiState.canUsePremiumPricing) "% Indirim" else "% Indirim [PRO]")
                                    }
                                    OutlinedButton(
                                        onClick = {
                                            if (uiState.canUsePremiumPricing) {
                                                fixedDiscountItem = item
                                                pendingFixedDiscount = ""
                                            } else {
                                                onNavigatePremium(PremiumFeature.LINE_PRICE_OVERRIDE)
                                            }
                                        },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(if (uiState.canUsePremiumPricing) "TL Indirim" else "TL Indirim [PRO]")
                                    }
                                }
                                if (item.hasCustomPrice) {
                                    OutlinedButton(
                                        onClick = {
                                            if (uiState.canUsePremiumPricing) {
                                                viewModel.resetPrice(item.barcode)
                                            } else {
                                                onNavigatePremium(PremiumFeature.LINE_PRICE_OVERRIDE)
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(if (uiState.canUsePremiumPricing) "Liste Fiyatına Dön" else "Liste Fiyatına Dön [PRO]")
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (uiState.isProcessing) {
                        CircularProgressIndicator()
                    }
                    Button(
                        onClick = viewModel::checkout,
                        enabled = uiState.items.isNotEmpty() && !uiState.isProcessing,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Satışı Tamamla")
                    }
                    OutlinedButton(onClick = viewModel::cancel, modifier = Modifier.fillMaxWidth()) {
                        Text("Tarama Ekranına Dön")
                    }
                }
            }
        }
    }

    if (quantityDialogItem != null) {
        val item = quantityDialogItem!!
        AlertDialog(
            onDismissRequest = { quantityDialogItem = null },
            title = { Text("Adet Düzenle") },
            text = {
                OutlinedTextField(
                    value = pendingQuantity,
                    onValueChange = { pendingQuantity = it.filter(Char::isDigit) },
                    label = { Text("Yeni adet") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            },
            dismissButton = {
                TextButton(onClick = { quantityDialogItem = null }) { Text("Vazgeç") }
            },
            confirmButton = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = {
                                val current = pendingQuantity.toIntOrNull() ?: item.quantity
                                pendingQuantity = (current + 5).toString()
                            }
                        ) {
                            Text("+5")
                        }
                        OutlinedButton(
                            onClick = {
                                val current = pendingQuantity.toIntOrNull() ?: item.quantity
                                pendingQuantity = (current + 10).toString()
                            }
                        ) {
                            Text("+10")
                        }
                    }
                    Button(onClick = {
                        quantityDialogItem = null
                        viewModel.setQuantity(item.barcode, pendingQuantity)
                    }) {
                        Text("Kaydet")
                    }
                }
            }
        )
    }

    if (priceDialogItem != null) {
        val item = priceDialogItem!!
        AlertDialog(
            onDismissRequest = { priceDialogItem = null },
            title = { Text("Özel Fiyat") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Liste fiyatı: ${MoneyUtils.formatKurus(item.baseSalePriceKurus)}")
                    OutlinedTextField(
                        value = pendingCustomPrice,
                        onValueChange = { pendingCustomPrice = MoneyUtils.sanitizeTlTypingInput(it) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        label = { Text("Yeni satış fiyatı (TL)") }
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { priceDialogItem = null }) { Text("Vazgeç") }
            },
            confirmButton = {
                Button(onClick = {
                    priceDialogItem = null
                    if (uiState.mode == AppMode.ADMIN) {
                        viewModel.applyCustomPrice(item.barcode, pendingCustomPrice, null)
                    } else {
                        askPinForPriceBarcode = item.barcode
                    }
                }) {
                    Text("Uygula")
                }
            }
        )
    }

    if (askPinForPriceBarcode != null) {
        val barcode = askPinForPriceBarcode!!
        PinDialog(
            title = "Fiyat Degisimi PIN",
            onDismiss = { askPinForPriceBarcode = null },
            onConfirm = { pin ->
                askPinForPriceBarcode = null
                viewModel.applyCustomPrice(barcode, pendingCustomPrice, pin)
            }
        )
    }

    if (percentDiscountItem != null) {
        val item = percentDiscountItem!!
        AlertDialog(
            onDismissRequest = { percentDiscountItem = null },
            title = { Text("Yüzde İndirim") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Mevcut fiyat: ${MoneyUtils.formatKurus(item.salePriceKurus)}")
                    OutlinedTextField(
                        value = pendingPercentDiscount,
                        onValueChange = {
                            pendingPercentDiscount = it.filter { char ->
                                char.isDigit() || char == ',' || char == '.'
                            }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        label = { Text("İndirim yüzdesi") }
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { percentDiscountItem = null }) { Text("Vazgeç") }
            },
            confirmButton = {
                Button(onClick = {
                    percentDiscountItem = null
                    if (uiState.mode == AppMode.ADMIN) {
                        viewModel.applyPercentDiscount(item.barcode, pendingPercentDiscount, null)
                    } else {
                        askPinForPercentBarcode = item.barcode
                    }
                }) { Text("Uygula") }
            }
        )
    }

    if (askPinForPercentBarcode != null) {
        val barcode = askPinForPercentBarcode!!
        PinDialog(
            title = "İndirim PIN",
            onDismiss = { askPinForPercentBarcode = null },
            onConfirm = { pin ->
                askPinForPercentBarcode = null
                viewModel.applyPercentDiscount(barcode, pendingPercentDiscount, pin)
            }
        )
    }

    if (fixedDiscountItem != null) {
        val item = fixedDiscountItem!!
        AlertDialog(
            onDismissRequest = { fixedDiscountItem = null },
            title = { Text("TL İndirim") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Mevcut fiyat: ${MoneyUtils.formatKurus(item.salePriceKurus)}")
                    OutlinedTextField(
                        value = pendingFixedDiscount,
                        onValueChange = { pendingFixedDiscount = MoneyUtils.sanitizeTlTypingInput(it) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        label = { Text("İndirim tutarı (TL)") }
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { fixedDiscountItem = null }) { Text("Vazgeç") }
            },
            confirmButton = {
                Button(onClick = {
                    fixedDiscountItem = null
                    if (uiState.mode == AppMode.ADMIN) {
                        viewModel.applyFixedDiscount(item.barcode, pendingFixedDiscount, null)
                    } else {
                        askPinForFixedBarcode = item.barcode
                    }
                }) { Text("Uygula") }
            }
        )
    }

    if (askPinForFixedBarcode != null) {
        val barcode = askPinForFixedBarcode!!
        PinDialog(
            title = "İndirim PIN",
            onDismiss = { askPinForFixedBarcode = null },
            onConfirm = { pin ->
                askPinForFixedBarcode = null
                viewModel.applyFixedDiscount(barcode, pendingFixedDiscount, pin)
            }
        )
    }

    if (showParkDialog) {
        AlertDialog(
            onDismissRequest = { showParkDialog = false },
            title = { Text("Sepeti Beklet") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("İsterseniz bekleyen sepet için kısa bir ad yazın. Boş bırakırsanız sistem otomatik ad verir.")
                    OutlinedTextField(
                        value = pendingParkLabel,
                        onValueChange = { pendingParkLabel = it.take(40) },
                        label = { Text("Sepet adı (opsiyonel)") }
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showParkDialog = false }) { Text("Vazgeç") }
            },
            confirmButton = {
                Button(onClick = {
                    showParkDialog = false
                    viewModel.parkCurrentCart(pendingParkLabel)
                }) {
                    Text("Beklet")
                }
            }
        )
    }

    if (showHeldCartsDialog) {
        AlertDialog(
            onDismissRequest = { showHeldCartsDialog = false },
            title = { Text("Bekleyen Sepetler") },
            text = {
                HeldCartList(
                    carts = uiState.heldCarts,
                    onRestore = { cartId ->
                        showHeldCartsDialog = false
                        viewModel.restoreHeldCart(cartId)
                    },
                    onDelete = { cartId ->
                        viewModel.deleteHeldCart(cartId)
                    }
                )
            },
            dismissButton = {
                TextButton(onClick = { showHeldCartsDialog = false }) { Text("Kapat") }
            },
            confirmButton = {}
        )
    }
}

@Composable
private fun HeldCartList(
    carts: List<HeldCart>,
    onRestore: (String) -> Unit,
    onDelete: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 420.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (carts.isEmpty()) {
            Text("Bekleyen sepet yok.")
            return
        }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(carts, key = { it.cartId }) { cart ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(cart.label, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                                Text(DateUtils.formatDateTime(cart.createdAt), style = MaterialTheme.typography.bodySmall)
                                Text(
                                    "${cart.totalItemCount} ürün / ${MoneyUtils.formatKurus(cart.totalAmountKurus)}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                        Text(
                            cart.items.joinToString(limit = 3, truncated = "...") { "${it.name} x${it.quantity}" },
                            style = MaterialTheme.typography.bodySmall
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(onClick = { onRestore(cart.cartId) }, modifier = Modifier.weight(1f)) {
                                Text("Geri Getir")
                            }
                            OutlinedButton(onClick = { onDelete(cart.cartId) }, modifier = Modifier.weight(1f)) {
                                Text("Sil")
                            }
                        }
                    }
                }
            }
        }
    }
}





