package com.ec.exercisegamification.activity

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CameraPermissionRequest()
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraPermissionRequest() {
    // Permission state for CAMERA
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)

    // Remember the permission granted state
    val hasPermission = cameraPermissionState.status.isGranted

    // Access the context (activity) for navigation
    val context = LocalContext.current

    // When permission is granted, navigate to the ExerciseListActivity
    LaunchedEffect(hasPermission) {
        if (hasPermission) {
            navigateToExerciseList(context)
        }
    }

    // UI for the camera permission request
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        if (hasPermission) {
            // If permission is granted, show loading or transition to the next screen
            Text("Permission Granted! Navigating to Exercise List...")
        } else {
            // Show the permission request UI
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(16.dp)
            ) {
                Text("Camera permission is required to use this app.")

                Spacer(modifier = Modifier.height(16.dp))

                Button(onClick = { cameraPermissionState.launchPermissionRequest() }) {
                    Text("Grant Camera Permission")
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Optional: If permission is denied permanently, show rationale or additional options
                if (cameraPermissionState.status.shouldShowRationale) {
                    TextButton(onClick = { cameraPermissionState.launchPermissionRequest() }) {
                        Text("Camera Permission Denied, the app cannot function without it")
                    }
                }
            }
        }
    }
}

fun navigateToExerciseList(context: Context) {
    val intent = Intent(context, NavActivity::class.java)
    context.startActivity(intent)
}

@Preview(showBackground = true)
@Composable
fun CameraPermissionRequestPreview() {
    CameraPermissionRequest()
}
