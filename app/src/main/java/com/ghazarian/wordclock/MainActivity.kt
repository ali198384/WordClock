package com.ghazarian.wordclock

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.ghazarian.wordclock.navigation.AppNavHost
import com.ghazarian.wordclock.ui.theme.WordClockTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WordClockTheme {
                AppNavHost()
            }
        }
    }
}
