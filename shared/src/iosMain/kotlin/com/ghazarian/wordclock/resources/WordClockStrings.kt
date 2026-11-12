package com.ghazarian.wordclock.resources

import dev.icerock.moko.resources.desc.PluralFormatted
import dev.icerock.moko.resources.desc.ResourceFormatted
import dev.icerock.moko.resources.desc.StringDesc

/** String formatting bridge used by the native SwiftUI app. */
object WordClockStrings {
    fun connectedDeviceCount(count: Int): StringDesc =
        StringDesc.PluralFormatted(MR.plurals.connected_device_count, count, count)

    fun deviceFirmware(version: String): StringDesc =
        StringDesc.ResourceFormatted(MR.strings.ios_device_firmware, version)

    fun removeDialogMessage(deviceName: String): StringDesc =
        StringDesc.ResourceFormatted(MR.strings.remove_dialog_message, deviceName)

    fun deviceAdded(deviceName: String): StringDesc =
        StringDesc.ResourceFormatted(MR.strings.device_added, deviceName)

    fun devicePowerTurnOff(deviceName: String): StringDesc =
        StringDesc.ResourceFormatted(MR.strings.device_power_turn_off, deviceName)

    fun devicePowerTurnOn(deviceName: String): StringDesc =
        StringDesc.ResourceFormatted(MR.strings.device_power_turn_on, deviceName)

    fun clockOnSummary(brightnessPercent: Int): StringDesc =
        StringDesc.ResourceFormatted(MR.strings.clock_on_summary, brightnessPercent)

    fun brightnessPercent(percent: Int): StringDesc =
        StringDesc.ResourceFormatted(MR.strings.brightness_percent, percent)

    fun selectedColor(hexColor: String): StringDesc =
        StringDesc.ResourceFormatted(MR.strings.selected_color, hexColor)
}
