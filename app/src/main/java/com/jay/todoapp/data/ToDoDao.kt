package com.jay.todoapp.data

import androidx.lifecycle.LiveData
import androidx.room.*
import com.jay.todoapp.data.model.ToDoArchive
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

    @Query("SELECT * FROM todo_table WHERE title LIKE :search OR description LIKE :search")
    fun searchAllData(search: String) : List<ToDoData>

    @Query("SELECT * FROM todo_table ORDER BY CASE WHEN priority LIKE 'H%' THEN 1 WHEN priority LIKE 'M%' THEN 2 WHEN priority LIKE 'L%' THEN 3 END")
    fun sortByHigh() : LiveData<List<ToDoData>>

    @Query("SELECT * FROM todo_table ORDER BY CASE WHEN priority LIKE 'H%' THEN 3 WHEN priority LIKE 'M%' THEN 2 WHEN priority LIKE 'L%' THEN 1 END")
    fun sortByLow() : LiveData<List<ToDoData>>

    @Query("SELECT * FROM todo_table ORDER BY id DESC")
    fun getAllDataOldFirst() : LiveData<List<ToDoData>>

    /*
    * TODO ARCHIVE QUERIES
    * */
    @Query("SELECT * FROM todo_archive_table ORDER BY id ASC")
    fun getAllArchive() : LiveData<List<ToDoArchive>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertToDoArchive(toDoArchive: ToDoArchive)

    @Update
    suspend fun updateToDoArchive(toDoArchive: ToDoArchive)

    @Query("DELETE FROM todo_archive_table WHERE oldId = :id")
    suspend fun deleteToDoArchive(id: Int) : Int

    @Query("SELECT * FROM todo_archive_table ORDER BY CASE WHEN priority LIKE 'H%' THEN 1 WHEN priority LIKE 'M%' THEN 2 WHEN priority LIKE 'L%' THEN 3 END")
    fun sortArchiveByHigh() : LiveData<List<ToDoArchive>>

    @Query("SELECT * FROM todo_archive_table ORDER BY CASE WHEN priority LIKE 'H%' THEN 3 WHEN priority LIKE 'M%' THEN 2 WHEN priority LIKE 'L%' THEN 1 END")
    fun sortArchiveByLow() : LiveData<List<ToDoArchive>>

    @Query("SELECT * FROM todo_archive_table ORDER BY id DESC")
    fun getArchiveOldFirst() : LiveData<List<ToDoArchive>>

    @Query("SELECT * FROM todo_archive_table WHERE title LIKE :search OR description LIKE :search")
    fun searchAllArchive(search: String) : List<ToDoArchive>

}