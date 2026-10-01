package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import com.example.ui.theme.LoanBorrowedColor
import com.example.ui.theme.LoanLentColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddLoanSheet(
    currencySymbol: String,
    isBn: Boolean,
    onDismiss: () -> Unit,
    onSave: (personName: String, type: String, totalAmount: Double, note: String, dueDateMillis: Long?) -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
) {
    var type by remember { mutableStateOf("LENT") }
    var personName by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }

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
                    text = if (isBn) "ঋণ বা ধারের হিসাব যোগ করুন" else "Add Loan or Debt",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDismiss, modifier = Modifier.testTag("close_loan_sheet")) {
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
                        .background(if (type == "LENT") LoanLentColor else Color.Transparent)
                        .clickable { type = "LENT" }
                        .padding(vertical = 10.dp)
                        .testTag("toggle_lent"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isBn) "টাকা দিয়েছি (পাব)" else "Lent (To Receive)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = if (type == "LENT") Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (type == "BORROWED") LoanBorrowedColor else Color.Transparent)
                        .clickable { type = "BORROWED" }
                        .padding(vertical = 10.dp)
                        .testTag("toggle_borrowed"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isBn) "টাকা নিয়েছি (দিতে হবে)" else "Borrowed (To Pay)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = if (type == "BORROWED") Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = if (isBn) "ব্যক্তির নাম" else "Person's Name",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = personName,
                onValueChange = { personName = it },
                placeholder = { Text(if (isBn) "যেমন: রহিম ভাই, করিম" else "e.g., John, Brother") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                shape = RoundedCornerShape(14.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                modifier = Modifier.fillMaxWidth().testTag("loan_person_name_input")
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = if (isBn) "টাকার পরিমাণ" else "Total Amount",
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
                        color = if (type == "LENT") LoanLentColor else LoanBorrowedColor,
                        modifier = Modifier.padding(start = 12.dp, end = 6.dp)
                    )
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                shape = RoundedCornerShape(14.dp),
                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold),
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("loan_amount_input")
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = if (isBn) "মন্তব্য / বিবরণ (ঐচ্ছিক)" else "Note / Description",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                placeholder = { Text(if (isBn) "যেমন: বিশেষ প্রয়োজনে ধার দেওয়া" else "e.g., Short-term emergency loan") },
                shape = RoundedCornerShape(14.dp),
                maxLines = 2,
                modifier = Modifier.fillMaxWidth().testTag("loan_note_input")
            )

            Spacer(modifier = Modifier.height(26.dp))

            val amountNum = amountText.toDoubleOrNull() ?: 0.0
            Button(
                onClick = {
                    if (personName.isNotBlank() && amountNum > 0) {
                        onSave(personName.trim(), type, amountNum, note.trim(), null)
                        onDismiss()
                    }
                },
                enabled = personName.isNotBlank() && amountNum > 0,
                modifier = Modifier.fillMaxWidth().height(52.dp).testTag("save_loan_button"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (type == "LENT") LoanLentColor else LoanBorrowedColor
                )
            ) {
                Text(
                    text = if (isBn) "ঋণের হিসাব সংরক্ষণ করুন" else "Save Loan",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
