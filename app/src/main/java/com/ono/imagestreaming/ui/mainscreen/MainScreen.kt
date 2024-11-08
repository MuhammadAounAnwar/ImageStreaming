package com.ono.imagestreaming.ui.mainscreen

import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import java.io.File
import java.util.concurrent.Executors

@Composable
fun MainScreen(modifier: Modifier, viewModel: MainViewModel = hiltViewModel()) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }

    val preview = Preview.Builder().build()
    val imageCapture = remember { ImageCapture.Builder().build() }
    val outputDirectory = context.filesDir

    val previewView = remember { PreviewView(context) }

    val captureImage = {
        val file = File(outputDirectory, "${System.currentTimeMillis()}.jpg")
        val outputOptions = ImageCapture.OutputFileOptions.Builder(file).build()
        imageCapture.takePicture(outputOptions,
            Executors.newSingleThreadExecutor(),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    viewModel.captureAndUploadImage(file.absolutePath)
                }

                override fun onError(exception: ImageCaptureException) {
                    Log.e("CameraX", "Error capturing image", exception)
                }
            })
    }

    DisposableEffect(context) {
        val cameraProvider = cameraProviderFuture.get()
        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

        try {
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                lifecycleOwner, cameraSelector, preview, imageCapture
            )

            preview.surfaceProvider = previewView.surfaceProvider
        } catch (e: Exception) {
            Log.e("CameraX", "Failed to bind use cases", e)
        }

        onDispose {
            cameraProvider.unbindAll()
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween
    ) {
        AndroidView(
            factory = { previewView }, modifier = Modifier.weight(1f)
        )

        Row(
            modifier = Modifier
                .wrapContentHeight()
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(onClick = captureImage) {
                Text("Capture")
            }

            Button(onClick = { viewModel.togglePauseResume(context) }) {
                Text("Pause/Resume")
            }

            Button(onClick = { viewModel.cancelUploadService(context) }) {
                Text("Cancel")
            }
        }
    }
}
