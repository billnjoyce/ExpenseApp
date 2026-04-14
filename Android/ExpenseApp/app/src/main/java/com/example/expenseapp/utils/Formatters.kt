package com.example.expenseapp.utils

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

/** 8500 → "8,500" */
fun Int.formatAmount(): String =
    NumberFormat.getNumberInstance(Locale.KOREA).format(this)

/** 8500 → "₩8,500" */
fun Int.formatAmountWithCurrency(): String = "₩${formatAmount()}"

/** Long(millis) → "4월 14일 (월)" */
fun Long.formatDate(): String =
    SimpleDateFormat("M월 d일 (E)", Locale.KOREAN).format(Date(this))

/** 오늘 여부 */
fun Long.isToday(): Boolean {
    val a = Calendar.getInstance().apply { timeInMillis = this@isToday }
    val b = Calendar.getInstance()
    return a.get(Calendar.YEAR)        == b.get(Calendar.YEAR) &&
           a.get(Calendar.DAY_OF_YEAR) == b.get(Calendar.DAY_OF_YEAR)
}

/** 0.875f → "87.5%" */
fun Float.percentFormatted(): String = "%.1f%%".format(this)
