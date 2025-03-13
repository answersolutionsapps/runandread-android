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

fun AudioBook.durationInSeconds(): Int {
    val retriever = MediaMetadataRetriever()
    return try {
        retriever.setDataSource(audioFilePath)
        val durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
        val durationMs = durationStr?.toLongOrNull() ?: 0L
        (durationMs / 1000).toInt() // Convert milliseconds to seconds
    } catch (e: Exception) {
        e.printStackTrace()
        0 // Return 0 if there's an error
    } finally {
        retriever.release()
    }
}

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
    override val bookmarks: MutableList<Bookmark> = emptyList<Bookmark>().toMutableList(),
    val parts: List<TextPart>,
    val audioFilePath: String,
    val voice: String,
    val model: String,
    @SerialName("book_source")
    val bookSource: String
): RunAndReadBook() {

    override fun playerType(): BookPlayerType = BookPlayerType.AUDIO

    override fun lazyCalculate(completed: () -> Unit) {
        _state.value = _state.value.copy(isCalculating = true)

        scope.launch(Dispatchers.IO) {
            val d = durationInSeconds()
            withContext(Dispatchers.Main) {
                _state.value = BookUIState(
                    isCompleted = d == lastPosition,
                    isCalculating = false,
                    progressTime = lastPosition.toDouble().formatSecondsToHMS(),
                    totalTime = d.toDouble().formatSecondsToHMS()
                )
                completed()
            }
        }
    }
}
