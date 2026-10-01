package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.DatabaseHelper
import com.example.data.model.LoanEntity
import com.example.data.model.TransactionEntity
import com.example.data.preferences.PreferenceManager
import com.example.data.repository.BalanceSummary
import com.example.data.repository.CategoryBreakdown
import com.example.data.repository.FinanceRepository
import com.example.data.repository.LoanSummary
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

class FinanceViewModel(application: Application) : AndroidViewModel(application) {
    private val db = DatabaseHelper.getInstance(application)
    private val repository = FinanceRepository(db)
    val preferenceManager = PreferenceManager(application)

    val hasCompletedSetup: StateFlow<Boolean> = preferenceManager.hasCompletedSetup
    val accountName: StateFlow<String> = preferenceManager.accountName
    val currencyCode: StateFlow<String> = preferenceManager.currencyCode
    val currencySymbol: StateFlow<String> = preferenceManager.currencySymbol
    val avatar: StateFlow<String> = preferenceManager.avatar
    val appLanguage: StateFlow<String> = preferenceManager.appLanguage
    val themeMode: StateFlow<String> = preferenceManager.themeMode

    private val calendar = Calendar.getInstance()
    private val initialYearMonth = calendar.get(Calendar.YEAR) * 100 + (calendar.get(Calendar.MONTH) + 1)

    private val _selectedYearMonth = MutableStateFlow(initialYearMonth)
    val selectedYearMonth: StateFlow<Int> = _selectedYearMonth.asStateFlow()

    private val _selectedDay = MutableStateFlow<Int?>(null)
    val selectedDay: StateFlow<Int?> = _selectedDay.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val lifetimeSummary: StateFlow<BalanceSummary> = repository.getLifetimeBalanceSummary()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), BalanceSummary())

    val monthlySummary: StateFlow<BalanceSummary> = _selectedYearMonth.flatMapLatest { ym ->
        repository.getMonthlyBalanceSummary(ym)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), BalanceSummary())

    val categoryBreakdown: StateFlow<List<CategoryBreakdown>> = _selectedYearMonth.flatMapLatest { ym ->
        repository.getCategoryBreakdown(ym)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val transactions: StateFlow<List<TransactionEntity>> = combine(
        _selectedYearMonth.flatMapLatest { repository.getTransactionsByMonth(it) },
        _selectedDay,
        _searchQuery
    ) { list, day, query ->
        list.filter { item ->
            val matchDay = day == null || item.day == day
            val matchQuery = query.isBlank() || item.title.contains(query, ignoreCase = true) || item.category.contains(query, ignoreCase = true)
            matchDay && matchQuery
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val monthTransactionDays: StateFlow<Set<Int>> = _selectedYearMonth.flatMapLatest { ym ->
        repository.getTransactionsByMonth(ym)
    }.combine(_selectedYearMonth) { list, _ ->
        list.map { it.day }.toSet()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    val allLoans: StateFlow<List<LoanEntity>> = repository.allLoans
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val loanSummary: StateFlow<LoanSummary> = repository.getLoanSummary()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), LoanSummary())

    fun previousMonth() {
        val current = _selectedYearMonth.value
        val year = current / 100
        val month = current % 100
        val newYm = if (month == 1) (year - 1) * 100 + 12 else year * 100 + (month - 1)
        _selectedYearMonth.value = newYm
        _selectedDay.value = null
    }

    fun nextMonth() {
        val current = _selectedYearMonth.value
        val year = current / 100
        val month = current % 100
        val newYm = if (month == 12) (year + 1) * 100 + 1 else year * 100 + (month + 1)
        _selectedYearMonth.value = newYm
        _selectedDay.value = null
    }

    fun selectDay(day: Int?) {
        _selectedDay.value = if (_selectedDay.value == day) null else day
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun addTransaction(
        type: String,
        title: String,
        amount: Double,
        category: String,
        dateMillis: Long,
        note: String = ""
    ) {
        viewModelScope.launch {
            val cal = Calendar.getInstance().apply { timeInMillis = dateMillis }
            val ym = cal.get(Calendar.YEAR) * 100 + (cal.get(Calendar.MONTH) + 1)
            val d = cal.get(Calendar.DAY_OF_MONTH)
            val entity = TransactionEntity(
                type = type,
                title = title.ifBlank { category },
                amount = amount,
                category = category,
                dateMillis = dateMillis,
                note = note,
                yearMonth = ym,
                day = d
            )
            repository.insertTransaction(entity)
        }
    }

    fun deleteTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            repository.deleteTransaction(transaction)
        }
    }

    fun addLoan(
        personName: String,
        type: String,
        totalAmount: Double,
        note: String,
        dueDateMillis: Long?
    ) {
        viewModelScope.launch {
            val loan = LoanEntity(
                personName = personName,
                type = type,
                totalAmount = totalAmount,
                paidAmount = 0.0,
                note = note,
                dateMillis = System.currentTimeMillis(),
                dueDateMillis = dueDateMillis,
                isSettled = false
            )
            repository.insertLoan(loan)
        }
    }

    fun recordRepayment(loan: LoanEntity, amount: Double, note: String) {
        viewModelScope.launch {
            repository.recordRepayment(
                loan = loan,
                amount = amount,
                note = note,
                dateMillis = System.currentTimeMillis()
            )
        }
    }

    fun toggleLoanSettled(loan: LoanEntity) {
        viewModelScope.launch {
            repository.toggleLoanSettled(loan)
        }
    }

    fun deleteLoan(loan: LoanEntity) {
        viewModelScope.launch {
            repository.deleteLoan(loan)
        }
    }

    fun completeOnboarding(name: String, code: String, symbol: String, avatar: String) {
        preferenceManager.completeOnboarding(name, code, symbol, avatar)
    }

    fun updateProfile(name: String, avatar: String) {
        preferenceManager.updateAccountName(name)
        preferenceManager.updateAvatar(avatar)
    }

    fun updateCurrency(code: String, symbol: String) {
        preferenceManager.updateCurrency(code, symbol)
    }

    fun setLanguage(lang: String) {
        preferenceManager.updateLanguage(lang)
    }

    fun setThemeMode(mode: String) {
        preferenceManager.updateThemeMode(mode)
    }

    fun clearAllData() {
        viewModelScope.launch {
            repository.clearAllData()
            preferenceManager.resetAll()
        }
    }
}
