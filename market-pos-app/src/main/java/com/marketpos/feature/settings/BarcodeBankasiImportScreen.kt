package com.marketpos.feature.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun BarcodeBankasiImportScreen(
    viewModel: BarcodeBankasiImportViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbar = remember { SnackbarHostState() }
    var groupMenuExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is BarcodeBankasiImportEvent.ShowMessage -> snackbar.showSnackbar(event.message)
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
            Text("BarkodBankası İçeri Aktar", style = MaterialTheme.typography.headlineSmall)
            Text(
                "Once ürün grubunu secin. Arama metni opsiyoneldir. Önizleme listesinden istemediginiz ürünleri secim disi birakabilirsiniz.",
                style = MaterialTheme.typography.bodyMedium
            )

            Box(modifier = Modifier.fillMaxWidth()) {
                val selectedGroupLabel = uiState.groups
                    .firstOrNull { it.value == uiState.selectedGroup }
                    ?.label
                    ?: "Tum Gruplar"

                OutlinedButton(
                    onClick = { groupMenuExpanded = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text("Ürün Grubu", style = MaterialTheme.typography.labelMedium)
                        Text(selectedGroupLabel, style = MaterialTheme.typography.bodyLarge)
                        Text(
                            if (uiState.isGroupsLoading) "Gruplar yukleniyor..." else "Ilk filtre olarak grup secmeniz onerilir",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                DropdownMenu(
                    expanded = groupMenuExpanded,
                    onDismissRequest = { groupMenuExpanded = false }
                ) {
                    uiState.groups.forEach { group ->
                        DropdownMenuItem(
                            text = { Text(group.label) },
                            onClick = {
                                viewModel.updateSelectedGroup(group.value)
                                groupMenuExpanded = false
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = uiState.query,
                onValueChange = viewModel::updateQuery,
                label = { Text("Arama Metni (Opsiyonel)") },
                placeholder = { Text("Ornek: kola, 869, cips") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = uiState.startPageInput,
                    onValueChange = viewModel::updateStartPage,
                    label = { Text("Başlangıç") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                OutlinedTextField(
                    value = uiState.itemCountInput,
                    onValueChange = viewModel::updateItemCount,
                    label = { Text("Adet") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(onClick = { viewModel.selectItemCount(100) }, modifier = Modifier.weight(1f)) {
                    Text("100")
                }
                OutlinedButton(onClick = { viewModel.selectItemCount(500) }, modifier = Modifier.weight(1f)) {
                    Text("500")
                }
                OutlinedButton(onClick = { viewModel.selectItemCount(1000) }, modifier = Modifier.weight(1f)) {
                    Text("1000")
                }
            }

            uiState.error?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = viewModel::toggleHideExisting,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(if (uiState.hideExisting) "Tumunu Göster" else "Ekliyi Gizle")
                }
                Button(
                    onClick = viewModel::loadPreview,
                    modifier = Modifier.weight(1f),
                    enabled = !uiState.isPreviewLoading && !uiState.isImporting
                ) {
                    Text(if (uiState.isPreviewLoading) "Hazirlaniyor..." else "Önizleme Getir")
                }

                Button(
                    onClick = viewModel::importPreview,
                    modifier = Modifier.weight(1f),
                    enabled = uiState.canImport && !uiState.isPreviewLoading && !uiState.isImporting
                ) {
                    Text(if (uiState.isImporting) "Aktariliyor..." else "Secilenleri Aktar")
                }
            }

            uiState.previewSummary?.let { summary ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(summary, style = MaterialTheme.typography.bodyMedium)
                        if (uiState.existingCount > 0) {
                            Text(
                                "Listede zaten olan barkod: ${uiState.existingCount}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        Text("Seçili ürün: ${uiState.selectedCount}", fontWeight = FontWeight.SemiBold)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(onClick = viewModel::selectAll) { Text("Tumunu Sec") }
                            OutlinedButton(onClick = viewModel::clearSelection) { Text("Seçimi Temizle") }
                        }
                    }
                }
            }

            if (uiState.isPreviewLoading) {
                CircularProgressIndicator()
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.visiblePreviewRows, key = { it.barcode }) { item ->
                    val selected = uiState.selectedBarcodes.contains(item.barcode)
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.toggleSelection(item.barcode) }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Checkbox(
                                checked = selected,
                                onCheckedChange = { viewModel.toggleSelection(item.barcode) }
                            )
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    item.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text("Barkod: ${item.barcode}")
                                Text("Fiyat: ${item.salePriceLabel}")
                                if (item.isExisting) {
                                    Text(
                                        "Durum: Uygulamada zaten var",
                                        color = MaterialTheme.colorScheme.primary,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                                Text("Kaynak sayfa: ${item.sourcePage}")
                                item.lastChangedAt?.let { Text("Kaynak tarih: $it") }
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



