package com.ec.exercisegamification.activity

import android.os.Bundle
import android.util.Size
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalContext
import com.ec.exercisegamification.camera.CameraPreview
import com.ec.exercisegamification.helper.loadPoseFromFile
import com.ec.exercisegamification.helper.poseMatcher
import com.ec.exercisegamification.helper.usePoseDetector
import com.ec.exercisegamification.state.ScoreState
import com.ec.exercisegamification.state.ShineEvent
import com.ec.exercisegamification.ui.canvas.PosePointsCanvas
import com.ec.exercisegamification.ui.components.ScoreOverlay
import com.ec.exercisegamification.state.PoseState

class TestActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val exerciseName = intent.getStringExtra("exerciseName") ?: "Unknown Exercise"

        enableEdgeToEdge()
        setContent {
            CameraScreen(currentExercise = exerciseName)
        }
    }
}



@Composable
fun CameraScreen(
    currentExercise: String,
    scoreState: ScoreState = remember { ScoreState() },
    onExerciseComplete: () -> Unit = {}
) {

    val context = LocalContext.current

    var detectedLandmarks by remember { mutableStateOf<List<Offset>>(emptyList()) }
    var cameraPreviewSize by remember { mutableStateOf(Size(0, 0)) }

    var restPose by remember { mutableStateOf<List<Offset>>(emptyList()) }
    var finalPose by remember { mutableStateOf<List<Offset>>(emptyList()) }

    var poseState by remember { mutableStateOf(PoseState.Idle) }

    var shineEvent by remember { mutableStateOf(ShineEvent.None) }
    var shineTrigger by remember { mutableIntStateOf(0) }

    // Load pose once
    LaunchedEffect(currentExercise) {
        val poseData = loadPoseFromFile(context, currentExercise)
        if (poseData != null) {
            restPose = poseData.restPose.map { Offset(it.x, it.y) }
            finalPose = poseData.finalPose.map { Offset(it.x, it.y) }
        }
    }

    var poseDetected by remember { mutableStateOf<(List<Offset>, Size) -> Unit>({ _, _ -> }) }

    LaunchedEffect(restPose, finalPose) {
        if (restPose.isNotEmpty() && finalPose.isNotEmpty()) {
            poseDetected = poseMatcher(
                restPose = restPose,
                finalPose = finalPose,
                scoreState = scoreState,
                onRepetitionDetected = {
                    scoreState.onPoseMatched()
                    shineEvent = ShineEvent.Repetition
                    shineTrigger++
                    onExerciseComplete()
                },
                onTimeout = {
                    shineEvent = ShineEvent.Timeout
                    shineTrigger++
                }
            )
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {

        CameraPreview(
            modifier = Modifier.fillMaxSize(),
            onFrameAvailable = usePoseDetector(
                context = context,
                onPoseDetected = { landmarks, previewSize ->
                    cameraPreviewSize = previewSize
                    detectedLandmarks = landmarks
                    poseDetected(landmarks, previewSize)
                }
            )
        )

        PosePointsCanvas(
            keypoints = detectedLandmarks,
            restPosePoints = restPose,
            finalPosePoints = finalPose,
            cameraPreviewSize = cameraPreviewSize,
            poseState = poseState
        )

        ScoreOverlay(
            scoreState = scoreState,
            exerciseName = currentExercise,
            shineEvent = shineEvent,
            shineTrigger = shineTrigger
        )
    }
}
