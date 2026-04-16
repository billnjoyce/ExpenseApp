package com.example.expenseapp.firebase

import com.example.expenseapp.model.Expense
import com.example.expenseapp.storage.DataStoreManager
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.Date

// MARK: - FirestoreManager
// DataStoreManager를 상속해 기존 ViewModel과 완전 호환
class FirestoreManager(private val userId: String) : DataStoreManager() {

    private val db: FirebaseFirestore = Firebase.firestore
    private val collection get() = db.collection("users")
        .document(userId)
        .collection("expenses")

    // MARK: - 실시간 동기화 Flow (Firestore Snapshot Listener)
    val expensesFlow: Flow<List<Expense>> = callbackFlow {
        val listener: ListenerRegistration = collection
            .orderBy("date", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error); return@addSnapshotListener
                }
                val expenses = snapshot?.documents
                    ?.mapNotNull { doc ->
                        runCatching {
                            Expense(
                                id       = doc.getString("id")       ?: doc.id,
                                amount   = (doc.getLong("amount")    ?: 0L).toInt(),
                                category = doc.getString("category") ?: "",
                                note     = doc.getString("note")     ?: "",
                                date     = doc.getDate("date")?.time ?: System.currentTimeMillis()
                            )
                        }.getOrNull()
                    } ?: emptyList()
                trySend(expenses)
            }
        awaitClose { listener.remove() }
    }

    // MARK: - DataStoreManager 구현 (호환용)
    override suspend fun save(expenses: List<Expense>) {
        val batch = db.batch()
        expenses.forEach { expense ->
            val ref = collection.document(expense.id)
            batch.set(ref, expense.toMap())
        }
        batch.commit().await()
    }

    override suspend fun load(): List<Expense> {
        return try {
            collection
                .orderBy("date", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get().await()
                .documents
                .mapNotNull { doc ->
                    runCatching {
                        Expense(
                            id       = doc.getString("id")       ?: doc.id,
                            amount   = (doc.getLong("amount")    ?: 0L).toInt(),
                            category = doc.getString("category") ?: "",
                            note     = doc.getString("note")     ?: "",
                            date     = doc.getDate("date")?.time ?: System.currentTimeMillis()
                        )
                    }.getOrNull()
                }
        } catch (e: Exception) {
            emptyList()
        }
    }

    // MARK: - 개별 CRUD (권장)

    suspend fun addExpense(expense: Expense): Result<Unit> = runCatching {
        collection.document(expense.id).set(expense.toMap()).await()
    }

    suspend fun updateExpense(expense: Expense): Result<Unit> = runCatching {
        collection.document(expense.id).set(expense.toMap()).await()
    }

    suspend fun deleteExpense(expense: Expense): Result<Unit> = runCatching {
        collection.document(expense.id).delete().await()
    }

    // MARK: - Expense → Firestore Map
    private fun Expense.toMap(): Map<String, Any> = mapOf(
        "id"       to id,
        "amount"   to amount,
        "category" to category,
        "note"     to note,
        "date"     to Date(date)     // Firestore Timestamp로 자동 변환
    )
}
