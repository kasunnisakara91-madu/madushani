package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import com.example.data.gemini.GeminiRepository
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.BuildConfig
import com.example.ui.JarvisViewModel
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

@Composable
fun SettingsScreen(
    viewModel: JarvisViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val settings by viewModel.settings.collectAsState()
    val isServiceActive by viewModel.isServiceActive.collectAsState()

    var inputApiKey by remember(settings.customApiKey) { mutableStateOf(settings.customApiKey) }
    var ttsRate by remember(settings.ttsRate) { mutableFloatStateOf(settings.ttsRate) }
    var ttsPitch by remember(settings.ttsPitch) { mutableFloatStateOf(settings.ttsPitch) }

    val scrollState = rememberScrollState()

    val hasBuildConfigKey = remember {
        val key = GeminiRepository.getBuildConfigApiKey()
        key.isNotBlank() && key != "MY_GEMINI_API_KEY"
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(JarvisNavyBlack, JarvisDeepNavy, JarvisNavyBlack)
                )
            )
            .padding(16.dp)
            .verticalScroll(scrollState)
    ) {
        Text(
            text = "CONFIGURATION PROTOCOLS",
            color = JarvisCyanBright,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.sp
        )
        Text(
            text = "Preferences, Voice engine, Background listening & Gemini AI",
            color = JarvisTextSecondary,
            fontSize = 12.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 1. Language Preferences
        SettingsSectionHeader(title = "VOICE LANGUAGE RECOGNITION")

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = JarvisSurface),
            border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(JarvisBorder, JarvisCyanDark)))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "Active Speech Recognition Engine",
                    color = JarvisTextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Controls speech-to-text language and parsing",
                    color = JarvisTextSecondary,
                    fontSize = 11.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    LanguageSelectOption(
                        title = "Mixed Mode",
                        subtitle = "සිංහල + English",
                        isSelected = settings.languageMode == "mixed",
                        modifier = Modifier.weight(1f)
                    ) {
                        viewModel.setLanguageMode("mixed")
                    }
                    LanguageSelectOption(
                        title = "සිංහල",
                        subtitle = "Sinhala Only",
                        isSelected = settings.languageMode == "sinhala",
                        modifier = Modifier.weight(1f)
                    ) {
                        viewModel.setLanguageMode("sinhala")
                    }
                    LanguageSelectOption(
                        title = "English",
                        subtitle = "English Only",
                        isSelected = settings.languageMode == "english",
                        modifier = Modifier.weight(1f)
                    ) {
                        viewModel.setLanguageMode("english")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 2. Background Listening & Wake-Word
        SettingsSectionHeader(title = "BACKGROUND LISTENING & LOCK SCREEN")

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = JarvisSurface),
            border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(JarvisBorder, JarvisBorderGlow)))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Mic, contentDescription = null, tint = JarvisCyan, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Background Listening Mode",
                                color = JarvisTextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Text(
                            text = "Runs a foreground service with persistent microphone notification and quick stop control.",
                            color = JarvisTextSecondary,
                            fontSize = 11.sp,
                            lineHeight = 16.sp
                        )
                    }

                    Switch(
                        checked = isServiceActive,
                        onCheckedChange = { enable ->
                            viewModel.toggleBackgroundService(enable)
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = JarvisNavyBlack,
                            checkedTrackColor = JarvisCyanBright,
                            uncheckedThumbColor = JarvisTextSecondary,
                            uncheckedTrackColor = JarvisSurfaceElevated
                        ),
                        modifier = Modifier.testTag("bg_listening_switch")
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Security, contentDescription = null, tint = JarvisCyan, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Lock Screen Assistant Window",
                                color = JarvisTextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Text(
                            text = "Display JARVIS assistant over lock screen via showWhenLocked.",
                            color = JarvisTextSecondary,
                            fontSize = 11.sp
                        )
                    }

                    Switch(
                        checked = settings.lockScreenAssistantEnabled,
                        onCheckedChange = { enable ->
                            viewModel.setLockScreenAssistant(enable)
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = JarvisNavyBlack,
                            checkedTrackColor = JarvisCyanBright,
                            uncheckedThumbColor = JarvisTextSecondary,
                            uncheckedTrackColor = JarvisSurfaceElevated
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 3. Text-to-Speech Engine
        SettingsSectionHeader(title = "TEXT-TO-SPEECH (TTS) AUDIO")

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = JarvisSurface),
            border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(JarvisBorder, JarvisCyanDark)))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Voice Responses Aloud",
                            color = JarvisTextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Speak aloud after recognizing commands",
                            color = JarvisTextSecondary,
                            fontSize = 11.sp
                        )
                    }

                    Switch(
                        checked = settings.voiceFeedbackEnabled,
                        onCheckedChange = { viewModel.setVoiceFeedback(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = JarvisNavyBlack,
                            checkedTrackColor = JarvisCyanBright,
                            uncheckedThumbColor = JarvisTextSecondary,
                            uncheckedTrackColor = JarvisSurfaceElevated
                        )
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Speech Rate: ${String.format("%.1fx", ttsRate)}",
                    color = JarvisTextSecondary,
                    fontSize = 12.sp
                )
                Slider(
                    value = ttsRate,
                    onValueChange = {
                        ttsRate = it
                        viewModel.setTtsRate(it)
                    },
                    valueRange = 0.5f..2.0f,
                    colors = SliderDefaults.colors(
                        thumbColor = JarvisCyanBright,
                        activeTrackColor = JarvisCyan,
                        inactiveTrackColor = JarvisSurfaceElevated
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Vocal Pitch: ${String.format("%.1fx", ttsPitch)}",
                    color = JarvisTextSecondary,
                    fontSize = 12.sp
                )
                Slider(
                    value = ttsPitch,
                    onValueChange = {
                        ttsPitch = it
                        viewModel.setTtsPitch(it)
                    },
                    valueRange = 0.5f..2.0f,
                    colors = SliderDefaults.colors(
                        thumbColor = JarvisCyanBright,
                        activeTrackColor = JarvisCyan,
                        inactiveTrackColor = JarvisSurfaceElevated
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        viewModel.ttsManager.speak("Hello! MADU JARVIS AI vocal synthesizer online and calibrated.")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = JarvisSurfaceElevated),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.VolumeUp, contentDescription = null, tint = JarvisCyan, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Test Voice Output", color = JarvisCyanBright)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 4. Gemini AI Configuration
        SettingsSectionHeader(title = "GEMINI AI INTELLIGENCE API")

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = JarvisSurface),
            border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(JarvisBorderGlow, JarvisBorder)))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Key, contentDescription = null, tint = JarvisCyan, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Gemini API Configuration",
                        color = JarvisTextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = if (hasBuildConfigKey) {
                        "✓ Active API key detected from project Secrets panel (BuildConfig)."
                    } else {
                        "Enter a custom Gemini API key or configure Secrets panel in AI Studio."
                    },
                    color = if (hasBuildConfigKey) Color(0xFF10B981) else JarvisTextSecondary,
                    fontSize = 11.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = inputApiKey,
                    onValueChange = { inputApiKey = it },
                    placeholder = { Text("Paste custom AI Studio Gemini API Key…", color = JarvisTextTertiary, fontSize = 12.sp) },
                    modifier = Modifier.fillMaxWidth().testTag("custom_api_key_input"),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = JarvisCyanBright,
                        unfocusedBorderColor = JarvisBorder,
                        focusedTextColor = JarvisTextPrimary,
                        unfocusedTextColor = JarvisTextPrimary,
                        focusedContainerColor = JarvisSurfaceElevated,
                        unfocusedContainerColor = JarvisSurfaceElevated
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            viewModel.setCustomApiKey(inputApiKey)
                            Toast.makeText(context, "API Key saved successfully", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.weight(1f).testTag("save_api_key_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = JarvisCyan),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Save Key", color = JarvisNavyBlack, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            viewModel.processCommand("Who are you and what can you do?")
                            Toast.makeText(context, "Testing Jarvis AI query…", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = JarvisSurfaceElevated),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Test AI Query", color = JarvisCyanBright)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}

@Composable
fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        color = JarvisTextTertiary,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = FontFamily.Monospace,
        letterSpacing = 1.sp,
        modifier = Modifier.padding(bottom = 6.dp, start = 2.dp)
    )
}

@Composable
fun LanguageSelectOption(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (isSelected) JarvisCyanDark else JarvisSurfaceElevated)
            .border(1.dp, if (isSelected) JarvisCyanBright else JarvisBorder, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = title,
                color = if (isSelected) JarvisCyanBright else JarvisTextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = subtitle,
                color = if (isSelected) JarvisCyan else JarvisTextTertiary,
                fontSize = 9.sp
            )
        }
    }
}
