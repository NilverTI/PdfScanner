package com.tantalean.scaneradd.ui.screens.camera

import android.content.Context
import android.net.Uri
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.runtime.*
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import java.io.File

@Composable
fun CameraPreview(
    lifecycleOwner: LifecycleOwner,
    onReady: (ImageCapture) -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }

    AndroidView(factory = { ctx ->
        val previewView = PreviewView(ctx)

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            val capture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build()

            val selector = CameraSelector.DEFAULT_BACK_CAMERA

            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(lifecycleOwner, selector, preview, capture)

            onReady(capture)

        }, ContextCompat.getMainExecutor(context))

        previewView
    })
}

fun capturePhotoToCache(
    context: Context,
    imageCapture: ImageCapture,
    onSaved: (Uri) -> Unit,
    onError: (Throwable) -> Unit
) {
    val outputDir = File(context.cacheDir, "captures").apply { mkdirs() }
    val photoFile = File(outputDir, "scan_${System.currentTimeMillis()}.jpg")

    val options = ImageCapture.OutputFileOptions.Builder(photoFile).build()

    imageCapture.takePicture(
        options,
        ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                onSaved(Uri.fromFile(photoFile))
            }
            override fun onError(exception: ImageCaptureException) {
                onError(exception)
            }
        }
    )
}