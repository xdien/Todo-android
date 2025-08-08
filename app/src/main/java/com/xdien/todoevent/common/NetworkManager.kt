package com.xdien.todoevent.common


import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.xdien.todoevent.data.api.EventApiService
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

    private var eventApiService: EventApiService? = null

    private fun createRetrofit(baseUrl: String): Retrofit {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()

        // Configure Gson to handle snake_case to camelCase conversion
        val gson = GsonBuilder()
            .setFieldNamingPolicy(com.google.gson.FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .create()

        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }


    
    fun getEventApiService(): EventApiService {
        val currentUrl = sharedPreferencesHelper.getApiUrl()
        
        // Check if we need to recreate the service
        if (retrofit == null || eventApiService == null) {
            retrofit = createRetrofit(currentUrl)
            eventApiService = retrofit!!.create(EventApiService::class.java)
        }
        
        return eventApiService!!
    }

    fun updateApiUrl(newUrl: String) {
        sharedPreferencesHelper.saveApiUrl(newUrl)
        // Force recreation of Retrofit instance
        retrofit = null
        eventApiService = null
    }
} 