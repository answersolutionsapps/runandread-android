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
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import java.util.*

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
    abstract fun lazyCalculate(completed: () -> Unit)

    data class BookUIState(
        val isCompleted: Boolean = false,
        val isCalculating: Boolean = false,
        val progressTime: String = "00:00",
        val totalTime: String = "00:00"
    )

    @Transient
    protected val _state = MutableStateFlow(BookUIState())
    val viewState: StateFlow<BookUIState> get() = _state.asStateFlow()

    @Transient
    protected val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    companion object {
        fun stab(): List<RunAndReadBook> {
            return listOf(
                AudioBook(
                    id = "0",
                    title = "Moby Dick",
                    author = "Herman Melville",
                    language = "en",
                    voiceRate = 1.25f,
                    parts = listOf(
                        TextPart(0, "Call me Ishmael."),
                        TextPart(1, "Call me Ishmael.")
                    ),
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
                        "Chapter I.. ",
                        "IT is a truth universally acknowledged, that a single man in possession of a good fortune must be in want of a wife.. "
                    ),
                    lastPosition = 1,
                    updated = System.currentTimeMillis()
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
                    updated = System.currentTimeMillis()
                )
            )
        }
    }
}


@Serializable
data class Bookmark(
    val position: Int,
    var title: String = ""
)


@Serializable
@SerialName("textbook")
data class Book(
    override val id: String = UUID.randomUUID().toString(),
    override val title: String,
    override val author: String,
    override val language: String = Locale.getDefault().languageId(),
    val voiceIdentifier: String = "",
    override val voiceRate: Float,
    val text: List<String>,
    override val lastPosition: Int,
    @SerialName("created")
    override val updated: Long,
    override val bookmarks: MutableList<Bookmark> = emptyList<Bookmark>().toMutableList()
) : RunAndReadBook() {

    override fun playerType(): BookPlayerType = BookPlayerType.TTS

    override fun lazyCalculate(completed: () -> Unit) {
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
        (words.take(lastPosition)
            .joinToString(" ").length * SECONDS_PER_CHARACTER) / voiceRate.toDouble()


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
                updated = created ?: data.updated,
                bookmarks = bookmarks ?: data.bookmarks,
            )
        }
    }
}
