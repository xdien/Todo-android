package com.xdien.todoevent.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xdien.todoevent.domain.model.Event
import com.xdien.todoevent.domain.model.EventType
import com.xdien.todoevent.domain.usecase.CreateEventUseCase
import com.xdien.todoevent.domain.usecase.GetEventTypesUseCase
import com.xdien.todoevent.domain.usecase.UploadEventImagesUseCase
import com.xdien.todoevent.domain.usecase.UpdateEventUseCase
import com.xdien.todoevent.domain.repository.EventRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class EventFormViewModel @Inject constructor(
    private val createEventUseCase: CreateEventUseCase,
    private val getEventTypesUseCase: GetEventTypesUseCase,
    private val uploadEventImagesUseCase: UploadEventImagesUseCase,
    private val updateEventUseCase: UpdateEventUseCase,
    private val eventRepository: EventRepository
) : ViewModel() {

    // UI State
    private val _uiState = MutableStateFlow(EventFormUiState())
    val uiState: StateFlow<EventFormUiState> = _uiState.asStateFlow()

    // Event Types
    private val _eventTypes = MutableStateFlow<List<EventType>>(emptyList())
    val eventTypes: StateFlow<List<EventType>> = _eventTypes.asStateFlow()

    // Selected Images
    private val _selectedImages = MutableStateFlow<List<File>>(emptyList())
    val selectedImages: StateFlow<List<File>> = _selectedImages.asStateFlow()

    // Current event for editing
    private var currentEvent: Event? = null
    private var isEditMode = false

    init {
        loadEventTypes()
    }

    /**
     * Load event for editing
     */
    fun loadEventForEdit(eventId: Long) {
        isEditMode = true
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                val event = eventRepository.getEventById(eventId.toInt()).first()
                if (event != null) {
                    currentEvent = event
                    _uiState.value = _uiState.value.copy(
                        title = event.title,
                        description = event.description,
                        typeId = event.typeId,
                        startDate = event.startDate,
                        location = event.location,
                        isLoading = false,
                        isEditMode = true
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Không tìm thấy sự kiện"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Lỗi khi tải sự kiện: ${e.message}"
                )
            }
        }
    }

    /**
     * Load available event types
     */
    private fun loadEventTypes() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            getEventTypesUseCase().onSuccess { types ->
                _eventTypes.value = types
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = null
                )
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = error.message ?: "Không thể tải danh sách loại sự kiện"
                )
            }
        }
    }

    /**
     * Update form fields
     */
    fun updateTitle(title: String) {
        _uiState.value = _uiState.value.copy(title = title)
    }

    fun updateDescription(description: String) {
        _uiState.value = _uiState.value.copy(description = description)
    }

    fun updateTypeId(typeId: Int) {
        _uiState.value = _uiState.value.copy(typeId = typeId)
    }

    fun updateStartDate(startDate: String) {
        _uiState.value = _uiState.value.copy(startDate = startDate)
    }

    fun updateLocation(location: String) {
        _uiState.value = _uiState.value.copy(location = location)
    }

    /**
     * Add images to selection (only for new events)
     */
    fun addImages(files: List<File>) {
        if (isEditMode) return // Don't allow image changes in edit mode
        
        val currentImages = _selectedImages.value.toMutableList()
        val remainingSlots = UploadEventImagesUseCase.MAX_IMAGES_PER_EVENT - currentImages.size
        
        if (remainingSlots > 0) {
            // Filter out duplicate files by name
            val uniqueFiles = files.filter { newFile ->
                !currentImages.any { existingFile -> 
                    existingFile.name == newFile.name 
                }
            }
            
            val duplicateCount = files.size - uniqueFiles.size
            val duplicateMessage = if (duplicateCount > 0) {
                "Đã bỏ qua $duplicateCount ảnh trùng tên"
            } else null
            
            val imagesToAdd = uniqueFiles.take(remainingSlots)
            currentImages.addAll(imagesToAdd)
            _selectedImages.value = currentImages
            
            _uiState.value = _uiState.value.copy(
                remainingImageSlots = UploadEventImagesUseCase.MAX_IMAGES_PER_EVENT - currentImages.size,
                duplicateImageMessage = duplicateMessage
            )
        }
    }



    /**
     * Remove image from selection
     */
    fun removeImage(file: File) {
        val currentImages = _selectedImages.value.toMutableList()
        currentImages.remove(file)
        _selectedImages.value = currentImages
        
        _uiState.value = _uiState.value.copy(
            remainingImageSlots = UploadEventImagesUseCase.MAX_IMAGES_PER_EVENT - currentImages.size
        )
    }

    /**
     * Clear all selected images
     */
    fun clearImages() {
        _selectedImages.value = emptyList()
        _uiState.value = _uiState.value.copy(
            remainingImageSlots = UploadEventImagesUseCase.MAX_IMAGES_PER_EVENT,
            duplicateImageMessage = null
        )
    }

    /**
     * Save event (create or update)
     */
    fun saveEvent() {
        val currentState = _uiState.value
        
        // Validate form
        val validationError = getValidationError(currentState)
        if (validationError != null) {
            _uiState.value = currentState.copy(error = validationError)
            return
        }

        viewModelScope.launch {
            _uiState.value = currentState.copy(isLoading = true, error = null)

            try {
                if (isEditMode) {
                    // Update existing event
                    currentEvent?.let { event ->
                        val result = updateEventUseCase(
                            id = event.id,
                            title = currentState.title,
                            description = currentState.description,
                            typeId = currentState.typeId,
                            startDate = currentState.startDate,
                            location = currentState.location
                        )
                        
                        result.onSuccess { updatedEvent ->
                            _uiState.value = currentState.copy(
                                isLoading = false,
                                isSuccess = true,
                                error = null
                            )
                        }.onFailure { error ->
                            _uiState.value = currentState.copy(
                                isLoading = false,
                                error = error.message ?: "Không thể cập nhật sự kiện"
                            )
                        }
                    }
                } else {
                    // Create new event
                    val result = if (_selectedImages.value.isNotEmpty()) {
                        createEventUseCase.invokeWithImages(
                            title = currentState.title,
                            description = currentState.description,
                            typeId = currentState.typeId,
                            startDate = currentState.startDate,
                            location = currentState.location,
                            imageFiles = _selectedImages.value
                        )
                    } else {
                        createEventUseCase.invoke(
                            title = currentState.title,
                            description = currentState.description,
                            typeId = currentState.typeId,
                            startDate = currentState.startDate,
                            location = currentState.location
                        )
                    }

                    result.onSuccess { event ->
                        _uiState.value = currentState.copy(
                            isLoading = false,
                            isSuccess = true,
                            createdEvent = event,
                            error = null
                        )
                    }.onFailure { error ->
                        _uiState.value = currentState.copy(
                            isLoading = false,
                            error = error.message ?: "Không thể lưu sự kiện"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.value = currentState.copy(
                    isLoading = false,
                    error = e.message ?: "Lỗi không xác định"
                )
            }
        }
    }

    /**
     * Get validation error message
     */
    private fun getValidationError(state: EventFormUiState): String? {
        return when {
            state.title.isBlank() -> "Tiêu đề sự kiện là bắt buộc"
            state.title.length > 100 -> "Tiêu đề không được vượt quá 100 ký tự"
            state.description.isBlank() -> "Mô tả là bắt buộc"
            state.description.length > 500 -> "Mô tả không được vượt quá 500 ký tự"
            state.typeId == 0 -> "Loại sự kiện là bắt buộc"
            state.startDate.isBlank() -> "Thời gian sự kiện là bắt buộc"
            !isValidDateTime(state.startDate) -> "Thời gian sự kiện không được trong quá khứ"
            state.location.isBlank() -> "Địa điểm là bắt buộc"
            state.location.length > 100 -> "Địa điểm không được vượt quá 100 ký tự"
            else -> null
        }
    }

    /**
     * Validate if date time is not in the past
     */
    private fun isValidDateTime(dateTimeString: String): Boolean {
        return try {
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
            val eventDateTime = LocalDateTime.parse(dateTimeString, formatter)
            val currentDateTime = LocalDateTime.now()
            eventDateTime.isAfter(currentDateTime)
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Clear error message
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    /**
     * Reset success state
     */
    fun resetSuccess() {
        _uiState.value = _uiState.value.copy(isSuccess = false, createdEvent = null)
    }
    
    /**
     * Clear duplicate image message
     */
    fun clearDuplicateMessage() {
        _uiState.value = _uiState.value.copy(duplicateImageMessage = null)
    }
}

/**
 * UI State for Event Form screen
 */
data class EventFormUiState(
    val title: String = "",
    val description: String = "",
    val typeId: Int = 0,
    val startDate: String = "",
    val location: String = "",
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val isEditMode: Boolean = false,
    val error: String? = null,
    val createdEvent: Event? = null,
    val remainingImageSlots: Int = UploadEventImagesUseCase.MAX_IMAGES_PER_EVENT,
    val duplicateImageMessage: String? = null
)
