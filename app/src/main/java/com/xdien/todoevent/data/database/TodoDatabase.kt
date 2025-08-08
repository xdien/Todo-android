package com.xdien.todoevent.data.database

import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import android.content.Context
import com.xdien.todoevent.data.dao.EventTypeDao
import com.xdien.todoevent.data.dao.EventDao
import com.xdien.todoevent.data.entity.EventTypeEntity
import com.xdien.todoevent.data.entity.EventEntity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [EventTypeEntity::class, EventEntity::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(TodoDatabase.Converters::class)
abstract class TodoDatabase : RoomDatabase() {
    abstract fun eventTypeDao(): EventTypeDao
    abstract fun eventDao(): EventDao
    
    class Converters {
        private val gson = Gson()

        @TypeConverter
        fun fromStringList(value: List<String>?): String? {
            return value?.let { gson.toJson(it) }
        }

        @TypeConverter
        fun toStringList(value: String?): List<String>? {
            return value?.let {
                val type = object : TypeToken<List<String>>() {}.type
                gson.fromJson(it, type)
            }
        }
    }
    
    companion object {
        // Migration from version 1 to version 2
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Create events table
                db.execSQL("""
                    CREATE TABLE events (
                        id INTEGER PRIMARY KEY NOT NULL,
                        title TEXT NOT NULL,
                        description TEXT NOT NULL,
                        eventTypeId INTEGER NOT NULL,
                        startDate TEXT NOT NULL,
                        location TEXT NOT NULL,
                        createdAt TEXT NOT NULL,
                        updatedAt TEXT,
                        images TEXT NOT NULL DEFAULT '[]'
                    )
                """)
            }
        }
        
        // Migration from version 4 to version 2
        val MIGRATION_4_2 = object : Migration(4, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Drop all tables and recreate them
                db.execSQL("DROP TABLE IF EXISTS event_type")
                db.execSQL("""
                    CREATE TABLE event_type (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL
                    )
                """)
                
                // Create events table
                db.execSQL("""
                    CREATE TABLE events (
                        id INTEGER PRIMARY KEY NOT NULL,
                        title TEXT NOT NULL,
                        description TEXT NOT NULL,
                        eventTypeId INTEGER NOT NULL,
                        startDate TEXT NOT NULL,
                        location TEXT NOT NULL,
                        createdAt TEXT NOT NULL,
                        updatedAt TEXT,
                        images TEXT NOT NULL DEFAULT '[]'
                    )
                """)
                
                // Recreate default data
                db.execSQL("INSERT INTO event_type (name) VALUES ('Meeting')")
                db.execSQL("INSERT INTO event_type (name) VALUES ('Party')")
                db.execSQL("INSERT INTO event_type (name) VALUES ('Conference')")
                db.execSQL("INSERT INTO event_type (name) VALUES ('Personal')")
                db.execSQL("INSERT INTO event_type (name) VALUES ('Work')")
            }
        }
        
        @Volatile
        private var INSTANCE: TodoDatabase? = null
        
        fun getDatabase(context: Context): TodoDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TodoDatabase::class.java,
                    "todo_database"
                )
                .fallbackToDestructiveMigration(true) // Allow destructive migration to handle version conflicts
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        // Create default event types when database is created for the first time
                        db.execSQL("INSERT INTO event_type (name) VALUES ('Meeting')")
                        db.execSQL("INSERT INTO event_type (name) VALUES ('Party')")
                        db.execSQL("INSERT INTO event_type (name) VALUES ('Conference')")
                        db.execSQL("INSERT INTO event_type (name) VALUES ('Personal')")
                        db.execSQL("INSERT INTO event_type (name) VALUES ('Work')")
                    }
                })
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
} 