package com.xdien.todoevent.di

import com.xdien.todoevent.common.NetworkManager
import com.xdien.todoevent.common.SharedPreferencesHelper
import com.xdien.todoevent.data.api.TodoApiService
import com.xdien.todoevent.data.api.EventApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    
    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()
    }
    
    @Provides
    @Singleton
    fun provideTodoApiService(networkManager: NetworkManager): TodoApiService {
        return networkManager.getTodoApiService()
    }
    
    @Provides
    @Singleton
    fun provideEventApiService(networkManager: NetworkManager): EventApiService {
        return networkManager.getEventApiService()
    }
} 