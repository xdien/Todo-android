package com.xdien.todoevent.domain.usecase

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.core.app.ActivityCompat
import javax.inject.Inject

class RequestCameraPermissionUseCase @Inject constructor() {
    
    fun hasCameraPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    fun hasPhotoPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ sử dụng READ_MEDIA_IMAGES
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_MEDIA_IMAGES
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            // Android 12 trở xuống sử dụng READ_EXTERNAL_STORAGE
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }
    
    fun getRequiredPermissions(): Array<String> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ cần cả camera và READ_MEDIA_IMAGES
            arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.READ_MEDIA_IMAGES
            )
        } else {
            // Android 12 trở xuống cần camera và READ_EXTERNAL_STORAGE
            arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        }
    }
    
    fun needsPermissionRequest(context: Context): Boolean {
        return !hasCameraPermission(context) || !hasPhotoPermission(context)
    }
    
    fun allPermissionsGranted(context: Context): Boolean {
        return hasCameraPermission(context) && hasPhotoPermission(context)
    }
    
    fun getPermissionExplanation(): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            "Ứng dụng cần quyền truy cập camera để chụp ảnh và quyền đọc ảnh để chọn ảnh từ thư viện."
        } else {
            "Ứng dụng cần quyền truy cập camera để chụp ảnh và quyền đọc bộ nhớ để chọn ảnh từ thư viện."
        }
    }
    
    fun shouldShowRequestPermissionRationale(context: Context, permission: String): Boolean {
        return if (context is android.app.Activity) {
            ActivityCompat.shouldShowRequestPermissionRationale(context, permission)
        } else {
            false
        }
    }
}
