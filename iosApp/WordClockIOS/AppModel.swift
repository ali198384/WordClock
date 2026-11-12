import Foundation
import SwiftUI
import UIKit
import WordClockShared

typealias ClockDevice = WordClockDeviceSnapshot
typealias ControlSnapshot = WordClockControlSnapshot

@MainActor
final class AppModel: ObservableObject {
    @Published private(set) var devices: [ClockDevice] = []
    @Published private(set) var control: ControlSnapshot?
    @Published private(set) var controlRevision = 0
    @Published private(set) var themeMode = "system"
    @Published private(set) var isLoadingDevices = false
    @Published private(set) var isAddingDevice = false
    @Published private(set) var isLoadingControl = false
    @Published private(set) var workingDeviceIDs: Set<String> = []
    @Published var errorMessage: String?
    @Published var successMessage: String?

    private let sdk = WordClockSdkFactory.shared.create()
    private var refreshAfterDeviceLoad = false
    private var pendingBrightnessByDeviceID: [String: Int] = [:]
    private var pendingColorByDeviceID: [String: Color] = [:]

    init() {
        loadThemeMode()
    }

    func loadDevices() {
        if isLoadingDevices {
            refreshAfterDeviceLoad = true
            return
        }
        isLoadingDevices = true
        errorMessage = nil
        sdk.loadDevices(onResult: { [weak self] result in
            DispatchQueue.main.async {
                guard let self else { return }
                self.isLoadingDevices = false
                do {
                    self.devices = try self.require(result.devices, from: result)
                } catch {
                    self.errorMessage = error.localizedDescription
                }
                if self.refreshAfterDeviceLoad {
                    self.refreshAfterDeviceLoad = false
                    self.loadDevices()
                }
            }
        })
    }

    func addDevice(ipAddress: String, name: String) {
        guard !isAddingDevice else { return }
        isAddingDevice = true
        errorMessage = nil
        sdk.addDevice(ipAddress: ipAddress, name: name, onResult: { [weak self] result in
            DispatchQueue.main.async {
                guard let self else { return }
                self.isAddingDevice = false
                do {
                    let device = try self.require(result.device, from: result)
                    self.devices.removeAll { $0.id == device.id }
                    self.devices.insert(device, at: 0)
                    self.successMessage = "\(device.name) is ready to control."
                } catch {
                    self.errorMessage = error.localizedDescription
                }
            }
        })
    }

    func renameDevice(_ device: ClockDevice, to name: String) {
        performDeviceAction(device.id, operation: { [weak self] completion in
            guard let self else { return }
            self.sdk.renameDevice(deviceId: device.id, name: name, onResult: completion)
        }, extract: { try self.require($0.device, from: $0) }, onSuccess: { [weak self] updated in
            guard let self else { return }
            self.replace(updated)
            self.successMessage = "Clock name updated."
        })
    }

    func removeDevice(_ device: ClockDevice) {
        performDeviceAction(device.id, operation: { [weak self] completion in
            guard let self else { return }
            self.sdk.removeDevice(deviceId: device.id, onResult: completion)
        }, extract: { try self.require($0.removedDeviceId, from: $0) }, onSuccess: { [weak self] _ in
            guard let self else { return }
            self.devices.removeAll { $0.id == device.id }
            if self.control?.device.id == device.id { self.updateControl(nil) }
            self.successMessage = "Clock removed from this app."
        })
    }

    func togglePower(for device: ClockDevice) {
        guard device.isOnline else { return }
        performDeviceAction(device.id, operation: { [weak self] completion in
            guard let self else { return }
            self.sdk.setPower(deviceId: device.id, isOn: !device.isOn, onResult: completion)
        }, extract: { try self.require($0.control, from: $0) }, onSuccess: { [weak self] snapshot in
            guard let self else { return }
            self.replace(snapshot.device, power: snapshot.state.isOn)
            if self.control?.device.id == device.id { self.updateControl(snapshot) }
        })
    }

    func loadControl(for deviceID: String) {
        isLoadingControl = true
        errorMessage = nil
        sdk.loadControl(deviceId: deviceID, onResult: { [weak self] result in
            DispatchQueue.main.async {
                guard let self else { return }
                self.isLoadingControl = false
                do {
                    let snapshot = try self.require(result.control, from: result)
                    self.updateControl(snapshot)
                    self.replace(snapshot.device, power: snapshot.state.isOn)
                } catch {
                    self.errorMessage = error.localizedDescription
                }
            }
        })
    }

    func setControlPower() {
        guard let current = control else { return }
        performDeviceAction(current.device.id, operation: { [weak self] completion in
            guard let self else { return }
            self.sdk.setPower(
                deviceId: current.device.id,
                isOn: !current.state.isOn,
                onResult: completion
            )
        }, extract: { try self.require($0.control, from: $0) }, onSuccess: { [weak self] snapshot in
            self?.apply(snapshot)
        })
    }

    func setBrightness(_ value: Int) {
        guard let current = control else { return }
        let deviceID = current.device.id
        guard !workingDeviceIDs.contains(deviceID) else {
            pendingBrightnessByDeviceID[deviceID] = value
            return
        }
        pendingBrightnessByDeviceID.removeValue(forKey: deviceID)
        guard Int(current.state.brightness) != value else {
            flushPendingControlChanges(for: deviceID)
            return
        }
        sendBrightness(value, for: current)
    }

