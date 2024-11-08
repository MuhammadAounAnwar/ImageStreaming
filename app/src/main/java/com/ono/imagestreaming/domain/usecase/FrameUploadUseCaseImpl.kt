package com.ono.imagestreaming.domain.usecase

import com.ono.imagestreaming.domain.model.FrameModel
import com.ono.imagestreaming.domain.repository.FrameRepository
import javax.inject.Inject

class FrameUploadUseCaseImpl @Inject constructor(private val repository: FrameRepository) :
    FrameUploadUseCase {
    override suspend fun invoke(frame: FrameModel): Boolean {
        return repository.uploadFrame(frame)
    }
}