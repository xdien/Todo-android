package com.xdien.todoevent.data.repository

import com.xdien.todoevent.data.api.EventApiService
import com.xdien.todoevent.data.dao.TodoDao
import com.xdien.todoevent.data.mapper.EventMapper.toCreateRequest
import com.xdien.todoevent.data.mapper.EventMapper.toDomain
import com.xdien.todoevent.data.mapper.EventMapper.toEventDomainList
import com.xdien.todoevent.data.mapper.EventMapper.toEventImageDomainList
import com.xdien.todoevent.data.mapper.EventMapper.toEventTypeDomainList
import com.xdien.todoevent.data.mapper.EventMapper.toEntity
import com.xdien.todoevent.domain.model.Event
import com.xdien.todoevent.domain.model.EventType
import com.xdien.todoevent.domain.model.EventImage
import com.xdien.todoevent.domain.repository.EventRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of EventRepository
 * 
 * This class implements the repository pattern and coordinates between
 * local database and remote API data sources.
 */
@Singleton
class EventRepositoryImpl @Inject constructor(
    private val todoDao: TodoDao,
    private val eventApiService: EventApiService
) : EventRepository {
    
    override suspend fun createEvent(event: Event): Event {
        return try {
            // First, try to create event via API
            val apiRequest = event.toCreateRequest()
            val apiResponse = eventApiService.createEvent(apiRequest)
            
            if (apiResponse.success) {
                val createdEvent = apiResponse.data.toDomain()
                
                // Save to local database
                val entity = createdEvent.toEntity()
                val localId = todoDao.insertTodo(entity)
                
                // Return the created event with local ID
                createdEvent.copy(id = localId.toInt())
            } else {
                throw Exception(apiResponse.message)
            }
        } catch (e: Exception) {
            // If API fails, save locally only
            val entity = event.toEntity()
            val localId = todoDao.insertTodo(entity)
            event.copy(id = localId.toInt())
        }
    }
    
    override fun getAllEvents(): Flow<List<Event>> {
        return todoDao.getAllTodos().map { entities ->
            entities.toEventDomainList()
        }
    }
    
    override fun getEventById(id: Int): Flow<Event?> {
        return todoDao.getTodoById(id.toLong()).map { entity ->
            entity?.toDomain()
        }
    }
    
    override suspend fun getEventFromApi(id: Int): Event? {
        return try {
            val apiResponse = eventApiService.getEventById(id)
            if (apiResponse.success) {
                val event = apiResponse.data.toDomain()
                
                // Save to local database for caching
                val entity = event.toEntity()
                todoDao.insertTodo(entity)
                
                event
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
    
    override suspend fun updateEvent(event: Event) {
        try {
            // First, try to update via API
            val apiRequest = event.toCreateRequest()
            val apiResponse = eventApiService.updateEvent(event.id, apiRequest)
            
            if (apiResponse.success) {
                // Update local database only if API succeeds
                val entity = event.toEntity()
                todoDao.updateTodo(entity)
            } else {
                throw Exception(apiResponse.message)
            }
        } catch (e: Exception) {
            // If API fails, don't update local database
            throw e // Re-throw to inform the caller
        }
    }
    
    override suspend fun deleteEvent(event: Event) {
        val entity = event.toEntity()
        todoDao.deleteTodo(entity)
        
        // Try to delete via API as well
        try {
            val apiResponse = eventApiService.deleteEvent(event.id)
            if (!apiResponse.success) {
                throw Exception(apiResponse.message)
            }
        } catch (e: Exception) {
            // API delete failed, but local delete succeeded
            // Could log this error or handle it as needed
        }
    }
    
    override suspend fun deleteEventById(id: Int) {
        todoDao.deleteTodoById(id.toLong())
        
        // Try to delete via API as well
        try {
            val apiResponse = eventApiService.deleteEvent(id)
            if (!apiResponse.success) {
                throw Exception(apiResponse.message)
            }
        } catch (e: Exception) {
            // API delete failed, but local delete succeeded
            // Could log this error or handle it as needed
        }
    }
    
    override suspend fun fetchEventsFromApi(keyword: String?, typeId: Int?): List<Event> {
        return try {
            val apiResponse = eventApiService.getEvents(keyword, typeId)
            
            if (apiResponse.success) {
                val eventsData = apiResponse.data["events"] as? List<Map<String, Any>>
                val domainEvents = eventsData?.map { eventMap ->
                    // Convert map to EventResponse then to domain
                    val eventResponse = mapToEventResponse(eventMap)
                    eventResponse.toDomain()
                } ?: emptyList()
                
                // Save to local database
                domainEvents.forEach { event ->
                    val entity = event.toEntity()
                    todoDao.insertTodo(entity)
                }
                
                domainEvents
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    override suspend fun getEventTypes(): List<EventType> {
        return try {
            val apiResponse = eventApiService.getEventTypes()
            if (apiResponse.success) {
                apiResponse.data.toEventTypeDomainList()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    override suspend fun uploadEventImages(eventId: Int, imageFiles: List<File>): List<EventImage> {
        return try {
            // Prepare multipart files
            val multipartFiles = imageFiles.map { file ->
                val requestBody = file.asRequestBody("image/*".toMediaTypeOrNull())
                MultipartBody.Part.createFormData("images", file.name, requestBody)
            }
            
            val apiResponse = eventApiService.uploadEventImages(eventId, multipartFiles)
            
            if (apiResponse.success) {
                val uploadedImages = apiResponse.data.uploadedImages.toEventImageDomainList()
                
                // Update local event with new images
                val currentEvent = getEventById(eventId).first()
                currentEvent?.let { event ->
                    val updatedEvent = event.copy(images = event.images + uploadedImages)
                    val entity = updatedEvent.toEntity()
                    todoDao.updateTodo(entity)
                }
                
                uploadedImages
            } else {
                throw Exception(apiResponse.message)
            }
        } catch (e: Exception) {
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
     * Helper function to convert map to EventResponse
     */
    private fun mapToEventResponse(eventMap: Map<String, Any>): com.xdien.todoevent.data.api.EventResponse {
        val images = (eventMap["images"] as? List<Map<String, Any>>)?.map { imageMap ->
            com.xdien.todoevent.data.api.EventImage(
                id = (imageMap["id"] as? Number)?.toInt() ?: 0,
                eventId = (imageMap["eventId"] as? Number)?.toInt() ?: 0,
                originalName = imageMap["originalName"] as? String ?: "",
                filename = imageMap["filename"] as? String ?: "",
                filePath = imageMap["filePath"] as? String ?: "",
                fileSize = (imageMap["fileSize"] as? Number)?.toInt() ?: 0,
                uploadedAt = imageMap["uploadedAt"] as? String ?: "",
                url = imageMap["url"] as? String ?: ""
            )
        } ?: emptyList()
        
        return com.xdien.todoevent.data.api.EventResponse(
            id = (eventMap["id"] as? Number)?.toInt() ?: 0,
            title = eventMap["title"] as? String ?: "",
            description = eventMap["description"] as? String ?: "",
            typeId = (eventMap["typeId"] as? Number)?.toInt() ?: 1,
            startDate = eventMap["startDate"] as? String ?: "",
            location = eventMap["location"] as? String ?: "",
            createdAt = eventMap["createdAt"] as? String ?: "",
            updatedAt = eventMap["updatedAt"] as? String,
            images = images
        )
    }
} 