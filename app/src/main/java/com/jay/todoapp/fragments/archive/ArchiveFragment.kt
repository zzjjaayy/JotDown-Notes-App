package com.jay.todoapp.fragments.archive

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.activity.addCallback
import androidx.appcompat.widget.SearchView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.updateLayoutParams
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.jay.todoapp.R
import com.jay.todoapp.ToDoSharedViewModel
import com.jay.todoapp.data.model.ToDoData
import com.jay.todoapp.data.viewModel.ToDoDbViewModel
import com.jay.todoapp.databinding.FragmentListBinding
import com.jay.todoapp.fragments.list.ListFragment
import com.jay.todoapp.utils.SwipeToArchive
import com.jay.todoapp.utils.hideKeyboard
import jp.wasabeef.recyclerview.animators.SlideInUpAnimator

class ArchiveFragment : Fragment(), SearchView.OnQueryTextListener {

    private val sharedViewModel : ToDoSharedViewModel by activityViewModels()
    private val dbViewModel : ToDoDbViewModel by activityViewModels()

    // Binding
    private var _binding : FragmentListBinding? = null
    private val binding get() = _binding!!

    // RecyclerView Adapter
    private lateinit var mAdapter: ArchiveAdapter

    // Search View
    private lateinit var searchView : SearchView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        requireActivity().onBackPressedDispatcher.addCallback(this){
            if(!searchView.isIconified) {
                searchView.setQuery("", true)
                searchView.isIconified = true
                binding.sortStatus.visibility = View.VISIBLE
            } else findNavController().navigate(R.id.action_archiveFragment_to_listFragment)
        }
        // Inflate the layout for this fragment
        _binding = DataBindingUtil.inflate(inflater, R.layout.fragment_list, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            floatingActionButton.hide()
            lifecycleOwner = this@ArchiveFragment
            viewModel = dbViewModel

            sortStatus.text = getString(R.string.sort_template, ListFragment.LATEST_SORT)
            extendedFab.setIconResource(R.drawable.ic_arrow_upward_24)
            extendedFab.text = getString(R.string.all_notes)
            // This is to change the constraints of the FAB
            extendedFab.updateLayoutParams<ConstraintLayout.LayoutParams> {
                endToEnd = view.id
            }
            extendedFab.setOnClickListener {
                if(!searchView.isIconified) {
                    searchView.setQuery("", true)
                    searchView.isIconified = true
                    binding.sortStatus.visibility = View.VISIBLE
                }
                findNavController().navigate(R.id.action_archiveFragment_to_listFragment)
            }
        }
        dbViewModel.getAllArchive.observe(viewLifecycleOwner, {
            Log.d("Jayischecking", "archive list is -> $it")
            mAdapter.setData(it)
        })
        setUpRecyclerView()
        setHasOptionsMenu(true)
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
        swipeToUnArchive(binding.notesListRecyclerView)
    }

    private fun swipeToUnArchive(recyclerView: RecyclerView) {
        val swipeToUnarchiveCallback = object : SwipeToArchive() {
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
        val itemTouchHelper = ItemTouchHelper(swipeToUnarchiveCallback)
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    /*
    * MENU OPTIONS
    * */

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.list_fragment_menu, menu)

        menu.findItem(R.id.menu_delete_all).isVisible = false

        val search = menu.findItem(R.id.menu_search)
        searchView = (search.actionView as? SearchView)!!
        searchView.setOnQueryTextListener(this)

        searchView.setOnSearchClickListener {
            binding.sortStatus.visibility = View.GONE
            setItemsVisibility(menu, search, false)
        }
        searchView.setOnCloseListener {
            binding.sortStatus.visibility = View.VISIBLE
            setItemsVisibility(menu, search, true)
            false
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_sort_new -> {
                dbViewModel.getAllArchive.observe(this, { mAdapter.setData(it) })
                binding.sortStatus.text = getString(R.string.sort_template,
                    ListFragment.LATEST_SORT
                )
            }
            R.id.menu_sort_old -> {
                dbViewModel.getAllArchiveOldFirst.observe(this, { mAdapter.setData(it) })
                binding.sortStatus.text = getString(R.string.sort_template,
                    ListFragment.OLDEST_SORT
                )
            }
            R.id.menu_priority_high -> {
                dbViewModel.getArchiveByHighPriority.observe(this, { mAdapter.setData(it) })
                binding.sortStatus.text = getString(R.string.sort_template, ListFragment.HIGH_SORT)
            }
            R.id.menu_priority_low -> {
                dbViewModel.getArchiveByLowPriority.observe(this, { mAdapter.setData(it) })
                binding.sortStatus.text = getString(R.string.sort_template, ListFragment.LOW_SORT)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    /*
    * SEARCH VIEW RELATED FUNCTIONS
    * */

    // Triggered when you hit enter
    override fun onQueryTextSubmit(query: String?): Boolean {
        if(query != null) {
            searchQueryInDb(query)
        }
        return true
    }

    // Triggered when you start typing
    override fun onQueryTextChange(query: String?): Boolean {
        if(query != null) {
            searchQueryInDb(query)
        }
        return true
    }

    private fun searchQueryInDb(query: String?) {
        val searchQuery = "%$query%"
        dbViewModel.searchAllArchive(searchQuery){
            mAdapter.setData(it)
        }
    }

    private fun setItemsVisibility(menu: Menu, exception: MenuItem, visible: Boolean) {
        for (i in 0 until menu.size()) {
            val item = menu.getItem(i)
            if (item !== exception) item.isVisible = visible
        }
    }
}