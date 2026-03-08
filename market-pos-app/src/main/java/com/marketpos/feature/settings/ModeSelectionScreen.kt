package com.marketpos.feature.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.marketpos.domain.model.AppSaleMode

@Composable
fun ModeSelectionScreen(
    viewModel: ModeSelectionViewModel,
    onNavigateMobileScan: () -> Unit,
    onNavigateWebCompanion: () -> Unit,
    onNavigateLogin: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                ModeSelectionEvent.NavigateMobileScan -> onNavigateMobileScan()
                ModeSelectionEvent.NavigateWebCompanion -> onNavigateWebCompanion()
                ModeSelectionEvent.NavigateLogin -> onNavigateLogin()
                is ModeSelectionEvent.ShowMessage -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    Scaffold(snackbarHost = { com.marketpos.ui.components.CenteredSnackbarHost(snackbarHostState) }) { innerPadding ->
        if (uiState.isLoading) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) { CircularProgressIndicator() }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Satis Modu Secimi", style = MaterialTheme.typography.headlineMedium)
            Text(
                "Giris sonrasi kullanmak istediginiz satis modunu secin.",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                "Mevcut secim: ${
                    if (uiState.currentSaleMode == AppSaleMode.WEB_SALES) "Web uzerinden satis" else "Mobil uzerinden satis"
                }",
                style = MaterialTheme.typography.bodyMedium
            )

            Button(
                onClick = viewModel::selectWebSale,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Web Uzerinden Satis")
            }

            OutlinedButton(
                onClick = viewModel::selectMobileSale,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Mobil Uzerinden Satis")
            }

            if (!uiState.isRegisteredSession) {
                Text(
                    "Not: Web satis modu icin kayitli kullanici girisi gerekir.",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
