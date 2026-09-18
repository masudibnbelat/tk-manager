package com.example.ui.screens.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Categories
import com.example.data.model.CategoryIconHelper
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionType
import com.example.data.repository.BalanceSummary
import com.example.ui.components.CascadingCalendarModal
import com.example.ui.components.DeleteConfirmDialog
import com.example.ui.components.MonthGridPickerModal
import com.example.ui.components.TransactionDialog
import com.example.ui.components.YearGridPickerModal
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.ExpenseRedContainer
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.IncomeGreenContainer
import com.example.ui.theme.IncomeGreenPillBorder
import com.example.ui.theme.PrimaryLight
import com.example.ui.theme.adaptiveIncomeBorder
import com.example.ui.theme.adaptiveIncomeContainer
import com.example.ui.viewmodel.DailyTransactionGroup
import com.example.ui.viewmodel.TransactionTab
import com.example.util.FormatUtils
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

// Custom tabs matching the screenshot
private enum class HomeSubTab(val title: String) {
    DAILY("Daily"),
    CALENDAR("Calendar"),
    MONTHLY("Monthly"),
    TOTAL("Total")
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    accountName: String,
    avatarSticker: String,
    currencySymbol: String,
    balanceSummary: BalanceSummary,
    monthlyBalanceSummary: BalanceSummary,
    selectedMonth: Int? = null,
    selectedDay: Int? = null,
    monthTransactionDays: Set<Int> = emptySet(),
    dailyGroups: List<DailyTransactionGroup> = emptyList(),
    transactions: List<TransactionEntity> = emptyList(),
    searchQuery: String,
    selectedTab: TransactionTab,
    incomeCategories: List<String> = emptyList(),
    expenseCategories: List<String> = emptyList(),
    onSearchQueryChange: (String) -> Unit,
    onClearSearch: () -> Unit,
    onTabSelected: (TransactionTab) -> Unit,
    onPreviousMonth: () -> Unit = {},
    onNextMonth: () -> Unit = {},
    onSelectDay: (Int) -> Unit = {},
    onClearDayFilter: () -> Unit = {},
    onSelectYearMonthDay: ((year: Int, month: Int, day: Int?) -> Unit)? = null,
    onAddTransaction: (
        type: TransactionType,
        title: String,
        amount: Double,
        category: String,
        dateMillis: Long,
        note: String?
    ) -> Unit,
    onUpdateTransaction: (TransactionEntity) -> Unit,
    onDeleteTransaction: (TransactionEntity) -> Unit
) {
    var currentSubTab by remember { mutableStateOf(HomeSubTab.DAILY) }
    var isSearchExpanded by remember { mutableStateOf(searchQuery.isNotEmpty()) }
    var showAddDialog by remember { mutableStateOf(false) }
    var showCalendarDrillDown by remember { mutableStateOf(false) }
    var showYearPicker by remember { mutableStateOf(false) }
    var showMonthPicker by remember { mutableStateOf(false) }
    var transactionToEdit by remember { mutableStateOf<TransactionEntity?>(null) }
    var transactionToDelete by remember { mutableStateOf<TransactionEntity?>(null) }

    val currentCal = Calendar.getInstance()
    val todayYear = currentCal.get(Calendar.YEAR)
    val todayMonth = currentCal.get(Calendar.MONTH) + 1
    val todayDay = currentCal.get(Calendar.DAY_OF_MONTH)

    val targetYear = (selectedMonth ?: (todayYear * 100 + todayMonth)) / 100
    val targetMonth = (selectedMonth ?: (todayYear * 100 + todayMonth)) % 100
    val isCurrentMonth = (targetYear == todayYear && targetMonth == todayMonth)

    val monthDisplayName = remember(targetYear, targetMonth) {
        val cal = Calendar.getInstance()
        cal.set(Calendar.YEAR, targetYear)
        cal.set(Calendar.MONTH, targetMonth - 1)
        cal.set(Calendar.DAY_OF_MONTH, 1)
        SimpleDateFormat("MMMM", Locale.getDefault()).format(cal.time)
    }

    val yearDisplayName = remember(targetYear) {
        targetYear.toString()
    }

    val monthName = remember(targetYear, targetMonth) {
        val cal = Calendar.getInstance()
        cal.set(Calendar.YEAR, targetYear)
        cal.set(Calendar.MONTH, targetMonth - 1)
        cal.set(Calendar.DAY_OF_MONTH, 1)
        SimpleDateFormat("MMM yyyy", Locale.getDefault()).format(cal.time)
    }

    val daysInMonth = remember(targetYear, targetMonth) {
        val cal = Calendar.getInstance()
        cal.set(Calendar.YEAR, targetYear)
        cal.set(Calendar.MONTH, targetMonth - 1)
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // 1. Top Bar: < Month Year >         Search
            TopSimpleHeader(
                monthDisplayName = monthDisplayName,
                yearDisplayName = yearDisplayName,
                onPreviousMonth = onPreviousMonth,
                onNextMonth = onNextMonth,
                onMonthClick = { showMonthPicker = true },
                onYearClick = { showYearPicker = true },
                isSearchExpanded = isSearchExpanded,
                onToggleSearch = {
                    isSearchExpanded = !isSearchExpanded
                    if (!isSearchExpanded) onClearSearch()
                }
            )

            // Search Bar with exact placeholder "Search by title"
            AnimatedVisibility(
                visible = isSearchExpanded || searchQuery.isNotEmpty(),
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChange,
                        placeholder = {
                            Text(
                                text = "Search by title",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search icon",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = onClearSearch) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Clear search",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedBorderColor = Color.Transparent,
                            focusedBorderColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("home_search_bar")
                    )
                }
            }

            // 2. Navigation Sub-Tabs: Daily | Calendar | Monthly | Total
            ScrollableTabRow(
                selectedTabIndex = currentSubTab.ordinal,
                edgePadding = 16.dp,
                containerColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.primary,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[currentSubTab.ordinal]),
                        color = Color(0xFFFF5252), // Coral red line from screenshot
                        height = 2.5.dp
                    )
                },
                divider = {
                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), thickness = 0.8.dp)
                }
            ) {
                HomeSubTab.entries.forEach { tab ->
                    val isSelected = currentSubTab == tab
                    Tab(
                        selected = isSelected,
                        onClick = { currentSubTab = tab },
                        text = {
                            Text(
                                text = tab.title,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    )
                }
            }

            // 3. Compact 3-Column Summary Bar: Income (Blue) | Expenses (Red) | Total
            ThreeColumnSummaryBar(
                income = monthlyBalanceSummary.totalIncome,
                expenses = monthlyBalanceSummary.totalExpenses,
                total = monthlyBalanceSummary.currentBalance,
                currencySymbol = currencySymbol
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f), thickness = 0.8.dp)

            // 4. Main Body Content Based on Selected Tab
            when (currentSubTab) {
                HomeSubTab.DAILY -> {
                    DailyListView(
                        balanceSummary = balanceSummary,
                        currencySymbol = currencySymbol,
                        dailyGroups = dailyGroups,
                        isSearching = searchQuery.isNotEmpty(),
                        selectedDay = selectedDay,
                        monthName = monthName,
                        onClearDayFilter = onClearDayFilter,
                        onEditTransaction = { transactionToEdit = it },
                        onDeleteTransaction = { transactionToDelete = it }
                    )
                }
                HomeSubTab.CALENDAR -> {
                    CalendarTabContent(
                        targetYear = targetYear,
                        targetMonth = targetMonth,
                        todayDay = todayDay,
                        isCurrentMonth = isCurrentMonth,
                        daysInMonth = daysInMonth,
                        selectedDay = selectedDay,
                        monthTransactionDays = monthTransactionDays,
                        dailyGroups = dailyGroups,
                        currencySymbol = currencySymbol,
                        onSelectDay = onSelectDay,
                        onClearDayFilter = onClearDayFilter,
                        onEditTransaction = { transactionToEdit = it },
                        onDeleteTransaction = { transactionToDelete = it }
                    )
                }
                HomeSubTab.MONTHLY -> {
                    MonthlyBreakdownContent(
                        monthlySummary = monthlyBalanceSummary,
                        transactions = transactions,
                        currencySymbol = currencySymbol,
                        monthName = monthName
                    )
                }
                HomeSubTab.TOTAL -> {
                    TotalOverviewContent(
                        balanceSummary = balanceSummary,
                        monthlySummary = monthlyBalanceSummary,
                        currencySymbol = currencySymbol
                    )
                }
            }
        }

        // Floating Action Button (Red Coral Circle with +)
        FloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 24.dp)
                .testTag("fab_add_transaction"),
            shape = CircleShape,
            containerColor = Color(0xFFFF5252),
            contentColor = Color.White
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Transaction", modifier = Modifier.size(28.dp))
        }
    }

    // Add Transaction Dialog
    if (showAddDialog) {
        TransactionDialog(
            currencySymbol = currencySymbol,
            incomeCategories = if (incomeCategories.isNotEmpty()) incomeCategories else Categories.INCOME_CATEGORIES,
            expenseCategories = if (expenseCategories.isNotEmpty()) expenseCategories else Categories.EXPENSE_CATEGORIES,
            onDismiss = { showAddDialog = false },
            onSave = { type, title, amount, category, dateMillis, note ->
                onAddTransaction(type, title, amount, category, dateMillis, note)
                showAddDialog = false
            }
        )
    }

    // Edit Transaction Dialog
    transactionToEdit?.let { tx ->
        TransactionDialog(
            existingTransaction = tx,
            currencySymbol = currencySymbol,
            incomeCategories = if (incomeCategories.isNotEmpty()) incomeCategories else Categories.INCOME_CATEGORIES,
            expenseCategories = if (expenseCategories.isNotEmpty()) expenseCategories else Categories.EXPENSE_CATEGORIES,
            onDismiss = { transactionToEdit = null },
            onSave = { type, title, amount, category, dateMillis, note ->
                onUpdateTransaction(
                    tx.copy(
                        type = type.name,
                        title = title,
                        amount = amount,
                        category = category,
                        dateMillis = dateMillis,
                        note = note
                    )
                )
                transactionToEdit = null
            }
        )
    }

    // Delete Confirmation Dialog
    transactionToDelete?.let { tx ->
        DeleteConfirmDialog(
            title = "Delete Transaction",
            message = "Are you sure you want to delete '${tx.title}'? This action cannot be undone.",
            onConfirm = {
                onDeleteTransaction(tx)
                transactionToDelete = null
            },
            onDismiss = { transactionToDelete = null }
        )
    }

    // Year Grid Picker Modal
    if (showYearPicker) {
        YearGridPickerModal(
            selectedYear = targetYear,
            onSelectYear = { y ->
                onSelectYearMonthDay?.invoke(y, targetMonth, null)
                showYearPicker = false
            },
            onDismiss = { showYearPicker = false }
        )
    }

    // Month Grid Picker Modal
    if (showMonthPicker) {
        MonthGridPickerModal(
            selectedMonth = targetMonth,
            onSelectMonth = { m ->
                onSelectYearMonthDay?.invoke(targetYear, m, null)
                showMonthPicker = false
            },
            onDismiss = { showMonthPicker = false }
        )
    }

    // Cascading Calendar Drill-Down Modal (Year ➔ Month ➔ Day)
    if (showCalendarDrillDown) {
        CascadingCalendarModal(
            initialYear = targetYear,
            initialMonth = targetMonth,
            initialDay = selectedDay,
            monthTransactionDays = monthTransactionDays,
            onSelectDate = { y, m, d ->
                onSelectYearMonthDay?.invoke(y, m, d)
            },
            onDismiss = { showCalendarDrillDown = false }
        )
    }
}

