package com.example.lockscreen

import android.app.Activity
import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.PowerManager
import android.util.Log

class LockScreenManager(private val context: Context) {

    companion object {
        private const val TAG = "LockScreenManager"
    }

    private val keyguardManager: KeyguardManager? by lazy {
        context.getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager
    }

    private val powerManager: PowerManager? by lazy {
        context.getSystemService(Context.POWER_SERVICE) as? PowerManager
    }

    fun isScreenLocked(): Boolean {
        return keyguardManager?.isKeyguardLocked ?: false
    }

    fun isDeviceSecure(): Boolean {
        return keyguardManager?.isDeviceSecure ?: false
    }

    /**
     * Wakes the screen safely for lock-screen assistant using permitted PowerManager wake lock flags.
     */
    fun wakeScreenBriefly() {
        try {
            val pm = powerManager ?: return
            @Suppress("DEPRECATION")
            val wakeLock = pm.newWakeLock(
                PowerManager.SCREEN_BRIGHT_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
                "madu:jarvis_screen_wake"
            )
            wakeLock.acquire(3000L) // Release after 3 seconds
        } catch (e: Exception) {
            Log.e(TAG, "Failed to acquire temporary wake lock", e)
        }
    }

    /**
     * Authenticates the user using Android's system Keyguard verification.
     * Never bypasses or intercepts user PIN, pattern, or biometric data.
     */
    fun requestSystemAuthentication(
        activity: Activity,
        onSuccess: () -> Unit,
        onCancelled: () -> Unit,
        onError: (String) -> Unit
    ) {
        val km = keyguardManager
        if (km == null) {
            onError("Keyguard service unavailable.")
            return
        }

        if (!km.isKeyguardLocked) {
            // Already unlocked
            onSuccess()
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            km.requestDismissKeyguard(activity, object : KeyguardManager.KeyguardDismissCallback() {
                override fun onDismissSucceeded() {
                    super.onDismissSucceeded()
                    Log.d(TAG, "Keyguard dismissal succeeded.")
                    activity.runOnUiThread { onSuccess() }
                }

                override fun onDismissCancelled() {
                    super.onDismissCancelled()
                    Log.d(TAG, "Keyguard dismissal cancelled by user.")
                    activity.runOnUiThread { onCancelled() }
                }

                override fun onDismissError() {
                    super.onDismissError()
                    Log.e(TAG, "Keyguard dismissal error.")
                    activity.runOnUiThread { onError("Authentication failed.") }
                }
            })
        } else {
            // Fallback for earlier versions
            val intent = km.createConfirmDeviceCredentialIntent(
                "MADU JARVIS AI",
                "Please authenticate to continue."
            )
            if (intent != null) {
                activity.startActivity(intent)
            } else {
                onSuccess()
            }
        }
    }
}
