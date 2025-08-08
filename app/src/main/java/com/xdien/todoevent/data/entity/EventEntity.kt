package com.xdien.todoevent.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.xdien.todoevent.data.database.TodoDatabase

@Entity(tableName = "events")
@TypeConverters(TodoDatabase.Converters::class)
data class EventEntity(
    @PrimaryKey
    val id: Int,
    val title: String,
    val description: String,
    val eventTypeId: Int,
    val startDate: String,
    val location: String,
    val createdAt: String,
    val updatedAt: String?,
    val images: List<String> = emptyList() // Store image URLs as JSON string list
)
