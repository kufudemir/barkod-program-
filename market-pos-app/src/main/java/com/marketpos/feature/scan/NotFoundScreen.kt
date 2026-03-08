package com.marketpos.feature.scan

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.marketpos.domain.model.AppMode
import com.marketpos.ui.components.PinDialog

@Composable
fun NotFoundScreen(
    viewModel: NotFoundViewModel,
    onNavigateScan: () -> Unit,
    onNavigateAddProduct: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbar = remember { SnackbarHostState() }
    var showPinDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is NotFoundEvent.NavigateAddProduct -> onNavigateAddProduct(event.barcode)
                NotFoundEvent.NavigateScan -> onNavigateScan()
                is NotFoundEvent.ShowMessage -> snackbar.showSnackbar(event.message)
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
            Text("Ürün bulunamadı", style = MaterialTheme.typography.headlineSmall)
            Text("Barkod: ${uiState.barcode}")

            Button(onClick = viewModel::onRetry, modifier = Modifier.fillMaxWidth()) {
                Text("Yeniden Tara")
            }

            if (uiState.mode == AppMode.ADMIN) {
                Button(onClick = viewModel::onAddDirect, modifier = Modifier.fillMaxWidth()) {
                    Text("Ürün Ekle")
                }
            } else {
                Button(onClick = { showPinDialog = true }, modifier = Modifier.fillMaxWidth()) {
                    Text("PIN ile Ürün Ekle")
                }
            }

            OutlinedButton(onClick = onNavigateScan, modifier = Modifier.fillMaxWidth()) {
                Text("Geri Dön")
            }
        }
    }

    if (showPinDialog) {
        PinDialog(
            title = "Ürün Ekleme PIN",
            onDismiss = { showPinDialog = false },
            onConfirm = { pin ->
                showPinDialog = false
                viewModel.onAddWithPin(pin)
            }
        )
    }
}




