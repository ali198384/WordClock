import SwiftUI
import UIKit
import WordClockShared

struct ControlView: View {
    let deviceID: String
    @ObservedObject var model: AppModel
    @ObservedObject var wifi: WifiMonitor
    @Environment(\.colorScheme) private var colorScheme
    @Environment(\.dismiss) private var dismiss
    @State private var brightnessDraft = 128.0
    @State private var colorDraft = Color.white
    @State private var selectedTab = 0
    @State private var draftsLoaded = false
    @State private var confirmedBrightness: Int?
    @State private var confirmedColor: Color?

    private var snapshot: ControlSnapshot? {
        model.control?.device.id == deviceID ? model.control : nil
    }

    private var canControl: Bool {
        wifi.isAvailable && snapshot != nil && snapshot?.device.isOnline != false
    }

    var body: some View {
        ScrollView {
            VStack(spacing: 16) {
                topBar
                if model.isLoadingControl && snapshot == nil {
                    ProgressView("Getting your clock’s status…")
                        .frame(maxWidth: .infinity, minHeight: 180)
                } else if let snapshot {
                    connectionBanner
                    powerCard(snapshot)
                    brightnessCard(snapshot)
                    colorCard(snapshot)
                    detailsCard(snapshot)
                } else {
                    unavailableCard
                }
            }
            .padding(.horizontal, 18)
            .padding(.top, 10)
            .padding(.bottom, 30)
            .frame(maxWidth: 760)
            .frame(maxWidth: .infinity)
        }
        .background(ClockPalette.background(colorScheme).ignoresSafeArea())
        .toolbar(.hidden, for: .navigationBar)
        .onAppear {
            model.loadControl(for: deviceID)
        }
        .onChange(of: model.controlRevision) { _ in
            guard let value = model.control, value.device.id == deviceID else { return }
            let latestBrightness = Int(value.state.brightness)
            if confirmedBrightness == nil || Int(brightnessDraft.rounded()) == confirmedBrightness {
                brightnessDraft = Double(latestBrightness)
            }
            if let confirmedColor,
               UIColor(colorDraft).isApproximatelyEqual(to: UIColor(confirmedColor)) {
                colorDraft = value.state.color
            } else if confirmedColor == nil {
                colorDraft = value.state.color
            }
            confirmedBrightness = latestBrightness
            confirmedColor = value.state.color
            draftsLoaded = true
        }
        .onChange(of: brightnessDraft) { value in
            guard canControl else { return }
            model.setBrightness(Int(value.rounded()))
        }
        .onChange(of: colorDraft) { value in
            guard canControl else { return }
            model.setColor(value)
        }
        .onChange(of: wifi.isAvailable) { available in
            if available { model.loadControl(for: deviceID) }
        }
        .alert("Something went wrong", isPresented: errorPresented) {
            Button("OK", role: .cancel) { model.clearNotices() }
        } message: {
            Text(model.errorMessage ?? "Please try again.")
        }
    }

    private var topBar: some View {
        HStack(spacing: 8) {
            Button { dismiss() } label: {
                Image(systemName: "chevron.left")
                    .font(.system(size: 18, weight: .semibold))
                    .frame(width: 42, height: 42)
                    .contentShape(Rectangle())
            }
            .accessibilityLabel("Back to devices")
            VStack(spacing: 2) {
                Text(snapshot?.device.name ?? "Clock control")
                    .font(.system(size: 17, weight: .semibold))
                    .lineLimit(1)
                Text(snapshot?.device.ipAddress ?? "")
                    .font(.system(size: 12, design: .monospaced))
                    .foregroundStyle(ClockPalette.secondaryText(colorScheme))
                    .lineLimit(1)
            }
            .frame(maxWidth: .infinity)
            Button { model.loadControl(for: deviceID) } label: {
                if model.isLoadingControl {
                    ProgressView().frame(width: 42, height: 42)
                } else {
                    Image(systemName: "arrow.clockwise")
                        .font(.system(size: 17, weight: .semibold))
                        .frame(width: 42, height: 42)
                }
            }
            .disabled(model.isLoadingControl || !wifi.isAvailable)
            .accessibilityLabel("Refresh clock status")
        }
        .foregroundStyle(ClockPalette.primary(colorScheme))
        .frame(height: 54)
    }

