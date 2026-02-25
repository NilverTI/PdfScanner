package com.tantalean.scaneradd.storage

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.provider.MediaStore

object PdfStorage {

    fun savePdfToDownloads(context: Context, fileName: String, pdfBytes: ByteArray): Uri? {
        val resolver = context.contentResolver

        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, "$fileName.pdf")
            put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
            put(MediaStore.MediaColumns.RELATIVE_PATH, "Download/Scans")
        }

        val collection = MediaStore.Downloads.EXTERNAL_CONTENT_URI
        val uri = resolver.insert(collection, values) ?: return null

        resolver.openOutputStream(uri)?.use { output ->
            output.write(pdfBytes)
        } ?: return null

        return uri
    }
}