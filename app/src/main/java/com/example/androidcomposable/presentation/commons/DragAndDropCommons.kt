package com.example.androidcomposable.presentation.commons

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

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
    if (fromIdx == toIdx || fromIdx < 0 || toIdx < 0 || fromIdx >= size || toIdx >= size) return

    val item = removeAt(fromIdx)
    add(toIdx, item)
}