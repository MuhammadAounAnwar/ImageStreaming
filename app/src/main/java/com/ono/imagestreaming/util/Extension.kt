package com.ono.imagestreaming.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import java.io.File

fun String.createFileFromPath(): File {
    val file = File(this)
    if (file.exists() && file.isFile) {
        return file
    } else {
        throw IllegalArgumentException("File at $this does not exist or is not a valid file.")
    }
}

fun Context.isInternetAvailable(onConnected: () -> Unit = {}): Boolean {
    // Get the ConnectivityManager system service
    val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        // For Android 6.0+ (API level 23+), use NetworkCapabilities
        connectivityManager.activeNetwork?.let { activeNetwork ->
            val isConnected = connectivityManager.getNetworkCapabilities(activeNetwork)?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
            if (isConnected) {
                onConnected() // Call the callback when the internet is connected
            }
            isConnected
        } ?: false
    } else {
        // For lower versions (API < 23), fallback to legacy method
        connectivityManager.activeNetworkInfo?.isConnected == true
    }
}
