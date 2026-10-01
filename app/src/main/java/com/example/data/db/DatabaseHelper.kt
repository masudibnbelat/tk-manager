package com.example.data.db

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.data.model.LoanEntity
import com.example.data.model.RepaymentEntity
import com.example.data.model.TransactionEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    private val dbScope = CoroutineScope(Dispatchers.IO)

    private val _allTransactions = MutableStateFlow<List<TransactionEntity>>(emptyList())
    val allTransactions: StateFlow<List<TransactionEntity>> = _allTransactions.asStateFlow()

    private val _allLoans = MutableStateFlow<List<LoanEntity>>(emptyList())
    val allLoans: StateFlow<List<LoanEntity>> = _allLoans.asStateFlow()

    init {
        refreshAll()
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE transactions (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                type TEXT NOT NULL,
                title TEXT NOT NULL,
                amount REAL NOT NULL,
                category TEXT NOT NULL,
                dateMillis INTEGER NOT NULL,
                note TEXT NOT NULL,
                yearMonth INTEGER NOT NULL,
                day INTEGER NOT NULL
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE loans (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                personName TEXT NOT NULL,
                type TEXT NOT NULL,
                totalAmount REAL NOT NULL,
                paidAmount REAL NOT NULL DEFAULT 0.0,
                note TEXT NOT NULL,
                dateMillis INTEGER NOT NULL,
                dueDateMillis INTEGER,
                isSettled INTEGER NOT NULL DEFAULT 0
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE repayments (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                loanId INTEGER NOT NULL,
                amount REAL NOT NULL,
                dateMillis INTEGER NOT NULL,
                note TEXT NOT NULL,
                FOREIGN KEY (loanId) REFERENCES loans(id) ON DELETE CASCADE
            )
            """.trimIndent()
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS repayments")
        db.execSQL("DROP TABLE IF EXISTS loans")
        db.execSQL("DROP TABLE IF EXISTS transactions")
        onCreate(db)
    }

    fun refreshAll() {
        dbScope.launch {
            _allTransactions.value = queryAllTransactions()
            _allLoans.value = queryAllLoans()
        }
    }

    private fun queryAllTransactions(): List<TransactionEntity> {
        val list = mutableListOf<TransactionEntity>()
        val db = readableDatabase
        val cursor: Cursor = db.rawQuery("SELECT * FROM transactions ORDER BY dateMillis DESC, id DESC", null)
        cursor.use { c ->
            val idIdx = c.getColumnIndexOrThrow("id")
            val typeIdx = c.getColumnIndexOrThrow("type")
            val titleIdx = c.getColumnIndexOrThrow("title")
            val amountIdx = c.getColumnIndexOrThrow("amount")
            val catIdx = c.getColumnIndexOrThrow("category")
            val dateIdx = c.getColumnIndexOrThrow("dateMillis")
            val noteIdx = c.getColumnIndexOrThrow("note")
            val ymIdx = c.getColumnIndexOrThrow("yearMonth")
            val dayIdx = c.getColumnIndexOrThrow("day")

            while (c.moveToNext()) {
                list.add(
                    TransactionEntity(
                        id = c.getLong(idIdx),
                        type = c.getString(typeIdx),
                        title = c.getString(titleIdx),
                        amount = c.getDouble(amountIdx),
                        category = c.getString(catIdx),
                        dateMillis = c.getLong(dateIdx),
                        note = c.getString(noteIdx),
                        yearMonth = c.getInt(ymIdx),
                        day = c.getInt(dayIdx)
                    )
                )
            }
        }
        return list
    }

    private fun queryAllLoans(): List<LoanEntity> {
        val list = mutableListOf<LoanEntity>()
        val db = readableDatabase
        val cursor: Cursor = db.rawQuery("SELECT * FROM loans ORDER BY isSettled ASC, dateMillis DESC", null)
        cursor.use { c ->
            val idIdx = c.getColumnIndexOrThrow("id")
            val nameIdx = c.getColumnIndexOrThrow("personName")
            val typeIdx = c.getColumnIndexOrThrow("type")
            val totalIdx = c.getColumnIndexOrThrow("totalAmount")
            val paidIdx = c.getColumnIndexOrThrow("paidAmount")
            val noteIdx = c.getColumnIndexOrThrow("note")
            val dateIdx = c.getColumnIndexOrThrow("dateMillis")
            val dueIdx = c.getColumnIndexOrThrow("dueDateMillis")
            val settledIdx = c.getColumnIndexOrThrow("isSettled")

            while (c.moveToNext()) {
                list.add(
                    LoanEntity(
                        id = c.getLong(idIdx),
                        personName = c.getString(nameIdx),
                        type = c.getString(typeIdx),
                        totalAmount = c.getDouble(totalIdx),
                        paidAmount = c.getDouble(paidIdx),
                        note = c.getString(noteIdx),
                        dateMillis = c.getLong(dateIdx),
                        dueDateMillis = if (c.isNull(dueIdx)) null else c.getLong(dueIdx),
                        isSettled = c.getInt(settledIdx) == 1
                    )
                )
            }
        }
        return list
    }

    fun insertTransaction(item: TransactionEntity): Long {
        val db = writableDatabase
        val cv = ContentValues().apply {
            put("type", item.type)
            put("title", item.title)
            put("amount", item.amount)
            put("category", item.category)
            put("dateMillis", item.dateMillis)
            put("note", item.note)
            put("yearMonth", item.yearMonth)
            put("day", item.day)
        }
        val id = db.insert("transactions", null, cv)
        refreshAll()
        return id
    }

    fun deleteTransaction(item: TransactionEntity) {
        val db = writableDatabase
        db.delete("transactions", "id = ?", arrayOf(item.id.toString()))
        refreshAll()
    }

    fun insertLoan(loan: LoanEntity): Long {
        val db = writableDatabase
        val cv = ContentValues().apply {
            put("personName", loan.personName)
            put("type", loan.type)
            put("totalAmount", loan.totalAmount)
            put("paidAmount", loan.paidAmount)
            put("note", loan.note)
            put("dateMillis", loan.dateMillis)
            if (loan.dueDateMillis != null) {
                put("dueDateMillis", loan.dueDateMillis)
            } else {
                putNull("dueDateMillis")
            }
            put("isSettled", if (loan.isSettled) 1 else 0)
        }
        val id = db.insert("loans", null, cv)
        refreshAll()
        return id
    }

    fun recordRepayment(loan: LoanEntity, amount: Double, note: String, dateMillis: Long) {
        val db = writableDatabase
        val newPaid = loan.paidAmount + amount
        val isSettled = newPaid >= loan.totalAmount
        val cv = ContentValues().apply {
            put("paidAmount", newPaid)
            put("isSettled", if (isSettled) 1 else 0)
        }
        db.update("loans", cv, "id = ?", arrayOf(loan.id.toString()))

        val rCv = ContentValues().apply {
            put("loanId", loan.id)
            put("amount", amount)
            put("dateMillis", dateMillis)
            put("note", note)
        }
        db.insert("repayments", null, rCv)
        refreshAll()
    }

    fun toggleLoanSettled(loan: LoanEntity) {
        val db = writableDatabase
        val newSettled = !loan.isSettled
        val newPaid = if (newSettled) loan.totalAmount else loan.paidAmount
        val cv = ContentValues().apply {
            put("isSettled", if (newSettled) 1 else 0)
            put("paidAmount", newPaid)
        }
        db.update("loans", cv, "id = ?", arrayOf(loan.id.toString()))
        refreshAll()
    }

    fun deleteLoan(loan: LoanEntity) {
        val db = writableDatabase
        db.delete("repayments", "loanId = ?", arrayOf(loan.id.toString()))
        db.delete("loans", "id = ?", arrayOf(loan.id.toString()))
        refreshAll()
    }

    fun clearAllData() {
        val db = writableDatabase
        db.delete("repayments", null, null)
        db.delete("loans", null, null)
        db.delete("transactions", null, null)
        refreshAll()
    }

    companion object {
        private const val DATABASE_NAME = "tk_koi_gelo.db"
        private const val DATABASE_VERSION = 1

        @Volatile
        private var INSTANCE: DatabaseHelper? = null

        fun getInstance(context: Context): DatabaseHelper {
            return INSTANCE ?: synchronized(this) {
                val instance = DatabaseHelper(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }
}
