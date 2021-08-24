package com.jay.todoapp.fragments.list

import android.app.AlertDialog
import android.os.Bundle
import android.view.*
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
import com.jay.todoapp.data.model.ToDoData
import com.jay.todoapp.data.viewModel.ToDoViewModel
import com.jay.todoapp.databinding.FragmentListBinding
import com.jay.todoapp.utils.hideKeyboard
import jp.wasabeef.recyclerview.animators.SlideInUpAnimator


class ListFragment : Fragment(), SearchView.OnQueryTextListener {

    /*
    * GLOBAL VARIABLES
    * */
    // ViewModel
    private val sharedViewModel : ToDoViewModel by activityViewModels()

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
            viewModel = sharedViewModel

            // This observer will change the isEmpty live data every time the data set is changed
            sharedViewModel.getAllData.observe(viewLifecycleOwner, {
                sharedViewModel.checkIfDbEmpty(it) // passing the new list to the checker
                mAdapter.setData(it)
            })
        }
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
        mAdapter = ToDoAdapter {
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
        swipeToDelete(binding.notesListRecyclerView)
    }

    private fun swipeToDelete(recyclerView: RecyclerView) {
        val swipeToDeleteCallback = object : SwipeToDelete() {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val item = mAdapter.dataSet[viewHolder.adapterPosition]
                sharedViewModel.deleteSingleItemFromDb(item.id,
                    item.title,
                    item.description,
                    sharedViewModel.parsedPriority(item.priority)
                )
                mAdapter.notifyItemRemoved(viewHolder.adapterPosition)
                restoreDeletedItem(viewHolder.itemView, item)
            }
        }

        val itemTouchHelper = ItemTouchHelper(swipeToDeleteCallback)
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    private fun restoreDeletedItem(view: View, deletedItem: ToDoData) {
        val snackBar = Snackbar.make(
            view, "Deleted ${deletedItem.title}", Snackbar.LENGTH_LONG
        )
        snackBar.setAction("Undo"){
            sharedViewModel.insertDataToDb(deletedItem.id,
                deletedItem.title,
                deletedItem.description,
                sharedViewModel.parsedPriority(deletedItem.priority)
            )
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
            setItemsVisibility(menu, search, false)
        }

        searchView.setOnCloseListener {
            setItemsVisibility(menu, search, true)
            false
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_delete_all -> confirmRemoval()
            R.id.menu_sort_new -> sharedViewModel.getAllData.observe(this, {mAdapter.setData(it)})
            R.id.menu_sort_old -> sharedViewModel.getAllDataOldFirst.observe(this, {mAdapter.setData(it)})
            R.id.menu_priority_high -> sharedViewModel.getDataByHighPriority.observe(this, {mAdapter.setData(it)})
            R.id.menu_priority_low -> sharedViewModel.getDataByLowPriority.observe(this, {mAdapter.setData(it)})
        }
        return super.onOptionsItemSelected(item)
    }

    // Confirms the removal of all items with a dialog box
    private fun confirmRemoval() {
        val alertDialogBuilder = AlertDialog.Builder(requireContext())
        alertDialogBuilder.setPositiveButton("Yes") { _, _ ->
            sharedViewModel.deleteAllData()
            Toast.makeText(context, "Successfully Deleted All TODOs", Toast.LENGTH_SHORT).show()
        }
        alertDialogBuilder.setNegativeButton("No") {_,_ -> } // Nothing should happen
        alertDialogBuilder.setTitle("Delete all TODOs?")
        alertDialogBuilder.setMessage("Are you sure you want to delete all TODOs?")
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
        sharedViewModel.searchDatabase(searchQuery){
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