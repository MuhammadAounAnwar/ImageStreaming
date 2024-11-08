package com.ono.imagestreaming.domain.usecase

import com.ono.imagestreaming.domain.model.FrameModel

interface FrameUploadUseCase {
    suspend operator fun invoke(frameId: Int): Boolean
}