package com.example.expenseapp.storage

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.expenseapp.model.Expense
import kotlinx.coroutines.flow.first
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

private val Context.dataStore by preferencesDataStore(name = "expense_prefs")

// ── 추상 기반 (테스트 격리) ────────────────────────────────────
abstract class DataStoreManager {
    abstract suspend fun save(expenses: List<Expense>)
    abstract suspend fun load(): List<Expense>
}

// ── 실제 DataStore 구현 ───────────────────────────────────────
class RealDataStoreManager(private val context: Context) : DataStoreManager() {

    private val KEY  = stringPreferencesKey("expenses_v1")
    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun save(expenses: List<Expense>) {
        context.dataStore.edit { prefs ->
            prefs[KEY] = json.encodeToString(ListSerializer(Expense.serializer()), expenses)
        }
    }

    override suspend fun load(): List<Expense> = try {
        val raw = context.dataStore.data.first()[KEY] ?: return emptyList()
        json.decodeFromString(ListSerializer(Expense.serializer()), raw)
    } catch (e: Exception) {
        emptyList()
    }
}

// ── 인메모리 Fake (테스트 전용) ──────────────────────────────
class FakeDataStoreManager : DataStoreManager() {
    private var stored: List<Expense> = emptyList()
    override suspend fun save(expenses: List<Expense>) { stored = expenses }
    override suspend fun load(): List<Expense> = stored
}
