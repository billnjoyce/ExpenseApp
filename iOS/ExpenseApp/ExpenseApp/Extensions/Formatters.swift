import Foundation

extension Int {
    func formatted() -> String {
        let f = NumberFormatter()
        f.numberStyle = .decimal
        f.locale = Locale(identifier: "ko_KR")
        return f.string(from: NSNumber(value: self)) ?? "\(self)"
    }
    func formattedWithCurrency() -> String { "₩\(formatted())" }
}

extension Date {
    func formattedExpense() -> String {
        let f = DateFormatter()
        f.locale = Locale(identifier: "ko_KR")
        f.dateFormat = "M월 d일 (E)"
        return f.string(from: self)
    }
    func isToday() -> Bool { Calendar.current.isDateInToday(self) }
    func isThisMonth() -> Bool {
        Calendar.current.isDate(self, equalTo: .now, toGranularity: .month)
    }
}

extension Double {
    func percentFormatted() -> String { String(format: "%.1f%%", self * 100) }
}
