package com.ec.exercisegamification.state

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue

class ScoreState {
    var score by mutableIntStateOf(0)
        private set

    var multiplier by mutableIntStateOf(1)
        private set

    fun onPoseMatched() {
        score += 5 * multiplier
        multiplier += 1
    }

    fun penalizeForTimeout() {
        multiplier = maxOf(multiplier / 2, 1)
    }

    fun resetMult() {
        multiplier = 1
    }
}
