package com.xdien.todoevent.data.database

import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import android.content.Context
import com.xdien.todoevent.data.dao.EventTypeDao
import com.xdien.todoevent.data.entity.EventTypeEntity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import androidx.room.RoomDatabase

@Database(
    entities = [EventTypeEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(TodoDatabase.Converters::class)
abstract class TodoDatabase : RoomDatabase() {
    abstract fun eventTypeDao(): EventTypeDao
    
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
        @Volatile
        private var INSTANCE: TodoDatabase? = null
        
        fun getDatabase(context: Context): TodoDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TodoDatabase::class.java,
                    "todo_database"
                )
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