    func setColor(_ color: Color) {
        guard let current = control else { return }
        let deviceID = current.device.id
        guard !workingDeviceIDs.contains(deviceID) else {
            pendingColorByDeviceID[deviceID] = color
            return
        }
        pendingColorByDeviceID.removeValue(forKey: deviceID)
        guard !UIColor(color).isApproximatelyEqual(to: UIColor(current.state.color)) else {
            flushPendingControlChanges(for: deviceID)
            return
        }
        sendColor(color, for: current)
    }

    private func sendBrightness(_ value: Int, for current: ControlSnapshot) {
        let deviceID = current.device.id
        performDeviceAction(deviceID, operation: { [weak self] completion in
            guard let self else { return }
            self.sdk.setBrightness(
                deviceId: deviceID,
                brightness: Int32(value),
                onResult: completion
            )
        }, extract: { try self.require($0.control, from: $0) }, onSuccess: { [weak self] snapshot in
            self?.apply(snapshot)
        })
    }

    private func sendColor(_ color: Color, for current: ControlSnapshot) {
        let deviceID = current.device.id
        let components = UIColor(color).rgbComponents
        performDeviceAction(deviceID, operation: { [weak self] completion in
            guard let self else { return }
            self.sdk.setColor(
                deviceId: deviceID,
                red: Int32(components.red),
                green: Int32(components.green),
                blue: Int32(components.blue),
                onResult: completion
            )
        }, extract: { try self.require($0.control, from: $0) }, onSuccess: { [weak self] snapshot in
            self?.apply(snapshot)
        })
    }

    func setThemeMode(_ mode: String) {
        sdk.setThemeMode(value: mode, onResult: { [weak self] result in
            DispatchQueue.main.async {
                guard let self else { return }
                do {
                    self.themeMode = try self.require(result.themeMode, from: result)
                } catch {
                    self.errorMessage = error.localizedDescription
                }
            }
        })
    }

    func clearNotices() {
        errorMessage = nil
        successMessage = nil
    }

    private func loadThemeMode() {
        sdk.loadThemeMode(onResult: { [weak self] result in
            DispatchQueue.main.async {
                guard let self else { return }
                if let mode = try? self.require(result.themeMode, from: result) {
                    self.themeMode = mode
                }
            }
        })
    }

    private func performDeviceAction<Value>(
        _ deviceID: String,
        operation: (@escaping (WordClockSdkResult) -> Void) -> Void,
        extract: @escaping (WordClockSdkResult) throws -> Value,
        onSuccess: @escaping (Value) -> Void
    ) {
        guard !workingDeviceIDs.contains(deviceID) else { return }
        workingDeviceIDs.insert(deviceID)
        errorMessage = nil
        operation { [weak self] result in
            DispatchQueue.main.async {
                guard let self else { return }
                self.workingDeviceIDs.remove(deviceID)
                do {
                    onSuccess(try extract(result))
                    self.flushPendingControlChanges(for: deviceID)
                } catch {
                    self.pendingBrightnessByDeviceID.removeValue(forKey: deviceID)
                    self.pendingColorByDeviceID.removeValue(forKey: deviceID)
                    self.errorMessage = error.localizedDescription
                    if let index = self.devices.firstIndex(where: { $0.id == deviceID }) {
                        let current = self.devices[index]
                        let offline = current.withConnectionState(isOnline: false, isOn: current.isOn)
                        self.devices[index] = offline
                        if self.control?.device.id == deviceID, let state = self.control?.state {
                            self.updateControl(ControlSnapshot(device: offline, state: state))
                        }
                    }
                }
            }
        }
    }

    private func flushPendingControlChanges(for deviceID: String) {
        guard !workingDeviceIDs.contains(deviceID),
              let current = control,
              current.device.id == deviceID else { return }

        if let brightness = pendingBrightnessByDeviceID.removeValue(forKey: deviceID),
           Int(current.state.brightness) != brightness {
            sendBrightness(brightness, for: current)
            return
        }

        if let color = pendingColorByDeviceID.removeValue(forKey: deviceID),
           !UIColor(color).isApproximatelyEqual(to: UIColor(current.state.color)) {
            sendColor(color, for: current)
        }
    }

    private func require<Value>(
        _ value: Value?,
        from result: WordClockSdkResult
    ) throws -> Value {
        if let message = result.errorMessage { throw SdkError(message: message) }
        guard let value else {
            throw SdkError(message: "The clock returned an empty response.")
        }
        return value
    }

    private func apply(_ snapshot: ControlSnapshot) {
        updateControl(snapshot)
        replace(snapshot.device, power: snapshot.state.isOn)
    }

    private func replace(_ updated: ClockDevice, power: Bool? = nil) {
        let index = devices.firstIndex { $0.id == updated.id }
        let value = updated.withConnectionState(
            isOnline: updated.isOnline || power != nil,
            isOn: power ?? updated.isOn
        )
        if let index {
            devices[index] = value
        } else {
            devices.insert(value, at: 0)
        }
    }

    private func updateControl(_ value: ControlSnapshot?) {
        control = value
        controlRevision += 1
    }
}

private struct SdkError: LocalizedError {
    let message: String
    var errorDescription: String? { message }
}

