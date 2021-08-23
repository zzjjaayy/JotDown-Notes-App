package com.jay.todoapp

import android.view.View
import androidx.databinding.BindingAdapter
import androidx.navigation.findNavController
import com.google.android.material.floatingactionbutton.FloatingActionButton

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