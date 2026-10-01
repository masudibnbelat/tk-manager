package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.screens.MainAppScreen
import com.example.ui.screens.onboarding.OnboardingScreen
import com.example.ui.theme.AppTheme
import com.example.ui.viewmodel.FinanceViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: FinanceViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val hasCompletedSetup by viewModel.hasCompletedSetup.collectAsStateWithLifecycle()
            val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()

            val isDark = when (themeMode) {
                "DARK" -> true
                "LIGHT" -> false
                else -> isSystemInDarkTheme()
            }

            AppTheme(darkTheme = isDark) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    if (hasCompletedSetup) {
                        MainAppScreen(viewModel = viewModel)
                    } else {
                        OnboardingScreen(
                            onComplete = { name, code, symbol, avatar ->
                                viewModel.completeOnboarding(name, code, symbol, avatar)
                            }
                        )
                    }
                }
            }
        }
    }
}
