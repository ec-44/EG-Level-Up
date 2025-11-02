package com.ec.exercisegamification.activity

import android.os.Bundle
import android.util.Size
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.ec.exercisegamification.R
import com.ec.exercisegamification.camera.CameraPreview
import com.ec.exercisegamification.helper.savePoseToFile
import com.ec.exercisegamification.helper.usePoseDetector
import com.ec.exercisegamification.state.PoseState
import com.ec.exercisegamification.ui.canvas.PosePointsCanvas
import com.ec.exercisegamification.ui.components.OutlinedText
import kotlinx.coroutines.delay

class PoseRecorderActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val name = intent.getStringExtra("exerciseName") ?: ""
            PoseRecorderScreen(initialName = name)
        }
    }
}

@Composable
fun PoseRecorderScreen(initialName: String) {
    var detectedLandmarks by remember { mutableStateOf<List<Offset>>(emptyList()) }
    var restPose by remember { mutableStateOf<List<Offset>>(emptyList()) }
    var finalPose by remember { mutableStateOf<List<Offset>>(emptyList()) }
    var cameraPreviewSize by remember { mutableStateOf(Size(0, 0)) }

    val exerciseName = remember { initialName }
    var isRecording by remember { mutableStateOf(false) }
    var currentPoseLabel by remember { mutableStateOf("Rest Pose") }

    var countdownValue by remember { mutableIntStateOf(0) }

    val context = LocalContext.current

    // Launch delay and countdown effect when recording starts
    LaunchedEffect(isRecording) {
        if (isRecording) {
            for (i in 3 downTo 1) {
                countdownValue = i
                delay(1000L)
            }

            countdownValue = 0

            // Save pose
            if (currentPoseLabel == "Rest Pose") {
                restPose = detectedLandmarks
                currentPoseLabel = "Final Pose"
            } else {
                finalPose = detectedLandmarks
            }

            delay(1000L)

            isRecording = false
        }
    }

    val poseState = when {
        finalPose.isNotEmpty() -> PoseState.MatchedFinal
        restPose.isNotEmpty() -> PoseState.MatchedRest
        else -> PoseState.Idle
    }


    Box(modifier = Modifier.fillMaxSize()) {

        CameraPreview(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Red),
            onFrameAvailable = usePoseDetector(
                context = context
            ) { landmarks, previewSize ->
                detectedLandmarks = landmarks
                cameraPreviewSize = previewSize
            }
        )

        PosePointsCanvas(
            keypoints = detectedLandmarks,
            restPosePoints = restPose,
            finalPosePoints = finalPose,
            cameraPreviewSize = cameraPreviewSize,
            snap = false,
            poseState = poseState
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ){
                OutlinedText(currentPoseLabel)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Button(
                        onClick = {
                            if (!isRecording) {
                                isRecording = true
                            }
                        },
                        enabled = exerciseName.isNotBlank() && detectedLandmarks.isNotEmpty()
                    ) {
                        Icon(
                            painterResource(R.drawable.record),
                            contentDescription = "Record Pose",
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Button(
                        onClick = {
                            if (restPose.isNotEmpty() && finalPose.isNotEmpty()) {
                                savePoseToFile(context, exerciseName, restPose, finalPose)
                                // Go back
                                (context as? ComponentActivity)?.finish()
                            }
                        },
                        enabled = exerciseName.isNotBlank() && restPose.isNotEmpty() && finalPose.isNotEmpty()
                    ) {
                        Icon(
                            painterResource(R.drawable.save),
                            contentDescription = "Save File",
                        )
                    }
                }
            }
        }

    }

    // Countdown
    if (countdownValue > 0) {
        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            OutlinedText(
                text = countdownValue.toString(),
            )
        }
    }
}

