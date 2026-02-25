package com.tantalean.scaneradd.ui.screens.documents

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.tantalean.scaneradd.storage.SavedPdf
import com.tantalean.scaneradd.ui.components.ConfirmDialog
import com.tantalean.scaneradd.ui.components.PdfItemCard
import com.tantalean.scaneradd.vm.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentsScreen(
    vm: MainViewModel,
    onBack: () -> Unit
) {
    val ctx = LocalContext.current

    var docs by remember { mutableStateOf<List<SavedPdf>>(emptyList()) }
    var msg by remember { mutableStateOf<String?>(null) }

    // 🔥 nuevo: pdf a eliminar
    var pdfToDelete by remember { mutableStateOf<SavedPdf?>(null) }

    fun refresh() {
        docs = vm.listPdfs()
    }

    LaunchedEffect(Unit) { refresh() }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("📁 Mis PDFs") },
                navigationIcon = { TextButton(onClick = onBack) { Text("Atrás") } },
                actions = {
                    TextButton(onClick = { refresh(); msg = "🔄 Lista actualizada." }) {
                        Text("Actualizar")
                    }
                }
            )
        }
    ) { padding ->

        if (docs.isEmpty()) {
            Column(
                modifier = Modifier
                    .padding(padding)
                    .padding(16.dp)
            ) {
                Text("No hay PDFs guardados en Download/Scans.")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(docs) { item ->
                    PdfItemCard(
                        item = item,
                        onOpen = { openPdf(ctx, item.uri) },
                        onShare = { sharePdf(ctx, item) },
                        onDelete = {
                            pdfToDelete = item   // 🔥 solo guardamos referencia
                        }
                    )
                }
            }
        }
    }

    // 🔔 Confirmación de eliminación
    pdfToDelete?.let { item ->
        AlertDialog(
            onDismissRequest = { pdfToDelete = null },
            title = { Text("Eliminar PDF") },
            text = { Text("¿Seguro que deseas eliminar \"${item.name}\"? Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        val ok = vm.deletePdf(item.uri)
                        msg = if (ok) "✅ PDF eliminado." else "❌ No se pudo eliminar."
                        pdfToDelete = null
                        refresh()
                    }
                ) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { pdfToDelete = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    msg?.let { ConfirmDialog("Mensaje", it, onDismiss = { msg = null }) }
}

private fun sharePdf(context: Context, item: SavedPdf) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "application/pdf"
        putExtra(Intent.EXTRA_STREAM, item.uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, "Compartir PDF"))
}

private fun openPdf(context: Context, uri: Uri) {
    val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(uri, "application/pdf")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, "Abrir PDF"))
}