package com.example

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lockscreen.LockScreenManager
import com.example.ui.JarvisViewModel
import com.example.ui.components.AiOrbComposable
import com.example.ui.components.WaveformVisualizer
import com.example.ui.screens.StatusBadge
import com.example.ui.theme.JarvisBorder
import com.example.ui.theme.JarvisBorderGlow
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisCyanBright
import com.example.ui.theme.JarvisCyanDark
import com.example.ui.theme.JarvisDeepNavy
import com.example.ui.theme.JarvisNavyBlack
import com.example.ui.theme.JarvisSurface
import com.example.ui.theme.JarvisSurfaceElevated
import com.example.ui.theme.JarvisTextPrimary
import com.example.ui.theme.JarvisTextSecondary
import com.example.ui.theme.JarvisTextTertiary
import com.example.ui.theme.MyApplicationTheme
import com.example.voice.VoiceState

class LockScreenActivity : ComponentActivity() {

    private val viewModel: JarvisViewModel by viewModels()
    private lateinit var lockScreenManager: LockScreenManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lockScreenManager = LockScreenManager(this)

        // Configure window for lock-screen overlay
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            )
        }

        enableEdgeToEdge()

        val initialCommand = intent.getStringExtra("INITIAL_VOICE_COMMAND")
        val triggerUnlockPrompt = intent.getBooleanExtra("TRIGGER_UNLOCK_PROMPT", false)

        setContent {
            MyApplicationTheme {
                LockScreenAssistantView(
                    viewModel = viewModel,
                    onDismiss = { finish() },
                    onPerformUnlock = { requestUnlock() }
                )
            }
        }

        if (!initialCommand.isNullOrBlank()) {
            viewModel.processCommand(initialCommand)
        }

        if (triggerUnlockPrompt) {
            requestUnlock()
        }
    }

    private fun requestUnlock() {
        lockScreenManager.requestSystemAuthentication(
            activity = this,
            onSuccess = {
                Toast.makeText(this, "Authentication successful. Access granted.", Toast.LENGTH_SHORT).show()
                val mainIntent = Intent(this, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
                startActivity(mainIntent)
                finish()
            },
            onCancelled = {
                Toast.makeText(this, "Authentication cancelled.", Toast.LENGTH_SHORT).show()
            },
            onError = { err ->
                Toast.makeText(this, "Authentication error: $err", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

@Composable
fun LockScreenAssistantView(
    viewModel: JarvisViewModel,
    onDismiss: () -> Unit,
    onPerformUnlock: () -> Unit
) {
    val voiceState by viewModel.voiceState.collectAsState()
    val amplitude by viewModel.rmsAmplitude.collectAsState()
    val aiResponse by viewModel.aiResponse.collectAsState()
    val isSpeaking by viewModel.ttsManager.isSpeaking.collectAsState()
    val unlockTrigger by viewModel.requestUnlockTrigger.collectAsState()

    LaunchedEffect(unlockTrigger) {
        if (unlockTrigger) {
            onPerformUnlock()
            viewModel.resetUnlockTrigger()
        }
    }

    val isListening = voiceState is VoiceState.Listening

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(JarvisNavyBlack, JarvisDeepNavy, JarvisNavyBlack)
                )
            )
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        // Lock Screen Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1E293B)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Device Secured",
                        tint = JarvisCyan,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "JARVIS LOCK ASSISTANT",
                        color = JarvisCyanBright,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Device Secured • Android Keyguard",
                        color = JarvisTextSecondary,
                        fontSize = 11.sp
                    )
                }
            }

            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .size(36.dp)
                    .background(JarvisSurfaceElevated, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Dismiss",
                    tint = JarvisTextSecondary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Glowing AI Orb
        AiOrbComposable(
            voiceState = voiceState,
            amplitude = amplitude,
            size = 200.dp
        )

        Spacer(modifier = Modifier.height(16.dp))

        StatusBadge(voiceState = voiceState, isSpeaking = isSpeaking)

        Spacer(modifier = Modifier.height(12.dp))

        WaveformVisualizer(
            voiceState = voiceState,
            amplitude = amplitude,
            modifier = Modifier.padding(horizontal = 24.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // AI Response Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = JarvisSurface),
            border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(JarvisBorderGlow, JarvisBorder)))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "MADU JARVIS AI",
                    color = JarvisCyanBright,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = aiResponse,
                    color = JarvisTextPrimary,
                    fontSize = 15.sp,
                    lineHeight = 21.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Unlock Trigger Protocol Button
        Button(
            onClick = onPerformUnlock,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("lock_screen_unlock_button"),
            colors = ButtonDefaults.buttonColors(containerColor = JarvisSurfaceElevated),
            shape = RoundedCornerShape(12.dp),
            border = ButtonDefaults.outlinedButtonBorder.copy(brush = Brush.horizontalGradient(listOf(JarvisCyan, JarvisBorderGlow)))
        ) {
            Icon(Icons.Default.LockOpen, contentDescription = null, tint = JarvisCyan, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Authenticate to Unlock Device", color = JarvisCyanBright, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.weight(1f))

        // Push-To-Talk Mic Button
        Box(
            modifier = Modifier
                .size(70.dp)
                .clip(CircleShape)
                .background(
                    if (isListening) Brush.radialGradient(listOf(JarvisCyanBright, JarvisCyanDark))
                    else Brush.radialGradient(listOf(JarvisCyan, JarvisDeepNavy))
                )
                .border(2.dp, if (isListening) Color.White else JarvisBorderGlow, CircleShape)
                .clickable {
                    if (isListening) {
                        viewModel.stopListening()
                    } else {
                        viewModel.startListening()
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isListening) Icons.Default.MicOff else Icons.Default.Mic,
                contentDescription = "Microphone",
                tint = if (isListening) Color.White else JarvisNavyBlack,
                modifier = Modifier.size(32.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = if (isListening) "Listening… Tap to Stop" else "Tap to Speak (Sinhala or English)",
            color = if (isListening) JarvisCyanBright else JarvisTextSecondary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}
