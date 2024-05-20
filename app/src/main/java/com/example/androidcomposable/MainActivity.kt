package com.example.androidcomposable

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.consumeAllChanges
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.example.androidcomposable.ui.theme.AndroidComposableTheme
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AndroidComposableTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        TopAppBar(
                            title = { Text(text = "Home") },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                titleContentColor = MaterialTheme.colorScheme.secondary,
                            ),
                        )
                    },

                    ) { innerPadding ->
                    HomeContent(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeContent(modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Spacer(modifier = Modifier.height(50.dp))

//        CommonDragItem()
//        ReorderingFallingIndex()
        ReorderWhenDrag(_uiState)
    }
}

@Composable
fun CommonDragItem() {
    Box(modifier = Modifier.fillMaxSize()) {
        var offsetX by remember { mutableFloatStateOf(100f) }
        var offsetY by remember { mutableFloatStateOf(100f) }

        Box(
            Modifier
                .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
                .background(Color.Blue)
                .size(50.dp)
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        offsetX += dragAmount.x
                        offsetY += dragAmount.y
                    }
                }
        )
    }
}

@Composable
fun ReorderingFallingIndex() {
    var itemList by remember { mutableStateOf(generateItemList()) }

    LazyColumn {
        items(itemList) { item ->
            ListItemFallingIndex(item = item, itemList = itemList) { reorderedList ->
                itemList = reorderedList
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

@Composable
fun ListItemContent(item: String) {
    // Conteúdo do item da lista
    Text(
        text = item,
        modifier = Modifier.padding(16.dp)
    )
}

fun generateItemList(): MutableList<String> {
    // Gera uma lista de itens de exemplo
    return MutableList(20) { index -> "Item $index" }
}


fun <T> MutableList<T>.move(fromIdx: Int, toIdx: Int) {
    if (toIdx > fromIdx) {
        for (i in fromIdx until toIdx) {
            this[i] = this[i + 1].also { this[i + 1] = this[i] }
        }
    } else {
        for (i in fromIdx downTo toIdx + 1) {
            this[i] = this[i - 1].also { this[i - 1] = this[i] }
        }
    }
}

class State {
    var _indexWithOffset: MutableStateFlow<Pair<Int, Float>?> = MutableStateFlow(null)
    var indexWithOffset: StateFlow<Pair<Int, Float>?> = _indexWithOffset.asStateFlow()

    val _itemList: MutableStateFlow<MutableList<String>> = MutableStateFlow(generateItemList())
    val itemList: StateFlow<MutableList<String>> = _itemList.asStateFlow()

    fun updateIndexWithOffset(new: Pair<Int, Float>?) {
        _indexWithOffset.value = new
    }
}

private val _uiState = State()

@Composable
fun ReorderWhenDrag(state: State) {
    val itemList by state.itemList.collectAsState()
    val indexWithOffset by state.indexWithOffset.collectAsState()

    val listState: LazyListState = rememberLazyListState()
    var position by remember {
        mutableStateOf<Float?>(null)
    }
    var draggedItem by remember {
        mutableStateOf<Int?>(null)
    }
    val coroutineScope = rememberCoroutineScope()

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
                        change.consumeAllChanges()
                        position = position?.plus(dragAmount.y)
                        // Start autoscrolling if position is out of bounds
                    },
                    onDragEnd = {
                        state.updateIndexWithOffset(null)
                        position = null
                        draggedItem = null
                    }
                )
            }
    ) {
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

                    draggedItem = when {
                        near == null -> null
                        draggedItem == null -> near
                        else -> near.also {
                            itemList.move(draggedItem!!, it)
                        }
                    }
                }
        }

        val newIndexWithOffset by derivedStateOf {
            draggedItem
                ?.let { listState.layoutInfo.visibleItemsInfo.getOrNull(it - listState.firstVisibleItemIndex) }
                ?.let { Pair(it.index, (position ?: 0f) - it.offset - it.size / 2f) }
        }
        state.updateIndexWithOffset(newIndexWithOffset)



        itemsIndexed(itemList) { idx, item ->
            val offset by remember {
                derivedStateOf { indexWithOffset?.takeIf { it.first == idx }?.second }
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