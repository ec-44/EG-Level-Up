package com.ec.exercisegamification.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ec.exercisegamification.state.ScoreState
import com.ec.exercisegamification.state.ShineEvent

@Composable
fun ScoreOverlay(
    scoreState: ScoreState,
    exerciseName: String,
    modifier: Modifier = Modifier,
    shineTrigger: Int = 0,
    shineEvent: ShineEvent = ShineEvent.None,
) {
    var triggerAnimation by remember { mutableIntStateOf(0) }
    var scaleUpComplete by remember { mutableStateOf(false) }

    // Shining border state
    val shineColor = when (shineEvent) {
        ShineEvent.Repetition -> Color.Blue
        ShineEvent.Timeout -> Color.Red
        else -> Color.Transparent
    }

    // Animations
    val scaleUp by animateFloatAsState(
        targetValue = if (triggerAnimation > 0 && !scaleUpComplete) 1.5f else 1f,
        animationSpec = tween(100, easing = FastOutSlowInEasing),
        label = "ScoreScaleUp"
    )

    val scaleDown by animateFloatAsState(
        targetValue = if (scaleUpComplete) 1f else scaleUp,
        animationSpec = tween(100, easing = FastOutSlowInEasing),
        label = "ScoreScaleDown"
    )

    val animatedScore by animateIntAsState(
        targetValue = scoreState.score,
        animationSpec = tween(500, easing = FastOutSlowInEasing),
        label = "ScoreAnimation"
    )

    LaunchedEffect(scaleUp) {
        if (scaleUp >= 1.5f) scaleUpComplete = true
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 64.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                OutlinedText(
                    text = animatedScore.toString(),
                    modifier = Modifier.scale(scaleDown),
                    fontSize = 40.sp,
                    textColor = Color.White,
                    outlineWidth = 4f
                )
                OutlinedText(
                    text = "x${scoreState.multiplier}",
                    fontSize = 20.sp,
                    textColor = Color.White,
                    outlineWidth = 2f
                )
            }
            Spacer(modifier = Modifier.size(8.dp))
            OutlinedText(
                text = exerciseName,
                fontSize = 24.sp,
                textColor = Color.Yellow,
                outlineWidth = 3f
            )
        }

        // Border animation
        ShiningScreenBorder(
            color = shineColor,
            trigger = shineTrigger,
            modifier = Modifier
        )
    }
}
