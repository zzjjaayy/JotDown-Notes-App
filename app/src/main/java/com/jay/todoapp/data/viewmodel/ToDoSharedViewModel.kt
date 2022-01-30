package com.jay.todoapp.data.viewmodel

import android.app.Application
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.jay.todoapp.data.model.ListSource
import com.jay.todoapp.data.model.Priority
import com.jay.todoapp.data.model.SortOrder
import com.jay.todoapp.data.model.ToDo
import com.jay.todoapp.data.repository.NetworkToDoRepository
import com.jay.todoapp.utils.LOG_TAG
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ToDoSharedViewModel(application: Application) : AndroidViewModel(application) {

    private val networkRepo = NetworkToDoRepository.getInstance()

    var mapOfDocIdWithAllToDo = mutableMapOf<String, ToDo>()
    private var mainList : List<ToDo> = emptyList()
    private var archivedList : List<ToDo> = emptyList()
    private var listSource: ListSource = ListSource.MAIN

    var mainSortOrder : SortOrder = SortOrder.LATEST_FIRST
    var archivedSortOrder : SortOrder = SortOrder.LATEST_FIRST

    val toDoList = MutableLiveData<List<ToDo>>()

    var isMainListEmpty : Boolean = true
    var isArchivedListEmpty : Boolean = true

    init {
        viewModelScope.launch(Dispatchers.IO) {
            networkRepo.getAllNotes {
                mapOfDocIdWithAllToDo = it
                Log.d(LOG_TAG, "there are ${it.size} items")
                it.map { it.value }.partition { !it.isArchived }.also {
                    mainList = it.first
                    archivedList = it.second
                    isMainListEmpty = it.first.isNullOrEmpty()
                    isArchivedListEmpty = it.second.isNullOrEmpty()
                }
                setSortedListToLiveData(listSource)
            }
        }
    }

    fun addNote(toDo: ToDo) = viewModelScope.launch(Dispatchers.IO) { networkRepo.addNote(toDo) }

    fun updateNote(toDo: ToDo) = viewModelScope.launch(Dispatchers.IO) { networkRepo.updateNote(toDo) }

    fun deleteNote(id: String) = viewModelScope.launch(Dispatchers.IO) { networkRepo.deleteNote(id) }

    fun setSource(source: ListSource) {
        listSource = source
        setSortedListToLiveData(source)
    }

    fun setSortedListToLiveData(source: ListSource) {
        val list: List<ToDo>
        val order = when(source) {
            ListSource.MAIN -> {
                list = mainList
                mainSortOrder
            }
            ListSource.ARCHIVE -> {
                list = archivedList
                archivedSortOrder
            }
        }
        toDoList.postValue(
            when(order) {
                SortOrder.LATEST_FIRST -> list.sortedByDescending { it.createdTS }
                SortOrder.OLDEST_FIRST -> list.sortedBy { it.createdTS }
                SortOrder.HIGH_PRIORITY -> list.sortedBy { it.priority.ordinal }
                SortOrder.LOW_PRIORITY -> list.sortedByDescending { it.priority.ordinal }
            }
        )
    }

    fun searchNotes(query: String) : List<ToDo>{
        val list = mutableListOf<ToDo>()
        toDoList.value?.let {
            for(item in it) {
                if(item.title.contains(query) || item.description.contains(query)) {
                    list.add(item)
                }
            }
        }
        return list
    }

    fun verifyUserData(title: String, priority: String) : Boolean{
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

    fun parseStringToPriority(priority: String) : Priority {
        return when(priority) {
            "High" -> Priority.HIGH
            "Medium" -> Priority.MEDIUM
            else -> Priority.LOW
        }
    }

    fun getStatusText(source: ListSource): String {
        val order = when(source) {
            ListSource.MAIN -> mainSortOrder
            ListSource.ARCHIVE -> archivedSortOrder
        }
        return when(order) {
            SortOrder.LATEST_FIRST -> "Latest First"
            SortOrder.OLDEST_FIRST -> "Oldest First"
            SortOrder.HIGH_PRIORITY ->"High to Low Priority"
            SortOrder.LOW_PRIORITY ->"Low to High Priority"
        }
    }
}