    private var connectionBanner: some View {
        let online = wifi.isAvailable && snapshot?.device.isOnline == true
        return HStack(spacing: 10) {
            Image(systemName: online ? "wifi" : "wifi.slash")
            Text(online ? "Connected to your clock" : (wifi.isAvailable ? "Clock is unavailable" : "Wi-Fi is not connected"))
                .font(.system(size: 14, weight: .medium))
            Spacer()
            Circle().fill(online ? ClockPalette.powerOn : ClockPalette.error).frame(width: 8, height: 8)
        }
        .foregroundStyle(online ? ClockPalette.powerOn : ClockPalette.error)
        .padding(14)
        .background((online ? ClockPalette.powerOn : ClockPalette.error).opacity(0.1), in: RoundedRectangle(cornerRadius: 15))
    }

    private func powerCard(_ value: ControlSnapshot) -> some View {
        HStack(spacing: 16) {
            VStack(alignment: .leading, spacing: 5) {
                Text(value.state.isOn ? "Clock is on" : "Clock is off")
                    .font(.system(size: 21, weight: .semibold))
                Text(value.state.isOn ? "\(Int(value.state.brightness * 100 / 255))% brightness" : "LEDs are currently off")
                    .font(.system(size: 14))
                    .foregroundStyle(ClockPalette.secondaryText(colorScheme))
            }
            Spacer()
            Button { model.setControlPower() } label: {
                Image(systemName: "power")
                    .font(.system(size: 23, weight: .semibold))
                    .foregroundStyle(value.state.isOn ? ClockPalette.powerOn : ClockPalette.powerOff)
                    .frame(width: 58, height: 58)
                    .background((value.state.isOn ? ClockPalette.powerOn : ClockPalette.powerOff).opacity(0.13), in: Circle())
            }
            .disabled(!canControl || model.workingDeviceIDs.contains(deviceID))
            .accessibilityLabel(value.state.isOn ? "Turn off" : "Turn on")
        }
        .padding(20)
        .clockCard(colorScheme, tint: ClockPalette.primary(colorScheme).opacity(colorScheme == .dark ? 0.14 : 0.07))
    }

    private func brightnessCard(_ value: ControlSnapshot) -> some View {
        VStack(alignment: .leading, spacing: 14) {
            HStack {
                Label("Brightness", systemImage: "sun.max.fill")
                    .font(.system(size: 18, weight: .semibold))
                    .foregroundStyle(ClockPalette.tertiary(colorScheme))
                Spacer()
                Text("\(Int(brightnessDraft.rounded()) * 100 / 255)%")
                    .font(.system(size: 16, weight: .semibold, design: .rounded))
                    .foregroundStyle(ClockPalette.primary(colorScheme))
            }
            Slider(value: $brightnessDraft, in: 0...255, step: 1)
                .tint(ClockPalette.tertiary(colorScheme))
                .disabled(!canControl)
                .accessibilityLabel("Brightness from zero to 255")
            HStack {
                Text("LED light intensity")
                    .font(.system(size: 13))
                    .foregroundStyle(ClockPalette.secondaryText(colorScheme))
            }
        }
        .padding(20)
        .clockCard(colorScheme)
    }

