package com.answersolutions.runandread.ui.player

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.answersolutions.runandread.data.model.Book
import com.answersolutions.runandread.data.model.Bookmark
import com.answersolutions.runandread.ui.components.NiceRoundButton
import com.answersolutions.runandread.ui.theme.RunAndReadTheme
import com.answersolutions.runandread.ui.theme.doubleLargeSpace
import com.answersolutions.runandread.ui.theme.largeSpace
import com.answersolutions.runandread.ui.theme.normalSpace
import com.answersolutions.runandread.ui.theme.smallSpace
import com.answersolutions.runandread.voice.toLocale
import java.util.Locale

@Preview(showBackground = true)
@Composable
fun PlayerScreenPreview() {
    RunAndReadTheme(darkTheme = true) {
        PlayerScreenContent(selectedBook = Book.stab().first(),
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
            currentFrame = listOf("Test 1", "Test 2", "Test 3"),
            currentWordIndexInFrame = 1,
            isSpeaking = true,
            progressTime = "00:00",
            progress = 100f,
            sliderRange = 0f..1000f,
            totalTime = "00:00",
            onSliderValueChange = {},
            onPlayClick = {

            },
            onFastForward = {},
            onFastRewind = {},
            onAddBookmark = {},
            onBackToLibrary = {
            }, onSettings = {
            },
            onBookmarkClick = { position ->

            }, onDeleteBookmark = {})
    }
}

