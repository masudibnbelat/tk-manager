package com.example.ui.screens.loans

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LoanEntity
import com.example.data.repository.LoanSummary
import com.example.ui.components.AddLoanSheet
import com.example.ui.components.RecordRepaymentDialog
import com.example.ui.theme.LoanBorrowedColor
import com.example.ui.theme.LoanLentColor
import com.example.ui.theme.TealPrimary
import com.example.util.AppStrings
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun LoansScreen(
    loans: List<LoanEntity>,
    loanSummary: LoanSummary,
    currencySymbol: String,
    isBn: Boolean,
    onAddLoan: (personName: String, type: String, totalAmount: Double, note: String, dueDateMillis: Long?) -> Unit,
    onRecordRepayment: (loan: LoanEntity, amount: Double, note: String) -> Unit,
    onToggleSettled: (loan: LoanEntity) -> Unit,
    onDeleteLoan: (loan: LoanEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var showAddLoanSheet by remember { mutableStateOf(false) }
    var repaymentLoanTarget by remember { mutableStateOf<LoanEntity?>(null) }

    val filterType = if (selectedTab == 0) "LENT" else "BORROWED"
    val filteredLoans = loans.filter { it.type == filterType }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = if (isBn) "ঋণ ও ধারের হিসাব" else "Loans & Debts",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (isBn) "কাউকে ধার দিলে বা নিলে লিখে রাখুন" else "Track money lent & borrowed",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Button(
                onClick = { showAddLoanSheet = true },
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                modifier = Modifier.testTag("add_loan_btn")
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (isBn) "নতুন ঋণ" else "Add Loan",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(
                modifier = Modifier.weight(1f).clickable { selectedTab = 0 },
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (selectedTab == 0) LoanLentColor.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = if (isBn) "টাকা পাব (বকেয়া)" else "To Receive (Lent)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = LoanLentColor
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$currencySymbol ${String.format(Locale.US, "%,.0f", loanSummary.remainingToReceive)}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = LoanLentColor
                    )
                }
            }

            Card(
                modifier = Modifier.weight(1f).clickable { selectedTab = 1 },
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (selectedTab == 1) LoanBorrowedColor.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = if (isBn) "দিতে হবে (বকেয়া)" else "To Pay (Borrowed)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = LoanBorrowedColor
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$currencySymbol ${String.format(Locale.US, "%,.0f", loanSummary.remainingToPay)}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = LoanBorrowedColor
                    )
                }
            }
        }

        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.Transparent,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = if (selectedTab == 0) LoanLentColor else LoanBorrowedColor
                )
            },
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = {
                    Text(
                        text = AppStrings.lentTitle(isBn),
                        fontSize = 13.sp,
                        fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Medium,
                        color = if (selectedTab == 0) LoanLentColor else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                modifier = Modifier.testTag("loan_tab_lent")
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = {
                    Text(
                        text = AppStrings.borrowedTitle(isBn),
                        fontSize = 13.sp,
                        fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Medium,
                        color = if (selectedTab == 1) LoanBorrowedColor else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                modifier = Modifier.testTag("loan_tab_borrowed")
            )
        }

        if (filteredLoans.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "🤝", fontSize = 48.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (isBn) "কোনো ঋণের হিসাব নেই" else "No loans recorded here",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isBn) "উপরের 'নতুন ঋণ' বাটনে ক্লিক করে হিসাব যোগ করুন।" else "Tap 'Add Loan' to start tracking.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 8.dp)) {
                items(filteredLoans, key = { it.id }) { loan ->
                    LoanCardItem(
                        loan = loan,
                        currencySymbol = currencySymbol,
                        isBn = isBn,
                        onRecordRepayment = { repaymentLoanTarget = loan },
                        onToggleSettled = { onToggleSettled(loan) },
                        onDelete = { onDeleteLoan(loan) }
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(90.dp))
                }
            }
        }
    }

    if (showAddLoanSheet) {
        AddLoanSheet(
            currencySymbol = currencySymbol,
            isBn = isBn,
            onDismiss = { showAddLoanSheet = false },
            onSave = onAddLoan
        )
    }

    repaymentLoanTarget?.let { target ->
        RecordRepaymentDialog(
            loan = target,
            currencySymbol = currencySymbol,
            isBn = isBn,
            onDismiss = { repaymentLoanTarget = null },
            onConfirm = { amount, note ->
                onRecordRepayment(target, amount, note)
            }
        )
    }
}

@Composable
fun LoanCardItem(
    loan: LoanEntity,
    currencySymbol: String,
    isBn: Boolean,
    onRecordRepayment: () -> Unit,
    onToggleSettled: () -> Unit,
    onDelete: () -> Unit
) {
    val isLent = loan.type == "LENT"
    val themeColor = if (isLent) LoanLentColor else LoanBorrowedColor
    val formattedDate = remember(loan.dateMillis) {
        SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(loan.dateMillis)
    }

    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(36.dp).clip(CircleShape).background(themeColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = if (isLent) "📤" else "📥", fontSize = 18.sp)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(text = loan.personName, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text(text = formattedDate, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (loan.isSettled) Color(0xFFDCFCE7) else themeColor.copy(alpha = 0.12f))
                        .clickable { onToggleSettled() }
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (loan.isSettled) AppStrings.settled(isBn) else AppStrings.pending(isBn),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (loan.isSettled) Color(0xFF16A34A) else themeColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text(text = if (isBn) "মোট পরিমাণ" else "Total Amount", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(text = "$currencySymbol ${String.format(Locale.US, "%,.0f", loan.totalAmount)}", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(text = if (isBn) "অবশিষ্ট বকেয়া" else "Remaining", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = "$currencySymbol ${String.format(Locale.US, "%,.0f", loan.remainingAmount)}",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (loan.isSettled) Color(0xFF16A34A) else themeColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            LinearProgressIndicator(
                progress = { loan.progress },
                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                color = themeColor,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!loan.isSettled) {
                    Button(
                        onClick = onRecordRepayment,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = themeColor),
                        modifier = Modifier.testTag("loan_repay_${loan.id}")
                    ) {
                        Icon(Icons.Default.Payments, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = AppStrings.recordRepayment(isBn), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF16A34A), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = if (isBn) "সম্পূর্ণ পরিশোধিত" else "Fully settled", fontSize = 12.sp, color = Color(0xFF16A34A))
                    }
                }

                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp).testTag("delete_loan_${loan.id}")) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete Loan", tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f), modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}
