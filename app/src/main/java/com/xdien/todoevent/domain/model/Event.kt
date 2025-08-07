package com.xdien.todoevent.domain.model

/**
 * Domain model for Event
 * This represents the business logic model for events
 */
data class Event(
    val id: Int = 0,
    val title: String,
    val description: String,
    val eventTypeId: Int,
    val startDate: String,
    val location: String,
    val createdAt: String = "",
    val updatedAt: String? = null,
    val images: List<EventImage> = emptyList()
) {
    /**
     * Check if the event is valid for creation
     */
    fun isValidForCreation(): Boolean {
        return title.isNotBlank() && 
               title.length <= 255 &&
               description.isNotBlank() &&
                eventTypeId > 0 &&
               startDate.isNotBlank() &&
               location.isNotBlank()
    }
    
    /**
     * Check if the event is valid for update
     */
    fun isValidForUpdate(): Boolean {
        return id > 0 &&
               title.isNotBlank() && 
               title.length <= 255 &&
               description.isNotBlank() &&
                eventTypeId > 0 &&
               startDate.isNotBlank() &&
               location.isNotBlank()
    }
    
    /**
     * Check if event can have more images (max 5)
     */
    fun canAddMoreImages(): Boolean {
        return images.size < 5
    }
    
    /**
     * Get number of remaining image slots
     */
    fun getRemainingImageSlots(): Int {
        return (5 - images.size).coerceAtLeast(0)
    }
    
    /**
     * Check if event has images
     */
    fun hasImages(): Boolean {
        return images.isNotEmpty()
    }
    
    /**
     * Get image URLs for display
     */
    fun getImageUrls(): List<String> {
        return images.map { it.url }
    }
    
    /**
     * Get event type name (helper method)
     */
    fun getEventTypeName(eventTypes: List<EventType>): String {
        return eventTypes.find { it.id == eventTypeId }?.name ?: "Unknown"
    }
}

/**
 * Domain model for Event Image
 */
data class EventImage(
    val id: Int = 0,
    val eventId: Int = 0,
    val originalName: String,
    val filename: String,
    val filePath: String?,
    val fileSize: Int,
    val uploadedAt: String,
    val url: String
) {
    /**
     * Get file size in human readable format
     */
    fun getFormattedFileSize(): String {
        return when {
            fileSize < 1024 -> "${fileSize} B"
            fileSize < 1024 * 1024 -> "${fileSize / 1024} KB"
            else -> "${fileSize / (1024 * 1024)} MB"
        }
    }
    
    /**
     * Get file extension
     */
    fun getFileExtension(): String {
        return originalName.substringAfterLast('.', "").lowercase()
    }
}

/**
 * Domain model for Event Type
 */
data class EventType(
    val id: Int,
    val name: String,
    val description: String
) 