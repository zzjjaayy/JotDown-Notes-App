package com.jay.todoapp.data

import android.content.Context
import androidx.room.*

@Database(entities = [ToDoData::class], version = 1, exportSchema = false)
@TypeConverters(PriorityConverter::class) // This is to let Room know about the conversion
abstract class ToDoDatabase : RoomDatabase(){

    abstract fun toDoDao() : ToDoDao

    // A companion abject can be accessed directly through the class without a object instance of the class
    companion object {
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
                ).build()
                INSTANCE = instance
                return instance
            }
        }
    }
}