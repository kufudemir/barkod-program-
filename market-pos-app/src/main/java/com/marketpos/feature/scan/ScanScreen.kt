package com.marketpos.feature.scan

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.marketpos.core.util.ScanFeedback
import com.marketpos.domain.model.AppMode
import com.marketpos.domain.model.PremiumFeature
import com.marketpos.ui.components.BarcodeCameraView

@Composable
fun ScanScreen(
    viewModel: ScanViewModel,
    onNavigateDetail: (String) -> Unit,
    onNavigateNotFound: (String) -> Unit,
    onNavigateSerialScan: () -> Unit,
    onNavigateStockTracking: () -> Unit,
    onNavigatePremium: (PremiumFeature) -> Unit,
    onNavigateCart: () -> Unit,
    onNavigateSettings: () -> Unit
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val uiState by viewModel.uiState.collectAsState()
    val snackbar = remember { SnackbarHostState() }
    var flashOn by remember { mutableStateOf(false) }
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
                ScanEvent.PlayFeedback -> ScanFeedback.play(context)
                is ScanEvent.NavigateDetail -> onNavigateDetail(event.barcode)
                is ScanEvent.NavigateNotFound -> onNavigateNotFound(event.barcode)
            }
        }
    }

    Scaffold(snackbarHost = { com.marketpos.ui.components.CenteredSnackbarHost(snackbar) }) { innerPadding ->
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

        val cameraHeight = (configuration.screenHeightDp.dp * 0.36f).coerceIn(240.dp, 340.dp)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(innerPadding)
                .padding(12.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text("Barkod Tara", style = MaterialTheme.typography.titleLarge)
                    Text("Mod: ${if (uiState.mode == AppMode.ADMIN) "ADMIN" else "KASIYER"}")
                    Text(
                        "Tarama kutusu: ${uiState.scanBoxSize.label}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Card(
                    modifier = Modifier.clickable(onClick = onNavigateCart),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text("Sepet", style = MaterialTheme.typography.labelLarge)
                        Text(
                            "${uiState.cartItemCount} ürün",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(uiState.cartTotalLabel, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

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
                    if (uiState.cameraEnabled) {
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

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        if (uiState.isPro) onNavigateStockTracking()
                        else onNavigatePremium(PremiumFeature.REPORTS)
                    },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text("Kritik Stok", style = MaterialTheme.typography.labelLarge)
                        Text(
                            "${uiState.criticalStockCount} ürün",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Text(
                        if (uiState.isPro) "Tıkla ve Aç" else "Takip [PRO]",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        if (uiState.isPro) onNavigateSerialScan()
                        else onNavigatePremium(PremiumFeature.SERIAL_SCAN)
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(if (uiState.isPro) "Seri Ürün Tarama" else "Seri Tarama [PRO]")
                }
                OutlinedButton(onClick = onNavigateSettings, modifier = Modifier.weight(1f)) {
                    Text("Ayarlar")
                }
            }
        }
    }
}



