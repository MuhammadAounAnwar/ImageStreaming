package com.ono.imagestreaming.data.scheduler

import android.content.Context
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.ono.imagestreaming.domain.scheduler.UploadScheduler
import com.ono.imagestreaming.ui.scheduler.FrameUploadWorker

class WorkManagerUploadScheduler(private val context: Context) : UploadScheduler {

    override fun scheduleUpload() {
        val workRequest = OneTimeWorkRequestBuilder<FrameUploadWorker>()
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .setRequiresBatteryNotLow(false)
                    .build()
            )
            .build()

        WorkManager.getInstance(context).enqueue(workRequest)
    }
}