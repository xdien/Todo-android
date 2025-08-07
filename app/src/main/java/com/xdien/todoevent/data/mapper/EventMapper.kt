package com.xdien.todoevent.data.mapper

import com.xdien.todoevent.data.api.CreateEventRequest
import com.xdien.todoevent.data.api.EventResponse
import com.xdien.todoevent.data.api.EventType as ApiEventType
import com.xdien.todoevent.data.api.EventImage as ApiEventImage
import com.xdien.todoevent.data.entity.TodoEntity
import com.xdien.todoevent.domain.model.Event
import com.xdien.todoevent.domain.model.EventType
import com.xdien.todoevent.domain.model.EventImage

/**
 * Mapper for converting between different event representations
 */
object EventMapper {
    
    /**
     * Convert domain Event to TodoEntity for database storage
     */
    fun Event.toEntity(): TodoEntity {
        // Convert ISO string to timestamp for database storage
        val eventTimeMillis = try {
            if (this.startDate.matches(Regex("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}"))) {
                // ISO format: "2024-01-01T10:30:00"
                val formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
                val dateTime = java.time.LocalDateTime.parse(this.startDate, formatter)
                dateTime.atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
            } else {
                // Try to parse as timestamp
                this.startDate.toLongOrNull() ?: System.currentTimeMillis()
            }
        } catch (e: Exception) {
            System.currentTimeMillis()
        }
        
        return TodoEntity(
            id = this.id.toLong(),
            title = this.title,
            description = this.description,
            thumbnailUrl = this.images.firstOrNull()?.url,
            galleryImages = this.images.map { it.url },
            eventTime = eventTimeMillis,
            eventEndTime = null, // Not used in new structure
            location = this.location,
            eventTypeId = this.typeId.toLong(),
            isCompleted = false,
            createdAt = this.createdAt.toLongOrNull() ?: System.currentTimeMillis(),
            updatedAt = this.updatedAt?.toLongOrNull() ?: System.currentTimeMillis()
        )
    }
    
    /**
     * Convert TodoEntity to domain Event
     */
    fun TodoEntity.toDomain(): Event {
        // Convert timestamp to ISO string format
        val startDateISO = try {
            val eventTimeMillis = this.eventTime ?: System.currentTimeMillis()
            val instant = java.time.Instant.ofEpochMilli(eventTimeMillis)
            val dateTime = instant.atZone(java.time.ZoneId.systemDefault()).toLocalDateTime()
            val formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
            dateTime.format(formatter)
        } catch (e: Exception) {
            (this.eventTime ?: System.currentTimeMillis()).toString() // Fallback to timestamp string
        }
        
        return Event(
            id = this.id.toInt(),
            title = this.title,
            description = this.description ?: "",
            typeId = this.eventTypeId?.toInt() ?: 1,
            startDate = startDateISO,
            location = this.location ?: "",
            createdAt = this.createdAt.toString(),
            updatedAt = this.updatedAt.toString(),
            images = this.galleryImages?.mapIndexed { index, url ->
                EventImage(
                    id = index,
                    eventId = this.id.toInt(),
                    originalName = "image_$index.jpg",
                    filename = "image_$index.jpg",
                    filePath = "",
                    fileSize = 0,
                    uploadedAt = this.createdAt.toString(),
                    url = url
                )
            } ?: emptyList()
        )
    }
    
    /**
     * Convert domain Event to CreateEventRequest for API
     */
    fun Event.toCreateRequest(): CreateEventRequest {
        return CreateEventRequest(
            title = this.title,
            description = this.description,
            typeId = this.typeId,
            startDate = this.startDate,
            location = this.location
        )
    }
    
    /**
     * Convert EventResponse to domain Event
     */
    fun EventResponse.toDomain(): Event {
        return Event(
            id = this.id,
            title = this.title ?: "",
            description = this.description ?: "",
            typeId = this.typeId ?: 1,
            startDate = this.startDate ?: "",
            location = this.location ?: "",
            createdAt = this.createdAt ?: "",
            updatedAt = this.updatedAt,
            images = this.images?.map { it.toDomain() } ?: emptyList()
        )
    }
    
    /**
     * Convert ApiEventImage to domain EventImage
     */
    fun ApiEventImage.toDomain(): EventImage {
        return EventImage(
            id = this.id,
            eventId = this.eventId,
            originalName = this.originalName,
            filename = this.filename,
            filePath = this.filePath,
            fileSize = this.fileSize,
            uploadedAt = this.uploadedAt,
            url = this.url
        )
    }
    
    /**
     * Convert domain EventImage to ApiEventImage
     */
    fun EventImage.toApi(): ApiEventImage {
        return ApiEventImage(
            id = this.id,
            eventId = this.eventId,
            originalName = this.originalName,
            filename = this.filename,
            filePath = this.filePath,
            fileSize = this.fileSize,
            uploadedAt = this.uploadedAt,
            url = this.url
        )
    }
    
    /**
     * Convert ApiEventType to domain EventType
     */
    fun ApiEventType.toDomain(): EventType {
        return EventType(
            id = this.id,
            name = this.name,
            description = this.description
        )
    }
    
    /**
     * Convert domain EventType to ApiEventType
     */
    fun EventType.toApi(): ApiEventType {
        return ApiEventType(
            id = this.id,
            name = this.name,
            description = this.description
        )
    }
    
    /**
     * Convert list of TodoEntity to list of domain Event
     */
    fun List<TodoEntity>.toEventDomainList(): List<Event> {
        return this.map { it.toDomain() }
    }
    
    /**
     * Convert list of EventResponse to list of domain Event
     */
    fun List<EventResponse>.toEventResponseDomainList(): List<Event> {
        return this.map { it.toDomain() }
    }
    
    /**
     * Convert list of ApiEventType to list of domain EventType
     */
    fun List<ApiEventType>.toEventTypeDomainList(): List<EventType> {
        return this.map { it.toDomain() }
    }
    
    /**
     * Convert list of ApiEventImage to list of domain EventImage
     */
    fun List<ApiEventImage>.toEventImageDomainList(): List<EventImage> {
        return this.map { it.toDomain() }
    }
} 