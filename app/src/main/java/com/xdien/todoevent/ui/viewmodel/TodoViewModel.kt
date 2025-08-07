package com.xdien.todoevent.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xdien.todoevent.data.entity.TodoEntity
import com.xdien.todoevent.data.entity.TodoWithEventType
import com.xdien.todoevent.data.repository.SyncResult
import com.xdien.todoevent.data.repository.TodoRepository
import com.xdien.todoevent.ui.components.toChipItems
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TodoViewModel @Inject constructor(
    private val todoRepository: TodoRepository
) : ViewModel() {
    
    private val _todos = MutableStateFlow<List<TodoEntity>>(emptyList())
    val todos: StateFlow<List<TodoEntity>> = _todos.asStateFlow()
    
    private val _todosWithEventType = MutableStateFlow<List<TodoWithEventType>>(emptyList())
    val todosWithEventType: StateFlow<List<TodoWithEventType>> = _todosWithEventType.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    // Sync state
    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()
    
    private val _syncResult = MutableStateFlow<SyncResult?>(null)
    val syncResult: StateFlow<SyncResult?> = _syncResult.asStateFlow()
    
    // LiveData for Fragment approach
    private val _todosLiveData = MutableLiveData<List<TodoEntity>>()
    val todosLiveData: LiveData<List<TodoEntity>> = _todosLiveData
    
    private val _isLoadingLiveData = MutableLiveData<Boolean>()
    val isLoadingLiveData: LiveData<Boolean> = _isLoadingLiveData
    
    // For event detail screen
    private val _selectedTodo = MutableStateFlow<TodoEntity?>(null)
    val selectedTodo: StateFlow<TodoEntity?> = _selectedTodo.asStateFlow()
    
    // For chip selection
    private val _selectedChipIds = MutableStateFlow<Set<Long>>(emptySet())
    val selectedChipIds: StateFlow<Set<Long>> = _selectedChipIds.asStateFlow()
    
    private val _chipItems = MutableStateFlow<List<com.xdien.todoevent.ui.components.ChipItem>>(emptyList())
    val chipItems: StateFlow<List<com.xdien.todoevent.ui.components.ChipItem>> = _chipItems.asStateFlow()
    
    init {
        loadTodos()
        loadTodosWithEventType()
        // Add sample events for testing
        addSampleEvents()
        // Initialize chip items
        updateChipItems()
    }
    
    fun loadTodos() {
        viewModelScope.launch {
            _isLoading.value = true
            _isLoadingLiveData.value = true
            try {
                todoRepository.getAllTodos().collect { todoList ->
                    _todos.value = todoList
                    _todosLiveData.value = todoList
                }
            } finally {
                _isLoading.value = false
                _isLoadingLiveData.value = false
            }
        }
    }
    
    fun loadTodosWithEventType() {
        viewModelScope.launch {
            try {
                todoRepository.getAllTodosWithEventType().collect { todosWithTypes ->
                    _todosWithEventType.value = todosWithTypes
                }
            } catch (e: Exception) {
                // Fallback to regular todos if relationship query fails
                _todosWithEventType.value = emptyList()
            }
        }
    }
    
    /**
     * Sync data with server - this is the main method for pull-to-refresh functionality
     */
    fun syncWithServer() {
        viewModelScope.launch {
            _isSyncing.value = true
            _syncResult.value = null
            
            try {
                val result = todoRepository.syncWithServer()
                _syncResult.value = result
                
                // Reload todos after sync
                loadTodos()
            } finally {
                _isSyncing.value = false
            }
        }
    }
    
    /**
     * Clear sync result - call this to reset sync status
     */
    fun clearSyncResult() {
        _syncResult.value = null
    }
    
    fun addTodo(title: String, description: String? = null, eventTypeId: Long? = null) {
        viewModelScope.launch {
            val todo = TodoEntity(
                title = title,
                description = description,
                eventTypeId = eventTypeId
            )
            todoRepository.insertTodo(todo)
        }
    }
    
    fun updateTodo(todo: TodoEntity) {
        viewModelScope.launch {
            todoRepository.updateTodo(todo)
        }
    }
    
    fun deleteTodo(todo: TodoEntity) {
        viewModelScope.launch {
            todoRepository.deleteTodo(todo)
        }
    }
    
    // Get todo by ID for detail screen
    fun getTodoById(id: Long): Flow<TodoEntity?> {
        return todoRepository.getTodoById(id)
    }
    
    // Load todo by ID and update selectedTodo
    fun loadTodoById(id: Long) {
        viewModelScope.launch {
            todoRepository.getTodoById(id).collect { todo ->
                _selectedTodo.value = todo
            }
        }
    }
    
    // Update todo with new data
    fun updateTodoWithNewData(
        id: Long,
        title: String,
        description: String?,
        thumbnailUrl: String?,
        galleryImages: List<String>?,
        eventTime: Long?,
        eventEndTime: Long?,
        location: String?,
        eventTypeId: Long?
    ) {
        viewModelScope.launch {
            val updatedTodo = TodoEntity(
                id = id,
                title = title,
                description = description,
                thumbnailUrl = thumbnailUrl,
                galleryImages = galleryImages,
                eventTime = eventTime,
                eventEndTime = eventEndTime,
                location = location,
                eventTypeId = eventTypeId,
                isCompleted = false,
                updatedAt = System.currentTimeMillis()
            )
            todoRepository.updateTodo(updatedTodo)
        }
    }
    
    fun fetchFromApi() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val apiTodos = todoRepository.fetchTodosFromApi()
                apiTodos.forEach { todo ->
                    todoRepository.insertTodo(todo)
                }
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Refresh todos - this method is called for pull-to-refresh
     * It syncs with server and updates local data
     */
    fun refreshTodos() {
        syncWithServer()
    }
    
    private fun addSampleEvents() {
        viewModelScope.launch {
            val sampleEvents = listOf(
                TodoEntity(
                    title = "Hội thảo Công nghệ 2024",
                    description = "Hội thảo về các xu hướng công nghệ mới nhất trong năm 2024. Sự kiện sẽ quy tụ các chuyên gia hàng đầu trong lĩnh vực AI, Machine Learning và Blockchain.",
                    thumbnailUrl = "https://images.unsplash.com/photo-1515187029135-18ee286d815b?w=400",
                    galleryImages = listOf(
                        "https://images.unsplash.com/photo-1515187029135-18ee286d815b?w=800",
                        "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=800",
                        "https://images.unsplash.com/photo-1552664730-d307ca884978?w=800"
                    ),
                    eventTime = System.currentTimeMillis() + 86400000, // Tomorrow
                    eventEndTime = System.currentTimeMillis() + 86400000 + 28800000, // Tomorrow + 8 hours
                    location = "Trung tâm Hội nghị Quốc gia, Hà Nội",
                    eventTypeId = null // No event type for sample data
                ),
                TodoEntity(
                    title = "Workshop Lập trình Android",
                    description = "Học cách phát triển ứng dụng Android với Kotlin và Jetpack Compose. Workshop thực hành từ cơ bản đến nâng cao.",
                    thumbnailUrl = "https://images.unsplash.com/photo-1517077304055-6e89abbf09b0?w=400",
                    galleryImages = listOf(
                        "https://images.unsplash.com/photo-1517077304055-6e89abbf09b0?w=800",
                        "https://images.unsplash.com/photo-1461749280684-dccba630e2f6?w=800"
                    ),
                    eventTime = System.currentTimeMillis() + 172800000, // Day after tomorrow
                    eventEndTime = System.currentTimeMillis() + 172800000 + 14400000, // Day after tomorrow + 4 hours
                    location = "FPT Software, TP.HCM",
                    eventTypeId = null // No event type for sample data
                ),
                TodoEntity(
                    title = "Meetup Cộng đồng Developer",
                    description = "Gặp gỡ và chia sẻ kinh nghiệm với các developer trong cộng đồng. Networking và học hỏi từ các chuyên gia.",
                    thumbnailUrl = "https://images.unsplash.com/photo-1522202176988-66273c2fd55f?w=400",
                    galleryImages = listOf(
                        "https://images.unsplash.com/photo-1522202176988-66273c2fd55f?w=800",
                        "https://images.unsplash.com/photo-1552664730-d307ca884978?w=800"
                    ),
                    eventTime = System.currentTimeMillis() + 259200000, // 3 days later
                    eventEndTime = System.currentTimeMillis() + 259200000 + 7200000, // 3 days later + 2 hours
                    location = "WeWork, Quận 1, TP.HCM",
                    eventTypeId = null // No event type for sample data
                ),
                TodoEntity(
                    title = "Hackathon 2024",
                    description = "Cuộc thi lập trình 48 giờ liên tục. Tạo ra những sản phẩm công nghệ sáng tạo và giải quyết các vấn đề thực tế.",
                    thumbnailUrl = "https://images.unsplash.com/photo-1552664730-d307ca884978?w=400",
                    galleryImages = listOf(
                        "https://images.unsplash.com/photo-1552664730-d307ca884978?w=800",
                        "https://images.unsplash.com/photo-1515187029135-18ee286d815b?w=800",
                        "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=800",
                        "https://images.unsplash.com/photo-1461749280684-dccba630e2f6?w=800"
                    ),
                    eventTime = System.currentTimeMillis() + 604800000, // 1 week later
                    eventEndTime = System.currentTimeMillis() + 604800000 + 172800000, // 1 week later + 48 hours
                    location = "Đại học Bách Khoa Hà Nội",
                    eventTypeId = null // No event type for sample data
                )
            )
            
            sampleEvents.forEach { event ->
                todoRepository.insertTodo(event)
            }
        }
    }
    
    // Update chip items when todos change
    private fun updateChipItems() {
        viewModelScope.launch {
            todos.collect { todoList ->
                val chipItems = todoList.toChipItems(_selectedChipIds.value)
                _chipItems.value = chipItems
            }
        }
    }
    
    // Handle chip selection
    fun selectChip(chipId: String, singleSelection: Boolean = true) {
        val id = chipId.toLongOrNull() ?: return
        
        if (singleSelection) {
            _selectedChipIds.value = setOf(id)
        } else {
            val currentSelected = _selectedChipIds.value.toMutableSet()
            if (currentSelected.contains(id)) {
                currentSelected.remove(id)
            } else {
                currentSelected.add(id)
            }
            _selectedChipIds.value = currentSelected
        }
        
        // Update chip items with new selection
        updateChipItems()
    }
    
    // Get selected todos
    fun getSelectedTodos(): List<TodoEntity> {
        return todos.value.filter { it.id in _selectedChipIds.value }
    }
    
    // Clear all selections
    fun clearChipSelection() {
        _selectedChipIds.value = emptySet()
        updateChipItems()
    }
} 