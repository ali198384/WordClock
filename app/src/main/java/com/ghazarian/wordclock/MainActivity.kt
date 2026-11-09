package com.ghazarian.wordclock

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.ghazarian.wordclock.core.domain.connectivity.WifiAvailabilityMonitor
import com.ghazarian.wordclock.core.domain.repository.WordClockRepository
import com.ghazarian.wordclock.core.storage.ThemeModeStorage
import com.ghazarian.wordclock.platform.AndroidWordClockApp
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var wordClockRepository: WordClockRepository

    @Inject
    lateinit var themeModeStorage: ThemeModeStorage

    @Inject
    lateinit var wifiAvailabilityMonitor: WifiAvailabilityMonitor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AndroidWordClockApp(
                repository = wordClockRepository,
                themeModeStorage = themeModeStorage,
                wifiAvailabilityMonitor = wifiAvailabilityMonitor,
            )
        }
    }
}
