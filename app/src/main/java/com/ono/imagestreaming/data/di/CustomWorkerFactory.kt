package com.ono.imagestreaming.data.di

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.ono.imagestreaming.domain.repository.FrameRepository
import com.ono.imagestreaming.domain.usecase.FrameUploadUseCase
import com.ono.imagestreaming.ui.scheduler.FrameUploadWorker
import javax.inject.Inject

class CustomWorkerFactory @Inject constructor(
    private val frameRepository: FrameRepository,
    private val uploadUseCase: FrameUploadUseCase
) : WorkerFactory() {

    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker? {
        return when (workerClassName) {
            FrameUploadWorker::class.java.name -> FrameUploadWorker(
                frameRepository = frameRepository,
                uploadUseCase = uploadUseCase,
                context = appContext,
                workerParams = workerParameters
            )

            else -> null
        }
    }
}