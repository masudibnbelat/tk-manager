package com.example.data.model

data class TransactionEntity(
    val id: Long = 0,
    val type: String, // "INCOME" or "EXPENSE"
    val title: String,
    val amount: Double,
    val category: String,
    val dateMillis: Long,
    val note: String = "",
    val yearMonth: Int, // e.g. 202609
    val day: Int        // 1..31
)
