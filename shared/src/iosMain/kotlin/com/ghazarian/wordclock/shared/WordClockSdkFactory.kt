package com.ghazarian.wordclock.shared

import com.ghazarian.wordclock.core.data.repository.createIosWordClockRepository
import com.ghazarian.wordclock.core.storage.createIosThemeModeStorage

object WordClockSdkFactory {
    fun create(): WordClockSdk = WordClockSdk(
        repository = createIosWordClockRepository(),
        themeModeStorage = createIosThemeModeStorage(),
    )
}
