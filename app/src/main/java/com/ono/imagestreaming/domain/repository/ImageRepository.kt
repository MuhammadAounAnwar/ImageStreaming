package com.ono.imagestreaming.domain.repository

import com.ono.imagestreaming.data.local.entity.ImageEntity

interface ImageRepository {
    suspend fun saveImageLocally(filePath: String)
    suspend fun uploadImage(filePath: String): Boolean
    suspend fun getPendingImages(): List<ImageEntity>
    suspend fun updateImageStatus(status: String, filePath: String):Boolean
}