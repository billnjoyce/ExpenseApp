import Foundation

// MARK: - Expense 모델
struct Expense: Identifiable, Codable, Equatable {
    var id: UUID
    var amount: Int
    var category: String
    var note: String
    var date: Date

    init(id: UUID = UUID(), amount: Int, category: String, note: String = "", date: Date = Date()) {
        self.id       = id
        self.amount   = amount
        self.category = category
        self.note     = note
        self.date     = date
    }

    var categoryEmoji: String {
        switch category {
        case "식비":  return "🍽️"
        case "교통":  return "🚌"
        case "카페":  return "☕"
        case "쇼핑":  return "🛍️"
        case "의료":  return "💊"
        case "문화":  return "🎬"
        default:     return "📦"
        }
    }

    static let samples: [Expense] = [
        Expense(amount: 12000, category: "식비",  note: "점심 - 설렁탕"),
        Expense(amount: 1500,  category: "교통",  note: "버스"),
        Expense(amount: 5500,  category: "카페",  note: "아메리카노"),
        Expense(amount: 35000, category: "쇼핑",  note: "편의점"),
        Expense(amount: 8000,  category: "식비",  note: "저녁 - 순대국"),
    ]
}

// MARK: - 상수
enum ExpenseLimit {
    static let maxAmount       = 10_000_000
    static let maxNoteLength   = 100
    static let defaultCategories = ["식비", "교통", "카페", "쇼핑", "의료", "문화", "기타"]
}
