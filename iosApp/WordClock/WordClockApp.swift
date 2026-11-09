import SwiftUI
import UIKit
import WordClockShared

@main
struct WordClockApp: App {
    var body: some Scene {
        WindowGroup {
            WordClockComposeView()
                .ignoresSafeArea()
        }
    }
}

private struct WordClockComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        MainViewControllerKt.MainViewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}
