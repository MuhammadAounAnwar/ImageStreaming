package com.ono.imagestreaming.ui.scheduler

import android.content.Context
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.ono.imagestreaming.R
import com.ono.imagestreaming.domain.repository.FrameRepository
import com.ono.imagestreaming.domain.usecase.FrameUploadUseCase
import com.ono.imagestreaming.ui.service.FrameUploadService.Companion.BATCH_SIZE
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll

@HiltWorker
class FrameUploadWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val frameRepository: FrameRepository,
    private val uploadUseCase: FrameUploadUseCase
) : CoroutineWorker(context, workerParams) {
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override suspend fun doWork(): Result {

        setForegroundAsync(createForegroundInfo())

        val frames =
            frameRepository.getFramesByStatus("pending") // Implement this in your repository

        if (frames.isNotEmpty()) {
            try {
                /**
                 * For Async Upload
                 */
                var currentIndex = 0
                while (currentIndex < frames.size) {

                    // Process the batch of images
                    val imageBatch = frames.drop(currentIndex).take(BATCH_SIZE)
                    val uploadJobs = imageBatch.map { frame ->
                        serviceScope.async {
                            val result = runCatching { uploadUseCase(frame.id) }

                            result.fold(
                                onSuccess = { success ->
                                    if (success) Log.d("UploadService", "Uploaded ${frame.id}")
                                    else Log.e("UploadService", "Failed to upload ${frame.id}")
                                },
                                onFailure = { e ->
                                    Log.e(
                                        "UploadService",
                                        "Error uploading ${frame.id}: ${e.message}"
                                    )
                                }
                            )
                        }
                    }

                    // Wait for all the jobs in the batch to complete before moving to the next batch
                    uploadJobs.awaitAll()
                    currentIndex += imageBatch.size
                }
                /**
                 * For Sequential Upload
                 */


                /*frames.forEachIndexed { index, frame ->

                    val response = uploadUseCase(frame.id) // Your server API call
                    if (response) {
                        frameRepository.updateFrameStatus("uploaded", frame.id)
                    } else {
                        Log.e("FrameUploadWorker", "Upload failed for frame: ${frame.id}")
                        return Result.retry() // Retry in case of server failure
                    }
                }*/
                return Result.success() // All frames uploaded successfully


            } catch (e: Exception) {
                Log.e("FrameUploadWorker", "Error uploading frames", e)
                return Result.retry() // Retry on any exception
            }
        } else {
            Log.d("FrameUploadWorker", "No frames to upload.")
            return Result.success()
        }
    }

    private fun createForegroundInfo(): ForegroundInfo {
        val notification = NotificationCompat.Builder(applicationContext, "upload_channel")
            .setContentTitle("Uploading Frames")
            .setContentText("Frames are being uploaded in the background")
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setOngoing(true)
            .build()

        return ForegroundInfo(1, notification)
    }

}
