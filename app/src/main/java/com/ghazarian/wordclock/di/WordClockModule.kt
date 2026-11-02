package com.ghazarian.wordclock.di

import android.content.Context
import com.ghazarian.wordclock.core.data.repository.createAndroidWordClockRepository
import com.ghazarian.wordclock.core.domain.connectivity.WifiAvailabilityMonitor
import com.ghazarian.wordclock.core.domain.repository.WordClockRepository
import com.ghazarian.wordclock.core.domain.usecase.AddWordClockDevice
import com.ghazarian.wordclock.core.domain.usecase.GetDevices
import com.ghazarian.wordclock.core.domain.usecase.GetWordClockState
import com.ghazarian.wordclock.core.domain.usecase.RefreshWordClockDevice
import com.ghazarian.wordclock.core.domain.usecase.RemoveWordClockDevice
import com.ghazarian.wordclock.core.domain.usecase.RenameWordClockDevice
import com.ghazarian.wordclock.core.domain.usecase.SetWordClockBrightness
import com.ghazarian.wordclock.core.domain.usecase.SetWordClockColor
import com.ghazarian.wordclock.core.domain.usecase.SetWordClockPower
import com.ghazarian.wordclock.network.AndroidWifiAvailabilityMonitor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object WordClockModule {

    @Provides
    @Singleton
    fun provideWordClockRepository(
        @ApplicationContext context: Context,
    ): WordClockRepository = createAndroidWordClockRepository(context)

    @Provides
    @Singleton
    fun provideWifiAvailabilityMonitor(
        @ApplicationContext context: Context,
    ): WifiAvailabilityMonitor = AndroidWifiAvailabilityMonitor(context)

    @Provides
    fun provideGetDevices(repository: WordClockRepository): GetDevices =
        GetDevices(repository)

    @Provides
    fun provideAddWordClockDevice(repository: WordClockRepository): AddWordClockDevice =
        AddWordClockDevice(repository)

    @Provides
    fun provideRenameWordClockDevice(repository: WordClockRepository): RenameWordClockDevice =
        RenameWordClockDevice(repository)

    @Provides
    fun provideRemoveWordClockDevice(repository: WordClockRepository): RemoveWordClockDevice =
        RemoveWordClockDevice(repository)

    @Provides
    fun provideRefreshWordClockDevice(repository: WordClockRepository): RefreshWordClockDevice =
        RefreshWordClockDevice(repository)

    @Provides
    fun provideGetWordClockState(repository: WordClockRepository): GetWordClockState =
        GetWordClockState(repository)

    @Provides
    fun provideSetWordClockPower(repository: WordClockRepository): SetWordClockPower =
        SetWordClockPower(repository)

    @Provides
    fun provideSetWordClockBrightness(
        repository: WordClockRepository,
    ): SetWordClockBrightness =
        SetWordClockBrightness(repository)

    @Provides
    fun provideSetWordClockColor(repository: WordClockRepository): SetWordClockColor =
        SetWordClockColor(repository)
}
