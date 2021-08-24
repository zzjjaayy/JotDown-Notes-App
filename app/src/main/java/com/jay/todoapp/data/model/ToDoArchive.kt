package com.jay.todoapp.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "todo_archive_table")
data class ToDoArchive(
    @PrimaryKey(autoGenerate = true) var id: Int,
    var oldId: Int,
    var priority: Priority,
    var title: String,
    var description: String
)