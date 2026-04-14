import SwiftUI

// MARK: - ExpenseRowView
struct ExpenseRowView: View {
    let expense: Expense

    var body: some View {
        HStack(spacing: 12) {
            ZStack {
                Circle().fill(Color(.systemGray6)).frame(width: 44, height: 44)
                Text(expense.categoryEmoji).font(.title3)
            }
            VStack(alignment: .leading, spacing: 2) {
                Text(expense.category).font(.headline)
                if !expense.note.isEmpty {
                    Text(expense.note).font(.caption).foregroundStyle(.secondary).lineLimit(1)
                }
                Text(expense.date.formattedExpense()).font(.caption2).foregroundStyle(.tertiary)
            }
            Spacer()
            Text("₩\(expense.amount.formatted())").font(.callout.bold())
        }
        .padding(.horizontal, 16).padding(.vertical, 10)
        .background(Color(.systemBackground))
        .contentShape(Rectangle())
    }
}

// MARK: - CategoryFilterRow
struct CategoryFilterRow: View {
    @Binding var selected: String?
    private let all = ["전체"] + ExpenseLimit.defaultCategories

    var body: some View {
        ScrollView(.horizontal, showsIndicators: false) {
            HStack(spacing: 8) {
                ForEach(all, id: \.self) { cat in
                    let isSelected = cat == "전체" ? selected == nil : selected == cat
                    Button {
                        withAnimation(.spring(duration: 0.2)) {
                            selected = cat == "전체" ? nil : cat
                        }
                    } label: {
                        Text(cat)
                            .font(.caption.weight(.semibold))
                            .padding(.horizontal, 12).padding(.vertical, 6)
                            .background(isSelected ? Color.accentColor : Color(.systemGray5))
                            .foregroundStyle(isSelected ? .white : .primary)
                            .clipShape(Capsule())
                    }
                    .buttonStyle(.plain)
                }
            }
            .padding(.horizontal, 16)
        }
    }
}

// MARK: - AddExpenseView
struct AddExpenseView: View {
    let vm: ExpenseViewModel
    @Environment(\.dismiss) private var dismiss
    @State private var amountText = ""
    @State private var category   = ExpenseLimit.defaultCategories[0]
    @State private var note       = ""
    @State private var amountError: String? = nil

    private var isValid: Bool { amountError == nil && !amountText.isEmpty }

    var body: some View {
        NavigationStack {
            Form {
                Section("금액") {
                    HStack {
                        Text("₩").foregroundStyle(.secondary)
                        TextField("0", text: $amountText)
                            .keyboardType(.numberPad)
                            .onChange(of: amountText) { _, v in validate(v) }
                    }
                    if let err = amountError {
                        Text(err).font(.caption).foregroundStyle(.red)
                    }
                }
                Section("카테고리") {
                    Picker("카테고리", selection: $category) {
                        ForEach(ExpenseLimit.defaultCategories, id: \.self) { Text($0).tag($0) }
                    }.pickerStyle(.wheel)
                }
                Section("메모") {
                    TextField("선택사항", text: $note, axis: .vertical).lineLimit(2...4)
                }
            }
            .navigationTitle("지출 추가").navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction)  { Button("취소") { dismiss() } }
                ToolbarItem(placement: .confirmationAction)  {
                    Button("저장") {
                        vm.add(amount: Int(amountText) ?? 0, category: category, note: note)
                        dismiss()
                    }.disabled(!isValid)
                }
            }
        }
    }

    private func validate(_ text: String) {
        guard !text.isEmpty else { amountError = nil; return }
        guard let v = Int(text) else { amountError = "숫자만 입력해주세요"; return }
        if v <= 0 { amountError = "0원보다 큰 금액을 입력해주세요"; return }
        if v > ExpenseLimit.maxAmount { amountError = "10,000,000원 이하로 입력해주세요"; return }
        amountError = nil
    }
}

// MARK: - EditExpenseView
struct EditExpenseView: View {
    let vm: ExpenseViewModel
    let expense: Expense
    @Environment(\.dismiss) private var dismiss
    @State private var amountText: String
    @State private var category:   String
    @State private var note:       String

    init(vm: ExpenseViewModel, expense: Expense) {
        self.vm = vm; self.expense = expense
        _amountText = State(initialValue: "\(expense.amount)")
        _category   = State(initialValue: expense.category)
        _note       = State(initialValue: expense.note)
    }

    private var isValid: Bool { (Int(amountText) ?? 0) > 0 }

    var body: some View {
        NavigationStack {
            Form {
                Section("금액") {
                    HStack {
                        Text("₩").foregroundStyle(.secondary)
                        TextField("0", text: $amountText).keyboardType(.numberPad)
                    }
                }
                Section("카테고리") {
                    Picker("카테고리", selection: $category) {
                        ForEach(ExpenseLimit.defaultCategories, id: \.self) { Text($0).tag($0) }
                    }.pickerStyle(.wheel)
                }
                Section("메모") {
                    TextField("선택사항", text: $note, axis: .vertical).lineLimit(2...4)
                }
            }
            .navigationTitle("지출 수정").navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) { Button("취소") { dismiss() } }
                ToolbarItem(placement: .confirmationAction) {
                    Button("저장") {
                        var updated = expense
                        updated.amount   = Int(amountText) ?? expense.amount
                        updated.category = category
                        updated.note     = note
                        vm.update(updated); dismiss()
                    }.disabled(!isValid)
                }
            }
        }
    }
}
