package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.AddTransactionSheet
import com.example.ui.screens.home.HomeScreen
import com.example.ui.screens.loans.LoansScreen
import com.example.ui.screens.settings.SettingsScreen
import com.example.ui.theme.TealPrimary
import com.example.ui.viewmodel.FinanceViewModel
import com.example.util.AppStrings

@Composable
fun MainAppScreen(
    viewModel: FinanceViewModel,
    modifier: Modifier = Modifier
) {
    var selectedNavIndex by remember { mutableIntStateOf(0) }
    var showAddTransactionSheet by remember { mutableStateOf(false) }

    val accountName by viewModel.accountName.collectAsStateWithLifecycle()
    val avatar by viewModel.avatar.collectAsStateWithLifecycle()
    val currencyCode by viewModel.currencyCode.collectAsStateWithLifecycle()
    val currencySymbol by viewModel.currencySymbol.collectAsStateWithLifecycle()
    val appLanguage by viewModel.appLanguage.collectAsStateWithLifecycle()
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()

    val isBn = appLanguage == "bn"

    val monthlySummary by viewModel.monthlySummary.collectAsStateWithLifecycle()
    val lifetimeSummary by viewModel.lifetimeSummary.collectAsStateWithLifecycle()
    val selectedYearMonth by viewModel.selectedYearMonth.collectAsStateWithLifecycle()
    val selectedDay by viewModel.selectedDay.collectAsStateWithLifecycle()
    val transactions by viewModel.transactions.collectAsStateWithLifecycle()
    val categoryBreakdown by viewModel.categoryBreakdown.collectAsStateWithLifecycle()
    val monthTransactionDays by viewModel.monthTransactionDays.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()

    val loans by viewModel.allLoans.collectAsStateWithLifecycle()
    val loanSummary by viewModel.loanSummary.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = selectedNavIndex == 0,
                    onClick = { selectedNavIndex = 0 },
                    icon = { Icon(Icons.Default.Home, contentDescription = AppStrings.navHome(isBn)) },
                    label = {
                        Text(
                            text = AppStrings.navHome(isBn),
                            fontSize = 11.sp,
                            fontWeight = if (selectedNavIndex == 0) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = TealPrimary,
                        selectedTextColor = TealPrimary,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier.testTag("nav_home")
                )

                NavigationBarItem(
                    selected = selectedNavIndex == 1,
                    onClick = { selectedNavIndex = 1 },
                    icon = { Icon(Icons.Default.Handshake, contentDescription = AppStrings.navLoans(isBn)) },
                    label = {
                        Text(
                            text = AppStrings.navLoans(isBn),
                            fontSize = 11.sp,
                            fontWeight = if (selectedNavIndex == 1) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = TealPrimary,
                        selectedTextColor = TealPrimary,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier.testTag("nav_loans")
                )

                NavigationBarItem(
                    selected = selectedNavIndex == 2,
                    onClick = { selectedNavIndex = 2 },
                    icon = { Icon(Icons.Default.Settings, contentDescription = AppStrings.navSettings(isBn)) },
                    label = {
                        Text(
                            text = AppStrings.navSettings(isBn),
                            fontSize = 11.sp,
                            fontWeight = if (selectedNavIndex == 2) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = TealPrimary,
                        selectedTextColor = TealPrimary,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier.testTag("nav_settings")
                )
            }
        },
        floatingActionButton = {
            AnimatedVisibility(
                visible = selectedNavIndex == 0,
                enter = scaleIn() + fadeIn(),
                exit = scaleOut() + fadeOut()
            ) {
                FloatingActionButton(
                    onClick = { showAddTransactionSheet = true },
                    containerColor = TealPrimary,
                    contentColor = Color.White,
                    shape = CircleShape,
                    modifier = Modifier
                        .size(58.dp)
                        .testTag("add_transaction_fab")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Transaction", modifier = Modifier.size(28.dp))
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedNavIndex) {
                0 -> {
                    HomeScreen(
                        accountName = accountName,
                        avatar = avatar,
                        currencySymbol = currencySymbol,
                        monthlySummary = monthlySummary,
                        lifetimeSummary = lifetimeSummary,
                        selectedYearMonth = selectedYearMonth,
                        selectedDay = selectedDay,
                        transactions = transactions,
                        categoryBreakdown = categoryBreakdown,
                        monthTransactionDays = monthTransactionDays,
                        searchQuery = searchQuery,
                        isBn = isBn,
                        onPreviousMonth = { viewModel.previousMonth() },
                        onNextMonth = { viewModel.nextMonth() },
                        onSelectDay = { viewModel.selectDay(it) },
                        onSearchChange = { viewModel.setSearchQuery(it) },
                        onDeleteTransaction = { viewModel.deleteTransaction(it) }
                    )
                }

                1 -> {
                    LoansScreen(
                        loans = loans,
                        loanSummary = loanSummary,
                        currencySymbol = currencySymbol,
                        isBn = isBn,
                        onAddLoan = { person, type, amount, note, due ->
                            viewModel.addLoan(person, type, amount, note, due)
                        },
                        onRecordRepayment = { loan, amount, note ->
                            viewModel.recordRepayment(loan, amount, note)
                        },
                        onToggleSettled = { viewModel.toggleLoanSettled(it) },
                        onDeleteLoan = { viewModel.deleteLoan(it) }
                    )
                }

                2 -> {
                    SettingsScreen(
                        accountName = accountName,
                        avatar = avatar,
                        currencyCode = currencyCode,
                        currencySymbol = currencySymbol,
                        appLanguage = appLanguage,
                        themeMode = themeMode,
                        isBn = isBn,
                        onUpdateProfile = { name, av -> viewModel.updateProfile(name, av) },
                        onUpdateCurrency = { code, sym -> viewModel.updateCurrency(code, sym) },
                        onSetLanguage = { viewModel.setLanguage(it) },
                        onSetThemeMode = { viewModel.setThemeMode(it) },
                        onClearAllData = { viewModel.clearAllData() }
                    )
                }
            }
        }
    }

    if (showAddTransactionSheet) {
        AddTransactionSheet(
            currencySymbol = currencySymbol,
            isBn = isBn,
            onDismiss = { showAddTransactionSheet = false },
            onSave = { type, title, amount, category, dateMillis, note ->
                viewModel.addTransaction(type, title, amount, category, dateMillis, note)
            }
        )
    }
}
