package com.ec.exercisegamification.state

enum class PoseState {
    Idle, // Starting point
    MatchedFinal, // Final pose matched
    MatchedRest // Rest pose matched after final (1 full rep done)
}