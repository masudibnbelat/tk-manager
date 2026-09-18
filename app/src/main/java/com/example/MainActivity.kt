package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.MainAppScreen
import com.example.ui.screens.onboarding.WelcomeScreen
import com.example.ui.theme.TkManagerTheme
import com.example.ui.viewmodel.FinanceViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: FinanceViewModel = viewModel()
            val themeMode by viewModel.themeMode.collectAsState()
            val hasCompletedSetup by viewModel.hasCompletedSetup.collectAsState()

            val systemInDark = isSystemInDarkTheme()
            val isDark = when (themeMode) {
                "DARK" -> true
                "LIGHT" -> false
                else -> systemInDark
            }

            DisposableEffect(isDark) {
                enableEdgeToEdge(
                    statusBarStyle = if (isDark) {
                        SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
                    } else {
                        SystemBarStyle.light(
                            android.graphics.Color.TRANSPARENT,
                            android.graphics.Color.TRANSPARENT
                        )
                    },
                    navigationBarStyle = if (isDark) {
                        SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
                    } else {
                        SystemBarStyle.light(
                            android.graphics.Color.TRANSPARENT,
                            android.graphics.Color.TRANSPARENT
                        )
                    }
                )
                val insetsController = WindowCompat.getInsetsController(window, window.decorView)
                insetsController.isAppearanceLightStatusBars = !isDark
                insetsController.isAppearanceLightNavigationBars = !isDark
                onDispose {}
            }

            TkManagerTheme(themeMode = themeMode) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    // Bypass onboarding if setup (accountName and currency) exists in local storage
                    when (hasCompletedSetup) {
                        true -> MainAppScreen(viewModel = viewModel)
                        false -> WelcomeScreen(
                            onComplete = { name, currency, avatar ->
                                viewModel.completeOnboarding(name, currency, avatar)
                            }
                        )
                        null -> {
                            // Initial state while loading DataStore
                            androidx.compose.foundation.layout.Box(modifier = Modifier.fillMaxSize())
                        }
                    }
                }
            }
        }
    }
}
