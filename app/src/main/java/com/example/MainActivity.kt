package com.example

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lockscreen.LockScreenManager
import com.example.ui.JarvisViewModel
import com.example.ui.screens.CommandHistoryScreen
import com.example.ui.screens.MainAssistantScreen
import com.example.ui.screens.PermissionStatusScreen
import com.example.ui.screens.PhoneControlsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisCyanBright
import com.example.ui.theme.JarvisDeepNavy
import com.example.ui.theme.JarvisNavyBlack
import com.example.ui.theme.JarvisSurfaceElevated
import com.example.ui.theme.JarvisTextSecondary
import com.example.ui.theme.JarvisTextTertiary
import com.example.ui.theme.MyApplicationTheme
import com.example.util.PermissionManager

enum class JarvisScreen(val label: String, val icon: ImageVector) {
    ASSISTANT("Jarvis", Icons.Default.Mic),
    CONTROLS("Controls", Icons.Default.Build),
    HISTORY("History", Icons.Default.History),
    PERMISSIONS("Security", Icons.Default.Security),
    SETTINGS("Settings", Icons.Default.Settings)
}

class MainActivity : ComponentActivity() {

    private val viewModel: JarvisViewModel by viewModels()
    private lateinit var lockScreenManager: LockScreenManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lockScreenManager = LockScreenManager(this)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                var currentScreen by remember { mutableStateOf(JarvisScreen.ASSISTANT) }

                // Request Audio and Notification permissions if needed
                val permissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestMultiplePermissions()
                ) {
                    // Update state if needed
                }

                LaunchedEffect(Unit) {
                    val permissions = mutableListOf(Manifest.permission.RECORD_AUDIO)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        permissions.add(Manifest.permission.POST_NOTIFICATIONS)
                    }
                    permissionLauncher.launch(permissions.toTypedArray())
                }

                val unlockTrigger by viewModel.requestUnlockTrigger.collectAsState()
                LaunchedEffect(unlockTrigger) {
                    if (unlockTrigger) {
                        requestUnlockChallenge()
                        viewModel.resetUnlockTrigger()
                    }
                }

                BackHandler(enabled = currentScreen != JarvisScreen.ASSISTANT) {
                    currentScreen = JarvisScreen.ASSISTANT
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = JarvisNavyBlack,
                    bottomBar = {
                        NavigationBar(
                            containerColor = JarvisDeepNavy,
                            contentColor = JarvisCyan,
                            tonalElevation = 8.dp,
                            modifier = Modifier.navigationBarsPadding()
                        ) {
                            JarvisScreen.values().forEach { screen ->
                                val isSelected = currentScreen == screen
                                NavigationBarItem(
                                    selected = isSelected,
                                    onClick = { currentScreen = screen },
                                    icon = {
                                        Icon(
                                            imageVector = screen.icon,
                                            contentDescription = screen.label
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = screen.label,
                                            fontSize = 11.sp
                                        )
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = JarvisNavyBlack,
                                        selectedTextColor = JarvisCyanBright,
                                        indicatorColor = JarvisCyan,
                                        unselectedIconColor = JarvisTextSecondary,
                                        unselectedTextColor = JarvisTextTertiary
                                    ),
                                    modifier = Modifier.testTag("nav_item_${screen.name.lowercase()}")
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        when (currentScreen) {
                            JarvisScreen.ASSISTANT -> MainAssistantScreen(
                                viewModel = viewModel,
                                onNavigateToPermissions = { currentScreen = JarvisScreen.PERMISSIONS }
                            )
                            JarvisScreen.CONTROLS -> PhoneControlsScreen(
                                viewModel = viewModel,
                                onRequestUnlock = { requestUnlockChallenge() }
                            )
                            JarvisScreen.HISTORY -> CommandHistoryScreen(
                                viewModel = viewModel
                            )
                            JarvisScreen.PERMISSIONS -> PermissionStatusScreen()
                            JarvisScreen.SETTINGS -> SettingsScreen(
                                viewModel = viewModel
                            )
                        }
                    }
                }
            }
        }
    }

    private fun requestUnlockChallenge() {
        lockScreenManager.requestSystemAuthentication(
            activity = this,
            onSuccess = {
                Toast.makeText(this, "Authentication verified. Phone unlocked.", Toast.LENGTH_SHORT).show()
            },
            onCancelled = {
                Toast.makeText(this, "Authentication cancelled.", Toast.LENGTH_SHORT).show()
            },
            onError = { err ->
                Toast.makeText(this, "Authentication error: $err", Toast.LENGTH_SHORT).show()
            }
        )
    }

    override fun onResume() {
        super.onResume()
        viewModel.checkServiceStatus()
    }
}
