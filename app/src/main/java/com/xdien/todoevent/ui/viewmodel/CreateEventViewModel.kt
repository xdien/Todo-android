package com.xdien.todoevent.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xdien.todoevent.domain.model.Event
import com.xdien.todoevent.domain.model.EventType
import com.xdien.todoevent.domain.usecase.CreateEventUseCase
import com.xdien.todoevent.domain.usecase.GetEventTypesUseCase
import com.xdien.todoevent.domain.usecase.UploadImagesInBackgroundUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

/**
 * ViewModel for creating events with image upload
 * 
 * This ViewModel manages the state and business logic for creating events,
 * including form validation, image selection, and API communication.
 */
@HiltViewModel
class CreateEventViewModel @Inject constructor(
    private val createEventUseCase: CreateEventUseCase,
    private val getEventTypesUseCase: GetEventTypesUseCase,
    private val uploadEventImagesUseCase: UploadImagesInBackgroundUseCase
) : ViewModel() {

    // UI State
    private val _uiState = MutableStateFlow(CreateEventUiState())
    val uiState: StateFlow<CreateEventUiState> = _uiState.asStateFlow()

    // Event Types
    private val _eventTypes = MutableStateFlow<List<EventType>>(emptyList())
    val eventTypes: StateFlow<List<EventType>> = _eventTypes.asStateFlow()

    // Selected Images
    private val _selectedImages = MutableStateFlow<List<File>>(emptyList())
    val selectedImages: StateFlow<List<File>> = _selectedImages.asStateFlow()

    init {
        loadEventTypes()
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
     * Add images to selection
     */
    fun addImages(files: List<File>) {
        val currentImages = _selectedImages.value.toMutableList()
        val remainingSlots = UploadImagesInBackgroundUseCase.MAX_IMAGES_PER_EVENT - currentImages.size
        
        if (remainingSlots > 0) {
            val imagesToAdd = files.take(remainingSlots)
            currentImages.addAll(imagesToAdd)
            _selectedImages.value = currentImages
            
            _uiState.value = _uiState.value.copy(
                remainingImageSlots = UploadImagesInBackgroundUseCase.MAX_IMAGES_PER_EVENT - currentImages.size
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
            remainingImageSlots = UploadImagesInBackgroundUseCase.MAX_IMAGES_PER_EVENT - currentImages.size
        )
    }

    /**
     * Clear all selected images
     */
    fun clearImages() {
        _selectedImages.value = emptyList()
        _uiState.value = _uiState.value.copy(remainingImageSlots = UploadImagesInBackgroundUseCase.MAX_IMAGES_PER_EVENT)
    }

    /**
     * Create event with or without images
     */
    fun createEvent() {
        val currentState = _uiState.value
        
        // Validate form
        val validationError = getValidationError(currentState)
        if (validationError != null) {
            _uiState.value = currentState.copy(
                error = validationError
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = currentState.copy(
                isLoading = true,
                error = null
            )

            // First, create the event without images
            val createEventResult = createEventUseCase.invoke(
                title = currentState.title,
                description = currentState.description,
                typeId = currentState.typeId,
                startDate = currentState.startDate,
                location = currentState.location
            )

            createEventResult.onSuccess { event ->
                // Event created successfully, save it to state first
                _uiState.value = currentState.copy(
                    createdEvent = event
                )
                
                // Now upload images if any
                if (_selectedImages.value.isNotEmpty()) {
                    uploadImagesToEvent(event.id)
                } else {
                    // No images to upload, complete the process
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isSuccess = true,
                        error = null
                    )
                    // Reset form
                    resetForm()
                }
            }.onFailure { error ->
                _uiState.value = currentState.copy(
                    isLoading = false,
                    error = error.message ?: "Không thể tạo sự kiện"
                )
            }
        }
    }

    /**
     * Upload additional images to existing event
     */
    fun uploadImagesToEvent(eventId: Int) {
        if (_selectedImages.value.isEmpty()) return

        _uiState.value = _uiState.value.copy(isLoading = true, error = null)

        uploadEventImagesUseCase.uploadImagesInBackground(
            eventId = eventId,
            imageFiles = _selectedImages.value,
            onProgress = { uploaded, total ->
                // Could show progress if needed
            },
            onSuccess = { images ->
                // Get the created event from current state
                val currentState = _uiState.value
                val createdEvent = currentState.createdEvent
                
                if (createdEvent != null) {
                    // Update the event with uploaded images
                    val eventWithImages = createdEvent.copy(images = images)
                    _uiState.value = currentState.copy(
                        isLoading = false,
                        isSuccess = true,
                        createdEvent = eventWithImages,
                        uploadedImages = images,
                        error = null
                    )
                } else {
                    // Fallback if createdEvent is null
                    _uiState.value = currentState.copy(
                        isLoading = false,
                        uploadedImages = images,
                        error = null
                    )
                }
                clearImages()
                // Reset form after successful upload
                resetForm()
            },
            onError = { error ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = error.message ?: "Không thể tải lên hình ảnh"
                )
            }
        )
    }

    /**
     * Validate form fields
     */
    private fun isFormValid(state: CreateEventUiState): Boolean {
        return state.title.isNotBlank() &&
               state.description.isNotBlank() &&
               state.typeId > 0 &&
               state.startDate.isNotBlank() &&
               state.location.isNotBlank()
    }

    /**
     * Get validation error message
     */
    private fun getValidationError(state: CreateEventUiState): String? {
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
     * Reset form to initial state
     */
    private fun resetForm() {
        _uiState.value = CreateEventUiState()
        clearImages()
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
}

/**
 * UI State for Create Event screen
 */
data class CreateEventUiState(
    val title: String = "",
    val description: String = "",
    val typeId: Int = 0,
    val startDate: String = "",
    val location: String = "",
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null,
    val createdEvent: Event? = null,
    val uploadedImages: List<com.xdien.todoevent.domain.model.EventImage> = emptyList(),
    val remainingImageSlots: Int = UploadImagesInBackgroundUseCase.MAX_IMAGES_PER_EVENT
) 