@Composable
fun PlayerScreenView(
    onBackToLibrary: () -> Unit,
    onSettings: (Book) -> Unit,
    viewModel: PlayerViewModel,
    onPlayback: (Float) -> Unit
) {

    LaunchedEffect(Unit) {
        viewModel.setUpBook()
    }

    val uiState = viewModel.viewState.collectAsState()
    val highlightingState = viewModel.highlightingState.collectAsState()
    val isSpeaking = uiState.value.isSpeaking
    val progress = uiState.value.progress
    val progressTime = uiState.value.progressTime
    val totalTimeString = uiState.value.totalTimeString
    val spokenTextRange = uiState.value.spokenTextRange
    val bookmarks = uiState.value.bookmarks

    val currentFrame = highlightingState.value.currentFrame
    val currentWordIndexInFrame = highlightingState.value.currentWordIndexInFrame


    PlayerScreenContent(
        selectedBook = viewModel.selectedBook,
        bookmarks = bookmarks,
        currentFrame = currentFrame,
        currentWordIndexInFrame = currentWordIndexInFrame,
        isSpeaking = isSpeaking,
        progressTime = progressTime,
        progress = progress,
        sliderRange = viewModel.sliderRange(),
        totalTime = totalTimeString,
        onSliderValueChange = { viewModel.updatePosition(it) },
        onPlayClick = {
            if (viewModel.isSpeaking()) {
                viewModel.stopSpeaking()
                viewModel.stopPlaybackService()
            } else {
                viewModel.speak()
                viewModel.startPlaybackService()
            }
            onPlayback(0f)
        },
        onFastForward = {
            viewModel.fastForward()
        },
        onFastRewind = {
            viewModel.fastRewind()
        },
        onAddBookmark = { viewModel.saveBookmark() },
        onBackToLibrary = onBackToLibrary,
        onSettings = {
            viewModel.selectedBook?.let(onSettings)
        },
        onBookmarkClick = { position ->
            viewModel.playFromBookmark(position.toInt())
        },
        onDeleteBookmark = { bookmark ->
            viewModel.deleteBookmark(bookmark)
        }
    )

    val lifecycleOwner = LocalLifecycleOwner.current
    val lifecycleObserver = remember(lifecycleOwner) {
        LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> {
                    // Handle onAppear event
                }

                Lifecycle.Event.ON_STOP -> {
                    // Handle onDisappear event
                    viewModel.saveBookChanges()
                }

                else -> {}
            }
        }
    }
    DisposableEffect(lifecycleOwner) {
        lifecycleOwner.lifecycle.addObserver(lifecycleObserver)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(lifecycleObserver)
            viewModel.stopSpeaking()
            viewModel.saveBookChanges()
        }
    }

}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun PlayerScreenContent(
    selectedBook: Book?,
    bookmarks: List<Bookmark>,
    currentFrame: List<String>,
    currentWordIndexInFrame: Int,
    isSpeaking: Boolean,
    progressTime: String,
    progress: Float,
    sliderRange: ClosedFloatingPointRange<Float>,
    totalTime: String,
    onSliderValueChange: (Float) -> Unit,
    onPlayClick: () -> Unit,
    onFastForward: () -> Unit,
    onFastRewind: () -> Unit,
    onAddBookmark: () -> Unit,
    onBackToLibrary: () -> Unit,
    onSettings: () -> Unit,
    onBookmarkClick: (position: Float) -> Unit,
    onDeleteBookmark: (Bookmark) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                actions = {
                    Text("Library",
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .padding(16.dp)
                            .clickable {
                                onBackToLibrary()
                            })
                    Spacer(modifier = Modifier.weight(1F))
                    Text("Edit",
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .padding(16.dp)
                            .clickable {
                                onSettings()
                            })
                }
            )
        },
        content = { padding ->
            var selectedBookmark by remember { mutableStateOf<Bookmark?>(null) }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                if (bookmarks.isNotEmpty()) {
                    Column(modifier = Modifier.padding(horizontal = largeSpace)) {
                        LazyColumn {
                            items(bookmarks) { bookMark ->
                                var showDelete by remember { mutableStateOf(false) }
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .combinedClickable(
                                            onClick = { onBookmarkClick(bookMark.position.toFloat()) },
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
                                                onDeleteBookmark(bookMark)
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

                Spacer(modifier = Modifier.weight(1f))
                HorizontalDivider()
            }
        },
        bottomBar = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                selectedBook?.let {
                    Spacer(modifier = Modifier.padding(vertical = normalSpace))
                    Text(
                        it.title,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Spacer(modifier = Modifier.padding(vertical = smallSpace))
                    Text(
                        it.author,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Column(
                        modifier = Modifier.padding(
                            horizontal = doubleLargeSpace,
                            vertical = largeSpace
                        )
                    ) {
                        Slider(
                            value = progress,
                            valueRange = sliderRange,
                            onValueChange = onSliderValueChange,
                            colors = SliderDefaults.colors(
                                thumbColor = colorScheme.primary,
                                activeTrackColor = colorScheme.primary
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Row {
                            Text(
                                text = progressTime,
                                maxLines = 1,
                                color = colorScheme.tertiary,
                                fontWeight = FontWeight.Normal
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            Text(
                                text = totalTime,
                                maxLines = 1,
                                color = colorScheme.tertiary,
                                fontWeight = FontWeight.Normal
                            )
                        }
                    }
                } ?: run {
                    Column(
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Loading..", textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.headlineMedium
                        )
                    }
                }
                HorizontalDivider()
                HorizontallyScrolledTextView(
                    words = currentFrame,
                    index = currentWordIndexInFrame,
                    language = selectedBook?.language?.toLocale() ?: Locale.getDefault()
                )
                HorizontalDivider()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(largeSpace),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    NiceRoundButton(
                        contentDescription = "Fast Rewind",
                        icon = Icons.Filled.FastRewind,
                        diameter = 44.dp,
                        clickHandler = onFastRewind
                    )
                    Spacer(modifier = Modifier.width(smallSpace))
                    NiceRoundButton(
                        contentDescription = "Play and Pause",
                        icon = if (isSpeaking) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        backgroundColor = colorScheme.primary,
                        diameter = 64.dp,
                        scale = 2f,
                        clickHandler = onPlayClick
                    )
                    Spacer(modifier = Modifier.width(smallSpace))
                    NiceRoundButton(
                        contentDescription = "Fast Forward",
                        icon = Icons.Filled.FastForward,
                        diameter = 44.dp,
                        clickHandler = onFastForward
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = largeSpace, start = largeSpace),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    NiceRoundButton(
                        enabled = isSpeaking,
                        contentDescription = "Bookmark",
                        icon = Icons.Filled.BookmarkAdd,
                        diameter = 44.dp,
                        clickHandler = onAddBookmark
                    )
                    Spacer(modifier = Modifier.width(smallSpace))

                }
            }

        }
    )
}