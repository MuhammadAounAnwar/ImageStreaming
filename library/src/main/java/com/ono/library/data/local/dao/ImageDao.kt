package com.ono.library.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.ono.imagestreaming.data.local.entity.ImageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ImageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertImage(image: ImageEntity)

    @Query("SELECT * FROM images WHERE status = :status")
    suspend fun getImagesByStatus(status: String): List<ImageEntity>

    @Query("SELECT * FROM images WHERE status = 'pending'")
    fun getPendingImages(): Flow<List<ImageEntity>>

    @Update
    suspend fun updateImage(image: ImageEntity)

    @Query("UPDATE images SET status = :status WHERE filePath = :filePath")
    suspend fun updateImageStatus(status: String, filePath: String): Int
}

