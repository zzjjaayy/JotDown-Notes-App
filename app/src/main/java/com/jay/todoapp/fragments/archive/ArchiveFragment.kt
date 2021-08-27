package com.jay.todoapp.fragments.archive

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.jay.todoapp.R
import com.jay.todoapp.ToDoSharedViewModel
import com.jay.todoapp.data.model.ToDoArchive
import com.jay.todoapp.data.model.ToDoData
import com.jay.todoapp.data.viewModel.ToDoDbViewModel
import com.jay.todoapp.databinding.FragmentListBinding
import com.jay.todoapp.fragments.list.ListFragmentDirections
import com.jay.todoapp.fragments.list.ToDoAdapter
import com.jay.todoapp.fragments.update.UpdateFragment
import com.jay.todoapp.utils.SwipeToDelete
import com.jay.todoapp.utils.hideKeyboard
import jp.wasabeef.recyclerview.animators.SlideInUpAnimator

class ArchiveFragment : Fragment() {

    private val sharedViewModel : ToDoSharedViewModel by activityViewModels()
    private val dbViewModel : ToDoDbViewModel by activityViewModels()

    // Binding
    private var _binding : FragmentListBinding? = null
    private val binding get() = _binding!!

    // RecyclerView Adapter
    private lateinit var mAdapter: ArchiveAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        requireActivity().onBackPressedDispatcher.addCallback(this){
            findNavController().navigate(R.id.action_archiveFragment_to_listFragment)
        }
        // Inflate the layout for this fragment
        _binding = DataBindingUtil.inflate(inflater, R.layout.fragment_list, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            floatingActionButton2.hide()
            lifecycleOwner = this@ArchiveFragment
            viewModel = dbViewModel
            floatingActionButton.setImageResource(R.drawable.ic_arrow_back_24)
            floatingActionButton.setOnClickListener {
                findNavController().navigate(R.id.action_archiveFragment_to_listFragment)
            }
        }
        dbViewModel.getAllArchive.observe(viewLifecycleOwner, {
            Log.d("Jayischecking", "archive list is -> $it")
            mAdapter.setData(it)
        })
        setUpRecyclerView()
        hideKeyboard(requireActivity())
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    private fun setUpRecyclerView() {
        // setting an adapter to the recycler view
        mAdapter = ArchiveAdapter {
            val action = ArchiveFragmentDirections.actionArchiveFragmentToUpdateFragment(
                currentTitle = it.title,
                currentDesc = it.description,
                currentPriority = it.priority.name,
                currentId = it.id,
                returnDestination = "Archive",
                currentOldId = it.oldId
            )
            findNavController().navigate(action)
        }
        binding.notesListRecyclerView.adapter = mAdapter
        // these functions and properties belong to a third party library by "github/wasabeef"
        binding.notesListRecyclerView.itemAnimator = SlideInUpAnimator().apply{
            addDuration = 300
        }
        swipeToDelete(binding.notesListRecyclerView)
    }

    private fun swipeToDelete(recyclerView: RecyclerView) {
        val swipeToDeleteCallback = object : SwipeToDelete() {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val item = mAdapter.dataSet[viewHolder.adapterPosition]

                dbViewModel.deleteSingleArchive(item)
                val restoredItem = ToDoData(
                    item.oldId,
                    item.priority,
                    item.title,
                    item.description
                )
                dbViewModel.insertData(restoredItem)
//                mAdapter.notifyItemRemoved(viewHolder.adapterPosition)
                Snackbar.make(binding.listLayout, "Removed from Archive", Snackbar.LENGTH_LONG)
                    .show()
            }
        }
        val itemTouchHelper = ItemTouchHelper(swipeToDeleteCallback)
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }
}