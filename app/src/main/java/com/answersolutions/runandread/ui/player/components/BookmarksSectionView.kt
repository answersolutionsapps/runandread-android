package com.answersolutions.runandread.ui.player.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.answersolutions.runandread.data.model.Bookmark
import com.answersolutions.runandread.ui.player.PlayerEvent
import com.answersolutions.runandread.ui.player.PlayerViewModel
import com.answersolutions.runandread.ui.theme.RunAndReadTheme
import com.answersolutions.runandread.ui.theme.largeSpace

@Preview(showBackground = true)
@Composable
fun BookmarksSectionViewPreview() {
    RunAndReadTheme(darkTheme = true) {
        BookmarksSectionView(uiState = PlayerViewModel.PlayerUIState(
            bookmarks = listOf(
                Bookmark(1, "Test 1 Test 1 Test 1 Test 1 Test 1 Test 1  Test 1"),
                Bookmark(2, "Test 2"),
                Bookmark(3, "Test 3"),
                Bookmark(4, "Test 4"),
                Bookmark(5, "Test 5"),
                Bookmark(6, "Test 6"),
                Bookmark(7, "Test 7"),
                Bookmark(8, "Test 8"),
                Bookmark(9, "Test 9"),
                Bookmark(9, "Test 9"),
                Bookmark(9, "Test 9"),
                Bookmark(9, "Test 9"),
                Bookmark(9, "Test 9")
            ),
            isSpeaking = true,
            progressTime = "00:00",
            progress = 100f,
            totalTimeString = "00:00"
        ), onEvent = {})
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BookmarksSectionView(
    uiState: PlayerViewModel.PlayerUIState,
    onEvent: (PlayerEvent) -> Unit,
) {
    var selectedBookmark by remember { mutableStateOf<Bookmark?>(null) }
    Column(modifier = Modifier.padding(horizontal = largeSpace)) {
        LazyColumn {
            items(uiState.bookmarks) { bookMark ->
                var showDelete by remember { mutableStateOf(false) }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .combinedClickable(
                            onClick = {
                                onEvent(PlayerEvent.BookmarkClick(bookMark.position.toFloat()))
                            },
                            onLongClick = {
                                selectedBookmark = bookMark
                                showDelete = true
                            }
                        )
                        .padding(horizontal = 8.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = bookMark.title,
                        maxLines = 2,
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.weight(1f)
                    )
                    if (showDelete && selectedBookmark == bookMark) {
                        VerticalDivider()
                        IconButton(
                            onClick = {
                                onEvent(PlayerEvent.DeleteBookmark(bookMark))
                                showDelete = false
                            },
                            modifier = Modifier
                                .align(Alignment.CenterVertically)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = colorScheme.error,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
                HorizontalDivider()
            }
        }
    }
}