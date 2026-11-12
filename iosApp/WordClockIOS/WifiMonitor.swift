import Network
import SwiftUI

@MainActor
final class WifiMonitor: ObservableObject {
    @Published private(set) var isAvailable = false

    private let monitor = NWPathMonitor()
    private let queue = DispatchQueue(label: "com.ghazarian.wordclock.network-monitor")

    init() {
        monitor.pathUpdateHandler = { [weak self] path in
            let available = path.status == .satisfied && path.usesInterfaceType(.wifi)
            DispatchQueue.main.async {
                self?.isAvailable = available
            }
        }
        monitor.start(queue: queue)
    }

    deinit {
        monitor.cancel()
    }
}
