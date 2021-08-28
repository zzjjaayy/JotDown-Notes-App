package com.jay.todoapp.fragments.list

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
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
import com.jay.todoapp.utils.SwipeToArchive
import com.jay.todoapp.utils.hideKeyboard
import jp.wasabeef.recyclerview.animators.SlideInUpAnimator


class ListFragment : Fragment(), SearchView.OnQueryTextListener {

    companion object {
        var LATEST_SORT = "Latest First"
        var OLDEST_SORT = "Oldest First"
        var HIGH_SORT = "High to Low Priority"
        var LOW_SORT = "Low to High Priority"
    }

    /*
    * GLOBAL VARIABLES
    * */
    // ViewModels
    private val sharedViewModel : ToDoSharedViewModel by activityViewModels()
    private val dbViewModel : ToDoDbViewModel by activityViewModels()

    // Binding
    private var _binding : FragmentListBinding? = null
    private val binding get() = _binding!!

    // RecyclerView Adapter
    private lateinit var mAdapter: ToDoAdapter

    // Search View
    private lateinit var searchView : SearchView

    /*
    * LIFECYCLE FUNCTIONS
    * */
    override fun onCreateView(
           inflater: LayoutInflater, container: ViewGroup?,
           savedInstanceState: Bundle?
       ): View? {
            (activity as AppCompatActivity?)!!.supportActionBar!!.show()

            requireActivity().onBackPressedDispatcher.addCallback(this){
                if(!searchView.isIconified) {
                    searchView.setQuery("", true)
                    searchView.isIconified = true
                    binding.sortStatus.visibility = View.VISIBLE
                } else activity?.finish()
            }

            // Inflate the layout for this fragment
            _binding = DataBindingUtil.inflate(inflater, R.layout.fragment_list, container, false)
            return binding.root
       }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            // Setting lifecycle owner so Data Binding can observe the LiveData
            lifecycleOwner = this@ListFragment
            viewModel = dbViewModel

