import XCTest
@testable import ExpenseApp

@MainActor
final class ExpenseViewModelTests: XCTestCase {

    var sut: ExpenseViewModel!

    override func setUp() {
        super.setUp()
        sut = ExpenseViewModel(storage: InMemoryStorage())
    }
    override func tearDown() { sut = nil; super.tearDown() }

    // MARK: - add

    func testAdd_validAmount_increasesCount() {
        let before = sut.expenses.count
        sut.add(amount: 5000, category: "식비", note: "점심")
        XCTAssertEqual(sut.expenses.count, before + 1)
    }

    func testAdd_zeroAmount_doesNotAdd() {
        let before = sut.expenses.count
        sut.add(amount: 0, category: "식비", note: "")
        XCTAssertEqual(sut.expenses.count, before)
    }

    func testAdd_overLimit_doesNotAdd() {
        let before = sut.expenses.count
        sut.add(amount: 99_999_999, category: "식비", note: "")
        XCTAssertEqual(sut.expenses.count, before)
    }

    // MARK: - totalAmount

    func testTotalAmount_correct() {
        sut.add(amount: 3000, category: "카페", note: "")
        sut.add(amount: 7000, category: "식비", note: "")
        XCTAssertEqual(sut.totalAmount, 10000)
    }

    // MARK: - delete

    func testDelete_removesExpense() {
        sut.add(amount: 5000, category: "교통", note: "")
        let exp = sut.expenses[0]
        sut.delete(exp)
        XCTAssertFalse(sut.expenses.contains { $0.id == exp.id })
    }

    func testUndoDelete_restores() {
        sut.add(amount: 5000, category: "교통", note: "버스")
        let exp = sut.expenses[0]
        sut.delete(exp)
        sut.undoDelete()
        XCTAssertTrue(sut.expenses.contains { $0.id == exp.id })
    }

    // MARK: - filter

    func testFilter_byCategory() {
        sut.add(amount: 1000, category: "식비", note: "")
        sut.add(amount: 500,  category: "교통", note: "")
        sut.add(amount: 800,  category: "식비", note: "")
        sut.filterCategory = "식비"
        XCTAssertEqual(sut.filteredExpenses.count, 2)
        XCTAssertTrue(sut.filteredExpenses.allSatisfy { $0.category == "식비" })
    }

    func testFilter_nil_returnsAll() {
        sut.add(amount: 1000, category: "식비", note: "")
        sut.add(amount: 500,  category: "교통", note: "")
        sut.filterCategory = nil
        XCTAssertEqual(sut.filteredExpenses.count, sut.expenses.count)
    }

    // MARK: - save/load

    func testSaveAndLoad_persists() {
        let storage = InMemoryStorage()
        let vm1 = ExpenseViewModel(storage: storage)
        vm1.add(amount: 12000, category: "식비", note: "저장 테스트")
        let vm2 = ExpenseViewModel(storage: storage)
        XCTAssertEqual(vm2.expenses.count, 1)
        XCTAssertEqual(vm2.expenses[0].amount, 12000)
    }
}
