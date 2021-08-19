package com.jay.todoapp.data.viewModel

import android.app.Application
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.jay.todoapp.R
import com.jay.todoapp.data.ToDoDatabase
import com.jay.todoapp.data.model.Priority
import com.jay.todoapp.data.model.ToDoData
import com.jay.todoapp.data.repository.ToDoRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

// AndroidViewModel is just like viewModel but provides access to the application context directly
class ToDoViewModel(application: Application) : AndroidViewModel(application) {

    /*
    * DATABASE QUERIES
    * */
    private val toDoDao = ToDoDatabase.getDatabase(application).toDoDao()
    private val repository : ToDoRepository

    private val _getAllData: LiveData<List<ToDoData>>

    init {
        repository = ToDoRepository(toDoDao)
        _getAllData = repository.getAllData
    }

    val getAllData: LiveData<List<ToDoData>> = _getAllData

    private fun insertData(toDoData: ToDoData) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertData(toDoData)
        }
    }

    private fun updateData(toDoData: ToDoData) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateData(toDoData)
        }
    }

    private fun deleteSingleDataItem(toDoData: ToDoData) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteData(toDoData)
        }
    }

    /*
    * FUNCTIONS FOR OTHER LOGIC
    * */

    fun insertDataToDb(toDoTitle: String, toDoDesc: String, priorityLevel: String) : Boolean{
        return if(verifyUserData(toDoTitle, priorityLevel)) {
            val newData = ToDoData(
                0, // This is set to auto increment so room will handle it
                parsePriority(priorityLevel),
                toDoTitle,
                toDoDesc
            )
            insertData(newData)
            true
        } else false
    }

    fun updateDataToDb(toDoId: Int, toDoTitle: String, toDoDesc: String, priorityLevel: String) : Boolean{
        return if(verifyUserData(toDoTitle, priorityLevel)) {
            val updatedData = ToDoData(
                toDoId,
                parsePriority(priorityLevel),
                toDoTitle,
                toDoDesc
            )
            updateData(updatedData)
            true
        } else false
    }

    fun deleteSingleItemFromDb(toDoId: Int, toDoTitle: String, toDoDesc: String, priorityLevel: String) {
        val itemToBeDeleted = ToDoData(
            toDoId,
            parsePriority(priorityLevel),
            toDoTitle,
            toDoDesc
        )
        deleteSingleDataItem(itemToBeDeleted)
    }

    private fun verifyUserData(title: String, priority: String) : Boolean{
        return when {
            title.trim() == "" -> {
                Toast.makeText(getApplication(), "Please enter a title", Toast.LENGTH_SHORT).show()
                false
            }
            priority.trim() == "" -> {
                Toast.makeText(getApplication(), "Please choose a priority level", Toast.LENGTH_SHORT).show()
                false
            }
            else -> true
        }
    }

    private fun parsePriority(priority: String) : Priority {
        return when(priority) {
            "High" -> {
                Priority.HIGH}
            "Medium" -> {
                Priority.MEDIUM}
            else -> {
                Priority.LOW}
        }
    }

}