package com.example.ui.screens

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import com.example.util.PermissionManager

@Composable
fun PermissionStatusScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var hasMic by remember { mutableStateOf(PermissionManager.hasRecordAudioPermission(context)) }
    var hasNotif by remember { mutableStateOf(PermissionManager.hasNotificationPermission(context)) }
    var hasCam by remember { mutableStateOf(PermissionManager.hasCameraPermission(context)) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        hasMic = results[Manifest.permission.RECORD_AUDIO] ?: hasMic
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            hasNotif = results[Manifest.permission.POST_NOTIFICATIONS] ?: hasNotif
        }
        hasCam = results[Manifest.permission.CAMERA] ?: hasCam
    }

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
            text = "SECURITY & PERMISSION STATUS",
            color = JarvisCyanBright,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.sp
        )
        Text(
            text = "Verify device permissions required for voice protocols",
            color = JarvisTextSecondary,
            fontSize = 12.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 1. Microphone Permission
        PermissionItemCard(
            title = "Microphone Access",
            permissionName = "android.permission.RECORD_AUDIO",
            description = "Required to capture Sinhala and English voice input during push-to-talk and wake-word protocols.",
            icon = Icons.Default.Mic,
            isGranted = hasMic,
            onRequest = {
                permissionLauncher.launch(arrayOf(Manifest.permission.RECORD_AUDIO))
            }
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 2. Notification Permission
        PermissionItemCard(
            title = "Notification Controls",
            permissionName = "android.permission.POST_NOTIFICATIONS",
            description = "Required to display the persistent background listening indicator and quick stop control in the status bar.",
            icon = Icons.Default.Notifications,
            isGranted = hasNotif,
            onRequest = {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                    permissionLauncher.launch(arrayOf(Manifest.permission.POST_NOTIFICATIONS))
                }
            }
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 3. Camera / Flashlight
        PermissionItemCard(
            title = "Camera & Flashlight Torch",
            permissionName = "android.hardware.camera.flash",
            description = "Used strictly by CameraManager to toggle the physical hardware flashlight on or off.",
            icon = Icons.Default.FlashlightOn,
            isGranted = hasCam,
            onRequest = {
                permissionLauncher.launch(arrayOf(Manifest.permission.CAMERA))
            }
        )

        Spacer(modifier = Modifier.height(24.dp))

        // App Details Settings Link
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = JarvisSurface),
            border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(JarvisBorder, JarvisCyanDark)))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "System Settings Management",
                    color = JarvisTextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "If any permission was denied with 'Don't ask again', open system settings to enable manually.",
                    color = JarvisTextSecondary,
                    fontSize = 11.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = {
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.fromParts("package", context.packageName, null)
                        }
                        context.startActivity(intent)
                    },
                    modifier = Modifier.fillMaxWidth().testTag("open_system_settings_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = JarvisSurfaceElevated),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Settings, contentDescription = null, tint = JarvisCyan, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Open Android App Settings", color = JarvisCyanBright)
                }
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}

@Composable
fun PermissionItemCard(
    title: String,
    permissionName: String,
    description: String,
    icon: ImageVector,
    isGranted: Boolean,
    onRequest: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = JarvisSurface),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.horizontalGradient(
                listOf(
                    if (isGranted) Color(0xFF0E7490) else Color(0xFF7F1D1D),
                    JarvisBorder
                )
            )
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(if (isGranted) Color(0xFF164E63) else Color(0xFF334155)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = title,
                            tint = if (isGranted) JarvisCyanBright else JarvisTextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(title, color = JarvisTextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        Text(permissionName, color = JarvisTextTertiary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isGranted) Color(0x2210B981) else Color(0x22EF4444))
                        .border(1.dp, if (isGranted) Color(0xFF10B981) else Color(0xFFEF4444), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (isGranted) "GRANTED" else "REQUIRED",
                        color = if (isGranted) Color(0xFF10B981) else Color(0xFFEF4444),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = description,
                color = JarvisTextSecondary,
                fontSize = 12.sp,
                lineHeight = 17.sp
            )

            if (!isGranted) {
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = onRequest,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = JarvisCyan),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Grant Permission", color = JarvisNavyBlack, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
