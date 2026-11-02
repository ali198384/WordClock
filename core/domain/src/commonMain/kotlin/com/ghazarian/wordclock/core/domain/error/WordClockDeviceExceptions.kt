package com.ghazarian.wordclock.core.domain.error

class WordClockDeviceAlreadyAddedException : Exception()

class WordClockDeviceConnectionException(cause: Throwable? = null) : Exception(cause)

class InvalidWordClockDeviceResponseException(cause: Throwable? = null) : Exception(cause)

class WordClockDeviceNotFoundException : Exception()
