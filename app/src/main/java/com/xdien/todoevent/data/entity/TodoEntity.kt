package com.xdien.todoevent.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "todos")
data class TodoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String?,
    val thumbnailUrl: String? = null,
    val galleryImages: List<String>? = null, // List of image URLs for gallery/carousel
    val eventTime: Long? = null,
    val eventEndTime: Long? = null, // End time for events
    val location: String? = null,
    val eventType: String? = null, // Type of event (e.g., "Meeting", "Party", "Conference")
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) 