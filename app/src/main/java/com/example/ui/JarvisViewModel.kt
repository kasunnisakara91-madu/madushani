package com.example.ui

import android.app.Application
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.db.CommandHistoryEntity
import com.example.data.db.CommandHistoryRepository
import com.example.data.gemini.GeminiRepository
import com.example.data.preferences.JarvisSettings
import com.example.data.preferences.SettingsRepository
import com.example.lockscreen.LockScreenManager
import com.example.parser.CommandParser
import com.example.parser.ParsedAction
import com.example.service.JarvisForegroundService
import com.example.service.PhoneCommandManager
import com.example.voice.TextToSpeechManager
import com.example.voice.VoiceRecognitionManager
import com.example.voice.VoiceState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class JarvisViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext
    private val db = AppDatabase.getDatabase(context)
    val historyRepository = CommandHistoryRepository(db.commandHistoryDao())
    val settingsRepository = SettingsRepository(context)
    val phoneCommandManager = PhoneCommandManager(context)
    val lockScreenManager = LockScreenManager(context)
    val geminiRepository = GeminiRepository()

    val settings: StateFlow<JarvisSettings> = settingsRepository.settingsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = JarvisSettings()
    )

    val historyList: StateFlow<List<CommandHistoryEntity>> = historyRepository.history.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _voiceState = MutableStateFlow<VoiceState>(VoiceState.Idle)
    val voiceState: StateFlow<VoiceState> = _voiceState.asStateFlow()

    private val _rmsAmplitude = MutableStateFlow(0f)
    val rmsAmplitude: StateFlow<Float> = _rmsAmplitude.asStateFlow()

    private val _currentTranscript = MutableStateFlow("")
    val currentTranscript: StateFlow<String> = _currentTranscript.asStateFlow()

    private val _aiResponse = MutableStateFlow("Greetings. I am MADU JARVIS AI. Tap the microphone or hold to speak in Sinhala or English.")
    val aiResponse: StateFlow<String> = _aiResponse.asStateFlow()

    private val _isServiceActive = MutableStateFlow(false)
    val isServiceActive: StateFlow<Boolean> = _isServiceActive.asStateFlow()

    private val _flashlightOn = MutableStateFlow(false)
    val flashlightOn: StateFlow<Boolean> = _flashlightOn.asStateFlow()

    private val _requestUnlockTrigger = MutableStateFlow(false)
    val requestUnlockTrigger: StateFlow<Boolean> = _requestUnlockTrigger.asStateFlow()

    private val voiceRecognitionManager = VoiceRecognitionManager(context) { recognizedText ->
        processCommand(recognizedText)
    }

    val ttsManager = TextToSpeechManager(context)

    init {
        // Collect voice recognizer state
        viewModelScope.launch {
            voiceRecognitionManager.voiceState.collect { state ->
                _voiceState.value = state
            }
        }
        viewModelScope.launch {
            voiceRecognitionManager.rmsAmplitude.collect { amp ->
                _rmsAmplitude.value = amp
            }
        }
        viewModelScope.launch {
            voiceRecognitionManager.partialText.collect { partial ->
                if (partial.isNotEmpty()) {
                    _currentTranscript.value = partial
                }
            }
        }
        viewModelScope.launch {
            settings.collect { s ->
                ttsManager.setSpeechRate(s.ttsRate)
                ttsManager.setPitch(s.ttsPitch)
            }
        }
        checkServiceStatus()
    }

    fun checkServiceStatus() {
        _isServiceActive.value = JarvisForegroundService.isRunning
    }

    fun startListening() {
        val mode = settings.value.languageMode
        ttsManager.stop()
        voiceRecognitionManager.startListening(mode)
    }

    fun stopListening() {
        voiceRecognitionManager.stopListening()
    }

    fun processCommand(input: String) {
        val trimmed = input.trim()
        if (trimmed.isEmpty()) return

        _currentTranscript.value = trimmed
        _voiceState.value = VoiceState.Processing

        viewModelScope.launch {
            val parseResult = CommandParser.parse(trimmed)
            val currentSettings = settings.value
            val isSinhala = parseResult.detectedLanguage == "Sinhala" || trimmed.any { it in '\u0D80'..'\u0DFF' }

            when (val action = parseResult.action) {
                is ParsedAction.RequestUnlock -> {
                    val response = if (isSinhala) {
                        "කරුණාකර ඉදිරියට යාමට ඔබගේ අනන්‍යතාවය තහවුරු කරන්න."
                    } else {
                        "Please authenticate to continue."
                    }
                    _aiResponse.value = response
                    _requestUnlockTrigger.value = true
                    speakIfEnabled(response)
                    historyRepository.logCommand(trimmed, "Unlock", response, parseResult.detectedLanguage, true)
                }

                is ParsedAction.Flashlight -> {
                    val (success, msg) = phoneCommandManager.toggleFlashlight(action.enable)
                    _flashlightOn.value = action.enable && success
                    val reply = parseResult.defaultLocalResponse ?: msg
                    _aiResponse.value = reply
                    speakIfEnabled(reply)
                    historyRepository.logCommand(trimmed, "Flashlight", reply, parseResult.detectedLanguage, success)
                }

                is ParsedAction.AdjustVolume -> {
                    val (success, msg) = phoneCommandManager.adjustVolume(action.increase)
                    val reply = parseResult.defaultLocalResponse ?: msg
                    _aiResponse.value = reply
                    speakIfEnabled(reply)
                    historyRepository.logCommand(trimmed, "Volume", reply, parseResult.detectedLanguage, success)
                }

                is ParsedAction.GetBatteryStatus -> {
                    val (success, reply) = phoneCommandManager.getBatteryStatus(isSinhala)
                    _aiResponse.value = reply
                    speakIfEnabled(reply)
                    historyRepository.logCommand(trimmed, "Battery", reply, parseResult.detectedLanguage, success)
                }

                is ParsedAction.GetCurrentTime -> {
                    val (success, reply) = phoneCommandManager.getCurrentTimeAndDate(isSinhala)
                    _aiResponse.value = reply
                    speakIfEnabled(reply)
                    historyRepository.logCommand(trimmed, "Time", reply, parseResult.detectedLanguage, success)
                }

                is ParsedAction.OpenYouTube -> {
                    val reply = parseResult.defaultLocalResponse ?: "Opening YouTube."
                    _aiResponse.value = reply
                    speakIfEnabled(reply)
                    val (success, _) = phoneCommandManager.openYouTube()
                    historyRepository.logCommand(trimmed, "YouTube", reply, parseResult.detectedLanguage, success)
                }

                is ParsedAction.OpenWhatsApp -> {
                    val reply = parseResult.defaultLocalResponse ?: "Opening WhatsApp."
                    _aiResponse.value = reply
                    speakIfEnabled(reply)
                    val (success, _) = phoneCommandManager.openWhatsApp()
                    historyRepository.logCommand(trimmed, "WhatsApp", reply, parseResult.detectedLanguage, success)
                }

                is ParsedAction.OpenCamera -> {
                    val reply = parseResult.defaultLocalResponse ?: "Opening Camera."
                    _aiResponse.value = reply
                    speakIfEnabled(reply)
                    val (success, _) = phoneCommandManager.openCamera()
                    historyRepository.logCommand(trimmed, "Camera", reply, parseResult.detectedLanguage, success)
                }

                is ParsedAction.OpenBrowser -> {
                    val reply = parseResult.defaultLocalResponse ?: "Opening Browser."
                    _aiResponse.value = reply
                    speakIfEnabled(reply)
                    val (success, _) = phoneCommandManager.openBrowser()
                    historyRepository.logCommand(trimmed, "Browser", reply, parseResult.detectedLanguage, success)
                }

                is ParsedAction.OpenDialer -> {
                    val reply = parseResult.defaultLocalResponse ?: "Opening Dialer."
                    _aiResponse.value = reply
                    speakIfEnabled(reply)
                    val (success, _) = phoneCommandManager.openDialer()
                    historyRepository.logCommand(trimmed, "Dialer", reply, parseResult.detectedLanguage, success)
                }

                is ParsedAction.OpenSms -> {
                    val reply = parseResult.defaultLocalResponse ?: "Opening Messaging app."
                    _aiResponse.value = reply
                    speakIfEnabled(reply)
                    val (success, _) = phoneCommandManager.openSms()
                    historyRepository.logCommand(trimmed, "SMS", reply, parseResult.detectedLanguage, success)
                }

                is ParsedAction.OpenSettings -> {
                    val reply = parseResult.defaultLocalResponse ?: "Opening Settings."
                    _aiResponse.value = reply
                    speakIfEnabled(reply)
                    val (success, _) = phoneCommandManager.openSettings(action.section)
                    historyRepository.logCommand(trimmed, "Settings", reply, parseResult.detectedLanguage, success)
                }

                is ParsedAction.SetAlarm -> {
                    val reply = parseResult.defaultLocalResponse ?: "Opening Alarm Clock."
                    _aiResponse.value = reply
                    speakIfEnabled(reply)
                    val (success, _) = phoneCommandManager.setAlarm()
                    historyRepository.logCommand(trimmed, "Alarm", reply, parseResult.detectedLanguage, success)
                }

                is ParsedAction.CreateCalendarEvent -> {
                    val reply = parseResult.defaultLocalResponse ?: "Opening Calendar."
                    _aiResponse.value = reply
                    speakIfEnabled(reply)
                    val (success, _) = phoneCommandManager.createCalendarEvent()
                    historyRepository.logCommand(trimmed, "Calendar", reply, parseResult.detectedLanguage, success)
                }

                is ParsedAction.LaunchApp -> {
                    val (success, reply) = phoneCommandManager.launchAppByName(action.appName)
                    _aiResponse.value = reply
                    speakIfEnabled(reply)
                    historyRepository.logCommand(trimmed, "App: ${action.appName}", reply, parseResult.detectedLanguage, success)
                }

                is ParsedAction.Greeting -> {
                    val reply = parseResult.defaultLocalResponse ?: "Yes, sir. I am listening."
                    _aiResponse.value = reply
                    speakIfEnabled(reply)
                    historyRepository.logCommand(trimmed, "Greeting", reply, parseResult.detectedLanguage, true)
                }

                is ParsedAction.ConversationalQuery -> {
                    val answer = geminiRepository.askJarvis(
                        userPrompt = action.query,
                        languagePreference = currentSettings.languageMode,
                        customKey = currentSettings.customApiKey,
                        model = currentSettings.geminiModel
                    )
                    _aiResponse.value = answer
                    speakIfEnabled(answer)
                    historyRepository.logCommand(trimmed, "AI Chat", answer, parseResult.detectedLanguage, true)
                }
            }
            _voiceState.value = VoiceState.Idle
        }
    }

    private fun speakIfEnabled(text: String) {
        if (settings.value.voiceFeedbackEnabled) {
            ttsManager.speak(text)
        }
    }

    fun resetUnlockTrigger() {
        _requestUnlockTrigger.value = false
    }

    fun toggleBackgroundService(enable: Boolean) {
        val intent = Intent(context, JarvisForegroundService::class.java).apply {
            action = if (enable) JarvisForegroundService.ACTION_START else JarvisForegroundService.ACTION_STOP
        }
        try {
            if (enable) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            } else {
                context.stopService(intent)
            }
            _isServiceActive.value = enable
            viewModelScope.launch {
                settingsRepository.setBackgroundListening(enable)
            }
        } catch (e: Exception) {
            Log.e("JarvisViewModel", "Failed to toggle service", e)
        }
    }

    fun setLanguageMode(mode: String) {
        viewModelScope.launch {
            settingsRepository.setLanguageMode(mode)
        }
    }

    fun setVoiceFeedback(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setVoiceFeedback(enabled)
        }
    }

    fun setTtsRate(rate: Float) {
        viewModelScope.launch {
            settingsRepository.setTtsRate(rate)
        }
    }

    fun setTtsPitch(pitch: Float) {
        viewModelScope.launch {
            settingsRepository.setTtsPitch(pitch)
        }
    }

    fun setCustomApiKey(key: String) {
        viewModelScope.launch {
            settingsRepository.setCustomApiKey(key)
        }
    }

    fun setLockScreenAssistant(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setLockScreenAssistant(enabled)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            historyRepository.clearHistory()
        }
    }

    fun deleteHistoryItem(id: Long) {
        viewModelScope.launch {
            historyRepository.deleteEntry(id)
        }
    }

    override fun onCleared() {
        super.onCleared()
        voiceRecognitionManager.stopListening()
        ttsManager.shutdown()
    }
}
