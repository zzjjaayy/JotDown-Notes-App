package com.jay.todoapp.data.repository

import androidx.lifecycle.LiveData
import com.jay.todoapp.data.ToDoDao
import com.jay.todoapp.data.model.ToDoData

// This is a repository which is a recommended way of getting data from different sources like DB, network request etc.
// This is NOT an architectural component unlike Room DB but is recommended
class ToDoRepository(private val toDoDao: ToDoDao) {
    val getAllData : LiveData<List<ToDoData>> = toDoDao.getAllData()
    suspend fun insertData(toDoData: ToDoData) {
        toDoDao.insertToDoData(toDoData)
    }
    suspend fun updateData(toDoData: ToDoData) {
        toDoDao.updateToDoData(toDoData)
    }
    suspend fun deleteData(toDoData: ToDoData) {
        toDoDao.deleteToDoData(toDoData)
    }
}