// Top Bar matching the screenshot: < Month Year >   [Search]
@Composable
private fun TopSimpleHeader(
    monthDisplayName: String,
    yearDisplayName: String,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onMonthClick: () -> Unit,
    onYearClick: () -> Unit,
    isSearchExpanded: Boolean,
    onToggleSearch: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Month & Year navigation: < Month [v]  Year [v] >
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            IconButton(
                onClick = onPreviousMonth,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Previous Month",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Month chip
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(onClick = onMonthClick)
                    .padding(horizontal = 6.dp, vertical = 4.dp)
                    .testTag("home_month_picker_trigger")
            ) {
                Text(
                    text = monthDisplayName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "Select Month",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }

            // Year chip
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(onClick = onYearClick)
                    .padding(horizontal = 6.dp, vertical = 4.dp)
                    .testTag("home_year_picker_trigger")
            ) {
                Text(
                    text = yearDisplayName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "Select Year",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }

            IconButton(
                onClick = onNextMonth,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Next Month",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Action Icon: Search toggle
        IconButton(
            onClick = onToggleSearch,
            modifier = Modifier.size(36.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = if (isSearchExpanded) Color(0xFFFF5252) else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

// 3-Column Summary Bar matching the screenshot:
// Income (Vibrant Green) | Expenses (Coral Red) | Total
@Composable
private fun ThreeColumnSummaryBar(
    income: Double,
    expenses: Double,
    total: Double,
    currencySymbol: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Income Column
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Income",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = String.format(Locale.US, "%,.2f", income),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = IncomeGreen
            )
        }

        // Expenses Column
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Expenses",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = String.format(Locale.US, "%,.2f", expenses),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = ExpenseRed
            )
        }

        // Total Column
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Total",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = String.format(Locale.US, "%,.2f", total),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (total < 0) ExpenseRed else if (total > 0) IncomeGreen else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

// Daily list view matching the screenshot
@Composable
private fun DailyListView(
    balanceSummary: BalanceSummary,
    currencySymbol: String,
    dailyGroups: List<DailyTransactionGroup>,
    isSearching: Boolean,
    selectedDay: Int?,
    monthName: String,
    onClearDayFilter: () -> Unit,
    onEditTransaction: (TransactionEntity) -> Unit,
    onDeleteTransaction: (TransactionEntity) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("home_screen_list"),
        contentPadding = PaddingValues(bottom = 96.dp)
    ) {
        // Day filter active chip
        if (selectedDay != null) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Showing only Day $selectedDay",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFFF5252),
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Show All Days",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable(onClick = onClearDayFilter)
                    )
                }
            }
        }

        // Empty state or daily transaction groups
        if (dailyGroups.isEmpty()) {
            item {
                EmptyStateView(
                    isSearching = isSearching,
                    selectedDay = selectedDay,
                    monthName = monthName
                )
            }
        } else {
            items(
                items = dailyGroups,
                key = { it.dateKey }
            ) { group ->
                DailyScreenshotStyleGroup(
                    group = group,
                    currencySymbol = currencySymbol,
                    onEditTransaction = onEditTransaction,
                    onDeleteTransaction = onDeleteTransaction
                )
            }
        }
    }
}

