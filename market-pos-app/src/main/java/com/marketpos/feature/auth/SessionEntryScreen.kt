package com.marketpos.feature.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun SessionEntryScreen(
    viewModel: SessionEntryViewModel = hiltViewModel(),
    onNavigateActivation: () -> Unit,
    onNavigateLogin: () -> Unit,
    onNavigateRegister: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                SessionEntryEvent.NavigateActivation -> onNavigateActivation()
                is SessionEntryEvent.ShowMessage -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    Scaffold(snackbarHost = { com.marketpos.ui.components.CenteredSnackbarHost(snackbarHostState) }) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp, androidx.compose.ui.Alignment.CenterVertically)
        ) {
            Text("Oturum Seçimi", style = MaterialTheme.typography.headlineSmall)
            Text("Misafir olarak devam edebilir veya kayıtlı hesapla giriş yapabilirsiniz. Kayıtlı hesap, ileride veri ve premium kurtarma için temel oluşturur.")

            Button(onClick = viewModel::continueAsGuest, modifier = Modifier.fillMaxWidth()) {
                Text("Misafir Olarak Devam Et")
            }

            OutlinedButton(onClick = onNavigateLogin, modifier = Modifier.fillMaxWidth()) {
                Text("Giriş Yap")
            }

            OutlinedButton(onClick = onNavigateRegister, modifier = Modifier.fillMaxWidth()) {
                Text("Kayıt Ol")
            }
        }
    }
}

