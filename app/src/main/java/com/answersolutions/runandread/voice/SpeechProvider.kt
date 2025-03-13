package com.answersolutions.runandread.voice

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import timber.log.Timber
import java.util.*

interface SpeakingCallBack {
    fun onStart()
    fun onStop()
    fun onDone()
}

class SpeechProvider(
    private val context: Context,
    private var currentLocale: Locale = Locale.getDefault(),
    private var currentVoice: Voice,
    private var speechRate: Float = 1.0f,
    private val callBack: (range: IntRange) -> Unit,
    private val speakingCallBack: SpeakingCallBack?
) {

    private val speechListener = object : UtteranceProgressListener() {
        override fun onRangeStart(utteranceId: String?, start: Int, end: Int, frame: Int) {
            spokenTextRange = start..end
            callBack(spokenTextRange)
        }

        override fun onStart(utteranceId: String?) {
            speakingCallBack?.onStart()
        }

        override fun onDone(utteranceId: String?) {
            spokenTextRange = 0..0
            speakingCallBack?.onDone()
        }

        @Deprecated(
            "Deprecated in Java",
            ReplaceWith("Timber.d(\"textToSpeech=> onError=>\$p0\")", "timber.log.Timber")
        )
        override fun onError(p0: String?) {
            Timber.d("textToSpeech=> onError=>$p0")
            speakingCallBack?.onStop()
        }

        override fun onError(utteranceId: String?, errorCode: Int) {
            super.onError(utteranceId, errorCode)
            Timber.d("textToSpeech=> onError=>$errorCode")
            if (errorCode == TextToSpeech.ERROR_NETWORK_TIMEOUT || errorCode == TextToSpeech.ERROR_NETWORK) {
                Timber.e("TTS", "Network error, retrying with offline voice...")
                // Retry with offline voices or notify the user
            }
            speakingCallBack?.onStop()
        }

        override fun onStop(utteranceId: String?, interrupted: Boolean) {
            super.onStop(utteranceId, interrupted)
            speakingCallBack?.onStop()
        }
    }

    private lateinit var textToSpeech: TextToSpeech
    var spokenTextRange = 0..0

    init {
        initSpeechProvider()
    }

    private fun initSpeechProvider() {
        textToSpeech = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech.language = currentLocale
                textToSpeech.voice = currentVoice
                textToSpeech.setSpeechRate(speechRate)
                textToSpeech.setOnUtteranceProgressListener(speechListener)
            }
        }
    }

    fun updateLocale(locale: Locale, voice: Voice, rate: Float) {
        textToSpeech.language = locale
        textToSpeech.voice = voice
        textToSpeech.setSpeechRate(rate)
    }

    fun speak(text: String) {
        val utteranceId = "my_utterance_id"
        val params = Bundle().apply {
            putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceId)
        }

        textToSpeech.speak(
            text,
            TextToSpeech.QUEUE_FLUSH,
            null,
            utteranceId
        )
    }

    fun stop() {
        textToSpeech.stop()
    }

    fun isSpeaking(): Boolean {
        return textToSpeech.isSpeaking
    }
}