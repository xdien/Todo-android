package com.xdien.todoevent.data.api

import retrofit2.http.*

data class TodoResponse(
    val id: Long,
    val title: String,
    val description: String?,
    val thumbnailUrl: String?,
    val galleryImages: List<String>?,
    val eventTime: Long?,
    val eventEndTime: Long?,
    val location: String?,
    val eventType: String?,
    val isCompleted: Boolean,
    val createdAt: String
)

data class EventsResponse(
    val events: List<TodoResponse>,
    val total: Int,
    val filters: Map<String, Any>?
)

interface TodoApiService {
    @GET("events")
    suspend fun getTodos(): ApiResponse<EventsResponse>
    
    @GET("events/{id}")
    suspend fun getTodoById(@Path("id") id: Long): ApiResponse<TodoResponse>
    
    @POST("events")
    suspend fun createTodo(@Body todo: TodoResponse): ApiResponse<TodoResponse>
    
    @PUT("events/{id}")
    suspend fun updateTodo(@Path("id") id: Long, @Body todo: TodoResponse): ApiResponse<TodoResponse>
    
    @DELETE("events/{id}")
    suspend fun deleteTodo(@Path("id") id: Long): ApiResponse<Unit>
} 