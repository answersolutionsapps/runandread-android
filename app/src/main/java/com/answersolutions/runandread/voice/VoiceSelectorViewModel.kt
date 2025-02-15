package com.answersolutions.runandread.voice

import android.speech.tts.Voice
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.answersolutions.runandread.data.repository.VoiceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.Locale
import javax.inject.Inject


fun Locale.languageId(): String {
    return "${this.language}_${this.country}"
}

fun Voice.toRunAndReadVoice(): RunAndReadVoice {
    return RunAndReadVoice(
        name,
        locale.languageId(),
        locale,
        quality,
        latency,
        isNetworkConnectionRequired,
        features
    )
}

fun RunAndReadVoice.toVoice(): Voice {
    return Voice(name, locale, quality, latency, requiresNetworkConnection, features)
}

fun RunAndReadVoice.toLocale(): Locale {
    return language.toLocale()
}

fun RunAndReadVoice.isVoiceNotInstalled(): Boolean {
    return features?.contains("notInstalled") != true
}

fun String.toLocale(): Locale {
    val parts = this.split("_")
    return when (parts.size) {
        1 -> Locale(parts[0]) // Only language (e.g., "en")
        2 -> Locale(parts[0], parts[1]) // Language and country (e.g., "en", "US")
        3 -> Locale(parts[0], parts[1], parts[2]) // Language, country, and variant
        else -> Locale.getDefault() // Fallback to device default
    }
}

data class RunAndReadVoice(
    val name: String,
    val language: String,
    val locale: Locale = language.toLocale(),
    val quality: Int = 0,
    val latency: Int = 0,
    val requiresNetworkConnection: Boolean = false,
    val features: Set<String>? = null
)

@HiltViewModel
class VoiceSelectorViewModel @Inject constructor(
    private val repository: VoiceRepository
) : ViewModel() {

    private val _availableVoices = MutableStateFlow<Set<RunAndReadVoice>>(emptySet())
    val availableVoices: StateFlow<Set<RunAndReadVoice>> = _availableVoices

    private val _availableLocales = MutableStateFlow<Set<Locale>>(emptySet())
    val availableLocales: StateFlow<Set<Locale>> = _availableLocales

    init {
        Timber.d("BookSettingsScreenView.VoiceSelectorViewModel.init=>")
    }

    fun loadVoices() {
        Timber.d("BookSettingsScreenView.loadVoices()=>")
        val start = System.currentTimeMillis()
        viewModelScope.launch {
            val (voices, locales) = withContext(Dispatchers.IO) {
                val fetchedVoices = repository.fetchAvailableVoices()
                val fetchedLocales = repository.getAvailableLocales()
                fetchedVoices to fetchedLocales
            }
            _availableVoices.value = voices
            _availableLocales.value = locales
            Timber.d("loadVoices=>${System.currentTimeMillis() - start}")
        }
    }

    fun nameToVoice(name: String, language: String): RunAndReadVoice {
        return repository.nameToVoice(name, language)
    }

//    fun localeToVoice(locale: Locale): RunAndReadVoice {
//        return repository.localeToVoice(locale)
//    }
//
//    fun languageToLocale(language: String): Locale {
//        return repository.languageToLocale(language)
//    }


//    var availableLocales: Set<Locale> = setOf()
//    var availableVoices: Set<RunAndReadVoice> = setOf()
//
//    fun runAndReadVoices(): List<RunAndReadVoice> {
//        return availableVoices.map {
//            RunAndReadVoice(it.name, it.locale.language)
//        }
//    }
//    init {
//
//    }
//
//    fun loadVoices(context: Context) {
//        Timber.d("loadVoices ---------------->")
//        var textToSpeech: TextToSpeech? = null
//        textToSpeech = TextToSpeech(context) { status ->
//            Timber.d("loadVoices ---------------->$status")
//            if (status == TextToSpeech.SUCCESS) {
//                textToSpeech?.let { tts->
//                    availableVoices = tts.voices.filter { voice ->
//                        !voice.features.contains("notInstalled") && voice.name.endsWith("-language")
//                    }.map { it.toRunAndReadVoice() }.toSet()
//                    availableLocales = availableVoices.map { voice ->
//                        voice.locale
//                    }.toSet()
//                }
//                textToSpeech?.stop()
//                textToSpeech?.shutdown()
//                textToSpeech = null
//            }
//        }
//    }
//
//    fun nameToVoice(name: String, language: String): RunAndReadVoice {
//        try {
//            val voices = if (name.isNotEmpty()) {
//                availableVoices.filter {
//                    it.locale.language == language && (it.name == name)
//                }
//            } else {
//                availableVoices.filter {
//                    it.locale.language == language
//                }
//            }
//            return if (voices.isNotEmpty()) {
//                voices.first()
//            } else if (availableVoices.isNotEmpty()) {
//                availableVoices.first()
//            } else {
//                return Voice("No voices installed", Locale.getDefault(), 5, 5, false, null).toRunAndReadVoice()
//            }
//        } catch (e: Exception) {
//            e.printStackTrace()
//            return Voice("No voices installed", Locale.getDefault(), 5, 5, false, null).toRunAndReadVoice()
//        }
//    }
//
//    fun localeToVoice(locale: Locale): RunAndReadVoice {
//        return availableVoices.first {
//            it.locale == locale
//        }
//    }
//
//    fun languageToLocale(language: String): Locale {
//        val locales = availableLocales.filter { it.toLanguageTag() == language }
//        if (locales.isNotEmpty()) {
//            return locales.first()
//        }
////        val legacy = availableLocales.filter { it.language == language }
////        if (legacy.isNotEmpty()) {
////            return legacy.first()
////        }
//        return Locale.getDefault()
//    }
}