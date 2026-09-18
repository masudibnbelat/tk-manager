package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
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
import com.example.data.model.LoanEntity
import com.example.data.model.LoanType
import com.example.ui.theme.LoanBlue
import com.example.ui.theme.PrimaryLight
import com.example.util.FormatUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoanDialog(
    existingLoan: LoanEntity? = null,
    currencySymbol: String,
    onDismiss: () -> Unit,
    onSave: (
        name: String,
        type: String,
        amount: Double,
        dateMillis: Long,
        dueDateMillis: Long,
        note: String?
    ) -> Unit
) {
    val isEditing = existingLoan != null

    var selectedType by remember {
        mutableStateOf(
            if (existingLoan?.type == LoanType.BORROWED.name) LoanType.BORROWED else LoanType.LENT
        )
    }

    var name by remember { mutableStateOf(existingLoan?.name ?: "") }
    var nameError by remember { mutableStateOf(false) }

    var amountText by remember {
        mutableStateOf(existingLoan?.let { String.format("%.2f", it.originalAmount) } ?: "")
    }
    var amountError by remember { mutableStateOf(false) }

    var dateMillis by remember {
        mutableStateOf(existingLoan?.dateMillis ?: System.currentTimeMillis())
    }

    var note by remember { mutableStateOf(existingLoan?.note ?: "") }

    var showDatePicker by remember { mutableStateOf(false) }

    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = dateMillis)

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { dateMillis = it }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
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
                    text = if (isEditing) "Edit Loan" else "Add Loan Record",
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
                // Loan Type Selector
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    SegmentedButton(
                        selected = selectedType == LoanType.LENT,
                        onClick = { selectedType = LoanType.LENT },
                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                        colors = SegmentedButtonDefaults.colors(
                            activeContainerColor = PrimaryLight.copy(alpha = 0.2f),
                            activeContentColor = PrimaryLight
                        )
                    ) {
                        Text("Lent (I gave)", fontWeight = FontWeight.SemiBold)
                    }
                    SegmentedButton(
                        selected = selectedType == LoanType.BORROWED,
                        onClick = { selectedType = LoanType.BORROWED },
                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                        colors = SegmentedButtonDefaults.colors(
                            activeContainerColor = LoanBlue.copy(alpha = 0.2f),
                            activeContentColor = LoanBlue
                        )
                    ) {
                        Text("Borrow (I took)", fontWeight = FontWeight.SemiBold)
                    }
                }

                Text(
                    text = if (selectedType == LoanType.LENT) {
                        "You lent money to someone. They owe you this amount."
                    } else {
                        "You borrowed money from someone. You owe this amount."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Person Name
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        nameError = false
                    },
                    label = {
                        Text(if (selectedType == LoanType.LENT) "Borrower Name" else "Lender Name")
                    },
                    placeholder = { Text("e.g. John Doe, Rahim, Alice") },
                    isError = nameError,
                    supportingText = if (nameError) {
                        { Text("Please enter a name") }
                    } else null,
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("loan_person_name_input")
                )

                // Amount Field
                OutlinedTextField(
                    value = amountText,
                    onValueChange = {
                        amountText = it
                        amountError = false
                    },
                    label = { Text("Loan Amount ($currencySymbol)") },
                    placeholder = { Text("0.00") },
                    isError = amountError,
                    supportingText = if (amountError) {
                        { Text("Please enter a valid positive amount") }
                    } else null,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("loan_amount_input")
                )

                // Single Consolidated Date Button
                OutlinedButton(
                    onClick = { showDatePicker = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("loan_date_button")
                ) {
                    Icon(Icons.Default.CalendarToday, contentDescription = "Date")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Date: ${FormatUtils.formatDate(dateMillis)}")
                }

                // Optional Note
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Optional Note / Terms") },
                    placeholder = { Text("Reason, contact number, or repayment terms...") },
                    maxLines = 3,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("loan_note_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val parsedAmount = amountText.toDoubleOrNull()
                    val isNameValid = name.trim().isNotEmpty()
                    val isAmountValid = parsedAmount != null && parsedAmount > 0.0

                    if (!isNameValid) nameError = true
                    if (!isAmountValid) amountError = true

                    if (isNameValid && isAmountValid && parsedAmount != null) {
                        onSave(
                            name.trim(),
                            selectedType.name,
                            parsedAmount,
                            dateMillis,
                            dateMillis, // Single date architecture
                            note.trim().ifEmpty { null }
                        )
                    }
                },
                modifier = Modifier.testTag("save_loan_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryLight,
                    contentColor = androidx.compose.ui.graphics.Color.White
                )
            ) {
                Text(if (isEditing) "Update" else "Save Loan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
