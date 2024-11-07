package com.ono.imagestreaming.domain.usecase

import com.ono.imagestreaming.domain.repository.ImageRepository
import javax.inject.Inject


class UploadImageUseCaseImpl @Inject constructor(private val repository: ImageRepository) :
    UploadImageUseCase {
    override suspend fun invoke(filePath: String): Boolean {
        return repository.uploadImage(filePath)
    }
}