    private func colorCard(_ value: ControlSnapshot) -> some View {
        VStack(alignment: .leading, spacing: 15) {
            HStack(spacing: 10) {
                Image(systemName: "paintpalette.fill")
                    .foregroundStyle(ClockPalette.secondary(colorScheme))
                Text("LED color").font(.system(size: 18, weight: .semibold))
                Spacer()
                Circle().fill(colorDraft).frame(width: 26, height: 26)
                    .padding(4)
                    .overlay(Circle().stroke(colorDraft, lineWidth: 2))
            }
            Picker("Color mode", selection: $selectedTab) {
                Text("Colors").tag(0)
                Text("Whites").tag(1)
            }
            .pickerStyle(.segmented)
            if selectedTab == 0 {
                HStack(spacing: 18) {
                    LedColorWheel(color: $colorDraft)
                        .frame(width: 190, height: 190)
                        .disabled(!canControl)
                    VStack(alignment: .leading, spacing: 11) {
                        Text("Choose color")
                            .font(.system(size: 15, weight: .semibold))
                        ForEach(ColorPreset.colors, id: \.name) { preset in
                            Button {
                                colorDraft = preset.color
                            } label: {
                                HStack(spacing: 8) {
                                    Circle().fill(preset.color).frame(width: 17, height: 17)
                                    Text(preset.name).font(.system(size: 13))
                                    Spacer(minLength: 0)
                                    if UIColor(colorDraft).isApproximatelyEqual(to: UIColor(preset.color)) {
                                        Image(systemName: "checkmark").font(.system(size: 11, weight: .bold))
                                    }
                                }
                                .foregroundStyle(ClockPalette.onSurface(colorScheme))
                            }
                            .buttonStyle(.plain)
                            .disabled(!canControl)
                        }
                    }
                    .frame(maxWidth: .infinity, alignment: .leading)
                }
                Text("Drag on the wheel to choose a color.")
                    .font(.system(size: 13))
                    .foregroundStyle(ClockPalette.secondaryText(colorScheme))
            } else {
                Text("White tones are adjusted using RGB values.")
                    .font(.system(size: 13))
                    .foregroundStyle(ClockPalette.secondaryText(colorScheme))
                HStack(spacing: 10) {
                    ForEach(ColorPreset.whites, id: \.name) { preset in
                        Button {
                            colorDraft = preset.color
                        } label: {
                            VStack(spacing: 8) {
                                Circle().fill(preset.color)
                                    .frame(width: 43, height: 43)
                                    .overlay(Circle().stroke(ClockPalette.secondaryText(colorScheme).opacity(0.3), lineWidth: 1))
                                Text(preset.name)
                                    .font(.system(size: 11, weight: .medium))
                                    .lineLimit(1)
                                    .minimumScaleFactor(0.8)
                            }
                            .frame(maxWidth: .infinity)
                            .foregroundStyle(ClockPalette.onSurface(colorScheme))
                        }
                        .buttonStyle(.plain)
                        .disabled(!canControl)
                    }
                }
                .padding(.vertical, 6)
            }
            HStack {
                Text(String(format: "Selected color · #%02X%02X%02X", UIColor(colorDraft).rgbComponents.red, UIColor(colorDraft).rgbComponents.green, UIColor(colorDraft).rgbComponents.blue))
                    .font(.system(size: 13, weight: .medium, design: .monospaced))
                    .foregroundStyle(ClockPalette.secondaryText(colorScheme))
            }
        }
        .padding(20)
        .clockCard(colorScheme)
        .onAppear {
            if !draftsLoaded { colorDraft = value.state.color }
        }
    }

    private func detailsCard(_ value: ControlSnapshot) -> some View {
        VStack(alignment: .leading, spacing: 12) {
            Label("Clock details", systemImage: "info.circle.fill")
                .font(.system(size: 18, weight: .semibold))
                .foregroundStyle(ClockPalette.secondary(colorScheme))
            detailRow("Firmware", value.device.firmwareVersion ?? "Unknown")
            detailRow("Physical LEDs", value.device.physicalLedCount ?? "Unknown")
            detailRow("Active segment LEDs", value.state.physicalLedCount ?? "Unknown")
            detailRow("Segments", value.state.segmentCount)
        }
        .padding(20)
        .clockCard(colorScheme)
    }

