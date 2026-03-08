package com.marketpos.feature.activation

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.marketpos.core.legal.LegalContent
import com.marketpos.ui.components.LegalTextDialog

@Composable
fun AppBootstrapScreen(
    viewModel: AppBootstrapViewModel = hiltViewModel(),
    onNavigateSessionEntry: () -> Unit,
    onNavigateActivation: () -> Unit,
    onNavigateModeSelection: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var showDisclosureDialog by remember { mutableStateOf(false) }
    var showUsageDialog by remember { mutableStateOf(false) }
    var consentChecked by remember(uiState.legalConsentAccepted) { mutableStateOf(uiState.legalConsentAccepted) }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                AppBootstrapEvent.NavigateSessionEntry -> onNavigateSessionEntry()
                AppBootstrapEvent.NavigateActivation -> onNavigateActivation()
                AppBootstrapEvent.NavigateModeSelection -> onNavigateModeSelection()
            }
        }
    }

    uiState.pendingUpdate?.let { update ->
        AlertDialog(
            onDismissRequest = {
                if (!update.forceUpdate) {
                    viewModel.skipUpdateAndContinue()
                }
            },
            title = { Text("Yeni Sürüm Hazır") },
            text = {
                val notesText = update.notes?.takeIf { it.isNotBlank() }
                Text(
                    buildString {
                        append("v${update.versionName} sürümü kullanıma hazır.\n\n")
                        append("Uygulamayı web sitesi üzerinden indirip güncelleyebilirsiniz.")
                        if (notesText != null) {
                            append("\n\nGüncelleme Notları:\n")
                            append(notesText)
                        }
                    }
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        context.startActivity(Intent(Intent.ACTION_VIEW).apply {
                            data = android.net.Uri.parse(update.apkUrl)
                        })
                        viewModel.continueAfterOpeningUpdateLink()
                    }
                ) {
                    Text("İndir ve Güncelle")
                }
            },
            dismissButton = if (!update.forceUpdate) {
                {
                    TextButton(onClick = viewModel::skipUpdateAndContinue) {
                        Text("Şimdilik Geç")
                    }
                }
            } else {
                null
            }
        )
    }

    if (uiState.requiresLegalConsent) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text("Veri Kullanımı Onayı", style = MaterialTheme.typography.headlineSmall)
                    Text(
                        "Devam etmeden önce aydınlatma metnini ve veri kullanım açıklamasını onaylamanız gerekir.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(LegalContent.SHORT_NOTICE, style = MaterialTheme.typography.bodySmall)

                    OutlinedButton(
                        onClick = { showDisclosureDialog = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Aydınlatma Metnini Gör")
                    }

                    OutlinedButton(
                        onClick = { showUsageDialog = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Veri Kullanım Açıklamasını Gör")
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top
                    ) {
                        Checkbox(
                            checked = consentChecked,
                            onCheckedChange = { consentChecked = it }
                        )
                        Text(
                            text = LegalContent.CONSENT_LABEL,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = 12.dp)
                        )
                    }

                    Button(
                        onClick = viewModel::acceptLegalConsent,
                        enabled = consentChecked,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Onayla ve Devam Et")
                    }
                }
            }
        }
    } else {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
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
}
