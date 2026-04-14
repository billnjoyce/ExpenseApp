import SwiftUI

struct StatisticsView: View {
    let vm: ExpenseViewModel

    var body: some View {
        NavigationStack {
            List {
                Section {
                    HStack {
                        VStack(alignment: .leading, spacing: 4) {
                            Text("이번 달 총 지출").font(.caption).foregroundStyle(.secondary)
                            Text("₩\(vm.thisMonthTotal.formatted())").font(.title2.bold())
                        }
                        Spacer()
                        Image(systemName: "chart.pie.fill").font(.largeTitle).foregroundStyle(.blue.gradient)
                    }
                    .padding(.vertical, 4)
                }

                Section("카테고리별 지출") {
                    if vm.categoryBreakdown.isEmpty {
                        ContentUnavailableView("데이터 없음", systemImage: "chart.bar")
                    } else {
                        ForEach(vm.categoryBreakdown) { stat in
                            VStack(alignment: .leading, spacing: 6) {
                                HStack {
                                    Text(stat.emoji)
                                    Text(stat.category).font(.subheadline.weight(.semibold))
                                    Spacer()
                                    Text("₩\(stat.total.formatted())").font(.subheadline.bold())
                                    Text(stat.percent.percentFormatted())
                                        .font(.caption).foregroundStyle(.secondary)
                                }
                                ProgressView(value: stat.percent)
                                    .tint(.blue)
                                    .animation(.spring(), value: stat.percent)
                            }
                            .padding(.vertical, 2)
                        }
                    }
                }
            }
            .navigationTitle("통계").navigationBarTitleDisplayMode(.large)
        }
    }
}
