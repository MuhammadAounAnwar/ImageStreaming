package com.ono.imagestreaming.ui.mainscreen

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ono.imagestreaming.domain.repository.ImageRepository
import com.ono.imagestreaming.domain.usecase.UploadImageUseCase
import com.ono.imagestreaming.util.isInternetAvailable
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: ImageRepository,
    private val uploadImageUseCase: UploadImageUseCase,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val TAG = MainViewModel::class.java.name

    private val _uploadState = MutableStateFlow("")
    val uploadState: StateFlow<String> get() = _uploadState


    init {
        getPendingImages()
    }

    fun captureAndUploadImage(filePath: String) {
        viewModelScope.launch {
            try {
                // Save image locally
                repository.saveImageLocally(filePath)
                Log.d(TAG, "Image saved locally: $filePath")

                // Check internet availability
                if (context.isInternetAvailable()) {
                    uploadImage(filePath)
                } else {
                    _uploadState.value = "No internet connection. Upload failed."
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error in captureAndUploadImage: ${e.message}")
                _uploadState.value = "An error occurred: ${e.message}"
            }
        }
    }

    private suspend fun uploadImage(filePath: String) {
        val uploadResult = runCatching {
            uploadImageUseCase(filePath)
        }

        // Update the state based on the result
        _uploadState.value = uploadResult.fold(
            onSuccess = { success ->
                if (success) "Upload Successful" else "Upload Failed"
            },
            onFailure = { exception ->
                "Upload Failed: ${exception.message}"
            }
        )
    }

    private fun getPendingImages() {
        viewModelScope.launch {
            val pendingImages = repository.getPendingImages()
            Log.d(TAG, "getPendingImages: $pendingImages")
        }
    }

}
