package com.xdien.todoevent.di

import com.xdien.todoevent.domain.usecase.GetEventByIdUseCase
import com.xdien.todoevent.domain.usecase.UploadImagesInBackgroundUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {
    
    @Provides
    @Singleton
    fun provideGetEventByIdUseCase(
        eventRepository: com.xdien.todoevent.domain.repository.EventRepository
    ): GetEventByIdUseCase {
        return GetEventByIdUseCase(eventRepository)
    }
    
    @Provides
    @Singleton
    fun provideUploadImagesInBackgroundUseCase(
        eventRepository: com.xdien.todoevent.domain.repository.EventRepository
    ): UploadImagesInBackgroundUseCase {
        return UploadImagesInBackgroundUseCase(eventRepository)
    }
}
