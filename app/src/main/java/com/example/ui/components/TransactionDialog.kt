package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.model.Categories
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionType
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.util.FormatUtils

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TransactionDialog(
    existingTransaction: TransactionEntity? = null,
    currencySymbol: String,
    incomeCategories: List<String> = Categories.INCOME_CATEGORIES,
    expenseCategories: List<String> = Categories.EXPENSE_CATEGORIES,
    onDismiss: () -> Unit,
    onSave: (
        type: TransactionType,
        title: String,
        amount: Double,
        category: String,
        dateMillis: Long,
        note: String?
    ) -> Unit
) {
    val isEditing = existingTransaction != null

    var selectedType by remember {
        mutableStateOf(
            if (existingTransaction?.type == TransactionType.INCOME.name) {
                TransactionType.INCOME
            } else {
                TransactionType.EXPENSE
            }
        )
    }

    var title by remember { mutableStateOf(existingTransaction?.title ?: "") }
    var titleError by remember { mutableStateOf(false) }

    var amountText by remember {
        mutableStateOf(
            existingTransaction?.let { String.format("%.2f", it.amount) } ?: ""
        )
    }
    var amountError by remember { mutableStateOf(false) }

    val defaultCategories = if (selectedType == TransactionType.INCOME) {
        incomeCategories
    } else {
        expenseCategories
    }

    var selectedCategory by remember {
        mutableStateOf(
            existingTransaction?.category ?: defaultCategories.firstOrNull() ?: "General"
        )
    }


    var dateMillis by remember {
        mutableStateOf(existingTransaction?.dateMillis ?: System.currentTimeMillis())
    }

    var note by remember { mutableStateOf(existingTransaction?.note ?: "") }
    var showDatePicker by remember { mutableStateOf(false) }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = dateMillis
    )

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { dateMillis = it }
                    showDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isEditing) "Edit Transaction" else "Add Transaction",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Type Selector: Income / Expense
                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    SegmentedButton(
                        selected = selectedType == TransactionType.EXPENSE,
                        onClick = {
                            selectedType = TransactionType.EXPENSE
                            if (!Categories.EXPENSE_CATEGORIES.contains(selectedCategory)) {
                                selectedCategory = Categories.EXPENSE_CATEGORIES.first()
                            }
                        },
                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                        colors = SegmentedButtonDefaults.colors(
                            activeContainerColor = ExpenseRed.copy(alpha = 0.2f),
                            activeContentColor = ExpenseRed
                        )
                    ) {
                        Text("Expense", fontWeight = FontWeight.SemiBold)
                    }
                    SegmentedButton(
                        selected = selectedType == TransactionType.INCOME,
                        onClick = {
                            selectedType = TransactionType.INCOME
                            if (!Categories.INCOME_CATEGORIES.contains(selectedCategory)) {
                                selectedCategory = Categories.INCOME_CATEGORIES.first()
                            }
                        },
                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                        colors = SegmentedButtonDefaults.colors(
                            activeContainerColor = IncomeGreen.copy(alpha = 0.2f),
                            activeContentColor = IncomeGreen
                        )
                    ) {
                        Text("Income", fontWeight = FontWeight.SemiBold)
                    }
                }

                // Amount Field
                OutlinedTextField(
                    value = amountText,
                    onValueChange = {
                        amountText = it
                        amountError = false
                    },
                    label = { Text("Amount ($currencySymbol)") },
                    placeholder = { Text("0.00") },
                    isError = amountError,
                    supportingText = if (amountError) {
                        { Text("Please enter a valid positive amount") }
                    } else null,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("transaction_amount_input")
                )

                // Title Field
                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        titleError = false
                    },
                    label = { Text("Title / Description") },
                    placeholder = { Text("e.g., Grocery shopping, Monthly Salary") },
                    isError = titleError,
                    supportingText = if (titleError) {
                        { Text("Title cannot be empty") }
                    } else null,
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("transaction_title_input")
                )

                // Category Selection Chips
                Text(
                    text = "Category",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                val currentCategoryList = if (selectedType == TransactionType.INCOME) {
                    incomeCategories
                } else {
                    expenseCategories
                }

                FlowRow(

                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    currentCategoryList.forEach { category ->
                        val isSelected = selectedCategory == category
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedCategory = category },
                            label = { Text(category, style = MaterialTheme.typography.bodySmall) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }

                // Date Picker Button
                OutlinedButton(
                    onClick = { showDatePicker = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.CalendarToday, contentDescription = "Date")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Date: ${FormatUtils.formatDate(dateMillis)}")
                }

                // Optional Note
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Optional Note") },
                    placeholder = { Text("Additional details...") },
                    maxLines = 3,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("transaction_note_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val parsedAmount = amountText.toDoubleOrNull()
                    val isTitleValid = title.trim().isNotEmpty()
                    val isAmountValid = parsedAmount != null && parsedAmount > 0.0

                    if (!isTitleValid) titleError = true
                    if (!isAmountValid) amountError = true

                    if (isTitleValid && isAmountValid && parsedAmount != null) {
                        onSave(
                            selectedType,
                            title.trim(),
                            parsedAmount,
                            selectedCategory,
                            dateMillis,
                            note.trim().ifEmpty { null }
                        )
                    }
                },
                modifier = Modifier.testTag("save_transaction_button")
            ) {
                Text(if (isEditing) "Update" else "Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
