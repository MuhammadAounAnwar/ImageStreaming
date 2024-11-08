package com.ono.library.domain.repository

import com.ono.imagestreaming.data.local.entity.ImageEntity
import kotlinx.coroutines.flow.Flow

interface ImageRepository {
    suspend fun saveImageLocally(filePath: String)
    suspend fun uploadImage(filePath: String): Boolean
    suspend fun getImagesByStatus(imageStatus: String): List<ImageEntity>
    suspend fun updateImageStatus(status: String, filePath: String):Boolean
    suspend fun getPendingImages(): Flow<List<ImageEntity>>
}