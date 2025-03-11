package com.mustfaibra.roffu.components

import android.util.Log
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.google.common.util.concurrent.ListenableFuture
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import androidx.camera.view.PreviewView

@Composable
fun CameraPreview(
    modifier: Modifier = Modifier,
    imageAnalyzer: ImageAnalysis.Analyzer? = null
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture: ListenableFuture<ProcessCameraProvider> =
        remember { ProcessCameraProvider.getInstance(context) }
    val executor: ExecutorService = remember { Executors.newSingleThreadExecutor() }

    AndroidView(
        factory = { ctx ->
            val previewView = PreviewView(ctx).apply {
                scaleType = PreviewView.ScaleType.FILL_CENTER
            }

            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider) // ✅ Sửa lỗi ở đây
                }

                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                val imageAnalysis = imageAnalyzer?.let {
                    ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()
                        .apply {
                            setAnalyzer(executor, it)
                        }
                }

                try {
                    cameraProvider.unbindAll()
                    val useCases = listOfNotNull(preview, imageAnalysis)
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner as LifecycleOwner,
                        cameraSelector,
                        *useCases.toTypedArray()
                    )
                } catch (e: Exception) {
                    Log.e("CameraPreview", "Use case binding failed", e)
                }
            }, ContextCompat.getMainExecutor(ctx))

            previewView
        },
        modifier = modifier.fillMaxSize()
    )
}
