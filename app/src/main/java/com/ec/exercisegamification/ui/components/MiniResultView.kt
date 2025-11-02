package com.ec.exercisegamification.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.dp
import com.ec.exercisegamification.helper.ResultData

@Composable
fun MiniResultView(result: ResultData) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("Final Score: ${result.totalScore}", style = MaterialTheme.typography.bodyMedium)
        Text("Max Multiplier: x${result.maxMultiplier}", style = MaterialTheme.typography.bodyMedium)

        // Mini bar chart
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
        ) {
            val scores = result.exerciseScores
            if (scores.isEmpty()) return@Canvas

            val maxScore = scores.maxOrNull() ?: 1
            val barWidth = size.width / (scores.size * 2)

            scores.forEachIndexed { index, score ->
                val barHeight = (score / maxScore.toFloat()) * size.height
                drawRect(
                    color = androidx.compose.ui.graphics.Color(0xFF4CAF50),
                    topLeft = Offset(
                        x = index * barWidth * 2 + barWidth / 2,
                        y = size.height - barHeight
                    ),
                    size = Size(barWidth, barHeight)
                )
            }
        }
    }
}
