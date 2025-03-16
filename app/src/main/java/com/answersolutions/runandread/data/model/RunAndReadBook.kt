package com.answersolutions.runandread.data.model

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

sealed class BookPlayerType {
    data object TTS : BookPlayerType()
    data object AUDIO : BookPlayerType()
}

@Serializable
sealed class RunAndReadBook {
    abstract val id: String
    abstract val title: String
    abstract val author: String
    abstract val language: String
    abstract val voiceRate: Float
    abstract val lastPosition: Int
    abstract val updated: Long
    abstract val bookmarks: MutableList<Bookmark>

    abstract fun playerType(): BookPlayerType
    abstract fun lazyCalculate(completion: () -> Unit)

    @Serializable
    data class BookUIState(
        val isCompleted: Boolean = false,
        val isCalculating: Boolean = true,
        val progressTime: String = "00:00",
        val totalTime: String = "00:00",
        val totalTimeSeconds: Long = 0
    )

    @Transient
    protected val _state = MutableStateFlow(BookUIState())
    val viewState: StateFlow<BookUIState> get() = _state.asStateFlow()

    @Transient
    protected val coroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    companion object {
        fun sampleBooks(): List<RunAndReadBook> = listOf(
            AudioBook(
                id = "0",
                title = "Moby Dick",
                author = "Herman Melville",
                language = "en",
                voiceRate = 1.25f,
                parts = listOf(TextPart(0, "Call me Ishmael.")),
                lastPosition = 0,
                updated = System.currentTimeMillis(),
                audioFilePath = "",
                voice = "",
                model = "",
                bookSource = ""
            ),
            Book(
                id = "1",
                title = "Pride and Prejudice",
                author = "Jane Austen",
                language = "en",
                voiceRate = 1.25f,
                text = listOf(
                    "Chapter I...",
                    "It is a truth universally acknowledged..."
                ),
                lastPosition = 1,
                updated = System.currentTimeMillis()
            )
        )
    }
}
