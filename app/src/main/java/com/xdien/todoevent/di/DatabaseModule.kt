package com.xdien.todoevent.di

import android.content.Context
import com.xdien.todoevent.data.dao.EventTypeDao
import com.xdien.todoevent.data.dao.EventDao
import com.xdien.todoevent.data.database.TodoDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideTodoDatabase(@ApplicationContext context: Context): TodoDatabase {
        return TodoDatabase.getDatabase(context)
    }
    
    @Provides
    fun provideEventTypeDao(database: TodoDatabase): EventTypeDao {
        return database.eventTypeDao()
    }
    
    @Provides
    fun provideEventDao(database: TodoDatabase): EventDao {
        return database.eventDao()
    }
} 