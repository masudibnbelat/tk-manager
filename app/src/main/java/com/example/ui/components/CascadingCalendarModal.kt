package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.IncomeGreenContainer
import com.example.ui.theme.PrimaryLight
import java.text.DateFormatSymbols
import java.util.Calendar
import java.util.Locale

enum class CalendarStage {
    YEAR,
    MONTH,
    DAY
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CascadingCalendarModal(
    initialYear: Int,
    initialMonth: Int, // 1-12
    initialDay: Int?,
    monthTransactionDays: Set<Int> = emptySet(),
    onSelectDate: (year: Int, month: Int, day: Int?) -> Unit,
    onDismiss: () -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
) {
    val currentCal = remember { Calendar.getInstance() }
    val todayYear = currentCal.get(Calendar.YEAR)
    val todayMonth = currentCal.get(Calendar.MONTH) + 1
    val todayDay = currentCal.get(Calendar.DAY_OF_MONTH)

    var currentStage by remember { mutableStateOf(CalendarStage.DAY) }
    var selectedYear by remember { mutableIntStateOf(initialYear) }
    var selectedMonth by remember { mutableIntStateOf(initialMonth) }
    var selectedDay by remember { mutableStateOf(initialDay) }

    val monthNames = remember {
        DateFormatSymbols(Locale.ENGLISH).shortMonths.filter { it.isNotBlank() }
    }

    val years = remember(todayYear) {
        ((todayYear - 4)..(todayYear + 4)).toList()
    }

    // Days calculation for selected year & month
    val daysInMonth = remember(selectedYear, selectedMonth) {
        val cal = Calendar.getInstance()
        cal.set(Calendar.YEAR, selectedYear)
        cal.set(Calendar.MONTH, selectedMonth - 1)
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    }

    val firstDayOfWeek = remember(selectedYear, selectedMonth) {
        val cal = Calendar.getInstance()
        cal.set(Calendar.YEAR, selectedYear)
        cal.set(Calendar.MONTH, selectedMonth - 1)
        cal.set(Calendar.DAY_OF_MONTH, 1)
        // Calendar.SUNDAY = 1, we want 0-based offset where Sunday = 0
        cal.get(Calendar.DAY_OF_WEEK) - 1
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 6.dp)
                    .size(width = 40.dp, height = 4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
            )
        },
        modifier = Modifier.testTag("cascading_calendar_modal")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            // Header Bar: Navigation breadcrumb + Today Reset + Close
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (currentStage != CalendarStage.YEAR) {
                        IconButton(
                            onClick = {
                                currentStage = when (currentStage) {
                                    CalendarStage.DAY -> CalendarStage.MONTH
                                    CalendarStage.MONTH -> CalendarStage.YEAR
                                    CalendarStage.YEAR -> CalendarStage.YEAR
                                }
                            },
                            modifier = Modifier.size(36.dp).testTag("cal_back_stage_btn")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                    }

                    // Breadcrumb hierarchy indicator
                    Column {
                        Text(
                            text = when (currentStage) {
                                CalendarStage.YEAR -> "Step 1: Select Year"
                                CalendarStage.MONTH -> "Step 2: Select Month ($selectedYear)"
                                CalendarStage.DAY -> "Step 3: Select Day (${monthNames.getOrNull(selectedMonth - 1) ?: ""} $selectedYear)"
                            },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "$selectedYear",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = if (currentStage == CalendarStage.YEAR) PrimaryLight else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .clickable { currentStage = CalendarStage.YEAR }
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                            Text(" ➔ ", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                            Text(
                                text = monthNames.getOrNull(selectedMonth - 1) ?: "Month",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = if (currentStage == CalendarStage.MONTH) PrimaryLight else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .clickable { currentStage = CalendarStage.MONTH }
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                            if (currentStage == CalendarStage.DAY) {
                                Text(" ➔ ", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                                Text(
                                    text = if (selectedDay != null) "Day $selectedDay" else "All Days",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryLight,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }

                // Quick "Today" jump button & close
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AssistChip(
                        onClick = {
                            selectedYear = todayYear
                            selectedMonth = todayMonth
                            selectedDay = todayDay
                            onSelectDate(todayYear, todayMonth, todayDay)
                            onDismiss()
                        },
                        label = { Text("Today", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Today,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                        },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                            labelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        modifier = Modifier.testTag("cal_today_chip")
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(14.dp))

            // Animated Hierarchical Content
            AnimatedContent(
                targetState = currentStage,
                transitionSpec = {
                    if (targetState.ordinal > initialState.ordinal) {
                        (slideInHorizontally { width -> width } + fadeIn()).togetherWith(
                            slideOutHorizontally { width -> -width } + fadeOut()
                        )
                    } else {
                        (slideInHorizontally { width -> -width } + fadeIn()).togetherWith(
                            slideOutHorizontally { width -> width } + fadeOut()
                        )
                    }
                },
                label = "calendar_drill_down"
            ) { stage ->
                when (stage) {
                    // Stage 1: Year Grid
                    CalendarStage.YEAR -> {
                        Column {
                            Text(
                                text = "Choose a Year",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(3),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(260.dp)
                                    .testTag("cal_year_grid")
                            ) {
                                items(years) { year ->
                                    val isSelected = year == selectedYear
                                    val isCurrent = year == todayYear

                                    Surface(
                                        shape = RoundedCornerShape(14.dp),
                                        color = if (isSelected) MaterialTheme.colorScheme.primary
                                        else if (isCurrent) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                        border = if (isSelected) null
                                        else if (isCurrent) androidx.compose.foundation.BorderStroke(1.5.dp, PrimaryLight)
                                        else null,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(14.dp))
                                            .clickable {
                                                selectedYear = year
                                                currentStage = CalendarStage.MONTH
                                            }
                                            .testTag("year_item_$year")
                                    ) {
                                        Box(
                                            modifier = Modifier.padding(vertical = 16.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "$year",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = if (isSelected || isCurrent) FontWeight.Bold else FontWeight.Medium,
                                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Stage 2: Month Grid
                    CalendarStage.MONTH -> {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Choose Month for $selectedYear",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Text(
                                    text = "Change Year",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = PrimaryLight,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier
                                        .clickable { currentStage = CalendarStage.YEAR }
                                        .padding(4.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            LazyVerticalGrid(
                                columns = GridCells.Fixed(3),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(260.dp)
                                    .testTag("cal_month_grid")
                            ) {
                                items(monthNames.indices.toList()) { index ->
                                    val monthNumber = index + 1
                                    val monthName = monthNames[index]
                                    val isSelected = monthNumber == selectedMonth
                                    val isCurrent = (selectedYear == todayYear && monthNumber == todayMonth)

                                    Surface(
                                        shape = RoundedCornerShape(14.dp),
                                        color = if (isSelected) MaterialTheme.colorScheme.primary
                                        else if (isCurrent) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                        border = if (isSelected) null
                                        else if (isCurrent) androidx.compose.foundation.BorderStroke(1.5.dp, PrimaryLight)
                                        else null,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(14.dp))
                                            .clickable {
                                                selectedMonth = monthNumber
                                                currentStage = CalendarStage.DAY
                                            }
                                            .testTag("month_item_$monthNumber")
                                    ) {
                                        Box(
                                            modifier = Modifier.padding(vertical = 16.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = monthName,
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = if (isSelected || isCurrent) FontWeight.Bold else FontWeight.Medium,
                                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Stage 3: Day View (Calendar Grid)
                    CalendarStage.DAY -> {
                        Column {
                            // Weekday headers
                            val dayLabels = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                dayLabels.forEach { label ->
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (label == "Sun") Color(0xFFEF4444) else MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Total grid cells: offset + daysInMonth
                            val totalSlots = firstDayOfWeek + daysInMonth
                            val rows = (totalSlots + 6) / 7

                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                for (r in 0 until rows) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceAround
                                    ) {
                                        for (c in 0 until 7) {
                                            val dayNumber = (r * 7 + c) - firstDayOfWeek + 1
                                            if (dayNumber in 1..daysInMonth) {
                                                val isSelected = selectedDay == dayNumber
                                                val isToday = (selectedYear == todayYear && selectedMonth == todayMonth && dayNumber == todayDay)
                                                val hasTransactions = monthTransactionDays.contains(dayNumber)

                                                Box(
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .aspectRatio(1f)
                                                        .padding(2.dp)
                                                        .clip(CircleShape)
                                                        .background(
                                                            if (isSelected) MaterialTheme.colorScheme.primary
                                                            else if (isToday) MaterialTheme.colorScheme.primaryContainer
                                                            else Color.Transparent
                                                        )
                                                        .border(
                                                            width = if (isToday && !isSelected) 1.5.dp else 0.dp,
                                                            color = if (isToday && !isSelected) PrimaryLight else Color.Transparent,
                                                            shape = CircleShape
                                                        )
                                                        .clickable {
                                                            selectedDay = dayNumber
                                                            onSelectDate(selectedYear, selectedMonth, dayNumber)
                                                            onDismiss()
                                                        }
                                                        .testTag("day_cell_$dayNumber"),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                        Text(
                                                            text = "$dayNumber",
                                                            style = MaterialTheme.typography.bodyMedium,
                                                            fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
                                                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                                        )

                                                        if (hasTransactions) {
                                                            Box(
                                                                modifier = Modifier
                                                                    .size(4.dp)
                                                                    .clip(CircleShape)
                                                                    .background(if (isSelected) Color.White else IncomeGreen)
                                                            )
                                                        }
                                                    }
                                                }
                                            } else {
                                                Spacer(modifier = Modifier.weight(1f))
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Action buttons: "View Entire Month" or "Confirm"
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        selectedDay = null
                                        onSelectDate(selectedYear, selectedMonth, null)
                                        onDismiss()
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(46.dp),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("Full Month")
                                }

                                Button(
                                    onClick = {
                                        onSelectDate(selectedYear, selectedMonth, selectedDay)
                                        onDismiss()
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(46.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryLight)
                                ) {
                                    Text("Apply Selection")
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
