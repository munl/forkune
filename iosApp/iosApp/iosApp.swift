import SwiftUI
import SharedUI

@main
struct ComposeApp: App {
    // Start Koin before any view is built — the Android side does the equivalent in
    // ForkuneApplication.onCreate().
    init() {
        KoinInitIosKt.doInitKoin()
    }

    var body: some Scene {
        WindowGroup {
            ContentView().ignoresSafeArea(.all)
        }
    }
}

struct ContentView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        return MainViewControllerKt.MainViewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {
        // Updates will be handled by Compose
    }
}
