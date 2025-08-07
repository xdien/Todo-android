package com.xdien.todoevent.domain.usecase

import android.util.Log
import com.xdien.todoevent.domain.model.EventImage
import com.xdien.todoevent.domain.repository.EventRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Use case for uploading images in background
 * 
 * This use case handles image uploads in the background and continues
 * even if the user navigates away from the screen.
 */
@Singleton
class UploadImagesInBackgroundUseCase @Inject constructor(
    private val eventRepository: EventRepository
) {
    companion object {
        const val MAX_IMAGES_PER_EVENT = 5
        val ALLOWED_EXTENSIONS = setOf("jpg", "jpeg", "png", "gif", "webp")
        const val MAX_FILE_SIZE_BYTES = 10 * 1024 * 1024 // 10MB
    }
    private val uploadJobs = mutableMapOf<Int, Job>()
    
    /**
     * Upload images for an event in background
     * 
     * @param eventId The event ID
     * @param imageFiles List of image files to upload
     * @param onProgress Callback for upload progress
     * @param onSuccess Callback for successful upload
     * @param onError Callback for upload error
     */
    fun uploadImagesInBackground(
        eventId: Int,
        imageFiles: List<File>,
        onProgress: (Int, Int) -> Unit = { _, _ -> },
        onSuccess: (List<EventImage>) -> Unit = {},
        onError: (Exception) -> Unit = {}
    ) {
        Log.d("UploadImagesInBackgroundUseCase", "Starting upload for event $eventId with ${imageFiles.size} images")
        
        // Cancel any existing upload job for this event
        uploadJobs[eventId]?.cancel()
        
        val job = CoroutineScope(Dispatchers.IO).launch {
            try {
                var uploadedCount = 0
                val totalImages = imageFiles.size
                val failedImages = mutableListOf<String>()

                Log.d("UploadImagesInBackgroundUseCase", "Starting upload loop for $totalImages images")
                
                // Upload images one by one to track progress
                val uploadedImages = mutableListOf<EventImage>()
                
                for ((index, imageFile) in imageFiles.withIndex()) {
                    try {
                        Log.d("UploadImagesInBackgroundUseCase", "Uploading image ${index + 1}/$totalImages: ${imageFile.name}")
                        
                        val result = eventRepository.uploadEventImages(eventId, listOf(imageFile))
                        uploadedImages.addAll(result)
                        uploadedCount++
                        
                        Log.d("UploadImagesInBackgroundUseCase", "Successfully uploaded image: ${imageFile.name}, result count: ${result.size}")
                        result
                        withContext(Dispatchers.Main) {
                            onProgress(uploadedCount, totalImages)
                        }
                    } catch (e: Exception) {
                        Log.e("UploadImagesInBackgroundUseCase", "Failed to upload image: ${imageFile.name}", e)
                        failedImages.add("${imageFile.name}: ${e.message}")
                        
                        // Continue with other images even if one fails
                        // Don't call onError here as we want to continue with other images
                    }
                }
                
                Log.d("UploadImagesInBackgroundUseCase", "Upload loop completed. Success: $uploadedCount/$totalImages, Failed: ${failedImages.size}")
                
                withContext(Dispatchers.Main) {
                    if (uploadedImages.isNotEmpty()) {
                        Log.d("UploadImagesInBackgroundUseCase", "Calling onSuccess with ${uploadedImages.size} uploaded images")
                        onSuccess(uploadedImages)
                    } else {
                        Log.e("UploadImagesInBackgroundUseCase", "No images were uploaded successfully. Failed images: $failedImages")
                        onError(Exception("Failed to upload any images. Errors: ${failedImages.joinToString(", ")}"))
                    }
                }
            } catch (e: Exception) {
                Log.e("UploadImagesInBackgroundUseCase", "Critical error during upload process for event $eventId", e)
                withContext(Dispatchers.Main) {
                    onError(e)
                }
            } finally {
                Log.d("UploadImagesInBackgroundUseCase", "Cleaning up upload job for event $eventId")
                uploadJobs.remove(eventId)
            }
        }
        
        uploadJobs[eventId] = job
    }
    
    /**
     * Cancel upload for a specific event
     * 
     * @param eventId The event ID
     */
    fun cancelUpload(eventId: Int) {
        Log.d("UploadImagesInBackgroundUseCase", "Cancelling upload for event $eventId")
        uploadJobs[eventId]?.cancel()
        uploadJobs.remove(eventId)
        Log.d("UploadImagesInBackgroundUseCase", "Upload cancelled for event $eventId")
    }
    
    /**
     * Cancel all uploads
     */
    fun cancelAllUploads() {
        Log.d("UploadImagesInBackgroundUseCase", "Cancelling all uploads. Active jobs: ${uploadJobs.size}")
        uploadJobs.values.forEach { it.cancel() }
        uploadJobs.clear()
        Log.d("UploadImagesInBackgroundUseCase", "All uploads cancelled")
    }
    
    /**
     * Check if there's an active upload for an event
     * 
     * @param eventId The event ID
     * @return True if there's an active upload
     */
    fun isUploading(eventId: Int): Boolean {
        val isActive = uploadJobs[eventId]?.isActive == true
        Log.d("UploadImagesInBackgroundUseCase", "Checking upload status for event $eventId: $isActive")
        return isActive
    }
}
