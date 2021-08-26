package com.jay.todoapp.data.repository

import androidx.lifecycle.LiveData
import com.jay.todoapp.data.ToDoDao
import com.jay.todoapp.data.model.ToDoArchive
import com.jay.todoapp.data.model.ToDoData

// This is a repository which is a recommended way of getting data from different sources like DB, network request etc.
// This is NOT an architectural component unlike Room DB but is recommended
class ToDoRepository(private val toDoDao: ToDoDao) {
    val getAllData : LiveData<List<ToDoData>> = toDoDao.getAllData()
    val getAllDataOldFirst : LiveData<List<ToDoData>> = toDoDao.getAllDataOldFirst()
    val getDataByHigh : LiveData<List<ToDoData>> = toDoDao.sortByHigh()
    val getDataByLow : LiveData<List<ToDoData>> = toDoDao.sortByLow()

    suspend fun insertData(toDoData: ToDoData) {
        toDoDao.insertToDoData(toDoData)
    }
    suspend fun updateData(toDoData: ToDoData) {
        toDoDao.updateToDoData(toDoData)
    }
    suspend fun deleteData(toDoData: ToDoData) {
        toDoDao.deleteToDoData(toDoData)
    }
    suspend fun deleteAllData() {
        toDoDao.deleteAllToDoData()
    }
    fun searchDatabase(search : String) : List<ToDoData>{
        return toDoDao.searchDb(search)
    }

    /*
    * TODO ARCHIVE QUERIES
    * */

    val getAllArchive : LiveData<List<ToDoArchive>> = toDoDao.getAllArchive()
    suspend fun insertArchive(toDoArchive: ToDoArchive) = toDoDao.insertToDoArchive(toDoArchive)
    suspend fun updateArchive(toDoArchive: ToDoArchive) = toDoDao.updateToDoArchive(toDoArchive)
    suspend fun deleteArchive(toDoArchive: ToDoArchive) = toDoDao.deleteToDoArchive(toDoArchive)
}