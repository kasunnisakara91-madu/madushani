package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.FlashlightOff
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VolumeDown
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.parser.SettingsSection
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
fun PhoneControlsScreen(
    viewModel: JarvisViewModel,
    onRequestUnlock: () -> Unit,
    modifier: Modifier = Modifier
) {
    val flashlightOn by viewModel.flashlightOn.collectAsState()
    val scrollState = rememberScrollState()

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
            text = "HARDWARE & SYSTEM PROTOCOLS",
            color = JarvisCyanBright,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.sp
        )
        Text(
            text = "Direct Android hardware controls and native triggers",
            color = JarvisTextSecondary,
            fontSize = 12.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Hardware Controls Grid (Flashlight, Volume, Unlock)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Flashlight Card
            Card(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(14.dp))
                    .border(1.dp, if (flashlightOn) JarvisCyanBright else JarvisBorder, RoundedCornerShape(14.dp))
                    .clickable {
                        viewModel.processCommand(if (flashlightOn) "flashlight off" else "flashlight on")
                    }
                    .testTag("flashlight_control_card"),
                colors = CardDefaults.cardColors(containerColor = if (flashlightOn) JarvisCyanDark else JarvisSurface)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = if (flashlightOn) Icons.Default.FlashlightOn else Icons.Default.FlashlightOff,
                        contentDescription = "Flashlight",
                        tint = if (flashlightOn) JarvisCyanBright else JarvisTextSecondary,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Flashlight",
                        color = JarvisTextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = if (flashlightOn) "ON" else "OFF",
                        color = if (flashlightOn) JarvisCyanBright else JarvisTextTertiary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Unlock Request Card
            Card(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(14.dp))
                    .border(1.dp, JarvisBorderGlow, RoundedCornerShape(14.dp))
                    .clickable {
                        viewModel.processCommand("unlock my phone")
                        onRequestUnlock()
                    }
                    .testTag("unlock_test_card"),
                colors = CardDefaults.cardColors(containerColor = JarvisSurface)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.LockOpen,
                        contentDescription = "Unlock Challenge",
                        tint = JarvisCyan,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Unlock Phone",
                        color = JarvisTextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Verify Credential",
                        color = JarvisCyanBright,
                        fontSize = 10.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Volume Controls Bar
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = JarvisSurface),
            border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(JarvisBorder, JarvisBorderGlow)))
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.VolumeUp,
                        contentDescription = "Volume Control",
                        tint = JarvisCyanBright,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Volume Control", color = JarvisTextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        Text("Adjust phone master audio", color = JarvisTextSecondary, fontSize = 11.sp)
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(
                        onClick = { viewModel.processCommand("volume down") },
                        modifier = Modifier.size(36.dp).background(JarvisSurfaceElevated, CircleShape)
                    ) {
                        Icon(Icons.Default.VolumeDown, contentDescription = "Down", tint = JarvisCyan, modifier = Modifier.size(18.dp))
                    }
                    IconButton(
                        onClick = { viewModel.processCommand("volume up") },
                        modifier = Modifier.size(36.dp).background(JarvisSurfaceElevated, CircleShape)
                    ) {
                        Icon(Icons.Default.VolumeUp, contentDescription = "Up", tint = JarvisCyan, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Battery & Time Query Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ActionQuickCard(
                title = "Battery Status",
                subtitle = "Query level & charge",
                icon = Icons.Default.BatteryChargingFull,
                modifier = Modifier.weight(1f),
                onClick = { viewModel.processCommand("battery status") }
            )
            ActionQuickCard(
                title = "System Time",
                subtitle = "Clock & calendar date",
                icon = Icons.Default.Alarm,
                modifier = Modifier.weight(1f),
                onClick = { viewModel.processCommand("what time is it") }
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Application Launchers
        Text(
            text = "APPLICATION LAUNCHERS",
            color = JarvisTextTertiary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            AppShortcutPill(title = "YouTube", icon = Icons.Default.PlayCircle, modifier = Modifier.weight(1f)) {
                viewModel.processCommand("open youtube")
            }
            AppShortcutPill(title = "WhatsApp", icon = Icons.Default.Message, modifier = Modifier.weight(1f)) {
                viewModel.processCommand("open whatsapp")
            }
            AppShortcutPill(title = "Camera", icon = Icons.Default.CameraAlt, modifier = Modifier.weight(1f)) {
                viewModel.processCommand("open camera")
            }
            AppShortcutPill(title = "Browser", icon = Icons.Default.Language, modifier = Modifier.weight(1f)) {
                viewModel.processCommand("open browser")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            AppShortcutPill(title = "Dialer", icon = Icons.Default.Phone, modifier = Modifier.weight(1f)) {
                viewModel.processCommand("open dialer")
            }
            AppShortcutPill(title = "SMS", icon = Icons.Default.Message, modifier = Modifier.weight(1f)) {
                viewModel.processCommand("open sms")
            }
            AppShortcutPill(title = "Alarm", icon = Icons.Default.Alarm, modifier = Modifier.weight(1f)) {
                viewModel.processCommand("set alarm")
            }
            AppShortcutPill(title = "Calendar", icon = Icons.Default.CalendarMonth, modifier = Modifier.weight(1f)) {
                viewModel.processCommand("open calendar")
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Android System Settings
        Text(
            text = "SYSTEM SETTINGS SHORTCUTS",
            color = JarvisTextTertiary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SettingShortcutPill(title = "Wi-Fi", icon = Icons.Default.Wifi, modifier = Modifier.weight(1f)) {
                viewModel.phoneCommandManager.openSettings(SettingsSection.WIFI)
            }
            SettingShortcutPill(title = "Bluetooth", icon = Icons.Default.Bluetooth, modifier = Modifier.weight(1f)) {
                viewModel.phoneCommandManager.openSettings(SettingsSection.BLUETOOTH)
            }
            SettingShortcutPill(title = "Display", icon = Icons.Default.Settings, modifier = Modifier.weight(1f)) {
                viewModel.phoneCommandManager.openSettings(SettingsSection.DISPLAY)
            }
            SettingShortcutPill(title = "Sound", icon = Icons.Default.VolumeUp, modifier = Modifier.weight(1f)) {
                viewModel.phoneCommandManager.openSettings(SettingsSection.SOUND)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun ActionQuickCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, JarvisBorder, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = JarvisSurface)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(JarvisSurfaceElevated),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = title, tint = JarvisCyan, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(title, color = JarvisTextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Text(subtitle, color = JarvisTextTertiary, fontSize = 10.sp)
            }
        }
    }
}

@Composable
fun AppShortcutPill(
    title: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        color = JarvisSurfaceElevated,
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(JarvisBorder, JarvisCyanDark)))
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = title, tint = JarvisCyanBright, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(6.dp))
            Text(title, color = JarvisTextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun SettingShortcutPill(
    title: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        color = JarvisSurface,
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(JarvisBorder, JarvisBorderGlow)))
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = title, tint = JarvisCyan, modifier = Modifier.size(22.dp))
            Spacer(modifier = Modifier.height(6.dp))
            Text(title, color = JarvisTextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Medium)
        }
    }
}
