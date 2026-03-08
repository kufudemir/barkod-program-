package com.marketpos.feature.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
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
import androidx.compose.ui.unit.dp
import com.marketpos.core.util.MoneyUtils
import com.marketpos.domain.model.AppMode
import com.marketpos.ui.components.PinDialog

@Composable
fun BulkPriceUpdateScreen(
    viewModel: BulkPriceUpdateViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbar = remember { SnackbarHostState() }
    var askPin by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is BulkPriceUpdateEvent.ShowMessage -> snackbar.showSnackbar(event.message)
            }
        }
    }

    Scaffold(snackbarHost = { com.marketpos.ui.components.CenteredSnackbarHost(snackbar) }) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("Toplu Fiyat Güncelle", style = MaterialTheme.typography.headlineSmall)
            Text("Mod: ${if (uiState.mode == AppMode.ADMIN) "ADMIN" else "KASIYER"}")

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Tum ürünlere uygula")
                Switch(checked = uiState.applyToAll, onCheckedChange = viewModel::setApplyToAll)
            }

            OutlinedTextField(
                value = uiState.percentText,
                onValueChange = viewModel::updatePercent,
                label = { Text("Yüzde artış") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = uiState.query,
                onValueChange = viewModel::updateQuery,
                label = { Text("Ürün ara (secili modda)") },
                modifier = Modifier.fillMaxWidth()
            )

            if (!uiState.applyToAll) {
                Text("Secili ürünler:")
                LazyColumn(modifier = Modifier.weight(1f, fill = false)) {
                    items(uiState.products, key = { it.barcode }) { product ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.toggleSelection(product.barcode) }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = uiState.selectedBarcodes.contains(product.barcode),
                                onCheckedChange = { viewModel.toggleSelection(product.barcode) }
                            )
                            Column {
                                Text(product.name)
                                Text("${product.barcode} - ${MoneyUtils.formatKurus(product.salePriceKurus)}")
                            }
                        }
                    }
                }
            }

            Text("Önizleme:")
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(uiState.preview, key = { it.barcode }) { item ->
                    Text("${item.name}: ${MoneyUtils.formatKurus(item.oldPriceKurus)} -> ${MoneyUtils.formatKurus(item.newPriceKurus)}")
                }
            }

            Button(
                onClick = {
                    if (uiState.mode == AppMode.CASHIER) {
                        askPin = true
                    } else {
                        viewModel.apply(pinIfCashier = null)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Onayla ve Uygula")
            }
            Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
                Text("Geri")
            }
        }
    }

    if (askPin) {
        PinDialog(
            title = "Kasiyer Modu PIN",
            onDismiss = { askPin = false },
            onConfirm = { pin ->
                askPin = false
                viewModel.apply(pin)
            }
        )
    }
}



