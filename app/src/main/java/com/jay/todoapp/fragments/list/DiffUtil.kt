package com.jay.todoapp.fragments.list

import androidx.recyclerview.widget.DiffUtil
import com.jay.todoapp.data.model.ToDoData

class ToDoDiffUtil(private val oldList : List<ToDoData>, private val newList : List<ToDoData>) : DiffUtil.Callback(){
    override fun getOldListSize(): Int = oldList.size

    override fun getNewListSize(): Int  = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].id == newList[newItemPosition].id
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition] == newList[newItemPosition]
    }
}