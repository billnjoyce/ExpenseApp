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
    private(set) var errorMessage: String?     = nil

    // MARK: - 저장소 (로컬 또는 Firestore)
    private let storage: any ExpenseStorageProtocol

    init(storage: any ExpenseStorageProtocol = UserDefaultsStorage.shared) {
        self.storage = storage
        let loaded = storage.load()
        if !loaded.isEmpty { expenses = loaded }
        observeFirestore()
    }

    // MARK: - Firestore 실시간 구독
    private func observeFirestore() {
        guard let fs = storage as? FirestoreManager else { return }
        fs.onExpensesUpdated = { [weak self] updated in
            self?.expenses = updated
        }
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

    var totalAmount: Int { filteredExpenses.reduce(0) { $0 + $1.amount } }

    var thisMonthTotal: Int {
        let cal = Calendar.current
        return expenses.filter {
            cal.isDate($0.date, equalTo: .now, toGranularity: .month)
        }.reduce(0) { $0 + $1.amount }
    }

    struct CategoryStat: Identifiable {
        var id: String { category }
        let category: String
        let total: Int; let percent: Double; let emoji: String
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
            }.sorted { $0.total > $1.total }
    }

    // MARK: - CRUD
    func add(amount: Int, category: String, note: String) {
        guard amount > 0, amount <= ExpenseLimit.maxAmount else { return }
        let expense = Expense(amount: amount, category: category,
                              note: String(note.prefix(ExpenseLimit.maxNoteLength)))
        expenses.insert(expense, at: 0)   // 낙관적 업데이트

        if let fs = storage as? FirestoreManager {
            Task {
                do { try await fs.addExpense(expense) }
                catch { rollbackAdd(expense, error: error) }
            }
        } else { persist() }
    }

    func delete(_ expense: Expense) {
        recentlyDeleted = expense
        expenses.removeAll { $0.id == expense.id }

        if let fs = storage as? FirestoreManager {
            Task {
                do { try await fs.deleteExpense(expense) }
                catch {
                    expenses.insert(expense, at: 0)
                    recentlyDeleted = nil
                    errorMessage = "삭제 실패: \(error.localizedDescription)"
                }
            }
        } else { persist() }

        undoTimer?.cancel()
        undoTimer = Task {
            try? await Task.sleep(for: .seconds(4))
            if !Task.isCancelled { recentlyDeleted = nil }
        }
    }

    func delete(at offsets: IndexSet) {
        offsets.map { filteredExpenses[$0] }.forEach { delete($0) }
    }

    func undoDelete() {
        guard let deleted = recentlyDeleted else { return }
        expenses.insert(deleted, at: 0)
        recentlyDeleted = nil
        undoTimer?.cancel()
        if let fs = storage as? FirestoreManager {
            Task { try? await fs.addExpense(deleted) }
        } else { persist() }
    }

    func update(_ expense: Expense) {
        guard expense.amount > 0,
              let idx = expenses.firstIndex(where: { $0.id == expense.id })
        else { return }
        expenses[idx] = expense
        if let fs = storage as? FirestoreManager {
            Task {
                do { try await fs.updateExpense(expense) }
                catch { errorMessage = "수정 실패: \(error.localizedDescription)" }
            }
        } else { persist() }
    }

    // MARK: - 내부 헬퍼
    private func rollbackAdd(_ expense: Expense, error: Error) {
        expenses.removeAll { $0.id == expense.id }
        errorMessage = "저장 실패: \(error.localizedDescription)"
    }

    private func persist() {
        let snap = expenses; let s = storage
        Task.detached(priority: .background) { s.save(snap) }
    }
}
