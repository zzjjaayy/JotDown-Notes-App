package com.jay.todoapp.fragments.list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.jay.todoapp.data.model.ToDoData
import com.jay.todoapp.databinding.ToDoLayoutBinding

class ToDoListAdapter : ListAdapter<ToDoData, ToDoListAdapter.ToDoViewHolder>(DiffCallback) {

    class ToDoViewHolder(private val binding: ToDoLayoutBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(toDoData: ToDoData) {
            binding.toDoTitle.text = toDoData.title
            binding.toDoDescription.text = toDoData.description
        }
    }

    companion object DiffCallback: DiffUtil.ItemCallback<ToDoData>(){
        override fun areItemsTheSame(oldItem: ToDoData, newItem: ToDoData): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ToDoData, newItem: ToDoData): Boolean {
            return oldItem == newItem
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ToDoViewHolder {
        return ToDoViewHolder(ToDoLayoutBinding.inflate(
            LayoutInflater.from(parent.context)))
    }

    override fun onBindViewHolder(holder: ToDoViewHolder, position: Int) {
        holder.bind(getItem(position))
    }


}