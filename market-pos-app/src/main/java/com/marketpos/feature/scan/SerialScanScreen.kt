package com.marketpos.feature.scan

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.marketpos.core.util.ScanFeedback
import com.marketpos.domain.model.AppMode
import com.marketpos.domain.model.PremiumFeature
import com.marketpos.ui.components.BarcodeCameraView

@Composable
fun SerialScanScreen(
    viewModel: SerialScanViewModel,
    onNavigateCart: () -> Unit,
    onNavigatePremium: (PremiumFeature) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val uiState by viewModel.uiState.collectAsState()
    val snackbar = remember { SnackbarHostState() }
    var flashOn by remember { mutableStateOf(false) }
    var pendingQuantity by remember { mutableStateOf("") }
    var showQuantityDialog by remember { mutableStateOf(false) }
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted -> hasCameraPermission = granted }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                SerialScanEvent.PlayFeedback -> ScanFeedback.play(context)
                is SerialScanEvent.ShowMessage -> snackbar.showSnackbar(event.message)
            }
        }
    }

    Scaffold(
        snackbarHost = { com.marketpos.ui.components.CenteredSnackbarHost(snackbar) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        if (!hasCameraPermission) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Kamera izni gerekli")
                    Button(onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }) {
                        Text("İzin Ver")
                    }
                }
            }
            return@Scaffold
        }

        val cameraHeight = (configuration.screenHeightDp.dp * 0.33f).coerceIn(220.dp, 300.dp)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(innerPadding)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (!uiState.isPro) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("Seri Ürün Tarama PRO sürümde", style = MaterialTheme.typography.titleMedium)
                        Button(
                            onClick = { onNavigatePremium(PremiumFeature.SERIAL_SCAN) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Premium Ekranını Aç")
                        }
                    }
                }
            }

            Text("Seri ürün tarama - Mod:${if (uiState.mode == AppMode.ADMIN) "Admin" else "Kasiyer"}")
            Text("Bekleme: ${uiState.cooldownOption.label} | Tarama kutusu: ${uiState.scanBoxSize.label}")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(onClick = viewModel::toggleCamera, modifier = Modifier.weight(1f)) {
                    Text(if (uiState.cameraEnabled) "Kamerayı Kapat" else "Kamerayı Aç")
                }
                OutlinedButton(
                    onClick = { flashOn = !flashOn },
                    enabled = uiState.cameraEnabled,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(if (flashOn) "Flaş Kapat" else "Flaş Aç")
                }
            }
            Text(
                "Kamerayı barkoda tutun. Her tarama arasında ${uiState.cooldownOption.label} bekleme uygulanır.",
                style = MaterialTheme.typography.bodySmall
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(cameraHeight),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    if (uiState.cameraEnabled && uiState.isPro) {
                        BarcodeCameraView(
                            modifier = Modifier.fillMaxSize(),
                            flashEnabled = flashOn,
                            scanBoxSize = uiState.scanBoxSize,
                            onBarcodeDetected = viewModel::onBarcodeScanned
                        )
                    } else {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Kamera kapalı", style = MaterialTheme.typography.titleMedium)
                        }
                    }
                }
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Sepet Özeti", style = MaterialTheme.typography.titleMedium)
                                Text("Sepetteki ürün adedi: ${uiState.cartItemCount}", style = MaterialTheme.typography.titleMedium)
                            }
                            Text(
                                "Toplam Sepet Tutarı: ${uiState.cartTotalLabel}",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFD32F2F)
                            )
                            Text(uiState.statusMessage, style = MaterialTheme.typography.bodySmall)
                            if (uiState.cartPreview.isNotEmpty()) {
                                uiState.cartPreview.forEach { item ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            "${item.quantity} x ${item.name} - ${item.lineTotalLabel}",
                                            modifier = Modifier.weight(1f),
                                            maxLines = 1
                                        )
                                        OutlinedButton(onClick = { viewModel.removePreviewItem(item.barcode) }) {
                                            Text("Çıkar")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text("Son Taranan Ürün", style = MaterialTheme.typography.titleMedium)
                            val lastScan = uiState.lastScan
                            if (lastScan == null) {
                                Text("Henüz ürün taranmadı")
                            } else {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text(lastScan.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                                        Text("Barkod: ${lastScan.barcode}")
                                        Text("Sepetteki adet: ${lastScan.cartQuantity}")
                                        Text("Stok: ${lastScan.stockQty}")
                                    }
                                    Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text("Satış Fiyatı", style = MaterialTheme.typography.titleSmall)
                                        Text(lastScan.salePriceLabel, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                                    }
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.weight(1f),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            OutlinedButton(onClick = viewModel::decreaseLastScan, modifier = Modifier.weight(1f)) { Text("-1") }
                                            OutlinedButton(onClick = viewModel::increaseLastScan, modifier = Modifier.weight(1f)) { Text("+1") }
                                        }
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            OutlinedButton(onClick = { viewModel.increaseLastScan(5) }, modifier = Modifier.weight(1f)) { Text("+5") }
                                            OutlinedButton(onClick = { viewModel.increaseLastScan(10) }, modifier = Modifier.weight(1f)) { Text("+10") }
                                        }
                                    }
                                    Column(
                                        modifier = Modifier.weight(1f),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        OutlinedButton(
                                            onClick = {
                                                pendingQuantity = lastScan.cartQuantity.toString()
                                                showQuantityDialog = true
                                            },
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text("Adet Gir")
                                        }
                                        OutlinedButton(onClick = viewModel::removeLastScan, modifier = Modifier.fillMaxWidth()) {
                                            Text("Sepetten Çıkar")
                                        }
                                    }
                                }
                                if (lastScan.stockQty <= lastScan.minStockQty) {
                                    Text(
                                        "Kritik stok seviyesinde",
                                        color = MaterialTheme.colorScheme.error,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(onClick = onBack, modifier = Modifier.weight(1f)) {
                            Text("Normal Tarama")
                        }
                        Button(onClick = onNavigateCart, modifier = Modifier.weight(1f)) {
                            Text("Sepete Git")
                        }
                    }
                }
            }
        }
    }

    if (showQuantityDialog) {
        AlertDialog(
            onDismissRequest = { showQuantityDialog = false },
            title = { Text("Adet Gir") },
            text = {
                OutlinedTextField(
                    value = pendingQuantity,
                    onValueChange = { pendingQuantity = it.filter(Char::isDigit) },
                    label = { Text("Yeni adet") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
            },
            dismissButton = {
                TextButton(onClick = { showQuantityDialog = false }) {
                    Text("Vazgeç")
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showQuantityDialog = false
                        viewModel.setLastScanQuantity(pendingQuantity)
                    }
                ) {
                    Text("Kaydet")
                }
            }
        )
    }
}



