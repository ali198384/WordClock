import WordClockShared

enum L10n {
    static var devicesTitle: String { MR.strings().ios_devices_title.desc().localized() }
    static var devicesWifiUnavailable: String { MR.strings().ios_wifi_unavailable.desc().localized() }
    static var errorTitle: String { MR.strings().error_title.desc().localized() }
    static var ok: String { MR.strings().ok.desc().localized() }
    static var errorFallback: String { MR.strings().error_fallback.desc().localized() }
    static var removeDialogTitle: String { MR.strings().remove_dialog_title.desc().localized() }
    static var remove: String { MR.strings().delete.desc().localized() }
    static var cancel: String { MR.strings().cancel.desc().localized() }
    static var screenTagline: String { MR.strings().screen_tagline.desc().localized() }
    static var screenDescription: String { MR.strings().screen_description.desc().localized() }
    static var addDeviceTitle: String { MR.strings().ios_add_device_title.desc().localized() }
    static var addDeviceDescription: String { MR.strings().ios_add_device_description.desc().localized() }
    static var addDeviceButton: String { MR.strings().ios_add_device_button.desc().localized() }
    static var connectingDevice: String { MR.strings().connecting_device.desc().localized() }
    static var ipAddressPlaceholder: String { MR.strings().ios_ip_address_placeholder.desc().localized() }
    static var nameOptional: String { MR.strings().device_name_optional.desc().localized() }
    static var deviceListTitle: String { MR.strings().device_list_title.desc().localized() }
    static var refreshDevicesDescription: String { MR.strings().refresh_devices_description.desc().localized() }
    static var emptyDevicesTitle: String { MR.strings().ios_empty_devices_title.desc().localized() }
    static var emptyDevicesDescription: String { MR.strings().ios_empty_devices_description.desc().localized() }
    static var deviceOnline: String { MR.strings().device_online.desc().localized() }
    static var deviceOffline: String { MR.strings().device_offline.desc().localized() }
    static var renameDevice: String { MR.strings().rename_device.desc().localized() }
    static var removeDevice: String { MR.strings().remove_device.desc().localized() }
    static var deviceNameLabel: String { MR.strings().device_name_label.desc().localized() }
    static var renameDialogTitle: String { MR.strings().rename_dialog_title.desc().localized() }
    static var save: String { MR.strings().save.desc().localized() }
    static var loadingDevices: String { MR.strings().loading_devices.desc().localized() }
    static var controlTitle: String { MR.strings().control_title.desc().localized() }
    static var loadingDeviceState: String { MR.strings().loading_device_state.desc().localized() }
    static var backToDevicesDescription: String { MR.strings().back_to_devices_description.desc().localized() }
    static var refreshClockDescription: String { MR.strings().refresh_clock_description.desc().localized() }
    static var connectedToClock: String { MR.strings().ios_connected_to_clock.desc().localized() }
    static var deviceUnavailable: String { MR.strings().device_unavailable.desc().localized() }
    static var wifiNotConnected: String { MR.strings().wifi_not_connected.desc().localized() }
    static var clockOn: String { MR.strings().ios_clock_on.desc().localized() }
    static var clockOff: String { MR.strings().ios_clock_off.desc().localized() }
    static var powerOffSummary: String { MR.strings().ios_power_off_summary.desc().localized() }
    static var brightnessTitle: String { MR.strings().brightness_title.desc().localized() }
    static var brightnessDescription: String { MR.strings().brightness_description.desc().localized() }
    static var brightnessSliderDescription: String { MR.strings().brightness_slider_description.desc().localized() }
    static var controlPowerTurnOff: String { MR.strings().control_power_turn_off.desc().localized() }
    static var controlPowerTurnOn: String { MR.strings().control_power_turn_on.desc().localized() }
    static var colorTitle: String { MR.strings().color_title.desc().localized() }
    static var colorMode: String { MR.strings().ios_color_mode.desc().localized() }
    static var colorsTab: String { MR.strings().colors_tab.desc().localized() }
    static var whitesTab: String { MR.strings().whites_tab.desc().localized() }
    static var chooseColor: String { MR.strings().choose_color.desc().localized() }
    static var colorWheelDescription: String { MR.strings().color_wheel_description.desc().localized() }
    static var whiteMixDescription: String { MR.strings().white_mix_description.desc().localized() }
    static var deviceInfoTitle: String { MR.strings().device_info_title.desc().localized() }
    static var firmware: String { MR.strings().firmware.desc().localized() }
    static var unknown: String { MR.strings().unknown.desc().localized() }
    static var physicalLedCount: String { MR.strings().physical_led_count.desc().localized() }
    static var activeSegmentLedCount: String { MR.strings().active_segment_led_count.desc().localized() }
    static var segmentCount: String { MR.strings().segment_count.desc().localized() }
    static var unavailableCheckIp: String { MR.strings().ios_unavailable_check_ip.desc().localized() }
    static var wifiReconnectShort: String { MR.strings().ios_wifi_reconnect_short.desc().localized() }
    static var retry: String { MR.strings().retry.desc().localized() }
    static var colorWheelContentDescription: String { MR.strings().color_wheel_content_description.desc().localized() }
    static var themeSwitchToDark: String { MR.strings().theme_switch_to_dark.desc().localized() }
    static var themeSwitchToLight: String { MR.strings().theme_switch_to_light.desc().localized() }

