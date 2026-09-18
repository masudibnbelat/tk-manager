package com.example.data.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalAtm
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Work
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "custom_categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val type: String, // "INCOME" or "EXPENSE" or "BOTH"
    val iconKey: String = "category",
    val isCustom: Boolean = false
)

object CategoryIconHelper {
    val AVAILABLE_ICONS = listOf(
        "fastfood" to Icons.Default.Fastfood,
        "shopping" to Icons.Default.ShoppingCart,
        "transport" to Icons.Default.DirectionsCar,
        "bills" to Icons.Default.Receipt,
        "medical" to Icons.Default.LocalHospital,
        "education" to Icons.Default.School,
        "entertainment" to Icons.Default.Movie,
        "family" to Icons.Default.People,
        "housing" to Icons.Default.Home,
        "personal" to Icons.Default.Spa,
        "fitness" to Icons.Default.FitnessCenter,
        "work" to Icons.Default.Work,
        "salary" to Icons.Default.LocalAtm,
        "business" to Icons.Default.AccountBalance,
        "investment" to Icons.Default.TrendingUp,
        "freelance" to Icons.Default.Computer,
        "gift" to Icons.Default.CardGiftcard,
        "category" to Icons.Default.Category
    )

    fun getAvailableKeys(): List<String> = AVAILABLE_ICONS.map { it.first }

    fun getIcon(key: String): ImageVector {
        return AVAILABLE_ICONS.find { it.first == key }?.second ?: Icons.Default.Category
    }

    fun getIconKeyForCategory(name: String): String {
        return when (name.lowercase()) {
            "food & dining", "food", "dining", "meal", "restaurant" -> "fastfood"
            "groceries", "grocery", "market" -> "shopping"
            "transport", "transportation", "travel", "fuel", "bus", "car" -> "transport"
            "shopping", "clothing", "clothes", "store" -> "shopping"
            "bills & utilities", "bills", "utilities", "electricity", "water", "gas" -> "bills"
            "health & medical", "medical", "health", "doctor", "medicine", "pharmacy" -> "medical"
            "education", "tuition", "course", "books", "school" -> "education"
            "entertainment", "movie", "games", "leisure" -> "movie"
            "family" -> "family"
            "housing & rent", "rent", "housing", "apartment" -> "housing"
            "personal care", "salon", "cosmetics" -> "personal"
            "salary" -> "salary"
            "business" -> "business"
            "freelance", "contract" -> "freelance"
            "investment", "shares", "crypto" -> "investment"
            "gift" -> "gift"
            "rental" -> "housing"
            "refund" -> "localatm"
            else -> "category"
        }
    }

    fun getIconForCategory(name: String): ImageVector {
        val key = getIconKeyForCategory(name)
        return getIcon(key)
    }
}
