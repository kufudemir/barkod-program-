package com.marketpos.feature.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.marketpos.BuildConfig
import com.marketpos.core.legal.LegalContent
import com.marketpos.domain.model.AppMode
import com.marketpos.domain.model.AppThemeMode
import com.marketpos.domain.model.PremiumFeature
import com.marketpos.domain.model.ScanBoxSizeOption
import com.marketpos.domain.model.SerialScanCooldownOption
import com.marketpos.ui.components.LegalTextDialog
import com.marketpos.ui.components.PinDialog
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val RESET_CONFIRMATION_TEXT = "katalog sıfırla"
private const val CLEAR_CONNECTION_TEXT = "firma bağını sıfırla"
private val DangerRed = Color(0xFFD50000)

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onOpenPremium: (PremiumFeature?) -> Unit,
    onOpenManualAdd: () -> Unit,
    onOpenBarcodeBankasiImport: () -> Unit,
    onOpenWebBarcodeSearch: () -> Unit,
    onOpenProductList: () -> Unit,
    onOpenBulkPriceUpdate: () -> Unit,
    onOpenBulkStockUpdate: () -> Unit,
    onOpenReports: () -> Unit,
    onOpenStockTracking: () -> Unit,
    onOpenStockCount: () -> Unit,
    onOpenSupport: () -> Unit,
    onOpenLogin: () -> Unit,
    onOpenRegister: () -> Unit,
    onConnectionCleared: () -> Unit,
    onBackToScan: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val isAdmin = uiState.mode == AppMode.ADMIN
    val isPro = uiState.premiumState.isPro
    val snackbar = remember { SnackbarHostState() }

    var expandedSection by remember { mutableStateOf<String?>(null) }
    var showAdminPinDialog by remember { mutableStateOf(false) }
    var showPinChangeDialog by remember { mutableStateOf(false) }
    var showPasswordChangeDialog by remember { mutableStateOf(false) }
    var showResetPhraseDialog by remember { mutableStateOf(false) }
    var showResetPinDialog by remember { mutableStateOf(false) }
    var showResetFinalDialog by remember { mutableStateOf(false) }
    var resetPhraseInput by remember { mutableStateOf("") }
    var resetApprovedPin by remember { mutableStateOf<String?>(null) }
    var showClearPhraseDialog by remember { mutableStateOf(false) }
    var showClearPinDialog by remember { mutableStateOf(false) }
    var showClearFinalDialog by remember { mutableStateOf(false) }
    var clearPhraseInput by remember { mutableStateOf("") }
    var clearApprovedPin by remember { mutableStateOf<String?>(null) }
    var showDisclosureDialog by remember { mutableStateOf(false) }
    var showUsageDialog by remember { mutableStateOf(false) }

    fun clearResetFlow() {
        showResetPhraseDialog = false
        showResetPinDialog = false
        showResetFinalDialog = false
        resetPhraseInput = ""
        resetApprovedPin = null
    }

    fun clearConnectionFlow() {
        showClearPhraseDialog = false
        showClearPinDialog = false
        showClearFinalDialog = false
        clearPhraseInput = ""
        clearApprovedPin = null
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is SettingsEvent.ShowMessage -> snackbar.showSnackbar(event.message)
                is SettingsEvent.RequestCatalogResetConfirmation -> {
                    resetApprovedPin = event.verifiedPin
                    showResetFinalDialog = true
                }
                SettingsEvent.ConnectionCleared -> onConnectionCleared()
                SettingsEvent.SessionCleared -> onConnectionCleared()
                SettingsEvent.PasswordChanged -> showPasswordChangeDialog = false
            }
        }
    }

    Scaffold(snackbarHost = { com.marketpos.ui.components.CenteredSnackbarHost(snackbar) }) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Ayarlar", style = MaterialTheme.typography.headlineSmall)
            Text(
                "Burada hesap, firma, premium ve yönetim ayarlarını alt menüler halinde yönetebilirsiniz.",
                style = MaterialTheme.typography.bodyMedium
            )

            SettingsMenuCard(
                title = "Hesap ve Firma",
                summary = buildAccountSummary(uiState),
                expanded = expandedSection == "account",
                onToggle = { expandedSection = if (expandedSection == "account") null else "account" }
            ) {
                Text("Firma Bilgisi", style = MaterialTheme.typography.titleSmall)
                if (uiState.activationState.isActivated) {
                    Text("Firma: ${uiState.activationState.companyName.orEmpty()}")
                    Text("Firma Kodu: ${uiState.activationState.companyCode.orEmpty()}")
                    Text("Cihaz: ${uiState.activationState.deviceName.orEmpty()}")
                    Text("Cihaz Kimliği: ${uiState.activationState.deviceUid.orEmpty()}", style = MaterialTheme.typography.bodySmall)
                } else {
                    Text("Bu cihaz henüz bir firmaya bağlı değil.")
                }

                Text("Oturum Bilgisi", style = MaterialTheme.typography.titleSmall)
                Text(
                    "Oturum Tipi: ${
                        when {
                            uiState.accountSession.isRegistered -> "Kayıtlı Kullanıcı"
                            uiState.accountSession.isGuest -> "Misafir"
                            else -> "Seçilmemiş"
                        }
                    }"
                )
                if (uiState.accountSession.isRegistered) {
                    Text("Kullanıcı: ${uiState.accountSession.userName.orEmpty()}")
                    Text("E-posta: ${uiState.accountSession.userEmail.orEmpty()}")
                    Text(
                        "Kayıtlı oturumda premium ve hesap bilgileri yeni cihaza geri yüklenebilir.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    OutlinedButton(
                        onClick = { showPasswordChangeDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !uiState.isChangingPassword
                    ) {
                        Text(if (uiState.isChangingPassword) "Şifre güncelleniyor..." else "Şifre Değiştir")
                    }
                    OutlinedButton(
                        onClick = viewModel::logoutSession,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Oturumu Kapat")
                    }
                } else {
                    Text(
                        "Misafir oturumunda kullanım devam eder. Hesap açarsanız premium ve verileriniz ileride geri yüklenebilir.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Button(onClick = onOpenLogin, modifier = Modifier.fillMaxWidth()) {
                        Text("Giriş Yap")
                    }
                    OutlinedButton(onClick = onOpenRegister, modifier = Modifier.fillMaxWidth()) {
                        Text("Kayıt Ol")
                    }
                }

                Text("Senkron Durumu", style = MaterialTheme.typography.titleSmall)
                Text("Bekleyen Kayıt: ${uiState.pendingSyncCount}")
                Text("Son Başarılı Senkron: ${uiState.activationState.lastSyncSuccessAt?.toReadableDateTime() ?: "-"}")
                Text("Son Hata: ${uiState.activationState.lastSyncError ?: "-"}")
                Button(
                    onClick = viewModel::syncNow,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = uiState.activationState.isActivated && !uiState.isSyncing
                ) {
                    Text(if (uiState.isSyncing) "Gönderiliyor..." else "Şimdi Senkronize Et")
                }

                Text("Aydınlatma ve Veri Kullanımı", style = MaterialTheme.typography.titleSmall)
                Text(
                    "Onay Sürümü: ${uiState.legalConsentState.acceptedVersion ?: uiState.legalConsentState.currentVersion}",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    "Onay Zamanı: ${uiState.legalConsentState.acceptedAt?.toReadableDateTime() ?: "-"}",
                    style = MaterialTheme.typography.bodySmall
                )
                OutlinedButton(onClick = { showDisclosureDialog = true }, modifier = Modifier.fillMaxWidth()) {
                    Text("Aydınlatma Metni")
                }
                OutlinedButton(onClick = { showUsageDialog = true }, modifier = Modifier.fillMaxWidth()) {
                    Text("Veri Kullanımı")
                }
                OutlinedButton(onClick = onOpenSupport, modifier = Modifier.fillMaxWidth()) {
                    Text("Destek ve Geri Bildirim")
                }
            }

            SettingsMenuCard(
                title = "Görünüm ve Özellikler",
                summary = "Tema: ${uiState.themeMode.label} / Bekleme: ${uiState.serialScanCooldown.label} / Kutu: ${uiState.scanBoxSize.label}",
                expanded = expandedSection == "appearance",
                onToggle = { expandedSection = if (expandedSection == "appearance") null else "appearance" }
            ) {
                Text("Tema Seçimi", style = MaterialTheme.typography.titleSmall)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AppThemeMode.entries.forEach { themeMode ->
                        SettingsChoiceButton(
                            selected = uiState.themeMode == themeMode,
                            onClick = { viewModel.updateThemeMode(themeMode) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(themeMode.label)
                        }
                    }
                }

                Text("Seri tarama bekleme süresi", style = MaterialTheme.typography.titleSmall)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SerialScanCooldownOption.entries.forEach { option ->
                        SettingsChoiceButton(
                            selected = uiState.serialScanCooldown == option,
                            onClick = {
                                if (isPro) viewModel.updateSerialScanCooldown(option)
                                else onOpenPremium(PremiumFeature.SERIAL_SCAN)
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(option.label)
                        }
                    }
                }

                Text("Tarama kutusu boyutu", style = MaterialTheme.typography.titleSmall)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ScanBoxSizeOption.entries.forEach { option ->
                        SettingsChoiceButton(
                            selected = uiState.scanBoxSize == option,
                            onClick = { viewModel.updateScanBoxSize(option) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(option.label)
                        }
                    }
                }
            }

            SettingsMenuCard(
                title = "Premium ve Yönetim",
                summary = "Lisans: ${if (isPro) "PRO" else "FREE"} / Mod: ${if (isAdmin) "ADMIN" else "KASIYER"}",
                expanded = expandedSection == "management",
                onToggle = { expandedSection = if (expandedSection == "management") null else "management" }
            ) {
                Button(onClick = { onOpenPremium(null) }, modifier = Modifier.fillMaxWidth()) {
                    Text(if (isPro) "Premium Durumu" else "Premium Aç")
                }

                if (isAdmin) {
                    OutlinedButton(
                        onClick = { viewModel.switchMode(AppMode.CASHIER, pin = null) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Admin Modunu Kapat")
                    }
                } else {
                    Button(
                        onClick = { showAdminPinDialog = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Admin Modunu Aç")
                    }
                }

                if (isAdmin) {
                    Button(onClick = onOpenManualAdd, modifier = Modifier.fillMaxWidth()) {
                        Text("Manuel Ürün Ekle")
                    }
                    OutlinedButton(onClick = { showPinChangeDialog = true }, modifier = Modifier.fillMaxWidth()) {
                        Text("PIN Değiştir")
                    }
                    OutlinedButton(onClick = onOpenProductList, modifier = Modifier.fillMaxWidth()) {
                        Text("Ürün Listesi")
                    }
                    OutlinedButton(onClick = onOpenBulkStockUpdate, modifier = Modifier.fillMaxWidth()) {
                        Text("Toplu Stok Güncelle")
                    }
                    OutlinedButton(
                        onClick = {
                            if (isPro) onOpenStockCount() else onOpenPremium(PremiumFeature.STOCK_TRACKING)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (isPro) "Stok Sayım Modu" else "Stok Sayım Modu [PRO]")
                    }
                    OutlinedButton(
                        onClick = {
                            if (isPro) onOpenBulkPriceUpdate() else onOpenPremium(PremiumFeature.BULK_PRICE_UPDATE)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (isPro) "Toplu Fiyat Güncelle" else "Toplu Fiyat Güncelle [PRO]")
                    }
                    OutlinedButton(
                        onClick = {
                            if (isPro) onOpenReports() else onOpenPremium(PremiumFeature.REPORTS)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (isPro) "Raporlar" else "Raporlar [PRO]")
                    }
                    OutlinedButton(
                        onClick = {
                            if (isPro) onOpenBarcodeBankasiImport() else onOpenPremium(PremiumFeature.BARKOD_BANKASI_IMPORT)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (isPro) "BarkodBankası İçeri Aktar" else "BarkodBankası İçeri Aktar [PRO]")
                    }
                    OutlinedButton(
                        onClick = {
                            if (isPro) onOpenWebBarcodeSearch() else onOpenPremium(PremiumFeature.WEB_BARCODE_SEARCH)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (isPro) "Web'de Barkod Ara" else "Web'de Barkod Ara [PRO]")
                    }
                } else {
                    Text(
                        "Yonetim islemleri icin bu bolumden Admin modunu acabilirsiniz. Giris sonrasi satis modu secimi ayri ekranda yapilir.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            SettingsMenuCard(
                title = "Sistem ve Uygulama",
                summary = "Aktif ürün: ${uiState.activeProductCount} / Satış: ${uiState.totalSaleCount} / Sürüm: ${BuildConfig.VERSION_NAME}",
                expanded = expandedSection == "system",
                onToggle = { expandedSection = if (expandedSection == "system") null else "system" }
            ) {
                Text("Sistem Özeti", style = MaterialTheme.typography.titleSmall)
                Text("Toplam ürün: ${uiState.totalProductCount}")
                Text("Aktif ürün: ${uiState.activeProductCount}")
                Text("Toplam satış: ${uiState.totalSaleCount}")
                OutlinedButton(onClick = viewModel::refreshSystemSummary, modifier = Modifier.fillMaxWidth()) {
                    Text("Özeti Yenile")
                }

                Text("Uygulama Bilgisi", style = MaterialTheme.typography.titleSmall)
                Text("Uygulama Sürümü: ${BuildConfig.VERSION_NAME}")
                Text("Build Numarası: ${BuildConfig.VERSION_CODE}")
                Text("Varsayılan ilk PIN: 1234", style = MaterialTheme.typography.bodySmall)
            }

            if (isAdmin) {
                SettingsMenuCard(
                    title = "Tehlikeli İşlemler",
                    summary = "Katalog sıfırlama ve firma bağlantısı temizleme işlemleri",
                    expanded = expandedSection == "danger",
                    onToggle = { expandedSection = if (expandedSection == "danger") null else "danger" },
                    danger = true
                ) {
                    Text(
                        "Bu bölümdeki işlemler aktif kataloğu, firma bağlantısını veya bekleyen senkron kayıtlarını temizler. Devam etmeden önce neyi sileceğinizi dikkatle kontrol edin.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White
                    )
                    Button(
                        onClick = { showResetPhraseDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !uiState.isResettingCatalog,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DangerRed,
                            contentColor = Color.White
                        )
                    ) {
                        Text(if (uiState.isResettingCatalog) "Sıfırlanıyor..." else "Ürün Listesini Sıfırla")
                    }
                    OutlinedButton(
                        onClick = { showClearPhraseDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                    ) {
                        Text("Firma Bağlantısını Sıfırla")
                    }
                }
            }

            Button(onClick = onBackToScan, modifier = Modifier.fillMaxWidth()) {
                Text("Tarama Ekranına Dön")
            }
        }
    }

    if (showAdminPinDialog) {
        PinDialog(
            title = "Admin PIN",
            onDismiss = { showAdminPinDialog = false },
            onConfirm = { pin ->
                showAdminPinDialog = false
                viewModel.switchMode(AppMode.ADMIN, pin)
            }
        )
    }

    if (showDisclosureDialog) {
        LegalTextDialog(
            title = LegalContent.DISCLOSURE_TITLE,
            body = LegalContent.DISCLOSURE_TEXT,
            onDismiss = { showDisclosureDialog = false }
        )
    }

    if (showUsageDialog) {
        LegalTextDialog(
            title = LegalContent.DATA_USAGE_TITLE,
            body = LegalContent.DATA_USAGE_TEXT,
            onDismiss = { showUsageDialog = false }
        )
    }

    if (showPinChangeDialog) {
        PinChangeDialog(
            onDismiss = { showPinChangeDialog = false },
            onConfirm = { current, new ->
                showPinChangeDialog = false
                viewModel.changePin(current, new)
            }
        )
    }

    if (showPasswordChangeDialog) {
        PasswordChangeDialog(
            isLoading = uiState.isChangingPassword,
            onDismiss = { showPasswordChangeDialog = false },
            onConfirm = { current, new, confirm ->
                viewModel.changeAccountPassword(current, new, confirm)
            }
        )
    }

    if (showResetPhraseDialog) {
        AlertDialog(
            onDismissRequest = { clearResetFlow() },
            title = { Text("Katalog Sıfırlama") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Devam etmek için aşağıdaki metni aynen yazın:")
                    Text(RESET_CONFIRMATION_TEXT, style = MaterialTheme.typography.titleSmall)
                    OutlinedTextField(
                        value = resetPhraseInput,
                        onValueChange = { resetPhraseInput = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Onay Metni") },
                        singleLine = true
                    )
                }
            },
            dismissButton = { TextButton(onClick = { clearResetFlow() }) { Text("Vazgeç") } },
            confirmButton = {
                Button(
                    enabled = resetPhraseInput.trim().lowercase() == RESET_CONFIRMATION_TEXT,
                    onClick = {
                        showResetPhraseDialog = false
                        showResetPinDialog = true
                    }
                ) {
                    Text("Devam Et")
                }
            }
        )
    }

    if (showResetPinDialog) {
        PinDialog(
            title = "Admin PIN Onayi",
            onDismiss = { clearResetFlow() },
            onConfirm = { pin ->
                showResetPinDialog = false
                viewModel.confirmResetPin(pin)
            }
        )
    }

    if (showResetFinalDialog) {
        AlertDialog(
            onDismissRequest = { clearResetFlow() },
            title = { Text("Son Onay") },
            text = {
                Text("Bu işlem tüm aktif ürünleri listeden kaldırır. Satış kayıtları silinmez. Emin misiniz?")
            },
            dismissButton = { TextButton(onClick = { clearResetFlow() }) { Text("Hayır") } },
            confirmButton = {
                Button(
                    onClick = {
                        val pin = resetApprovedPin
                        clearResetFlow()
                        if (!pin.isNullOrBlank()) {
                            viewModel.resetProductCatalog(pin)
                        }
                    }
                ) {
                    Text("Evet, Sıfırla")
                }
            }
        )
    }

    if (showClearPhraseDialog) {
        AlertDialog(
            onDismissRequest = { clearConnectionFlow() },
            title = { Text("Firma Bağlantısını Sıfırla") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Devam etmek için aşağıdaki metni aynen yazın:")
                    Text(CLEAR_CONNECTION_TEXT, style = MaterialTheme.typography.titleSmall)
                    OutlinedTextField(
                        value = clearPhraseInput,
                        onValueChange = { clearPhraseInput = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Onay Metni") },
                        singleLine = true
                    )
                }
            },
            dismissButton = { TextButton(onClick = { clearConnectionFlow() }) { Text("Vazgeç") } },
            confirmButton = {
                Button(
                    enabled = clearPhraseInput.trim().lowercase() == CLEAR_CONNECTION_TEXT,
                    onClick = {
                        showClearPhraseDialog = false
                        showClearPinDialog = true
                    }
                ) {
                    Text("Devam Et")
                }
            }
        )
    }

    if (showClearPinDialog) {
        PinDialog(
            title = "Admin PIN Onayı",
            onDismiss = { clearConnectionFlow() },
            onConfirm = { pin ->
                clearApprovedPin = pin
                showClearPinDialog = false
                showClearFinalDialog = true
            }
        )
    }

    if (showClearFinalDialog) {
        AlertDialog(
            onDismissRequest = { clearConnectionFlow() },
            title = { Text("Son Onay") },
            text = { Text("Firma bağlantısı, aktivasyon tokeni ve bekleyen senkron kayıtları temizlenecek. Emin misiniz?") },
            dismissButton = { TextButton(onClick = { clearConnectionFlow() }) { Text("Hayır") } },
            confirmButton = {
                Button(
                    onClick = {
                        val pin = clearApprovedPin
                        clearConnectionFlow()
                        if (!pin.isNullOrBlank()) {
                            viewModel.clearCompanyConnection(pin)
                        }
                    }
                ) {
                    Text("Evet, Sıfırla")
                }
            }
        )
    }
}

private fun buildAccountSummary(uiState: SettingsUiState): String {
    val sessionLabel = when {
        uiState.accountSession.isRegistered -> uiState.accountSession.userEmail.orEmpty()
        uiState.accountSession.isGuest -> "Misafir oturumu"
        else -> "Oturum seçilmedi"
    }
    val companyLabel = uiState.activationState.companyName ?: "Firma seçimi gerekli"
    return "$sessionLabel / $companyLabel"
}

private fun Long.toReadableDateTime(): String {
    return SimpleDateFormat("dd.MM.yyyy HH:mm", Locale("tr", "TR")).format(Date(this))
}

@Composable
private fun SettingsMenuCard(
    title: String,
    summary: String,
    expanded: Boolean,
    onToggle: () -> Unit,
    danger: Boolean = false,
    content: @Composable ColumnScope.() -> Unit
) {
    val containerColor = if (danger) DangerRed else MaterialTheme.colorScheme.surfaceContainerHighest
    val contentColor = if (danger) Color.White else MaterialTheme.colorScheme.onSurface
    val borderColor = if (danger) DangerRed else MaterialTheme.colorScheme.outlineVariant

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = containerColor, contentColor = contentColor),
        border = BorderStroke(1.dp, borderColor),
        onClick = onToggle
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = contentColor)
            Text(summary, style = MaterialTheme.typography.bodyMedium, color = contentColor)
            Text(if (expanded) "Kapat" else "Aç", style = MaterialTheme.typography.labelLarge, color = contentColor)
            if (expanded) {
                content()
            }
        }
    }
}

