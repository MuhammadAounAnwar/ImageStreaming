package com.ono.library.data.di

import android.app.Application
import androidx.room.Room
import com.ono.imagestreaming.data.local.AppDatabase
import com.ono.imagestreaming.data.local.dao.FrameDao
import com.ono.imagestreaming.data.local.dao.ImageDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(app: Application): AppDatabase {
        return Room.databaseBuilder(app, AppDatabase::class.java, "image_stream_db").build()
    }

    @Provides
    @Singleton
    fun provideImageDao(db: AppDatabase): ImageDao = db.imageDao()

    @Provides
    @Singleton
    fun provideFrameDao(db: AppDatabase): FrameDao = db.frameDao()
}
