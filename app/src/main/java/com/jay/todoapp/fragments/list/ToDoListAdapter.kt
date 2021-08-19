package com.jay.todoapp.fragments.list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.jay.todoapp.R
import com.jay.todoapp.data.model.Priority
import com.jay.todoapp.data.model.ToDoData
import com.jay.todoapp.databinding.ToDoLayoutBinding

class ToDoListAdapter(private val onToDoClicked: (ToDoData) -> Unit) : ListAdapter<ToDoData, ToDoListAdapter.ToDoViewHolder>(DiffCallback) {

    class ToDoViewHolder(private val binding: ToDoLayoutBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(toDoData: ToDoData) {
            binding.toDoTitle.text = toDoData.title
            binding.toDoDescription.text = toDoData.description
            when(toDoData.priority) {
                Priority.HIGH -> binding.priorityIndicator.setCardBackgroundColor(ContextCompat.getColor(binding.root.context, R.color.red))
                Priority.MEDIUM -> binding.priorityIndicator.setCardBackgroundColor(ContextCompat.getColor(binding.root.context, R.color.yellow))
                Priority.LOW -> binding.priorityIndicator.setCardBackgroundColor(ContextCompat.getColor(binding.root.context, R.color.green))
            }
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
        val viewHolder = ToDoViewHolder(
            ToDoLayoutBinding.inflate(
            LayoutInflater.from(parent.context), parent, false))
        // passing the parent view is important to inflate all XML properties.

        viewHolder.itemView.setOnClickListener {
            onToDoClicked(getItem(viewHolder.adapterPosition))
        }
        return viewHolder
    }

    override fun onBindViewHolder(holder: ToDoViewHolder, position: Int) {
        holder.bind(getItem(position))
    }


}