package com.ono.library.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.ono.imagestreaming.data.local.dao.FrameDao
import com.ono.imagestreaming.data.local.dao.ImageDao
import com.ono.imagestreaming.data.local.entity.FrameEntity
import com.ono.imagestreaming.data.local.entity.ImageEntity

@Database(entities = [ImageEntity::class, FrameEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun imageDao(): ImageDao
    abstract fun frameDao(): FrameDao
}
