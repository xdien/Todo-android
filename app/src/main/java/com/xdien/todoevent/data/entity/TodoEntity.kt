package com.xdien.todoevent.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "todos",
    foreignKeys = [
        ForeignKey(
            entity = EventTypeEntity::class,
            parentColumns = ["id"],
            childColumns = ["eventTypeId"],
            onDelete = ForeignKey.CASCADE // Khi xóa EventType thì xóa luôn các Todo liên quan
        )
    ],
    indices = [Index("eventTypeId")] // Thêm index cho foreign key
)
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
    val eventTypeId: Long? = null, // Foreign key to EventTypeEntity
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) 