// Daily Header & Items styled exactly like screenshot:
// 23 [Wed] 09.2026           ৳ 0.00   ৳ 555.00
// 🧘 Health   Cash                    ৳ 555.00
@Composable
private fun DailyScreenshotStyleGroup(
    group: DailyTransactionGroup,
    currencySymbol: String,
    onEditTransaction: (TransactionEntity) -> Unit,
    onDeleteTransaction: (TransactionEntity) -> Unit
) {
    val dateParts = remember(group.dateKey) {
        // e.g. "2026-09-23" -> day "23", monthYear "09.2026"
        val parts = group.dateKey.split("-")
        if (parts.size == 3) {
            val yr = parts[0]
            val mo = parts[1]
            val dy = parts[2]
            val cal = Calendar.getInstance()
            cal.set(yr.toIntOrNull() ?: 2026, (mo.toIntOrNull() ?: 1) - 1, dy.toIntOrNull() ?: 1)
            val dayOfWeek = SimpleDateFormat("EEE", Locale.getDefault()).format(cal.time)
            Triple(dy, dayOfWeek, "$mo.$yr")
        } else {
            Triple(group.dateFormatted, "", "")
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
    ) {
        // Date Header Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                .padding(horizontal = 16.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Day Number in bold
            Text(
                text = dateParts.first,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.width(6.dp))

            // Day of week badge
            if (dateParts.second.isNotEmpty()) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = dateParts.second,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Month.Year
            Text(
                text = dateParts.third,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.weight(1f))

            // Income Total Column
            Text(
                text = "$currencySymbol ${String.format(Locale.US, "%,.2f", group.totalIncome)}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = if (group.totalIncome > 0) IncomeGreen else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
            )

            Spacer(modifier = Modifier.width(16.dp))

            // Expense Total Column
            Text(
                text = "$currencySymbol ${String.format(Locale.US, "%,.2f", group.totalExpense)}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = if (group.totalExpense > 0) ExpenseRed else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
            )
        }

        // Transactions under this date
        group.transactions.forEach { tx ->
            val isIncome = tx.type == TransactionType.INCOME.name
            val categoryIcon = CategoryIconHelper.getIconForCategory(tx.category)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onEditTransaction(tx) }
                    .padding(horizontal = 16.dp, vertical = 10.dp)
                    .testTag("transaction_item_${tx.id}"),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Category Icon
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(
                            if (isIncome) adaptiveIncomeContainer() else MaterialTheme.colorScheme.surfaceVariant
                        )
                        .then(
                            if (isIncome) Modifier.border(0.8.dp, adaptiveIncomeBorder(), CircleShape) else Modifier
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = categoryIcon,
                        contentDescription = tx.category,
                        tint = if (isIncome) IncomeGreen else ExpenseRed,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                // Title or Category
                Text(
                    text = tx.title.ifEmpty { tx.category },
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.width(16.dp))

                // Category or Note badge
                Text(
                    text = if (tx.note.isNullOrEmpty()) tx.category else tx.note,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )

                Spacer(modifier = Modifier.weight(1f))

                // Amount with crisp high-contrast styling
                if (isIncome) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = adaptiveIncomeContainer(),
                        border = BorderStroke(1.dp, adaptiveIncomeBorder())
                    ) {
                        Text(
                            text = "+ $currencySymbol ${String.format(Locale.US, "%,.2f", tx.amount)}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = IncomeGreen,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                } else {
                    Text(
                        text = "- $currencySymbol ${String.format(Locale.US, "%,.2f", tx.amount)}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = ExpenseRed,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 3.dp)
                    )
                }

                IconButton(
                    onClick = { onDeleteTransaction(tx) },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            HorizontalDivider(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f),
                thickness = 0.5.dp
            )
        }
    }
}

