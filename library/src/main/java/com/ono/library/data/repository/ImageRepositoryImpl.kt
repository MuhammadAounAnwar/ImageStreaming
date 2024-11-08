package com.ono.library.data.repository

import android.util.Log
import com.ono.imagestreaming.data.local.dao.ImageDao
import com.ono.imagestreaming.data.local.entity.ImageEntity
import com.ono.imagestreaming.data.remote.ApiService
import com.ono.imagestreaming.domain.repository.ImageRepository
import kotlinx.coroutines.flow.Flow
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
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
        return try {
            val response = apiService.uploadImage(filePath.toMultiPart())

            if (response.isSuccessful) {
                Log.d(TAG, "uploadImage: Successfully uploaded image${response.body()?.link}")
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
        return imageDao.updateImageStatus(status, filePath) > 0
    }

}

fun String.toMultiPart(): MultipartBody.Part {
    val file = File(this)  // Directly use the string as a file path

    if (!file.exists() || !file.isFile) {
        throw IllegalArgumentException("Invalid file path: $this")
    }

    val extension = file.extension.takeIf { it.isNotEmpty() } ?: "jpg"  // Default extension
//    val requestBody = file.asRequestBody("multipart/form-data".toMediaType())
    val requestBody = file.readBytes().toRequestBody("*/*".toMediaTypeOrNull())

    // Use current time to create a unique file name for the multipart part
    return MultipartBody.Part.createFormData(
        "images",
        "${System.currentTimeMillis()}.$extension",
        requestBody
    )
}

fun File.asRequestBody(mediaType: MediaType): RequestBody {
    return this.inputStream().use { inputStream ->
        inputStream.readBytes().toRequestBody(mediaType)
    }
}