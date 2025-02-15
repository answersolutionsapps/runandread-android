package com.answersolutions.runandread.voice

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import timber.log.Timber
import java.util.*

interface SimpleSpeakingCallBack {
    fun onError(utteranceId: String?, errorCode: Int)
}

class SimpleSpeechProvider(
    private val context: Context,
    private var currentLocale: Locale = Locale.getDefault(),
    private var currentVoice: Voice,
    private var speechRate: Float = 1.0f,
    private val speakingCallBack: SimpleSpeakingCallBack?
) {

    private val speechListener = object : UtteranceProgressListener() {
        override fun onRangeStart(utteranceId: String?, start: Int, end: Int, frame: Int) {
        }

        override fun onStart(utteranceId: String?) {
        }

        override fun onDone(utteranceId: String?) {
        }

        @Deprecated(
            "Deprecated in Java",
            ReplaceWith("Timber.d(\"textToSpeech=> onError=>\$p0\")", "timber.log.Timber")
        )
        override fun onError(p0: String?) {
            Timber.d("textToSpeech=> onError=>$p0")
            speakingCallBack?.onError(null, 0)
        }

        override fun onError(utteranceId: String?, errorCode: Int) {
            super.onError(utteranceId, errorCode)
            speakingCallBack?.onError(utteranceId, errorCode)
        }

        override fun onStop(utteranceId: String?, interrupted: Boolean) {
            super.onStop(utteranceId, interrupted)
        }
    }

    private lateinit var textToSpeech: TextToSpeech

    init {
        initSpeechProvider()
    }

    private fun initSpeechProvider() {
        textToSpeech = TextToSpeech(context) { status ->
            Timber.d("textToSpeech.init=>$status")
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
        Timber.d("textToSpeech.stop()=>${textToSpeech.isSpeaking}")
        textToSpeech.stop()
    }

    fun isSpeaking(): Boolean {
        return textToSpeech.isSpeaking
    }
}