// Calendar View Tab
@Composable
private fun CalendarTabContent(
    targetYear: Int,
    targetMonth: Int,
    todayDay: Int,
    isCurrentMonth: Boolean,
    daysInMonth: Int,
    selectedDay: Int?,
    monthTransactionDays: Set<Int>,
    dailyGroups: List<DailyTransactionGroup>,
    currencySymbol: String,
    onSelectDay: (Int) -> Unit,
    onClearDayFilter: () -> Unit,
    onEditTransaction: (TransactionEntity) -> Unit,
    onDeleteTransaction: (TransactionEntity) -> Unit
) {
    val listState = rememberLazyListState()
    LaunchedEffect(targetMonth, targetYear) {
        val targetIndex = if (isCurrentMonth && todayDay in 1..daysInMonth) {
            (todayDay - 3).coerceAtLeast(0)
        } else 0
        listState.animateScrollToItem(targetIndex)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Date Selector Strip
        LazyRow(
            state = listState,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            items(count = daysInMonth) { index ->
                val day = index + 1
                val isToday = isCurrentMonth && (day == todayDay)
                val isSelected = (selectedDay == day)
                val hasTransactions = monthTransactionDays.contains(day)

                val dayOfWeek = remember(targetYear, targetMonth, day) {
                    val cal = Calendar.getInstance()
                    cal.set(Calendar.YEAR, targetYear)
                    cal.set(Calendar.MONTH, targetMonth - 1)
                    cal.set(Calendar.DAY_OF_MONTH, day)
                    SimpleDateFormat("EEE", Locale.getDefault()).format(cal.time)
                }

                Box(
                    modifier = Modifier
                        .width(46.dp)
                        .height(64.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            when {
                                isSelected -> Color(0xFFFF5252)
                                isToday -> Color(0xFFFF5252).copy(alpha = 0.15f)
                                else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                            }
                        )
                        .clickable { onSelectDay(day) }
                        .padding(vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = dayOfWeek,
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = day.toString(),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Box(
                            modifier = Modifier
                                .size(4.dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        hasTransactions && isSelected -> Color.White
                                        hasTransactions -> Color(0xFFFF5252)
                                        else -> Color.Transparent
                                    }
                                )
                        )
                    }
                }
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), thickness = 0.8.dp)

        // Transactions for selected day
        DailyListView(
            balanceSummary = BalanceSummary(),
            currencySymbol = currencySymbol,
            dailyGroups = dailyGroups,
            isSearching = false,
            selectedDay = selectedDay,
            monthName = "",
            onClearDayFilter = onClearDayFilter,
            onEditTransaction = onEditTransaction,
            onDeleteTransaction = onDeleteTransaction
        )
    }
}

