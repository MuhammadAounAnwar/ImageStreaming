package com.ono.imagestreaming.data.di

import com.ono.imagestreaming.data.local.dao.ImageDao
import com.ono.imagestreaming.data.remote.ApiService
import com.ono.imagestreaming.data.repository.ImageRepositoryImpl
import com.ono.imagestreaming.domain.repository.ImageRepository
import com.ono.imagestreaming.domain.usecase.UploadImageUseCase
import com.ono.imagestreaming.domain.usecase.UploadImageUseCaseImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

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

}