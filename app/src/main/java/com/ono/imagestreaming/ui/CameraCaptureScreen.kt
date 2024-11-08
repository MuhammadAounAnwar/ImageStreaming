package com.ono.imagestreaming.ui

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.YuvImage
import android.os.Handler
import android.os.Looper
import android.util.Size
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import java.io.ByteArrayOutputStream
import java.util.concurrent.Executors


@Composable
fun CameraCaptureScreen() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val captureHandler = remember { Handler(Looper.getMainLooper()) }
    val captureInterval = 1000L // 1 second interval
    var isCapturing = remember { mutableStateOf(false) }

    val previewView = remember { PreviewView(context) }
    val cameraProviderState = remember { mutableStateOf<ProcessCameraProvider?>(null) }
    val imageAnalysis = remember { mutableStateOf<ImageAnalysis?>(null) }
    val preview = Preview.Builder().build()
    // Initialize CameraX
    LaunchedEffect(true) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            cameraProviderState.value = cameraProvider

            // Bind the camera use cases
            bindCameraUseCases(
                cameraProvider,
                lifecycleOwner,
                previewView,
                imageAnalysis,
                preview
            )
        }, ContextCompat.getMainExecutor(context))
    }

    // Start capturing frames every 1 second
    Column(modifier = Modifier.fillMaxSize()) {
        Button(onClick = {
            isCapturing.value = true
            startCapturing(captureHandler, captureInterval) {
                isCapturing.value = true
            }
        }) {
            Text(text = "Start Capturing")
        }

        Button(onClick = {
            isCapturing.value = false
            stopCapturing(captureHandler)
        }) {
            Text(text = "Stop Capturing")
        }

        AndroidView(
            factory = { context ->
                PreviewView(context).apply {
                    // Set the surface provider directly during initialization
//                    this.surfaceProvider = previewView.surfaceProvider
                }
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}

// Refactor bindCameraUseCases to a regular function
fun bindCameraUseCases(
    cameraProvider: ProcessCameraProvider,
    lifecycleOwner: LifecycleOwner,
    previewView: PreviewView,
    imageAnalysis: MutableState<ImageAnalysis?>,
    preview: Preview
) {

    preview.surfaceProvider = previewView.surfaceProvider

    val imageAnalysisUseCase = ImageAnalysis.Builder()
        .setTargetResolution(Size(1920, 1080))
        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
        .build()

    imageAnalysis.value = imageAnalysisUseCase

    imageAnalysisUseCase.setAnalyzer(Executors.newSingleThreadExecutor()) { imageProxy ->
        // Process the image (implement your logic here)
        processImage(imageProxy)
    }

    // Bind use cases to the lifecycle
    cameraProvider.bindToLifecycle(
        lifecycleOwner,
        CameraSelector.DEFAULT_BACK_CAMERA,
        preview,
        imageAnalysisUseCase
    )
}


private fun processImage(imageProxy: ImageProxy) {
    // Convert image to Bitmap or ByteArray
    val byteArray = convertImageToByteArray(imageProxy)

    // Optionally resize the image before uploading
    val resizedBitmap = resizeBitmap(convertImageToBitmap(imageProxy), 800, 600)

    // Now, upload the resized image or byte array
    uploadImage(resizedBitmap)

    // Close the image proxy to release resources
    imageProxy.close()
}

private fun convertImageToByteArray(imageProxy: ImageProxy): ByteArray {
    val buffer = imageProxy.planes[0].buffer
    val bytes = ByteArray(buffer.remaining())
    buffer.get(bytes)
    return bytes
}

private fun convertImageToBitmap(imageProxy: ImageProxy): Bitmap {
    val yPlane = imageProxy.planes[0]
    val uPlane = imageProxy.planes[1]
    val vPlane = imageProxy.planes[2]

    val ySize = yPlane.buffer.remaining()
    val uvSize = uPlane.buffer.remaining() + vPlane.buffer.remaining()

    val nv21ByteArray = ByteArray(ySize + uvSize)
    yPlane.buffer.get(nv21ByteArray, 0, ySize)
    uPlane.buffer.get(nv21ByteArray, ySize, uPlane.buffer.remaining())
    vPlane.buffer.get(nv21ByteArray, ySize + uPlane.buffer.remaining(), vPlane.buffer.remaining())

    val yuvImage =
        YuvImage(nv21ByteArray, ImageFormat.NV21, imageProxy.width, imageProxy.height, null)
    val outputStream = ByteArrayOutputStream()
    yuvImage.compressToJpeg(Rect(0, 0, imageProxy.width, imageProxy.height), 100, outputStream)

    val byteArray = outputStream.toByteArray()
    return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
}

private fun resizeBitmap(bitmap: Bitmap, width: Int, height: Int): Bitmap {
    return Bitmap.createScaledBitmap(bitmap, width, height, true)
}

private fun uploadImage(bitmap: Bitmap) {
    // Logic to upload image (e.g., send it to a server)
    // You can convert the bitmap to a byte array before uploading if necessary.
}

// Start capturing frames every 1 second
private fun startCapturing(
    captureHandler: Handler,
    captureInterval: Long,
    captureAction: () -> Unit
) {
    captureHandler.postDelayed(object : Runnable {
        override fun run() {
            // Capture frame and process it
            captureAction()
            captureHandler.postDelayed(this, captureInterval)
        }
    }, captureInterval)
}

// Stop capturing frames
private fun stopCapturing(captureHandler: Handler) {
    captureHandler.removeCallbacksAndMessages(null)
}
