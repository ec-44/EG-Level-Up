package com.ec.exercisegamification.helper

import android.app.Application
import android.util.Log
import androidx.compose.ui.geometry.Offset

class PoseLandmarkerSingleton : Application() {

    companion object {
        private const val TAG = "PoseLandmarkerSingleton"
    }

    lateinit var poseLandmarkerHelper: PoseLandmarkerHelper
    private var previousLandmarks: List<Offset>? = null
    var onPoseDetectedCallback: ((List<Offset>, Int, Int) -> Unit)? = null

    override fun onCreate() {
        super.onCreate()

        poseLandmarkerHelper = PoseLandmarkerHelper(
            context = applicationContext,
            poseLandmarkerHelperListener = object : PoseLandmarkerHelper.LandmarkerListener {
                override fun onError(error: String, errorCode: Int) {
                    Log.e(TAG, "Error $errorCode: $error")
                }

                override fun onResults(resultBundle: PoseLandmarkerHelper.ResultBundle) {
                    // Apply smoothing and call the callback
                    val smoothedLandmarks = applySmoothing(resultBundle)
                    // Check if the callback is not null, then call it
                    onPoseDetectedCallback?.invoke(smoothedLandmarks, resultBundle.inputImageWidth, resultBundle.inputImageHeight)
                }
            }
        )

        Log.d(TAG, "Created Singleton")
    }

    // Apply smoothing to landmarks
    private fun applySmoothing(resultBundle: PoseLandmarkerHelper.ResultBundle): List<Offset> {
        val importantIndices = listOf(
            0,      // Nose
            11, 12, // Shoulders
            13, 14, // Elbows
            15, 16, // Wrists
            23, 24, // Hips
            25, 26, // Knees
            27, 28  // Ankles
        )

        val allLandmarks = resultBundle.results.firstOrNull()?.landmarks()?.firstOrNull()
        val rawLandmarks = importantIndices.mapNotNull { index ->
            allLandmarks?.getOrNull(index)?.let { landmark ->
                Offset(
                    landmark.x() * resultBundle.inputImageWidth,
                    landmark.y() * resultBundle.inputImageHeight
                )
            }
        }

        val smoothingFactor = 0.6f
        val smoothed = if (previousLandmarks != null && previousLandmarks!!.size == rawLandmarks.size) {
            rawLandmarks.mapIndexed { i, now ->
                val prev = previousLandmarks!![i]
                Offset(
                    x = smoothingFactor * prev.x + (1 - smoothingFactor) * now.x,
                    y = smoothingFactor * prev.y + (1 - smoothingFactor) * now.y
                )
            }
        } else {
            rawLandmarks
        }

        previousLandmarks = smoothed
        return smoothed
    }

    fun cleanupPoseLandmarker() {
        poseLandmarkerHelper.onDestroy()
    }
}
