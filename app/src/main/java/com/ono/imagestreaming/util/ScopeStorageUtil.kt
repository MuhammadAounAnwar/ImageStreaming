package com.ono.imagestreaming.util

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.provider.MediaStore
import java.io.OutputStream

object ScopeStorageUtil {
    fun saveFileToScopedStorage(context: Context, fileName: String, content: ByteArray) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, "application/octet-stream")
                put(MediaStore.MediaColumns.RELATIVE_PATH, "Download/MyApp")
            }
            val uri = context.contentResolver.insert(
                MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                contentValues
            )
            uri?.let {
                val outputStream: OutputStream? = context.contentResolver.openOutputStream(it)
                outputStream?.use { stream ->
                    stream.write(content)
                }
            }
        }
    }
}