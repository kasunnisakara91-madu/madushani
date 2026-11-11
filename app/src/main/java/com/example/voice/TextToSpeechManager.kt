package com.example.voice

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale
import java.util.UUID

class TextToSpeechManager(
    private val context: Context,
    private val onInitComplete: ((Boolean) -> Unit)? = null
) {
    companion object {
        private const val TAG = "TextToSpeechManager"
    }

    private var tts: TextToSpeech? = null
    private var isInitialized = false

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private var currentRate: Float = 1.0f
    private var currentPitch: Float = 1.0f

    init {
        tts = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                isInitialized = true
                setupUtteranceListener()
                tts?.setSpeechRate(currentRate)
                tts?.setPitch(currentPitch)
                Log.d(TAG, "TTS initialized successfully.")
                onInitComplete?.invoke(true)
            } else {
                Log.e(TAG, "TTS initialization failed with status $status")
                onInitComplete?.invoke(false)
            }
        }
    }

    private fun setupUtteranceListener() {
        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                _isSpeaking.value = true
            }

            override fun onDone(utteranceId: String?) {
                _isSpeaking.value = false
            }

            override fun onError(utteranceId: String?) {
                _isSpeaking.value = false
                Log.e(TAG, "TTS error on utterance $utteranceId")
            }
        })
    }

    fun setSpeechRate(rate: Float) {
        currentRate = rate
        if (isInitialized) {
            tts?.setSpeechRate(rate)
        }
    }

    fun setPitch(pitch: Float) {
        currentPitch = pitch
        if (isInitialized) {
            tts?.setPitch(pitch)
        }
    }

    fun speak(text: String, onDone: (() -> Unit)? = null) {
        if (!isInitialized || text.isBlank()) {
            onDone?.invoke()
            return
        }

        try {
            val hasSinhala = text.any { it in '\u0D80'..'\u0DFF' }
            if (hasSinhala) {
                val sinhalaLocale = Locale("si", "LK")
                val result = tts?.setLanguage(sinhalaLocale)
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.w(TAG, "Sinhala TTS not supported natively on this device TTS engine. Falling back to default.")
                    tts?.setLanguage(Locale.getDefault())
                }
            } else {
                tts?.setLanguage(Locale.US)
            }

            val utteranceId = UUID.randomUUID().toString()
            _isSpeaking.value = true

            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
        } catch (e: Exception) {
            Log.e(TAG, "Exception during TTS speak", e)
            _isSpeaking.value = false
            onDone?.invoke()
        }
    }

    fun stop() {
        try {
            tts?.stop()
            _isSpeaking.value = false
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping TTS", e)
        }
    }

    fun shutdown() {
        try {
            tts?.stop()
            tts?.shutdown()
            tts = null
            isInitialized = false
            _isSpeaking.value = false
        } catch (e: Exception) {
            Log.e(TAG, "Error shutting down TTS", e)
        }
    }
}
