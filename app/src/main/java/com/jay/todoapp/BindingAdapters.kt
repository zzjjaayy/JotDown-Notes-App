package com.jay.todoapp

import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.jay.todoapp.data.model.ToDoData
import com.jay.todoapp.fragments.list.ToDoListAdapter

@BindingAdapter("listData")
fun bindRecyclerView(recyclerView: RecyclerView,
                     data: List<ToDoData>?) {
    val adapter = recyclerView.adapter as ToDoListAdapter
    adapter.submitList(data)
}