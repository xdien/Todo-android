package com.xdien.todoevent.data.database

import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import android.content.Context
import com.xdien.todoevent.data.dao.TodoDao
import com.xdien.todoevent.data.entity.TodoEntity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

@Database(
    entities = [TodoEntity::class],
    version = 4,
    exportSchema = false
)
@TypeConverters(TodoDatabase.Converters::class)
abstract class TodoDatabase : RoomDatabase() {
    abstract fun todoDao(): TodoDao
    
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
        
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Add new columns for event information
                db.execSQL("ALTER TABLE todos ADD COLUMN thumbnailUrl TEXT")
                db.execSQL("ALTER TABLE todos ADD COLUMN eventTime INTEGER")
                db.execSQL("ALTER TABLE todos ADD COLUMN location TEXT")
            }
        }
        
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Add new columns for enhanced event details
                db.execSQL("ALTER TABLE todos ADD COLUMN galleryImages TEXT")
                db.execSQL("ALTER TABLE todos ADD COLUMN eventEndTime INTEGER")
                db.execSQL("ALTER TABLE todos ADD COLUMN eventType TEXT")
                db.execSQL("ALTER TABLE todos ADD COLUMN updatedAt INTEGER")
            }
        }
        
        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Fix updatedAt column to be NOT NULL
                db.execSQL("UPDATE todos SET updatedAt = createdAt WHERE updatedAt IS NULL")
                db.execSQL("CREATE TABLE todos_new (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "title TEXT NOT NULL, " +
                    "description TEXT, " +
                    "thumbnailUrl TEXT, " +
                    "galleryImages TEXT, " +
                    "eventTime INTEGER, " +
                    "eventEndTime INTEGER, " +
                    "location TEXT, " +
                    "eventType TEXT, " +
                    "isCompleted INTEGER NOT NULL, " +
                    "createdAt INTEGER NOT NULL, " +
                    "updatedAt INTEGER NOT NULL" +
                    ")")
                db.execSQL("INSERT INTO todos_new SELECT * FROM todos")
                db.execSQL("DROP TABLE todos")
                db.execSQL("ALTER TABLE todos_new RENAME TO todos")
            }
        }
        
        fun getDatabase(context: Context): TodoDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TodoDatabase::class.java,
                    "todo_database"
                )
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
} 