package com.ono.imagestreaming.data.repository

import android.util.Log
import com.ono.imagestreaming.data.local.dao.ImageDao
import com.ono.imagestreaming.data.local.entity.ImageEntity
import com.ono.imagestreaming.data.remote.ApiService
import com.ono.imagestreaming.domain.repository.ImageRepository
import com.ono.imagestreaming.util.createFileFromPath
import kotlinx.coroutines.flow.Flow
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject

class ImageRepositoryImpl @Inject constructor(
    private val imageDao: ImageDao,
    private val apiService: ApiService
) : ImageRepository {

    private val TAG = "ImageRepositoryImpl"

    override suspend fun saveImageLocally(filePath: String) {
        val image = ImageEntity(filePath = filePath, status = "pending")
        imageDao.insertImage(image)
    }

    override suspend fun uploadImage(filePath: String): Boolean {
        val file = filePath.createFileFromPath()
            ?: return false // Return false if the file is invalid or not found

        // Get the file extension
        val extension = file.extension
        if (extension.isEmpty()) {
            Log.e(TAG, "Invalid file extension")
            return false
        }

        // Prepare the file content for the request
        val requestBody = file.readBytes().toRequestBody("*/*".toMediaTypeOrNull())

        // Create a multipart part with file data
        val fileToUpload = MultipartBody.Part.createFormData(
            "images",
            "${System.currentTimeMillis()}.$extension",
            requestBody
        )

        return try {
            // Call the API to upload the image
            val response = apiService.uploadImage(fileToUpload)

            if (response.isSuccessful) {
                Log.d(TAG, "uploadImage: Successfully uploaded image")
                updateImageStatus("uploaded", filePath)
                true // Return true if upload is successful
            } else {
                Log.e(TAG, "Image upload failed with response code: ${response.code()}")
                false // Return false if response is not successful
            }
        } catch (e: Exception) {
            // Log any errors during the upload process
            Log.e(TAG, "Upload image failed: ${e.message}", e)
            false // Return false if an exception occurs
        }
    }

    override suspend fun getImagesByStatus(imageStatus: String): List<ImageEntity> {
        return imageDao.getImagesByStatus(imageStatus)
    }


    override suspend fun getPendingImages(): Flow<List<ImageEntity>> {
        return imageDao.getPendingImages()
    }

    override suspend fun updateImageStatus(status: String, filePath: String): Boolean {
        return imageDao.updateImageStatus(status, filePath) > 0
    }


    sealed class UploadState {
        object Idle : UploadState()
        object Uploading : UploadState()
        data class Success(val message: String = "Upload Successful") : UploadState()
        data class Error(val message: String) : UploadState()
    }
}