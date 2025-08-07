package com.xdien.todoevent.data.dao

import androidx.room.*
import com.xdien.todoevent.data.entity.TodoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TodoDao {
    @Query("SELECT * FROM todos ORDER BY createdAt DESC")
    fun getAllTodos(): Flow<List<TodoEntity>>
    
    @Query("SELECT * FROM todos WHERE id = :id")
    suspend fun getTodoByIdSuspend(id: Long): TodoEntity?
    
    @Query("SELECT * FROM todos WHERE id = :id")
    fun getTodoById(id: Long): Flow<TodoEntity?>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTodo(todo: TodoEntity): Long
    
    @Update
    suspend fun updateTodo(todo: TodoEntity)
    
    @Delete
    suspend fun deleteTodo(todo: TodoEntity)
    
    @Query("DELETE FROM todos WHERE id = :id")
    suspend fun deleteTodoById(id: Long)
    
    @Query("DELETE FROM todos")
    suspend fun deleteAllTodos()
} 