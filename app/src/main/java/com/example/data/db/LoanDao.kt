package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.LoanEntity
import com.example.data.model.RepaymentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LoanDao {
    @Query("SELECT * FROM loans ORDER BY isSettled ASC, dateMillis DESC")
    fun getAllLoans(): Flow<List<LoanEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLoan(loan: LoanEntity): Long

    @Update
    suspend fun updateLoan(loan: LoanEntity)

    @Delete
    suspend fun deleteLoan(loan: LoanEntity)

    @Query("SELECT * FROM repayments WHERE loanId = :loanId ORDER BY dateMillis DESC")
    fun getRepaymentsForLoan(loanId: Long): Flow<List<RepaymentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRepayment(repayment: RepaymentEntity): Long

    @Query("DELETE FROM repayments WHERE loanId = :loanId")
    suspend fun deleteRepaymentsForLoan(loanId: Long)

    @Query("DELETE FROM loans")
    suspend fun deleteAllLoans()
}
