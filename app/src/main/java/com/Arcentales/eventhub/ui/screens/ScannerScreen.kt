package com.Arcentales.eventhub.ui.screens

import android.Manifest
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.Arcentales.eventhub.data.models.ScanResult
import com.Arcentales.eventhub.ui.theme.*
import com.Arcentales.eventhub.viewmodel.ScanState
import com.Arcentales.eventhub.viewmodel.ScannerViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ScannerScreen(
    onClose: () -> Unit,
    viewModel: ScannerViewModel = viewModel()
) {
    val uiState     by viewModel.uiState.collectAsStateWithLifecycle()
    val cameraPermission = rememberPermissionState(Manifest.permission.CAMERA)

    LaunchedEffect(Unit) {
        if (!cameraPermission.status.isGranted) cameraPermission.launchPermissionRequest()
        else viewModel.startScanning()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0A0A))
    ) {
        // ── Vista de cámara ───────────────────────────────────────────────
        if (cameraPermission.status.isGranted) {
            CameraPreview(
                onQrCodeDetected = viewModel::onQrCodeDetected,
                modifier = Modifier.fillMaxSize()
            )
        }

        // ── Overlay de visor ──────────────────────────────────────────────
        ScannerOverlay(
            isValid = uiState.scanState is ScanState.Valid,
            modifier = Modifier.fillMaxSize()
        )

        // ── Header ────────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onClose,
                modifier = Modifier
                    .size(40.dp)
                    .background(Color.White.copy(alpha = 0.15f), CircleShape)
            ) {
                Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Color.White)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("EventHub", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("Staff Ticket Scanner", color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
            }
            IconButton(
                onClick = viewModel::toggleFlash,
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        if (uiState.flashEnabled) Color(0x4DFDE68A) else Color.White.copy(alpha = 0.15f),
                        CircleShape
                    )
            ) {
                Icon(
                    Icons.Default.FlashOn,
                    contentDescription = "Flash",
                    tint = if (uiState.flashEnabled) Color(0xFFFDE68A) else Color.White
                )
            }
        }

        // ── Contador de escaneos ──────────────────────────────────────────
        if (uiState.scanCount > 0) {
            Surface(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 100.dp),
                shape = RoundedCornerShape(20.dp),
                color = Color.White.copy(alpha = 0.15f)
            ) {
                Text(
                    "Escaneados: ${uiState.scanCount}",
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium
                )
            }
        }

        // ── Panel de resultado ────────────────────────────────────────────
        when (val state = uiState.scanState) {
            is ScanState.Valid -> {
                ValidResultPanel(
                    result = state.result,
                    onNextScan = viewModel::resetToScanning,
                    onClose = onClose,
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }
            is ScanState.Invalid -> {
                InvalidResultPanel(
                    message = state.message,
                    onNextScan = viewModel::resetToScanning,
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }
            is ScanState.Processing -> {
                Box(Modifier.align(Alignment.BottomCenter).padding(32.dp)) {
                    CircularProgressIndicator(color = Blue500)
                }
            }
            else -> {}
        }

        // ── Sin permiso ───────────────────────────────────────────────────
        if (!cameraPermission.status.isGranted) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.Default.CameraAlt, contentDescription = null, tint = Color.White, modifier = Modifier.size(56.dp))
                Spacer(Modifier.height(16.dp))
                Text("Se necesita permiso de cámara", color = Color.White, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(12.dp))
                Button(onClick = { cameraPermission.launchPermissionRequest() }) {
                    Text("Conceder permiso")
                }
            }
        }
    }
}

// ── CameraX Preview ───────────────────────────────────────────────────────

