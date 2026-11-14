import SwiftUI

struct DevicesView: View {
    @ObservedObject var model: AppModel
    @ObservedObject var wifi: WifiMonitor
    @Environment(\.colorScheme) private var colorScheme
    @Environment(\.openURL) private var openURL
    @State private var path: [String] = []
    @State private var isDrawerPresented = false
    @State private var isLanguageOptionsExpanded = false
    @State private var isContactPresented = false
    @State private var ipAddress = ""
    @State private var deviceName = ""
    @State private var renameTarget: ClockDevice?
    @State private var renameValue = ""
    @State private var removeTarget: ClockDevice?

    var body: some View {
        NavigationStack(path: $path) {
            ScrollView {
                VStack(alignment: .leading, spacing: 18) {
                    topBar
                    heading
                    addDeviceCard
                    deviceListHeading
                    if !wifi.isAvailable {
                        notice(L10n.devicesWifiUnavailable, symbol: "wifi.slash", warning: true)
                    }
                    if let success = model.successMessage {
                        notice(success, symbol: "checkmark.circle.fill", warning: false)
                    }
                    if model.isLoadingDevices && model.devices.isEmpty {
                        ProgressView(L10n.loadingDevices)
                            .frame(maxWidth: .infinity)
                            .padding(28)
                    } else if model.devices.isEmpty {
                        emptyState
                    } else {
                        LazyVGrid(columns: [GridItem(.adaptive(minimum: 305), spacing: 14)], spacing: 14) {
                            ForEach(model.devices, id: \.id) { device in
                                DeviceCardView(
                                    device: device,
                                    wifiAvailable: wifi.isAvailable,
                                    isWorking: model.workingDeviceIDs.contains(device.id),
                                    onSelect: { path.append(device.id) },
                                    onPower: { model.togglePower(for: device) },
                                    onRename: {
                                        renameValue = device.name
                                        renameTarget = device
                                    },
                                    onRemove: { removeTarget = device }
                                )
                            }
                        }
                    }
                }
                .padding(.horizontal, 18)
                .padding(.top, 10)
                .padding(.bottom, 28)
                .frame(maxWidth: 960)
                .frame(maxWidth: .infinity)
            }
            .background(ClockPalette.background(colorScheme).ignoresSafeArea())
            .overlay {
                if isDrawerPresented {
                    GeometryReader { proxy in
                        ZStack(alignment: .leading) {
                            Color.black.opacity(0.38)
                                .ignoresSafeArea()
                                .onTapGesture(perform: closeDrawer)

                            settingsDrawer(
                                topInset: max(proxy.safeAreaInsets.top, 20),
                                bottomInset: max(proxy.safeAreaInsets.bottom, 16)
                            )
                            .frame(width: proxy.size.width * 0.8)
                            .frame(maxHeight: .infinity)
                            .background(ClockPalette.surface(colorScheme))
                            .clipShape(RoundedRectangle(cornerRadius: 26, style: .continuous))
                            .shadow(color: .black.opacity(0.2), radius: 24, x: 8, y: 0)
                            .transition(.move(edge: .leading))
                        }
                        .ignoresSafeArea()
                    }
                    .transition(.opacity)
                    .zIndex(2)
                }
            }
            .animation(.spring(response: 0.34, dampingFraction: 0.88), value: isDrawerPresented)
            .navigationDestination(for: String.self) { deviceID in
                ControlView(deviceID: deviceID, model: model, wifi: wifi)
            }
            .toolbar(.hidden, for: .navigationBar)
            .onAppear { model.loadDevices() }
            .onChange(of: wifi.isAvailable) { available in
                if available { model.loadDevices() }
            }
            .alert(L10n.errorTitle, isPresented: errorPresented) {
                Button(L10n.ok, role: .cancel) { model.clearNotices() }
            } message: {
                Text(model.errorMessage ?? L10n.errorFallback)
            }
            .sheet(isPresented: activeSheetPresented) {
                if isContactPresented {
                    contactUsSheet
                        .presentationDetents([.medium, .large])
                        .presentationDragIndicator(.visible)
                } else if let device = renameTarget {
                    renameSheet(for: device)
                        .presentationDetents([.height(260)])
                        .presentationDragIndicator(.visible)
                }
            }
            .confirmationDialog(
                L10n.removeDialogTitle,
                isPresented: removePresented,
                titleVisibility: .visible,
                presenting: removeTarget
            ) { device in
                Button(L10n.remove, role: .destructive) { model.removeDevice(device) }
                Button(L10n.cancel, role: .cancel) {}
            } message: { device in
                Text(L10n.removeDialogMessage(device.name))
            }
        }
    }

