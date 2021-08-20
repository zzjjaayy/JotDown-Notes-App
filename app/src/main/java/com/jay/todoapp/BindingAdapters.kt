package com.jay.todoapp

import android.view.View
import androidx.databinding.BindingAdapter
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.jay.todoapp.data.model.ToDoData
import com.jay.todoapp.fragments.list.ToDoListAdapter

//@BindingAdapter("listData")
//fun bindRecyclerView(recyclerView: RecyclerView,
//                     data: List<ToDoData>?) {
//    val adapter = recyclerView.adapter as ToDoListAdapter
//    adapter.submitList(data)
//}

@BindingAdapter("navigateToAddFragment")
fun navigateToAddFragment(view: FloatingActionButton, navigate: Boolean) {
    view.setOnClickListener {
        if(navigate) view.findNavController().navigate(R.id.action_listFragment_to_addFragment)
    }
}

@BindingAdapter("emptyDatabase")
fun emptyDatabase(view: View, emptyDb : Boolean) {
    if(emptyDb) {
        view.visibility = View.VISIBLE
    } else view.visibility = View.INVISIBLE
}