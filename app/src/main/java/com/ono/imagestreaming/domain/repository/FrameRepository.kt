package com.ono.imagestreaming.domain.repository

import com.ono.imagestreaming.data.local.entity.FrameEntity
import com.ono.imagestreaming.domain.model.FrameModel
import kotlinx.coroutines.flow.Flow

interface FrameRepository {
    suspend fun saveFrameLocally(frame: ByteArray)
    suspend fun uploadFrame(frameId: Int): Boolean
    suspend fun getFramesByStatus(frameStatus: String): List<FrameModel>
    suspend fun updateFrameStatus(status: String, id: Int): Boolean
    suspend fun getPendingFrames(): Flow<List<FrameEntity>>
    suspend fun getFrameById(id: Int): FrameModel
}