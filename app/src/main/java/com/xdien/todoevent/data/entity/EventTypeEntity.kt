package com.xdien.todoevent.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "event_type")
data class EventTypeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
)