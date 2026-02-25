package com.tantalean.scaneradd.ui.screens.preview

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.tantalean.scaneradd.ui.components.ConfirmDialog
import com.tantalean.scaneradd.vm.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreviewScreen(
    vm: MainViewModel,
    onBack: () -> Unit,
    onSaved: () -> Unit
) {
    val points by vm.points.collectAsState()
    val pages = vm.getCaptured()

    var name by remember { mutableStateOf("") }
    var msg by remember { mutableStateOf<String?>(null) }
    var saving by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("🧾 Vista previa") },
                navigationIcon = { TextButton(onClick = onBack) { Text("Atrás") } }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Páginas: ${pages.size}")
            Text("Puntos actuales: $points (Generar PDF consume ${vm.POINTS_PER_SCAN})")

            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(pages) { uri ->
                    Card {
                        AsyncImage(
                            model = uri,
                            contentDescription = "Hoja",
                            modifier = Modifier.size(130.dp)
                        )
                    }
                }
            }

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nombre del PDF") },
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    if (!vm.canScan(points)) {
                        msg = "❌ No tienes puntos suficientes."
                        return@Button
                    }
                    if (pages.isEmpty()) {
                        msg = "❌ No hay páginas."
                        return@Button
                    }

                    saving = true
                    vm.generateAndSavePdf(
                        docName = name,
                        onResult = { uri ->
                            saving = false
                            if (uri == null) msg = "❌ No se pudo guardar (sin puntos o error)."
                            else {
                                msg = "✅ PDF guardado correctamente."
                                vm.clearCaptured()
                                onSaved()
                            }
                        }
                    )
                },
                enabled = !saving,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (saving) "Guardando..." else "💾 Generar y guardar PDF")
            }

            OutlinedButton(
                onClick = {
                    vm.clearCaptured()
                    msg = "🗑 Lote limpiado."
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Limpiar lote") }
        }
    }

    msg?.let { ConfirmDialog("Mensaje", it, onDismiss = { msg = null }) }
}