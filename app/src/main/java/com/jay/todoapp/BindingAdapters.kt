package com.jay.todoapp

import android.view.View
import androidx.databinding.BindingAdapter
import androidx.navigation.findNavController
import com.google.android.material.floatingactionbutton.FloatingActionButton

@BindingAdapter("emptyDatabase")
fun emptyDatabase(view: View, emptyDb : Boolean) {
    if(emptyDb) {
        view.visibility = View.VISIBLE
    } else view.visibility = View.INVISIBLE
}