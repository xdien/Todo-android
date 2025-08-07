package com.xdien.todoevent.data.entity

import androidx.room.Embedded
import androidx.room.Relation

data class TodoWithEventType(
    @Embedded
    val todo: TodoEntity,
    
    @Relation(
        parentColumn = "eventTypeId",
        entityColumn = "id"
    )
    val eventType: EventTypeEntity?
)
