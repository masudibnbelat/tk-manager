package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY dateMillis DESC, id DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE yearMonth = :yearMonth ORDER BY dateMillis DESC, id DESC")
    fun getTransactionsByMonth(yearMonth: Int): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE yearMonth = :yearMonth AND day = :day ORDER BY dateMillis DESC, id DESC")
    fun getTransactionsByDay(yearMonth: Int, day: Int): Flow<List<TransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(item: TransactionEntity): Long

    @Update
    suspend fun updateTransaction(item: TransactionEntity)

    @Delete
    suspend fun deleteTransaction(item: TransactionEntity)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteTransactionById(id: Long)

    @Query("DELETE FROM transactions")
    suspend fun deleteAllTransactions()
}
