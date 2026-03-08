package com.marketpos.feature.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ForgotPasswordScreen(
    viewModel: ForgotPasswordViewModel = hiltViewModel(),
    onBackToLogin: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { androidx.compose.material3.SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is ForgotPasswordEvent.ShowMessage -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    Scaffold(snackbarHost = { com.marketpos.ui.components.CenteredSnackbarHost(snackbarHostState) }) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Şifremi Unuttum", style = androidx.compose.material3.MaterialTheme.typography.headlineSmall)
            Text(
                "E-posta adresinize 6 haneli kod gönderilir. Kodu girdikten sonra yeni şifrenizi belirleyebilirsiniz.",
                style = androidx.compose.material3.MaterialTheme.typography.bodyMedium
            )

            OutlinedTextField(
                value = uiState.email,
                onValueChange = viewModel::updateEmail,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("E-posta") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                isError = uiState.error != null
            )

            Button(
                onClick = viewModel::requestCode,
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isRequestingCode
            ) {
                Text(if (uiState.isRequestingCode) "Kod gönderiliyor..." else "Kodu Gönder")
            }

            uiState.expiresAt?.let {
                Text(
                    "Kodun tahmini bitiş süresi: ${SimpleDateFormat("dd.MM.yyyy HH:mm", Locale("tr", "TR")).format(Date(it))}",
                    style = androidx.compose.material3.MaterialTheme.typography.bodySmall
                )
            }

            OutlinedTextField(
                value = uiState.code,
                onValueChange = viewModel::updateCode,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("6 Haneli Kod") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                isError = uiState.error != null,
                supportingText = { uiState.error?.let { Text(it) } }
            )

            OutlinedTextField(
                value = uiState.newPassword,
                onValueChange = viewModel::updateNewPassword,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Yeni Şifre") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )

            OutlinedTextField(
                value = uiState.confirmPassword,
                onValueChange = viewModel::updateConfirmPassword,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Yeni Şifre Tekrar") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )

            Button(
                onClick = viewModel::resetPassword,
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isResetting
            ) {
                Text(if (uiState.isResetting) "Şifre güncelleniyor..." else "Şifreyi Sıfırla")
            }

            TextButton(onClick = onBackToLogin, modifier = Modifier.fillMaxWidth()) {
                Text("Giriş ekranına dön")
            }
        }
    }

    if (uiState.showSuccessDialog) {
        AlertDialog(
            onDismissRequest = viewModel::dismissSuccessDialog,
            title = { Text("Şifre Sıfırlandı") },
            text = { Text("Şifreniz başarıyla güncellendi. Giriş ekranına dönebilirsiniz.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.dismissSuccessDialog()
                        onBackToLogin()
                    }
                ) {
                    Text("Giriş Yap")
                }
            }
        )
    }
}
