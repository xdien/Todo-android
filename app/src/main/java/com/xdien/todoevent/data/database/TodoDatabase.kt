package com.xdien.todoevent.data.database

import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import android.content.Context
import com.xdien.todoevent.data.dao.TodoDao
import com.xdien.todoevent.data.dao.EventTypeDao
import com.xdien.todoevent.data.entity.TodoEntity
import com.xdien.todoevent.data.entity.EventTypeEntity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import androidx.room.RoomDatabase

@Database(
    entities = [TodoEntity::class, EventTypeEntity::class],
    version = 5,
    exportSchema = false
)
@TypeConverters(TodoDatabase.Converters::class)
abstract class TodoDatabase : RoomDatabase() {
    abstract fun todoDao(): TodoDao
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
        
        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Create event_type table
                db.execSQL("CREATE TABLE event_type (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "name TEXT NOT NULL" +
                    ")")
                
                // Insert default event types
                db.execSQL("INSERT INTO event_type (name) VALUES ('Meeting')")
                db.execSQL("INSERT INTO event_type (name) VALUES ('Party')")
                db.execSQL("INSERT INTO event_type (name) VALUES ('Conference')")
                db.execSQL("INSERT INTO event_type (name) VALUES ('Personal')")
                db.execSQL("INSERT INTO event_type (name) VALUES ('Work')")
                
                // Create temporary table for todos with new structure
                db.execSQL("CREATE TABLE todos_new (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "title TEXT NOT NULL, " +
                    "description TEXT, " +
                    "thumbnailUrl TEXT, " +
                    "galleryImages TEXT, " +
                    "eventTime INTEGER, " +
                    "eventEndTime INTEGER, " +
                    "location TEXT, " +
                    "eventTypeId INTEGER, " +
                    "isCompleted INTEGER NOT NULL, " +
                    "createdAt INTEGER NOT NULL, " +
                    "updatedAt INTEGER NOT NULL, " +
                    "FOREIGN KEY(eventTypeId) REFERENCES event_type(id) ON DELETE CASCADE" +
                    ")")
                
                // Create index for foreign key
                db.execSQL("CREATE INDEX index_todos_eventTypeId ON todos_new (eventTypeId)")
                
                // Migrate existing data
                db.execSQL("INSERT INTO todos_new (id, title, description, thumbnailUrl, galleryImages, eventTime, eventEndTime, location, isCompleted, createdAt, updatedAt) " +
                    "SELECT id, title, description, thumbnailUrl, galleryImages, eventTime, eventEndTime, location, isCompleted, createdAt, updatedAt FROM todos")
                
                // Update eventTypeId based on eventType string
                db.execSQL("UPDATE todos_new SET eventTypeId = (SELECT id FROM event_type WHERE name = 'Meeting') WHERE eventType = 'Meeting'")
                db.execSQL("UPDATE todos_new SET eventTypeId = (SELECT id FROM event_type WHERE name = 'Party') WHERE eventType = 'Party'")
                db.execSQL("UPDATE todos_new SET eventTypeId = (SELECT id FROM event_type WHERE name = 'Conference') WHERE eventType = 'Conference'")
                db.execSQL("UPDATE todos_new SET eventTypeId = (SELECT id FROM event_type WHERE name = 'Personal') WHERE eventType = 'Personal'")
                db.execSQL("UPDATE todos_new SET eventTypeId = (SELECT id FROM event_type WHERE name = 'Work') WHERE eventType = 'Work'")
                
                // Drop old table and rename new table
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
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
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