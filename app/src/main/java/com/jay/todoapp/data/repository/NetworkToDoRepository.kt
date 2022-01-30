package com.jay.todoapp.data.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.jay.todoapp.data.model.ToDo
import com.jay.todoapp.utils.LOG_TAG

class NetworkToDoRepository private constructor() {

    private val fireStore = Firebase.firestore
    private val mAuth = FirebaseAuth.getInstance()
    private var currentUserCollection : CollectionReference? =
        mAuth.currentUser?.uid?.let { fireStore.collection(it) }

    companion object {
        private var INSTANCE : NetworkToDoRepository? = null
        fun getInstance() : NetworkToDoRepository {
            if(INSTANCE == null) {
                return NetworkToDoRepository()
            }
            return INSTANCE!!
        }
    }

    fun getAllNotes(resultCallback: (MutableMap<String, ToDo>) -> Unit) {
        currentUserCollection?.addSnapshotListener { value, error ->
            if (error != null) {
                Log.w(LOG_TAG, "Listen failed.", error)
                return@addSnapshotListener
            }
            value?.mapNotNull { docSnap ->
                docSnap.id to docSnap.toObject(ToDo::class.java).also { it.id=docSnap.id }
            }?.toMap()?.toMutableMap()?.also { resultCallback(it) }
        }
    }

    fun addNote(toDo: ToDo) {
        val ref = currentUserCollection?.add(toDo)
        ref?.addOnCompleteListener {
            Log.d(LOG_TAG, "id is ${it.result.id}")
        }?.addOnFailureListener {
            Log.d(LOG_TAG, "Something went wrong while adding new note -> ${it.stackTraceToString()}")
        }
    }

    fun updateNote(toDo: ToDo) {
        Log.d(LOG_TAG, "id is ${toDo.id}")
        currentUserCollection?.document(toDo.id)?.also {
            toDo.id = ""
            Log.d(LOG_TAG, "todo created -> ${toDo.createdTS}")
            it.set(toDo)
        }
    }

    fun deleteNote(id: String) = currentUserCollection?.document(id)?.delete()
}