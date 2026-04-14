package com.example.expenseapp.model

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class Expense(
    val id:       String = UUID.randomUUID().toString(),
    val amount:   Int,
    val category: String,
    val note:     String = "",
    val date:     Long   = System.currentTimeMillis()
) {
    val categoryEmoji: String
        get() = when (category) {
            "식비"  -> "🍽️"
            "교통"  -> "🚌"
            "카페"  -> "☕"
            "쇼핑"  -> "🛍️"
            "의료"  -> "💊"
            "문화"  -> "🎬"
            else   -> "📦"
        }

    companion object {
        val samples = listOf(
            Expense(amount = 12000, category = "식비",  note = "점심 - 설렁탕"),
            Expense(amount = 1500,  category = "교통",  note = "버스"),
            Expense(amount = 5500,  category = "카페",  note = "아메리카노"),
            Expense(amount = 35000, category = "쇼핑",  note = "편의점"),
            Expense(amount = 8000,  category = "식비",  note = "저녁 - 순대국"),
        )
    }
}

object ExpenseConstants {
    const val MAX_AMOUNT       = 10_000_000
    const val MAX_NOTE_LENGTH  = 100
    val DEFAULT_CATEGORIES     = listOf("식비", "교통", "카페", "쇼핑", "의료", "문화", "기타")
}