    private var topBar: some View {
        ZStack {
            HStack(spacing: 8) {
                Text(L10n.devicesTitle)
                    .wordClockFont(size: 20, weight: .semibold)
                    .lineLimit(1)
                Spacer(minLength: 80)
                Button {
                    withAnimation(.spring(response: 0.34, dampingFraction: 0.88)) {
                        isDrawerPresented = true
                    }
                } label: {
                    Image(systemName: "line.3.horizontal")
                        .font(.system(size: 18, weight: .semibold))
                        .foregroundStyle(ClockPalette.primary(colorScheme))
                        .frame(width: 42, height: 42)
                        .background(ClockPalette.surfaceVariant(colorScheme).opacity(0.72), in: Circle())
                        .contentShape(Rectangle())
                }
                .accessibilityLabel(L10n.openSettings)
            }
            Image("word_clock_logo")
                .resizable()
                .scaledToFit()
                .frame(width: 76, height: 46)
                .allowsHitTesting(false)
        }
        .frame(height: 54)
    }

    private func settingsDrawer(topInset: CGFloat, bottomInset: CGFloat) -> some View {
        VStack(alignment: .leading, spacing: 22) {
            HStack(spacing: 13) {
                Image("word_clock_logo")
                    .resizable()
                    .scaledToFit()
                    .padding(8)
                    .frame(width: 48, height: 48)
                    .background(ClockPalette.primary(colorScheme).opacity(0.12), in: RoundedRectangle(cornerRadius: 16))
                VStack(alignment: .leading, spacing: 4) {
                    Text(L10n.settingsTitle)
                        .wordClockFont(size: 21, weight: .semibold)
                    Text(L10n.settingsDescription)
                        .wordClockFont(size: 13)
                        .foregroundStyle(ClockPalette.secondaryText(colorScheme))
                        .fixedSize(horizontal: false, vertical: true)
                }
            }

            Rectangle()
                .fill(ClockPalette.secondaryText(colorScheme).opacity(0.16))
                .frame(height: 1)

            Button {
                model.setThemeMode(colorScheme == .dark ? "light" : "dark")
                closeDrawer()
            } label: {
                settingsDrawerRow(
                    symbol: colorScheme == .dark ? "sun.max.fill" : "moon.fill",
                    title: L10n.appearance,
                    detail: colorScheme == .dark ? L10n.darkTheme : L10n.lightTheme,
                    enabled: true
                )
            }
            .buttonStyle(.plain)

            Button {
                withAnimation(.spring(response: 0.3, dampingFraction: 0.86)) {
                    isLanguageOptionsExpanded.toggle()
                }
            } label: {
                settingsDrawerRow(
                    symbol: "globe",
                    title: L10n.language,
                    detail: model.languageCode == "fa" ? L10n.languagePersian : L10n.languageEnglish,
                    enabled: true
                )
            }
            .buttonStyle(.plain)
            if isLanguageOptionsExpanded {
                HStack(spacing: 8) {
                    languageOption(title: L10n.languageEnglish, code: "en")
                    languageOption(title: L10n.languagePersian, code: "fa")
                }
                .padding(.leading, 55)
                .transition(.opacity.combined(with: .move(edge: .top)))
            }

            Button {
                withAnimation(.spring(response: 0.34, dampingFraction: 0.88)) {
                    isDrawerPresented = false
                    isContactPresented = true
                }
            } label: {
                settingsDrawerRow(
                    symbol: "bubble.left.and.bubble.right.fill",
                    title: L10n.contactUs,
                    enabled: true
                )
            }
            .buttonStyle(.plain)

            Spacer(minLength: 0)

            Rectangle()
                .fill(ClockPalette.secondaryText(colorScheme).opacity(0.16))
                .frame(height: 1)

            HStack(spacing: 8) {
                Image(systemName: "info.circle")
                    .font(.system(size: 13, weight: .medium))
                    .foregroundStyle(ClockPalette.primary(colorScheme))
                Text(L10n.appVersion(appVersion))
                    .wordClockFont(size: 12, weight: .medium)
                    .foregroundStyle(ClockPalette.secondaryText(colorScheme))
                Spacer(minLength: 0)
            }
            .padding(.horizontal, 12)
            .padding(.vertical, 10)
            .background(ClockPalette.surfaceVariant(colorScheme).opacity(0.6), in: RoundedRectangle(cornerRadius: 14))
        }
        .padding(.horizontal, 18)
        .padding(.top, topInset + 28)
        .padding(.bottom, bottomInset + 16)
        .frame(maxHeight: .infinity, alignment: .topLeading)
    }

