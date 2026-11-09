package com.ghazarian.wordclock.di

import android.content.Context
import com.ghazarian.wordclock.core.data.repository.createAndroidWordClockRepository
import com.ghazarian.wordclock.core.domain.connectivity.WifiAvailabilityMonitor
import com.ghazarian.wordclock.core.domain.repository.WordClockRepository
import com.ghazarian.wordclock.core.storage.ThemeModeStorage
import com.ghazarian.wordclock.core.storage.createAndroidThemeModeStorage
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
    fun provideThemeModeStorage(
        @ApplicationContext context: Context,
    ): ThemeModeStorage = createAndroidThemeModeStorage(context)

    @Provides
    @Singleton
    fun provideWifiAvailabilityMonitor(
        @ApplicationContext context: Context,
    ): WifiAvailabilityMonitor = AndroidWifiAvailabilityMonitor(context)

}
