package com.ghazarian.wordclock.feature.control.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModelStore
import app.cash.turbine.test
import com.ghazarian.wordclock.core.domain.connectivity.WifiAvailabilityMonitor
import com.ghazarian.wordclock.core.domain.error.WordClockDeviceConnectionException
import com.ghazarian.wordclock.core.domain.usecase.GetDevices
import com.ghazarian.wordclock.core.domain.usecase.GetWordClockState
import com.ghazarian.wordclock.core.domain.usecase.SetWordClockBrightness
import com.ghazarian.wordclock.core.domain.usecase.SetWordClockColor
import com.ghazarian.wordclock.core.domain.usecase.SetWordClockPower
import com.ghazarian.wordclock.core.testing.FakeWordClockRepository
import com.ghazarian.wordclock.core.testing.WordClockFixtures
import com.ghazarian.wordclock.feature.control.navigation.ControlRouteArguments
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.Test
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalCoroutinesApi::class)
class ControlViewModelTest {
    @Test
    fun retriesAfterWifiReconnectAndAllowsPowerControlWhenDeviceResponds() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val viewModelStore = ViewModelStore()
        try {
            val device = WordClockFixtures.device()
            val repository = FakeWordClockRepository(initialDevices = listOf(device))
            var stateRequests = 0
            repository.getStateHandler = {
                stateRequests += 1
                if (stateRequests == 1) throw WordClockDeviceConnectionException()
                WordClockFixtures.state(isOn = true)
            }
            val wifiState = MutableStateFlow(false)
            val viewModel = ControlViewModel(
                savedStateHandle = SavedStateHandle(
                    mapOf(ControlRouteArguments.DEVICE_ID to device.id),
                ),
                wifiAvailabilityMonitor = FakeWifiAvailabilityMonitor(wifiState),
                getDevices = GetDevices(repository),
                getWordClockState = GetWordClockState(repository),
                setWordClockPower = SetWordClockPower(repository),
                setWordClockBrightness = SetWordClockBrightness(repository),
                setWordClockColor = SetWordClockColor(repository),
            )
            viewModelStore.put("control", viewModel)
            runCurrent()

            viewModel.uiState.test {
                assertThat(awaitItem().isDeviceOnline).isFalse()
                viewModel.togglePower()
                assertThat(repository.powerRequests).isEmpty()

                wifiState.value = true
                runCurrent()
                advanceTimeBy(750.milliseconds)
                runCurrent()
                advanceTimeBy(1_500.milliseconds)
                runCurrent()

                var recoveredState = awaitItem()
                while (!recoveredState.isDeviceOnline) {
                    recoveredState = awaitItem()
                }
                assertThat(stateRequests).isEqualTo(2)

                viewModel.togglePower()
                runCurrent()

                var poweredOffState = awaitItem()
                while (poweredOffState.deviceState?.isOn != false) {
                    poweredOffState = awaitItem()
                }
                assertThat(repository.powerRequests).containsExactly(device.id to false)
                cancelAndIgnoreRemainingEvents()
            }
        } finally {
            viewModelStore.clear()
            Dispatchers.resetMain()
        }
    }

    private class FakeWifiAvailabilityMonitor(
        override val isWifiAvailable: StateFlow<Boolean>,
    ) : WifiAvailabilityMonitor
}
