package com.example.androidcomposable.presentation.draganddrop

import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.example.androidcomposable.presentation.commons.ListItemContent
import com.example.androidcomposable.presentation.commons.generateItemList
import com.example.androidcomposable.presentation.commons.move
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue

class MyState {
    var _indexWithOffset: MutableStateFlow<Pair<Int, Float>?> = MutableStateFlow(null)
    var indexWithOffset: StateFlow<Pair<Int, Float>?> = _indexWithOffset.asStateFlow()

    val _itemList: MutableStateFlow<MutableList<String>> = MutableStateFlow(generateItemList())
    val itemList: StateFlow<MutableList<String>> = _itemList.asStateFlow()

    val _diff: MutableStateFlow<Int> = MutableStateFlow(0)
    val diff: StateFlow<Int> = _diff.asStateFlow()

    fun updateIndexWithOffset(new: Pair<Int, Float>?) {
        _indexWithOffset.value = new
    }

    fun updateDiff(new: Int) {
        _diff.value = new
    }
}

@Composable
fun ReorderWhenDrag(myState: MyState, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        val itemList by myState.itemList.collectAsState()
        val diff by myState.diff.collectAsState()
        val indexWithOffset by myState.indexWithOffset.collectAsState()

        val listState: LazyListState = rememberLazyListState()
        var position by remember {
            mutableStateOf<Float?>(null)
        }
        var draggedItem by remember {
            mutableStateOf<Int?>(null)
        }
        val coroutineScope = rememberCoroutineScope()
        var scrollJob by remember { mutableStateOf<Job?>(null) }

        LazyColumn(
            state = listState,
            modifier = Modifier
                .pointerInput(Unit) {
                    detectDragGesturesAfterLongPress(
                        onDragStart = { offset ->
                            listState.layoutInfo.visibleItemsInfo
                                .firstOrNull { offset.y.toInt() in it.offset..it.offset + it.size }
                                ?.also {
                                    position = it.offset + it.size / 2f
                                }
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            position = position?.plus(dragAmount.y)
                            val extendedTouchPadding = Size(400f, 400f)
                            when {
                                change.position.y <= extendedTouchPadding.height -> {
                                    scrollJob = coroutineScope.launch {
                                        while (true) {
                                            listState.animateScrollBy(-200f) // Scroll para cima
                                            delay(50)
                                        }
                                    }
                                }

                                change.position.y >= size.height - extendedTouchPadding.height -> {
                                    scrollJob = coroutineScope.launch {
                                        while (true) {
                                            listState.animateScrollBy(200f) // Scroll para baixo
                                            delay(50)
                                        }
                                    }
                                }

                                else -> scrollJob?.cancel()
                            }
                        },
                        onDragEnd = {
                            myState.updateIndexWithOffset(null)
                            position = null
                            draggedItem = null
                            scrollJob?.cancel()
                        },
                        onDragCancel = {
                            myState.updateIndexWithOffset(null)
                            position = null
                            draggedItem = null
                            scrollJob?.cancel()
                        }
                    )
                }
        ) {
            item { ListItemContent("diff amount $diff") }
            item { ListItemContent("diff amount $diff") }
            coroutineScope.launch {
                snapshotFlow { listState.layoutInfo }
                    .combine(snapshotFlow { position }.distinctUntilChanged()) { state, pos ->
                        pos?.let { draggedCenter ->
                            state.visibleItemsInfo
                                .minByOrNull { (draggedCenter - (it.offset + it.size / 2f)).absoluteValue }
                        }?.index
                    }
                    .distinctUntilChanged()
                    .collect { near ->
                        if (near == null || near == draggedItem) return@collect
                        draggedItem?.let { itemList.move(it - diff, near - diff) }
                        draggedItem = near
                    }
            }

            val newIndexWithOffset by derivedStateOf {
                draggedItem
                    ?.let { listState.layoutInfo.visibleItemsInfo.getOrNull(it - listState.firstVisibleItemIndex) }
                    ?.let { Pair(it.index, (position ?: 0f) - it.offset - it.size / 2f) }
            }
            myState.updateIndexWithOffset(newIndexWithOffset)
            myState.updateDiff(listState.layoutInfo.totalItemsCount - itemList.size)

            itemsIndexed(itemList) { idx, item ->
                val offset by remember {
                    derivedStateOf { indexWithOffset?.takeIf { it.first == (idx + diff) }?.second }
                }
                Card(
                    modifier = Modifier
                        .padding(8.dp)
                        .fillMaxWidth()
                        .zIndex(offset?.let { 1f } ?: 0f)
                        .graphicsLayer {
                            translationY = offset ?: 0f
                        }
                ) {
                    ListItemContent(item = item)
                }
            }
        }
    }
}