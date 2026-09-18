package com.example.ui.screens.loan

import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material3.Button
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
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LoanEntity
import com.example.data.model.LoanType
import com.example.data.model.RepaymentEntity
import com.example.data.repository.BalanceSummary
import com.example.ui.components.DeleteConfirmDialog
import com.example.ui.components.LoanDetailDrawer
import com.example.ui.components.LoanDialog
import com.example.ui.components.RepaymentDialog
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.ExpenseRedContainer
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.IncomeGreenContainer
import com.example.ui.theme.LoanBlue
import com.example.ui.theme.LoanBlueContainer
import com.example.ui.theme.adaptiveExpenseBorder
import com.example.ui.theme.adaptiveExpenseContainer
import com.example.ui.theme.adaptiveIncomeBorder
import com.example.ui.theme.adaptiveIncomeContainer
import com.example.ui.viewmodel.LoanTab
import com.example.util.FormatUtils

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun LoanScreen(
    currencySymbol: String,
    balanceSummary: BalanceSummary,
    activeLoans: List<LoanEntity>,
    completedLoans: List<LoanEntity>,
    repayments: List<RepaymentEntity>,
    selectedTab: LoanTab,
    onTabSelected: (LoanTab) -> Unit,
    onAddLoan: (
        name: String,
        type: String,
        amount: Double,
        dateMillis: Long,
        dueDateMillis: Long,
        note: String?
    ) -> Unit,
    onUpdateLoan: (LoanEntity) -> Unit,
    onDeleteLoan: (LoanEntity) -> Unit,
    onRecordRepayment: (loanId: Long, amount: Double, dateMillis: Long, note: String?) -> Unit
) {
    var showAddLoanDialog by remember { mutableStateOf(false) }
    var loanToRepay by remember { mutableStateOf<LoanEntity?>(null) }
    var loanToEdit by remember { mutableStateOf<LoanEntity?>(null) }
    var loanToDelete by remember { mutableStateOf<LoanEntity?>(null) }
    var loanDetailSelected by remember { mutableStateOf<LoanEntity?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("loan_screen_list"),
            contentPadding = PaddingValues(bottom = 96.dp)
        ) {
            // Screen Title
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 20.dp, end = 20.dp, top = 14.dp, bottom = 6.dp)
                ) {
                    Text(
                        text = "Loan Manager",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // To Receive & To Pay Cards
            item {
                LoanSummaryRow(
                    balanceSummary = balanceSummary,
                    currencySymbol = currencySymbol
                )
            }

            // Tabs: Active vs Completed
            item {
                TabRow(
                    selectedTabIndex = selectedTab.ordinal,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    containerColor = Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.primary
                ) {
                    Tab(
                        selected = selectedTab == LoanTab.ACTIVE,
                        onClick = { onTabSelected(LoanTab.ACTIVE) },
                        text = {
                            Text(
                                "Active (${activeLoans.size})",
                                fontWeight = if (selectedTab == LoanTab.ACTIVE) FontWeight.Bold else FontWeight.Medium,
                                color = if (selectedTab == LoanTab.ACTIVE) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    )
                    Tab(
                        selected = selectedTab == LoanTab.COMPLETED,
                        onClick = { onTabSelected(LoanTab.COMPLETED) },
                        text = {
                            Text(
                                "Completed (${completedLoans.size})",
                                fontWeight = if (selectedTab == LoanTab.COMPLETED) FontWeight.Bold else FontWeight.Medium,
                                color = if (selectedTab == LoanTab.COMPLETED) IncomeGreen else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    )
                }
            }

            // Loan List
            val currentList = if (selectedTab == LoanTab.ACTIVE) activeLoans else completedLoans

            if (currentList.isEmpty()) {
                item {
                    EmptyLoansView(selectedTab = selectedTab)
                }
            } else {
                items(
                    items = currentList,
                    key = { it.id }
                ) { loan ->
                    LoanCard(
                        loan = loan,
                        currencySymbol = currencySymbol,
                        onCardClick = { loanDetailSelected = loan },
                        onEditClick = { loanToEdit = loan },
                        onDeleteClick = { loanToDelete = loan }
                    )
                }
            }
        }

        // Add Loan Floating Action Button
        FloatingActionButton(
            onClick = { showAddLoanDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 24.dp)
                .testTag("fab_add_loan"),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Loan")
        }
    }

    // Add Loan Dialog
    if (showAddLoanDialog) {
        LoanDialog(
            currencySymbol = currencySymbol,
            onDismiss = { showAddLoanDialog = false },
            onSave = { name, type, amount, dateMillis, dueDateMillis, note ->
                onAddLoan(name, type, amount, dateMillis, dueDateMillis, note)
                showAddLoanDialog = false
            }
        )
    }

    // Edit Loan Dialog
    loanToEdit?.let { loan ->
        LoanDialog(
            existingLoan = loan,
            currencySymbol = currencySymbol,
            onDismiss = { loanToEdit = null },
            onSave = { name, type, amount, dateMillis, dueDateMillis, note ->
                onUpdateLoan(
                    loan.copy(
                        name = name,
                        type = type,
                        originalAmount = amount,
                        dateMillis = dateMillis,
                        dueDateMillis = dueDateMillis,
                        note = note
                    )
                )
                loanToEdit = null
            }
        )
    }

    // Record Repayment Dialog
    loanToRepay?.let { loan ->
        RepaymentDialog(
            loan = loan,
            currencySymbol = currencySymbol,
            onDismiss = { loanToRepay = null },
            onConfirm = { amount, dateMillis, note ->
                onRecordRepayment(loan.id, amount, dateMillis, note)
                loanToRepay = null
            }
        )
    }

    // Loan Detail & Repayment History Drawer
    loanDetailSelected?.let { selectedLoan ->
        LoanDetailDrawer(
            loan = selectedLoan,
            repayments = repayments,
            currencySymbol = currencySymbol,
            onDismiss = { loanDetailSelected = null },
            onAddRepaymentClick = {
                loanToRepay = selectedLoan
                loanDetailSelected = null
            },
            onEditLoanClick = {
                loanToEdit = selectedLoan
                loanDetailSelected = null
            },
            onDeleteLoanClick = {
                loanToDelete = selectedLoan
                loanDetailSelected = null
            }
        )
    }

    // Delete Confirmation Dialog
    loanToDelete?.let { loan ->
        DeleteConfirmDialog(
            title = "Delete Loan Record",
            message = "Are you sure you want to delete the loan with '${loan.name}'? All corresponding repayment records will also be removed.",
            onConfirm = {
                onDeleteLoan(loan)
                loanToDelete = null
            },
            onDismiss = { loanToDelete = null }
        )
    }
}

@Composable
private fun LoanSummaryRow(
    balanceSummary: BalanceSummary,
    currencySymbol: String
) {
    // Two-column breakdown: To Receive (Lent) vs To Pay (Borrowed) - compact and clean
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp)
            .testTag("loan_summary_row"),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // To Receive (Lent)
        Surface(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(14.dp),
            color = adaptiveIncomeContainer(),
            border = BorderStroke(1.dp, adaptiveIncomeBorder())
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(IncomeGreen)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "To Receive",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = IncomeGreen
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = FormatUtils.formatMoney(balanceSummary.outstandingLent, currencySymbol),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = IncomeGreen
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Total: ${FormatUtils.formatCompactMoney(balanceSummary.totalMoneyLent, currencySymbol)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // To Pay (Borrowed)
        Surface(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(14.dp),
            color = adaptiveExpenseContainer(),
            border = BorderStroke(1.dp, adaptiveExpenseBorder())
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(ExpenseRed)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "To Pay",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = ExpenseRed
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = FormatUtils.formatMoney(balanceSummary.outstandingBorrowed, currencySymbol),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = ExpenseRed
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Total: ${FormatUtils.formatCompactMoney(balanceSummary.totalMoneyBorrowed, currencySymbol)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun LoanCard(
    loan: LoanEntity,
    currencySymbol: String,
    onCardClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val isLent = loan.type == LoanType.LENT.name
    val isCompleted = loan.isCompleted || loan.remainingAmount <= 0.0
    val animatedProgress by animateFloatAsState(
        targetValue = loan.progressFraction,
        label = "loan_progress"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onCardClick)
            .testTag("loan_card_${loan.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, ExpenseRed.copy(alpha = 0.6f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Top Row: Counterpart Name + Status Badges + Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = loan.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    // Type Badge (Lent or Borrowed)
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (isLent) adaptiveIncomeContainer() else adaptiveExpenseContainer(),
                        border = BorderStroke(1.dp, if (isLent) adaptiveIncomeBorder() else adaptiveExpenseBorder())
                    ) {
                        Text(
                            text = if (isLent) "Lent" else "Borrowed",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isLent) IncomeGreen else ExpenseRed,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    // Completed Badge if settled
                    if (isCompleted) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = adaptiveIncomeContainer(),
                            border = BorderStroke(1.dp, adaptiveIncomeBorder())
                        ) {
                            Text(
                                text = "Settled",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = IncomeGreen,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                // Edit & Delete icons
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onEditClick,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Loan",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    IconButton(
                        onClick = onDeleteClick,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Delete Loan",
                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Amount Hierarchy: Large Remaining Amount & Original Amount
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = if (isCompleted) "Fully Settled" else if (isLent) "To Receive" else "To Pay",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = FormatUtils.formatMoney(if (isCompleted) loan.originalAmount else loan.remainingAmount, currencySymbol),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isCompleted) IncomeGreen else if (isLent) IncomeGreen else ExpenseRed
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Total: ${FormatUtils.formatMoney(loan.originalAmount, currencySymbol)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (loan.repaidAmount > 0.0 && !isCompleted) {
                        Text(
                            text = "Repaid: ${FormatUtils.formatMoney(loan.repaidAmount, currencySymbol)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = IncomeGreen,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Animated Smooth Progress Bar
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = if (isCompleted) IncomeGreen else if (isLent) IncomeGreen else ExpenseRed,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        text = "${loan.progressPercentage}% paid",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        color = if (isCompleted) IncomeGreen else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Note if present
            if (!loan.note.isNullOrBlank()) {
                Text(
                    text = loan.note,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f),
                    maxLines = 2
                )
            }
        }
    }
}

@Composable
private fun EmptyLoansView(selectedTab: LoanTab) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(adaptiveExpenseContainer())
                .border(1.dp, ExpenseRed.copy(alpha = 0.5f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Handshake,
                contentDescription = null,
                tint = ExpenseRed,
                modifier = Modifier.size(32.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = if (selectedTab == LoanTab.ACTIVE) "No active loans" else "No completed loans yet",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = if (selectedTab == LoanTab.ACTIVE) {
                "Tap + below to track money you lent or borrowed"
            } else {
                "When active loans are fully repaid, they appear here"
            },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
