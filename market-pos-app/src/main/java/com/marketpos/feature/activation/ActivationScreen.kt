package com.marketpos.feature.activation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ActivationScreen(
    viewModel: ActivationViewModel = hiltViewModel(),
    onActivated: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                ActivationEvent.NavigateScan -> onActivated()
                is ActivationEvent.ShowMessage -> snackbarHostState.showSnackbar(event.message)
                is ActivationEvent.ShowExistingCompanyPrompt -> snackbarHostState.showSnackbar(
                    "Bu cihaz daha önce ${event.companyName} ile aktive edilmiş."
                )
                is ActivationEvent.ShowRestorePrompt -> snackbarHostState.showSnackbar(
                    "${event.companyName} firmasında ${event.productCount} ürün bulundu."
                )
            }
        }
    }

    Scaffold(snackbarHost = { com.marketpos.ui.components.CenteredSnackbarHost(snackbarHostState) }) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Firma Aktivasyonu", style = MaterialTheme.typography.headlineSmall)
            Text("Uygulamayı kullanmaya başlamadan önce firma ünvanını girin. Sistem web tarafında firmayı otomatik oluşturur ve firma kodunu kendisi üretir.")

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHighest)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Cihaz: ${uiState.deviceName}")
                    Text("Cihaz Kimliği: ${uiState.deviceUid}", style = MaterialTheme.typography.bodySmall)
                }
            }

            if (uiState.ownedCompanies.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("Hesap veya Bu Cihazdaki Firmalar", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "Daha önce bu hesapta veya bu cihazda kullanılan firmalardan biriyle devam edip bulut kataloğunu geri yükleyebilirsiniz.",
                            style = MaterialTheme.typography.bodySmall
                        )
                        uiState.ownedCompanies.forEach { company ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(company.companyName, style = MaterialTheme.typography.titleSmall)
                                    Text("Firma Kodu: ${company.companyCode}", style = MaterialTheme.typography.bodySmall)
                                    Text("Bulut ürün sayısı: ${company.productCount}", style = MaterialTheme.typography.bodySmall)
                                    company.lastSyncedAt?.let {
                                        val formatted = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale("tr", "TR")).format(Date(it))
                                        Text("Son senkron: $formatted", style = MaterialTheme.typography.bodySmall)
                                    }
                                    Button(
                                        onClick = { viewModel.activateOwnedCompany(company) },
                                        modifier = Modifier.fillMaxWidth(),
                                        enabled = !uiState.isActivating
                                    ) {
                                        Text("Bu Firma ile Devam Et")
                                    }
                                }
                            }
                        }
                    }
                }
            }

            OutlinedTextField(
                value = uiState.companyName,
                onValueChange = viewModel::updateCompanyName,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Firma Ünvanı") },
                singleLine = true,
                isError = uiState.error != null,
                supportingText = {
                    val error = uiState.error
                    if (!error.isNullOrBlank()) {
                        Text(error)
                    }
                }
            )

            Button(
                onClick = viewModel::activate,
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isLoading && !uiState.isActivating
            ) {
                Text(if (uiState.isActivating) "Kayıt yapılıyor..." else "Firmayı Oluştur ve Cihazı Aktive Et")
            }
        }
    }

    val existingCompanyName = uiState.existingCompanyName
    if (!existingCompanyName.isNullOrBlank()) {
        AlertDialog(
            onDismissRequest = viewModel::dismissExistingCompanyPrompt,
            title = { Text("Firma Eşleşmesi") },
            text = {
                Text("Bu cihaz daha önce $existingCompanyName firması ile oturum açtı. Mevcut firma ile devam etmek ister misiniz?")
            },
            dismissButton = {
                TextButton(onClick = viewModel::createNewCompanyInstead) {
                    Text("Hayır, Yeni Firma Aç")
                }
            },
            confirmButton = {
                Button(onClick = viewModel::continueWithExistingCompany) {
                    Text("Evet, Devam Et")
                }
            }
        )
    }

    val restoreCompanyCode = uiState.pendingRestoreCompanyCode
    if (!restoreCompanyCode.isNullOrBlank()) {
        AlertDialog(
            onDismissRequest = viewModel::skipRestore,
            title = { Text("Bulut Katalog Bulundu") },
            text = {
                Text(
                    "${uiState.pendingRestoreCompanyName.orEmpty()} firması için bulutta ${uiState.pendingRestoreProductCount} ürün bulundu. Yerel kataloğu koruyarak ekleyebilir veya mevcut kataloğu bununla değiştirebilirsiniz."
                )
            },
            dismissButton = {
                TextButton(onClick = viewModel::skipRestore) {
                    Text("Şimdilik Geç")
                }
            },
            confirmButton = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { viewModel.restoreCatalog(replaceExisting = false) }) {
                        Text("Yerel Kataloğa Ekle")
                    }
                    TextButton(onClick = { viewModel.restoreCatalog(replaceExisting = true) }) {
                        Text("Yereli Sil ve Bulutu Yükle")
                    }
                }
            }
        )
    }
}




