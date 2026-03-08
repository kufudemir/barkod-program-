package com.marketpos.feature.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.platform.LocalClipboardManager
import com.marketpos.core.util.DateUtils
import com.marketpos.domain.model.PremiumFeature
import com.marketpos.domain.model.PremiumSource

@Composable
fun PremiumScreen(
    viewModel: PremiumViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbar = remember { SnackbarHostState() }
    val clipboard = LocalClipboardManager.current

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is PremiumEvent.ShowMessage -> snackbar.showSnackbar(event.message)
                is PremiumEvent.CopyText -> {
                    clipboard.setText(AnnotatedString(event.text))
                    snackbar.showSnackbar(event.message)
                }
            }
        }
    }

    val canStartTrial = !uiState.state.isPro && !uiState.state.trialUsed

    Scaffold(snackbarHost = { com.marketpos.ui.components.CenteredSnackbarHost(snackbar) }) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Premium", style = MaterialTheme.typography.headlineSmall)

            uiState.focusedFeature?.let { focused ->
                InfoCard("Bu özellik PRO sürümde") {
                    Text(focused.title, fontWeight = FontWeight.Bold)
                    Text("Lisans veya deneme açıldığında bu özellik anında kullanılabilir.")
                }
            }

            InfoCard("Sürüm Durumu") {
                Text(if (uiState.state.isPro) "PRO" else "FREE", style = MaterialTheme.typography.headlineSmall)
                Text("Kaynak: ${sourceLabel(uiState.state.source)}")
                Text(
                    "Kayıtlı kullanıcı oturumunda premium durumunuz hesabınıza senkronize edilir ve yeni cihazda geri yüklenebilir.",
                    style = MaterialTheme.typography.bodySmall
                )
                uiState.state.activatedAt?.let { Text("Aktif edilme: ${DateUtils.formatDateTime(it)}") }
                uiState.state.expiresAt?.let { Text("Biti?: ${DateUtils.formatDateTime(it)}") }
                Text("Cihaz Kodu", fontWeight = FontWeight.SemiBold)
                Text(uiState.state.deviceCode, fontWeight = FontWeight.SemiBold)
                OutlinedButton(onClick = viewModel::copyDeviceCode, modifier = Modifier.fillMaxWidth()) {
                    Text("Cihaz Kodunu Kopyala")
                }
                uiState.state.licenseCodeMasked?.let { Text("Lisans: $it") }
            }

            InfoCard("Free / Pro Karşılaştırma") {
                OutlinedButton(onClick = viewModel::toggleComparison, modifier = Modifier.fillMaxWidth()) {
                    Text(if (uiState.comparisonExpanded) "Karşılaştırmayı Kapat" else "Karşılaştırmayı Göster")
                }
                if (uiState.comparisonExpanded) {
                    ComparisonRow("Tekli barkod tarama", "Var", "Var")
                    ComparisonRow("Seri ürün tarama", "Yok", "Var")
                    ComparisonRow("Internet isim önerisi", "Yok", "Var")
                    ComparisonRow("Ambalajdan Oku", "Yok", "Var")
                    ComparisonRow("BarkodBankası import", "Yok", "Var")
                    ComparisonRow("Raporlar", "Yok", "Var")
                    ComparisonRow("Toplu fiyat / stok güncelleme", "Yok", "Var")
                    ComparisonRow("Sepette özel fiyat / indirim", "Yok", "Var")
                }
            }

            InfoCard("Deneme Sürümü") {
                if (canStartTrial) {
                    Text("PRO özellikleri test etmek için 3 gün veya 7 gün deneme başlatabilirsiniz.")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { viewModel.startTrial(3) },
                            modifier = Modifier.weight(1f),
                            enabled = !uiState.isActivating
                        ) {
                            Text("3 Gün")
                        }
                        Button(
                            onClick = { viewModel.startTrial(7) },
                            modifier = Modifier.weight(1f),
                            enabled = !uiState.isActivating
                        ) {
                            Text("7 Gün")
                        }
                    }
                } else {
                    Text(
                        when {
                            uiState.state.source == PremiumSource.TRIAL && uiState.state.isPro ->
                                "Deneme sürümü aktif."
                            uiState.state.trialUsed ->
                                "Deneme sürümü daha önce kullanıldı."
                            else ->
                                "Deneme sürümü uygun değil."
                        }
                    )
                }
            }

            InfoCard("Lisans Kodu Gir") {
                Text(
                    "Yeni kısa lisans kodları desteklenir. Boşluklu veya alt satırlı yapıştırırsanız da kabul edilir.",
                    style = MaterialTheme.typography.bodySmall
                )
                OutlinedTextField(
                    value = uiState.licenseInput,
                    onValueChange = viewModel::updateLicenseInput,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Lisans Kodu") },
                    minLines = 3,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
                )
                if (uiState.isActivating) {
                    CircularProgressIndicator()
                }
                Button(
                    onClick = viewModel::activateLicense,
                    enabled = !uiState.isActivating,
                    modifier = Modifier.fillMaxWidth()
                ) {
                Text("Lisansı Doğrula ve Aç")
                }
            }

            InfoCard("Aktivasyon Adımları") {
                Text("1. Cihaz kodunu kopyalayın")
                Text("2. Lisans üretici ile bu cihaz için kod oluşturun")
                Text("3. Kodu buraya yapıştırın")
                Text("4. Lisansı Doğrula ve Aç ile PRO'yu aktif edin")
            }

            if (uiState.state.isPro) {
                OutlinedButton(onClick = viewModel::clearLicense, modifier = Modifier.fillMaxWidth()) {
                    Text(if (uiState.state.source == PremiumSource.TRIAL) "Denemeyi Sonlandır" else "Premium Lisansını Temizle")
                }
            }

            PremiumFeature.values().forEach { feature ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Text(text = feature.title, modifier = Modifier.padding(14.dp))
                }
            }

            OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
                Text("Geri")
            }
        }
    }
}

@Composable
private fun InfoCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            content()
        }
    }
}

@Composable
private fun ComparisonRow(
    title: String,
    freeValue: String,
    proValue: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(title, modifier = Modifier.weight(1.4f))
        Text(freeValue, modifier = Modifier.weight(0.6f))
        Text(proValue, modifier = Modifier.weight(0.6f), fontWeight = FontWeight.SemiBold)
    }
}

private fun sourceLabel(source: PremiumSource): String {
    return when (source) {
        PremiumSource.NONE -> "Yok"
        PremiumSource.TRIAL -> "Deneme"
        PremiumSource.LICENSE_CODE -> "Lisans Kodu"
        PremiumSource.GOOGLE_PLAY -> "Google Play"
    }
}



