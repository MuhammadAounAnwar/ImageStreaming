package com.ono.imagestreaming.ui.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleService
import com.ono.imagestreaming.R
import com.ono.imagestreaming.domain.usecase.UploadImageUseCase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import javax.inject.Inject
import kotlin.coroutines.resume

@AndroidEntryPoint
class UploadService : LifecycleService() {

    private val TAG = "UploadService"
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    @Inject
    lateinit var uploadImageUseCase: UploadImageUseCase

    private val mutex = Mutex()
    private val imagePaths = mutableListOf<String>()

    private val _uploadState = MutableStateFlow(false)
    private val isUploadingState: StateFlow<Boolean> = _uploadState.asStateFlow()

    private var isUploading = false
    private var uploadJob: Job? = null

    companion object {
        const val CHANNEL_ID = "UploadServiceChannel"
        const val NOTIFICATION_ID = 1
        const val ACTION_START = "START"
        const val ACTION_PAUSE_RESUME = "PAUSE_RESUME"
        const val ACTION_CANCEL = "CANCEL"
        const val BATCH_SIZE = 3

        fun startService(context: Context, imagePaths: List<String>) {
            val intent = Intent(context, UploadService::class.java).apply {
                action = ACTION_START
                putStringArrayListExtra("imagePaths", ArrayList(imagePaths))
            }
            ContextCompat.startForegroundService(context, intent)
        }

        fun pauseOrResumeService(context: Context) {
            val intent = Intent(context, UploadService::class.java).apply {
                action = ACTION_PAUSE_RESUME
            }
            context.startService(intent)
        }

        fun cancelService(context: Context) {
            val intent = Intent(context, UploadService::class.java).apply {
                action = ACTION_CANCEL
            }
            context.startService(intent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        startForeground(NOTIFICATION_ID, createNotification("Uploading images..."))
        when (intent?.action) {
            ACTION_START -> {
                val newPaths = intent.getStringArrayListExtra("imagePaths") ?: listOf()
                imagePaths.addAll(newPaths)
                if (!isUploading) startUploading()
            }

            ACTION_PAUSE_RESUME -> togglePauseResume()
            ACTION_CANCEL -> cancelUploading()
        }
        return START_STICKY
    }

    private fun togglePauseResume() {
        if (_uploadState.value) {
            // If the upload is paused, resume it by releasing the mutex lock
            _uploadState.value = false
            mutex.unlock()
            updateNotification("Uploading...")
        } else {
            // If the upload is not paused, pause it by locking the mutex
            _uploadState.value = true
            updateNotification("Paused")
        }
    }

    private fun startUploading() {
        uploadJob = serviceScope.launch {
            isUploading = true
            var currentIndex = 0

            while (currentIndex < imagePaths.size) {
                // Before processing the batch, check if the upload should be paused
                if (_uploadState.value) {
                    Log.d("UploadService", "Upload paused.")
                    mutex.lock()  // Locking the mutex to pause the coroutine
                    Log.d("UploadService", "Upload resumed.")
                }

                // Process the batch of images
                val imageBatch = imagePaths.drop(currentIndex).take(BATCH_SIZE)
                val uploadJobs = imageBatch.map { imagePath ->
                    async {
                        val result = runCatching { uploadImageUseCase(imagePath) }

                        result.fold(
                            onSuccess = { success ->
                                if (success) Log.d("UploadService", "Uploaded $imagePath")
                                else Log.e("UploadService", "Failed to upload $imagePath")
                            },
                            onFailure = { e ->
                                Log.e("UploadService", "Error uploading $imagePath: ${e.message}")
                            }
                        )
                    }
                }

                // Wait for all the jobs in the batch to complete before moving to the next batch
                uploadJobs.awaitAll()
                currentIndex += imageBatch.size
            }

            isUploading = false
            imagePaths.clear()
            stopSelf()
        }

        uploadJob?.invokeOnCompletion {
            if (it != null) {
                Log.d("UploadService", "Upload process canceled or failed")
            } else {
                Log.d("UploadService", "Upload completed successfully")
                cancelUploading()
            }
        }
    }


    private fun cancelUploading() {
//        serviceScope.coroutineContext.cancelChildren()
        uploadJob?.cancel()
        stopSelf()
        Log.d("UploadService", "Upload canceled by user.")
    }


    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Upload Service",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun updateNotification(content: String) {
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Image Upload")
            .setContentText(content)
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setOngoing(true)
            .build()
        startForeground(NOTIFICATION_ID, notification)
    }

    private fun createNotification(content: String): Notification {
        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Image Upload")
            .setContentText(content)
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)

        // For Android 8.0 (API level 26) and above, create a notification channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Upload Service",
                NotificationManager.IMPORTANCE_LOW
            )
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }

        return notificationBuilder.build()
    }
}
