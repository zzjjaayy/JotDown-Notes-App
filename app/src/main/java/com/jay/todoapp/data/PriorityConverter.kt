package com.jay.todoapp.data

import androidx.room.TypeConverter

// Room allows primitive and boxed data types but not object references, therefore, we create a
// converter which converts the Priority value from the enum class when reading or writing to the DB.
// This converter is called a TypeConverter and annotated to notify Room
class PriorityConverter {

    @TypeConverter
    fun fromPriority(priority: Priority): String {
        return priority.name
    }

    @TypeConverter
    fun toPriority(name: String): Priority {
        return Priority.valueOf(name)
    }
}