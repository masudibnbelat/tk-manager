package com.example.data.model

data class CurrencyInfo(
    val code: String,
    val symbol: String,
    val nameEn: String,
    val nameBn: String
)

object AvailableCurrencies {
    val list = listOf(
        CurrencyInfo("BDT", "৳", "Bangladeshi Taka", "বাংলাদেশী টাকা"),
        CurrencyInfo("USD", "$", "US Dollar", "মার্কিন ডলার"),
        CurrencyInfo("EUR", "€", "Euro", "ইউরো"),
        CurrencyInfo("INR", "₹", "Indian Rupee", "ভারতীয় রুপি"),
        CurrencyInfo("GBP", "£", "British Pound", "ব্রিটিশ পাউন্ড"),
        CurrencyInfo("SAR", "﷼", "Saudi Riyal", "সৌদি রিয়াল"),
        CurrencyInfo("AED", "د.إ", "UAE Dirham", "ইউএই দিরহাম"),
        CurrencyInfo("MYR", "RM", "Malaysian Ringgit", "মালয়েশিয়ান রিঙ্গিত"),
        CurrencyInfo("CAD", "C$", "Canadian Dollar", "কানাডিয়ান ডলার")
    )

    fun defaultCurrency() = list[0]
}
