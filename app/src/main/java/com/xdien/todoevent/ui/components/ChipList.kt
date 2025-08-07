package com.xdien.todoevent.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.StateFlow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChipList(
    chips: StateFlow<List<ChipItem>>,
    onChipClick: (ChipItem) -> Unit,
    modifier: Modifier = Modifier,
    singleSelection: Boolean = true,
    horizontalSpacing: Dp = 8.dp,
    verticalSpacing: Dp = 8.dp,
    showEmptyState: Boolean = true,
    emptyStateText: String = "Không có sự kiện nào"
) {
    val chipList by chips.collectAsState()
    
    if (chipList.isEmpty() && showEmptyState) {
        Box(
            modifier = modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = emptyStateText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    } else {
        LazyRow(
            modifier = modifier,
            horizontalArrangement = Arrangement.spacedBy(horizontalSpacing),
            contentPadding = PaddingValues(vertical = verticalSpacing)
        ) {
            items(chipList) { chip ->
                ChipItem(
                    chip = chip,
                    onClick = {
                        if (singleSelection) {
                            // For single selection, update all chips
                            val updatedChips = chipList.map { 
                                it.copy(isSelected = it.id == chip.id)
                            }
                            // This would need to be handled by the parent component
                            onChipClick(chip)
                        } else {
                            // For multiple selection, just toggle the clicked chip
                            onChipClick(chip)
                        }
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChipItem(
    chip: ChipItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val chipColor = chip.color ?: if (chip.isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }
    
    val textColor = if (chip.isSelected) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
    
    FilterChip(
        onClick = onClick,
        label = {
            Text(
                text = chip.title,
                color = textColor,
                style = MaterialTheme.typography.bodyMedium
            )
        },
        selected = chip.isSelected,
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = chipColor,
            selectedLabelColor = textColor,
            containerColor = chipColor,
            labelColor = textColor
        ),
        modifier = modifier
    )
}

// Extension function to convert TodoEntity list to ChipItem list
fun List<com.xdien.todoevent.data.entity.TodoEntity>.toChipItems(
    selectedIds: Set<Long> = emptySet()
): List<ChipItem> {
    return this.map { todo ->
        ChipItem(
            id = todo.id.toString(),
            title = todo.title,
            isSelected = selectedIds.contains(todo.id),
            color = when (todo.eventTypeId) {
                1L -> Color(0xFF2196F3) // Blue - Meeting
                2L -> Color(0xFF4CAF50) // Green - Work
                3L -> Color(0xFFFF9800) // Orange - Personal
                4L -> Color(0xFF9C27B0) // Purple - Party
                5L -> Color(0xFFE91E63) // Pink - Conference
                else -> null
            }
        )
    }
} 