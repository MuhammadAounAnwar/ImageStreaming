package com.ono.imagestreaming.ui.imagestream

import android.content.Context
import android.util.Log
import androidx.camera.core.ImageProxy
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ono.imagestreaming.data.local.entity.toDomainModelIds
import com.ono.imagestreaming.domain.model.FrameModel
import com.ono.imagestreaming.domain.repository.FrameRepository
import com.ono.imagestreaming.ui.service.FrameUploadService
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
        getFramesStats()
        observePendingFrames()
    }

    suspend fun processImage(imageProxy: ImageProxy) = withContext(Dispatchers.Default) {
        imageProxy.use { imageProxy ->
            val bitmap = imageProxy.convertImageToBitmap()
            val resizedBitmap = bitmap.resizeBitmap(600, 400)
            val byteArray = resizedBitmap.bitmapToByteArray()
            val compressedByteArray = resizedBitmap.compressBitmap()

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
                        context.startUploadService(pendingFrames.toDomainModelIds())
                    }
                }
            }
        }
    }

    private fun getFramesStats() {
        viewModelScope.launch {
            val pendingFrames = repository.getFramesByStatus("pending")
            Log.d(TAG, "Pending Frames: ${pendingFrames.size}")
            val uploadedFrames = repository.getFramesByStatus("uploaded")
            Log.d(TAG, "UploadedS Frames: ${uploadedFrames.size}")
        }
    }


}

fun Context.startUploadServiceWithFrames(imagePaths: List<FrameModel>) {
    FrameUploadService.startServiceWithFrames(this, imagePaths)
}

fun Context.startUploadService(imagePaths: List<Int>) {
    FrameUploadService.startService(this, imagePaths)
}

fun Context.togglePauseResume() {
    FrameUploadService.pauseOrResumeService(this)
}

fun Context.cancelUploadService() {
    FrameUploadService.cancelService(this)
}
