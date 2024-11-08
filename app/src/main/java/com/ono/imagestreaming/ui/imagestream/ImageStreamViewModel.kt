package com.ono.imagestreaming.ui.imagestream

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.YuvImage
import android.util.Log
import androidx.camera.core.ImageProxy
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ono.imagestreaming.data.local.entity.toDomainModel
import com.ono.imagestreaming.domain.model.FrameModel
import com.ono.imagestreaming.domain.repository.FrameRepository
import com.ono.imagestreaming.ui.service.FrameUploadService
import com.ono.imagestreaming.ui.service.UploadService
import com.ono.imagestreaming.util.bitmapToByteArray
import com.ono.imagestreaming.util.compressBitmap
import com.ono.imagestreaming.util.convertImageToBitmap
import com.ono.imagestreaming.util.isInternetAvailable
import com.ono.imagestreaming.util.resizeBitmap
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import javax.inject.Inject

@HiltViewModel
class ImageStreamViewModel @Inject constructor(
    private val repository: FrameRepository,
    @ApplicationContext private val context: Context
) :
    ViewModel() {

    private val TAG = "ImageStreamViewModel"

    private val _uploadState = MutableStateFlow("")
    val uploadState: StateFlow<String> get() = _uploadState


    init {
        observePendingFrames()
    }

    suspend fun processImage(imageProxy: ImageProxy) = withContext(Dispatchers.Default) {
        imageProxy.use { imageProxy ->
            val bitmap = imageProxy.convertImageToBitmap()
            val resizedBitmap = bitmap.resizeBitmap(600, 400)
            val byteArray = resizedBitmap.bitmapToByteArray()
            val compressedByteArray = resizedBitmap.compressBitmap()

            Log.d(TAG, "processImage: ${byteArray.size}")
            Log.d(TAG, "processImage: ${compressedByteArray.size}")

            saveFrameLocally(compressedByteArray)
        }
    }


    private fun saveFrameLocally(byteArray: ByteArray) {
        viewModelScope.launch {
            try {
                repository.saveFrameLocally(byteArray)
            } catch (e: Exception) {
                Log.e(TAG, "Error in captureAndUploadImage: ${e.message}")
                _uploadState.value = "An error occurred: ${e.message}"
            }
        }
    }

    private fun observePendingFrames() {
        viewModelScope.launch {
            repository.getPendingFrames().collect { pendingFrames ->
                Log.d(TAG, "observePendingImages: ${pendingFrames.size}")
                if (pendingFrames.isNotEmpty()) {
                    context.isInternetAvailable {
                        startUploadService(context, pendingFrames.toDomainModel())
                    }
                }
            }
        }
    }


    private fun startUploadService(context: Context, imagePaths: List<FrameModel>) {
        FrameUploadService.startService(context, imagePaths)
    }

    fun togglePauseResume(context: Context) {
        FrameUploadService.pauseOrResumeService(context)
    }

    fun cancelUploadService(context: Context) {
        FrameUploadService.cancelService(context)
    }
}
