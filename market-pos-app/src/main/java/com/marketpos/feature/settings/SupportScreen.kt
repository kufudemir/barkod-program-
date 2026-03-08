package com.marketpos.feature.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.marketpos.domain.model.SupportTicketSummary
import com.marketpos.domain.model.SupportTicketType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SupportScreen(
    viewModel: SupportViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = SnackbarHostState()

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is SupportEvent.ShowMessage -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    Scaffold(
        snackbarHost = { com.marketpos.ui.components.CenteredSnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("Destek ve Geri Bildirim", style = MaterialTheme.typography.headlineSmall)
            Text("Hata bildirimi, ozellik istegi veya genel geri bildirim gonderebilirsiniz.")

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { viewModel.startCreateMode() },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Yeni Ticket")
                }
                OutlinedButton(
                    onClick = { viewModel.refreshInbox(keepSelection = true) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(if (uiState.isLoadingInbox) "Yukleniyor..." else "Yenile")
                }
            }

            StatusFilterRow(
                selected = uiState.statusFilter,
                onSelect = viewModel::updateStatusFilter
            )

            if (uiState.isCreateMode) {
                CreateTicketCard(
                    uiState = uiState,
                    onTypeSelected = viewModel::updateCreateType,
                    onTitleChange = viewModel::updateCreateTitle,
                    onDescriptionChange = viewModel::updateCreateDescription,
                    onCancel = viewModel::cancelCreateMode,
                    onSubmit = viewModel::submitCreate
                )
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Gelen Kutusu", style = MaterialTheme.typography.titleMedium)

                    if (uiState.isLoadingInbox && uiState.tickets.isEmpty()) {
                        CircularProgressIndicator()
                    } else if (uiState.tickets.isEmpty()) {
                        Text("Ticket bulunamadi.")
                    } else {
                        uiState.tickets.forEach { ticket ->
                            TicketListRow(
                                ticket = ticket,
                                selected = uiState.selectedTicketId == ticket.ticketId,
                                onSelect = { viewModel.selectTicket(ticket.ticketId) }
                            )
                        }
                    }
                }
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Ticket Detayi", style = MaterialTheme.typography.titleMedium)

                    val detail = uiState.selectedTicket
                    if (uiState.isLoadingDetail && detail == null) {
                        CircularProgressIndicator()
                    } else if (detail == null) {
                        Text("Detay gormek icin ticket secin.")
                    } else {
                        Text("#${detail.ticketId} - ${detail.title}")
                        Text("Durum: ${statusLabel(detail.status)}")
                        Text("Tur: ${typeLabel(detail.type)}")
                        Text("Olusturma: ${detail.createdAt?.toReadableDateTime() ?: "-"}")
                        Text("Aciklama: ${detail.description}")

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 260.dp)
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            detail.messages.forEach { message ->
                                Card(modifier = Modifier.fillMaxWidth()) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(10.dp),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            "${if (message.authorType == "admin") "Admin" else "Kullanici"} - ${message.createdAt?.toReadableDateTime() ?: "-"}",
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                        Text(message.message)
                                    }
                                }
                            }
                        }

                        if (detail.status == "closed") {
                            OutlinedButton(
                                onClick = viewModel::reopenTicket,
                                modifier = Modifier.fillMaxWidth(),
                                enabled = !uiState.isSubmitting
                            ) {
                                Text("Ticketi Yeniden Ac")
                            }
                        } else {
                            OutlinedTextField(
                                value = uiState.replyMessage,
                                onValueChange = viewModel::updateReplyMessage,
                                modifier = Modifier.fillMaxWidth(),
                                minLines = 3,
                                label = { Text("Yanıtınız") }
                            )
                            Button(
                                onClick = viewModel::sendReply,
                                modifier = Modifier.fillMaxWidth(),
                                enabled = !uiState.isSubmitting
                            ) {
                                Text(if (uiState.isSubmitting) "Gonderiliyor..." else "Yaniti Gonder")
                            }
                        }
                    }
                }
            }

            OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
                Text("Ayarlar'a Don")
            }
        }
    }
}

@Composable
private fun CreateTicketCard(
    uiState: SupportUiState,
    onTypeSelected: (SupportTicketType) -> Unit,
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onCancel: () -> Unit,
    onSubmit: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Yeni Ticket", style = MaterialTheme.typography.titleMedium)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SupportTicketType.entries.forEach { type ->
                    OutlinedButton(
                        onClick = { onTypeSelected(type) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(type.shortLabel())
                    }
                }
            }

            OutlinedTextField(
                value = uiState.createTitle,
                onValueChange = onTitleChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Baslik") }
            )
            OutlinedTextField(
                value = uiState.createDescription,
                onValueChange = onDescriptionChange,
                modifier = Modifier.fillMaxWidth(),
                minLines = 4,
                label = { Text("Aciklama") }
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(onClick = onCancel, modifier = Modifier.weight(1f)) {
                    Text("Vazgec")
                }
                Button(
                    onClick = onSubmit,
                    modifier = Modifier.weight(1f),
                    enabled = !uiState.isSubmitting
                ) {
                    Text(if (uiState.isSubmitting) "Gonderiliyor..." else "Olustur")
                }
            }
        }
    }
}

@Composable
private fun TicketListRow(
    ticket: SupportTicketSummary,
    selected: Boolean,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onSelect
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text("#${ticket.ticketId} - ${ticket.title}")
            Text("Durum: ${statusLabel(ticket.status)} · Tur: ${typeLabel(ticket.type)}")
            Text("Guncelleme: ${ticket.updatedAt?.toReadableDateTime() ?: "-"}")
            if (selected) {
                Text("Secili", style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
private fun StatusFilterRow(
    selected: String,
    onSelect: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        listOf(
            "" to "Tum",
            "new" to "Yeni",
            "reviewing" to "Inceleme",
            "answered" to "Yanit",
            "closed" to "Kapali"
        ).forEach { (key, label) ->
            OutlinedButton(
                onClick = { onSelect(key) },
                modifier = Modifier.weight(1f)
            ) {
                Text(label)
            }
        }
    }
}

private fun SupportTicketType.shortLabel(): String = when (this) {
    SupportTicketType.BUG -> "Hata"
    SupportTicketType.FEATURE_REQUEST -> "Ozellik"
    SupportTicketType.GENERAL -> "Genel"
}

private fun statusLabel(status: String): String = when (status) {
    "new" -> "Yeni"
    "reviewing" -> "Incelemede"
    "answered" -> "Yanitlandi"
    "closed" -> "Kapali"
    else -> status
}

private fun typeLabel(type: String): String = when (type) {
    "bug" -> "Hata"
    "feature_request" -> "Ozellik"
    "general" -> "Genel"
    else -> type
}

private fun Long.toReadableDateTime(): String {
    return runCatching {
        val formatter = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale("tr", "TR"))
        formatter.format(Date(this))
    }.getOrDefault("-")
}
