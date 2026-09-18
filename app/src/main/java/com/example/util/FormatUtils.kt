package com.example.util

import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object FormatUtils {

    private val numberFormat = DecimalFormat("#,##0.00")
    private val compactFormat = DecimalFormat("#,##0")
    private val shortDateFormat = SimpleDateFormat("dd MMM, yyyy", Locale.getDefault())
    private val dayOfWeekFormat = SimpleDateFormat("EEEE", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())

    fun formatMoney(amount: Double, symbol: String): String {
        val isNegative = amount < 0
        val absAmount = kotlin.math.abs(amount)
        val formatted = numberFormat.format(absAmount)
        return if (isNegative) "- $symbol $formatted" else "$symbol $formatted"
    }

    fun formatCompactMoney(amount: Double, symbol: String): String {
        val isNegative = amount < 0
        val absAmount = kotlin.math.abs(amount)
        val formatted = compactFormat.format(absAmount)
        return if (isNegative) "- $symbol $formatted" else "$symbol $formatted"
    }

    fun formatDate(millis: Long): String {
        return shortDateFormat.format(Date(millis))
    }

    fun formatDayOfWeek(millis: Long): String {
        return dayOfWeekFormat.format(Date(millis))
    }

    fun formatDateAndDay(millis: Long): String {
        return "${shortDateFormat.format(Date(millis))} • ${dayOfWeekFormat.format(Date(millis))}"
    }

    fun formatDateTime(millis: Long): String {
        return "${shortDateFormat.format(Date(millis))} ${timeFormat.format(Date(millis))}"
    }

}