// Monthly Breakdown Content
@Composable
private fun MonthlyBreakdownContent(
    monthlySummary: BalanceSummary,
    transactions: List<TransactionEntity>,
    currencySymbol: String,
    monthName: String
) {
    val categoryTotals = remember(transactions) {
        transactions
            .filter { it.type == TransactionType.EXPENSE.name }
            .groupBy { it.category }
            .mapValues { entry -> entry.value.sumOf { it.amount } }
            .toList()
            .sortedByDescending { it.second }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Expense Categories in $monthName",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        if (categoryTotals.isEmpty()) {
            item {
                Text(
                    text = "No expenses recorded in $monthName",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            items(categoryTotals) { (category, total) ->
                val percentage = if (monthlySummary.totalExpenses > 0) {
                    (total / monthlySummary.totalExpenses).toFloat()
                } else 0f

                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = category,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "$currencySymbol ${String.format(Locale.US, "%,.2f", total)}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFF5252)
                            )
                        }
                        LinearProgressIndicator(
                            progress = { percentage },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(CircleShape),
                            color = Color(0xFFFF5252),
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    }
                }
            }
        }
    }
}

// Total Overview Content
@Composable
private fun TotalOverviewContent(
    balanceSummary: BalanceSummary,
    monthlySummary: BalanceSummary,
    currencySymbol: String
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Lifetime Financial Position",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = FormatUtils.formatMoney(balanceSummary.currentBalance, currencySymbol),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Total Income", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            text = "+$currencySymbol ${FormatUtils.formatCompactMoney(balanceSummary.totalIncome, "")}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = IncomeGreen
                        )
                    }
                    Column {
                        Text("Total Expenses", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            text = "-$currencySymbol ${FormatUtils.formatCompactMoney(balanceSummary.totalExpenses, "")}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = ExpenseRed
                        )
                    }
                    Column {
                        Text("Active Loans", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            text = "$currencySymbol ${FormatUtils.formatCompactMoney(balanceSummary.totalOutstanding, "")}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

// Empty State View
@Composable
private fun EmptyStateView(
    isSearching: Boolean,
    selectedDay: Int?,
    monthName: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.ReceiptLong,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(28.dp)
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = when {
                isSearching -> "No matching transactions found"
                selectedDay != null -> "No records on Day $selectedDay"
                else -> "No transactions for $monthName"
            },
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}
