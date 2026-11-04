package com.ghazarian.wordclock.core.domain.usecase

import com.ghazarian.wordclock.core.model.LedColor
import com.ghazarian.wordclock.core.testing.FakeWordClockRepository
import com.ghazarian.wordclock.core.testing.WordClockFixtures
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class WordClockUseCasesTest {
    @Test
    fun getDevicesReturnsRepositoryDevices() = runTest {
        val device = WordClockFixtures.device()
        val repository = FakeWordClockRepository(initialDevices = listOf(device))

        assertEquals(listOf(device), GetDevices(repository)())
    }

    @Test
    fun setPowerAndColorForwardChangesToRepository() = runTest {
        val device = WordClockFixtures.device()
        val repository = FakeWordClockRepository(initialDevices = listOf(device))
        repository.states[device.id] = WordClockFixtures.state(isOn = true)

        val updatedPower = SetWordClockPower(repository)(device.id, false)
        val updatedColor = SetWordClockColor(repository)(device.id, LedColor(12, 34, 56))

        assertEquals(false, updatedPower.isOn)
        assertEquals(LedColor(12, 34, 56), updatedColor.color)
        assertEquals(listOf(device.id to false), repository.powerRequests)
    }
}
