package com.answersolutions.runandread.data.model

import com.answersolutions.extensions.formatSecondsToHMS
import com.answersolutions.runandread.voice.languageId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.*

import android.media.MediaMetadataRetriever

@Serializable
@SerialName("audiobook")
data class AudioBook(
    override val id: String = UUID.randomUUID().toString(),
    override val title: String,
    override val author: String,
    override val language: String = Locale.getDefault().languageId(),
    override val voiceRate: Float,
    override val lastPosition: Int,
    @SerialName("created")
    override val updated: Long,
    override val bookmarks: MutableList<Bookmark> = mutableListOf(),
    val parts: List<TextPart>,
    val audioFilePath: String,
    val voice: String,
    val model: String,
    @SerialName("book_source")
    val bookSource: String
) : RunAndReadBook() {

    override fun playerType(): BookPlayerType = BookPlayerType.AUDIO

    override fun lazyCalculate(completion: () -> Unit) {
        _state.value = _state.value.copy(isCalculating = true)

        coroutineScope.launch(Dispatchers.IO) {
            val duration = calculateDuration()
            val formattedTotalTime = (duration / voiceRate).toDouble().formatSecondsToHMS()
            val formattedElapsedTime = (lastPosition / voiceRate).toDouble().formatSecondsToHMS()

            withContext(Dispatchers.Main) {
                _state.value = BookUIState(
                    isCompleted = duration <= lastPosition,
                    isCalculating = false,
                    progressTime = formattedElapsedTime,
                    totalTime = formattedTotalTime,
                    totalTimeSeconds = (duration / voiceRate).toLong()
                )
                completion()
            }
        }
    }

    private fun calculateDuration(): Int {
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(audioFilePath)
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                ?.toLongOrNull()
                ?.div(1000) // Convert ms to seconds
                ?.toInt() ?: 0
        } catch (e: Exception) {
            e.printStackTrace()
            0
        } finally {
            retriever.release()
        }
    }

    fun getCurrentText(elapsedMilliseconds: Double): AudioBookTextFrame {
        if (parts.isEmpty()) return AudioBookTextFrame("", 0, null, null)

        for (i in parts.indices.reversed()) {
            val part = parts[i]
            val nextPart = parts.getOrNull(i + 1)
            if (part.startTimeMms <= elapsedMilliseconds) {
                return AudioBookTextFrame(
                    text = part.text.replace("\n", ""),
                    startTimeMms = part.startTimeMms,
                    nextStartTime = nextPart?.startTimeMms,
                    nextText = nextPart?.text?.replace("\n", "")
                )
            }
        }
        return AudioBookTextFrame("", 0, null, null)
    }
}

data class AudioBookTextFrame(
    val text: String,
    val startTimeMms: Int,
    val nextStartTime: Int?,
    val nextText: String?
)
