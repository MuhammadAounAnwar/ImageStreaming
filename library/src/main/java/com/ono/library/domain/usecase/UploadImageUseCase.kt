package com.ono.library.domain.usecase

interface UploadImageUseCase {
    suspend operator fun invoke(filePath: String): Boolean
}