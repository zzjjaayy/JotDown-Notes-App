package com.jay.todoapp.fragments.archive

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.jay.todoapp.R
import com.jay.todoapp.ToDoSharedViewModel
import com.jay.todoapp.data.viewModel.ToDoDbViewModel
import com.jay.todoapp.databinding.FragmentListBinding
import com.jay.todoapp.fragments.list.ListFragmentDirections
import com.jay.todoapp.fragments.list.ToDoAdapter
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
        }
        dbViewModel.getAllArchive.observe(viewLifecycleOwner, {
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
            val action = ListFragmentDirections.actionListFragmentToUpdateFragment(
                currentTitle = it.title,
                currentDesc = it.description,
                currentPriority = it.priority.name,
                currentId = it.id
            )
            findNavController().navigate(action)
        }
        binding.notesListRecyclerView.adapter = mAdapter
        // these functions and properties belong to a third party library by "github/wasabeef"
        binding.notesListRecyclerView.itemAnimator = SlideInUpAnimator().apply{
            addDuration = 300
        }
//        swipeToDelete(binding.notesListRecyclerView)
    }
}