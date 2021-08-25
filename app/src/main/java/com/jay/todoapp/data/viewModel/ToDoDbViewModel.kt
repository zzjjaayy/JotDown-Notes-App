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
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.jay.todoapp.R
import com.jay.todoapp.data.ToDoDatabase
import com.jay.todoapp.data.model.Priority
import com.jay.todoapp.data.model.ToDoData
import com.jay.todoapp.data.repository.ToDoRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

// AndroidViewModel is just like viewModel but provides access to the application context directly
class ToDoDbViewModel(application: Application) : AndroidViewModel(application) {

    /*
    * DATABASE QUERIES
    * */
    private val toDoDao = ToDoDatabase.getDatabase(application).toDoDao()
    private val repository : ToDoRepository

    private val _getAllData: LiveData<List<ToDoData>>
    val getAllDataOldFirst : LiveData<List<ToDoData>>
    val getDataByHighPriority : LiveData<List<ToDoData>>
    val getDataByLowPriority : LiveData<List<ToDoData>>

    val isEmptyDb : MutableLiveData<Boolean> = MutableLiveData(false)
    fun checkIfDbEmpty(toDoData: List<ToDoData>) {
        isEmptyDb.value = toDoData.isEmpty()
    }

    init {
        repository = ToDoRepository(toDoDao)
        _getAllData = repository.getAllData
        getAllDataOldFirst = repository.getAllDataOldFirst
        getDataByHighPriority = repository.getDataByHigh
        getDataByLowPriority = repository.getDataByLow
    }

    val getAllData: LiveData<List<ToDoData>> = _getAllData

    fun insertData(toDoData: ToDoData) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertData(toDoData)
        }
    }

    fun updateData(toDoData: ToDoData) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateData(toDoData)
        }
    }

    fun deleteSingleDataItem(toDoData: ToDoData) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteData(toDoData)
        }
    }

    fun deleteAllData() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteAllData()
        }
    }

    fun searchDatabase(search : String, callbackResult: (List<ToDoData>) -> Unit){
        viewModelScope.launch(Dispatchers.IO) {
            val result = repository.searchDatabase(search)
            viewModelScope.launch(Dispatchers.Main) {
                callbackResult(result)
            }
        }
    }

}