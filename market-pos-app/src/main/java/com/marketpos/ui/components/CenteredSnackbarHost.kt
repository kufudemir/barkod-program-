package com.marketpos.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
fun CenteredSnackbarHost(
    hostState: SnackbarHostState,
    autoDismissMillis: Long = 2_000L
) {
    val snackbarData = hostState.currentSnackbarData ?: return

    LaunchedEffect(snackbarData.visuals.message) {
        delay(autoDismissMillis)
        snackbarData.dismiss()
    }

    Dialog(onDismissRequest = { snackbarData.dismiss() }) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = snackbarData.visuals.message,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Button(
                    onClick = { snackbarData.dismiss() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Tamam")
                }
            }
        }
    }
}

