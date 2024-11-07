package com.ono.imagestreaming.util

import android.content.Context
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.accompanist.permissions.PermissionState


@Composable
fun RequestPermissions(onPermissionsResult: (Map<String, Boolean>) -> Unit) {
    val permissions = mutableListOf(
        android.Manifest.permission.CAMERA
    ).apply {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
    }

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { permissionsResult ->
            onPermissionsResult(permissionsResult)
        }
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("Camera and Storage permissions are required.")
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
            requestPermissionLauncher.launch(permissions.toTypedArray())
        }) {
            Text("Request Permissions")
        }
    }
}


fun handlePermissionsResult(context: Context, permissions: Map<String, Boolean>) {
    val allGranted = permissions.values.all { it }
    if (allGranted) {
        Toast.makeText(context, "All Permissions Granted", Toast.LENGTH_SHORT).show()
    } else {
        Toast.makeText(context, "Some Permissions Denied", Toast.LENGTH_SHORT).show()
    }
}


//------------------------------------------------------------------------------------


@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun RequestPermissionsScreen(
    multiplePermissionsState: MultiplePermissionsState,
    onPermissionsGranted: @Composable () -> Unit
) {
    if (multiplePermissionsState.allPermissionsGranted) {
        GrantedPermissionsContent(multiplePermissionsState.permissions)
        onPermissionsGranted()
    } else {
        RevokedPermissionsContent(
            permissions = multiplePermissionsState.revokedPermissions,
            shouldShowRationale = multiplePermissionsState.shouldShowRationale,
            onRequestPermissions = { multiplePermissionsState.launchMultiplePermissionRequest() }
        )
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun GrantedPermissionsContent(permissions: List<PermissionState>) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "${permissions.GetPermissionsNames()} permissions are granted! Thank you!",
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun RevokedPermissionsContent(
    permissions: List<PermissionState>,
    shouldShowRationale: Boolean,
    onRequestPermissions: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = getPermissionsExplanation(permissions, shouldShowRationale),
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRequestPermissions) {
            Text("Request Permissions")
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
private fun getPermissionsExplanation(
    permissions: List<PermissionState>,
    shouldShowRationale: Boolean
): String {
    if (permissions.isEmpty()) return ""

    val permissionsText = permissions
        .map { it.permission.split('.').last() } // Get permission name (e.g., CAMERA)
        .joinToString(", ") // Combine names into a readable list

    val actionText = if (shouldShowRationale) {
        "are important for the app to function properly. Please grant them."
    } else {
        "are denied. The app cannot function without them."
    }

    return "The following permissions $permissionsText $actionText"
}

@OptIn(ExperimentalPermissionsApi::class)
fun List<PermissionState>.GetPermissionsNames() =
    this.joinToString(", ") {
        it.permission.split('.').last()
    } // Combine names into a readable list

