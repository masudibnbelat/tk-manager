package com.example.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Handshake
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ui.screens.home.HomeScreen
import com.example.ui.screens.loan.LoanScreen
import com.example.ui.screens.settings.SettingsScreen
import com.example.ui.viewmodel.FinanceViewModel

enum class NavigationDestination(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
) {
    HOME(
        title = "Trans.",
        selectedIcon = Icons.Filled.ReceiptLong,
        unselectedIcon = Icons.Outlined.ReceiptLong,
        testTag = "nav_home"
    ),
    LOAN(
        title = "Loans",
        selectedIcon = Icons.Filled.Handshake,
        unselectedIcon = Icons.Outlined.Handshake,
        testTag = "nav_loan"
    ),
    SETTINGS(
        title = "Settings",
        selectedIcon = Icons.Filled.Settings,
        unselectedIcon = Icons.Outlined.Settings,
        testTag = "nav_settings"
    )
}

@Composable
fun MainAppScreen(viewModel: FinanceViewModel) {
    var currentDestination by remember { mutableStateOf(NavigationDestination.HOME) }
    val snackbarHostState = remember { SnackbarHostState() }

    val accountName by viewModel.accountName.collectAsState()
    val avatarSticker by viewModel.avatarSticker.collectAsState()
    val currencyCode by viewModel.currencyCode.collectAsState()
    val currencySymbol by viewModel.currencySymbol.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()

    val balanceSummary by viewModel.balanceSummary.collectAsState()
    val monthlyBalanceSummary by viewModel.monthlyBalanceSummary.collectAsState()
    val selectedMonth by viewModel.selectedMonth.collectAsState()
    val selectedDay by viewModel.selectedDay.collectAsState()
    val monthTransactionDays by viewModel.monthTransactionDays.collectAsState()
    val dailyGroups by viewModel.dailyTransactionGroups.collectAsState()
    val transactions by viewModel.filteredTransactions.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedTransactionTab by viewModel.selectedTab.collectAsState()

    val activeLoans by viewModel.activeLoans.collectAsState()
    val completedLoans by viewModel.completedLoans.collectAsState()
    val allRepayments by viewModel.allRepayments.collectAsState()
    val selectedLoanTab by viewModel.selectedLoanTab.collectAsState()

    val customCategories by viewModel.customCategories.collectAsState()
    val incomeCategories by viewModel.incomeCategories.collectAsState()
    val expenseCategories by viewModel.expenseCategories.collectAsState()

    val uiMessage by viewModel.uiMessage.collectAsState()

    LaunchedEffect(uiMessage) {
        uiMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearUiMessage()
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            NavigationBar(
                modifier = Modifier.testTag("main_bottom_nav"),
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                NavigationDestination.entries.forEach { destination ->
                    val isSelected = currentDestination == destination
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { currentDestination = destination },
                        icon = {
                            Icon(
                                imageVector = if (isSelected) destination.selectedIcon else destination.unselectedIcon,
                                contentDescription = destination.title
                            )
                        },
                        label = { Text(destination.title) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.testTag(destination.testTag)
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(bottom = innerPadding.calculateBottomPadding())
        ) {
            when (currentDestination) {
                NavigationDestination.HOME -> {
                    HomeScreen(
                        accountName = accountName,
                        avatarSticker = avatarSticker,
                        currencySymbol = currencySymbol,
                        balanceSummary = balanceSummary,
                        monthlyBalanceSummary = monthlyBalanceSummary,
                        selectedMonth = selectedMonth,
                        selectedDay = selectedDay,
                        monthTransactionDays = monthTransactionDays,
                        dailyGroups = dailyGroups,
                        transactions = transactions,
                        searchQuery = searchQuery,
                        selectedTab = selectedTransactionTab,
                        incomeCategories = incomeCategories,
                        expenseCategories = expenseCategories,
                        onSearchQueryChange = { viewModel.setSearchQuery(it) },
                        onClearSearch = { viewModel.clearSearchQuery() },
                        onTabSelected = { viewModel.setSelectedTab(it) },
                        onPreviousMonth = { viewModel.previousMonth() },
                        onNextMonth = { viewModel.nextMonth() },
                        onSelectDay = { viewModel.selectDay(it) },
                        onClearDayFilter = { viewModel.clearSelectedDay() },
                        onSelectYearMonthDay = { y, m, d -> viewModel.selectYearMonthDay(y, m, d) },
                        onAddTransaction = { type, title, amount, category, dateMillis, note ->
                            viewModel.addTransaction(type, title, amount, category, dateMillis, note)
                        },
                        onUpdateTransaction = { viewModel.updateTransaction(it) },
                        onDeleteTransaction = { viewModel.deleteTransaction(it) }
                    )
                }

                NavigationDestination.LOAN -> {
                    LoanScreen(
                        currencySymbol = currencySymbol,
                        balanceSummary = balanceSummary,
                        activeLoans = activeLoans,
                        completedLoans = completedLoans,
                        repayments = allRepayments,
                        selectedTab = selectedLoanTab,
                        onTabSelected = { viewModel.setSelectedLoanTab(it) },
                        onAddLoan = { name, type, amount, dateMillis, dueDateMillis, note ->
                            viewModel.addLoan(name, type, amount, dateMillis, dueDateMillis, note)
                        },
                        onUpdateLoan = { viewModel.updateLoan(it) },
                        onDeleteLoan = { viewModel.deleteLoan(it) },
                        onRecordRepayment = { loanId, amount, dateMillis, note ->
                            viewModel.recordRepayment(loanId, amount, dateMillis, note)
                        }
                    )
                }

                NavigationDestination.SETTINGS -> {
                    SettingsScreen(
                        accountName = accountName,
                        avatarSticker = avatarSticker,
                        currencyCode = currencyCode,
                        currencySymbol = currencySymbol,
                        themeMode = themeMode,
                        customCategories = customCategories,
                        onAddCategory = { name, type, iconKey ->
                            viewModel.addCategory(name, type, iconKey)
                        },
                        onUpdateCategory = { viewModel.updateCategory(it) },
                        onDeleteCategory = { viewModel.deleteCategory(it) },
                        onUpdateAccountName = { viewModel.updateAccountName(it) },
                        onUpdateAvatar = { viewModel.updateAvatar(it) },
                        onUpdateCurrency = { viewModel.updateCurrency(it) },
                        onUpdateThemeMode = { viewModel.updateThemeMode(it) },
                        onExportToExcel = { stream -> viewModel.exportToExcel(stream) },
                        onResetAllData = { viewModel.resetAllData() }
                    )
                }
            }
        }
    }
}
