package com.xdien.todoevent.data.repository

import android.util.Log
import com.xdien.todoevent.common.SharedPreferencesHelper
import com.xdien.todoevent.data.api.ApiResponse
import com.xdien.todoevent.data.api.EventApiService
import com.xdien.todoevent.data.api.EventImage as ApiEventImage
import com.xdien.todoevent.data.api.EventResponse
import com.xdien.todoevent.data.api.EventType as ApiEventType
import com.xdien.todoevent.data.api.CreateEventRequest
import com.xdien.todoevent.data.dao.EventTypeDao
import com.xdien.todoevent.data.entity.EventTypeEntity
import com.xdien.todoevent.domain.model.Event
import com.xdien.todoevent.domain.model.EventImage
import com.xdien.todoevent.domain.model.EventType
import com.xdien.todoevent.domain.repository.EventRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
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
                
                // Ensure EventType exists in local database
                ensureEventTypeExists(createdEvent.eventTypeId)
                
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
            Log.d("EventRepositoryImpl", "Fetching events with keyword: $keyword, typeId: $typeId")
            val apiResponse = eventApiService.getEvents(keyword, typeId)
            
            Log.d("EventRepositoryImpl", "API response success: ${apiResponse.success}")
            Log.d("EventRepositoryImpl", "API response message: ${apiResponse.message}")
            
            if (apiResponse.success) {
                Log.d("EventRepositoryImpl", "API data events count: ${apiResponse.data.events.size}")
                val events = apiResponse.data.events.map { eventResponse ->
                    Log.d("EventRepositoryImpl", "Mapping event: ${eventResponse.title} (ID: ${eventResponse.id})")
                    eventResponse.toDomain()
                }
                
                Log.d("EventRepositoryImpl", "Mapped ${events.size} events to domain models")
                
                // Ensure all EventTypes exist in local database
                events.forEach { event ->
                    ensureEventTypeExists(event.eventTypeId)
                }
                
                flow { 
                    Log.d("EventRepositoryImpl", "Emitting ${events.size} events to flow")
                    emit(events) 
                }
            } else {
                Log.e("EventRepositoryImpl", "API call failed: ${apiResponse.message}")
                // Return empty list if API fails
                flow { emit(emptyList()) }
            }
        } catch (e: Exception) {
            Log.e("EventRepositoryImpl", "Exception during getEvents", e)
            // Return empty list if API fails
            flow { emit(emptyList()) }
        }
    }

    override suspend fun getEventById(id: Int): Flow<Event?> {
        return try {
            val apiResponse = eventApiService.getEventById(id)
            
            if (apiResponse.success) {
                val event = apiResponse.data.toDomain()
                
                // Ensure EventType exists in local database
                ensureEventTypeExists(event.eventTypeId)
                
                flow { emit(event) }
            } else {
                // Return null if API fails
                flow { emit(null) }
            }
        } catch (e: Exception) {
            // Return null if API fails
            flow { emit(null) }
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
                    ensureEventTypeExists(updatedEvent.eventTypeId)
                    
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
            
            if (!apiResponse.success) {
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
                        Log.w("EventRepositoryImpl", "Skipping image with invalid filePath: ${apiImage.originalName}")
                        null
                    }
                }
                
                Log.d("EventRepositoryImpl", "Processed ${uploadedImages.size} uploaded images")
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
        // For now, we'll just log the request
        Log.d("EventRepositoryImpl", "Delete image request for event $eventId, image $imageId")
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
}

// Extension functions for mapping between API and Domain models
private fun EventResponse.toDomain(): Event {
    Log.d("EventRepositoryImpl", "Mapping EventResponse to Domain: ${this.title} (ID: ${this.id})")
    return Event(
        id = this.id,
        title = this.title ?: "Untitled Event",
        description = this.description ?: "",
        eventTypeId = this.eventTypeId,
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
        originalName = this.originalName ?: "unknown.jpg",
        filename = this.filename ?: "unknown.jpg",
        filePath = this.filePath ?: "",
        fileSize = this.fileSize ?: 0,
        uploadedAt = this.uploadedAt ?: "",
        url = "" // Will be set by repository when creating full URL
    )
}

private fun ApiEventType.toDomain(): EventType {
    return EventType(
        id = this.id,
        name = this.name,
        description = this.description
    )
} 