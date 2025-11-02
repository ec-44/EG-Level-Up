package com.ec.exercisegamification.helper

import android.content.Context
import android.util.Size
import androidx.camera.core.ImageProxy
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset

@Composable
fun usePoseDetector(
    context: Context,
    onPoseDetected: (List<Offset>, Size) -> Unit
): (imageProxy: ImageProxy) -> Unit {

    // Access the PoseLandmarkerHelper instance from the Singleton
    val poseHelper = remember {
        (context.applicationContext as PoseLandmarkerSingleton).poseLandmarkerHelper
    }

    LaunchedEffect(Unit) {
        val poseLandmarkerSingleton = context.applicationContext as PoseLandmarkerSingleton
            poseLandmarkerSingleton.onPoseDetectedCallback = { smoothedLandmarks, width, height ->
            // Forward the detected pose landmarks
            onPoseDetected(smoothedLandmarks, Size(width, height))
        }
    }

    // Return the function that handles image proxy processing
    return remember {
        { imageProxy: ImageProxy ->
            poseHelper.detectLiveStream(imageProxy)
        }
    }
}
