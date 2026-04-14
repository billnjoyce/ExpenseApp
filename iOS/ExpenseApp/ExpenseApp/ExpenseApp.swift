import SwiftUI

@main
struct ExpenseApp: App {
    var body: some Scene {
        WindowGroup {
            ContentRootView()
        }
    }
}

struct ContentRootView: View {
    @State private var vm = ExpenseViewModel()

    var body: some View {
        TabView {
            ExpenseListView(vm: vm)
                .tabItem {
                    Label("지출", systemImage: "list.bullet.rectangle.portrait")
                }

            StatisticsView(vm: vm)
                .tabItem {
                    Label("통계", systemImage: "chart.bar.fill")
                }
        }
    }
}
