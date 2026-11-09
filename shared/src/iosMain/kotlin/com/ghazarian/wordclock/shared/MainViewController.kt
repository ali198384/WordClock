package com.ghazarian.wordclock.shared

import androidx.compose.ui.window.ComposeUIViewController
import com.ghazarian.wordclock.core.data.repository.createIosWordClockRepository
import com.ghazarian.wordclock.core.storage.createIosThemeModeStorage
import com.ghazarian.wordclock.network.createIosWifiAvailabilityMonitor
import platform.UIKit.UIViewController

fun MainViewController(): UIViewController = ComposeUIViewController {
    WordClockApp(
        repository = createIosWordClockRepository(),
        themeModeStorage = createIosThemeModeStorage(),
        wifiAvailabilityMonitor = createIosWifiAvailabilityMonitor(),
        requestLocalNetworkAccess = { onGranted, _ -> onGranted() },
    )
}
