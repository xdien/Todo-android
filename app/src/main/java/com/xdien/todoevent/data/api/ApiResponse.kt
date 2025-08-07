package com.xdien.todoevent.data.api

/**
 * Common API response wrapper
 */
data class ApiResponse<T>(
    val success: Boolean,
    val data: T,
    val message: String
) 