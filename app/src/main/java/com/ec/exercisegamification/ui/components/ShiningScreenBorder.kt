package com.ec.exercisegamification.ui.components

import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color

@Composable
fun ShiningScreenBorder(
    color: Color,
    trigger: Int,
    modifier: Modifier = Modifier
) {
    val alphaAnim = remember { androidx.compose.animation.core.Animatable(0f) }

    LaunchedEffect(trigger) {
        alphaAnim.snapTo(1f)
        alphaAnim.animateTo(
            targetValue = 0f,
            animationSpec = tween(durationMillis = 1000)
        )
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val strokeWidth = 40f
        val animatedAlpha = alphaAnim.value
        val baseColor = color.copy(alpha = animatedAlpha)

        val width = size.width
        val height = size.height

        // Top border: fade from color (opaque) at top edge to transparent downward
        drawRect(
            brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                colors = listOf(baseColor, baseColor.copy(alpha = 0f)),
                startY = 0f,
                endY = strokeWidth
            ),
            topLeft = Offset(0f, 0f),
            size = androidx.compose.ui.geometry.Size(width, strokeWidth)
        )

        // Bottom border: fade from color (opaque) at bottom edge to transparent upward
        drawRect(
            brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                colors = listOf(baseColor, baseColor.copy(alpha = 0f)),
                startY = height,
                endY = height - strokeWidth
            ),
            topLeft = Offset(0f, height - strokeWidth),
            size = androidx.compose.ui.geometry.Size(width, strokeWidth)
        )

        // Left border: fade from color (opaque) at left edge to transparent rightward
        drawRect(
            brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                colors = listOf(baseColor, baseColor.copy(alpha = 0f)),
                startX = 0f,
                endX = strokeWidth
            ),
            topLeft = Offset(0f, 0f),
            size = androidx.compose.ui.geometry.Size(strokeWidth, height)
        )

        // Right border: fade from color (opaque) at right edge to transparent leftward
        drawRect(
            brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                colors = listOf(baseColor, baseColor.copy(alpha = 0f)),
                startX = width,
                endX = width - strokeWidth
            ),
            topLeft = Offset(width - strokeWidth, 0f),
            size = androidx.compose.ui.geometry.Size(strokeWidth, height)
        )
    }
}