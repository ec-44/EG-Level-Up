package com.ec.exercisegamification.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

// TODO: Fix this text component

@Composable
fun OutlinedText(
    text: String,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 40.sp,
    textColor: Color = Color.White,
    outlineColor: Color = Color.Black,
    outlineWidth: Float = 4f
) {
    Box(modifier = modifier) {
        Text(
            text = text,
            color = outlineColor,
            style = TextStyle.Default.copy(
                fontSize = fontSize,
                drawStyle = Stroke(
                    width = outlineWidth,
                    join = StrokeJoin.Round
                )
            )
        )
        Text(
            text = text,
            color = textColor,
            fontSize = fontSize,
        )
    }
}
