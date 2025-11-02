package com.ec.exercisegamification.ui.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ListComponent(
    itemList: List<String>,
    expandedListItem: String?,
    onItemExpand: (String) -> Unit,
    expandable: Boolean = true,
    actions: @Composable (String) -> List<ListItemAction>,
    expandedContent: (@Composable (String) -> Unit)? = null
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        items(itemList) { item ->
            ListItem(
                itemName = item,
                isExpanded = expandedListItem == item,
                onClick = { if (expandable) onItemExpand(item) },
                actions = actions,
                expandedContent = expandedContent?.let { { it(item) } }
            )
        }
    }
}