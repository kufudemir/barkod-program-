package com.marketpos.feature.product

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.marketpos.domain.model.PremiumFeature

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun ProductEditScreen(
    viewModel: ProductEditViewModel,
    scannedBarcode: String?,
    onConsumeScannedBarcode: () -> Unit,
    ocrSuggestedNames: List<String>?,
    onConsumeOcrSuggestedNames: () -> Unit,
    onBack: () -> Unit,
    onNavigateScanAfterSave: () -> Unit,
    onNavigateScanForProduct: () -> Unit,
    onOpenExistingProduct: (String) -> Unit,
    onNavigatePremium: (PremiumFeature) -> Unit,
    onNavigatePackageTextScan: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbar = remember { SnackbarHostState() }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val barcodeFocus = remember { FocusRequester() }
    val nameFocus = remember { FocusRequester() }
    val salePriceFocus = remember { FocusRequester() }
    val costPriceFocus = remember { FocusRequester() }
    val stockFocus = remember { FocusRequester() }
    val minStockFocus = remember { FocusRequester() }
    val noteFocus = remember { FocusRequester() }
    var showSavedDialog by remember { mutableStateOf(false) }
    var suggestionsExpanded by remember { mutableStateOf(false) }

    fun requestFocus(field: ProductEditField) {
        when (field) {
            ProductEditField.BARCODE -> barcodeFocus.requestFocus()
            ProductEditField.NAME -> nameFocus.requestFocus()
            ProductEditField.SALE_PRICE -> salePriceFocus.requestFocus()
            ProductEditField.COST_PRICE -> costPriceFocus.requestFocus()
            ProductEditField.STOCK_QTY -> stockFocus.requestFocus()
            ProductEditField.MIN_STOCK_QTY -> minStockFocus.requestFocus()
            ProductEditField.NOTE -> noteFocus.requestFocus()
        }
    }

    LaunchedEffect(scannedBarcode) {
        if (!scannedBarcode.isNullOrBlank()) {
            viewModel.onBarcodeScanned(scannedBarcode)
            onConsumeScannedBarcode()
        }
    }

    LaunchedEffect(ocrSuggestedNames) {
        if (!ocrSuggestedNames.isNullOrEmpty()) {
            viewModel.onOcrSuggestions(ocrSuggestedNames)
            onConsumeOcrSuggestedNames()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                ProductEditEvent.ShowSavedDialog -> {
                    keyboardController?.hide()
                    showSavedDialog = true
                }

                ProductEditEvent.NavigateScanForBarcode -> onNavigateScanForProduct()
                is ProductEditEvent.OpenExistingProduct -> onOpenExistingProduct(event.barcode)
                is ProductEditEvent.ShowMessage -> snackbar.showSnackbar(event.message)
                is ProductEditEvent.FocusField -> requestFocus(event.field)
            }
        }
    }

    Scaffold(snackbarHost = { com.marketpos.ui.components.CenteredSnackbarHost(snackbar) }) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                if (uiState.isEditMode) "Ürün Düzenle" else "Yeni Ürün Ekle",
                style = MaterialTheme.typography.headlineSmall
            )
            Text(
                "Barkodu girin veya okutun. İsim önerisi otomatik denenir, son alanda Done ile kaydedebilirsiniz.",
                style = MaterialTheme.typography.bodySmall
            )

            OutlinedTextField(
                value = uiState.barcode,
                onValueChange = viewModel::updateBarcode,
                label = { Text("Barkod *") },
                placeholder = { Text("Elle gir veya tara") },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(barcodeFocus),
                isError = uiState.errors.barcode != null,
                supportingText = {
                    uiState.errors.barcode?.let { Text(it) } ?: Text("Sadece sayı girişi kabul edilir")
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { nameFocus.requestFocus() }
                )
            )

            OutlinedButton(
                onClick = viewModel::requestBarcodeScan,
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isSaving
            ) {
                Text("Barkod Tara")
            }

            OutlinedTextField(
                value = uiState.name,
                onValueChange = viewModel::updateName,
                label = { Text("Ürün Adı *") },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(nameFocus),
                isError = uiState.errors.name != null,
                supportingText = {
                    uiState.errors.name?.let { Text(it) } ?: Text("Ürün adını kısa ve anlaşılır yazın")
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { salePriceFocus.requestFocus() }
                )
            )

            OutlinedTextField(
                value = uiState.groupName,
                onValueChange = viewModel::updateGroupName,
                label = { Text("Ürün Grubu") },
                placeholder = { Text("Örnek: İçecek, Tekel, Temizlik") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Next
                )
            )

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (uiState.isLoadingNameSuggestion) {
                    CircularProgressIndicator()
                } else {
                    OutlinedButton(
                        onClick = {
                            if (uiState.canUsePremiumNameTools) viewModel.tryNameSuggestion()
                            else onNavigatePremium(PremiumFeature.WEB_NAME_SUGGESTION)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !uiState.isSaving
                    ) {
                        Text(if (uiState.canUsePremiumNameTools) "İsim Öner (İnternet)" else "İsim Öner (İnternet) [PRO]")
                    }
                }
                OutlinedButton(
                    onClick = {
                        if (uiState.canUsePremiumNameTools) onNavigatePackageTextScan(uiState.barcode)
                        else onNavigatePremium(PremiumFeature.OCR_NAME_SCAN)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.isSaving
                ) {
                    Text(if (uiState.canUsePremiumNameTools) "Ambalajdan Oku" else "Ambalajdan Oku [PRO]")
                }
            }

            if (uiState.suggestions.isNotEmpty()) {
                ExposedDropdownMenuBox(
                    expanded = suggestionsExpanded,
                    onExpandedChange = { suggestionsExpanded = !suggestionsExpanded }
                ) {
                    OutlinedTextField(
                        value = "${uiState.suggestions.size} öneriden seç",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Ürün Önerileri") },
                        supportingText = {
                            Text(
                                uiState.selectedSuggestionSourceLabel?.let { "Otomatik seçilen: $it" } ?: "Kaynaklar birleştirildi"
                            )
                        },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = suggestionsExpanded) },
                        modifier = Modifier
                            .menuAnchor(
                                type = androidx.compose.material3.ExposedDropdownMenuAnchorType.PrimaryNotEditable,
                                enabled = true
                            )
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = suggestionsExpanded,
                        onDismissRequest = { suggestionsExpanded = false }
                    ) {
                        uiState.suggestions.forEach { suggestion ->
                            DropdownMenuItem(
                                text = {
                                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                        Text(
                                            text = suggestion.name,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = suggestion.sourceLabel,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                },
                                onClick = {
                                    suggestionsExpanded = false
                                    viewModel.applySuggestion(suggestion.name)
                                }
                            )
                        }
                    }
                }
                uiState.selectedSuggestionSourceLabel?.let { sourceLabel ->
                    Text(
                        text = "Seçili öneri: $sourceLabel",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            OutlinedTextField(
                value = uiState.salePriceInput,
                onValueChange = viewModel::updateSalePrice,
                label = { Text("Satış Fiyatı (TL) *") },
                placeholder = { Text("Örnek: 45,50") },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(salePriceFocus),
                isError = uiState.errors.salePrice != null,
                supportingText = {
                    uiState.errors.salePrice?.let { Text(it) } ?: Text("Ondalıklı giriş kabul edilir")
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { costPriceFocus.requestFocus() }
                )
            )

            OutlinedTextField(
                value = uiState.costPriceInput,
                onValueChange = viewModel::updateCostPrice,
                label = { Text("Alış Fiyatı (TL) *") },
                placeholder = { Text("Örnek: 39,90") },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(costPriceFocus),
                isError = uiState.errors.costPrice != null,
                supportingText = {
                    uiState.errors.costPrice?.let { Text(it) } ?: Text("0 değeri kabul edilir")
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { stockFocus.requestFocus() }
                )
            )

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = uiState.targetMarginInput,
                    onValueChange = viewModel::updateTargetMargin,
                    label = { Text("Hedef Kâr Marjı (%)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Next
                    )
                )
                Text(
                    text = uiState.currentMarginLabel?.let { "Mevcut Kâr Marjı: $it" }
                        ?: "Alış ve satış fiyatı girildiğinde kâr marjı otomatik hesaplanır",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    androidx.compose.foundation.layout.Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(onClick = { viewModel.applyTargetMarginPreset(20) }, modifier = Modifier.weight(1f)) {
                            Text("%20")
                        }
                        OutlinedButton(onClick = { viewModel.applyTargetMarginPreset(25) }, modifier = Modifier.weight(1f)) {
                            Text("%25")
                        }
                        OutlinedButton(onClick = { viewModel.applyTargetMarginPreset(30) }, modifier = Modifier.weight(1f)) {
                            Text("%30")
                        }
                    }
                    OutlinedButton(
                        onClick = viewModel::applyTargetMargin,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Hedef Kâra Göre Satış Fiyatı Hesapla")
                    }
                }
            }

            OutlinedTextField(
                value = uiState.stockQtyInput,
                onValueChange = viewModel::updateStockQty,
                label = { Text("Stok Miktarı *") },
                placeholder = { Text("Sadece tam sayı") },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(stockFocus),
                isError = uiState.errors.stockQty != null,
                supportingText = {
                    uiState.errors.stockQty?.let { Text(it) } ?: Text("Ürün adedini girin")
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { minStockFocus.requestFocus() }
                )
            )

            OutlinedTextField(
                value = uiState.minStockQtyInput,
                onValueChange = viewModel::updateMinStockQty,
                label = { Text("Minimum Stok *") },
                placeholder = { Text("Sadece tam sayı") },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(minStockFocus),
                isError = uiState.errors.minStockQty != null,
                supportingText = {
                    uiState.errors.minStockQty?.let { Text(it) } ?: Text("Kritik stok seviyesi")
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { noteFocus.requestFocus() }
                )
            )

            OutlinedButton(
                onClick = viewModel::suggestMinStock,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Min Stok Öner")
            }
            uiState.minStockSuggestionLabel?.let { suggestion ->
                Text(
                    text = suggestion,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            OutlinedTextField(
                value = uiState.note,
                onValueChange = viewModel::updateNote,
                label = { Text("Not (Opsiyonel)") },
                placeholder = { Text("Raf bilgisi veya kısa not") },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(noteFocus),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(
                    onDone = {
                        focusManager.clearFocus()
                        keyboardController?.hide()
                        viewModel.save()
                    }
                )
            )

            uiState.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }

            Button(
                onClick = {
                    focusManager.clearFocus()
                    keyboardController?.hide()
                    viewModel.save()
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isSaving
            ) {
                Text(if (uiState.isSaving) "Kaydediliyor..." else "Kaydet")
            }
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isSaving
            ) {
                Text("İptal")
            }
        }
    }

    if (showSavedDialog) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text("Bilgi") },
            text = { Text("Ürün kaydedildi") },
            confirmButton = {
                Button(onClick = {
                    showSavedDialog = false
                    onNavigateScanAfterSave()
                }) { Text("Tamam") }
            }
        )
    }

    uiState.duplicateProduct?.let { duplicate ->
        AlertDialog(
            onDismissRequest = viewModel::dismissDuplicateProduct,
            title = { Text("Ürün Zaten Kayıtlı") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(duplicate.name, style = MaterialTheme.typography.titleMedium)
                    Text("Barkod: ${duplicate.barcode}")
                    Text("Satış Fiyatı: ${duplicate.salePriceLabel}")
                    Text("Stok: ${duplicate.stockLabel}")
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::rescanAfterDuplicate) {
                    Text("Yeni Barkod Tara")
                }
            },
            confirmButton = {
                Button(onClick = viewModel::editDuplicateProduct) {
                    Text("Ürünü Düzenle")
                }
            }
        )
    }
}



