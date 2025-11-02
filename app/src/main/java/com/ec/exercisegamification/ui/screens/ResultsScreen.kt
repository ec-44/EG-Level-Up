package com.ec.exercisegamification.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.dp

@Composable
fun ResultsScreen(
    totalScore: Int,
    maxMultiplier: Int,
    exerciseScores: List<Int>,
    onFinish: () -> Unit
) {
    Scaffold { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(
                text = "Routine Complete!",
                style = MaterialTheme.typography.headlineMedium
            )

            Text("Final Score: $totalScore")
            Text("Max Multiplier: x$maxMultiplier")

            Text("Performance by Exercise")

            // Simple bar graph
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .padding(vertical = 8.dp)
            ) {
                if (exerciseScores.isEmpty()) return@Canvas
                val maxScore = exerciseScores.maxOrNull() ?: 1
                val barWidth = size.width / (exerciseScores.size * 2)
                exerciseScores.forEachIndexed { index, score ->
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

            Button(onClick = onFinish) {
                Text("OK")
            }
        }
    }
}