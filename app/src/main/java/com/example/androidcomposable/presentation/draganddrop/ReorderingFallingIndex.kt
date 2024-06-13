package com.example.androidcomposable.presentation.draganddrop

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.androidcomposable.presentation.commons.ListItemContent
import com.example.androidcomposable.presentation.commons.generateItemList

@Composable
fun ReorderingFallingIndex(modifier: Modifier = Modifier) {
    Column(modifier= modifier) {
        var itemList by remember { mutableStateOf(generateItemList()) }

        LazyColumn {
            items(itemList) { item ->
                ListItemFallingIndex(item = item, itemList = itemList) { reorderedList ->
                    itemList = reorderedList
                }
            }
        }
    }
}


@Composable
fun ListItemFallingIndex(
    item: String,
    itemList: MutableList<String>,
    onListReordered: (MutableList<String>) -> Unit
) {
    var dragOffset by remember { mutableStateOf(0f) }

    Card(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
            .offset { IntOffset(x = 0, y = dragOffset.toInt()) }
            .draggable(
                orientation = Orientation.Vertical,
                state = rememberDraggableState { delta ->
                    dragOffset += delta
                },
                onDragStopped = {
                    val draggedIndex = (dragOffset / 80)
                        .toInt()
                        .coerceIn(0, itemList.size - 1)
                    if (draggedIndex != itemList.indexOf(item)) {
                        val reorderedList = itemList.toMutableList()
                        val oldIndex = itemList.indexOf(item)
                        reorderedList.removeAt(oldIndex)
                        reorderedList.add(draggedIndex, item)
                        onListReordered(reorderedList)
                    }
                    dragOffset = 0f
                },
                onDragStarted = {
                    // Chamado quando o usuário inicia o arrastar do item
                }
            )
    ) {
        ListItemContent(item = item)
    }
}