package com.marketpos.ui.components

import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@Composable
fun TextRecognitionCameraView(
    modifier: Modifier = Modifier,
    flashEnabled: Boolean,
    captureSignal: Int,
    onCaptureResult: (String, List<String>) -> Unit
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val recognizer = remember { TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS) }
    val executor: ExecutorService = remember { Executors.newSingleThreadExecutor() }
    val currentCaptureSignal by rememberUpdatedState(captureSignal)
    val currentOnCaptureResult by rememberUpdatedState(onCaptureResult)
    var isReady by remember { mutableStateOf(false) }
    var enableTorch by remember { mutableStateOf<(Boolean) -> Unit>({}) }

    LaunchedEffect(flashEnabled) {
        enableTorch(flashEnabled)
    }

    AndroidView(
        modifier = modifier,
        factory = { context ->
            val previewView = PreviewView(context).apply {
                scaleType = PreviewView.ScaleType.FILL_CENTER
            }
            val providerFuture = ProcessCameraProvider.getInstance(context)
            providerFuture.addListener(
                {
                    val provider = providerFuture.get()
                    val preview = Preview.Builder().build().also {
                        it.surfaceProvider = previewView.surfaceProvider
                    }
                    val analysis = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()
                    analysis.setAnalyzer(
                        executor,
                        TextCameraAnalyzer(
                            recognizer = recognizer,
                            captureSignalProvider = { currentCaptureSignal },
                            onCaptureResult = { text, lines -> currentOnCaptureResult(text, lines) }
                        )
                    )

                    provider.unbindAll()
                    val camera = provider.bindToLifecycle(
                        lifecycleOwner,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        preview,
                        analysis
                    )
                    enableTorch = { camera.cameraControl.enableTorch(it) }
                    isReady = true
                },
                ContextCompat.getMainExecutor(context)
            )
            previewView
        }
    )

    if (!isReady) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            executor.shutdown()
            recognizer.close()
        }
    }
}

private class TextCameraAnalyzer(
    private val recognizer: com.google.mlkit.vision.text.TextRecognizer,
    private val captureSignalProvider: () -> Int,
    private val onCaptureResult: (String, List<String>) -> Unit
) : ImageAnalysis.Analyzer {
    private var lastHandledSignal = 0
    private var isProcessing = false

    override fun analyze(imageProxy: ImageProxy) {
        val signal = captureSignalProvider()
        if (signal <= 0 || signal == lastHandledSignal || isProcessing) {
            imageProxy.close()
            return
        }
        lastHandledSignal = signal
        isProcessing = true

        val mediaImage = imageProxy.image ?: run {
            isProcessing = false
            imageProxy.close()
            return
        }

        val inputImage = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
        recognizer.process(inputImage)
            .addOnSuccessListener { result ->
                val centerLines = extractCenterLines(result, imageProxy)
                val text = centerLines.joinToString("\n").trim()
                onCaptureResult(text, centerLines)
            }
            .addOnCompleteListener {
                isProcessing = false
                imageProxy.close()
            }
    }

    private fun extractCenterLines(result: Text, imageProxy: ImageProxy): List<String> {
        val rotation = imageProxy.imageInfo.rotationDegrees
        val imageWidth = if (rotation == 90 || rotation == 270) imageProxy.height.toFloat() else imageProxy.width.toFloat()
        val imageHeight = if (rotation == 90 || rotation == 270) imageProxy.width.toFloat() else imageProxy.height.toFloat()
        val focusRect = Rect(
            left = imageWidth * 0.16f,
            top = imageHeight * 0.24f,
            right = imageWidth * 0.84f,
            bottom = imageHeight * 0.60f
        )

        return result.textBlocks
            .flatMap { block -> block.lines }
            .mapNotNull { line ->
                val box = line.boundingBox ?: return@mapNotNull null
                val centerX = (box.left + box.right) / 2f
                val centerY = (box.top + box.bottom) / 2f
                if (!focusRect.contains(Offset(centerX, centerY))) return@mapNotNull null
                line.text.trim().takeIf { it.isNotBlank() }
            }
    }
}
