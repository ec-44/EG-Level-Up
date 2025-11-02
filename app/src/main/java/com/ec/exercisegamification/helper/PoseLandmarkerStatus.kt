package com.ec.exercisegamification.helper

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object PoseLandmarkerStatus {
    private val _isModelLoaded = MutableStateFlow(false)
    val isModelLoaded: StateFlow<Boolean> get() = _isModelLoaded

    fun setModelLoaded(loaded: Boolean) {
        _isModelLoaded.value = loaded
    }
}
