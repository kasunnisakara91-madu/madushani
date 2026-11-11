package com.example.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class ScreenStateReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "ScreenStateReceiver"
        var onScreenOffListener: (() -> Unit)? = null
        var onScreenOnListener: (() -> Unit)? = null
        var onUserPresentListener: (() -> Unit)? = null
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        val action = intent?.action ?: return
        Log.d(TAG, "Screen state action received: $action")
        when (action) {
            Intent.ACTION_SCREEN_OFF -> {
                onScreenOffListener?.invoke()
            }
            Intent.ACTION_SCREEN_ON -> {
                onScreenOnListener?.invoke()
            }
            Intent.ACTION_USER_PRESENT -> {
                onUserPresentListener?.invoke()
            }
        }
    }
}