@Composable
private fun CameraPreview(
    onQrCodeDetected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context       = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraExecutor: ExecutorService = remember { Executors.newSingleThreadExecutor() }

    val options = remember {
        BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
            .build()
    }
    val barcodeScanner = remember { BarcodeScanning.getClient(options) }

    AndroidView(
        modifier = modifier,
        factory  = { ctx ->
            val previewView = PreviewView(ctx)
            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build()
                    .also { it.setSurfaceProvider(previewView.surfaceProvider) }

                val imageAnalyzer = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .also { analysis ->
                        analysis.setAnalyzer(cameraExecutor) { imageProxy ->
                            @androidx.camera.core.ExperimentalGetImage
                            val mediaImage = imageProxy.image
                            if (mediaImage != null) {
                                val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                                barcodeScanner.process(image)
                                    .addOnSuccessListener { barcodes ->
                                        barcodes.firstOrNull()?.rawValue?.let { onQrCodeDetected(it) }
                                    }
                                    .addOnCompleteListener { imageProxy.close() }
                            } else {
                                imageProxy.close()
                            }
                        }
                    }

                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        preview,
                        imageAnalyzer
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }, ContextCompat.getMainExecutor(ctx))

            previewView
        }
    )

    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
            barcodeScanner.close()
        }
    }
}

// ── Overlay del visor ─────────────────────────────────────────────────────

@Composable
private fun ScannerOverlay(isValid: Boolean, modifier: Modifier = Modifier) {
    Box(modifier) {
        // Visor central — se dibuja con Canvas en una implementación completa
        // Aquí se usa un Surface como placeholder visual
        Surface(
            modifier = Modifier
                .size(240.dp)
                .align(Alignment.Center),
            shape = RoundedCornerShape(8.dp),
            color = Color.Transparent,
            border = androidx.compose.foundation.BorderStroke(
                width = 2.dp,
                color = if (isValid) Green500 else Blue500
            )
        ) {}
    }
}

// ── Panel Ticket Válido ───────────────────────────────────────────────────

@Composable
private fun ValidResultPanel(
    result: ScanResult,
    onNextScan: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        color    = Color(0xFF111827),
        border   = androidx.compose.foundation.BorderStroke(2.dp, Green500)
    ) {
        Column(Modifier.padding(24.dp).navigationBarsPadding()) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Green500, modifier = Modifier.size(24.dp))
                Column {
                    Text("Ticket Válido", color = Green500, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text("Acceso concedido", color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
                }
            }
            Spacer(Modifier.height(20.dp))
            listOf("Evento" to result.eventTitle, "Asistente" to result.attendeeName).forEach { (label, value) ->
                Row(Modifier.fillMaxWidth().padding(vertical = 10.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(label, color = Color.White.copy(alpha = 0.4f), fontSize = 13.sp)
                    Text(value, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
                HorizontalDivider(color = Color.White.copy(alpha = 0.06f))
            }
            Row(Modifier.fillMaxWidth().padding(vertical = 10.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Tipo", color = Color.White.copy(alpha = 0.4f), fontSize = 13.sp)
                Surface(shape = RoundedCornerShape(20.dp), color = Blue500.copy(alpha = 0.2f)) {
                    Text(result.ticketType, modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp), color = Color(0xFF60A5FA), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.height(20.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = onNextScan, modifier = Modifier.weight(1f), shape = RoundedCornerShape(14.dp), colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Navy900)) {
                    Text("Siguiente Escaneo", fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 4.dp))
                }
                Button(onClick = onClose, modifier = Modifier.weight(1f), shape = RoundedCornerShape(14.dp), colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.1f), contentColor = Color.White.copy(alpha = 0.7f))) {
                    Text("Cerrar", fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(vertical = 4.dp))
                }
            }
        }
    }
}

// ── Panel Ticket Inválido ─────────────────────────────────────────────────

@Composable
private fun InvalidResultPanel(
    message: String,
    onNextScan: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        color    = Color(0xFF111827),
        border   = androidx.compose.foundation.BorderStroke(2.dp, Red500)
    ) {
        Column(Modifier.padding(24.dp).navigationBarsPadding()) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Icon(Icons.Default.Cancel, contentDescription = null, tint = Red500, modifier = Modifier.size(24.dp))
                Column {
                    Text("Ticket Inválido", color = Red500, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text(message, color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
                }
            }
            Spacer(Modifier.height(20.dp))
            Button(onClick = onNextScan, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), colors = ButtonDefaults.buttonColors(containerColor = Red500)) {
                Text("Intentar de nuevo", fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 4.dp))
            }
        }
    }
}
