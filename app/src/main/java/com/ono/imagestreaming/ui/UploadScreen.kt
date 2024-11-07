package com.ono.imagestreaming.ui

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.ono.imagestreaming.ui.mainscreen.MainViewModel

@Composable
fun UploadScreen(viewModel: MainViewModel, context: Context) {
    var imagePaths = listOf("image1.jpg", "image2.jpg", "image3.jpg")

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Button(onClick = { viewModel.startUploadService(context, imagePaths) }) {
            Text("Start Upload")
        }

        Button(onClick = { viewModel.togglePauseResume(context) }) {
            Text("Pause/Resume Upload")
        }

        Button(onClick = { viewModel.cancelUploadService(context) }) {
            Text("Cancel Upload")
        }
    }
}
