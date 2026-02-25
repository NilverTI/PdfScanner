package com.tantalean.scaneradd.ui.screens.camera

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.ImageCapture
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.tantalean.scaneradd.ui.components.ConfirmDialog
import com.tantalean.scaneradd.vm.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraScreen(
    vm: MainViewModel,
    onBack: () -> Unit,
    onFinishBatch: () -> Unit
) {
    val ctx = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    var hasPermission by remember { mutableStateOf(false) }
    var msg by remember { mutableStateOf<String?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasPermission = granted
        if (!granted) msg = "❌ Permiso de cámara denegado."
    }

    LaunchedEffect(Unit) {
        permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("📷 Escaneo") },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text("Atrás") }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            if (!hasPermission) {
                Column(Modifier.padding(16.dp)) {
                    Text("Necesitas permiso de cámara.")
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }) {
                        Text("Solicitar permiso")
                    }
                }
                return@Column
            }

            Box(modifier = Modifier.weight(1f)) {
                CameraPreview(
                    lifecycleOwner = lifecycleOwner,
                    onReady = { capture -> imageCapture = capture }
                )
            }

            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Hojas capturadas: ${vm.getCaptured().size}")

                Button(
                    onClick = {
                        val cap = imageCapture ?: return@Button
                        capturePhotoToCache(
                            context = ctx,
                            imageCapture = cap,
                            onSaved = { uri ->
                                vm.addCaptured(uri)
                                msg = "✅ Hoja capturada."
                            },
                            onError = { e -> msg = "❌ Error: ${e.message}" }
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("📸 Capturar hoja") }

                OutlinedButton(
                    onClick = {
                        if (vm.getCaptured().isEmpty()) msg = "❌ Captura al menos 1 hoja."
                        else onFinishBatch()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("✅ Finalizar lote (vista previa)") }
            }
        }
    }

    msg?.let {
        ConfirmDialog(title = "Info", text = it, onDismiss = { msg = null })
    }
}