import SwiftUI

struct DevicesView: View {
    @ObservedObject var model: AppModel
    @ObservedObject var wifi: WifiMonitor
    @Environment(\.colorScheme) private var colorScheme
    @State private var path: [String] = []
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
                        notice("Wi-Fi is unavailable. Device controls are paused.", symbol: "wifi.slash", warning: true)
                    }
                    if let success = model.successMessage {
                        notice(success, symbol: "checkmark.circle.fill", warning: false)
                    }
                    if model.isLoadingDevices && model.devices.isEmpty {
                        ProgressView("Loading your clocks…")
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
            .navigationDestination(for: String.self) { deviceID in
                ControlView(deviceID: deviceID, model: model, wifi: wifi)
            }
            .toolbar(.hidden, for: .navigationBar)
            .onAppear { model.loadDevices() }
            .onChange(of: wifi.isAvailable) { available in
                if available { model.loadDevices() }
            }
            .alert("Something went wrong", isPresented: errorPresented) {
                Button("OK", role: .cancel) { model.clearNotices() }
            } message: {
                Text(model.errorMessage ?? "Please try again.")
            }
            .sheet(isPresented: renameSheetPresented) {
                if let device = renameTarget {
                    renameSheet(for: device)
                        .presentationDetents([.height(260)])
                        .presentationDragIndicator(.visible)
                }
            }
            .confirmationDialog(
                "Remove this clock?",
                isPresented: removePresented,
                titleVisibility: .visible,
                presenting: removeTarget
            ) { device in
                Button("Remove", role: .destructive) { model.removeDevice(device) }
                Button("Cancel", role: .cancel) {}
            } message: { device in
                Text("\(device.name) will be removed from this app.")
            }
        }
    }

    private var topBar: some View {
        ZStack {
            HStack(spacing: 8) {
                Text("Devices")
                    .font(.system(size: 20, weight: .semibold))
                    .lineLimit(1)
                Spacer(minLength: 80)
                Button {
                    model.setThemeMode(model.themeMode == "dark" ? "light" : "dark")
                } label: {
                    Image(systemName: colorScheme == .dark ? "sun.max.fill" : "moon.fill")
                        .font(.system(size: 18, weight: .semibold))
                        .foregroundStyle(ClockPalette.primary(colorScheme))
                        .frame(width: 42, height: 42)
                        .contentShape(Rectangle())
                }
                .accessibilityLabel(colorScheme == .dark ? "Switch to light theme" : "Switch to dark theme")
            }
            Image("word_clock_logo")
                .resizable()
                .scaledToFit()
                .frame(width: 76, height: 46)
                .allowsHitTesting(false)
        }
        .frame(height: 54)
    }

    private var heading: some View {
        VStack(alignment: .leading, spacing: 6) {
            Text("Your light, in perfect time.")
                .font(.system(size: 28, weight: .bold, design: .rounded))
                .foregroundStyle(
                    LinearGradient(
                        colors: [ClockPalette.primary(colorScheme), ClockPalette.secondary(colorScheme), ClockPalette.tertiary(colorScheme)],
                        startPoint: .leading,
                        endPoint: .trailing
                    )
                )
                .fixedSize(horizontal: false, vertical: true)
            Text("Connect your clock and shape every moment.")
                .font(.system(size: 16))
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
                    Text("Add a word clock").font(.system(size: 20, weight: .semibold))
                    Text("Enter the clock’s local IP address.")
                        .font(.system(size: 14))
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
                    Text(model.isAddingDevice ? "Connecting…" : "Add clock")
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
        TextField("IP address (e.g. 192.168.1.100)", text: $ipAddress)
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
        TextField("Name (optional)", text: $deviceName)
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
                Text("Your clocks").font(.system(size: 21, weight: .semibold))
                Text("\(model.devices.count) connected device\(model.devices.count == 1 ? "" : "s")")
                    .font(.system(size: 13))
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
            .accessibilityLabel("Refresh devices")
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
            Text("Your next favorite light starts here")
                .font(.system(size: 18, weight: .semibold))
            Text("Add a clock using its local IP address to begin controlling its LEDs.")
                .font(.system(size: 14))
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
            Text(message).font(.system(size: 14, weight: .medium))
            Spacer(minLength: 0)
        }
        .foregroundStyle(warning ? ClockPalette.error : ClockPalette.powerOn)
        .padding(14)
        .background((warning ? ClockPalette.error : ClockPalette.powerOn).opacity(0.1), in: RoundedRectangle(cornerRadius: 14))
    }

    private func renameSheet(for device: ClockDevice) -> some View {
        NavigationStack {
            Form {
                TextField("Clock name", text: $renameValue)
            }
            .navigationTitle("Rename clock")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) { Button("Cancel") { renameTarget = nil } }
                ToolbarItem(placement: .confirmationAction) {
                    Button("Save") {
                        model.renameDevice(device, to: renameValue)
                        renameTarget = nil
                    }
                    .disabled(renameValue.trimmingCharacters(in: .whitespaces).isEmpty)
                }
            }
        }
        .environment(\.layoutDirection, .leftToRight)
    }

    private var errorPresented: Binding<Bool> {
        Binding(get: { model.errorMessage != nil }, set: { if !$0 { model.clearNotices() } })
    }

    private var renameSheetPresented: Binding<Bool> {
        Binding(get: { renameTarget != nil }, set: { if !$0 { renameTarget = nil } })
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
                                .font(.system(size: 17, weight: .semibold))
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
                        Text(online ? "Online" : "Offline")
                            .font(.system(size: 12, weight: .semibold))
                            .foregroundStyle(online ? ClockPalette.powerOn : ClockPalette.error)
                        if let firmware = device.firmwareVersion {
                            Text("· Firmware \(firmware)")
                                .font(.system(size: 12))
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
                    Button(action: onRename) { Label("Rename", systemImage: "pencil") }
                    Button(role: .destructive, action: onRemove) {
                        Label("Remove device", systemImage: "trash")
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
                .accessibilityLabel(device.isOn ? "Turn off \(device.name)" : "Turn on \(device.name)")
            }
        }
        .padding(16)
        .clockCard(colorScheme, tint: ClockPalette.primary(colorScheme).opacity(colorScheme == .dark ? 0.13 : 0.06))
        .opacity(online ? 1 : 0.78)
    }
}
