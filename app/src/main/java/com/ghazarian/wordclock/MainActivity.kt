package com.ghazarian.wordclock

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ghazarian.wordclock.appearance.AppearanceViewModel
import com.ghazarian.wordclock.core.designsystem.theme.WordClockTheme
import com.ghazarian.wordclock.navigation.AppNavHost
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val appearanceViewModel: AppearanceViewModel = hiltViewModel()
            val themeMode by appearanceViewModel.themeMode.collectAsStateWithLifecycle()
            WordClockTheme(themeMode = themeMode) {
                AppNavHost(
                    themeMode = themeMode,
                    onThemeModeChanged = appearanceViewModel::setThemeMode
                )
            }
        }
    }
}
