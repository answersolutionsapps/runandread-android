package com.answersolutions.runandread.data.model

import com.answersolutions.extensions.formatSecondsToHMS
import com.answersolutions.runandread.voice.languageId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.*

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
    override val bookmarks: MutableList<Bookmark> = mutableListOf()
) : RunAndReadBook() {

    override fun playerType(): BookPlayerType = BookPlayerType.TTS

    override fun lazyCalculate(completion: () -> Unit) {
        _state.value = _state.value.copy(isCalculating = true)
        coroutineScope.launch(Dispatchers.IO) {
            val words = text.flatMap { it.split(Regex("\\s+")) }.filter { it.isNotEmpty() }
            val (totalSeconds, wordsCount) = calculateTotalDuration(words)
            val elapsedSeconds = calculateElapsedTime(words, lastPosition)

            withContext(Dispatchers.Main) {
                _state.value = BookUIState(
                    isCompleted = (lastPosition + 1) >= wordsCount,
                    isCalculating = false,
                    progressTime = elapsedSeconds.formatSecondsToHMS(),
                    totalTime = totalSeconds.formatSecondsToHMS(),
                    totalTimeSeconds = wordsCount.toLong() //for text book we measure in words
                )
                completion()
            }
        }
    }

    private fun calculateTotalDuration(words: List<String>): Pair<Double, Int> {

        val totalSeconds = (words.joinToString(" ").length * SECONDS_PER_CHARACTER) / voiceRate.toDouble()
        return Pair(totalSeconds, words.size)
    }


    private fun calculateElapsedTime(words: List<String>, progress: Int): Double {
        return (words.take(progress)
            .joinToString(" ").length * SECONDS_PER_CHARACTER) / voiceRate.toDouble()

    }

    companion object {
        const val SECONDS_PER_CHARACTER = 0.080
    }
}

