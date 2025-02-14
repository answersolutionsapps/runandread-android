package com.answersolutions.runandread.data.datasource

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.Voice
import com.answersolutions.runandread.voice.RunAndReadVoice
import com.answersolutions.runandread.voice.toRunAndReadVoice
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.Locale
import javax.inject.Inject
import kotlin.coroutines.resume

class VoiceDataSource @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private var textToSpeech: TextToSpeech? = null
    private var availableVoices: Set<Voice> = setOf()

    suspend fun loadVoices(): Set<RunAndReadVoice> = withContext(Dispatchers.IO) {
        return@withContext suspendCancellableCoroutine { continuation ->
            textToSpeech = TextToSpeech(context) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    textToSpeech?.let { tts ->
                        availableVoices = tts.voices.filter { voice ->
                            !voice.features.contains("notInstalled") //&& voice.name.endsWith("-language")
                        }.toSet()
                    }
                }
                textToSpeech?.shutdown()
                textToSpeech = null
                continuation.resume(availableVoices.map { it.toRunAndReadVoice() }.toSet())
            }
        }
    }

    fun getAvailableLocales(): Set<Locale> {
        return availableVoices.map { it.locale }.toSet()
    }
}
