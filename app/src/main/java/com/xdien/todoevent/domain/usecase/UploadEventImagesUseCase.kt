package com.xdien.todoevent.domain.usecase

import com.xdien.todoevent.domain.model.EventImage
import com.xdien.todoevent.domain.repository.EventRepository
import kotlinx.coroutines.flow.first
import java.io.File
import javax.inject.Inject

/**
 * Use case for uploading images to an event
 * 
 * This use case encapsulates the business logic for uploading images,
 * including validation and coordination with the repository.
 */
class UploadEventImagesUseCase @Inject constructor(
    private val eventRepository: EventRepository
) {
    companion object {
        const val MAX_IMAGES_PER_EVENT = 5
        val ALLOWED_EXTENSIONS = setOf("jpg", "jpeg", "png", "gif", "webp")
        const val MAX_FILE_SIZE_BYTES = 10 * 1024 * 1024 // 10MB
    }
    
    /**
     * Upload images to an event
     * 
     * @param eventId The event ID
     * @param imageFiles List of image files to upload
     * @return Result containing the uploaded images or error
     */
    suspend operator fun invoke(eventId: Int, imageFiles: List<File>): Result<List<EventImage>> {
        return try {
            // Validate input
            val validationResult = validateImages(eventId, imageFiles)
            if (validationResult.isFailure) {
                return Result.failure(validationResult.exceptionOrNull() ?: Exception("Validation failed"))
            }
            
            // Upload images through repository
            val uploadedImages = eventRepository.uploadEventImages(eventId, imageFiles)
            Result.success(uploadedImages)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Validate images before upload
     * 
     * @param eventId The event ID
     * @param imageFiles List of image files to validate
     * @return Result indicating validation success or failure
     */
    private suspend fun validateImages(eventId: Int, imageFiles: List<File>): Result<Unit> {
        // Check if event exists and can accept more images
        val currentEvent = eventRepository.getEventById(eventId).first()
        if (currentEvent == null) {
            return Result.failure(IllegalArgumentException("Event not found"))
        }
        
        val currentImageCount = currentEvent.images.size
        if (currentImageCount + imageFiles.size > MAX_IMAGES_PER_EVENT) {
            return Result.failure(
                IllegalArgumentException(
                    "Cannot upload ${imageFiles.size} images. Event already has $currentImageCount images. " +
                    "Maximum allowed is $MAX_IMAGES_PER_EVENT."
                )
            )
        }
        
        // Validate each image file
        imageFiles.forEach { file ->
            val validationResult = validateImageFile(file)
            if (validationResult.isFailure) {
                return validationResult
            }
        }
        
        return Result.success(Unit)
    }
    
    /**
     * Validate a single image file
     * 
     * @param file The image file to validate
     * @return Result indicating validation success or failure
     */
    private fun validateImageFile(file: File): Result<Unit> {
        // Check if file exists
        if (!file.exists()) {
            return Result.failure(IllegalArgumentException("File does not exist: ${file.name}"))
        }
        
        // Check file size
        if (file.length() > MAX_FILE_SIZE_BYTES) {
            return Result.failure(
                IllegalArgumentException(
                    "File too large: ${file.name}. Maximum size is ${MAX_FILE_SIZE_BYTES / (1024 * 1024)}MB"
                )
            )
        }
        
        // Check file extension
        val extension = file.extension.lowercase()
        if (extension !in ALLOWED_EXTENSIONS) {
            return Result.failure(
                IllegalArgumentException(
                    "Invalid file type: ${file.name}. Allowed types: ${ALLOWED_EXTENSIONS.joinToString(", ")}"
                )
            )
        }
        
        return Result.success(Unit)
    }
    
    /**
     * Get remaining image slots for an event
     * 
     * @param eventId The event ID
     * @return Number of remaining image slots
     */
    suspend fun getRemainingImageSlots(eventId: Int): Int {
        val event = eventRepository.getEventById(eventId).first()
        return if (event != null) {
            (MAX_IMAGES_PER_EVENT - event.images.size).coerceAtLeast(0)
        } else {
            0
        }
    }
    
    /**
     * Check if event can accept more images
     * 
     * @param eventId The event ID
     * @return True if event can accept more images
     */
    suspend fun canAcceptMoreImages(eventId: Int): Boolean {
        return getRemainingImageSlots(eventId) > 0
    }
} 