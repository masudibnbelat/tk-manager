package com.example.data.repository

import com.example.data.db.DatabaseHelper
import com.example.data.model.LoanEntity
import com.example.data.model.TransactionEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

data class BalanceSummary(
    val totalIncome: Double = 0.0,
    val totalExpenses: Double = 0.0,
    val currentBalance: Double = 0.0
)

data class CategoryBreakdown(
    val category: String,
    val totalAmount: Double,
    val percentage: Float,
    val count: Int,
    val icon: String
)

data class LoanSummary(
    val totalLent: Double = 0.0,
    val totalBorrowed: Double = 0.0,
    val remainingToReceive: Double = 0.0,
    val remainingToPay: Double = 0.0
)

class FinanceRepository(private val dbHelper: DatabaseHelper) {
    val allTransactions: Flow<List<TransactionEntity>> = dbHelper.allTransactions
    val allLoans: Flow<List<LoanEntity>> = dbHelper.allLoans

    fun getTransactionsByMonth(yearMonth: Int): Flow<List<TransactionEntity>> {
        return allTransactions.map { list ->
            list.filter { it.yearMonth == yearMonth }
        }
    }

    fun getLifetimeBalanceSummary(): Flow<BalanceSummary> {
        return allTransactions.map { list ->
            var income = 0.0
            var expense = 0.0
            for (item in list) {
                if (item.type == "INCOME") {
                    income += item.amount
                } else {
                    expense += item.amount
                }
            }
            BalanceSummary(
                totalIncome = income,
                totalExpenses = expense,
                currentBalance = income - expense
            )
        }
    }

    fun getMonthlyBalanceSummary(yearMonth: Int): Flow<BalanceSummary> {
        return getTransactionsByMonth(yearMonth).map { list ->
            var income = 0.0
            var expense = 0.0
            for (item in list) {
                if (item.type == "INCOME") {
                    income += item.amount
                } else {
                    expense += item.amount
                }
            }
            BalanceSummary(
                totalIncome = income,
                totalExpenses = expense,
                currentBalance = income - expense
            )
        }
    }

    fun getCategoryBreakdown(yearMonth: Int): Flow<List<CategoryBreakdown>> {
        return getTransactionsByMonth(yearMonth).map { list ->
            val expenses = list.filter { it.type == "EXPENSE" }
            val totalExpense = expenses.sumOf { it.amount }
            if (totalExpense <= 0) {
                emptyList()
            } else {
                expenses.groupBy { it.category }
                    .map { (cat, items) ->
                        val amount = items.sumOf { it.amount }
                        val pct = (amount / totalExpense).toFloat()
                        CategoryBreakdown(
                            category = cat,
                            totalAmount = amount,
                            percentage = pct,
                            count = items.size,
                            icon = getCategoryIcon(cat)
                        )
                    }
                    .sortedByDescending { it.totalAmount }
            }
        }
    }

    fun getLoanSummary(): Flow<LoanSummary> {
        return allLoans.map { list ->
            var lentTotal = 0.0
            var lentRemaining = 0.0
            var borrowedTotal = 0.0
            var borrowedRemaining = 0.0

            for (loan in list) {
                if (loan.type == "LENT") {
                    lentTotal += loan.totalAmount
                    if (!loan.isSettled) {
                        lentRemaining += loan.remainingAmount
                    }
                } else {
                    borrowedTotal += loan.totalAmount
                    if (!loan.isSettled) {
                        borrowedRemaining += loan.remainingAmount
                    }
                }
            }

            LoanSummary(
                totalLent = lentTotal,
                totalBorrowed = borrowedTotal,
                remainingToReceive = lentRemaining,
                remainingToPay = borrowedRemaining
            )
        }
    }

    fun insertTransaction(transaction: TransactionEntity): Long {
        return dbHelper.insertTransaction(transaction)
    }

    fun deleteTransaction(transaction: TransactionEntity) {
        dbHelper.deleteTransaction(transaction)
    }

    fun insertLoan(loan: LoanEntity): Long {
        return dbHelper.insertLoan(loan)
    }

    fun recordRepayment(loan: LoanEntity, amount: Double, note: String, dateMillis: Long) {
        dbHelper.recordRepayment(loan, amount, note, dateMillis)
    }

    fun toggleLoanSettled(loan: LoanEntity) {
        dbHelper.toggleLoanSettled(loan)
    }

    fun deleteLoan(loan: LoanEntity) {
        dbHelper.deleteLoan(loan)
    }

    fun clearAllData() {
        dbHelper.clearAllData()
    }

    companion object {
        fun getCategoryIcon(category: String): String {
            return when (category.lowercase()) {
                "food", "খাবার" -> "🍔"
                "transport", "যাতায়াত" -> "🚗"
                "shopping", "বাজার-সদাই", "বাজার" -> "🛍️"
                "bills", "বিল ও ভাড়া", "বিল", "ভাড়া" -> "💡"
                "health", "চিকিৎসা" -> "💊"
                "education", "পড়াশোনা" -> "📚"
                "entertainment", "বিনোদন" -> "🎬"
                "salary", "বেতন" -> "💰"
                "business", "ব্যবসা" -> "📈"
                "gift", "উপহার" -> "🎁"
                else -> "✨"
            }
        }
    }
}