    private var contactUsSheet: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 20) {
                HStack(alignment: .top, spacing: 14) {
                    Image("word_clock_logo")
                        .resizable()
                        .scaledToFit()
                        .padding(8)
                        .frame(width: 58, height: 58)
                        .background(ClockPalette.primary(colorScheme).opacity(0.12), in: RoundedRectangle(cornerRadius: 18))

                    VStack(alignment: .leading, spacing: 5) {
                        Text(L10n.contactBrand)
                            .wordClockFont(size: 13, weight: .semibold)
                            .foregroundStyle(ClockPalette.primary(colorScheme))
                        Text(L10n.contactTitle)
                            .wordClockFont(size: 20, weight: .bold, design: .rounded)
                            .foregroundStyle(ClockPalette.onSurface(colorScheme))
                            .fixedSize(horizontal: false, vertical: true)
                    }

                    Spacer(minLength: 0)

                }

                Text(L10n.contactDescription)
                    .wordClockFont(size: 15)
                    .foregroundStyle(ClockPalette.secondaryText(colorScheme))
                    .fixedSize(horizontal: false, vertical: true)

                VStack(spacing: 12) {
                    contactActionRow(
                        symbol: "phone.fill",
                        title: L10n.contactPhoneLabel,
                        detail: L10n.contactPhoneNumber,
                        hint: L10n.contactCallHint
                    ) {
                        let phoneNumber = L10n.contactPhoneNumber
                            .replacingOccurrences(of: " ", with: "")
                        openExternalLink("tel:\(phoneNumber)")
                    }

                    contactActionRow(
                        symbol: "camera.fill",
                        title: L10n.contactInstagramLabel,
                        detail: L10n.contactInstagramHandle,
                        hint: L10n.contactInstagramHint
                    ) {
                        let handle = L10n.contactInstagramHandle
                            .trimmingCharacters(in: CharacterSet(charactersIn: "@"))
                        openExternalLink("https://www.instagram.com/\(handle)/")
                    }
                }
            }
            .padding(.horizontal, 24)
            .padding(.top, 20)
            .padding(.bottom, 32)
            .frame(maxWidth: 560)
            .frame(maxWidth: .infinity)
        }
        .background(ClockPalette.surface(colorScheme).ignoresSafeArea())
    }

    private func contactActionRow(
        symbol: String,
        title: String,
        detail: String,
        hint: String,
        action: @escaping () -> Void
    ) -> some View {
        Button(action: action) {
            HStack(spacing: 12) {
                Image(systemName: symbol)
                    .font(.system(size: 17, weight: .semibold))
                    .foregroundStyle(ClockPalette.primary(colorScheme))
                    .frame(width: 42, height: 42)
                    .background(ClockPalette.primary(colorScheme).opacity(0.11), in: RoundedRectangle(cornerRadius: 14))

                VStack(alignment: .leading, spacing: 4) {
                    Text(title)
                        .wordClockFont(size: 12, weight: .medium)
                        .foregroundStyle(ClockPalette.secondaryText(colorScheme))
                    Text(detail)
                        .wordClockFont(size: 14, weight: .semibold)
                        .foregroundStyle(ClockPalette.onSurface(colorScheme))
                        .lineLimit(1)
                        .minimumScaleFactor(0.72)
                }
                Spacer(minLength: 0)
                Image(systemName: "arrow.up.right")
                    .font(.system(size: 12, weight: .semibold))
                    .foregroundStyle(ClockPalette.primary(colorScheme))
            }
            .padding(11)
            .background(ClockPalette.surfaceVariant(colorScheme).opacity(0.72), in: RoundedRectangle(cornerRadius: 18, style: .continuous))
            .contentShape(RoundedRectangle(cornerRadius: 18, style: .continuous))
        }
        .buttonStyle(.plain)
        .accessibilityHint(hint)
        .environment(\.layoutDirection, .leftToRight)
    }

    private var appVersion: String {
        Bundle.main.infoDictionary?["CFBundleShortVersionString"] as? String ?? L10n.unknown
    }

    private func openExternalLink(_ value: String) {
        guard let url = URL(string: value) else { return }
        openURL(url)
    }

    private func settingsDrawerRow(
        symbol: String,
        title: String,
        detail: String? = nil,
        enabled: Bool
    ) -> some View {
        HStack(spacing: 13) {
            Image(systemName: symbol)
                .font(.system(size: 17, weight: .semibold))
                .foregroundStyle(ClockPalette.primary(colorScheme))
                .frame(width: 42, height: 42)
                .background(ClockPalette.primary(colorScheme).opacity(0.11), in: RoundedRectangle(cornerRadius: 14))
            Text(title)
                .wordClockFont(size: 16, weight: .medium)
                .foregroundStyle(ClockPalette.onSurface(colorScheme))
                .lineLimit(1)
            Spacer(minLength: 6)
            if let detail {
                Text(detail)
                    .wordClockFont(size: 11, weight: .medium)
                    .foregroundStyle(ClockPalette.secondaryText(colorScheme))
                    .lineLimit(1)
                    .padding(.horizontal, 9)
                    .padding(.vertical, 6)
                    .background(ClockPalette.surfaceVariant(colorScheme), in: Capsule())
            }
        }
        .padding(.horizontal, 12)
        .padding(.vertical, 9)
        .background(ClockPalette.surfaceVariant(colorScheme).opacity(enabled ? 0.72 : 0.42), in: RoundedRectangle(cornerRadius: 18, style: .continuous))
        .opacity(enabled ? 1 : 0.58)
        .contentShape(RoundedRectangle(cornerRadius: 18, style: .continuous))
    }

    private func languageOption(title: String, code: String) -> some View {
        let isSelected = model.languageCode == code
        return Button {
            model.setLanguageCode(code)
            closeDrawer()
        } label: {
            HStack(spacing: 5) {
                ZStack {
                    Circle()
                        .strokeBorder(
                            isSelected
                                ? ClockPalette.primary(colorScheme)
                                : ClockPalette.secondaryText(colorScheme).opacity(0.78),
                            lineWidth: 1.5
                        )
                    if isSelected {
                        Circle()
                            .fill(ClockPalette.primary(colorScheme))
                            .padding(4)
                    }
                }
                .frame(width: 16, height: 16)
                Text(title)
                    .wordClockFont(size: 12, weight: .semibold)
                    .lineLimit(1)
                    .minimumScaleFactor(0.8)
            }
            .foregroundStyle(
                isSelected
                    ? ClockPalette.primary(colorScheme)
                    : ClockPalette.onSurface(colorScheme)
            )
            .frame(maxWidth: .infinity, minHeight: 44)
            .background(
                isSelected
                    ? ClockPalette.primary(colorScheme).opacity(0.16)
                    : ClockPalette.surfaceVariant(colorScheme),
                in: RoundedRectangle(cornerRadius: 14, style: .continuous)
            )
            .overlay {
                RoundedRectangle(cornerRadius: 14, style: .continuous)
                    .strokeBorder(
                        isSelected
                            ? ClockPalette.primary(colorScheme).opacity(0.5)
                            : ClockPalette.secondaryText(colorScheme).opacity(0.5),
                        lineWidth: 1
                    )
            }
        }
        .buttonStyle(.plain)
    }

    private func closeDrawer() {
        withAnimation(.spring(response: 0.34, dampingFraction: 0.88)) {
            isDrawerPresented = false
        }
    }

    private var heading: some View {
        VStack(alignment: .leading, spacing: 6) {
            Text(L10n.screenTagline)
                .wordClockFont(size: 28, weight: .bold, design: .rounded)
                .foregroundStyle(
                    LinearGradient(
                        colors: [ClockPalette.primary(colorScheme), ClockPalette.secondary(colorScheme), ClockPalette.tertiary(colorScheme)],
                        startPoint: .leading,
                        endPoint: .trailing
                    )
                )
                .fixedSize(horizontal: false, vertical: true)
                .frame(maxWidth: .infinity, alignment: .leading)
                .environment(\.layoutDirection, .leftToRight)
            Text(L10n.screenDescription)
                .wordClockFont(size: 16)
                .foregroundStyle(ClockPalette.secondaryText(colorScheme))
        }
        .padding(.top, 6)
        .padding(.bottom, 2)
    }

    private var addDeviceCard: some View {
        VStack(alignment: .leading, spacing: 15) {
            HStack(spacing: 12) {
                Image(systemName: "clock.fill")
                    .font(.system(size: 20, weight: .semibold))
                    .foregroundStyle(ClockPalette.tertiary(colorScheme))
                    .frame(width: 46, height: 46)
                    .background(ClockPalette.tertiary(colorScheme).opacity(0.14), in: Circle())
                VStack(alignment: .leading, spacing: 3) {
                    Text(L10n.addDeviceTitle).wordClockFont(size: 20, weight: .semibold)
                    Text(L10n.addDeviceDescription)
                        .wordClockFont(size: 14)
                        .foregroundStyle(ClockPalette.secondaryText(colorScheme))
                }
            }
            ViewThatFits(in: .horizontal) {
                HStack(spacing: 12) { ipField; nameField }
                VStack(spacing: 12) { ipField; nameField }
            }
            Button {
                model.addDevice(ipAddress: ipAddress, name: deviceName)
            } label: {
                HStack(spacing: 9) {
                    if model.isAddingDevice { ProgressView().tint(.white) }
                    else { Image(systemName: "plus.circle.fill") }
                    Text(model.isAddingDevice ? L10n.connectingDevice : L10n.addDeviceButton)
                        .wordClockFont(size: 16, weight: .semibold)
                }
                .font(.system(size: 16, weight: .semibold))
                .frame(maxWidth: .infinity, minHeight: 52)
                .foregroundStyle(.white)
                .background(ClockPalette.primary(colorScheme), in: RoundedRectangle(cornerRadius: 16, style: .continuous))
            }
            .disabled(model.isAddingDevice || ipAddress.trimmingCharacters(in: .whitespaces).isEmpty || !wifi.isAvailable)
            .opacity((model.isAddingDevice || !wifi.isAvailable) ? 0.65 : 1)
        }
        .padding(20)
        .clockCard(colorScheme, tint: ClockPalette.tertiary(colorScheme).opacity(colorScheme == .dark ? 0.14 : 0.09))
    }

    private var ipField: some View {
        TextField(L10n.ipAddressPlaceholder, text: $ipAddress)
            .keyboardType(.decimalPad)
            .textInputAutocapitalization(.never)
            .autocorrectionDisabled()
            .textFieldStyle(.plain)
            .padding(.horizontal, 14)
            .frame(minHeight: 52)
            .background(ClockPalette.surface(colorScheme), in: RoundedRectangle(cornerRadius: 14, style: .continuous))
            .overlay(RoundedRectangle(cornerRadius: 14).stroke(ClockPalette.secondaryText(colorScheme).opacity(0.2)))
            .environment(\.layoutDirection, .leftToRight)
    }

    private var nameField: some View {
        TextField(L10n.nameOptional, text: $deviceName)
            .textInputAutocapitalization(.words)
            .textFieldStyle(.plain)
            .padding(.horizontal, 14)
            .frame(minHeight: 52)
            .background(ClockPalette.surface(colorScheme), in: RoundedRectangle(cornerRadius: 14, style: .continuous))
            .overlay(RoundedRectangle(cornerRadius: 14).stroke(ClockPalette.secondaryText(colorScheme).opacity(0.2)))
    }

    private var deviceListHeading: some View {
        HStack {
            VStack(alignment: .leading, spacing: 3) {
                Text(L10n.deviceListTitle).wordClockFont(size: 21, weight: .semibold)
                Text(L10n.connectedDeviceCount(model.devices.count))
                    .wordClockFont(size: 13)
                    .foregroundStyle(ClockPalette.secondaryText(colorScheme))
            }
            Spacer()
            Button { model.loadDevices() } label: {
                Image(systemName: "arrow.clockwise")
                    .font(.system(size: 17, weight: .semibold))
                    .foregroundStyle(ClockPalette.primary(colorScheme))
                    .frame(width: 44, height: 44)
                    .background(ClockPalette.surfaceVariant(colorScheme).opacity(0.75), in: Circle())
            }
            .disabled(model.isLoadingDevices)
            .accessibilityLabel(L10n.refreshDevicesDescription)
        }
        .padding(.top, 4)
    }

    private var emptyState: some View {
        VStack(spacing: 10) {
            Image(systemName: "lightbulb.2.fill")
                .font(.system(size: 34))
                .foregroundStyle(ClockPalette.tertiary(colorScheme))
                .padding(12)
                .background(ClockPalette.tertiary(colorScheme).opacity(0.12), in: Circle())
            Text(L10n.emptyDevicesTitle)
                .wordClockFont(size: 18, weight: .semibold)
            Text(L10n.emptyDevicesDescription)
                .wordClockFont(size: 14)
                .multilineTextAlignment(.center)
                .foregroundStyle(ClockPalette.secondaryText(colorScheme))
        }
        .frame(maxWidth: .infinity)
        .padding(24)
        .clockCard(colorScheme)
    }

    private func notice(_ message: String, symbol: String, warning: Bool) -> some View {
        HStack(spacing: 10) {
            Image(systemName: symbol)
            Text(message).wordClockFont(size: 14, weight: .medium)
            Spacer(minLength: 0)
        }
        .foregroundStyle(warning ? ClockPalette.error : ClockPalette.powerOn)
        .padding(14)
        .background((warning ? ClockPalette.error : ClockPalette.powerOn).opacity(0.1), in: RoundedRectangle(cornerRadius: 14))
    }

    private func renameSheet(for device: ClockDevice) -> some View {
        NavigationStack {
            Form {
                TextField(L10n.deviceNameLabel, text: $renameValue)
            }
            .navigationTitle(L10n.renameDialogTitle)
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) { Button(L10n.cancel) { renameTarget = nil } }
                ToolbarItem(placement: .confirmationAction) {
                    Button(L10n.save) {
                        model.renameDevice(device, to: renameValue)
                        renameTarget = nil
                    }
                    .disabled(renameValue.trimmingCharacters(in: .whitespaces).isEmpty)
                }
            }
        }
    }

    private var errorPresented: Binding<Bool> {
        Binding(get: { model.errorMessage != nil }, set: { if !$0 { model.clearNotices() } })
    }

    private var activeSheetPresented: Binding<Bool> {
        Binding(
            get: { isContactPresented || renameTarget != nil },
            set: { isPresented in
                if !isPresented {
                    isContactPresented = false
                    renameTarget = nil
                }
            }
        )
    }

    private var removePresented: Binding<Bool> {
        Binding(get: { removeTarget != nil }, set: { if !$0 { removeTarget = nil } })
    }
}

