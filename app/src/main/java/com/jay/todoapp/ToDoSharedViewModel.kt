package com.jay.todoapp

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import com.jay.todoapp.data.model.Priority

class ToDoSharedViewModel(application: Application) : AndroidViewModel(application) {

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
            "High" -> {
                Priority.HIGH}
            "Medium" -> {
                Priority.MEDIUM}
            else -> {
                Priority.LOW}
        }
    }

}