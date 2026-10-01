package com.example.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LoanEntity
import com.example.ui.theme.TealPrimary

@Composable
fun RecordRepaymentDialog(
    loan: LoanEntity,
    currencySymbol: String,
    isBn: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (amount: Double, note: String) -> Unit
) {
    var amountText by remember { mutableStateOf(loan.remainingAmount.toString().removeSuffix(".0")) }
    var note by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isBn) "পরিশোধ বা কিস্তি জমা" else "Record Repayment",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Column {
                Text(
                    text = if (isBn)
                        "${loan.personName} এর বকেয়া: $currencySymbol ${loan.remainingAmount}"
                    else
                        "Remaining for ${loan.personName}: $currencySymbol ${loan.remainingAmount}",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { input ->
                        if (input.isEmpty() || input.matches(Regex("""^\d*\.?\d{0,2}$"""))) {
                            amountText = input
                        }
                    },
                    label = { Text(if (isBn) "পরিশোধের পরিমাণ" else "Repaid Amount") },
                    leadingIcon = { Text(currencySymbol, fontWeight = FontWeight.Bold) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("repayment_amount_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text(if (isBn) "নোট / মাধ্যম (ঐচ্ছিক)" else "Note (e.g. Bkash / Cash)") },
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("repayment_note_input")
                )
            }
        },
        confirmButton = {
            val amountNum = amountText.toDoubleOrNull() ?: 0.0
            Button(
                onClick = {
                    if (amountNum > 0) {
                        onConfirm(amountNum, note.trim())
                        onDismiss()
                    }
                },
                enabled = amountNum > 0,
                colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                modifier = Modifier.testTag("confirm_repayment_button")
            ) {
                Text(if (isBn) "নিশ্চিত করুন" else "Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isBn) "বাতিল" else "Cancel")
            }
        },
        shape = RoundedCornerShape(20.dp)
    )
}
