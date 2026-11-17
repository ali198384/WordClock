package com.ghazarian.wordclock.platform

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.ghazarian.wordclock.core.domain.connectivity.WifiAvailabilityMonitor
import com.ghazarian.wordclock.core.domain.repository.WordClockRepository
import com.ghazarian.wordclock.core.storage.ThemeModeStorage
import com.ghazarian.wordclock.shared.WordClockApp

@Composable
fun AndroidWordClockApp(
    repository: WordClockRepository,
    themeModeStorage: ThemeModeStorage,
    wifiAvailabilityMonitor: WifiAvailabilityMonitor,
) {
    val context = LocalContext.current
    var pendingPermissionCallbacks by remember {
        mutableStateOf<Pair<() -> Unit, () -> Unit>?>(null)
    }
    val localNetworkPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        val callbacks = pendingPermissionCallbacks
        pendingPermissionCallbacks = null
        if (granted) callbacks?.first?.invoke() else callbacks?.second?.invoke()
    }

    WordClockApp(
        repository = repository,
        themeModeStorage = themeModeStorage,
        wifiAvailabilityMonitor = wifiAvailabilityMonitor,
        requestLocalNetworkAccess = { onGranted, onDenied ->
            val needsPermission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.CINNAMON_BUN &&
                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_LOCAL_NETWORK,
                    ) != PackageManager.PERMISSION_GRANTED

            if (needsPermission) {
                pendingPermissionCallbacks = onGranted to onDenied
                localNetworkPermissionLauncher.launch(Manifest.permission.ACCESS_LOCAL_NETWORK)
            } else {
                onGranted()
            }
        },
    )
}
