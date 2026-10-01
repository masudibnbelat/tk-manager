package com.example.data.model

data class LoanEntity(
    val id: Long = 0,
    val personName: String,
    val type: String, // "LENT" or "BORROWED"
    val totalAmount: Double,
    val paidAmount: Double = 0.0,
    val note: String = "",
    val dateMillis: Long,
    val dueDateMillis: Long? = null,
    val isSettled: Boolean = false
) {
    val remainingAmount: Double
        get() = (totalAmount - paidAmount).coerceAtLeast(0.0)

    val progress: Float
        get() = if (totalAmount > 0) (paidAmount / totalAmount).coerceIn(0.0, 1.0).toFloat() else 1f
}

data class RepaymentEntity(
    val id: Long = 0,
    val loanId: Long,
    val amount: Double,
    val dateMillis: Long,
    val note: String = ""
)
