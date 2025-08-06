package com.xdien.todoevent.common

import com.xdien.todoevent.data.api.TodoApiService
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NetworkManager @Inject constructor(
    @ApplicationContext private val context: android.content.Context,
    private val sharedPreferencesHelper: SharedPreferencesHelper
) {
    private var retrofit: Retrofit? = null
    private var todoApiService: TodoApiService? = null

    private fun createRetrofit(baseUrl: String): Retrofit {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()

        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    fun getTodoApiService(): TodoApiService {
        val currentUrl = sharedPreferencesHelper.getApiUrl()
        
        // Check if we need to recreate the service
        if (retrofit == null || todoApiService == null) {
            retrofit = createRetrofit(currentUrl)
            todoApiService = retrofit!!.create(TodoApiService::class.java)
        }
        
        return todoApiService!!
    }

    fun updateApiUrl(newUrl: String) {
        sharedPreferencesHelper.saveApiUrl(newUrl)
        // Force recreation of Retrofit instance
        retrofit = null
        todoApiService = null
    }
} 