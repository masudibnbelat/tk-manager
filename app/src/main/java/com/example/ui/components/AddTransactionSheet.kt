package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddTransactionSheet(
    currencySymbol: String,
    isBn: Boolean,
    onDismiss: () -> Unit,
    onSave: (type: String, title: String, amount: Double, category: String, dateMillis: Long, note: String) -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
) {
    var type by remember { mutableStateOf("EXPENSE") }
    var amountText by remember { mutableStateOf("") }
    var title by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }

    val expenseCategories = listOf(
        "খাবার" to "🍔",
        "যাতায়াত" to "🚗",
        "বাজার-সদাই" to "🛍️",
        "বিল ও ভাড়া" to "💡",
        "চিকিৎসা" to "💊",
        "পড়াশোনা" to "📚",
        "বিনোদন" to "🎬",
        "অন্যান্য" to "✨"
    )

    val incomeCategories = listOf(
        "বেতন" to "💰",
        "ব্যবসা" to "📈",
        "উপহার" to "🎁",
        "বিনিয়োগ" to "🪙",
        "অন্যান্য" to "✨"
    )

    val currentCategories = if (type == "EXPENSE") expenseCategories else incomeCategories
    var selectedCategory by remember(type) { mutableStateOf(currentCategories[0].first) }
    val quickAddAmounts = listOf(50.0, 100.0, 500.0, 1000.0)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isBn) "নতুন হিসাব যোগ করুন" else "Add New Transaction",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDismiss, modifier = Modifier.testTag("close_sheet_btn")) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                    .padding(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (type == "EXPENSE") ExpenseRed else Color.Transparent)
                        .clickable {
                            type = "EXPENSE"
                            selectedCategory = expenseCategories[0].first
                        }
                        .padding(vertical = 10.dp)
                        .testTag("toggle_expense"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isBn) "খরচ (Expense)" else "Expense",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = if (type == "EXPENSE") Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (type == "INCOME") IncomeGreen else Color.Transparent)
                        .clickable {
                            type = "INCOME"
                            selectedCategory = incomeCategories[0].first
                        }
                        .padding(vertical = 10.dp)
                        .testTag("toggle_income"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isBn) "আয় (Income)" else "Income",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = if (type == "INCOME") Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = if (isBn) "টাকার পরিমাণ" else "Amount",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = amountText,
                onValueChange = { input ->
                    if (input.isEmpty() || input.matches(Regex("""^\d*\.?\d{0,2}$"""))) {
                        amountText = input
                    }
                },
                placeholder = { Text("0.00", fontSize = 22.sp, fontWeight = FontWeight.Bold) },
                leadingIcon = {
                    Text(
                        text = currencySymbol,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (type == "EXPENSE") ExpenseRed else IncomeGreen,
                        modifier = Modifier.padding(start = 12.dp, end = 6.dp)
                    )
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                shape = RoundedCornerShape(16.dp),
                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 22.sp, fontWeight = FontWeight.Bold),
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("transaction_amount_input")
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                quickAddAmounts.forEach { quickVal ->
                    Card(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        modifier = Modifier.weight(1f).clickable {
                            val current = amountText.toDoubleOrNull() ?: 0.0
                            val nextVal = current + quickVal
                            amountText = if (nextVal % 1.0 == 0.0) nextVal.toLong().toString() else nextVal.toString()
                        }
                    ) {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "+${quickVal.toInt()}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = if (isBn) "ক্যাটাগরি" else "Category",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                currentCategories.forEach { (catName, catIcon) ->
                    val isSelected = catName == selectedCategory
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) {
                                if (type == "EXPENSE") ExpenseRed else IncomeGreen
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            }
                        ),
                        modifier = Modifier.clickable { selectedCategory = catName }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = catIcon, fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = catName,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = if (isBn) "বিবরণ (ঐচ্ছিক)" else "Title / Description",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                placeholder = { Text(if (isBn) "যেমন: দুপুরের খাবার, বিদ্যুৎ বিল" else "e.g., Lunch, Bus fare") },
                shape = RoundedCornerShape(14.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                modifier = Modifier.fillMaxWidth().testTag("transaction_title_input")
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = if (isBn) "নোট (ঐচ্ছিক)" else "Note (Optional)",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                placeholder = { Text(if (isBn) "অতিরিক্ত কোনো তথ্য থাকলে লিখুন" else "Add extra details") },
                shape = RoundedCornerShape(14.dp),
                maxLines = 2,
                modifier = Modifier.fillMaxWidth().testTag("transaction_note_input")
            )

            Spacer(modifier = Modifier.height(24.dp))

            val amountNum = amountText.toDoubleOrNull() ?: 0.0
            Button(
                onClick = {
                    if (amountNum > 0) {
                        onSave(
                            type,
                            if (title.isBlank()) selectedCategory else title.trim(),
                            amountNum,
                            selectedCategory,
                            System.currentTimeMillis(),
                            note.trim()
                        )
                        onDismiss()
                    }
                },
                enabled = amountNum > 0,
                modifier = Modifier.fillMaxWidth().height(52.dp).testTag("save_transaction_button"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (type == "EXPENSE") ExpenseRed else IncomeGreen
                )
            ) {
                Text(
                    text = if (isBn) "সংরক্ষণ করুন" else "Save Transaction",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
