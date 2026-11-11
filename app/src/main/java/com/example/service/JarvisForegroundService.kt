package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.LockScreenActivity
import com.example.MainActivity
import com.example.R
import com.example.data.db.AppDatabase
import com.example.data.db.CommandHistoryRepository
import com.example.data.preferences.SettingsRepository
import com.example.lockscreen.LockScreenManager
import com.example.parser.CommandParser
import com.example.parser.ParsedAction
import com.example.voice.TextToSpeechManager
import com.example.voice.VoiceRecognitionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class JarvisForegroundService : Service() {

    companion object {
        private const val TAG = "JarvisService"
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "jarvis_voice_channel"

        const val ACTION_START = "com.example.service.action.START"
        const val ACTION_STOP = "com.example.service.action.STOP"

        var isRunning: Boolean = false
            private set
    }

    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())
    private lateinit var voiceRecognitionManager: VoiceRecognitionManager
    private lateinit var ttsManager: TextToSpeechManager
    private lateinit var phoneCommandManager: PhoneCommandManager
    private lateinit var lockScreenManager: LockScreenManager
    private lateinit var historyRepository: CommandHistoryRepository
    private lateinit var settingsRepository: SettingsRepository

    private val screenReceiver = ScreenStateReceiver()

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "JarvisForegroundService created")
        isRunning = true

        val db = AppDatabase.getDatabase(this)
        historyRepository = CommandHistoryRepository(db.commandHistoryDao())
        settingsRepository = SettingsRepository(this)
        phoneCommandManager = PhoneCommandManager(this)
        lockScreenManager = LockScreenManager(this)

        ttsManager = TextToSpeechManager(this)

        voiceRecognitionManager = VoiceRecognitionManager(this) { recognizedText ->
            handleRecognizedVoice(recognizedText)
        }

        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_OFF)
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_USER_PRESENT)
        }
        registerReceiver(screenReceiver, filter)

        ScreenStateReceiver.onScreenOffListener = {
            Log.d(TAG, "Screen off event in service")
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopSelf()
            return START_NOT_STICKY
        }

        createNotificationChannel()
        val notification = buildForegroundNotification()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }

        // Start listening
        serviceScope.launch {
            val settings = settingsRepository.settingsFlow.first()
            voiceRecognitionManager.startListening(settings.languageMode)
        }

        return START_STICKY
    }

    private fun handleRecognizedVoice(text: String) {
        if (text.isBlank()) return
        Log.d(TAG, "Foreground voice input: $text")

        serviceScope.launch {
            val parseResult = CommandParser.parse(text)
            val isSinhala = parseResult.detectedLanguage == "Sinhala" || text.any { it in '\u0D80'..'\u0DFF' }

            when (val action = parseResult.action) {
                is ParsedAction.RequestUnlock -> {
                    val response = if (isSinhala) {
                        "කරුණාකර ඉදිරියට යාමට ඔබගේ අනන්‍යතාවය තහවුරු කරන්න."
                    } else {
                        "Please authenticate to continue."
                    }
                    ttsManager.speak(response)
                    lockScreenManager.wakeScreenBriefly()

                    val lockIntent = Intent(this@JarvisForegroundService, LockScreenActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                        putExtra("INITIAL_VOICE_COMMAND", text)
                        putExtra("TRIGGER_UNLOCK_PROMPT", true)
                    }
                    startActivity(lockIntent)
                }

                is ParsedAction.Flashlight -> {
                    val (success, msg) = phoneCommandManager.toggleFlashlight(action.enable)
                    val reply = parseResult.defaultLocalResponse ?: msg
                    ttsManager.speak(reply)
                    historyRepository.logCommand(text, "Flashlight", reply, parseResult.detectedLanguage, success)
                }

                is ParsedAction.AdjustVolume -> {
                    val (success, msg) = phoneCommandManager.adjustVolume(action.increase)
                    val reply = parseResult.defaultLocalResponse ?: msg
                    ttsManager.speak(reply)
                    historyRepository.logCommand(text, "Volume", reply, parseResult.detectedLanguage, success)
                }

                is ParsedAction.GetBatteryStatus -> {
                    val (success, reply) = phoneCommandManager.getBatteryStatus(isSinhala)
                    ttsManager.speak(reply)
                    historyRepository.logCommand(text, "Battery", reply, parseResult.detectedLanguage, success)
                }

                is ParsedAction.GetCurrentTime -> {
                    val (success, reply) = phoneCommandManager.getCurrentTimeAndDate(isSinhala)
                    ttsManager.speak(reply)
                    historyRepository.logCommand(text, "Time", reply, parseResult.detectedLanguage, success)
                }

                is ParsedAction.OpenYouTube -> {
                    ttsManager.speak(parseResult.defaultLocalResponse ?: "Opening YouTube.")
                    phoneCommandManager.openYouTube()
                }

                is ParsedAction.OpenWhatsApp -> {
                    ttsManager.speak(parseResult.defaultLocalResponse ?: "Opening WhatsApp.")
                    phoneCommandManager.openWhatsApp()
                }

                is ParsedAction.OpenCamera -> {
                    ttsManager.speak(parseResult.defaultLocalResponse ?: "Opening Camera.")
                    phoneCommandManager.openCamera()
                }

                is ParsedAction.OpenBrowser -> {
                    ttsManager.speak(parseResult.defaultLocalResponse ?: "Opening Browser.")
                    phoneCommandManager.openBrowser()
                }

                is ParsedAction.Greeting -> {
                    val reply = parseResult.defaultLocalResponse ?: "Yes, I am listening."
                    ttsManager.speak(reply)
                    historyRepository.logCommand(text, "Greeting", reply, parseResult.detectedLanguage, true)
                }

                else -> {
                    // Other queries: launch LockScreenActivity or MainActivity to show full AI answer
                    val targetActivity = if (lockScreenManager.isScreenLocked()) {
                        LockScreenActivity::class.java
                    } else {
                        MainActivity::class.java
                    }
                    val intent = Intent(this@JarvisForegroundService, targetActivity).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        putExtra("INITIAL_VOICE_COMMAND", text)
                    }
                    startActivity(intent)
                }
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.channel_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.channel_desc)
                setShowBadge(false)
            }
            val nm = getSystemService(NotificationManager::class.java)
            nm?.createNotificationChannel(channel)
        }
    }

    private fun buildForegroundNotification(): Notification {
        val openIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val openPendingIntent = PendingIntent.getActivity(
            this,
            0,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(this, JarvisForegroundService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            this,
            1,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("MADU JARVIS AI")
            .setContentText("Background microphone listening active (Push-to-Talk or Wake-Word)")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setOngoing(true)
            .setContentIntent(openPendingIntent)
            .addAction(
                android.R.drawable.ic_media_pause,
                getString(R.string.action_stop),
                stopPendingIntent
            )
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "JarvisForegroundService destroyed")
        isRunning = false
        try {
            unregisterReceiver(screenReceiver)
        } catch (e: Exception) {
            Log.e(TAG, "Error unregistering receiver", e)
        }
        voiceRecognitionManager.stopListening()
        ttsManager.shutdown()
        serviceScope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
