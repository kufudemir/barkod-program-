package com.marketpos.feature.companion

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.AlertDialog
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.marketpos.core.util.MoneyUtils
import com.marketpos.domain.model.CompanionCartItem
import com.marketpos.ui.components.BarcodeCameraView

@Composable
fun WebCompanionScreen(
    viewModel: WebCompanionViewModel,
    onNavigateMobileScan: () -> Unit
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showCustomPriceDialog by remember { mutableStateOf(false) }
    var showPercentDialog by remember { mutableStateOf(false) }
    var showFixedDiscountDialog by remember { mutableStateOf(false) }

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted -> hasCameraPermission = granted }

    val selectedItem = uiState.cartItems.firstOrNull { it.barcode == uiState.selectedBarcode }
    val cameraHeight = (configuration.screenHeightDp.dp * 0.28f).coerceIn(200.dp, 300.dp)

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is WebCompanionEvent.ShowMessage -> snackbarHostState.showSnackbar(event.message)
                is WebCompanionEvent.OpenExternalUrl -> {
                    val viewIntent = Intent(Intent.ACTION_VIEW, Uri.parse(event.url))
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    val packageManager = context.packageManager

                    if (viewIntent.resolveActivity(packageManager) != null) {
                        context.startActivity(viewIntent)
                    } else {
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, event.url)
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        context.startActivity(
                            Intent.createChooser(shareIntent, "Yazdirma baglantisini paylas")
                        )
                    }
                }
                WebCompanionEvent.NavigateMobileScan -> onNavigateMobileScan()
            }
        }
    }

    Scaffold(snackbarHost = { com.marketpos.ui.components.CenteredSnackbarHost(snackbarHostState) }) { innerPadding ->
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

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(12.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("Web Uzerinden Satis (Companion)")
            Text(uiState.statusMessage ?: "Telefon barkod okuyucu ve yardimci POS olarak calisir.")

            if (!uiState.hasActiveSession) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CardDefaults.cardColors().containerColor)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("Web uzerinde oturumunuz bulunmuyor.")
                        Text("Web POS ekraninda aktif bir kasa oturumu acin, sonra yeniden deneyin.")
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = viewModel::refreshActiveSession,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Yeniden Dene")
                            }
                            OutlinedButton(
                                onClick = viewModel::switchToMobilePos,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Mobil POS'a Gec")
                            }
                        }
                    }
                }
                return@Column
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text("Firma: ${uiState.companyName}")
                    Text("Sube/Kasa: ${uiState.branchName} / ${uiState.registerName}")
                    Text("Aktif sekme: ${uiState.sessionLabel}")
                }
            }

            if (!hasCameraPermission) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("Kamera izni gerekli")
                        Button(onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }) {
                            Text("Kamera Iznini Ver")
                        }
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = viewModel::toggleCamera,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(if (uiState.cameraEnabled) "Kamerayi Kapat" else "Kamerayi Ac")
                    }
                    OutlinedButton(
                        onClick = viewModel::toggleFlash,
                        enabled = uiState.cameraEnabled,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(if (uiState.flashEnabled) "Flaşi Kapat" else "Flaşi Ac")
                    }
                }

                if (uiState.cameraEnabled) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(cameraHeight),
                        shape = RoundedCornerShape(18.dp)
                    ) {
                        BarcodeCameraView(
                            modifier = Modifier.fillMaxSize(),
                            flashEnabled = uiState.flashEnabled,
                            scanBoxSize = uiState.scanBoxSize,
                            onBarcodeDetected = viewModel::onBarcodeScanned
                        )
                    }
                }
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Sepet Ozeti")
                    Text("Sepetteki urun adedi: ${uiState.itemCount}")
                    Text("Toplam: ${uiState.totalAmountLabel}")
                    Button(
                        onClick = viewModel::completeSale,
                        enabled = uiState.canCheckout && !uiState.isProcessing,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (uiState.isProcessing) "Isleniyor..." else "Satisi Tamamla")
                    }
                    OutlinedButton(
                        onClick = viewModel::triggerPrint,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Yazdir / PDF")
                    }
                }
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Sepet Urunleri")

                    if (uiState.cartItems.isEmpty()) {
                        Text("Sepet bos. Barkod okutunca urun burada gorunur.")
                    } else {
                        uiState.cartItems.forEach { item ->
                            CompanionCartItemRow(
                                item = item,
                                selected = uiState.selectedBarcode == item.barcode,
                                onSelect = { viewModel.selectBarcode(item.barcode) },
                                onIncrement = { viewModel.increment(item.barcode) },
                                onDecrement = { viewModel.decrement(item.barcode) },
                                onRemove = { viewModel.remove(item.barcode) }
                            )
                        }
                    }
                }
            }

            if (selectedItem != null) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("Secili satir: ${selectedItem.productName}")
                        Text("Birim fiyat: ${MoneyUtils.formatKurus(selectedItem.salePriceKurus)}")
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { showCustomPriceDialog = true },
                                modifier = Modifier.weight(1f)
                            ) { Text("Ozel Fiyat") }
                            OutlinedButton(
                                onClick = { showPercentDialog = true },
                                modifier = Modifier.weight(1f)
                            ) { Text("% Indirim") }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { showFixedDiscountDialog = true },
                                modifier = Modifier.weight(1f)
                            ) { Text("TL Indirim") }
                            OutlinedButton(
                                onClick = { viewModel.resetPrice(selectedItem.barcode) },
                                modifier = Modifier.weight(1f)
                            ) { Text("Fiyati Sifirla") }
                        }
                    }
                }
            }

            OutlinedButton(onClick = viewModel::switchToMobilePos, modifier = Modifier.fillMaxWidth()) {
                Text("Mobil POS'a Gec")
            }
        }
    }

    if (showCustomPriceDialog && selectedItem != null) {
        PriceInputDialog(
            title = "Ozel Satis Fiyati",
            hint = "Ornek: 100 veya 99,90",
            onDismiss = { showCustomPriceDialog = false },
            onConfirm = { input ->
                showCustomPriceDialog = false
                val priceKurus = MoneyUtils.parseTlInputToKurus(input)
                if (priceKurus != null && priceKurus > 0L) {
                    viewModel.setCustomPrice(selectedItem.barcode, priceKurus)
                }
            }
        )
    }

    if (showPercentDialog && selectedItem != null) {
        PriceInputDialog(
            title = "Yuzde Indirim",
            hint = "Ornek: 10",
            onDismiss = { showPercentDialog = false },
            onConfirm = { input ->
                showPercentDialog = false
                input.replace(',', '.').toDoubleOrNull()?.let { percent ->
                    if (percent > 0.0) {
                        viewModel.applyPercentDiscount(selectedItem.barcode, percent)
                    }
                }
            }
        )
    }

    if (showFixedDiscountDialog && selectedItem != null) {
        PriceInputDialog(
            title = "Sabit TL Indirim",
            hint = "Ornek: 5 veya 2,50",
            onDismiss = { showFixedDiscountDialog = false },
            onConfirm = { input ->
                showFixedDiscountDialog = false
                val discountKurus = MoneyUtils.parseTlInputToKurus(input)
                if (discountKurus != null && discountKurus > 0L) {
                    viewModel.applyFixedDiscount(selectedItem.barcode, discountKurus)
                }
            }
        )
    }
}

