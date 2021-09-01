package com.jay.todoapp.fragments.archive

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.jay.todoapp.R
import com.jay.todoapp.data.model.Priority
import com.jay.todoapp.data.model.ToDoArchive
import com.jay.todoapp.utils.archiveDiffUtil

class ArchiveAdapter(private val onArchiveClicked: (ToDoArchive) -> Unit,
                     private val onArchiveLongPressed: (ToDoArchive, View) -> Unit)
    : RecyclerView.Adapter<ArchiveAdapter.ArchiveViewHolder>(){

    var dataSet: List<ToDoArchive> = emptyList()

    class ArchiveViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val toDoTitle: TextView = view.findViewById(R.id.to_do_title)
        val toDoDesc: TextView = view.findViewById(R.id.to_do_description)
        val toDoPriority: CardView = view.findViewById(R.id.priority_indicator)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArchiveViewHolder {
        val adapterLayout =
            LayoutInflater.from(parent.context).inflate(R.layout.to_do_layout, parent, false)
        return ArchiveViewHolder(adapterLayout)
    }

    override fun onBindViewHolder(holder: ArchiveViewHolder, position: Int) {
        val item = dataSet[position]
        holder.toDoTitle.text = item.title
        holder.toDoDesc.text = item.description
        when (item.priority) {
            Priority.HIGH -> holder.toDoPriority.setCardBackgroundColor(
                ContextCompat.getColor(
                    holder.itemView.context,
                    R.color.red
                )
            )
            Priority.MEDIUM -> holder.toDoPriority.setCardBackgroundColor(
                ContextCompat.getColor(
                    holder.itemView.context,
                    R.color.yellow
                )
            )
            Priority.LOW -> holder.toDoPriority.setCardBackgroundColor(
                ContextCompat.getColor(
                    holder.itemView.context,
                    R.color.green
                )
            )
        }
        holder.itemView.setOnClickListener {
            onArchiveClicked(getItem(holder.adapterPosition))
        }
        holder.itemView.setOnLongClickListener {
            onArchiveLongPressed(getItem(holder.adapterPosition), holder.itemView)
            true
        }
    }

    private fun getItem(position: Int): ToDoArchive = dataSet[position]

    override fun getItemCount(): Int = dataSet.size

    fun setData(toDoData: List<ToDoArchive>) {
        val diffUtil = archiveDiffUtil(dataSet, toDoData)
        val diffUtilResult = DiffUtil.calculateDiff(diffUtil) // This will calculate the difference
        dataSet = toDoData
        diffUtilResult.dispatchUpdatesTo(this)
    }
}