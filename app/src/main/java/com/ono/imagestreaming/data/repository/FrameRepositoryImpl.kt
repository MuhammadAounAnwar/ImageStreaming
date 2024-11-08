package com.ono.imagestreaming.data.repository

import android.util.Log
import com.ono.imagestreaming.data.local.dao.FrameDao
import com.ono.imagestreaming.data.local.dao.ImageDao
import com.ono.imagestreaming.data.local.entity.FrameEntity
import com.ono.imagestreaming.data.local.entity.ImageEntity
import com.ono.imagestreaming.data.local.entity.toDomainModel
import com.ono.imagestreaming.data.remote.ApiService
import com.ono.imagestreaming.domain.model.FrameModel
import com.ono.imagestreaming.domain.repository.FrameRepository
import com.ono.imagestreaming.util.createFileFromPath
import kotlinx.coroutines.flow.Flow
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject

class FrameRepositoryImpl @Inject constructor(
    private val frameDao: FrameDao,
    private val apiService: ApiService
) : FrameRepository {

    private val TAG = "FrameRepositoryImpl"

    override suspend fun saveFrameLocally(frame: ByteArray) {
        val frame = FrameEntity(frameData = frame, status = "pending")
        frameDao.insertFrame(frame)
    }

    override suspend fun uploadFrame(frame: FrameModel): Boolean {

        // Prepare the file content for the request
        val requestBody = createRequestBody(frame.frameData)

        // Create a multipart part with file data
        val fileToUpload = MultipartBody.Part.createFormData(
            "images",
            "${System.currentTimeMillis()}.jpg",
            requestBody
        )

        return try {
            // Call the API to upload the image
            val response = apiService.uploadImage(fileToUpload)

            if (response.isSuccessful) {
                Log.d(TAG, "uploadImage: Successfully uploaded image")
                updateFrameStatus("uploaded", frame.id)
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

    override suspend fun getFramesByStatus(frameStatus: String): List<FrameModel> {
        val frames = frameDao.getFramesByStatus(frameStatus)
        return frames.toDomainModel()
    }

    override suspend fun getPendingFrames(): Flow<List<FrameEntity>> {
        return frameDao.getPendingFrames()
    }

    override suspend fun updateFrameStatus(status: String, id: Int): Boolean {
        Log.d(TAG, "updateImageStatus: $status $id")
        return frameDao.updateFrameStatus(status, id) > 0
    }

    fun createRequestBody(byteArray: ByteArray): RequestBody {
        return byteArray.toRequestBody("image/jpeg".toMediaTypeOrNull(), 0, byteArray.size)
    }


    sealed class UploadState {
        object Idle : UploadState()
        object Uploading : UploadState()
        data class Success(val message: String = "Upload Successful") : UploadState()
        data class Error(val message: String) : UploadState()
    }
}