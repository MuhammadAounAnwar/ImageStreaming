package com.ono.imagestreaming.ui.imagestream

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.YuvImage
import android.util.Log
import androidx.camera.core.ImageProxy
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import javax.inject.Inject

@HiltViewModel
class ImageStreamViewModel @Inject constructor() : ViewModel() {

    private val TAG = "ImageStreamViewModel"

    suspend fun processImage(imageProxy: ImageProxy) = withContext(Dispatchers.Default) {
        imageProxy.use { imageProxy ->
            val bitmap = convertImageToBitmap(imageProxy)
            val resizedBitmap = resizeBitmap(bitmap, 600, 400)
            val byteArray = bitmapToByteArray(resizedBitmap)
            val compressedByteArray = compressBitmap(resizedBitmap)

            Log.d(TAG, "processImage: ${byteArray.size}")
            Log.d(TAG, "processImage: ${compressedByteArray.size}")
        }
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
        vPlane.buffer.get(
            nv21ByteArray,
            ySize + uPlane.buffer.remaining(),
            vPlane.buffer.remaining()
        )

        val yuvImage = YuvImage(
            nv21ByteArray, ImageFormat.NV21, imageProxy.width, imageProxy.height, null
        )
        val outputStream = ByteArrayOutputStream()
        yuvImage.compressToJpeg(Rect(0, 0, imageProxy.width, imageProxy.height), 100, outputStream)

        return BitmapFactory.decodeByteArray(outputStream.toByteArray(), 0, outputStream.size())
    }

    // Resizes a Bitmap
    private fun resizeBitmap(bitmap: Bitmap, width: Int, height: Int): Bitmap {
        return Bitmap.createScaledBitmap(bitmap, width, height, true)
    }

    // Converts a Bitmap to ByteArray
    private fun bitmapToByteArray(bitmap: Bitmap): ByteArray {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        return outputStream.toByteArray()
    }


    private fun compressBitmap(bitmap: Bitmap, quality: Int = 80): ByteArray {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, byteArrayOutputStream)
        return byteArrayOutputStream.toByteArray()
    }
}
