package com.xdien.todoevent.data.api

import okhttp3.MultipartBody
import retrofit2.http.*

// Request model for creating event (matching mock server)
data class CreateEventRequest(
    val title: String,
    val description: String,
    val typeId: Int,
    val startDate: String,
    val location: String
)

// Event response model (matching mock server)
data class EventResponse(
    val id: Int,
    val title: String?,
    val description: String?,
    val typeId: Int?,
    val startDate: String?,
    val location: String?,
    val createdAt: String?,
    val updatedAt: String?,
    val images: List<EventImage>?
)

// Event image model
data class EventImage(
    val id: Int,
    val eventId: Int,
    val originalName: String,
    val filename: String,
    val filePath: String,
    val fileSize: Int,
    val uploadedAt: String,
    val url: String
)

// Event type model (matching mock server)
data class EventType(
    val id: Int,
    val name: String,
    val description: String
)

// Image upload response
data class ImageUploadResponse(
    val eventId: Int,
    val uploadedImages: List<EventImage>,
    val totalImages: Int
)

interface EventApiService {
    /**
     * Get all event types
     * 
     * @return List of available event types
     */
    @GET("event-types")
    suspend fun getEventTypes(): ApiResponse<List<EventType>>
    
    /**
     * Create a new event
     * 
     * @param event The event data to create
     * @return The created event with server-generated ID
     */
    @POST("events")
    suspend fun createEvent(@Body event: CreateEventRequest): ApiResponse<EventResponse>
    
    /**
     * Get all events with optional filtering
     * 
     * @param q Search keyword (optional)
     * @param typeId Filter by event type ID (optional)
     * @return List of events with metadata
     */
    @GET("events")
    suspend fun getEvents(
        @Query("q") keyword: String? = null,
        @Query("typeId") typeId: Int? = null
    ): ApiResponse<Map<String, Any>>
    
    /**
     * Get event by ID
     * 
     * @param id The event ID
     * @return The event details
     */
    @GET("events/{id}")
    suspend fun getEventById(@Path("id") id: Int): ApiResponse<EventResponse>
    
    /**
     * Update an existing event
     * 
     * @param id The event ID
     * @param event The updated event data
     * @return The updated event
     */
    @PUT("events/{id}")
    suspend fun updateEvent(
        @Path("id") id: Int, 
        @Body event: CreateEventRequest
    ): ApiResponse<Map<String, Any>>
    
    /**
     * Delete an event
     * 
     * @param id The event ID to delete
     */
    @DELETE("events/{id}")
    suspend fun deleteEvent(@Path("id") id: Int): ApiResponse<Map<String, Any>>
    
    /**
     * Upload images for an event (max 5 images)
     * 
     * @param eventId The event ID
     * @param images List of image files to upload
     * @return Upload response with image details
     */
    @Multipart
    @POST("events/{eventId}/images")
    suspend fun uploadEventImages(
        @Path("eventId") eventId: Int,
        @Part images: List<MultipartBody.Part>
    ): ApiResponse<ImageUploadResponse>
} 