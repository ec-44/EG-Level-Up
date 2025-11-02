package com.ec.exercisegamification.ui.components

import androidx.compose.ui.graphics.painter.Painter

data class ListItemAction(
    val onClick: () -> Unit,
    val icon: Painter,
    val contentDescription: String,
)
