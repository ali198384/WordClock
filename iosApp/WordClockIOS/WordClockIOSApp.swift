import SwiftUI

@main
struct WordClockIOSApp: App {
    @StateObject private var model = AppModel()
    @StateObject private var wifiMonitor = WifiMonitor()

    var body: some Scene {
        WindowGroup {
            DevicesView(model: model, wifi: wifiMonitor)
                .environment(\.layoutDirection, .leftToRight)
                .preferredColorScheme(colorScheme)
                .tint(ClockPalette.primary(colorScheme ?? .light))
        }
    }

    private var colorScheme: ColorScheme? {
        switch model.themeMode {
        case "light": .light
        case "dark": .dark
        default: nil
        }
    }
}
