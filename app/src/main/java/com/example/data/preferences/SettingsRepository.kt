package com.example.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "jarvis_settings")

data class JarvisSettings(
    val languageMode: String = "mixed", // "sinhala", "english", "mixed"
    val voiceFeedbackEnabled: Boolean = true,
    val ttsRate: Float = 1.0f,
    val ttsPitch: Float = 1.0f,
    val backgroundListeningEnabled: Boolean = false,
    val lockScreenAssistantEnabled: Boolean = true,
    val customApiKey: String = "",
    val geminiModel: String = "gemini-2.5-flash"
)

class SettingsRepository(private val context: Context) {
    companion object {
        val KEY_LANGUAGE_MODE = stringPreferencesKey("language_mode")
        val KEY_VOICE_FEEDBACK = booleanPreferencesKey("voice_feedback")
        val KEY_TTS_RATE = floatPreferencesKey("tts_rate")
        val KEY_TTS_PITCH = floatPreferencesKey("tts_pitch")
        val KEY_BG_LISTENING = booleanPreferencesKey("background_listening")
        val KEY_LOCK_ASSISTANT = booleanPreferencesKey("lock_screen_assistant")
        val KEY_CUSTOM_API_KEY = stringPreferencesKey("custom_api_key")
        val KEY_GEMINI_MODEL = stringPreferencesKey("gemini_model")
    }

    val settingsFlow: Flow<JarvisSettings> = context.dataStore.data.map { preferences ->
        JarvisSettings(
            languageMode = preferences[KEY_LANGUAGE_MODE] ?: "mixed",
            voiceFeedbackEnabled = preferences[KEY_VOICE_FEEDBACK] ?: true,
            ttsRate = preferences[KEY_TTS_RATE] ?: 1.0f,
            ttsPitch = preferences[KEY_TTS_PITCH] ?: 1.0f,
            backgroundListeningEnabled = preferences[KEY_BG_LISTENING] ?: false,
            lockScreenAssistantEnabled = preferences[KEY_LOCK_ASSISTANT] ?: true,
            customApiKey = preferences[KEY_CUSTOM_API_KEY] ?: "",
            geminiModel = preferences[KEY_GEMINI_MODEL] ?: "gemini-2.5-flash"
        )
    }

    suspend fun setLanguageMode(mode: String) {
        context.dataStore.edit { it[KEY_LANGUAGE_MODE] = mode }
    }

    suspend fun setVoiceFeedback(enabled: Boolean) {
        context.dataStore.edit { it[KEY_VOICE_FEEDBACK] = enabled }
    }

    suspend fun setTtsRate(rate: Float) {
        context.dataStore.edit { it[KEY_TTS_RATE] = rate }
    }

    suspend fun setTtsPitch(pitch: Float) {
        context.dataStore.edit { it[KEY_TTS_PITCH] = pitch }
    }

    suspend fun setBackgroundListening(enabled: Boolean) {
        context.dataStore.edit { it[KEY_BG_LISTENING] = enabled }
    }

    suspend fun setLockScreenAssistant(enabled: Boolean) {
        context.dataStore.edit { it[KEY_LOCK_ASSISTANT] = enabled }
    }

    suspend fun setCustomApiKey(key: String) {
        context.dataStore.edit { it[KEY_CUSTOM_API_KEY] = key.trim() }
    }

    suspend fun setGeminiModel(model: String) {
        context.dataStore.edit { it[KEY_GEMINI_MODEL] = model }
    }
}
