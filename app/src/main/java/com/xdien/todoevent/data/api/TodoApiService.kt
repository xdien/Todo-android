package com.xdien.todoevent.data.api

import retrofit2.http.*

data class TodoResponse(
    val id: Long,
    val title: String,
    val description: String?,
    val isCompleted: Boolean,
    val createdAt: String
)

interface TodoApiService {
    @GET("todos")
    suspend fun getTodos(): List<TodoResponse>
    
    @GET("todos/{id}")
    suspend fun getTodoById(@Path("id") id: Long): TodoResponse
    
    @POST("todos")
    suspend fun createTodo(@Body todo: TodoResponse): TodoResponse
    
    @PUT("todos/{id}")
    suspend fun updateTodo(@Path("id") id: Long, @Body todo: TodoResponse): TodoResponse
    
    @DELETE("todos/{id}")
    suspend fun deleteTodo(@Path("id") id: Long)
} 