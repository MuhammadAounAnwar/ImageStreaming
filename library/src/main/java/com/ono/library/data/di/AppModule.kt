package com.ono.library.data.di

import android.content.Context
import androidx.work.WorkerFactory
import com.ono.imagestreaming.data.local.dao.FrameDao
import com.ono.imagestreaming.data.local.dao.ImageDao
import com.ono.imagestreaming.data.remote.ApiService
import com.ono.imagestreaming.data.repository.FrameRepositoryImpl
import com.ono.imagestreaming.data.repository.ImageRepositoryImpl
import com.ono.imagestreaming.data.scheduler.WorkManagerUploadScheduler
import com.ono.imagestreaming.domain.repository.FrameRepository
import com.ono.imagestreaming.domain.repository.ImageRepository
import com.ono.imagestreaming.domain.scheduler.UploadScheduler
import com.ono.imagestreaming.domain.usecase.FrameUploadUseCase
import com.ono.imagestreaming.domain.usecase.FrameUploadUseCaseImpl
import com.ono.imagestreaming.domain.usecase.ScheduleFrameUploadUseCase
import com.ono.imagestreaming.domain.usecase.UploadImageUseCase
import com.ono.imagestreaming.domain.usecase.UploadImageUseCaseImpl
import com.ono.library.data.local.dao.FrameDao
import com.ono.library.data.local.dao.ImageDao
import com.ono.library.data.remote.ApiService
import com.ono.library.data.repository.ImageRepositoryImpl
import com.ono.library.domain.repository.FrameRepository
import com.ono.library.domain.repository.ImageRepository
import com.ono.library.domain.usecase.UploadImageUseCase
import com.ono.library.domain.usecase.UploadImageUseCaseImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

//    Image

    @Provides
    @Singleton
    fun provideImageRepository(imageDao: ImageDao, apiService: ApiService): ImageRepository {
        return ImageRepositoryImpl(imageDao, apiService)
    }

    @Provides
    @Singleton
    fun provideImageUploadUseCase(imageRepository: ImageRepository): UploadImageUseCase {
        return UploadImageUseCaseImpl(imageRepository)
    }

//    Frame

    @Provides
    @Singleton
    fun provideFrameRepository(frameDao: FrameDao, apiService: ApiService): FrameRepository {
        return FrameRepositoryImpl(frameDao, apiService)
    }


    @Provides
    @Singleton
    fun provideFrameUploadUseCase(frameRepository: FrameRepository): FrameUploadUseCase {
        return FrameUploadUseCaseImpl(frameRepository)
    }

//    WorkManager

    @Provides
    fun provideUploadScheduler(@ApplicationContext context: Context): UploadScheduler {
        return WorkManagerUploadScheduler(context)
    }


    @Provides
    fun provideFileUploadSchedulerUseCase(uploadScheduler: UploadScheduler): ScheduleFrameUploadUseCase {
        return ScheduleFrameUploadUseCase(uploadScheduler)
    }

    @Provides
    @Singleton
    fun provideCustomWorkerFactory(
        frameRepository: FrameRepository,
        uploadUseCase: FrameUploadUseCase
    ): WorkerFactory {
        return CustomWorkerFactory(frameRepository, uploadUseCase)
    }

}