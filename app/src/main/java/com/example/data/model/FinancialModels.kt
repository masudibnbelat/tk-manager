package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class TransactionType {
    INCOME,
    EXPENSE
}

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val type: String, // "INCOME" or "EXPENSE"
    val title: String,
    val category: String,
    val amount: Double,
    val dateMillis: Long,
    val note: String? = null
)

enum class LoanType {
    BORROWED, // Money borrowed from someone (we owe)
    LENT      // Money lent to someone (they owe us)
}

@Entity(tableName = "loans")
data class LoanEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val type: String, // "BORROWED" or "LENT"
    val originalAmount: Double,
    val repaidAmount: Double = 0.0,
    val dateMillis: Long,
    val dueDateMillis: Long,
    val note: String? = null,
    val isCompleted: Boolean = false
) {
    val remainingAmount: Double
        get() = maxOf(0.0, originalAmount - repaidAmount)

    val progressFraction: Float
        get() = if (originalAmount > 0) {
            (repaidAmount / originalAmount).coerceIn(0.0, 1.0).toFloat()
        } else {
            1.0f
        }

    val progressPercentage: Int
        get() = (progressFraction * 100).toInt()
}

@Entity(tableName = "repayments")
data class RepaymentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val loanId: Long,
    val loanName: String,
    val amount: Double,
    val dateMillis: Long,
    val note: String? = null
)

data class CurrencyInfo(
    val code: String,
    val name: String,
    val symbol: String
)

object Currencies {
    val SUPPORTED = listOf(
        // Asia & Middle East
        CurrencyInfo("BDT", "Bangladeshi Taka", "৳"),
        CurrencyInfo("USD", "US Dollar", "$"),
        CurrencyInfo("EUR", "Euro", "€"),
        CurrencyInfo("INR", "Indian Rupee", "₹"),
        CurrencyInfo("GBP", "British Pound", "£"),
        CurrencyInfo("PKR", "Pakistani Rupee", "₨"),
        CurrencyInfo("SAR", "Saudi Riyal", "﷼"),
        CurrencyInfo("AED", "UAE Dirham", "د.إ"),
        CurrencyInfo("QAR", "Qatari Riyal", "﷼"),
        CurrencyInfo("KWD", "Kuwaiti Dinar", "KD"),
        CurrencyInfo("BHD", "Bahraini Dinar", "BD"),
        CurrencyInfo("OMR", "Omani Rial", "﷼"),
        CurrencyInfo("MYR", "Malaysian Ringgit", "RM"),
        CurrencyInfo("SGD", "Singapore Dollar", "S$"),
        CurrencyInfo("IDR", "Indonesian Rupiah", "Rp"),
        CurrencyInfo("THB", "Thai Baht", "฿"),
        CurrencyInfo("PHP", "Philippine Peso", "₱"),
        CurrencyInfo("VND", "Vietnamese Dong", "₫"),
        CurrencyInfo("JPY", "Japanese Yen", "¥"),
        CurrencyInfo("CNY", "Chinese Yuan", "¥"),
        CurrencyInfo("KRW", "South Korean Won", "₩"),
        CurrencyInfo("HKD", "Hong Kong Dollar", "HK$"),
        CurrencyInfo("TWD", "New Taiwan Dollar", "NT$"),
        CurrencyInfo("LKR", "Sri Lankan Rupee", "Rs"),
        CurrencyInfo("NPR", "Nepalese Rupee", "Rs"),
        // Americas
        CurrencyInfo("CAD", "Canadian Dollar", "C$"),
        CurrencyInfo("AUD", "Australian Dollar", "A$"),
        CurrencyInfo("NZD", "New Zealand Dollar", "NZ$"),
        CurrencyInfo("BRL", "Brazilian Real", "R$"),
        CurrencyInfo("MXN", "Mexican Peso", "Mex$"),
        CurrencyInfo("ARS", "Argentine Peso", "$"),
        CurrencyInfo("CLP", "Chilean Peso", "CLP$"),
        CurrencyInfo("COP", "Colombian Peso", "COL$"),
        CurrencyInfo("PEN", "Peruvian Sol", "S/"),
        // Europe
        CurrencyInfo("CHF", "Swiss Franc", "CHF"),
        CurrencyInfo("TRY", "Turkish Lira", "₺"),
        CurrencyInfo("RUB", "Russian Ruble", "₽"),
        CurrencyInfo("SEK", "Swedish Krona", "kr"),
        CurrencyInfo("NOK", "Norwegian Krone", "kr"),
        CurrencyInfo("DKK", "Danish Krone", "kr"),
        CurrencyInfo("PLN", "Polish Zloty", "zł"),
        CurrencyInfo("CZK", "Czech Koruna", "Kč"),
        CurrencyInfo("HUF", "Hungarian Forint", "Ft"),
        CurrencyInfo("RON", "Romanian Leu", "lei"),
        CurrencyInfo("ILS", "Israeli Shekel", "₪"),
        // Africa
        CurrencyInfo("EGP", "Egyptian Pound", "E£"),
        CurrencyInfo("ZAR", "South African Rand", "R"),
        CurrencyInfo("NGN", "Nigerian Naira", "₦"),
        CurrencyInfo("KES", "Kenyan Shilling", "KSh"),
        CurrencyInfo("GHS", "Ghanaian Cedi", "GH₵"),
        CurrencyInfo("MAD", "Moroccan Dirham", "MAD"),
        CurrencyInfo("DZD", "Algerian Dinar", "DA")
    )

    val DEFAULT = SUPPORTED[0] // BDT (৳)
}

object Categories {
    val INCOME_CATEGORIES = listOf(
        "Salary",
        "Business",
        "Freelance",
        "Investment",
        "Gift",
        "Rental",
        "Refund",
        "Other Income"
    )

    val EXPENSE_CATEGORIES = listOf(
        "Food & Dining",
        "Groceries",
        "Transport",
        "Shopping",
        "Bills & Utilities",
        "Health & Medical",
        "Education",
        "Entertainment",
        "Family",
        "Housing & Rent",
        "Personal Care",
        "Other Expense"
    )
}
