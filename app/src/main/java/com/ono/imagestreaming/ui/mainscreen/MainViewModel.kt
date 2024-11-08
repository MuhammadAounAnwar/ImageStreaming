package com.ono.imagestreaming.ui.mainscreen

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ono.imagestreaming.domain.repository.ImageRepository
import com.ono.imagestreaming.ui.service.UploadService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: ImageRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val TAG = MainViewModel::class.java.name

    private val _uploadState = MutableStateFlow("")
    val uploadState: StateFlow<String> get() = _uploadState

    init {
        getImagesStats()
        observePendingImages()
    }

    fun captureAndUploadImage(filePath: String) {
        viewModelScope.launch {
            try {
                repository.saveImageLocally(filePath)
            } catch (e: Exception) {
                Log.e(TAG, "Error in captureAndUploadImage: ${e.message}")
                _uploadState.value = "An error occurred: ${e.message}"
            }
        }
    }

    private fun observePendingImages() {
        viewModelScope.launch {
            repository.getPendingImages().collect { pendingImages ->
                Log.d(TAG, "observePendingImages: ${pendingImages.size}")
                if (pendingImages.isNotEmpty()) {
                    startUploadService(context, pendingImages.map { it.filePath })
                }
            }
        }
    }

    private fun getImagesStats() {
        viewModelScope.launch {
            val pendingImages = repository.getImagesByStatus("pending")
            Log.d(TAG, "getPendingImages: ${pendingImages.size}")
            val uploadedImages = repository.getImagesByStatus("uploaded")
            Log.d(TAG, "getUploadedImages: ${uploadedImages.size}")
        }
    }

    fun startUploadService(context: Context, imagePaths: List<String>) {
        UploadService.startService(context, imagePaths)
    }

    fun togglePauseResume(context: Context) {
        UploadService.pauseOrResumeService(context)
    }

    fun cancelUploadService(context: Context) {
        UploadService.cancelService(context)
    }

}
