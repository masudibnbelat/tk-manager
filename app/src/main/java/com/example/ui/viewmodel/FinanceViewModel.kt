package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.model.Categories
import com.example.data.model.CategoryEntity
import com.example.data.model.CurrencyInfo
import com.example.data.model.LoanEntity
import com.example.data.model.RepaymentEntity
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionType
import com.example.data.preferences.DataStoreManager
import com.example.data.repository.BalanceSummary
import com.example.data.repository.FinancialRepository
import com.example.util.ExcelExporter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class TransactionTab {
    ALL,
    INCOME,
    EXPENSE
}

enum class LoanTab {
    ACTIVE,
    COMPLETED
}

data class DailyTransactionGroup(
    val dateKey: String,
    val dateFormatted: String, // e.g. "17 Sep 2026"
    val dayOfWeek: String, // e.g. "Thursday"
    val isToday: Boolean,
    val totalAmount: Double,
    val totalIncome: Double,
    val totalExpense: Double,
    val transactions: List<TransactionEntity>
)

class FinanceViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val repository = FinancialRepository(db.transactionDao(), db.loanDao(), db.repaymentDao(), db.categoryDao())
    private val dataStore = DataStoreManager(application)

    // User Preferences
    val accountName: StateFlow<String> = dataStore.accountName.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = "My Account"
    )

    val currencyCode: StateFlow<String> = dataStore.currencyCode.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = "BDT"
    )

    val currencySymbol: StateFlow<String> = dataStore.currencySymbol.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = "৳"
    )

    val avatarSticker: StateFlow<String> = dataStore.avatarSticker.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = "💼"
    )

    val themeMode: StateFlow<String> = dataStore.themeMode.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = "SYSTEM"
    )

    val isOnboardingCompleted: StateFlow<Boolean> = dataStore.isOnboardingCompleted.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    // Check setup state: specifically accountName and currency must exist
    val hasCompletedSetup: StateFlow<Boolean?> = dataStore.hasCompletedSetup.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = null
    )

    val hasSeenTutorial: StateFlow<Boolean> = dataStore.hasSeenTutorial.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    // Calendar / Month Selection (year * 100 + month, e.g. 202609 for Sep 2026)
    private val _selectedMonth = MutableStateFlow<Int?>(run {
        val cal = Calendar.getInstance()
        cal.get(Calendar.YEAR) * 100 + (cal.get(Calendar.MONTH) + 1)
    })
    val selectedMonth: StateFlow<Int?> = _selectedMonth.asStateFlow()

    // Day of month filter (1..31, null = whole month)
    private val _selectedDay = MutableStateFlow<Int?>(null)
    val selectedDay: StateFlow<Int?> = _selectedDay.asStateFlow()

    // Lifetime Balance Summary (All-time records are NEVER reset)
    val lifetimeBalanceSummary: StateFlow<BalanceSummary> = repository.balanceSummary.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = BalanceSummary()
    )

    // Monthly Balance Summary (Calculated for currently selected month, or all-time if null)
    val monthlyBalanceSummary: StateFlow<BalanceSummary> = combine(
        repository.allTransactions,
        repository.allLoans,
        _selectedMonth
    ) { transactions, loans, month ->
        if (month == null) {
            // Return lifetime summary calculation
            var income = 0.0
            var expense = 0.0
            for (tx in transactions) {
                if (tx.type == TransactionType.INCOME.name) income += tx.amount else expense += tx.amount
            }
            var lentTotal = 0.0
            var borrowedTotal = 0.0
            var outLent = 0.0
            var outBorrowed = 0.0
            var activeCount = 0
            for (loan in loans) {
                val remaining = loan.remainingAmount
                if (loan.type == com.example.data.model.LoanType.LENT.name) {
                    lentTotal += loan.originalAmount
                    if (!loan.isCompleted && remaining > 0.0) { outLent += remaining; activeCount++ }
                } else {
                    borrowedTotal += loan.originalAmount
                    if (!loan.isCompleted && remaining > 0.0) { outBorrowed += remaining; activeCount++ }
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
        } else {
            val targetYear = month / 100
            val targetMonth = month % 100 // 1-based month
            val cal = Calendar.getInstance()

            var monthIncome = 0.0
            var monthExpense = 0.0
            for (tx in transactions) {
                cal.timeInMillis = tx.dateMillis
                if (cal.get(Calendar.YEAR) == targetYear && (cal.get(Calendar.MONTH) + 1) == targetMonth) {
                    if (tx.type == TransactionType.INCOME.name) monthIncome += tx.amount else monthExpense += tx.amount
                }
            }

            var lentTotal = 0.0
            var borrowedTotal = 0.0
            var outLent = 0.0
            var outBorrowed = 0.0
            var activeCount = 0
            for (loan in loans) {
                val remaining = loan.remainingAmount
                if (loan.type == com.example.data.model.LoanType.LENT.name) {
                    lentTotal += loan.originalAmount
                    if (!loan.isCompleted && remaining > 0.0) { outLent += remaining; activeCount++ }
                } else {
                    borrowedTotal += loan.originalAmount
                    if (!loan.isCompleted && remaining > 0.0) { outBorrowed += remaining; activeCount++ }
                }
            }

            BalanceSummary(
                currentBalance = monthIncome - monthExpense,
                totalIncome = monthIncome,
                totalExpenses = monthExpense,
                totalMoneyLent = lentTotal,
                totalMoneyBorrowed = borrowedTotal,
                outstandingLent = outLent,
                outstandingBorrowed = outBorrowed,
                totalOutstanding = outLent + outBorrowed,
                activeLoanCount = activeCount
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = BalanceSummary()
    )

    // Expose balanceSummary for backwards compatibility with existing screens
    val balanceSummary: StateFlow<BalanceSummary> = lifetimeBalanceSummary

    // Days in the selected month that contain at least one transaction
    val monthTransactionDays: StateFlow<Set<Int>> = combine(
        repository.allTransactions,
        _selectedMonth
    ) { transactions, monthYear ->
        val cal = Calendar.getInstance()
        val targetYear = (monthYear ?: (cal.get(Calendar.YEAR) * 100 + cal.get(Calendar.MONTH) + 1)) / 100
        val targetMonth = (monthYear ?: (cal.get(Calendar.YEAR) * 100 + cal.get(Calendar.MONTH) + 1)) % 100
        val days = mutableSetOf<Int>()
        for (tx in transactions) {
            cal.timeInMillis = tx.dateMillis
            if (cal.get(Calendar.YEAR) == targetYear && (cal.get(Calendar.MONTH) + 1) == targetMonth) {
                days.add(cal.get(Calendar.DAY_OF_MONTH))
            }
        }
        days
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptySet()
    )

    // Search and Tabs
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedTab = MutableStateFlow(TransactionTab.ALL)
    val selectedTab: StateFlow<TransactionTab> = _selectedTab.asStateFlow()

    private val _selectedLoanTab = MutableStateFlow(LoanTab.ACTIVE)
    val selectedLoanTab: StateFlow<LoanTab> = _selectedLoanTab.asStateFlow()

    // Daily Transaction Groups
    val dailyTransactionGroups: StateFlow<List<DailyTransactionGroup>> = combine(
        repository.allTransactions,
        _selectedMonth,
        _selectedDay,
        _searchQuery,
        _selectedTab
    ) { transactions, monthYear, dayFilter, query, tab ->
        val cal = Calendar.getInstance()
        val todayCal = Calendar.getInstance()
        val todayYear = todayCal.get(Calendar.YEAR)
        val todayMonth = todayCal.get(Calendar.MONTH) + 1
        val todayDay = todayCal.get(Calendar.DAY_OF_MONTH)

        val targetYear = (monthYear ?: (todayYear * 100 + todayMonth)) / 100
        val targetMonth = (monthYear ?: (todayYear * 100 + todayMonth)) % 100

        val cleanQuery = query.trim()
        val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        val dayOfWeekFormat = SimpleDateFormat("EEEE", Locale.getDefault())

        val filtered = transactions.filter { tx ->
            cal.timeInMillis = tx.dateMillis
            val txYear = cal.get(Calendar.YEAR)
            val txMonth = cal.get(Calendar.MONTH) + 1
            val txDay = cal.get(Calendar.DAY_OF_MONTH)

            val matchesMonth = (txYear == targetYear && txMonth == targetMonth)
            val matchesDay = (dayFilter == null || txDay == dayFilter)

            val matchesTab = when (tab) {
                TransactionTab.ALL -> true
                TransactionTab.INCOME -> tx.type == TransactionType.INCOME.name
                TransactionTab.EXPENSE -> tx.type == TransactionType.EXPENSE.name
            }

            // Search by title (per user prompt requirement)
            val matchesSearch = if (cleanQuery.isEmpty()) {
                true
            } else {
                tx.title.contains(cleanQuery, ignoreCase = true)
            }

            matchesMonth && matchesDay && matchesTab && matchesSearch
        }

        // Group by YYYY-MM-DD
        val groupedMap = linkedMapOf<String, MutableList<TransactionEntity>>()
        for (tx in filtered) {
            cal.timeInMillis = tx.dateMillis
            val y = cal.get(Calendar.YEAR)
            val m = cal.get(Calendar.MONTH) + 1
            val d = cal.get(Calendar.DAY_OF_MONTH)
            val dateKey = String.format(Locale.US, "%04d-%02d-%02d", y, m, d)
            val list = groupedMap.getOrPut(dateKey) { mutableListOf() }
            list.add(tx)
        }

        groupedMap.map { (key, txList) ->
            val firstTxMillis = txList.first().dateMillis
            cal.timeInMillis = firstTxMillis
            val y = cal.get(Calendar.YEAR)
            val m = cal.get(Calendar.MONTH) + 1
            val d = cal.get(Calendar.DAY_OF_MONTH)
            val isToday = (y == todayYear && m == todayMonth && d == todayDay)

            var income = 0.0
            var expense = 0.0
            for (tx in txList) {
                if (tx.type == TransactionType.INCOME.name) income += tx.amount else expense += tx.amount
            }

            val totalForTab = when (tab) {
                TransactionTab.ALL -> income - expense
                TransactionTab.INCOME -> income
                TransactionTab.EXPENSE -> expense
            }

            DailyTransactionGroup(
                dateKey = key,
                dateFormatted = dateFormat.format(Date(firstTxMillis)),
                dayOfWeek = dayOfWeekFormat.format(Date(firstTxMillis)),
                isToday = isToday,
                totalAmount = totalForTab,
                totalIncome = income,
                totalExpense = expense,
                transactions = txList
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Flat filtered transactions list for backwards compatibility
    val filteredTransactions: StateFlow<List<TransactionEntity>> = dailyTransactionGroups.map { groups ->
        groups.flatMap { it.transactions }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Loans
    val activeLoans: StateFlow<List<LoanEntity>> = repository.activeLoans.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val completedLoans: StateFlow<List<LoanEntity>> = repository.completedLoans.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val allRepayments: StateFlow<List<RepaymentEntity>> = repository.allRepayments.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Categories
    val customCategories: StateFlow<List<CategoryEntity>> = repository.allCategories.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val incomeCategories: StateFlow<List<String>> = repository.allCategories.map { customList ->
        val custom = customList.filter { it.type == "INCOME" || it.type == "BOTH" }.map { it.name }
        (Categories.INCOME_CATEGORIES + custom).distinct()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = Categories.INCOME_CATEGORIES
    )

    val expenseCategories: StateFlow<List<String>> = repository.allCategories.map { customList ->
        val custom = customList.filter { it.type == "EXPENSE" || it.type == "BOTH" }.map { it.name }
        (Categories.EXPENSE_CATEGORIES + custom).distinct()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = Categories.EXPENSE_CATEGORIES
    )

    // UI Feedback Message

    private val _uiMessage = MutableStateFlow<String?>(null)
    val uiMessage: StateFlow<String?> = _uiMessage.asStateFlow()

    fun clearUiMessage() {
        _uiMessage.value = null
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun clearSearchQuery() {
        _searchQuery.value = ""
    }

    fun setSelectedTab(tab: TransactionTab) {
        _selectedTab.value = tab
    }

    fun setSelectedLoanTab(tab: LoanTab) {
        _selectedLoanTab.value = tab
    }

    fun previousMonth() {
        val current = _selectedMonth.value ?: run {
            val cal = Calendar.getInstance()
            cal.get(Calendar.YEAR) * 100 + (cal.get(Calendar.MONTH) + 1)
        }
        var year = current / 100
        var month = current % 100
        month -= 1
        if (month < 1) {
            month = 12
            year -= 1
        }
        _selectedMonth.value = year * 100 + month
        _selectedDay.value = null
    }

    fun nextMonth() {
        val current = _selectedMonth.value ?: run {
            val cal = Calendar.getInstance()
            cal.get(Calendar.YEAR) * 100 + (cal.get(Calendar.MONTH) + 1)
        }
        var year = current / 100
        var month = current % 100
        month += 1
        if (month > 12) {
            month = 1
            year += 1
        }
        _selectedMonth.value = year * 100 + month
        _selectedDay.value = null
    }

    fun selectDay(day: Int) {
        if (_selectedDay.value == day) {
            _selectedDay.value = null
        } else {
            _selectedDay.value = day
        }
    }

    fun selectYearMonthDay(year: Int, month: Int, day: Int?) {
        _selectedMonth.value = year * 100 + month
        _selectedDay.value = day
    }

    fun jumpToToday() {
        val cal = Calendar.getInstance()
        val y = cal.get(Calendar.YEAR)
        val m = cal.get(Calendar.MONTH) + 1
        val d = cal.get(Calendar.DAY_OF_MONTH)
        _selectedMonth.value = y * 100 + m
        _selectedDay.value = d
    }

    fun clearSelectedDay() {
        _selectedDay.value = null
    }

    fun resetCurrentMonth() {
        viewModelScope.launch {
            val current = _selectedMonth.value ?: return@launch
            val year = current / 100
            val month0 = (current % 100) - 1
            val cal = Calendar.getInstance()
            cal.set(year, month0, 1, 0, 0, 0)
            cal.set(Calendar.MILLISECOND, 0)
            val startMillis = cal.timeInMillis
            cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
            cal.set(Calendar.HOUR_OF_DAY, 23)
            cal.set(Calendar.MINUTE, 59)
            cal.set(Calendar.SECOND, 59)
            cal.set(Calendar.MILLISECOND, 999)
            val endMillis = cal.timeInMillis
            repository.deleteTransactionsInRange(startMillis, endMillis)
            _uiMessage.value = "Selected month transactions have been reset"
        }
    }

    // Onboarding
    fun completeOnboarding(name: String, currency: CurrencyInfo, avatar: String) {
        viewModelScope.launch {
            dataStore.saveAccountName(name)
            dataStore.saveCurrency(currency.code, currency.symbol)
            dataStore.saveAvatarSticker(avatar)
            dataStore.setOnboardingCompleted(true)
        }
    }

    fun setTutorialSeen(seen: Boolean) {
        viewModelScope.launch {
            dataStore.setTutorialSeen(seen)
        }
    }

    // Settings
    fun updateAccountName(name: String) {
        viewModelScope.launch {
            dataStore.saveAccountName(name)
            _uiMessage.value = "Account name updated"
        }
    }

    fun updateCurrency(currency: CurrencyInfo) {
        viewModelScope.launch {
            dataStore.saveCurrency(currency.code, currency.symbol)
            _uiMessage.value = "Currency changed to ${currency.code} (${currency.symbol})"
        }
    }

    fun updateAvatar(avatar: String) {
        viewModelScope.launch {
            dataStore.saveAvatarSticker(avatar)
            _uiMessage.value = "Avatar updated"
        }
    }

    fun updateThemeMode(mode: String) {
        viewModelScope.launch {
            dataStore.saveThemeMode(mode)
        }
    }

    // Transactions
    fun addTransaction(
        type: TransactionType,
        title: String,
        amount: Double,
        category: String,
        dateMillis: Long,
        note: String?
    ) {
        viewModelScope.launch {
            val tx = TransactionEntity(
                type = type.name,
                title = title.trim(),
                amount = amount,
                category = category.trim(),
                dateMillis = dateMillis,
                note = note?.trim()?.ifEmpty { null }
            )
            repository.insertTransaction(tx)
            _uiMessage.value = "${if (type == TransactionType.INCOME) "Income" else "Expense"} added"
        }
    }

    fun updateTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            repository.updateTransaction(transaction)
            _uiMessage.value = "Transaction updated"
        }
    }

    fun deleteTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            repository.deleteTransaction(transaction)
            _uiMessage.value = "Transaction deleted"
        }
    }

    // Loans
    fun addLoan(
        name: String,
        type: String, // "BORROWED" or "LENT"
        amount: Double,
        dateMillis: Long,
        dueDateMillis: Long,
        note: String?
    ) {
        viewModelScope.launch {
            val loan = LoanEntity(
                name = name.trim(),
                type = type,
                originalAmount = amount,
                repaidAmount = 0.0,
                dateMillis = dateMillis,
                dueDateMillis = dueDateMillis,
                note = note?.trim()?.ifEmpty { null },
                isCompleted = false
            )
            repository.insertLoan(loan)
            _uiMessage.value = "Loan record created"
        }
    }

    fun updateLoan(loan: LoanEntity) {
        viewModelScope.launch {
            repository.updateLoan(loan)
            _uiMessage.value = "Loan updated"
        }
    }

    fun deleteLoan(loan: LoanEntity) {
        viewModelScope.launch {
            repository.deleteLoan(loan)
            _uiMessage.value = "Loan deleted"
        }
    }

    fun recordRepayment(loanId: Long, amount: Double, dateMillis: Long, note: String?) {
        viewModelScope.launch {
            val result = repository.recordRepayment(loanId, amount, dateMillis, note)
            if (result.isSuccess) {
                _uiMessage.value = "Repayment of $amount recorded successfully"
            } else {
                _uiMessage.value = result.exceptionOrNull()?.message ?: "Repayment failed"
            }
        }
    }

    // Export to Excel
    fun exportToExcel(outputStream: OutputStream): Boolean {
        return try {
            val transactions = repository.allTransactions
            val loans = repository.allLoans
            val repayments = repository.allRepayments

            // Collect current snapshot synchronously or in coroutine
            var txList: List<TransactionEntity> = emptyList()
            var loanList: List<LoanEntity> = emptyList()
            var repList: List<RepaymentEntity> = emptyList()

            kotlinx.coroutines.runBlocking {
                txList = transactions.first()
                loanList = loans.first()
                repList = repayments.first()
            }

            ExcelExporter.exportToStream(outputStream, txList, loanList, repList)
            _uiMessage.value = "Backup file exported successfully (.xlsx)"
            true
        } catch (e: Exception) {
            e.printStackTrace()
            _uiMessage.value = "Failed to export: ${e.localizedMessage}"
            false
        }
    }

    // Category Management
    fun addCategory(name: String, type: String, iconKey: String = "category") {
        viewModelScope.launch {
            val category = CategoryEntity(
                name = name.trim(),
                type = type,
                iconKey = iconKey,
                isCustom = true
            )
            repository.insertCategory(category)
            _uiMessage.value = "Category '$name' created"
        }
    }

    fun updateCategory(category: CategoryEntity) {
        viewModelScope.launch {
            repository.updateCategory(category)
            _uiMessage.value = "Category updated"
        }
    }

    fun deleteCategory(category: CategoryEntity) {
        viewModelScope.launch {
            repository.deleteCategory(category)
            _uiMessage.value = "Category deleted"
        }
    }

    fun resetAllData() {
        viewModelScope.launch {
            repository.clearAllData()
            dataStore.resetAll()
            _uiMessage.value = "All data has been reset"
        }
    }
}

