import SwiftUI
import UIKit
import WordClockShared

private let whitePresetGridColumns = Array(
    repeating: GridItem(.flexible(), spacing: 10),
    count: 2
)
private let brightnessPresets = [25, 50, 75, 100]

struct ControlView: View {
    let deviceID: String
    @ObservedObject var model: AppModel
    @ObservedObject var wifi: WifiMonitor
    @Environment(\.colorScheme) private var colorScheme
    @Environment(\.dismiss) private var dismiss
    @State private var brightnessDraft = 128.0
    @State private var colorDraft = Color.white
    @State private var whiteTemperatureKelvin = 4500
    @State private var selectedTab = 0
    @State private var isColorPickerPresented = false
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
        VStack(spacing: 0) {
            topBar
                .padding(.horizontal, 18)
                .padding(.top, 10)
                .frame(maxWidth: 760)
                .frame(maxWidth: .infinity)

            ScrollView {
                VStack(spacing: 16) {
                    if model.isLoadingControl && snapshot == nil {
                        ProgressView(L10n.loadingDeviceState)
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
                .padding(.top, 16)
                .padding(.bottom, 30)
                .frame(maxWidth: 760)
                .frame(maxWidth: .infinity)
            }
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
        .alert(L10n.errorTitle, isPresented: errorPresented) {
            Button(L10n.ok, role: .cancel) { model.clearNotices() }
        } message: {
            Text(model.errorMessage ?? L10n.errorFallback)
        }
        .sheet(isPresented: $isColorPickerPresented) {
            colorPickerSheet
                .presentationDetents([.height(560), .large])
                .presentationDragIndicator(.visible)
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
            .accessibilityLabel(L10n.backToDevicesDescription)
            VStack(spacing: 2) {
                Text(snapshot?.device.name ?? L10n.controlTitle)
                    .wordClockFont(size: 17, weight: .semibold)
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
            .accessibilityLabel(L10n.refreshClockDescription)
        }
        .foregroundStyle(ClockPalette.primary(colorScheme))
        .frame(height: 54)
    }

    private var connectionBanner: some View {
        let online = wifi.isAvailable && snapshot?.device.isOnline == true
        return HStack(spacing: 10) {
            Image(systemName: online ? "wifi" : "wifi.slash")
            Text(online ? L10n.connectedToClock : (wifi.isAvailable ? L10n.deviceUnavailable : L10n.wifiNotConnected))
                .wordClockFont(size: 14, weight: .medium)
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
                Text(value.state.isOn ? L10n.clockOn : L10n.clockOff)
                    .wordClockFont(size: 21, weight: .semibold)
                Text(value.state.isOn ? L10n.clockOnSummary(Int(value.state.brightness * 100 / 255)) : L10n.powerOffSummary)
                    .wordClockFont(size: 14)
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
            .accessibilityLabel(value.state.isOn ? L10n.controlPowerTurnOff : L10n.controlPowerTurnOn)
        }
        .padding(20)
        .clockCard(colorScheme, tint: ClockPalette.primary(colorScheme).opacity(colorScheme == .dark ? 0.14 : 0.07))
    }

    private func brightnessCard(_ value: ControlSnapshot) -> some View {
        VStack(alignment: .leading, spacing: 14) {
            HStack {
                HStack(spacing: 8) {
                    Image(systemName: "sun.max.fill")
                        .font(.system(size: 18, weight: .semibold))
                    Text(L10n.brightnessTitle)
                        .wordClockFont(size: 18, weight: .semibold)
                }
                .foregroundStyle(ClockPalette.tertiary(colorScheme))
                Spacer()
                Text(L10n.brightnessPercent(Int((brightnessDraft * 100 / 255).rounded())))
                    .wordClockFont(size: 16, weight: .semibold, design: .rounded)
                    .foregroundStyle(ClockPalette.primary(colorScheme))
            }
            Slider(value: $brightnessDraft, in: 0...255, step: 1)
                .tint(ClockPalette.tertiary(colorScheme))
                .disabled(!canControl)
                .accessibilityLabel(L10n.brightnessSliderDescription)
            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: 8) {
                    ForEach(brightnessPresets, id: \.self) { percent in
                        let presetValue = percent * 255 / 100
                        let isSelected = abs(Int(brightnessDraft.rounded()) - presetValue) <= 1
                        Button {
                            brightnessDraft = Double(presetValue)
                        } label: {
                            Text(L10n.brightnessPercent(percent))
                                .wordClockFont(size: 13, weight: .semibold)
                                .foregroundStyle(
                                    isSelected
                                        ? ClockPalette.primary(colorScheme)
                                        : ClockPalette.onSurface(colorScheme)
                                )
                                .frame(minWidth: 58, minHeight: 40)
                                .padding(.horizontal, 5)
                                .background(
                                    isSelected
                                        ? ClockPalette.primary(colorScheme).opacity(0.16)
                                        : ClockPalette.surfaceVariant(colorScheme).opacity(0.7),
                                    in: Capsule()
                                )
                                .overlay {
                                    Capsule().strokeBorder(
                                        isSelected
                                            ? ClockPalette.primary(colorScheme).opacity(0.65)
                                            : ClockPalette.secondaryText(colorScheme).opacity(0.24),
                                        lineWidth: 1
                                    )
                                }
                        }
                        .buttonStyle(.plain)
                        .disabled(!canControl)
                    }
                }
            }
            HStack {
                Text(L10n.brightnessDescription)
                    .wordClockFont(size: 13)
                    .foregroundStyle(ClockPalette.secondaryText(colorScheme))
            }
        }
        .padding(20)
        .clockCard(colorScheme)
    }

    private func colorCard(_ value: ControlSnapshot) -> some View {
        VStack(alignment: .leading, spacing: 15) {
            HStack(spacing: 12) {
                HStack(spacing: 9) {
                    Image(systemName: "paintpalette.fill")
                        .font(.system(size: 19, weight: .semibold))
                        .foregroundStyle(ClockPalette.secondary(colorScheme))
                    VStack(alignment: .leading, spacing: 3) {
                        Text(L10n.colorTitle).wordClockFont(size: 18, weight: .semibold)
                        Text(selectedColorHex)
                            .font(.system(size: 13, weight: .medium, design: .monospaced))
                            .foregroundStyle(ClockPalette.secondaryText(colorScheme))
                    }
                }
                Spacer(minLength: 4)
                Button {
                    isColorPickerPresented = true
                } label: {
                    HStack(spacing: 7) {
                        Image(systemName: "eyedropper")
                            .font(.system(size: 15, weight: .semibold))
                        Text(L10n.chooseColor)
                            .wordClockFont(size: 14, weight: .semibold)
                            .lineLimit(1)
                    }
                    .foregroundStyle(ClockPalette.primary(colorScheme))
                    .padding(.horizontal, 13)
                    .frame(minHeight: 42)
                    .background(ClockPalette.primary(colorScheme).opacity(0.1), in: Capsule())
                    .overlay(Capsule().strokeBorder(ClockPalette.primary(colorScheme).opacity(0.45)))
                }
                .buttonStyle(.plain)
                .disabled(!canControl)
            }
            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: 12) {
                    ForEach(ColorPreset.colors, id: \.name) { preset in
                        quickColorSwatch(preset)
                    }
                }
            }
        }
        .padding(20)
        .clockCard(colorScheme)
        .onAppear {
            if !draftsLoaded { colorDraft = value.state.color }
        }
    }

    private func quickColorSwatch(_ preset: ColorPreset) -> some View {
        let isSelected = UIColor(colorDraft).isApproximatelyEqual(to: UIColor(preset.color))
        let brightness = UIColor(preset.color).rgbComponents
        let checkmarkColor: Color = brightness.red + brightness.green + brightness.blue > 420
            ? ClockPalette.onSurface(colorScheme)
            : ClockPalette.surface(colorScheme)

        return Button {
            colorDraft = preset.color
        } label: {
            ZStack {
                if isSelected {
                    Circle()
                        .fill(ClockPalette.surface(colorScheme))
                        .frame(width: 50, height: 50)
                        .overlay(Circle().strokeBorder(ClockPalette.onSurface(colorScheme), lineWidth: 2))
                }
                Circle()
                    .fill(preset.color)
                    .frame(width: 42, height: 42)
                    .overlay(Circle().strokeBorder(ClockPalette.secondaryText(colorScheme).opacity(0.35), lineWidth: 1))
                if isSelected {
                    Image(systemName: "checkmark")
                        .font(.system(size: 14, weight: .bold))
                        .foregroundStyle(checkmarkColor)
                }
            }
            .frame(width: 50, height: 50)
            .contentShape(Circle())
        }
        .buttonStyle(.plain)
        .disabled(!canControl)
        .accessibilityLabel(preset.name)
        .accessibilityAddTraits(isSelected ? .isSelected : [])
    }

    private var colorPickerSheet: some View {
        VStack(spacing: 15) {
            HStack(spacing: 9) {
                Circle()
                    .fill(colorDraft)
                    .frame(width: 18, height: 18)
                    .overlay(Circle().strokeBorder(ClockPalette.secondaryText(colorScheme).opacity(0.35)))
                Text(selectedColorDescription)
                    .wordClockFont(size: 16, weight: .semibold)
                    .frame(maxWidth: .infinity, alignment: .leading)
            }
            .padding(.horizontal, 20)

            colorModeTabs
                .padding(.horizontal, 20)

            ScrollView {
                Group {
                    if selectedTab == 0 {
                        VStack(spacing: 15) {
                            GeometryReader { proxy in
                                let size = min(min(proxy.size.width, proxy.size.height), 320)
                                LedColorWheel(color: $colorDraft)
                                    .frame(width: size, height: size)
                                    .frame(maxWidth: .infinity, maxHeight: .infinity)
                                    .disabled(!canControl)
                            }
                            .frame(height: 300)
                            Text(L10n.colorWheelDescription)
                                .wordClockFont(size: 13)
                                .foregroundStyle(ClockPalette.secondaryText(colorScheme))
                                .multilineTextAlignment(.center)
                        }
                    } else {
                        VStack(alignment: .leading, spacing: 12) {
                            Text(L10n.whiteMixDescription)
                                .wordClockFont(size: 13)
                                .foregroundStyle(ClockPalette.secondaryText(colorScheme))
                            HStack {
                                Text(L10n.whiteTemperatureTitle)
                                    .wordClockFont(size: 15, weight: .semibold)
                                Spacer()
                                Text(L10n.whiteTemperatureValue(whiteTemperatureKelvin))
                                    .wordClockFont(size: 13, weight: .medium, design: .rounded)
                                    .foregroundStyle(ClockPalette.secondaryText(colorScheme))
                            }
                            Slider(
                                value: Binding(
                                    get: { Double(whiteTemperatureKelvin) },
                                    set: { value in
                                        whiteTemperatureKelvin = Int(value.rounded())
                                        colorDraft = WordClockColorPresets.shared
                                            .whiteColorAtTemperature(kelvin: Int32(whiteTemperatureKelvin))
                                            .swiftUIColor
                                    }
                                ),
                                in: 2700...8000,
                                step: 10
                            )
                            .tint(ClockPalette.primary(colorScheme))
                            .disabled(!canControl)
                            Text(L10n.whitePresetsTitle)
                                .wordClockFont(size: 15, weight: .semibold)
                            LazyVGrid(columns: whitePresetGridColumns, spacing: 10) {
                                ForEach(ColorPreset.whites, id: \.name) { preset in
                                    whitePresetButton(preset)
                                }
                            }
                        }
                    }
                }
                .frame(maxWidth: .infinity, alignment: .topLeading)
                .frame(minHeight: 340)
                .padding(.horizontal, 20)
                .padding(.bottom, 24)
            }
        }
        .padding(.top, 20)
        .background(ClockPalette.sheetSurface(colorScheme).ignoresSafeArea())
    }

    private func whitePresetButton(_ preset: ColorPreset) -> some View {
        let isSelected = UIColor(colorDraft).isApproximatelyEqual(to: UIColor(preset.color))
        let shape = RoundedRectangle(cornerRadius: 16, style: .continuous)
        return Button {
            if preset.temperatureKelvin > 0 {
                whiteTemperatureKelvin = Int(preset.temperatureKelvin)
            }
            colorDraft = preset.color
        } label: {
            HStack(spacing: 8) {
                Circle()
                    .fill(preset.color)
                    .frame(width: 36, height: 36)
                    .overlay(Circle().stroke(ClockPalette.secondaryText(colorScheme).opacity(0.3), lineWidth: 1))
                VStack(alignment: .leading, spacing: 2) {
                    Text(preset.name)
                        .wordClockFont(size: 12, weight: .medium)
                        .lineLimit(1)
                        .minimumScaleFactor(0.78)
                    if preset.temperatureKelvin > 0 {
                        Text(L10n.whiteTemperatureValue(Int(preset.temperatureKelvin)))
                            .wordClockFont(size: 10)
                            .foregroundStyle(ClockPalette.secondaryText(colorScheme))
                            .lineLimit(1)
                            .minimumScaleFactor(0.75)
                    }
                }
                .frame(maxWidth: .infinity, alignment: .leading)
                if isSelected {
                    Image(systemName: "checkmark.circle.fill")
                        .font(.system(size: 15, weight: .semibold))
                        .foregroundStyle(ClockPalette.primary(colorScheme))
                }
            }
            .padding(.horizontal, 9)
            .frame(maxWidth: .infinity, minHeight: 72)
            .foregroundStyle(ClockPalette.onSurface(colorScheme))
            .background(
                isSelected
                    ? ClockPalette.primary(colorScheme).opacity(colorScheme == .dark ? 0.2 : 0.1)
                    : ClockPalette.surfaceVariant(colorScheme).opacity(0.62),
                in: shape
            )
            .overlay {
                shape.strokeBorder(
                    isSelected
                        ? ClockPalette.primary(colorScheme).opacity(0.8)
                        : ClockPalette.secondaryText(colorScheme).opacity(0.2),
                    lineWidth: isSelected ? 1.5 : 1
                )
            }
            .contentShape(shape)
        }
        .buttonStyle(.plain)
        .disabled(!canControl)
    }

    private var colorModeTabs: some View {
        HStack(spacing: 0) {
            colorModeTab(title: L10n.colorsTab, index: 0)
            colorModeTab(title: L10n.whitesTab, index: 1)
        }
        .padding(.horizontal, 10)
        .background(
            ClockPalette.sheetSurface(colorScheme),
            in: RoundedRectangle(cornerRadius: 14, style: .continuous)
        )
    }

    private func colorModeTab(title: String, index: Int) -> some View {
        Button {
            withAnimation(.easeInOut(duration: 0.18)) {
                if index == 1,
                   let preset = ColorPreset.whites.first(where: {
                       UIColor(colorDraft).isApproximatelyEqual(to: UIColor($0.color))
                   }) {
                    whiteTemperatureKelvin = Int(preset.temperatureKelvin)
                }
                selectedTab = index
            }
        } label: {
            VStack(spacing: 8) {
                Text(title)
                    .wordClockFont(size: 14, weight: .medium)
                    .frame(maxWidth: .infinity, minHeight: 40)
                Rectangle()
                    .fill(selectedTab == index ? ClockPalette.primary(colorScheme) : .clear)
                    .frame(height: 2)
            }
            .contentShape(Rectangle())
        }
        .buttonStyle(.plain)
        .foregroundStyle(
            selectedTab == index
                ? ClockPalette.primary(colorScheme)
                : ClockPalette.onSurface(colorScheme)
        )
    }

    private var selectedColorDescription: String {
        L10n.selectedColor(selectedColorHex)
    }

    private var selectedColorHex: String {
        let components = UIColor(colorDraft).rgbComponents
        return String(format: "#%02X%02X%02X", components.red, components.green, components.blue)
    }

    private func detailsCard(_ value: ControlSnapshot) -> some View {
        VStack(alignment: .leading, spacing: 12) {
            HStack(spacing: 8) {
                Image(systemName: "info.circle.fill")
                    .font(.system(size: 18, weight: .semibold))
                Text(L10n.deviceInfoTitle)
                    .wordClockFont(size: 18, weight: .semibold)
            }
            .foregroundStyle(ClockPalette.secondary(colorScheme))
            detailRow(L10n.firmware, value.device.firmwareVersion ?? L10n.unknown)
            detailRow(L10n.physicalLedCount, value.device.physicalLedCount ?? L10n.unknown)
            detailRow(L10n.activeSegmentLedCount, value.state.physicalLedCount ?? L10n.unknown)
            detailRow(L10n.segmentCount, value.state.segmentCount)
        }
        .padding(20)
        .clockCard(colorScheme)
    }

    private var unavailableCard: some View {
        VStack(spacing: 12) {
            Image(systemName: "wifi.slash").font(.system(size: 30)).foregroundStyle(ClockPalette.error)
            Text(L10n.deviceUnavailable)
            Text(wifi.isAvailable ? L10n.unavailableCheckIp : L10n.wifiReconnectShort)
                .wordClockFont(size: 14)
                .multilineTextAlignment(.center)
                .foregroundStyle(ClockPalette.secondaryText(colorScheme))
            Button(L10n.retry) { model.loadControl(for: deviceID) }
                .wordClockFont(size: 15, weight: .semibold)
                .disabled(!wifi.isAvailable)
        }
        .frame(maxWidth: .infinity)
        .padding(24)
        .clockCard(colorScheme)
    }

    private func detailRow(_ title: String, _ value: String) -> some View {
        HStack {
            Text(title)
                .wordClockFont(size: 14)
                .foregroundStyle(ClockPalette.secondaryText(colorScheme))
            Spacer()
            Text(value)
                .wordClockFont(size: 14, weight: .medium)
        }
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
        .accessibilityLabel(L10n.colorWheelContentDescription)
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
    let temperatureKelvin: Int32

    static var colors: [ColorPreset] = WordClockColorPresets.shared.quickColors.map {
        ColorPreset(name: displayName(for: $0.id), color: $0.color.swiftUIColor, temperatureKelvin: $0.temperatureKelvin)
    }

    static var whites: [ColorPreset] = WordClockColorPresets.shared.whiteColors.map {
        ColorPreset(name: displayName(for: $0.id), color: $0.color.swiftUIColor, temperatureKelvin: $0.temperatureKelvin)
    }

    private static func displayName(for id: String) -> String {
        L10n.colorPreset(id)
    }
}