            sortStatus.text = getString(R.string.sort_template, LATEST_SORT)
            floatingActionButton.setOnClickListener {
                findNavController().navigate(R.id.action_listFragment_to_addFragment)
            }
            extendedFab.setOnClickListener {
                if(!searchView.isIconified) {
                    searchView.setQuery("", true)
                    searchView.isIconified = true
                    binding.sortStatus.visibility = View.VISIBLE
                }
                findNavController().navigate(R.id.action_listFragment_to_archiveFragment)
            }
        }
        // This observer will change the isEmpty live data every time the data set is changed
        dbViewModel.getAllData.observe(viewLifecycleOwner, {
            dbViewModel.checkIfDbEmpty(it) // passing the new list to the checker
            mAdapter.setData(it)
        })

        setUpRecyclerView()
        setHasOptionsMenu(true)
        hideKeyboard(requireActivity())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /*
    * RECYCLER VIEW & SWIPE FUNCTIONALITY
    * */

    private fun setUpRecyclerView() {
        // setting an adapter to the recycler view
        mAdapter = ToDoAdapter ({
            updateShortcut(it)
        }, { data, view ->
            val popup = PopupMenu(requireContext(), view)
            popup.menuInflater.inflate(R.menu.item_long_press_menu, popup.menu)
            popup.menu.findItem(R.id.menu_press_unarchive).isVisible = false

            try {
                val fieldMPopup = PopupMenu::class.java.getDeclaredField("mPopup")
                fieldMPopup.isAccessible = true
                val mPopup = fieldMPopup.get(popup)
                mPopup.javaClass
                    .getDeclaredMethod("setForceShowIcon", Boolean::class.java)
                    .invoke(mPopup, true)
            } catch (e: Exception){
                Log.e("Main", "Error showing menu icons.", e)
            } finally {
                popup.show()
            }

            popup.setOnMenuItemClickListener { menuItem: MenuItem ->
                when(menuItem.itemId){
                    R.id.menu_press_update -> updateShortcut(data)
                    R.id.menu_press_delete -> deleteShortcut(data)
                    R.id.menu_press_archive -> {
                        archiveShortcut(view, data, ToDoArchive(
                            0, data.id, data.priority, data.title, data.description
                        ))
                    }
                }
                true
            }
        })
        binding.notesListRecyclerView.adapter = mAdapter
        // these functions and properties belong to a third party library by "github/wasabeef"
        binding.notesListRecyclerView.itemAnimator = SlideInUpAnimator().apply{
            addDuration = 300
        }
        swipeToArchive(binding.notesListRecyclerView)
    }

    private fun updateShortcut(it : ToDoData) {
        val action = ListFragmentDirections.actionListFragmentToUpdateFragment(
            currentTitle = it.title,
            currentDesc = it.description,
            currentPriority = it.priority.name,
            currentId = it.id,
            returnDestination = "List",
            currentOldId = -1
        )
        findNavController().navigate(action)
    }

    private fun deleteShortcut(toDoData: ToDoData) {
        val alertDialogBuilder = AlertDialog.Builder(requireContext())
        alertDialogBuilder.setPositiveButton("Yes") { _, _ ->
            dbViewModel.deleteSingleDataItem(toDoData)
            Toast.makeText(context, "Successfully Deleted", Toast.LENGTH_SHORT).show()
        }
        alertDialogBuilder.setNegativeButton("No") {_,_ -> } // Nothing should happen
        alertDialogBuilder.setTitle("Delete this Todo?")
        alertDialogBuilder.setMessage("Caution : This is an irreversible action")
        alertDialogBuilder.create().show()
    }

    private fun archiveShortcut(view: View, item: ToDoData, itemToBeArchived: ToDoArchive) {
        dbViewModel.insertArchive(itemToBeArchived)
        dbViewModel.deleteSingleDataItem(item)
        restoreDeletedItem(view, item, itemToBeArchived)
    }

    private fun swipeToArchive(recyclerView: RecyclerView) {
        val swipeToArchiveCallback = object : SwipeToArchive() {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val item = mAdapter.dataSet[viewHolder.adapterPosition]
                val itemToBeArchived = ToDoArchive(
                    0,
                    item.id,
                    item.priority,
                    item.title,
                    item.description
                )
                archiveShortcut(viewHolder.itemView, item, itemToBeArchived)
            }
        }
        val itemTouchHelper = ItemTouchHelper(swipeToArchiveCallback)
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    private fun restoreDeletedItem(view: View, deletedItem: ToDoData, archivedItem : ToDoArchive) {
        val snackBar = Snackbar.make(
            view, "Archived '${deletedItem.title}'", Snackbar.LENGTH_LONG
        )
        snackBar.setAction("Undo"){
            dbViewModel.insertData(deletedItem)
            dbViewModel.deleteSingleArchive(archivedItem)
        }
        snackBar.show()
    }

    /*
    * MENU FUNCTIONS
    * */

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.list_fragment_menu, menu)

        val search = menu.findItem(R.id.menu_search)
        searchView = (search.actionView as? SearchView)!!
        searchView.setOnQueryTextListener(this)

        searchView.setOnSearchClickListener {
            binding.sortStatus.visibility = View.GONE
            setItemsVisibility(menu, search, false)
        }

        searchView.setOnCloseListener {
            binding.sortStatus.visibility = View.INVISIBLE
            setItemsVisibility(menu, search, true)
            false
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_delete_all -> confirmRemoval()
            R.id.menu_sort_new -> {
                dbViewModel.getAllData.observe(this, { mAdapter.setData(it) })
                binding.sortStatus.text = getString(R.string.sort_template, LATEST_SORT)
            }
            R.id.menu_sort_old -> {
                dbViewModel.getAllDataOldFirst.observe(this, { mAdapter.setData(it) })
                binding.sortStatus.text = getString(R.string.sort_template, OLDEST_SORT)
            }
            R.id.menu_priority_high -> {
                dbViewModel.getDataByHighPriority.observe(this, { mAdapter.setData(it) })
                binding.sortStatus.text = getString(R.string.sort_template, HIGH_SORT)
            }
            R.id.menu_priority_low -> {
                dbViewModel.getDataByLowPriority.observe(this, { mAdapter.setData(it) })
                binding.sortStatus.text = getString(R.string.sort_template, LOW_SORT)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    // Confirms the removal of all items with a dialog box
    private fun confirmRemoval() {
        val alertDialogBuilder = AlertDialog.Builder(requireContext())
        alertDialogBuilder.setPositiveButton("Yes") { _, _ ->
            dbViewModel.deleteAllData()
            Toast.makeText(context, "Successfully Deleted All TODOs", Toast.LENGTH_SHORT).show()
        }
        alertDialogBuilder.setNegativeButton("No") {_,_ -> } // Nothing should happen
        alertDialogBuilder.setTitle("Delete all TODOs?")
        alertDialogBuilder.setMessage("Caution : This is an irreversible action")
        alertDialogBuilder.create().show()
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
        dbViewModel.searchAllData(searchQuery){
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