    private var unavailableCard: some View {
        VStack(spacing: 12) {
            Image(systemName: "wifi.slash").font(.system(size: 30)).foregroundStyle(ClockPalette.error)
            Text("Clock is unavailable.").font(.system(size: 18, weight: .semibold))
            Text(wifi.isAvailable ? "Check the clock’s local IP address and try again." : "Wi-Fi will be checked automatically when it reconnects.")
                .font(.system(size: 14))
                .multilineTextAlignment(.center)
                .foregroundStyle(ClockPalette.secondaryText(colorScheme))
            Button("Try again") { model.loadControl(for: deviceID) }
                .font(.system(size: 15, weight: .semibold))
                .disabled(!wifi.isAvailable)
        }
        .frame(maxWidth: .infinity)
        .padding(24)
        .clockCard(colorScheme)
    }

    private func detailRow(_ title: String, _ value: String) -> some View {
        HStack {
            Text(title).foregroundStyle(ClockPalette.secondaryText(colorScheme))
            Spacer()
            Text(value).fontWeight(.medium)
        }
        .font(.system(size: 14))
    }

    private var errorPresented: Binding<Bool> {
        Binding(get: { model.errorMessage != nil }, set: { if !$0 { model.clearNotices() } })
    }
}

private struct LedColorWheel: View {
    @Binding var color: Color

    private let hues: [Color] = [
        .red, .orange, .yellow, .green, .cyan, .blue, .purple, .red
    ]

    var body: some View {
        GeometryReader { proxy in
            let size = min(proxy.size.width, proxy.size.height)
            ZStack {
                Circle()
                    .fill(AngularGradient(colors: hues, center: .center))
                    .overlay {
                        Circle().fill(
                            RadialGradient(
                                colors: [.white.opacity(0.98), .white.opacity(0.75), .clear],
                                center: .center,
                                startRadius: 0,
                                endRadius: size * 0.51
                            )
                        )
                    }
                    .clipShape(Circle())
                Circle().stroke(.white.opacity(0.5), lineWidth: 2)
                Circle()
                    .fill(color)
                    .frame(width: 20, height: 20)
                    .overlay(Circle().stroke(.white, lineWidth: 3))
                    .shadow(color: .black.opacity(0.25), radius: 4, y: 2)
                    .offset(selectionOffset(in: size))
            }
            .frame(width: size, height: size)
            .contentShape(Circle())
            .gesture(DragGesture(minimumDistance: 0).onChanged { value in
                let center = CGPoint(x: size / 2, y: size / 2)
                let selected = LedColorMath.shared.colorAt(
                    x: Double(value.location.x),
                    y: Double(value.location.y),
                    centerX: Double(center.x),
                    centerY: Double(center.y),
                    radius: Double(size / 2)
                )
                color = selected.swiftUIColor
            })
        }
        .aspectRatio(1, contentMode: .fit)
        .accessibilityLabel("RGB color wheel")
    }

    private func selectionOffset(in size: CGFloat) -> CGSize {
        let components = UIColor(color).rgbComponents
        let position = LedColorMath.shared.hueAndSaturation(
            color: LedColor(
                red: Int32(components.red),
                green: Int32(components.green),
                blue: Int32(components.blue)
            )
        )
        let angle = position.hueDegrees * .pi / 180
        let radius = position.saturation * size * 0.46
        return CGSize(width: cos(angle) * radius, height: sin(angle) * radius)
    }
}

private struct ColorPreset {
    let name: String
    let color: Color

    static let colors: [ColorPreset] = WordClockColorPresets.shared.quickColors.map {
        ColorPreset(name: displayName(for: $0.id), color: $0.color.swiftUIColor)
    }

    static let whites: [ColorPreset] = WordClockColorPresets.shared.whiteColors.map {
        ColorPreset(name: displayName(for: $0.id), color: $0.color.swiftUIColor)
    }

    private static func displayName(for id: String) -> String {
        switch id {
        case "warm_white": return "Warm"
        case "soft_white": return "Soft"
        case "natural_white": return "Natural"
        case "day_light": return "Daylight"
        case "cool_white": return "Cool"
        case "blue_sky": return "Blue Sky"
        default: return id.capitalized
        }
    }
}

