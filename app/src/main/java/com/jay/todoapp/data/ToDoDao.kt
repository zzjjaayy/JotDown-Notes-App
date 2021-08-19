package com.jay.todoapp.data

import androidx.lifecycle.LiveData
import androidx.room.*
import com.jay.todoapp.data.model.ToDoData

// Dao stands for Data Access Object which holds all the functions mapping to various queries to the DB
@Dao
interface ToDoDao {

    @Query("SELECT * FROM todo_table ORDER BY id ASC")
    fun getAllData() : LiveData<List<ToDoData>>

    // OnConflict specified that duplicate data will be ignored
    // suspend keyword states that this function will be executed in a coroutine {BG thread}
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertToDoData(toDoData: ToDoData)

    @Update
    suspend fun updateToDoData(toDoData: ToDoData)

    @Delete
    suspend fun deleteToDoData(toDoData: ToDoData)

    @Query("DELETE FROM todo_table")
    suspend fun deleteAllToDoData()
}