package com.marketpos.feature.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun WebBarcodeSearchScreen(
    viewModel: WebBarcodeSearchViewModel,
    scannedBarcode: String?,
    onConsumeScannedBarcode: () -> Unit,
    onBack: () -> Unit,
    onNavigateScanForBarcode: () -> Unit,
    onOpenProductAdd: (WebBarcodeSearchResultUi) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(scannedBarcode) {
        if (!scannedBarcode.isNullOrBlank()) {
            viewModel.onBarcodeScanned(scannedBarcode)
            onConsumeScannedBarcode()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is WebBarcodeSearchEvent.ShowMessage -> snackbar.showSnackbar(event.message)
            }
        }
    }

    Scaffold(snackbarHost = { com.marketpos.ui.components.CenteredSnackbarHost(snackbar) }) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Web'de Barkod Ara", style = MaterialTheme.typography.headlineSmall)
            Text(
                "Barkod numarasini girin. Sistem web kaynaklarindan ürün adi ve varsa fiyat adaylarini getirir. Uygun sonucu secip ürün formuna tasiyabilirsiniz.",
                style = MaterialTheme.typography.bodyMedium
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = uiState.barcodeInput,
                    onValueChange = viewModel::updateBarcodeInput,
                    modifier = Modifier.weight(1f),
                    label = { Text("Barkod Numarası") },
                    placeholder = { Text("Ornek: 8690504961482") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                OutlinedButton(
                    onClick = onNavigateScanForBarcode,
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text("Kameradan Oku")
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = viewModel::toggleHideExisting,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(if (uiState.hideExisting) "Tumunu Göster" else "Ekliyi Gizle")
                }
                Button(
                    onClick = viewModel::search,
                    modifier = Modifier.weight(1f),
                    enabled = !uiState.isLoading
                ) {
                    Text(if (uiState.isLoading) "Araniyor..." else "Web'de Ara")
                }
            }

            uiState.error?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }

            if (uiState.existingCount > 0) {
                Text(
                    "Listede zaten olan barkod: ${uiState.existingCount}",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            if (uiState.isLoading) {
                CircularProgressIndicator()
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.visibleResults, key = { "${it.sourceLabel}:${it.name}:${it.barcode}" }) { result ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                result.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text("Barkod: ${result.barcode}")
                            result.priceLabel?.let { Text("Fiyat: $it") } ?: Text("Fiyat: Bilinmiyor")
                            if (result.isExisting) {
                                Text(
                                    "Durum: Uygulamada zaten var",
                                    color = MaterialTheme.colorScheme.primary,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            Text("Kaynak: ${result.sourceLabel}")
                            result.sourceUrl?.takeIf { it.isNotBlank() }?.let { Text("Link: $it") }
                            Button(
                                onClick = { onOpenProductAdd(result) },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Bu Sonucla Ürün Ekle")
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
}



