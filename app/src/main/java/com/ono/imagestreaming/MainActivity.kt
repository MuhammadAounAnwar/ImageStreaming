package com.ono.imagestreaming

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.core.app.NotificationManagerCompat
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.ono.imagestreaming.ui.imagestream.ImageStream
import com.ono.imagestreaming.ui.mainscreen.MainScreen
import com.ono.imagestreaming.ui.mainscreen.MainViewModel
import com.ono.imagestreaming.ui.theme.ImageStreamingTheme
import com.ono.imagestreaming.util.RequestPermissionsScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @OptIn(ExperimentalPermissionsApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val viewModel: MainViewModel by viewModels()

//        enableEdgeToEdge()
        setContent {
            ImageStreamingTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    val multiplePermissionsState = rememberMultiplePermissionsState(
                        permissions = getRequiredPermissions()
                    )

                    RequestPermissionsScreen(
                        multiplePermissionsState = multiplePermissionsState,
                        onPermissionsGranted = {
                             /*if (hasNotificationPermission()) {
                                 MainScreen(
                                     modifier = Modifier.padding(innerPadding),
                                     viewModel = viewModel
                                 )

                                 ImageStream()
                             } else {
                                 requestNotificationPermission()
                             }*/

                            /*MainScreen(
                                modifier = Modifier.padding(innerPadding),
                                viewModel = viewModel
                            )*/
                            ImageStream()
                        }
                    )
                }
            }
        }
    }

    private fun getRequiredPermissions(): List<String> {
        return mutableListOf(Manifest.permission.CAMERA).apply {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }
    }

    private fun hasNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // For Android 13 (API level 33) and above
            NotificationManagerCompat.from(this).areNotificationsEnabled()
        } else {
            // For versions below Android 13, notifications are always enabled
            true
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!NotificationManagerCompat.from(this).areNotificationsEnabled()) {
                val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                    putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
                }
                startActivity(intent)
            }
        }
    }
}




