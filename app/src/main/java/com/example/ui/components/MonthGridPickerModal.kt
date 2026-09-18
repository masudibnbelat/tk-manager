package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Calendar

data class MonthItem(
    val monthNumber: Int, // 1 - 12
    val fullName: String,
    val shortName: String
)

/**
 * Grid-based Month Picker Modal
 * Displays January to December in a clean 3-column grid.
 * Example:
 *   January    February    March
 *   April      May         June
 *   July       August      September
 *   October    November    December
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonthGridPickerModal(
    selectedMonth: Int, // 1-12
    onSelectMonth: (Int) -> Unit,
    onDismiss: () -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
) {
    val currentCal = remember { Calendar.getInstance() }
    val todayMonth = currentCal.get(Calendar.MONTH) + 1

    val months = remember {
        listOf(
            MonthItem(1, "January", "Jan"),
            MonthItem(2, "February", "Feb"),
            MonthItem(3, "March", "Mar"),
            MonthItem(4, "April", "Apr"),
            MonthItem(5, "May", "May"),
            MonthItem(6, "June", "Jun"),
            MonthItem(7, "July", "Jul"),
            MonthItem(8, "August", "Aug"),
            MonthItem(9, "September", "Sep"),
            MonthItem(10, "October", "Oct"),
            MonthItem(11, "November", "Nov"),
            MonthItem(12, "December", "Dec")
        )
    }

    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 28.dp)
                .testTag("month_grid_picker_modal")
        ) {
            // Header: Title & Close Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Select Month",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Choose any month to filter transactions",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

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

            HorizontalDivider(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                thickness = 0.8.dp
            )

            Spacer(modifier = Modifier.height(10.dp))

            // 3-Column Grid for Months
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(vertical = 10.dp)
            ) {
                items(months, key = { it.monthNumber }) { item ->
                    val isSelected = item.monthNumber == selectedMonth
                    val isToday = item.monthNumber == todayMonth

                    val containerColor = when {
                        isSelected -> MaterialTheme.colorScheme.primary
                        isDark -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                    }

                    val textColor = when {
                        isSelected -> MaterialTheme.colorScheme.onPrimary
                        else -> MaterialTheme.colorScheme.onSurface
                    }

                    val borderStroke = when {
                        isSelected -> null
                        isToday -> BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.7f))
                        isDark -> BorderStroke(0.8.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
                        else -> BorderStroke(0.8.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    }

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .clickable {
                                onSelectMonth(item.monthNumber)
                                onDismiss()
                            }
                            .testTag("month_item_${item.monthNumber}"),
                        shape = RoundedCornerShape(14.dp),
                        color = containerColor,
                        border = borderStroke
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 18.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = item.fullName,
                                style = MaterialTheme.typography.titleMedium,
                                fontSize = 14.sp,
                                fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Medium,
                                color = textColor,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}
