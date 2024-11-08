package com.ono.imagestreaming.data.repository

import android.util.Log
import com.ono.imagestreaming.data.local.dao.FrameDao
import com.ono.imagestreaming.data.local.entity.FrameEntity
import com.ono.imagestreaming.data.local.entity.toDomainModel
import com.ono.imagestreaming.data.remote.ApiService
import com.ono.imagestreaming.domain.model.FrameModel
import com.ono.imagestreaming.domain.repository.FrameRepository
import kotlinx.coroutines.flow.Flow
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

    override suspend fun uploadFrame(frameId: Int): Boolean {
        val frame = getFrameById(frameId)
        val requestBody = createRequestBody(frame.frameData)
        val fileToUpload = MultipartBody.Part.createFormData(
            "images",
            "${System.currentTimeMillis()}.jpg",
            requestBody
        )

        return try {
            val response = apiService.uploadImage(fileToUpload)

            if (response.isSuccessful) {
                Log.d(TAG, "uploadImage: Successfully uploaded image")
                updateFrameStatus("uploaded", frame.id)
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

    override suspend fun getFramesByStatus(frameStatus: String): List<FrameModel> {
        val frames = frameDao.getFramesByStatus(frameStatus)
        return frames.toDomainModel()
    }

    override suspend fun getPendingFrames(): Flow<List<FrameEntity>> {
        return frameDao.getPendingFrames()
    }

    override suspend fun getFrameById(id: Int): FrameModel {
        return frameDao.getFrameById(id).toDomainModel()
    }

    override suspend fun updateFrameStatus(status: String, id: Int): Boolean {
        Log.d(TAG, "updateImageStatus: $status $id")
        return frameDao.updateFrameStatus(status, id) > 0
    }

    private fun createRequestBody(byteArray: ByteArray): RequestBody {
        return byteArray.toRequestBody("image/jpeg".toMediaTypeOrNull(), 0, byteArray.size)
    }

}