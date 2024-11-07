package com.ono.imagestreaming.domain.usecase

interface UploadImageUseCase {
    suspend operator fun invoke(filePath: String): Boolean
}