    static func connectedDeviceCount(_ count: Int) -> String {
        WordClockStrings.shared.connectedDeviceCount(count: Int32(count)).localized()
    }

    static func removeDialogMessage(_ deviceName: String) -> String {
        WordClockStrings.shared.removeDialogMessage(deviceName: deviceName).localized()
    }

    static func deviceFirmware(_ version: String) -> String {
        WordClockStrings.shared.deviceFirmware(version: version).localized()
    }

    static func devicePowerLabel(isOn: Bool, deviceName: String) -> String {
        let description = isOn
            ? WordClockStrings.shared.devicePowerTurnOff(deviceName: deviceName)
            : WordClockStrings.shared.devicePowerTurnOn(deviceName: deviceName)
        return description.localized()
    }

    static func clockOnSummary(_ percent: Int) -> String {
        WordClockStrings.shared.clockOnSummary(brightnessPercent: Int32(percent)).localized()
    }

    static func brightnessPercent(_ percent: Int) -> String {
        WordClockStrings.shared.brightnessPercent(percent: Int32(percent)).localized()
    }

    static func selectedColor(_ hex: String) -> String {
        WordClockStrings.shared.selectedColor(hexColor: hex).localized()
    }

    static func deviceAdded(_ deviceName: String) -> String {
        WordClockStrings.shared.deviceAdded(deviceName: deviceName).localized()
    }

    static var clockNameUpdated: String { MR.strings().device_renamed.desc().localized() }
    static var clockRemoved: String { MR.strings().device_removed.desc().localized() }
    static var emptyResponse: String { MR.strings().empty_response.desc().localized() }

    static func errorMessage(for code: String) -> String {
        let strings = MR.strings()
        switch code {
        case "devices_load_error": return strings.devices_load_error.desc().localized()
        case "device_already_added": return strings.device_already_added.desc().localized()
        case "invalid_word_clock_response": return strings.invalid_word_clock_response.desc().localized()
        case "invalid_device_response": return strings.invalid_device_response.desc().localized()
        case "device_connection_error": return strings.device_connection_error.desc().localized()
        case "control_device_connection_error": return strings.control_device_connection_error.desc().localized()
        case "device_not_found": return strings.device_not_found.desc().localized()
        case "control_device_not_found": return strings.control_device_not_found.desc().localized()
        case "generic_device_error": return strings.generic_device_error.desc().localized()
        default: return strings.generic_control_error.desc().localized()
        }
    }

    static func colorPreset(_ id: String) -> String {
        let strings = MR.strings()
        switch id {
        case "red": return strings.color_red.desc().localized()
        case "orange": return strings.color_orange.desc().localized()
        case "yellow": return strings.color_yellow.desc().localized()
        case "green": return strings.color_green.desc().localized()
        case "cyan": return strings.color_cyan.desc().localized()
        case "blue": return strings.color_blue.desc().localized()
        case "purple": return strings.color_purple.desc().localized()
        case "white": return strings.color_white.desc().localized()
        case "warm_white": return strings.color_warm_white.desc().localized()
        case "soft_white": return strings.color_soft_white.desc().localized()
        case "natural_white": return strings.color_natural_white.desc().localized()
        case "day_light": return strings.color_day_light.desc().localized()
        case "cool_white": return strings.color_cool_white.desc().localized()
        case "blue_sky": return strings.color_blue_sky.desc().localized()
        default: return strings.color_blue_sky.desc().localized()
        }
    }
}
