package com.example.service

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.net.Uri
import android.os.BatteryManager
import android.os.Build
import android.provider.AlarmClock
import android.provider.CalendarContract
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import com.example.parser.SettingsSection
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PhoneCommandManager(private val context: Context) {

    private val cameraManager: CameraManager? by lazy {
        context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
    }

    private val audioManager: AudioManager? by lazy {
        context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
    }

    private var isTorchOn: Boolean = false

    fun toggleFlashlight(enable: Boolean): Pair<Boolean, String> {
        return try {
            val cm = cameraManager ?: return Pair(false, "Flashlight hardware unavailable.")
            val cameraId = cm.cameraIdList.firstOrNull { id ->
                val characteristics = cm.getCameraCharacteristics(id)
                characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
            } ?: return Pair(false, "Flashlight not supported on this device.")

            cm.setTorchMode(cameraId, enable)
            isTorchOn = enable
            val msg = if (enable) "Flashlight turned ON." else "Flashlight turned OFF."
            Pair(true, msg)
        } catch (e: CameraAccessException) {
            Log.e("PhoneCommandManager", "Flashlight error", e)
            Pair(false, "Camera access error: ${e.message}")
        } catch (e: Exception) {
            Log.e("PhoneCommandManager", "Flashlight error", e)
            Pair(false, "Could not control flashlight: ${e.message}")
        }
    }

    fun adjustVolume(increase: Boolean): Pair<Boolean, String> {
        return try {
            val am = audioManager ?: return Pair(false, "Audio manager unavailable.")
            val direction = if (increase) AudioManager.ADJUST_RAISE else AudioManager.ADJUST_LOWER
            am.adjustStreamVolume(AudioManager.STREAM_MUSIC, direction, AudioManager.FLAG_SHOW_UI)
            val current = am.getStreamVolume(AudioManager.STREAM_MUSIC)
            val max = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            val percent = (current * 100) / max
            val msg = if (increase) "Volume raised to $percent%." else "Volume lowered to $percent%."
            Pair(true, msg)
        } catch (e: Exception) {
            Log.e("PhoneCommandManager", "Volume error", e)
            Pair(false, "Could not adjust volume: ${e.message}")
        }
    }

    fun getBatteryStatus(isSinhala: Boolean): Pair<Boolean, String> {
        return try {
            val ifilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            val batteryStatus: Intent? = context.registerReceiver(null, ifilter)
            val level: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
            val scale: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
            val status: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1

            val batteryPct = if (level >= 0 && scale > 0) (level * 100 / scale) else 100
            val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                    status == BatteryManager.BATTERY_STATUS_FULL

            val message = if (isSinhala) {
                if (isCharging) {
                    "ඔබගේ දුරකථනයේ බැටරි මට්ටම $batteryPct% කි. දැනට චාජ් වෙමින් පවතී."
                } else {
                    "ඔබගේ දුරකථනයේ බැටරි මට්ටම $batteryPct% කි."
                }
            } else {
                if (isCharging) {
                    "Battery level is at $batteryPct% and charging."
                } else {
                    "Battery level is at $batteryPct%."
                }
            }
            Pair(true, message)
        } catch (e: Exception) {
            Log.e("PhoneCommandManager", "Battery query error", e)
            val fallback = if (isSinhala) "බැටරි තොරතුරු ලබා ගැනීමට නොහැකි විය." else "Unable to read battery status."
            Pair(false, fallback)
        }
    }

    fun getCurrentTimeAndDate(isSinhala: Boolean): Pair<Boolean, String> {
        val now = Date()
        val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
        val dateFormat = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault())
        val timeStr = timeFormat.format(now)
        val dateStr = dateFormat.format(now)

        val message = if (isSinhala) {
            "දැන් වේලාව $timeStr වේ. දිනය $dateStr."
        } else {
            "The current time is $timeStr on $dateStr."
        }
        return Pair(true, message)
    }

    fun openYouTube(): Pair<Boolean, String> {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com")).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            setPackage("com.google.android.youtube")
        }
        return launchIntentSafely(intent, "YouTube", "https://www.youtube.com")
    }

    fun openWhatsApp(): Pair<Boolean, String> {
        val pm = context.packageManager
        val intent = pm.getLaunchIntentForPackage("com.whatsapp")
        return if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            Pair(true, "Launching WhatsApp.")
        } else {
            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://web.whatsapp.com")).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            launchIntentSafely(webIntent, "WhatsApp (Browser)", null)
        }
    }

    fun openCamera(): Pair<Boolean, String> {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        return launchIntentSafely(intent, "Camera", null)
    }

    fun openBrowser(): Pair<Boolean, String> {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com")).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        return launchIntentSafely(intent, "Browser", null)
    }

    fun openDialer(): Pair<Boolean, String> {
        val intent = Intent(Intent.ACTION_DIAL).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        return launchIntentSafely(intent, "Phone Dialer", null)
    }

    fun openSms(): Pair<Boolean, String> {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("smsto:")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        return launchIntentSafely(intent, "SMS Messenger", null)
    }

    fun openSettings(section: SettingsSection): Pair<Boolean, String> {
        val action = when (section) {
            SettingsSection.WIFI -> Settings.ACTION_WIFI_SETTINGS
            SettingsSection.BLUETOOTH -> Settings.ACTION_BLUETOOTH_SETTINGS
            SettingsSection.DISPLAY -> Settings.ACTION_DISPLAY_SETTINGS
            SettingsSection.SOUND -> Settings.ACTION_SOUND_SETTINGS
            SettingsSection.APPS -> Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            SettingsSection.GENERAL -> Settings.ACTION_SETTINGS
        }
        val intent = Intent(action).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            if (section == SettingsSection.APPS) {
                data = Uri.fromParts("package", context.packageName, null)
            }
        }
        return launchIntentSafely(intent, "${section.name.lowercase().replaceFirstChar { it.uppercase() }} Settings", null)
    }

    fun setAlarm(): Pair<Boolean, String> {
        val intent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            putExtra(AlarmClock.EXTRA_SKIP_UI, false)
        }
        return launchIntentSafely(intent, "Alarm Clock", null)
    }

    fun createCalendarEvent(): Pair<Boolean, String> {
        val intent = Intent(Intent.ACTION_INSERT).apply {
            data = CalendarContract.Events.CONTENT_URI
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            putExtra(CalendarContract.Events.TITLE, "Jarvis Reminder")
        }
        return launchIntentSafely(intent, "Calendar", null)
    }

    fun launchAppByName(name: String): Pair<Boolean, String> {
        val pm = context.packageManager
        val query = name.lowercase().trim()
        val installedApps = pm.getInstalledApplications(PackageManager.GET_META_DATA)

        for (app in installedApps) {
            val label = pm.getApplicationLabel(app).toString().lowercase()
            if (label.contains(query) || app.packageName.lowercase().contains(query)) {
                val launchIntent = pm.getLaunchIntentForPackage(app.packageName)
                if (launchIntent != null) {
                    launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(launchIntent)
                    return Pair(true, "Launching $label.")
                }
            }
        }
        return Pair(false, "Could not find an installed application matching \"$name\".")
    }

    private fun launchIntentSafely(intent: Intent, appName: String, fallbackUrl: String?): Pair<Boolean, String> {
        return try {
            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
                Pair(true, "Opening $appName.")
            } else if (fallbackUrl != null) {
                val fallbackIntent = Intent(Intent.ACTION_VIEW, Uri.parse(fallbackUrl)).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(fallbackIntent)
                Pair(true, "Opening $appName in browser.")
            } else {
                Pair(false, "No suitable application found to open $appName.")
            }
        } catch (e: Exception) {
            Log.e("PhoneCommandManager", "Failed to launch $appName", e)
            Pair(false, "Failed to launch $appName: ${e.message}")
        }
    }
}
