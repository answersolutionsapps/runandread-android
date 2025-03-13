package com.answersolutions.runandread.ui.settings

import android.speech.tts.Voice
import com.answersolutions.runandread.data.model.Book
import com.answersolutions.runandread.data.model.RunAndReadBook
import com.answersolutions.runandread.voice.languageId
import com.answersolutions.runandread.voice.RunAndReadVoice
import timber.log.Timber
import java.util.Locale

sealed class BookSettingsEvent {
    data class TitleChanged(val title: String) : BookSettingsEvent()
    data class AuthorChanged(val author: String) : BookSettingsEvent()
    data object Cancel : BookSettingsEvent()
    data object Save : BookSettingsEvent()
    data object DeleteClicked : BookSettingsEvent()
    data class LanguageSelected(val locale: Locale) : BookSettingsEvent()
    data class VoiceSelected(val voice: RunAndReadVoice) : BookSettingsEvent()
    data class SpeedSelected(val speed: Float) : BookSettingsEvent()
    data class PageSelected(val page: Int) : BookSettingsEvent()
    data class PlayVoiceSample(val language: Locale, val voice: Voice, val rate: Float) : BookSettingsEvent()
    data object DismissVoiceErrorDialog : BookSettingsEvent()
}

fun BookSettingsEvent.onEvent(model: BookSettingsViewModel, onNavigateBack: (RunAndReadBook?) -> Unit) {
    Timber.d("BookSettingsScreenView.onEvent=>${this}")
    when (this) {
        is BookSettingsEvent.TitleChanged -> model.updateBookDetails(title = this.title)
        is BookSettingsEvent.AuthorChanged -> model.updateBookDetails(author = this.author)
        BookSettingsEvent.Cancel -> {
            onNavigateBack(null)
        }

        BookSettingsEvent.Save -> {
            model.onSave {
                onNavigateBack(model.bookState.value.book)
            }
        }

        BookSettingsEvent.DeleteClicked -> model.onShowDelete(true)
        is BookSettingsEvent.LanguageSelected -> model.updateBookDetails(language = this.locale.languageId())
        is BookSettingsEvent.VoiceSelected -> model.updateBookDetails(voiceIdentifier = this.voice.name)
        is BookSettingsEvent.SpeedSelected -> model.updateBookDetails(voiceRate = this.speed)
        is BookSettingsEvent.PageSelected -> model.onPageSelected(this.page)
        is BookSettingsEvent.PlayVoiceSample -> model.payTextSample(this.language, this.voice, this.rate)
        is BookSettingsEvent.DismissVoiceErrorDialog -> model.dismissVoiceError()
    }
}