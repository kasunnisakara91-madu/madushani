package com.example.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class VoiceRecognitionManager(
    private val context: Context,
    private val onResultRecognized: (String) -> Unit
) {

    companion object {
        private const val TAG = "VoiceRecognition"
    }

    private var speechRecognizer: SpeechRecognizer? = null

    private val _voiceState = MutableStateFlow<VoiceState>(VoiceState.Idle)
    val voiceState: StateFlow<VoiceState> = _voiceState.asStateFlow()

    private val _rmsAmplitude = MutableStateFlow(0f)
    val rmsAmplitude: StateFlow<Float> = _rmsAmplitude.asStateFlow()

    private val _partialText = MutableStateFlow("")
    val partialText: StateFlow<String> = _partialText.asStateFlow()

    val isAvailable: Boolean
        get() = SpeechRecognizer.isRecognitionAvailable(context)

    fun startListening(languageMode: String = "mixed") {
        if (!isAvailable) {
            _voiceState.value = VoiceState.Error("Speech recognition is not supported on this device.")
            return
        }

        stopListening()

        try {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(createListener())
            }

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)

                when (languageMode.lowercase()) {
                    "sinhala" -> {
                        putExtra(RecognizerIntent.EXTRA_LANGUAGE, "si-LK")
                        putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "si-LK")
                    }
                    "english" -> {
                        putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US")
                        putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "en-US")
                    }
                    else -> {
                        // Mixed: default to current locale with Sinhala/English fallbacks
                        val currentLocale = Locale.getDefault().toLanguageTag()
                        putExtra(RecognizerIntent.EXTRA_LANGUAGE, currentLocale)
                        putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "si-LK,en-US")
                    }
                }
            }

            _partialText.value = ""
            _voiceState.value = VoiceState.Listening
            speechRecognizer?.startListening(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start listening", e)
            _voiceState.value = VoiceState.Error("Could not start microphone: ${e.message}")
        }
    }

    fun stopListening() {
        try {
            speechRecognizer?.stopListening()
            speechRecognizer?.cancel()
            speechRecognizer?.destroy()
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping recognizer", e)
        } finally {
            speechRecognizer = null
            _rmsAmplitude.value = 0f
            if (_voiceState.value is VoiceState.Listening) {
                _voiceState.value = VoiceState.Idle
            }
        }
    }

    private fun createListener(): RecognitionListener {
        return object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                _voiceState.value = VoiceState.Listening
            }

            override fun onBeginningOfSpeech() {
                _voiceState.value = VoiceState.Listening
            }

            override fun onRmsChanged(rmsdB: Float) {
                // Normalize rmsdB (-2 to 10 typical) to 0.0f .. 1.0f range for animations
                val normalized = ((rmsdB + 2f) / 12f).coerceIn(0f, 1f)
                _rmsAmplitude.value = normalized
            }

            override fun onBufferReceived(buffer: ByteArray?) {}

            override fun onEndOfSpeech() {
                _voiceState.value = VoiceState.Processing
                _rmsAmplitude.value = 0f
            }

            override fun onError(error: Int) {
                _rmsAmplitude.value = 0f
                val errorMessage = when (error) {
                    SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
                    SpeechRecognizer.ERROR_CLIENT -> "Speech recognition client error"
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission required"
                    SpeechRecognizer.ERROR_NETWORK -> "Network connection error for speech"
                    SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout while recognizing"
                    SpeechRecognizer.ERROR_NO_MATCH -> "No speech recognized. Try again"
                    SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Speech service busy"
                    SpeechRecognizer.ERROR_SERVER -> "Recognition server error"
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Speech timeout. No input heard"
                    else -> "Speech recognition error ($error)"
                }
                Log.w(TAG, "SpeechRecognizer error: $errorMessage")
                if (error == SpeechRecognizer.ERROR_NO_MATCH || error == SpeechRecognizer.ERROR_SPEECH_TIMEOUT) {
                    _voiceState.value = VoiceState.Idle
                } else {
                    _voiceState.value = VoiceState.Error(errorMessage)
                }
            }

            override fun onResults(results: Bundle?) {
                _rmsAmplitude.value = 0f
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val recognized = matches?.firstOrNull()?.trim() ?: ""
                if (recognized.isNotEmpty()) {
                    _partialText.value = recognized
                    _voiceState.value = VoiceState.Processing
                    onResultRecognized(recognized)
                } else {
                    _voiceState.value = VoiceState.Idle
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {
                val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val text = matches?.firstOrNull()?.trim() ?: ""
                if (text.isNotEmpty()) {
                    _partialText.value = text
                }
            }

            override fun onEvent(eventType: Int, params: Bundle?) {}
        }
    }
}
