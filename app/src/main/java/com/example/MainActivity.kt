package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.data.TapPreferences
import com.example.ui.HomeScreen
import com.example.ui.TikTokSimulatorScreen
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.TapTapTheme

enum class Screen {
    HOME,
    SIMULATOR
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TapTapTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = DarkBackground
                ) {
                    TapTapApp()
                }
            }
        }
    }
}

@Composable
fun TapTapApp() {
    val context = LocalContext.current
    val tapPreferences = remember { TapPreferences(context) }
    var currentScreen by remember { mutableStateOf(Screen.HOME) }

    AnimatedContent(
        targetState = currentScreen,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        label = "screen_transition"
    ) { screen ->
        when (screen) {
            Screen.HOME -> {
                HomeScreen(
                    onNavigateToSimulator = {
                        currentScreen = Screen.SIMULATOR
                    }
                )
            }
            Screen.SIMULATOR -> {
                TikTokSimulatorScreen(
                    initialConfig = tapPreferences.loadConfig(),
                    onBack = {
                        currentScreen = Screen.HOME
                    }
                )
            }
        }
    }
}
