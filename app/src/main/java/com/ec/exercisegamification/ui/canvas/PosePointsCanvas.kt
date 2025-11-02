package com.ec.exercisegamification.ui.canvas

import android.util.Size
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import com.ec.exercisegamification.state.PoseState
import kotlin.math.hypot

@Composable
fun PosePointsCanvas(
    keypoints: List<Offset>,
    restPosePoints: List<Offset>,
    finalPosePoints: List<Offset>,
    poseState: PoseState,
    cameraPreviewSize: Size,
    snap: Boolean = true
) {
    Canvas(
        modifier = Modifier.fillMaxSize()
    ) {
        val scaleX = size.width / cameraPreviewSize.width
        val scaleY = size.height / cameraPreviewSize.height

        val scaledKeypoints = keypoints.map { Offset(it.x * scaleX, it.y * scaleY) }
        val scaledRestPose = restPosePoints.map { Offset(it.x * scaleX, it.y * scaleY) }
        val scaledFinalPose = finalPosePoints.map { Offset(it.x * scaleX, it.y * scaleY) }

        val activeReference = when (poseState) {
            PoseState.Idle, PoseState.MatchedRest -> scaledRestPose
            PoseState.MatchedFinal -> scaledFinalPose
        }


        // Draw Active Reference Pose Points
        activeReference.forEach {
            drawCircleWithRing(it, Color(0xFF90CAF9)) // Light blue
        }

        // Draw Keypoints
        val snapThreshold = 30f

        scaledKeypoints.forEach { point ->
            val renderedPoint = if (snap) {
                snapToClosest(point, activeReference, snapThreshold) ?: point
            } else {
                point
            }

            drawCircleWithRing(renderedPoint, Color.White)
        }
    }
}


fun snapToClosest(
    point: Offset,
    referencePoints: List<Offset>,
    threshold: Float
): Offset? {
    return referencePoints.minByOrNull { it.getDistance(point) }
        ?.takeIf { it.getDistance(point) < threshold }
}

fun Offset.getDistance(other: Offset): Float {
    return hypot(x - other.x, y - other.y)
}

fun DrawScope.drawCircleWithRing(
    centerPx: Offset,
    color: Color
){
    drawCircle(
        color = color,
        radius = 16f,
        style = Stroke(width = 4f),
        center = centerPx
    )
    drawCircle(
        color = color,
        radius = 6f,
        center = centerPx
    )
}
