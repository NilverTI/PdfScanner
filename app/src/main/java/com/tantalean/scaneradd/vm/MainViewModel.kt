package com.tantalean.scaneradd.vm

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.tantalean.scaneradd.data.PointsStore
import com.tantalean.scaneradd.pdf.PdfGenerator
import com.tantalean.scaneradd.storage.DocumentRepository
import com.tantalean.scaneradd.storage.PdfStorage
import com.tantalean.scaneradd.storage.SavedPdf
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(app: Application) : AndroidViewModel(app) {

    private val pointsStore = PointsStore(app)
    private val docsRepo = DocumentRepository(app)

    // reglas
    val POINTS_PER_AD = 5
    val POINTS_PER_SCAN = 2

    // ✅ puntos iniciales para pruebas
    private val START_POINTS = 6

    val points: StateFlow<Int> =
        pointsStore.points.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    init {
        // ✅ Si inicia en 0, le metemos 6 una sola vez
        viewModelScope.launch {
            val current = pointsStore.points.first()
            if (current == 0) {
                pointsStore.addPoints(START_POINTS)
            }
        }
    }

    // lote de fotos
    private val captured = mutableListOf<Uri>()
    fun addCaptured(uri: Uri) { captured.add(uri) }
    fun clearCaptured() { captured.clear() }
    fun getCaptured(): List<Uri> = captured.toList()

    fun canScan(currentPoints: Int) = currentPoints >= POINTS_PER_SCAN

    fun rewardUser() {
        viewModelScope.launch { pointsStore.addPoints(POINTS_PER_AD) }
    }

    fun generateAndSavePdf(
        docName: String,
        onResult: (Uri?) -> Unit
    ) {
        viewModelScope.launch {
            val ok = pointsStore.consumePoints(POINTS_PER_SCAN)
            if (!ok) {
                onResult(null)
                return@launch
            }

            val ctx = getApplication<Application>()
            val bytes = PdfGenerator.createPdfBytes(ctx.contentResolver, getCaptured())
            val uri = PdfStorage.savePdfToDownloads(
                ctx,
                docName.ifBlank { "Scan_${System.currentTimeMillis()}" },
                bytes
            )
            onResult(uri)
        }
    }

    fun listPdfs(): List<SavedPdf> = docsRepo.listSavedPdfs()
    fun deletePdf(uri: Uri): Boolean = docsRepo.deletePdf(uri)
}