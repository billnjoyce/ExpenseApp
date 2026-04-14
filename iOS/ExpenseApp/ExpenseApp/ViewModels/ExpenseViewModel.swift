import Foundation
import Observation

@MainActor
@Observable
final class ExpenseViewModel {

    // MARK: - 상태
    private(set) var expenses: [Expense] = []
    var filterCategory: String?  = nil
    var searchText:     String   = ""
    private(set) var recentlyDeleted: Expense? = nil
    private var undoTimer: Task<Void, Never>?  = nil

    // MARK: - 저장소 (의존성 주입)
    private let storage: any ExpenseStorageProtocol

    init(storage: any ExpenseStorageProtocol = UserDefaultsStorage.shared) {
        self.storage  = storage
        self.expenses = storage.load()
    }

    // MARK: - Computed
    var filteredExpenses: [Expense] {
        var result = expenses
        if let cat = filterCategory {
            result = result.filter { $0.category == cat }
        }
        if !searchText.isEmpty {
            result = result.filter {
                $0.category.localizedCaseInsensitiveContains(searchText) ||
                $0.note.localizedCaseInsensitiveContains(searchText)
            }
        }
        return result
    }

    var totalAmount: Int     { filteredExpenses.reduce(0) { $0 + $1.amount } }

    var thisMonthTotal: Int {
        let cal = Calendar.current
        return expenses.filter {
            cal.isDate($0.date, equalTo: .now, toGranularity: .month)
        }.reduce(0) { $0 + $1.amount }
    }

    // MARK: - Statistics
    struct CategoryStat: Identifiable {
        var id: String { category }
        let category: String
        let total:    Int
        let percent:  Double
        let emoji:    String
    }

    var categoryBreakdown: [CategoryStat] {
        guard !filteredExpenses.isEmpty else { return [] }
        let total = Double(filteredExpenses.reduce(0) { $0 + $1.amount })
        return Dictionary(grouping: filteredExpenses, by: \.category)
            .map { cat, items in
                let sum = items.reduce(0) { $0 + $1.amount }
                return CategoryStat(category: cat, total: sum,
                                    percent: Double(sum) / total,
                                    emoji: items.first?.categoryEmoji ?? "📦")
            }
            .sorted { $0.total > $1.total }
    }

    // MARK: - CRUD
    func add(amount: Int, category: String, note: String) {
        guard amount > 0, amount <= ExpenseLimit.maxAmount else { return }
        expenses.insert(
            Expense(amount: amount, category: category,
                    note: String(note.prefix(ExpenseLimit.maxNoteLength))),
            at: 0
        )
        persist()
    }

    func delete(_ expense: Expense) {
        recentlyDeleted = expense
        expenses.removeAll { $0.id == expense.id }
        persist()
        undoTimer?.cancel()
        undoTimer = Task {
            try? await Task.sleep(for: .seconds(4))
            if !Task.isCancelled { recentlyDeleted = nil }
        }
    }

    func delete(at offsets: IndexSet) {
        offsets.map { filteredExpenses[$0] }.forEach { exp in
            expenses.removeAll { $0.id == exp.id }
        }
        persist()
    }

    func undoDelete() {
        guard let deleted = recentlyDeleted else { return }
        expenses.insert(deleted, at: 0)
        recentlyDeleted = nil
        undoTimer?.cancel()
        persist()
    }

    func update(_ expense: Expense) {
        guard expense.amount > 0,
              let idx = expenses.firstIndex(where: { $0.id == expense.id })
        else { return }
        expenses[idx] = expense
        persist()
    }

    // MARK: - Persist
    private func persist() {
        let snapshot = expenses
        let s = storage
        Task.detached(priority: .background) { s.save(snapshot) }
    }
}
