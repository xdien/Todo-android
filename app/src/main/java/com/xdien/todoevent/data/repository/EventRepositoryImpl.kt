package com.xdien.todoevent.data.repository

import android.util.Log
import com.xdien.todoevent.common.SharedPreferencesHelper
import com.xdien.todoevent.data.api.ApiResponse
import com.xdien.todoevent.data.api.EventApiService
import com.xdien.todoevent.data.api.EventImage as ApiEventImage
import com.xdien.todoevent.data.api.EventResponse
import com.xdien.todoevent.data.api.EventType as ApiEventType
import com.xdien.todoevent.data.api.CreateEventRequest
import com.xdien.todoevent.data.dao.TodoDao
import com.xdien.todoevent.data.dao.EventTypeDao
import com.xdien.todoevent.data.entity.TodoEntity
import com.xdien.todoevent.data.entity.EventTypeEntity
import com.xdien.todoevent.domain.model.Event
import com.xdien.todoevent.domain.model.EventImage
import com.xdien.todoevent.domain.model.EventType
import com.xdien.todoevent.domain.repository.EventRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first
import okhttp3.MultipartBody
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EventRepositoryImpl @Inject constructor(
    private val eventApiService: EventApiService,
    private val todoDao: TodoDao,
    private val eventTypeDao: EventTypeDao,
    private val sharedPreferencesHelper: SharedPreferencesHelper
) : EventRepository {

    override suspend fun createEvent(event: Event): Event {
        return try {
            val request = CreateEventRequest(
                title = event.title,
                description = event.description,
                eventTypeId = event.eventTypeId,
                startDate = event.startDate,
                location = event.location
            )
            
            val apiResponse = eventApiService.createEvent(request)
            
            if (apiResponse.success) {
                val createdEvent = apiResponse.data.toDomain()
                
                // Ensure EventType exists in local database before saving event
                val eventTypeExists = ensureEventTypeExists(createdEvent.eventTypeId)
                
                // Save to local database
                val entity = createdEvent.toEntity(eventTypeExists)
                todoDao.insertTodo(entity)
                
                // Return the created event with SERVER ID
                createdEvent
            } else {
                throw Exception(apiResponse.message)
            }
        } catch (e: Exception) {
            throw e
        }
    }

    override suspend fun getEvents(keyword: String?, typeId: Int?): Flow<List<Event>> {
        return try {
            val apiResponse = eventApiService.getEvents(keyword, typeId)
            
            if (apiResponse.success) {
                val events = (apiResponse.data["events"] as? List<Map<String, Any>>)?.map { eventMap ->
                    eventMap.toEventResponse().toDomain()
                } ?: emptyList()
                
                // Ensure all EventTypes exist in local database
                events.forEach { event ->
                    ensureEventTypeExists(event.eventTypeId)
                }
                
                // Save to local database
                events.forEach { event ->
                    val entity = event.toEntity()
                    todoDao.insertTodo(entity)
                }
                
                kotlinx.coroutines.flow.flow { emit(events) }
            } else {
                // Fallback to local database
                todoDao.getAllTodos().map { entities ->
                    entities.map { it.toDomain() }
                }
            }
        } catch (e: Exception) {
            // Fallback to local database
            todoDao.getAllTodos().map { entities ->
                entities.map { it.toDomain() }
            }
        }
    }

    override suspend fun getEventById(id: Int): Flow<Event?> {
        return try {
            val apiResponse = eventApiService.getEventById(id)
            
            if (apiResponse.success) {
                val event = apiResponse.data.toDomain()
                
                // Ensure EventType exists in local database
                ensureEventTypeExists(event.eventTypeId)
                
                // Save to local database
                val entity = event.toEntity()
                todoDao.insertTodo(entity)
                
                kotlinx.coroutines.flow.flow { emit(event) }
            } else {
                // Fallback to local database
                todoDao.getTodoById(id.toLong()).map { entity ->
                    entity?.toDomain()
                }
            }
        } catch (e: Exception) {
            // Fallback to local database
            todoDao.getTodoById(id.toLong()).map { entity ->
                entity?.toDomain()
            }
        }
    }

    override suspend fun updateEvent(id: Int, title: String, description: String, typeId: Int, startDate: String, location: String): Result<Event> {
        return try {
            val request = CreateEventRequest(
                title = title,
                description = description,
                eventTypeId = typeId,
                startDate = startDate,
                location = location
            )
            
            val apiResponse = eventApiService.updateEvent(id, request)
            
            if (apiResponse.success) {
                // Get the updated event from API
                val updatedEventResponse = eventApiService.getEventById(id)
                if (updatedEventResponse.success) {
                    val updatedEvent = updatedEventResponse.data.toDomain()
                    
                    // Ensure EventType exists in local database
                    val eventTypeExists = ensureEventTypeExists(updatedEvent.eventTypeId)
                    
                    // Update local database
                    val entity = updatedEvent.toEntity(eventTypeExists)
                    todoDao.updateTodo(entity)
                    
                    Result.success(updatedEvent)
                } else {
                    Result.failure(Exception("Failed to get updated event"))
                }
            } else {
                Result.failure(Exception(apiResponse.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteEvent(id: Int) {
        try {
            val apiResponse = eventApiService.deleteEvent(id)
            
            if (apiResponse.success) {
                // Delete from local database
                todoDao.deleteTodoById(id.toLong())
            } else {
                throw Exception(apiResponse.message)
            }
        } catch (e: Exception) {
            throw e
        }
    }

    override suspend fun getEventTypes(): Result<List<EventType>> {
        return try {
            val apiResponse = eventApiService.getEventTypes()
            
            if (apiResponse.success) {
                val eventTypes = apiResponse.data.map { it.toDomain() }
                
                // Save EventTypes to local database
                eventTypes.forEach { eventType ->
                    val entity = EventTypeEntity(
                        id = eventType.id.toLong(),
                        name = eventType.name
                    )
                    eventTypeDao.insertEventType(entity)
                }
                
                Result.success(eventTypes)
            } else {
                Result.failure(Exception(apiResponse.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun uploadEventImages(eventId: Int, imageFiles: List<File>): List<EventImage> {
        return try {
            Log.d("EventRepositoryImpl", "Starting upload for event $eventId with ${imageFiles.size} images")
            
            // Prepare multipart files
            val multipartFiles = imageFiles.map { file ->
                Log.d("EventRepositoryImpl", "Preparing multipart file: ${file.name}, size: ${file.length()} bytes")
                val requestBody = file.asRequestBody("image/*".toMediaTypeOrNull())
                MultipartBody.Part.createFormData("images", file.name, requestBody)
            }
            
            Log.d("EventRepositoryImpl", "Calling API to upload images for event $eventId")
            Log.d("EventRepositoryImpl", "Multipart files count: ${multipartFiles.size}")
            Log.d("EventRepositoryImpl", "About to call uploadEventImages API endpoint")
            val apiResponse = eventApiService.uploadEventImages(eventId, multipartFiles)
            Log.d("EventRepositoryImpl", "API response received: success=${apiResponse.success}, message=${apiResponse.message}")
            Log.d("EventRepositoryImpl", "API response data: ${apiResponse.data}")
            Log.d("EventRepositoryImpl", "API response data type: ${apiResponse.data?.javaClass?.simpleName}")
            
            if (apiResponse.success) {
                Log.d("EventRepositoryImpl", "API call successful, processing uploaded images")
                Log.d("EventRepositoryImpl", "Response data keys: ${apiResponse.data?.javaClass?.declaredFields?.map { it.name }}")
                Log.d("EventRepositoryImpl", "Uploaded images from API: ${apiResponse.data.uploadedImages}")
                
                val uploadedImages = apiResponse.data.uploadedImages.mapNotNull { apiImage ->
                    Log.d("EventRepositoryImpl", "Processing API image: $apiImage")
                    // Create full URL using base URL + filePath
                    val fullUrl = sharedPreferencesHelper.createFullImageUrl(apiImage.filePath)
                    Log.d("EventRepositoryImpl", "Created full URL: $fullUrl for filePath: ${apiImage.filePath}")
                    
                    if (fullUrl != null) {
                        apiImage.toDomain().copy(url = fullUrl)
                    } else {
                        Log.w("EventRepositoryImpl", "Skipping image with null filePath: ${apiImage.originalName}")
                        null
                    }
                }
                
                Log.d("EventRepositoryImpl", "Processed ${uploadedImages.size} uploaded images")
                
                // Update local event with new images
                Log.d("EventRepositoryImpl", "Updating local database with new images")
                val currentEvent = getEventById(eventId).first()
                currentEvent?.let { event ->
                    val updatedEvent = event.copy(images = event.images + uploadedImages)
                    val entity = updatedEvent.toEntity()
                    todoDao.updateTodo(entity)
                    Log.d("EventRepositoryImpl", "Local database updated successfully")
                } ?: run {
                    Log.w("EventRepositoryImpl", "Event $eventId not found in local database")
                }
                
                Log.d("EventRepositoryImpl", "Upload completed successfully, returning ${uploadedImages.size} images")
                uploadedImages
            } else {
                Log.e("EventRepositoryImpl", "API call failed: ${apiResponse.message}")
                throw Exception(apiResponse.message)
            }
        } catch (e: Exception) {
            Log.e("EventRepositoryImpl", "Exception during upload for event $eventId", e)
            throw e
        }
    }
    
    override suspend fun deleteEventImage(eventId: Int, imageId: Int) {
        // This would require a new API endpoint for deleting individual images
        // For now, we'll just update the local event
        val currentEvent = getEventById(eventId).first()
        currentEvent?.let { event ->
            val updatedImages = event.images.filter { it.id != imageId }
            val updatedEvent = event.copy(images = updatedImages)
            val entity = updatedEvent.toEntity()
            todoDao.updateTodo(entity)
        }
    }
    
    /**
     * Ensure EventType exists in local database
     * If not, create a default one with the given ID
     * Returns true if EventType exists or was created successfully, false otherwise
     */
    private suspend fun ensureEventTypeExists(typeId: Int): Boolean {
        return try {
            val existingEventType = eventTypeDao.getEventTypeById(typeId.toLong())
            if (existingEventType == null) {
                // Create a default EventType with the given ID
                val defaultEventType = EventTypeEntity(
                    id = typeId.toLong(),
                    name = "Event Type $typeId"
                )
                eventTypeDao.insertEventType(defaultEventType)
                true
            } else {
                true
            }
        } catch (e: Exception) {
            // If there's an error, try to create a default EventType
            try {
                val defaultEventType = EventTypeEntity(
                    id = typeId.toLong(),
                    name = "Event Type $typeId"
                )
                eventTypeDao.insertEventType(defaultEventType)
                true
            } catch (insertError: Exception) {
                // If insert fails, return false to indicate EventType doesn't exist
                false
            }
        }
    }
    
    /**
     * Helper function to convert map to EventResponse
     */
    private fun Map<String, Any>.toEventResponse(): EventResponse {
        return EventResponse(
            id = (this["id"] as? Number)?.toInt() ?: 0,
            title = this["title"] as? String,
            description = this["description"] as? String,
            eventTypeId = (this["typeId"] as? Number)?.toInt(),
            startDate = this["startDate"] as? String,
            location = this["location"] as? String,
            createdAt = this["createdAt"] as? String,
            updatedAt = this["updatedAt"] as? String,
            images = (this["images"] as? List<Map<String, Any>>)?.map { imageMap ->
                Log.d("EventRepositoryImpl", "Mapping image: $imageMap")
                val mappedImage = ApiEventImage(
                    id = (imageMap["id"] as? Number)?.toInt() ?: 0,
                    eventId = (imageMap["event_id"] as? Number)?.toInt() ?: 0,
                    originalName = imageMap["original_name"] as? String ?: "",
                    filename = imageMap["filename"] as? String ?: "",
                    filePath = imageMap["file_path"] as? String,
                    fileSize = (imageMap["file_size"] as? Number)?.toInt() ?: 0,
                    uploadedAt = imageMap["uploaded_at"] as? String ?: "",
                    url = imageMap["url"] as? String
                )
                Log.d("EventRepositoryImpl", "Mapped image: id=${mappedImage.id}, filePath=${mappedImage.filePath}, url=${mappedImage.url}")
                mappedImage
            }
        )
    }
}

// Extension functions for mapping between API and Domain models
private fun EventResponse.toDomain(): Event {
    return Event(
        id = this.id,
        title = this.title ?: "",
        description = this.description ?: "",
        eventTypeId = this.eventTypeId ?: 0,
        startDate = this.startDate ?: "",
        location = this.location ?: "",
        createdAt = this.createdAt ?: "",
        updatedAt = this.updatedAt,
        images = this.images?.map { it.toDomain() } ?: emptyList()
    )
}

private fun ApiEventImage.toDomain(): EventImage {
    return EventImage(
        id = this.id,
        eventId = this.eventId,
        originalName = this.originalName,
        filename = this.filename,
        filePath = this.filePath,
        fileSize = this.fileSize,
        uploadedAt = this.uploadedAt,
        url = this.url ?: ""
    )
}

private fun ApiEventType.toDomain(): EventType {
    return EventType(
        id = this.id,
        name = this.name,
        description = this.description
    )
}

// Simple mapping for TodoEntity (since it doesn't have all Event fields)
private fun Event.toEntity(eventTypeExists: Boolean = true): TodoEntity {
    return TodoEntity(
        id = this.id.toLong(),
        title = this.title,
        description = this.description,
        location = this.location,
        eventTypeId = if (eventTypeExists) this.eventTypeId.toLong() else null, // Use null if EventType doesn't exist
        eventTime = this.startDate.hashCode().toLong(), // Simple conversion
        createdAt = System.currentTimeMillis(),
        updatedAt = System.currentTimeMillis()
    )
}

private fun TodoEntity.toDomain(): Event {
    return Event(
        id = this.id.toInt(),
        title = this.title,
        description = this.description ?: "",
        eventTypeId = this.eventTypeId?.toInt() ?: 0,
        startDate = this.eventTime?.toString() ?: "",
        location = this.location ?: "",
        createdAt = this.createdAt.toString(),
        updatedAt = this.updatedAt.toString(),
        images = emptyList() // TodoEntity doesn't store images
    )
} 