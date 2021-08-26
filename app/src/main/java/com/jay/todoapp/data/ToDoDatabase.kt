package com.jay.todoapp.data

import android.content.Context
import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.jay.todoapp.data.model.ToDoArchive
import com.jay.todoapp.data.model.ToDoData

@Database(entities = [ToDoData::class, ToDoArchive::class], version = 2, exportSchema = false)
@TypeConverters(PriorityConverter::class) // This is to let Room know about the conversion
abstract class ToDoDatabase : RoomDatabase(){

    abstract fun toDoDao() : ToDoDao

    // A companion abject can be accessed directly through the class without a object instance of the class
    companion object {
        private val migration_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("CREATE TABLE IF NOT EXISTS `todo_archive_table` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `oldId` INTEGER NOT NULL , `priority` TEXT NOT NULL, `title` TEXT NOT NULL, `description` TEXT NOT NULL)")
            }
        }

        // Volatile means writes to this field are immediately made visible to other threads
        @Volatile
        private var INSTANCE: ToDoDatabase? = null

        // This function gets the instance of the DB
        fun getDatabase(context: Context) : ToDoDatabase {
            val tempInstance = INSTANCE
            if(tempInstance != null) {
                return tempInstance
            }
            // Synchronised means once a thread calls this block {acquires a lock}, no other thread
            // can access it before the first one releases the lock
            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ToDoDatabase::class.java,
                    "todo_database"
                ).addMigrations(migration_1_2).build()
                INSTANCE = instance
                return instance
            }
        }
    }
}