@Composable
private fun CompanionCartItemRow(
    item: CompanionCartItem,
    selected: Boolean,
    onSelect: () -> Unit,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    onRemove: () -> Unit
) {
    val container = if (selected) {
        CardDefaults.cardColors(containerColor = CardDefaults.cardColors().containerColor)
    } else {
        CardDefaults.cardColors()
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = container,
        onClick = onSelect
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(item.productName)
            Text("Barkod: ${item.barcode}")
            Text("Adet: ${item.quantity} / Tutar: ${MoneyUtils.formatKurus(item.lineTotalKurus)}")
            if (item.hasCustomPrice) {
                Text("Ozel fiyat uygulandi")
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(onClick = onDecrement, modifier = Modifier.weight(1f)) { Text("-1") }
                OutlinedButton(onClick = onIncrement, modifier = Modifier.weight(1f)) { Text("+1") }
                OutlinedButton(onClick = onRemove, modifier = Modifier.weight(1f)) { Text("Sil") }
            }
        }
    }
}

@Composable
private fun PriceInputDialog(
    title: String,
    hint: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var value by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            OutlinedTextField(
                value = value,
                onValueChange = { value = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(hint) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Vazgec") }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(value) },
                enabled = value.isNotBlank()
            ) {
                Text("Uygula")
            }
        }
    )
}
