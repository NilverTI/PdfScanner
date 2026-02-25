package com.tantalean.scaneradd.pdf

import android.content.ContentResolver
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.media.ExifInterface
import android.net.Uri
import java.io.ByteArrayOutputStream
import kotlin.math.min

object PdfGenerator {

    fun createPdfBytes(
        resolver: ContentResolver,
        pages: List<Uri>,
        pageWidth: Int = 595,   // A4 portrait
        pageHeight: Int = 842
    ): ByteArray {

        val doc = PdfDocument()
        val margin = 24

        pages.forEachIndexed { index, uri ->

            val bitmapRaw = resolver.openInputStream(uri)?.use { input ->
                BitmapFactory.decodeStream(input)
            } ?: return@forEachIndexed

            // ✅ Corregir orientación EXIF
            val rotation = getExifRotation(resolver, uri)
            val bitmap = if (rotation != 0) rotateBitmap(bitmapRaw, rotation) else bitmapRaw

            if (bitmap !== bitmapRaw) bitmapRaw.recycle()

            val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, index + 1).create()
            val page = doc.startPage(pageInfo)
            val canvas = page.canvas

            canvas.drawColor(Color.WHITE)

            val contentW = pageWidth - margin * 2
            val contentH = pageHeight - margin * 2

            val scale = min(
                contentW.toFloat() / bitmap.width.toFloat(),
                contentH.toFloat() / bitmap.height.toFloat()
            )

            val drawW = (bitmap.width * scale).toInt()
            val drawH = (bitmap.height * scale).toInt()

            val left = margin + (contentW - drawW) / 2
            val top = margin + (contentH - drawH) / 2

            val src = Rect(0, 0, bitmap.width, bitmap.height)
            val dst = Rect(left, top, left + drawW, top + drawH)

            val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                isFilterBitmap = true
                isDither = true
            }

            canvas.drawBitmap(bitmap, src, dst, paint)

            doc.finishPage(page)
            bitmap.recycle()
        }

        val out = ByteArrayOutputStream()
        doc.writeTo(out)
        doc.close()
        return out.toByteArray()
    }

    private fun getExifRotation(resolver: ContentResolver, uri: Uri): Int {
        return try {
            resolver.openInputStream(uri)?.use { input ->
                val exif = ExifInterface(input)
                when (exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> 90
                    ExifInterface.ORIENTATION_ROTATE_180 -> 180
                    ExifInterface.ORIENTATION_ROTATE_270 -> 270
                    else -> 0
                }
            } ?: 0
        } catch (_: Exception) {
            0
        }
    }

    private fun rotateBitmap(src: Bitmap, degrees: Int): Bitmap {
        val matrix = Matrix().apply { postRotate(degrees.toFloat()) }
        return Bitmap.createBitmap(src, 0, 0, src.width, src.height, matrix, true)
    }
}