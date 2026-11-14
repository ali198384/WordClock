package com.ghazarian.wordclock

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.os.LocaleListCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ghazarian.wordclock.appearance.AppearanceViewModel
import com.ghazarian.wordclock.core.designsystem.theme.WordClockTheme
import com.ghazarian.wordclock.navigation.AppNavHost
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val appearanceViewModel: AppearanceViewModel = hiltViewModel()
            val themeMode by appearanceViewModel.themeMode.collectAsStateWithLifecycle()
            var languageCode by remember { mutableStateOf(currentApplicationLanguage()) }
            WordClockTheme(themeMode = themeMode, languageCode = languageCode) {
                AppNavHost(
                    themeMode = themeMode,
                    languageCode = languageCode,
                    onThemeModeChanged = appearanceViewModel::setThemeMode,
                    onLanguageChanged = { newLanguage ->
                        if (newLanguage != languageCode) {
                            AppCompatDelegate.setApplicationLocales(
                                LocaleListCompat.forLanguageTags(newLanguage),
                            )
                            languageCode = newLanguage
                        }
                    },
                )
            }
        }
    }
}

private fun currentApplicationLanguage(): String {
    val selectedLanguage = AppCompatDelegate.getApplicationLocales().get(0)?.language
        ?: Locale.getDefault().language
    return if (selectedLanguage == "fa") "fa" else "en"
}
