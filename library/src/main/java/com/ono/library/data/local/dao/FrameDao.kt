package com.ono.library.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.ono.imagestreaming.data.local.entity.FrameEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FrameDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFrame(frame: FrameEntity)

    @Query("SELECT * FROM frames WHERE status = :status")
    suspend fun getFramesByStatus(status: String): List<FrameEntity>

    @Query("SELECT * FROM frames WHERE status = 'pending'")
    fun getPendingFrames(): Flow<List<FrameEntity>>

    @Update
    suspend fun updateFrame(frame: FrameEntity)

    @Query("UPDATE frames SET status = :status WHERE id = :id")
    suspend fun updateFrameStatus(status: String, id: Int): Int

    @Query("SELECT * FROM frames WHERE id = :id")
    suspend fun getFrameById(id: Int): FrameEntity
}