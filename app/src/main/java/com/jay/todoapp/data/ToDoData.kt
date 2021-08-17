package com.jay.todoapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

// Entity act as tables in the DB and the properties of the data class act as the columns of the DB
@Entity(tableName = "todo_table")
data class ToDoData(
    @PrimaryKey(autoGenerate = true) var id: Int,
    var priority: Priority,
    var title: String,
    var description: String
)