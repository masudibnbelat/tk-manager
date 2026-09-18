package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

// ==========================================
// Tk Manager Solid Red Theme Palette (#BE1A1A)
// Rich, Bold, Smooth Solid Red with High Contrast
// ==========================================

// Solid Red Brand Colors
val BrandSolidRed = Color(0xFFBE1A1A) // Primary requested #BE1A1A
val BrandSolidRedDark = Color(0xFF9E1414) // Deeper shade for gradients & active buttons
val BrandSolidRedLight = Color(0xFFD63232) // Bright highlight shade

// Light Theme
val PrimaryLight = Color(0xFFBE1A1A) // Solid Red #BE1A1A
val OnPrimaryLight = Color(0xFFFFFFFF)
val PrimaryContainerLight = Color(0xFFFFEBEE) // Soft clean rose-red tint
val OnPrimaryContainerLight = Color(0xFF5A0004)

val SecondaryLight = Color(0xFF9E1414) // Deep Red Accent
val OnSecondaryLight = Color(0xFFFFFFFF)
val SecondaryContainerLight = Color(0xFFFFCDD2)
val OnSecondaryContainerLight = Color(0xFF450002)

val TertiaryLight = Color(0xFF7B0000)
val OnTertiaryLight = Color(0xFFFFFFFF)
val TertiaryContainerLight = Color(0xFFFFEBEE)
val OnTertiaryContainerLight = Color(0xFF380000)

val BackgroundLight = Color(0xFFFBF9F9) // Warm, crisp, clean neutral canvas
val OnBackgroundLight = Color(0xFF1C1919)
val SurfaceLight = Color(0xFFFFFFFF) // Crisp solid card surface
val OnSurfaceLight = Color(0xFF1C1919)
val SurfaceVariantLight = Color(0xFFF6E8E8) // Elegant soft red-tinted container
val OnSurfaceVariantLight = Color(0xFF4D3939)
val OutlineLight = Color(0xFFE0D0D0)

// Dark Theme - STRICTLY DARK (No white backgrounds in Dark Mode)
val PrimaryDark = Color(0xFFFF5252) // Vibrant Solid Red for Dark Mode
val OnPrimaryDark = Color(0xFF450002)
val PrimaryContainerDark = Color(0xFF680D0D) // Rich solid deep red container
val OnPrimaryContainerDark = Color(0xFFFFDCDA)

val SecondaryDark = Color(0xFFFF8A80)
val OnSecondaryDark = Color(0xFF450002)
val SecondaryContainerDark = Color(0xFF550A0A)
val OnSecondaryContainerDark = Color(0xFFFFDAD6)

val TertiaryDark = Color(0xFFFFB4AB)
val OnTertiaryDark = Color(0xFF450002)
val TertiaryContainerDark = Color(0xFF680D0D)
val OnTertiaryContainerDark = Color(0xFFFFDAD6)

val BackgroundDark = Color(0xFF120E0E) // Deep obsidian dark canvas with warm undertone
val OnBackgroundDark = Color(0xFFF0EBEB)
val SurfaceDark = Color(0xFF1B1414) // Elevated dark card surface
val OnSurfaceDark = Color(0xFFF0EBEB)
val SurfaceVariantDark = Color(0xFF281D1D) // Dark container surface
val OnSurfaceVariantDark = Color(0xFFD6C3C3)
val OutlineDark = Color(0xFF544444)

// ==========================================
// Financial Semantic Colors
// Vibrant Crisp Green (#10B981) for Income & Repayments
// Solid Red (#BE1A1A / #EF4444) for Expenses & Borrowed Amounts
// ==========================================
val IncomeGreen = Color(0xFF10B981) // Vibrant crisp Emerald 500
val IncomeGreenContainer = Color(0xFFECFDF5) // Soft emerald pill container
val IncomeGreenDark = Color(0xFF34D399) // Emerald 400 for dark mode
val IncomeGreenPillBorder = Color(0xFFA7F3D0) // Emerald 200 border

val ExpenseRed = Color(0xFFBE1A1A) // Solid Red #BE1A1A
val ExpenseRedContainer = Color(0xFFFFEBEE) // Soft rose pill container
val ExpenseRedDark = Color(0xFFFF6B6B)
val ExpenseRedPillBorder = Color(0xFFFFCDD2)

val DebtReceivableBlue = Color(0xFF0284C7) // Crisp Sky/Blue for Lent/Receivables
val DebtReceivableContainer = Color(0xFFE0F2FE)

val LoanBlue = Color(0xFFBE1A1A) // Solid Red #BE1A1A for Loans
val LoanBlueContainer = Color(0xFFFFEBEE)
val LoanBlueDark = Color(0xFFFF8A80)

// Helper composables to guarantee Dark Mode never renders white containers
@Composable
fun isDarkModeActive(): Boolean = MaterialTheme.colorScheme.surface.luminance() < 0.5f

@Composable
fun adaptiveIncomeContainer(): Color =
    if (isDarkModeActive()) Color(0xFF0D2818) else Color(0xFFECFDF5)

@Composable
fun adaptiveIncomeBorder(): Color =
    if (isDarkModeActive()) IncomeGreen.copy(alpha = 0.5f) else IncomeGreenPillBorder

@Composable
fun adaptiveExpenseContainer(): Color =
    if (isDarkModeActive()) Color(0xFF261212) else Color(0xFFFFEBEE)

@Composable
fun adaptiveExpenseBorder(): Color =
    if (isDarkModeActive()) ExpenseRed.copy(alpha = 0.5f) else ExpenseRedPillBorder


