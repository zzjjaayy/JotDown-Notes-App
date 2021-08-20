package com.jay.todoapp.fragments.list

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.jay.todoapp.R
import com.jay.todoapp.data.model.Priority
import com.jay.todoapp.data.model.ToDoData

class ToDoAdapter(private val onToDoClicked: (ToDoData) -> Unit) : RecyclerView.Adapter<ToDoAdapter.ToDoViewHolder>(){

    private var dataSet: List<ToDoData> = emptyList()

    class ToDoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val toDoTitle: TextView = view.findViewById(R.id.to_do_title)
        val toDoDesc: TextView = view.findViewById(R.id.to_do_description)
        val toDoPriority: CardView = view.findViewById(R.id.priority_indicator)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ToDoViewHolder {
        val adapterLayout = LayoutInflater.from(parent.context).inflate(R.layout.to_do_layout, parent, false)
        return ToDoViewHolder(adapterLayout)
    }

    override fun onBindViewHolder(holder: ToDoViewHolder, position: Int) {
        val item = dataSet[position]
        holder.toDoTitle.text = item.title
        holder.toDoDesc.text = item.description
        when(item.priority) {
            Priority.HIGH -> holder.toDoPriority.setCardBackgroundColor(ContextCompat.getColor(holder.itemView.context, R.color.red))
            Priority.MEDIUM -> holder.toDoPriority.setCardBackgroundColor(ContextCompat.getColor(holder.itemView.context, R.color.yellow))
            Priority.LOW -> holder.toDoPriority.setCardBackgroundColor(ContextCompat.getColor(holder.itemView.context, R.color.green))
        }
        holder.itemView.setOnClickListener {
            onToDoClicked(getItem(holder.adapterPosition))
        }
    }

    fun getItem(position: Int) : ToDoData = dataSet[position]

    override fun getItemCount(): Int = dataSet.size

    fun setData(toDoData: List<ToDoData>) {
        dataSet = toDoData
        notifyDataSetChanged()
    }
}