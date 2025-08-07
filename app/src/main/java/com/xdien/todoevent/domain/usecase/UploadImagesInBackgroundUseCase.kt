package com.xdien.todoevent.domain.usecase

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
        // Cancel any existing upload job for this event
        uploadJobs[eventId]?.cancel()
        
        val job = CoroutineScope(Dispatchers.IO).launch {
            try {
                var uploadedCount = 0
                val totalImages = imageFiles.size
                
                // Upload images one by one to track progress
                val uploadedImages = mutableListOf<EventImage>()
                
                for (imageFile in imageFiles) {
                    try {
                        val result = eventRepository.uploadEventImages(eventId, listOf(imageFile))
                        uploadedImages.addAll(result)
                        uploadedCount++
                        
                        withContext(Dispatchers.Main) {
                            onProgress(uploadedCount, totalImages)
                        }
                    } catch (e: Exception) {
                        // Continue with other images even if one fails
                        withContext(Dispatchers.Main) {
                            onError(e)
                        }
                    }
                }
                
                withContext(Dispatchers.Main) {
                    onSuccess(uploadedImages)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onError(e)
                }
            } finally {
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
        uploadJobs[eventId]?.cancel()
        uploadJobs.remove(eventId)
    }
    
    /**
     * Cancel all uploads
     */
    fun cancelAllUploads() {
        uploadJobs.values.forEach { it.cancel() }
        uploadJobs.clear()
    }
    
    /**
     * Check if there's an active upload for an event
     * 
     * @param eventId The event ID
     * @return True if there's an active upload
     */
    fun isUploading(eventId: Int): Boolean {
        return uploadJobs[eventId]?.isActive == true
    }
}
