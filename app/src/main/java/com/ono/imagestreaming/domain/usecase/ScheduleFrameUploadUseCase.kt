package com.ono.imagestreaming.domain.usecase

import com.ono.imagestreaming.domain.scheduler.UploadScheduler

class ScheduleFrameUploadUseCase(private val uploadScheduler: UploadScheduler) {
    operator fun invoke() {
        uploadScheduler.scheduleUpload()
    }
}