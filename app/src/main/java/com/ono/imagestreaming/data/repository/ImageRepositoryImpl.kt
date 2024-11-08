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

        val extension = file.extension
        if (extension.isEmpty()) {
            Log.e(TAG, "Invalid file extension")
            return false
        }

        val requestBody = file.readBytes().toRequestBody("*/*".toMediaTypeOrNull())

        val fileToUpload = MultipartBody.Part.createFormData(
            "images",
            "${System.currentTimeMillis()}.$extension",
            requestBody
        )

        return try {
            val response = apiService.uploadImage(fileToUpload)

            if (response.isSuccessful) {
                Log.d(TAG, "uploadImage: Successfully uploaded image")
                updateImageStatus("uploaded", filePath)
                true
            } else {
                Log.e(TAG, "Image upload failed with response code: ${response.code()}")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Upload image failed: ${e.message}", e)
            false
        }
    }

    override suspend fun getImagesByStatus(imageStatus: String): List<ImageEntity> {
        return imageDao.getImagesByStatus(imageStatus)
    }


    override suspend fun getPendingImages(): Flow<List<ImageEntity>> {
        return imageDao.getPendingImages()
    }

    override suspend fun updateImageStatus(status: String, filePath: String): Boolean {
        Log.d(TAG, "updateImageStatus: $status $filePath")
        return imageDao.updateImageStatus(status, filePath) > 0
    }
}