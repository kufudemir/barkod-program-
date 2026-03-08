package com.marketpos.feature.product

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.marketpos.ui.components.TextRecognitionCameraView

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PackageTextScanScreen(
    barcode: String,
    onSuggestionsReady: (List<String>) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }
    var flashOn by remember { mutableStateOf(false) }
    var cameraActive by remember { mutableStateOf(true) }
    var captureSignal by remember { mutableStateOf(0) }
    val detectedLines = remember { mutableStateListOf<String>() }
    val selectableWords = remember { mutableStateListOf<String>() }
    val selectedWords = remember { mutableStateListOf<String>() }
    val candidates = remember { mutableStateListOf<String>() }
    var customName by remember { mutableStateOf("") }
    var infoMessage by remember { mutableStateOf<String?>(null) }
    val snackbar = remember { SnackbarHostState() }

    val mergedSuggestions by remember {
        derivedStateOf {
            buildList {
                customName.trim().takeIf { it.isNotBlank() }?.let { add(it) }
                addAll(candidates)
            }.distinct()
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted -> hasPermission = granted }

    LaunchedEffect(infoMessage) {
        infoMessage?.let {
            snackbar.showSnackbar(it)
            infoMessage = null
        }
    }

    if (!hasPermission) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Kamera izni gerekli")
                Button(onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }) {
                    Text("İzin Ver")
                }
                OutlinedButton(onClick = onBack) {
                    Text("Geri")
                }
            }
        }
        return
    }

    Scaffold(snackbarHost = { com.marketpos.ui.components.CenteredSnackbarHost(snackbar) }) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (cameraActive) {
                TextRecognitionCameraView(
                    modifier = Modifier.fillMaxSize(),
                    flashEnabled = flashOn,
                    captureSignal = captureSignal,
                    onCaptureResult = { _, lines ->
                        if (lines.isEmpty()) {
                            infoMessage = "Metin algılanamadı. Ürünü kutuya daha net yerleştirip tekrar okutun."
                            return@TextRecognitionCameraView
                        }
                        detectedLines.clear()
                        detectedLines.addAll(lines)
                        selectableWords.clear()
                        selectableWords.addAll(PackageTextSuggestionSupport.extractSelectableWords(lines, barcode))
                        selectedWords.clear()
                        customName = ""
                        candidates.clear()
                        candidates.addAll(PackageTextSuggestionSupport.extractCandidates(lines, barcode))
                        cameraActive = false
                    }
                )

                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .width(280.dp)
                        .height(180.dp)
                        .border(width = 2.dp, color = MaterialTheme.colorScheme.primary)
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                )
            }

            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.82f))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Ambalajdan Oku", style = MaterialTheme.typography.titleLarge)
                Text(
                    if (cameraActive) {
                        "Ürün adını ortadaki kılavuz kutuya hizalayın ve Okut'a basın. Yalnızca orta alan analiz edilir."
                    } else {
                        "Kamera durduruldu. Algılanan kelimelerden ürün adını düzenleyip forma aktarabilirsiniz."
                    },
                    style = MaterialTheme.typography.bodySmall
                )
                if (barcode.isNotBlank()) {
                    Text("Barkod: $barcode", style = MaterialTheme.typography.bodySmall)
                }
                OutlinedButton(onClick = { flashOn = !flashOn }, enabled = cameraActive) {
                    Text(if (flashOn) "Flaş Kapat" else "Flaş Aç")
                }
            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
                    .padding(12.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (cameraActive) {
                    Text("Kamera hazır. Okut'a bastığınızda tek kare analiz edilir ve kamera durur.")
                    Button(
                        onClick = { captureSignal += 1 },
                        enabled = cameraActive,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Okut")
                    }
                } else {
                    Text(
                        "Algılanan adaylar",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    if (candidates.isEmpty()) {
                        Text(
                            "Uygun bir ürün ismi çıkmadı. Ambalajı daha net gösterip tekrar deneyin.",
                            color = MaterialTheme.colorScheme.error
                        )
                    } else {
                        candidates.forEachIndexed { index, candidate ->
                            Text(candidate, style = MaterialTheme.typography.bodyMedium)
                            if (index == 0) {
                                Text(
                                    "Öneriler tam değilse alttaki kelimelerden ürün adını kendiniz oluşturun.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    Text(
                        "Algılanan kelimeler",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (selectableWords.isEmpty()) {
                        Text("Seçilebilir kelime bulunamadı", style = MaterialTheme.typography.bodySmall)
                    } else {
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            selectableWords.forEach { word ->
                                AssistChip(
                                    onClick = {
                                        selectedWords.add(word)
                                        customName = PackageTextSuggestionSupport.cleanupCustomName(selectedWords.joinToString(" "))
                                    },
                                    label = { Text(word) }
                                )
                            }
                        }
                    }

                    Text(
                        "Özel Ürün İsmi",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    OutlinedTextField(
                        value = customName,
                        onValueChange = { customName = PackageTextSuggestionSupport.cleanupCustomName(it) },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Kelime seçerek veya elle düzenleyerek ürün adını oluşturun") }
                    )

                    if (selectedWords.isNotEmpty()) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            selectedWords.forEachIndexed { index, word ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(word, modifier = Modifier.weight(1f))
                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        TextButton(
                                            onClick = {
                                                if (index > 0) {
                                                    val moved = selectedWords.removeAt(index)
                                                    selectedWords.add(index - 1, moved)
                                                    customName = PackageTextSuggestionSupport.cleanupCustomName(selectedWords.joinToString(" "))
                                                }
                                            }
                                        ) { Text("<") }
                                        TextButton(
                                            onClick = {
                                                if (index < selectedWords.lastIndex) {
                                                    val moved = selectedWords.removeAt(index)
                                                    selectedWords.add(index + 1, moved)
                                                    customName = PackageTextSuggestionSupport.cleanupCustomName(selectedWords.joinToString(" "))
                                                }
                                            }
                                        ) { Text(">") }
                                        TextButton(
                                            onClick = {
                                                selectedWords.removeAt(index)
                                                customName = PackageTextSuggestionSupport.cleanupCustomName(selectedWords.joinToString(" "))
                                            }
                                        ) { Text("Sil") }
                                    }
                                }
                            }
                        }
                    }

                    OutlinedButton(
                        onClick = {
                            if (selectedWords.isNotEmpty()) {
                                selectedWords.removeAt(selectedWords.lastIndex)
                                customName = PackageTextSuggestionSupport.cleanupCustomName(selectedWords.joinToString(" "))
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = selectedWords.isNotEmpty()
                    ) {
                        Text("Son Kelimeyi Geri Al")
                    }

                    OutlinedButton(
                        onClick = { customName = PackageTextSuggestionSupport.cleanupCustomName(customName) },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = customName.isNotBlank()
                    ) {
                        Text("Özel İsmi Düzenle")
                    }

                    OutlinedButton(
                        onClick = {
                            selectedWords.clear()
                            customName = ""
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = selectedWords.isNotEmpty() || customName.isNotBlank()
                    ) {
                        Text("Özel İsmi Temizle")
                    }

                    Button(
                        onClick = { onSuggestionsReady(mergedSuggestions) },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = mergedSuggestions.isNotEmpty()
                    ) {
                        Text("Önerileri Ürün Formuna Aktar")
                    }

                    Text(
                        "Algılanan satırlar",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    if (detectedLines.isEmpty()) {
                        Text("-", style = MaterialTheme.typography.bodySmall, modifier = Modifier.sizeIn(minHeight = 48.dp))
                    } else {
                        detectedLines.forEach { line ->
                            Text(line, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }

                OutlinedButton(
                    onClick = {
                        detectedLines.clear()
                        selectableWords.clear()
                        selectedWords.clear()
                        candidates.clear()
                        customName = ""
                        cameraActive = true
                        captureSignal = 0
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (cameraActive) "Taramayı Sıfırla" else "Tekrar Tara")
                }

                OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
                    Text("Geri")
                }
            }
        }
    }
}
