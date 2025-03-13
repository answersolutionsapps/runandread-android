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
import com.answersolutions.runandread.data.model.RunAndReadBook
import com.answersolutions.runandread.ui.theme.RunAndReadTheme
import com.answersolutions.runandread.voice.toLocale


@Composable
fun BookItemView(item: RunAndReadBook, onSelect: () -> Unit) {
    var isPressed by remember { mutableStateOf(false) }
    val uiState by item.viewState.collectAsState()

    Row(modifier = Modifier
        .fillMaxWidth()
        .padding(8.dp)
        .background(MaterialTheme.colorScheme.primary)
        .clickable {
            isPressed = true
            onSelect()
        }) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(
                    start = if (uiState.isCompleted) {
                        10.dp
                    } else {
                        0.dp
                    }
                )
                .background(MaterialTheme.colorScheme.surface)
        ) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp)
            )
            Text(
                text = item.author,
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
                    .padding(bottom = 8.dp)
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
                        text = "Finished | ${item.language.toLocale().displayLanguage}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
                    )
                } else {
                    Text(
                        text = "${uiState.progressTime} of ${uiState.totalTime} | ${item.language.toLocale().displayLanguage}",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
                    )
                }
            }
        }
    }

    // Scale effect for press animation
    Modifier
        .scale(if (isPressed) 0.95f else 1f)
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
        Column(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {
            BookItemView(
                item = RunAndReadBook.stab().first().apply {
                    lazyCalculate {}
                },
                onSelect = { println("Book selected") }
            )
            HorizontalDivider()
            BookItemView(
                item = RunAndReadBook.stab()[1],
                onSelect = { println("Book selected") }
            )
            HorizontalDivider()
            BookItemView(
                item = RunAndReadBook.stab().last(),
                onSelect = { println("Book selected") }
            )
        }

    }
}