private struct DeviceCardView: View {
    let device: ClockDevice
    let wifiAvailable: Bool
    let isWorking: Bool
    let onSelect: () -> Void
    let onPower: () -> Void
    let onRename: () -> Void
    let onRemove: () -> Void
    @Environment(\.colorScheme) private var colorScheme

    private var online: Bool { wifiAvailable && device.isOnline }

    var body: some View {
        HStack(spacing: 14) {
            Button(action: onSelect) {
                VStack(alignment: .leading, spacing: 12) {
                    HStack(spacing: 9) {
                        Image(systemName: "clock")
                            .font(.system(size: 17, weight: .semibold))
                            .foregroundStyle(ClockPalette.secondary(colorScheme))
                            .frame(width: 40, height: 40)
                            .background(ClockPalette.secondary(colorScheme).opacity(0.12), in: Circle())
                        VStack(alignment: .leading, spacing: 3) {
                            Text(device.name)
                                .wordClockFont(size: 17, weight: .semibold)
                                .lineLimit(1)
                            Text(device.ipAddress)
                                .font(.system(size: 13, design: .monospaced))
                                .foregroundStyle(ClockPalette.secondaryText(colorScheme))
                                .lineLimit(1)
                        }
                        Spacer(minLength: 0)
                    }
                    HStack(spacing: 6) {
                        Circle().fill(online ? ClockPalette.powerOn : ClockPalette.error).frame(width: 7, height: 7)
                        Text(online ? L10n.deviceOnline : L10n.deviceOffline)
                            .wordClockFont(size: 12, weight: .semibold)
                            .foregroundStyle(online ? ClockPalette.powerOn : ClockPalette.error)
                        if let firmware = device.firmwareVersion {
                            Text(L10n.deviceFirmware(firmware))
                                .wordClockFont(size: 12)
                                .foregroundStyle(ClockPalette.secondaryText(colorScheme))
                                .lineLimit(1)
                        }
                    }
                }
                .contentShape(Rectangle())
            }
            .buttonStyle(.plain)
            .disabled(!online)

            VStack(spacing: 10) {
                Menu {
                    Button(action: onRename) {
                        Label(L10n.renameDevice, systemImage: "pencil")
                            .wordClockFont(size: 15, weight: .medium)
                    }
                    Button(role: .destructive, action: onRemove) {
                        Label(L10n.removeDevice, systemImage: "trash")
                            .wordClockFont(size: 15, weight: .medium)
                    }
                } label: {
                    Image(systemName: "ellipsis")
                        .font(.system(size: 16, weight: .bold))
                        .foregroundStyle(ClockPalette.secondaryText(colorScheme))
                        .frame(width: 34, height: 30)
                        .contentShape(Rectangle())
                }
                Button(action: onPower) {
                    if isWorking {
                        ProgressView().frame(width: 42, height: 42)
                    } else {
                        Image(systemName: "power")
                            .font(.system(size: 18, weight: .semibold))
                            .foregroundStyle(device.isOn ? ClockPalette.powerOn : ClockPalette.powerOff)
                            .frame(width: 42, height: 42)
                            .background((device.isOn ? ClockPalette.powerOn : ClockPalette.powerOff).opacity(0.12), in: Circle())
                    }
                }
                .disabled(!online || isWorking)
                .accessibilityLabel(L10n.devicePowerLabel(isOn: device.isOn, deviceName: device.name))
            }
        }
        .padding(16)
        .clockCard(colorScheme, tint: ClockPalette.primary(colorScheme).opacity(colorScheme == .dark ? 0.13 : 0.06))
        .opacity(online ? 1 : 0.78)
    }
}
