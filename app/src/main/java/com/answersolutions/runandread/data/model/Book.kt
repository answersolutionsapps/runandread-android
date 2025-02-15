package com.answersolutions.runandread.data.model

import com.answersolutions.extensions.formatSecondsToHMS
import com.answersolutions.runandread.voice.languageId
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import java.util.*

@Serializable
data class Bookmark(
    val position: Int,
    var title: String = ""
)


@Serializable
data class Book(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val author: String,
    val language: String = Locale.getDefault().languageId(),
    val voiceIdentifier: String = "",
    val voiceRate: Float,
    val text: List<String>,
    val lastPosition: Int,
    val created: Long,
    val bookmarks: MutableList<Bookmark> = emptyList<Bookmark>().toMutableList()
) {

    data class BookUIState(
        val isCompleted: Boolean = false,
        val isCalculating: Boolean = false,
        val progressTime: String = "00:00",
        val totalTime: String = "00:00"
    )

    @Transient
    private val _state = MutableStateFlow(BookUIState())
    val viewState: StateFlow<BookUIState> get() = _state.asStateFlow()

    @Transient
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())


    fun lazyCalculate(completed: () -> Unit) {
        _state.value = _state.value.copy(isCalculating = true)

        scope.launch(Dispatchers.IO) {
            val words = text.flatMap { it.split(Regex("\\s+")) }.filter { it.isNotEmpty() }
            val totalSeconds = calculateTotalSeconds(words)
            val elapsedSeconds = calculateElapsedSeconds(words)

            withContext(Dispatchers.Main) {
                _state.value = BookUIState(
                    isCompleted = (lastPosition + 1) >= words.size,
                    isCalculating = false,
                    progressTime = elapsedSeconds.formatSecondsToHMS(),
                    totalTime = totalSeconds.formatSecondsToHMS()
                )
                completed()
            }
        }
    }

    fun calculateTotalSeconds(words: List<String>) =
        (words.joinToString(" ").length * SECONDS_PER_CHARACTER) / voiceRate.toDouble()

    fun calculateElapsedSeconds(words: List<String>) =
        (words.take(lastPosition).joinToString(" ").length * SECONDS_PER_CHARACTER) / voiceRate.toDouble()


    companion object {
        const val SECONDS_PER_CHARACTER = 0.080

        fun withDetails(
            data: Book,
            title: String? = null,
            author: String? = null,
            language: String? = null,
            voiceIdentifier: String? = null,
            voiceRate: Float? = null,
            text: List<String>? = null,
            lastPosition: Int? = null,
            created: Long? = null,
            bookmarks: MutableList<Bookmark>? = null,
        ): Book {
            return Book(
                id = data.id,
                title = title ?: data.title,
                author = author ?: data.author,
                language = language ?: data.language,
                voiceIdentifier = voiceIdentifier ?: data.voiceIdentifier,
                voiceRate = voiceRate ?: data.voiceRate,
                text = text ?: data.text,
                lastPosition = lastPosition ?: data.lastPosition,
                created = created ?: data.created,
                bookmarks = bookmarks ?: data.bookmarks,
            )
        }

        fun stab(): List<Book> {
            return listOf(
                Book(
                    id = "0",
                    title = "Moby Dick",
                    author = "Herman Melville",
                    language = "en",
                    voiceRate = 1.25f,
                    text = listOf("Call me Ishmael.", "Call me Ishmael."),
                    lastPosition = 0,
                    created = System.currentTimeMillis()
                ),
                Book(
                    id = "1",
                    title = "Pride and Prejudice",
                    author = "Jane Austen",
                    language = "en",
                    voiceRate = 1.25f,
                    text = listOf(
                        "Chapter I.. ",
                        "IT is a truth universally acknowledged, that a single man in possession of a good fortune must be in want of a wife.. "
                    ),
                    lastPosition = 1,
                    created = System.currentTimeMillis()
                ),
                Book(
                    id = "2",
                    title = "Tales",
                    author = "Carl Ewald",
                    language = "he",
                    voiceRate = 1.25f,
                    text = listOf(
                        "הנער הקטן וקיבתו..",
                        "היֹֹה היָה נער קטן, שהיתה לו קיבה...",
                        "ואמנם אין בזה מאומה מן החידוש, כי הלא לרובם הגדול של הנערים יש קיבה. כשאני נותן דעתי אל הדבר, אינני סבור שאני מכיר אפילו נער אחד בלי קיבה...",
                        "אבל הנער הזה והקיבה הזאת היו בכל זאת מיוחדים..",
                    ),
                    lastPosition = 0,
                    created = System.currentTimeMillis()
                )
            )
        }
    }
}
