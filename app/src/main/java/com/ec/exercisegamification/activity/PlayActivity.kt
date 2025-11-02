package com.ec.exercisegamification.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.ec.exercisegamification.helper.ResultData
import com.ec.exercisegamification.helper.RoutineData
import com.ec.exercisegamification.helper.loadRoutineFromFile
import com.ec.exercisegamification.helper.saveResultToFile
import com.ec.exercisegamification.state.ScoreState
import com.ec.exercisegamification.ui.screens.ResultsScreen
import com.ec.exercisegamification.ui.theme.ExerciseGamificationTheme

class PlayActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val routineName = intent.getStringExtra("routineName") ?: return

        enableEdgeToEdge()
        setContent {
            ExerciseGamificationTheme {
                RoutinePlayerScreen(
                    routineName = routineName,
                    onRoutineFinished = {
                        finish()
                    }
                )
            }
        }
    }
}


@Composable
fun RoutinePlayerScreen(
    routineName: String,
    onRoutineFinished: () -> Unit
) {
    val context = LocalContext.current
    var routineData by remember { mutableStateOf<RoutineData?>(null) }

    var currentIndex by remember { mutableIntStateOf(0) }
    var currentRep by remember { mutableIntStateOf(0) }

    val scoreState = remember { ScoreState() }
    var maxMultiplier by remember { mutableIntStateOf(1) }
    val exerciseScores = remember { mutableListOf<Int>() }

    var initialScore by remember { mutableIntStateOf(0) }

    // Protection for double save edge case
    var resultSaved by remember { mutableStateOf(false) }

    // Load routine once
    LaunchedEffect(routineName) {
        routineData = loadRoutineFromFile(context, routineName)
    }

    val poses = routineData?.poses ?: return

    // When we move to a new exercise, update the starting score baseline
    LaunchedEffect(currentIndex) {
        initialScore = scoreState.score
    }

    if (currentIndex >= poses.size) {
        // Save results
        if (!resultSaved) {
            val result = ResultData(
                routineName = routineName,
                timestamp = System.currentTimeMillis(),
                totalScore = scoreState.score,
                maxMultiplier = maxMultiplier,
                exerciseScores = exerciseScores
            )
            saveResultToFile(context, result)
            resultSaved = true
        }

        // Show results
        ResultsScreen(
            totalScore = scoreState.score,
            maxMultiplier = maxMultiplier,
            exerciseScores = exerciseScores,
            onFinish = onRoutineFinished
        )
        return
    }

    val currentPose = poses[currentIndex]

    CameraScreen(
        currentExercise = currentPose.name,
        scoreState = scoreState,
        onExerciseComplete = {
            currentRep++
            maxMultiplier = maxOf(maxMultiplier, scoreState.multiplier)

            if (currentRep >= currentPose.repetitions) {
                val gained = scoreState.score - initialScore
                exerciseScores.add(gained)

                // Move to next exercise
                currentIndex++
                currentRep = 0
            }
        }
    )
}




