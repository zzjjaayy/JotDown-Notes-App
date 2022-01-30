package com.jay.todoapp.data.model

data class ToDo(
    var id: String = "",
    val createdTS: Long = -1L,
    var archivedTS: Long = -1L,
    var isArchived: Boolean = false,
    var priority: Priority = Priority.LOW,
    var title: String = "",
    var description: String = ""
)