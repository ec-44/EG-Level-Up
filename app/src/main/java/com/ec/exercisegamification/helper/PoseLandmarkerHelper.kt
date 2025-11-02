package com.ec.exercisegamification.helper

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.os.SystemClock
import android.util.Log
import androidx.camera.core.ImageProxy
import androidx.core.graphics.createBitmap
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.framework.image.MPImage
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.core.Delegate
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarker
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class PoseLandmarkerHelper(
    var minPoseDetectionConfidence: Float = DEFAULT_POSE_DETECTION_CONFIDENCE,
    var minPoseTrackingConfidence: Float = DEFAULT_POSE_TRACKING_CONFIDENCE,
    var minPosePresenceConfidence: Float = DEFAULT_POSE_PRESENCE_CONFIDENCE,
    val context: Context,
    val poseLandmarkerHelperListener: LandmarkerListener? = null
) {
    private var poseLandmarker: PoseLandmarker? = null
    private val coroutineScope = CoroutineScope(Dispatchers.Default)

    init {
        setupPoseLandmarker()
    }

    private fun setupPoseLandmarker() {
        coroutineScope.launch {
            try {
                val baseOptionBuilder = BaseOptions.builder()

                baseOptionBuilder.setDelegate(Delegate.GPU)

                baseOptionBuilder.setModelAssetPath(LITE_MODEL_NAME)

                if (poseLandmarkerHelperListener == null) {
                    throw IllegalStateException(
                        "poseLandmarkerHelperListener must be set."
                    )
                }

                val baseOptions = baseOptionBuilder.build()
                // Create an option builder with base options and specific
                // options only use for Pose Landmarker.
                val optionsBuilder =
                    PoseLandmarker.PoseLandmarkerOptions.builder()
                        .setBaseOptions(baseOptions)
                        .setMinPoseDetectionConfidence(minPoseDetectionConfidence)
                        .setMinTrackingConfidence(minPoseTrackingConfidence)
                        .setMinPosePresenceConfidence(minPosePresenceConfidence)
                        .setNumPoses(DEFAULT_NUM_POSES)
                        .setRunningMode(RunningMode.LIVE_STREAM)
                        .setResultListener(this@PoseLandmarkerHelper::returnLivestreamResult)
                        .setErrorListener(this@PoseLandmarkerHelper::returnLivestreamError)

                val options = optionsBuilder.build()

                poseLandmarker =
                    PoseLandmarker.createFromOptions(context, options)

                // Notify that initialization is done (back on the main thread)
                withContext(Dispatchers.Main) {
                    PoseLandmarkerStatus.setModelLoaded(true)
                    Log.d(TAG, "Model Initialization Complete")
                    poseLandmarkerHelperListener.onResults(
                        ResultBundle(
                            emptyList(), // No results yet
                            0, // No inference time yet
                            0, // No image dimensions yet
                            0
                        )
                    )
                }
            } catch (e: Exception) {
                // Handle error (back on main thread)
                withContext(Dispatchers.Main) {
                    PoseLandmarkerStatus.setModelLoaded(false)

                    poseLandmarkerHelperListener?.onError(
                        "Pose Landmarker failed to initialize. See error logs for details"
                    )
                    Log.e(TAG, "$e")
                }
            }
        }
    }


    // Convert the ImageProxy to MP Image and feed it to PoselandmakerHelper.
    fun detectLiveStream(
        imageProxy: ImageProxy,
    ) {
        val frameTime = SystemClock.uptimeMillis()

        // Copy out RGB bits from the frame to a bitmap buffer
        val bitmapBuffer =
            createBitmap(imageProxy.width, imageProxy.height)

        imageProxy.use { bitmapBuffer.copyPixelsFromBuffer(imageProxy.planes[0].buffer) }
        imageProxy.close()

        val matrix = Matrix().apply {
            // Rotate the frame received from the camera to be in the same direction as it'll be shown
            postRotate(imageProxy.imageInfo.rotationDegrees.toFloat())

            postScale(
                -1f,
                1f,
                imageProxy.width.toFloat(),
                imageProxy.height.toFloat()
            )
        }
        val rotatedBitmap = Bitmap.createBitmap(
            bitmapBuffer, 0, 0,
            bitmapBuffer.width, bitmapBuffer.height,
            matrix, true
        )

        // Convert the input Bitmap object to an MPImage object to run inference
        val mpImage = BitmapImageBuilder(rotatedBitmap).build()

        detectAsync(mpImage, frameTime)
    }

    // Run pose landmark using MediaPipe Pose Landmarker API
    private fun detectAsync(mpImage: MPImage, frameTime: Long) {
        poseLandmarker?.detectAsync(mpImage, frameTime)
        // The landmark result will be returned
        // in returnLivestreamResult function
    }

    // Return the landmark result to this PoseLandmarkerHelper's caller
    private fun returnLivestreamResult(
        result: PoseLandmarkerResult,
        input: MPImage
    ) {
        val finishTimeMs = SystemClock.uptimeMillis()
        val inferenceTime = finishTimeMs - result.timestampMs()

        poseLandmarkerHelperListener?.onResults(
            ResultBundle(
                listOf(result),
                inferenceTime,
                input.height,
                input.width
            )
        )
    }

    // Return errors thrown during detection to this PoseLandmarkerHelper's
    // caller
    private fun returnLivestreamError(error: RuntimeException) {
        poseLandmarkerHelperListener?.onError(
            error.message ?: "An unknown error has occurred"
        )
    }

    fun onDestroy() {
        coroutineScope.cancel()  // Cancel any ongoing background tasks when the activity/fragment is destroyed
    }

    companion object {
        const val TAG = "PoseLandmarkerHelper"
        const val DEFAULT_POSE_DETECTION_CONFIDENCE = 0.5F
        const val DEFAULT_POSE_TRACKING_CONFIDENCE = 0.5F
        const val DEFAULT_POSE_PRESENCE_CONFIDENCE = 0.5F
        const val DEFAULT_NUM_POSES = 1
        const val OTHER_ERROR = 0
        const val GPU_ERROR = 1
        const val LITE_MODEL_NAME = "pose_landmarker_lite.task"
    }

    data class ResultBundle(
        val results: List<PoseLandmarkerResult>,
        val inferenceTime: Long,
        val inputImageHeight: Int,
        val inputImageWidth: Int,
    )

    interface LandmarkerListener {
        fun onError(error: String, errorCode: Int = OTHER_ERROR)
        fun onResults(resultBundle: ResultBundle)
    }
}