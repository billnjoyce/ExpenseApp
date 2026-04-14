import Foundation

// MARK: - 저장소 프로토콜
protocol ExpenseStorageProtocol {
    func save(_ expenses: [Expense])
    func load() -> [Expense]
}

// MARK: - UserDefaults 구현체
final class UserDefaultsStorage: ExpenseStorageProtocol {
    static let shared = UserDefaultsStorage()
    private let key = "expenses_v1"
    private let defaults: UserDefaults

    init(suiteName: String? = nil) {
        defaults = suiteName.map { UserDefaults(suiteName: $0)! } ?? .standard
    }

    func save(_ expenses: [Expense]) {
        guard let data = try? JSONEncoder().encode(expenses) else { return }
        defaults.set(data, forKey: key)
    }

    func load() -> [Expense] {
        guard let data = defaults.data(forKey: key) else { return [] }
        return (try? JSONDecoder().decode([Expense].self, from: data)) ?? []
    }
}

// MARK: - 인메모리 (테스트용)
final class InMemoryStorage: ExpenseStorageProtocol {
    private var stored: [Expense] = []
    func save(_ expenses: [Expense]) { stored = expenses }
    func load() -> [Expense] { stored }
}
