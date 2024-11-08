package com.ono.imagestreaming.ui.imagestream

import android.util.Log
import android.util.Size
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.launch
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

@Composable
fun ImageStream(viewModel: ImageStreamViewModel = hiltViewModel()) {
    val TAG = "ImageStream"
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    val coroutineScope = rememberCoroutineScope()
    val scheduler: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor()
    val isCapturing = remember { mutableStateOf(false) }

    val preview = Preview.Builder().build()
    val previewView = remember { PreviewView(context) }
    val imageAnalysisUseCase = remember {
        ImageAnalysis.Builder()
            .setTargetResolution(Size(800, 600))
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
    }

    imageAnalysisUseCase.setAnalyzer(Executors.newSingleThreadExecutor()) { imageProxy ->
        scheduler.schedule({
            coroutineScope.launch {
                viewModel.processImage(imageProxy)
            }
        }, 1, TimeUnit.SECONDS)
    }


    DisposableEffect(context) {
        val cameraProvider = cameraProviderFuture.get()
        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

        try {
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                lifecycleOwner, cameraSelector, preview, imageAnalysisUseCase
            )
            preview.surfaceProvider = previewView.surfaceProvider
        } catch (e: Exception) {
            Log.e("CameraX", "Failed to bind use cases", e)
        }

        onDispose {
            cameraProvider.unbindAll()
        }
    }

    // UI Components
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        AndroidView(
            factory = { previewView },
            modifier = Modifier.weight(1f)
        )

        Row(
            modifier = Modifier
                .wrapContentHeight()
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(
                onClick = {
                    isCapturing.value = !isCapturing.value
                }
            ) {
                Text(if (isCapturing.value) "Stop" else "Start")
            }

            Button(onClick = { context.togglePauseResume() }) {
                Text("Pause/Resume")
            }

            Button(onClick = { context.cancelUploadService() }) {
                Text("Cancel")
            }
        }
    }
}


