import Foundation
import FirebaseFirestore

// MARK: - FirestoreManager
// ExpenseStorageProtocol을 채택하여 기존 ViewModel과 완전 호환
final class FirestoreManager: ExpenseStorageProtocol {

    private let db:     Firestore
    private let userId: String
    private var listener: ListenerRegistration?

    // 실시간 스트림 콜백
    var onExpensesUpdated: (([Expense]) -> Void)?

    private var collection: CollectionReference {
        db.collection("users").document(userId).collection("expenses")
    }

    init(userId: String) {
        self.db     = Firestore.firestore()
        self.userId = userId
        startListening()
    }

    deinit {
        listener?.remove()
    }

    // MARK: - 실시간 동기화 (Firestore Listener)
    private func startListening() {
        listener = collection
            .order(by: "date", descending: true)
            .addSnapshotListener { [weak self] snapshot, error in
                guard let self, let snapshot else {
                    print("Firestore 리스너 오류:", error?.localizedDescription ?? "")
                    return
                }
                let expenses = snapshot.documents.compactMap {
                    try? $0.data(as: FirestoreExpense.self)
                }.map(\.toExpense)

                Task { @MainActor in
                    self.onExpensesUpdated?(expenses)
                }
            }
    }

    // MARK: - ExpenseStorageProtocol 구현

    /// 전체 목록 저장 (로컬→클라우드 Batch Write)
    func save(_ expenses: [Expense]) {
        let batch = db.batch()
        // 기존 문서 삭제 후 새로 쓰는 방식 대신,
        // 개별 CRUD 메서드를 사용하는 것을 권장.
        // 이 메서드는 초기 마이그레이션 용도
        expenses.forEach { expense in
            let ref = collection.document(expense.id.uuidString)
            try? batch.setData(from: FirestoreExpense(expense), forDocument: ref)
        }
        batch.commit { error in
            if let error { print("Batch 저장 오류:", error) }
        }
    }

    /// 초기 로드 (한 번 읽기)
    func load() -> [Expense] {
        // Firestore는 비동기이므로, 초기값은 빈 배열 반환 후
        // startListening()의 onExpensesUpdated 콜백으로 업데이트됨
        return []
    }

    // MARK: - 개별 CRUD (권장)

    func addExpense(_ expense: Expense) async throws {
        let ref = collection.document(expense.id.uuidString)
        try ref.setData(from: FirestoreExpense(expense))
    }

    func updateExpense(_ expense: Expense) async throws {
        let ref = collection.document(expense.id.uuidString)
        try ref.setData(from: FirestoreExpense(expense), merge: true)
    }

    func deleteExpense(_ expense: Expense) async throws {
        try await collection.document(expense.id.uuidString).delete()
    }

    func fetchExpenses() async throws -> [Expense] {
        let snapshot = try await collection
            .order(by: "date", descending: true)
            .getDocuments()
        return snapshot.documents
            .compactMap { try? $0.data(as: FirestoreExpense.self) }
            .map(\.toExpense)
    }
}

// MARK: - Firestore DTO (Codable 직렬화용)
struct FirestoreExpense: Codable {
    @DocumentID var documentId: String?
    let id:       String
    let amount:   Int
    let category: String
    let note:     String
    let date:     Date        // Firestore Timestamp ↔ Date 자동 변환

    init(_ expense: Expense) {
        self.id       = expense.id.uuidString
        self.amount   = expense.amount
        self.category = expense.category
        self.note     = expense.note
        self.date     = expense.date
    }

    var toExpense: Expense {
        Expense(
            id:       UUID(uuidString: id) ?? UUID(),
            amount:   amount,
            category: category,
            note:     note,
            date:     date
        )
    }
}
