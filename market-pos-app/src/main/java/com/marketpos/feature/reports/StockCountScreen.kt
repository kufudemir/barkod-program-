package com.marketpos.feature.reports

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.marketpos.core.util.ScanFeedback
import com.marketpos.domain.model.PremiumFeature
import com.marketpos.domain.model.StockCountItem
import com.marketpos.ui.components.BarcodeCameraView

@Composable
fun StockCountScreen(
    viewModel: StockCountViewModel,
    onNavigatePremium: (PremiumFeature) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val uiState by viewModel.uiState.collectAsState()
    val snackbar = remember { SnackbarHostState() }
    var flashOn by remember { mutableStateOf(false) }
    var editItem by remember { mutableStateOf<StockCountItem?>(null) }
    var pendingCount by remember { mutableStateOf("") }
    var showApplyConfirm by remember { mutableStateOf(false) }
    var applyAffectedCount by remember { mutableIntStateOf(0) }
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
                is StockCountEvent.ShowMessage -> snackbar.showSnackbar(event.message)
                StockCountEvent.PlayFeedback -> ScanFeedback.play(context)
                StockCountEvent.RequirePremium -> onNavigatePremium(PremiumFeature.STOCK_TRACKING)
                is StockCountEvent.RequestApplyConfirmation -> {
                    applyAffectedCount = event.affectedCount
                    showApplyConfirm = true
                }
            }
        }
    }

    Scaffold(snackbarHost = { com.marketpos.ui.components.CenteredSnackbarHost(snackbar) }) { innerPadding ->
        val cameraHeight = (configuration.screenHeightDp.dp * 0.32f).coerceIn(220.dp, 300.dp)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Stok Sayım Modu", style = MaterialTheme.typography.headlineSmall)

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
                        Text("Stok sayım modu PRO sürümde", style = MaterialTheme.typography.titleMedium)
                        Button(
                            onClick = { onNavigatePremium(PremiumFeature.STOCK_TRACKING) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Premium Ekranını Aç")
                        }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(cameraHeight)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                if (!hasCameraPermission) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text("Kamera izni gerekli")
                            Button(onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }) {
                                Text("İzin Ver")
                            }
                        }
                    }
                } else if (uiState.cameraEnabled && uiState.isPro) {
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

                Column(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.84f))
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Sayım Kamerası", style = MaterialTheme.typography.titleLarge)
                    Text("Tarama kutusu: ${uiState.scanBoxSize.label} | Bekleme: ${uiState.cooldownOption.label}")
                    Text(
                        "Kamerayı barkoda tutun. Tarama alanı sabit kalır, son ürün bilgisi altta görünür.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = viewModel::toggleCamera) {
                            Text(if (uiState.cameraEnabled) "Kamerayı Kapat" else "Kamerayı Aç")
                        }
                        OutlinedButton(onClick = { flashOn = !flashOn }, enabled = uiState.cameraEnabled) {
                            Text(if (flashOn) "Flaşı Kapat" else "Flaşı Aç")
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
                            Text("Sayım Özeti", style = MaterialTheme.typography.titleMedium)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Ürün: ${uiState.session.uniqueProductCount}")
                                Text("Adet: ${uiState.session.totalCountedUnits}")
                                Text("Farklı: ${uiState.session.differenceProductCount}")
                            }
                            Text(uiState.statusMessage, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text("Son Taranan Ürün", style = MaterialTheme.typography.titleMedium)
                            uiState.lastScannedItem?.let { last ->
                                Text(last.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                Text("Barkod: ${last.barcode}")
                                Text("Beklenen: ${last.expectedQty}  |  Sayılan: ${last.countedQty}  |  Fark: ${formatDifference(last.differenceQty)}")
                            } ?: Text("Henüz ürün taranmadı")
                        }
                    }
                }

                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("Sayım Fark Raporu", style = MaterialTheme.typography.titleMedium)
                            Text(
                                "Bu rapor sadece sayılan barkodları içerir. Sayılmayan ürünler fark raporuna dahil edilmez.",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                StockCountFilter.entries.forEach { filter ->
                                    OutlinedButton(
                                        onClick = { viewModel.selectFilter(filter) },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(if (uiState.selectedFilter == filter) "${filter.label} *" else filter.label)
                                    }
                                }
                            }
                        }
                    }
                }

                if (uiState.visibleItems.isEmpty()) {
                    item {
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                "Gösterilecek sayım satırı yok.",
                                modifier = Modifier.padding(14.dp)
                            )
                        }
                    }
                } else {
                    items(uiState.visibleItems, key = { it.barcode }) { item ->
                        StockCountItemCard(
                            item = item,
                            onEdit = {
                                editItem = item
                                pendingCount = item.countedQty.toString()
                            },
                            onRemove = { viewModel.removeItem(item.barcode) }
                        )
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(onClick = viewModel::clearSession, modifier = Modifier.weight(1f)) {
                            Text("Sayımı Temizle")
                        }
                        Button(onClick = viewModel::requestApplyCountResult, modifier = Modifier.weight(1f)) {
                            Text("Farkları Uygula")
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
    }

    if (editItem != null) {
        val item = editItem!!
        AlertDialog(
            onDismissRequest = { editItem = null },
            title = { Text("Sayım Adedi Düzenle") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(item.name)
                    OutlinedTextField(
                        value = pendingCount,
                        onValueChange = { pendingCount = it.filter(Char::isDigit) },
                        label = { Text("Sayılan adet") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { editItem = null }) { Text("Vazgeç") }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateItemCount(item.barcode, pendingCount)
                        editItem = null
                    }
                ) {
                    Text("Kaydet")
                }
            }
        )
    }

    if (showApplyConfirm) {
        AlertDialog(
            onDismissRequest = { showApplyConfirm = false },
            title = { Text("Sayım Farklarını Uygula") },
            text = {
                Text("$applyAffectedCount ürün için sayılan adet stok miktarı olarak kaydedilecek. Bu işlem geri alınmaz.")
            },
            dismissButton = {
                TextButton(onClick = { showApplyConfirm = false }) { Text("Vazgeç") }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showApplyConfirm = false
                        viewModel.applyCountResult()
                    }
                ) {
                    Text("Uygula")
                }
            }
        )
    }
}

@Composable
private fun StockCountItemCard(
    item: StockCountItem,
    onEdit: () -> Unit,
    onRemove: () -> Unit
) {
    val differenceColor = when {
        item.differenceQty > 0 -> Color(0xFF1B8A3C)
        item.differenceQty < 0 -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(item.name, style = MaterialTheme.typography.titleMedium)
            Text("Barkod: ${item.barcode}")
            Text("Beklenen: ${item.expectedQty}")
            Text("Sayilan: ${item.countedQty}")
            Text("Fark: ${formatDifference(item.differenceQty)}", color = differenceColor, fontWeight = FontWeight.Bold)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(onClick = onEdit, modifier = Modifier.weight(1f)) {
                    Text("Adet Duzenle")
                }
                OutlinedButton(onClick = onRemove, modifier = Modifier.weight(1f)) {
                    Text("Kaldir")
                }
            }
        }
    }
}

private fun formatDifference(value: Int): String {
    return when {
        value > 0 -> "+$value"
        else -> value.toString()
    }
}



