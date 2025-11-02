package com.ec.exercisegamification.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun ListActionRow(
    itemName: String,
    actions: @Composable (String) -> List<ListItemAction>,
    ) {

    val itemActions = actions(itemName)

    Row(
        modifier = Modifier.fillMaxWidth()
    ) {
        itemActions.forEach { action ->
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                IconButton(onClick = action.onClick) {
                    Icon(
                        painter = action.icon,
                        contentDescription = action.contentDescription
                    )
                }
            }
        }
    }
}