package com.ec.exercisegamification.helper

import android.util.Size
import androidx.compose.ui.geometry.Offset
import com.ec.exercisegamification.helper.PoseConstants.DEFAULT_THRESHOLD
import com.ec.exercisegamification.helper.PoseConstants.DEFAULT_TIMEOUT
import com.ec.exercisegamification.helper.PoseConstants.DEFAULT_TIMEOUT_PUNISH
import com.ec.exercisegamification.state.ScoreState
import com.ec.exercisegamification.state.PoseState

object PoseConstants {
    const val DEFAULT_THRESHOLD = 2500f
    const val DEFAULT_TIMEOUT = 3000L
    const val DEFAULT_TIMEOUT_PUNISH = 10L
}

fun poseMatcher(
    restPose: List<Offset>,
    finalPose: List<Offset>,
    poseTimeout: Long = DEFAULT_TIMEOUT,
    timeoutPunish: Long = DEFAULT_TIMEOUT_PUNISH,
    onRepetitionDetected: () -> Unit,
    onTimeout: () -> Unit,
    scoreState: ScoreState
): (List<Offset>, Size) -> Unit {

    var poseState = PoseState.Idle
    var lastPoseMatchTime = System.currentTimeMillis()

    val poseLambda: (List<Offset>, Size) -> Unit = poseLambda@{ landmarks, _ ->
        // Log.d("PoseMatcher", "poseLambda called. landmarks size: ${landmarks.size}, restPose: ${restPose.size}, finalPose: ${finalPose.size}")
        if (landmarks.isEmpty() || restPose.isEmpty() || finalPose.isEmpty()) {
            // Reset state if no landmarks detected
            scoreState.resetMult()
            poseState = PoseState.Idle
            return@poseLambda
        }
        val now = System.currentTimeMillis()
        val averageDistanceToFinal = averagePoseDistance(landmarks, finalPose)
        val averageDistanceToRest = averagePoseDistance(landmarks, restPose)

        when (poseState) {
            PoseState.Idle, PoseState.MatchedRest -> {
                if (isWithinThreshold(averageDistanceToFinal)) {
                    poseState = PoseState.MatchedFinal
                    lastPoseMatchTime = now
                }
            }

            PoseState.MatchedFinal -> {
                if (isWithinThreshold(averageDistanceToRest)) {
                    poseState = PoseState.MatchedRest
                    lastPoseMatchTime = now
                    onRepetitionDetected()
                }
            }
        }

        if (now - lastPoseMatchTime > poseTimeout - timeoutPunish*scoreState.multiplier) {
            lastPoseMatchTime = now
            scoreState.penalizeForTimeout()
            poseState = PoseState.Idle
            onTimeout()
        }
    }

    return poseLambda
}


fun averagePoseDistance(a: List<Offset>, b: List<Offset>): Double {
    if (a.size != b.size || a.isEmpty()) return Double.NaN
    return a.zip(b).map { (p1, p2) -> (p1 - p2).getDistanceSquared() }.average()
}

fun isWithinThreshold(distance: Double, threshold: Float = DEFAULT_THRESHOLD): Boolean {
    return distance < threshold
}
