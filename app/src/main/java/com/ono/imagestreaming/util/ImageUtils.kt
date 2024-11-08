package com.ono.imagestreaming.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.YuvImage
import androidx.camera.core.ImageProxy
import java.io.ByteArrayOutputStream

fun ImageProxy.convertImageToBitmap(): Bitmap {
    val yPlane = this.planes[0]
    val uPlane = this.planes[1]
    val vPlane = this.planes[2]

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
        nv21ByteArray, ImageFormat.NV21, this.width, this.height, null
    )
    val outputStream = ByteArrayOutputStream()
    yuvImage.compressToJpeg(Rect(0, 0, this.width, this.height), 100, outputStream)

    return BitmapFactory.decodeByteArray(outputStream.toByteArray(), 0, outputStream.size())
}

fun Bitmap.resizeBitmap(width: Int, height: Int): Bitmap {
    return Bitmap.createScaledBitmap(this, width, height, true)
}

fun Bitmap.bitmapToByteArray(): ByteArray {
    val outputStream = ByteArrayOutputStream()
    this.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
    return outputStream.toByteArray()
}

fun Bitmap.compressBitmap(quality: Int = 80): ByteArray {
    val byteArrayOutputStream = ByteArrayOutputStream()
    this.compress(Bitmap.CompressFormat.JPEG, quality, byteArrayOutputStream)
    return byteArrayOutputStream.toByteArray()
}