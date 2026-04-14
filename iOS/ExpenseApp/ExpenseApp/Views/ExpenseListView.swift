import SwiftUI

struct ExpenseListView: View {
    let vm: ExpenseViewModel
    @State private var showingAdd   = false
    @State private var editExpense: Expense? = nil

    var body: some View {
        NavigationStack {
            Group {
                if vm.filteredExpenses.isEmpty { emptyView }
                else { listView }
            }
            .navigationTitle("지출 내역")
            .toolbar { toolbarContent }
            .searchable(text: Bindable(vm).searchText,
                        placement: .navigationBarDrawer(displayMode: .always),
                        prompt: "지출 검색")
            .sheet(isPresented: $showingAdd)  { AddExpenseView(vm: vm) }
            .sheet(item: $editExpense)        { EditExpenseView(vm: vm, expense: $0) }
            .safeAreaInset(edge: .bottom)     { totalCard }
            .overlay(alignment: .bottom) {
                if vm.recentlyDeleted != nil {
                    undoToast
                        .padding(.bottom, 90)
                        .transition(.move(edge: .bottom).combined(with: .opacity))
                }
            }
            .animation(.spring(), value: vm.recentlyDeleted != nil)
        }
    }

    private var listView: some View {
        List {
            CategoryFilterRow(selected: Bindable(vm).filterCategory)
                .listRowInsets(EdgeInsets(top: 6, leading: 0, bottom: 6, trailing: 0))
                .listRowSeparator(.hidden)

            ForEach(vm.filteredExpenses) { expense in
                ExpenseRowView(expense: expense)
                    .listRowInsets(EdgeInsets())
                    .listRowSeparator(.hidden)
                    .contentShape(Rectangle())
                    .onTapGesture { editExpense = expense }
                    .swipeActions(edge: .trailing, allowsFullSwipe: true) {
                        Button(role: .destructive) {
                            withAnimation { vm.delete(expense) }
                        } label: { Label("삭제", systemImage: "trash") }
                    }
            }
        }
        .listStyle(.plain)
    }

    private var emptyView: some View {
        ContentUnavailableView {
            Label("지출 내역 없음", systemImage: "creditcard.slash")
        } description: {
            Text("+ 버튼을 눌러 첫 지출을 기록해보세요")
        }
    }

    private var totalCard: some View {
        HStack {
            Text("총 지출").font(.subheadline).foregroundStyle(.secondary)
            Spacer()
            Text("₩\(vm.totalAmount.formatted())").font(.title3.bold())
        }
        .padding(.horizontal, 20).padding(.vertical, 12)
        .background(.regularMaterial)
        .overlay(Divider(), alignment: .top)
    }

    @ToolbarContentBuilder
    private var toolbarContent: some ToolbarContent {
        ToolbarItem(placement: .navigationBarTrailing) {
            Button { showingAdd = true } label: {
                Image(systemName: "plus.circle.fill").font(.title3)
            }
        }
    }

    private var undoToast: some View {
        HStack(spacing: 12) {
            Text("삭제됨").font(.subheadline)
            Spacer()
            Button("실행 취소") { withAnimation { vm.undoDelete() } }
                .font(.subheadline.bold()).foregroundStyle(.blue)
        }
        .padding(.horizontal, 20).padding(.vertical, 12)
        .background(.ultraThinMaterial, in: RoundedRectangle(cornerRadius: 12))
        .padding(.horizontal, 16)
    }
}
