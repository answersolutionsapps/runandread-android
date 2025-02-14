package com.answersolutions.runandread.ui.library

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.answersolutions.runandread.data.model.Book
import com.answersolutions.runandread.ui.theme.RunAndReadTheme


@Composable
fun BookItemView(item: Book, onSelect: () -> Unit) {
    var isPressed by remember { mutableStateOf(false) }
    val uiState by item.viewState.collectAsState()

    Row(modifier = Modifier.fillMaxWidth().padding(8.dp).background(MaterialTheme.colorScheme.primary)
        .clickable {
        isPressed = true
        onSelect()
    }) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = if (uiState.isCompleted) {10.dp} else {0.dp})
                .background(MaterialTheme.colorScheme.background)
        ) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp)
            )
            Text(
                text = item.author,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
                    .padding(bottom = 2.dp)
            )

            HorizontalDivider()

            if (uiState.isCalculating) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                        .width(20.dp)
                        .height(20.dp),
                    color = MaterialTheme.colorScheme.primary
                )
            } else {
                if (uiState.isCompleted) {
                    Text(
                        text = "Finished | ${item.language}",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                } else {
                    Text(
                        text = "${uiState.progressTime} of ${uiState.totalTime} | ${item.language}",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }

    // Scale effect for press animation
    Modifier.scale(if (isPressed) 0.95f else 1f)
        .animateContentSize()
        .onGloballyPositioned { coordinates ->
            if (isPressed) {
                // Execute animation or other actions if required
            }
        }
}

@Preview(showBackground = true)
@Composable
fun PreviewBookItemView() {
    RunAndReadTheme {
        Column {
            BookItemView(
                item = Book.stab().first(),
                onSelect = { println("Book selected") }
            )
            HorizontalDivider()
            BookItemView(
                item = Book.stab()[1],
                onSelect = { println("Book selected") }
            )
            HorizontalDivider()
            BookItemView(
                item = Book.stab().last(),
                onSelect = { println("Book selected") }
            )
        }

    }
}
