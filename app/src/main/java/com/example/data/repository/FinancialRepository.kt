package com.example.data.repository

import com.example.data.db.CategoryDao
import com.example.data.db.LoanDao
import com.example.data.db.RepaymentDao
import com.example.data.db.TransactionDao
import com.example.data.model.CategoryEntity
import com.example.data.model.LoanEntity
import com.example.data.model.LoanType
import com.example.data.model.RepaymentEntity
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

data class BalanceSummary(
    val currentBalance: Double = 0.0,
    val totalIncome: Double = 0.0,
    val totalExpenses: Double = 0.0,
    val totalMoneyLent: Double = 0.0,
    val totalMoneyBorrowed: Double = 0.0,
    val outstandingLent: Double = 0.0,
    val outstandingBorrowed: Double = 0.0,
    val totalOutstanding: Double = 0.0,
    val activeLoanCount: Int = 0
)

class FinancialRepository(
    private val transactionDao: TransactionDao,
    private val loanDao: LoanDao,
    private val repaymentDao: RepaymentDao,
    private val categoryDao: CategoryDao
) {
    val allTransactions: Flow<List<TransactionEntity>> = transactionDao.getAllTransactions()
    val allLoans: Flow<List<LoanEntity>> = loanDao.getAllLoans()
    val activeLoans: Flow<List<LoanEntity>> = loanDao.getActiveLoans()
    val completedLoans: Flow<List<LoanEntity>> = loanDao.getCompletedLoans()
    val allRepayments: Flow<List<RepaymentEntity>> = repaymentDao.getAllRepayments()
    val allCategories: Flow<List<CategoryEntity>> = categoryDao.getAllCategories()

    val balanceSummary: Flow<BalanceSummary> = combine(
        allTransactions,
        allLoans
    ) { transactions, loans ->
        var income = 0.0
        var expense = 0.0
        for (tx in transactions) {
            if (tx.type == TransactionType.INCOME.name) {
                income += tx.amount
            } else {
                expense += tx.amount
            }
        }

        var lentTotal = 0.0
        var borrowedTotal = 0.0
        var outLent = 0.0
        var outBorrowed = 0.0
        var activeCount = 0

        for (loan in loans) {
            val remaining = loan.remainingAmount
            if (loan.type == LoanType.LENT.name) {
                lentTotal += loan.originalAmount
                if (!loan.isCompleted && remaining > 0.0) {
                    outLent += remaining
                    activeCount++
                }
            } else {
                borrowedTotal += loan.originalAmount
                if (!loan.isCompleted && remaining > 0.0) {
                    outBorrowed += remaining
                    activeCount++
                }
            }
        }

        BalanceSummary(
            currentBalance = income - expense,
            totalIncome = income,
            totalExpenses = expense,
            totalMoneyLent = lentTotal,
            totalMoneyBorrowed = borrowedTotal,
            outstandingLent = outLent,
            outstandingBorrowed = outBorrowed,
            totalOutstanding = outLent + outBorrowed,
            activeLoanCount = activeCount
        )
    }

    suspend fun insertTransaction(transaction: TransactionEntity): Long {
        return transactionDao.insert(transaction)
    }

    suspend fun updateTransaction(transaction: TransactionEntity) {
        transactionDao.update(transaction)
    }

    suspend fun deleteTransaction(transaction: TransactionEntity) {
        transactionDao.delete(transaction)
    }

    suspend fun deleteTransactionsInRange(startMillis: Long, endMillis: Long) {
        transactionDao.deleteInRange(startMillis, endMillis)
    }

    suspend fun insertLoan(loan: LoanEntity): Long {
        return loanDao.insert(loan)
    }

    suspend fun updateLoan(loan: LoanEntity) {
        loanDao.update(loan)
    }

    suspend fun deleteLoan(loan: LoanEntity) {
        loanDao.delete(loan)
        repaymentDao.deleteByLoanId(loan.id)
    }

    /**
     * Records a repayment against a loan.
     * Prevents repayment greater than remaining amount.
     * Marks completed when remaining balance reaches zero.
     * Does NOT count loan repayments as ordinary income/expense.
     */
    suspend fun recordRepayment(
        loanId: Long,
        repaymentAmount: Double,
        dateMillis: Long,
        note: String? = null
    ): Result<Unit> {
        val loan = loanDao.getById(loanId) ?: return Result.failure(IllegalArgumentException("Loan not found"))
        val remaining = loan.remainingAmount

        if (repaymentAmount <= 0.0) {
            return Result.failure(IllegalArgumentException("Repayment amount must be greater than zero"))
        }

        if (repaymentAmount > remaining + 0.0001) {
            return Result.failure(IllegalArgumentException("Repayment cannot exceed remaining amount"))
        }

        val newRepaid = loan.repaidAmount + repaymentAmount
        val isCompleted = (loan.originalAmount - newRepaid) <= 0.001

        val updatedLoan = loan.copy(
            repaidAmount = newRepaid,
            isCompleted = isCompleted
        )

        loanDao.update(updatedLoan)
        repaymentDao.insert(
            RepaymentEntity(
                loanId = loan.id,
                loanName = loan.name,
                amount = repaymentAmount,
                dateMillis = dateMillis,
                note = note
            )
        )

        return Result.success(Unit)
    }

    fun getRepaymentsForLoan(loanId: Long): Flow<List<RepaymentEntity>> {
        return repaymentDao.getRepaymentsForLoan(loanId)
    }

    suspend fun insertCategory(category: CategoryEntity): Long {
        return categoryDao.insert(category)
    }

    suspend fun updateCategory(category: CategoryEntity) {
        categoryDao.update(category)
    }

    suspend fun deleteCategory(category: CategoryEntity) {
        categoryDao.delete(category)
    }

    suspend fun clearAllData() {
        transactionDao.deleteAll()
        loanDao.deleteAll()
        repaymentDao.deleteAll()
        categoryDao.deleteAll()
    }
}

