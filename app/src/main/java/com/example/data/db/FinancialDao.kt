package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.LoanEntity
import com.example.data.model.RepaymentEntity
import com.example.data.model.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY dateMillis DESC, id DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE type = :type ORDER BY dateMillis DESC, id DESC")
    fun getTransactionsByType(type: String): Flow<List<TransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaction: TransactionEntity): Long

    @Update
    suspend fun update(transaction: TransactionEntity)

    @Delete
    suspend fun delete(transaction: TransactionEntity)

    @Query("SELECT * FROM transactions WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): TransactionEntity?

    @Query("DELETE FROM transactions WHERE dateMillis >= :startMillis AND dateMillis <= :endMillis")
    suspend fun deleteInRange(startMillis: Long, endMillis: Long)

    @Query("DELETE FROM transactions")
    suspend fun deleteAll()
}

@Dao
interface LoanDao {
    @Query("SELECT * FROM loans ORDER BY isCompleted ASC, dueDateMillis ASC, id DESC")
    fun getAllLoans(): Flow<List<LoanEntity>>

    @Query("SELECT * FROM loans WHERE isCompleted = 0 ORDER BY dueDateMillis ASC, id DESC")
    fun getActiveLoans(): Flow<List<LoanEntity>>

    @Query("SELECT * FROM loans WHERE isCompleted = 1 ORDER BY dateMillis DESC, id DESC")
    fun getCompletedLoans(): Flow<List<LoanEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(loan: LoanEntity): Long

    @Update
    suspend fun update(loan: LoanEntity)

    @Delete
    suspend fun delete(loan: LoanEntity)

    @Query("SELECT * FROM loans WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): LoanEntity?

    @Query("DELETE FROM loans")
    suspend fun deleteAll()
}

@Dao
interface RepaymentDao {
    @Query("SELECT * FROM repayments WHERE loanId = :loanId ORDER BY dateMillis DESC, id DESC")
    fun getRepaymentsForLoan(loanId: Long): Flow<List<RepaymentEntity>>

    @Query("SELECT * FROM repayments ORDER BY dateMillis DESC, id DESC")
    fun getAllRepayments(): Flow<List<RepaymentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(repayment: RepaymentEntity): Long

    @Query("DELETE FROM repayments WHERE loanId = :loanId")
    suspend fun deleteByLoanId(loanId: Long)

    @Query("DELETE FROM repayments")
    suspend fun deleteAll()
}

@Dao
interface CategoryDao {
    @Query("SELECT * FROM custom_categories ORDER BY id ASC")
    fun getAllCategories(): Flow<List<com.example.data.model.CategoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(category: com.example.data.model.CategoryEntity): Long

    @Update
    suspend fun update(category: com.example.data.model.CategoryEntity)

    @Delete
    suspend fun delete(category: com.example.data.model.CategoryEntity)

    @Query("DELETE FROM custom_categories")
    suspend fun deleteAll()
}

