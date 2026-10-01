package com.example.util

object AppStrings {
    fun navHome(isBn: Boolean) = if (isBn) "হোম" else "Home"
    fun navLoans(isBn: Boolean) = if (isBn) "ঋণ ও ধার" else "Loans"
    fun navSettings(isBn: Boolean) = if (isBn) "সেটিংস" else "Settings"

    fun tabDaily(isBn: Boolean) = if (isBn) "দৈনিক" else "Daily"
    fun tabCalendar(isBn: Boolean) = if (isBn) "ক্যালেন্ডার" else "Calendar"
    fun tabMonthly(isBn: Boolean) = if (isBn) "মাসিক" else "Monthly"
    fun tabOverview(isBn: Boolean) = if (isBn) "সারসংক্ষেপ" else "Overview"

    fun totalBalance(isBn: Boolean) = if (isBn) "বর্তমান ব্যালেন্স" else "Current Balance"
    fun income(isBn: Boolean) = if (isBn) "মোট আয়" else "Total Income"
    fun expense(isBn: Boolean) = if (isBn) "মোট খরচ" else "Total Expense"

    fun search(isBn: Boolean) = if (isBn) "খুঁজুন..." else "Search transactions..."

    fun lentTitle(isBn: Boolean) = if (isBn) "টাকা পাব (প্রদত্ত)" else "To Receive (Lent)"
    fun borrowedTitle(isBn: Boolean) = if (isBn) "টাকা দিতে হবে (গৃহীত)" else "To Pay (Borrowed)"
    fun recordRepayment(isBn: Boolean) = if (isBn) "পরিশোধ লিখুন" else "Record Payment"
    fun settled(isBn: Boolean) = if (isBn) "পরিশোধিত" else "Settled"
    fun pending(isBn: Boolean) = if (isBn) "বকেয়া" else "Pending"
}
