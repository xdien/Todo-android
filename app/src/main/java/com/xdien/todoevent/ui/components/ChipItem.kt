package com.xdien.todoevent.ui.components

data class ChipItem(
    val id: String,
    val title: String,
    val isSelected: Boolean = false,
    val icon: Int? = null, // Resource ID for icon
    val color: androidx.compose.ui.graphics.Color? = null // Custom color for chip
) 