package com.answersolutions.runandread.data.repository

import android.speech.tts.Voice
import com.answersolutions.runandread.data.datasource.VoiceDataSource
import com.answersolutions.runandread.voice.RunAndReadVoice
import com.answersolutions.runandread.voice.languageId
import com.answersolutions.runandread.voice.toRunAndReadVoice
import java.util.Locale
import javax.inject.Inject

class VoiceRepository @Inject constructor(
    private val voiceDataSource: VoiceDataSource
) {
    private var availableVoices: Set<RunAndReadVoice> = setOf()

    suspend fun fetchAvailableVoices(): Set<RunAndReadVoice> {
        availableVoices = voiceDataSource.loadVoices()
        return availableVoices
    }

    fun getAvailableLocales(): Set<Locale> {
        return voiceDataSource.getAvailableLocales()
    }

    fun nameToVoice(name: String, language: String): RunAndReadVoice {
        val voices = availableVoices.filter { it.locale.languageId() == language && it.name == name }
        return voices.firstOrNull() ?: defaultVoice()
    }

    fun localeToVoice(locale: Locale): RunAndReadVoice {
        return availableVoices.firstOrNull { it.locale.languageId() == locale.languageId() } ?: defaultVoice()
    }

    fun languageToLocale(language: String): Locale {
        return getAvailableLocales().firstOrNull { it.languageId() == language } ?: Locale.getDefault()
    }

    private fun defaultVoice(): RunAndReadVoice {
        return Voice("No voices installed", Locale.getDefault(), 5, 5, false, null).toRunAndReadVoice()
    }
}
