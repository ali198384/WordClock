import SwiftUI
import UIKit
import WordClockShared

private struct WordClockLanguageKey: EnvironmentKey {
    static let defaultValue = "en"
}

extension EnvironmentValues {
    var wordClockLanguage: String {
        get { self[WordClockLanguageKey.self] }
        set { self[WordClockLanguageKey.self] = newValue }
    }
}

private struct WordClockFontModifier: ViewModifier {
    @Environment(\.wordClockLanguage) private var language
    let size: CGFloat
    let weight: Font.Weight
    let design: Font.Design

    func body(content: Content) -> some View {
        let font: Font
        if language == "fa" {
            let name: String
            if weight == .bold || weight == .heavy || weight == .black {
                name = "Vazirmatn-Bold"
            } else if weight == .semibold {
                name = "Vazirmatn-SemiBold"
            } else if weight == .medium {
                name = "Vazirmatn-Medium"
            } else {
                name = "Vazirmatn-Regular"
            }
            font = .custom(name, size: size, relativeTo: .body)
        } else {
            font = .system(size: size, weight: weight, design: design)
        }
        return content.font(font)
    }
}

enum ClockPalette {
    private static func token(_ rgb: Int32) -> Color {
        Color(hex: UInt32(rgb))
    }

    static func background(_ scheme: ColorScheme) -> Color {
        token(scheme == .dark
            ? WordClockColorTokens.shared.darkBackground
            : WordClockColorTokens.shared.lightBackground)
    }

    static func surface(_ scheme: ColorScheme) -> Color {
        token(scheme == .dark
            ? WordClockColorTokens.shared.darkSurface
            : WordClockColorTokens.shared.lightSurface)
    }

    static func surfaceVariant(_ scheme: ColorScheme) -> Color {
        token(scheme == .dark
            ? WordClockColorTokens.shared.darkSurfaceVariant
            : WordClockColorTokens.shared.lightSurfaceVariant)
    }

    static func primary(_ scheme: ColorScheme) -> Color {
        token(scheme == .dark
            ? WordClockColorTokens.shared.darkPrimary
            : WordClockColorTokens.shared.lightPrimary)
    }

    static func secondary(_ scheme: ColorScheme) -> Color {
        token(scheme == .dark
            ? WordClockColorTokens.shared.darkSecondary
            : WordClockColorTokens.shared.lightSecondary)
    }

    static func tertiary(_ scheme: ColorScheme) -> Color {
        token(scheme == .dark
            ? WordClockColorTokens.shared.darkTertiary
            : WordClockColorTokens.shared.lightTertiary)
    }

    static func onSurface(_ scheme: ColorScheme) -> Color {
        token(scheme == .dark
            ? WordClockColorTokens.shared.darkOnSurface
            : WordClockColorTokens.shared.lightOnSurface)
    }

    static func secondaryText(_ scheme: ColorScheme) -> Color {
        token(scheme == .dark
            ? WordClockColorTokens.shared.darkSecondaryText
            : WordClockColorTokens.shared.lightSecondaryText)
    }

    static let powerOn = token(WordClockColorTokens.shared.powerOn)
    static let powerOff = token(WordClockColorTokens.shared.powerOff)
    static let error = powerOff
}

extension Color {
    init(hex: UInt32) {
        self.init(
            .sRGB,
            red: Double((hex >> 16) & 0xFF) / 255,
            green: Double((hex >> 8) & 0xFF) / 255,
            blue: Double(hex & 0xFF) / 255,
            opacity: 1
        )
    }
}

extension View {
    func wordClockFont(
        size: CGFloat,
        weight: Font.Weight = .regular,
        design: Font.Design = .default
    ) -> some View {
        modifier(WordClockFontModifier(size: size, weight: weight, design: design))
    }

    func clockCard(_ scheme: ColorScheme, tint: Color? = nil) -> some View {
        background(
            RoundedRectangle(cornerRadius: 26, style: .continuous)
                .fill(tint ?? ClockPalette.surface(scheme))
        )
        .overlay {
            RoundedRectangle(cornerRadius: 26, style: .continuous)
                .strokeBorder(ClockPalette.secondaryText(scheme).opacity(0.12), lineWidth: 1)
        }
    }
}

extension UIColor {
    var rgbComponents: (red: Int, green: Int, blue: Int) {
        var red: CGFloat = 1
        var green: CGFloat = 1
        var blue: CGFloat = 1
        var alpha: CGFloat = 1
        getRed(&red, green: &green, blue: &blue, alpha: &alpha)
        return (
            Int((red * 255).rounded()),
            Int((green * 255).rounded()),
            Int((blue * 255).rounded())
        )
    }

    func isApproximatelyEqual(to other: UIColor) -> Bool {
        let lhs = rgbComponents
        let rhs = other.rgbComponents
        return abs(lhs.red - rhs.red) < 3 &&
            abs(lhs.green - rhs.green) < 3 &&
            abs(lhs.blue - rhs.blue) < 3
    }
}

extension WordClockControlStateSnapshot {
    var color: Color {
        Color(
            .sRGB,
            red: red / 255,
            green: green / 255,
            blue: blue / 255,
            opacity: 1
        )
    }
}

extension LedColor {
    var swiftUIColor: Color {
        Color(
            .sRGB,
            red: Double(red) / 255,
            green: Double(green) / 255,
            blue: Double(blue) / 255,
            opacity: 1
        )
    }
}