@Composable
private fun SettingsChoiceButton(
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit
) {
    if (selected) {
        Button(onClick = onClick, modifier = modifier, content = content)
    } else {
        FilledTonalButton(onClick = onClick, modifier = modifier, content = content)
    }
}

@Composable
private fun PinChangeDialog(
    onDismiss: () -> Unit,
    onConfirm: (current: String, new: String) -> Unit
) {
    var currentPin by remember { mutableStateOf("") }
    var newPin by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("PIN Değiştir") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = currentPin,
                    onValueChange = { if (it.length <= 4 && it.all(Char::isDigit)) currentPin = it },
                    label = { Text("Mevcut PIN") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    visualTransformation = PasswordVisualTransformation()
                )
                OutlinedTextField(
                    value = newPin,
                    onValueChange = { if (it.length <= 4 && it.all(Char::isDigit)) newPin = it },
                    label = { Text("Yeni PIN") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    visualTransformation = PasswordVisualTransformation()
                )
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Vazgeç") } },
        confirmButton = {
            Button(
                enabled = currentPin.length == 4 && newPin.length == 4,
                onClick = { onConfirm(currentPin, newPin) }
            ) {
                Text("Kaydet")
            }
        }
    )
}

@Composable
private fun PasswordChangeDialog(
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (current: String, new: String, confirm: String) -> Unit
) {
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Şifre Değiştir") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Yeni şifre en az 8 karakter olmalı.", style = MaterialTheme.typography.bodySmall)
                OutlinedTextField(
                    value = currentPassword,
                    onValueChange = { currentPassword = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Mevcut Şifre") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                )
                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Yeni Şifre") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                )
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Yeni Şifre Tekrar") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                )
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Vazgeç") } },
        confirmButton = {
            Button(
                enabled = !isLoading && currentPassword.isNotBlank() && newPassword.isNotBlank() && confirmPassword.isNotBlank(),
                onClick = { onConfirm(currentPassword, newPassword, confirmPassword) }
            ) {
                Text(if (isLoading) "Güncelleniyor..." else "Kaydet")
            }
        }
    )
}




