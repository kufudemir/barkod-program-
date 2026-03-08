package com.marketpos.ui.components

import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.marketpos.domain.model.ScanBoxSizeOption
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@Composable
fun BarcodeCameraView(
    modifier: Modifier = Modifier,
    flashEnabled: Boolean,
    scanBoxSize: ScanBoxSizeOption = ScanBoxSizeOption.MEDIUM,
    onBarcodeDetected: (String) -> Unit
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val currentScanBoxSize by rememberUpdatedState(scanBoxSize)
    val currentOnBarcodeDetected by rememberUpdatedState(onBarcodeDetected)
    val scanner = remember {
        BarcodeScanning.getClient(
            BarcodeScannerOptions.Builder()
                .setBarcodeFormats(
                    Barcode.FORMAT_EAN_13,
                    Barcode.FORMAT_EAN_8,
                    Barcode.FORMAT_UPC_A,
                    Barcode.FORMAT_UPC_E
                )
                .build()
        )
    }
    val executor: ExecutorService = remember { Executors.newSingleThreadExecutor() }
    var isReady by remember { mutableStateOf(false) }
    var enableTorch by remember { mutableStateOf<(Boolean) -> Unit>({}) }

    LaunchedEffect(flashEnabled) {
        enableTorch(flashEnabled)
    }

    Box(modifier = modifier.clipToBounds()) {
        AndroidView(
            modifier = Modifier
                .fillMaxSize()
                .clipToBounds(),
            factory = { context ->
                val previewView = PreviewView(context).apply {
                    implementationMode = PreviewView.ImplementationMode.COMPATIBLE
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
                            CameraAnalyzer(
                                scanner = scanner,
                                onBarcodeDetected = { barcode -> currentOnBarcodeDetected(barcode) },
                                scanBoxSizeProvider = { currentScanBoxSize }
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

        BarcodeScanOverlay(
            modifier = Modifier.fillMaxSize(),
            scanBoxSize = scanBoxSize
        )
    }

    if (!isReady) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            executor.shutdown()
            scanner.close()
        }
    }
}

@Composable
private fun BarcodeScanOverlay(
    modifier: Modifier,
    scanBoxSize: ScanBoxSizeOption
) {
    Canvas(modifier = modifier) {
        val overlayColor = Color.Black.copy(alpha = 0.42f)
        val boxWidth = size.width * scanBoxSize.widthFraction
        val boxHeight = size.height * scanBoxSize.heightFraction
        val left = (size.width - boxWidth) / 2f
        val top = (size.height - boxHeight) / 2f

        drawRect(color = overlayColor, size = Size(size.width, top))
        drawRect(
            color = overlayColor,
            topLeft = Offset(0f, top + boxHeight),
            size = Size(size.width, size.height - top - boxHeight)
        )
        drawRect(
            color = overlayColor,
            topLeft = Offset(0f, top),
            size = Size(left, boxHeight)
        )
        drawRect(
            color = overlayColor,
            topLeft = Offset(left + boxWidth, top),
            size = Size(size.width - left - boxWidth, boxHeight)
        )

        drawRect(
            color = Color.White.copy(alpha = 0.95f),
            topLeft = Offset(left, top),
            size = Size(boxWidth, boxHeight),
            style = Stroke(width = 4f)
        )

        val accent = Color(0xFF4CAF50)
        val cornerLength = 44f
        val strokeWidth = 8f

        drawLine(accent, Offset(left, top), Offset(left + cornerLength, top), strokeWidth = strokeWidth, cap = StrokeCap.Round)
        drawLine(accent, Offset(left, top), Offset(left, top + cornerLength), strokeWidth = strokeWidth, cap = StrokeCap.Round)
        drawLine(
            accent,
            Offset(left + boxWidth, top),
            Offset(left + boxWidth - cornerLength, top),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
        drawLine(
            accent,
            Offset(left + boxWidth, top),
            Offset(left + boxWidth, top + cornerLength),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
        drawLine(
            accent,
            Offset(left, top + boxHeight),
            Offset(left + cornerLength, top + boxHeight),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
        drawLine(
            accent,
            Offset(left, top + boxHeight),
            Offset(left, top + boxHeight - cornerLength),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
        drawLine(
            accent,
            Offset(left + boxWidth, top + boxHeight),
            Offset(left + boxWidth - cornerLength, top + boxHeight),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
        drawLine(
            accent,
            Offset(left + boxWidth, top + boxHeight),
            Offset(left + boxWidth, top + boxHeight - cornerLength),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
    }
}

private class CameraAnalyzer(
    private val scanner: com.google.mlkit.vision.barcode.BarcodeScanner,
    private val onBarcodeDetected: (String) -> Unit,
    private val scanBoxSizeProvider: () -> ScanBoxSizeOption
) : ImageAnalysis.Analyzer {
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image ?: run {
            imageProxy.close()
            return
        }
        val inputImage = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
        scanner.process(inputImage)
            .addOnSuccessListener { barcodes ->
                val isRotated = imageProxy.imageInfo.rotationDegrees % 180 != 0
                val frameWidth = if (isRotated) imageProxy.height.toFloat() else imageProxy.width.toFloat()
                val frameHeight = if (isRotated) imageProxy.width.toFloat() else imageProxy.height.toFloat()
                val activeWindow = scanBoxSizeProvider()
                val left = frameWidth * (1f - activeWindow.widthFraction) / 2f
                val right = frameWidth - left
                val top = frameHeight * (1f - activeWindow.heightFraction) / 2f
                val bottom = frameHeight - top

                barcodes.firstOrNull { barcode ->
                    val box = barcode.boundingBox ?: return@firstOrNull true
                    val centerX = box.centerX().toFloat()
                    val centerY = box.centerY().toFloat()
                    centerX in left..right && centerY in top..bottom
                }?.rawValue?.trim()?.let { value ->
                    if (value.isNotBlank()) onBarcodeDetected(value)
                }
            }
            .addOnCompleteListener { imageProxy.close() }
    }
}
