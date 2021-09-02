package com.jay.todoapp.fragments.archive

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.PopupMenu
import android.widget.Toast
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
import com.jay.todoapp.data.model.ToDoArchive
import com.jay.todoapp.data.model.ToDoData
import com.jay.todoapp.data.viewModel.ToDoDbViewModel
import com.jay.todoapp.databinding.FragmentListBinding
import com.jay.todoapp.fragments.list.ListFragment
import com.jay.todoapp.utils.SwipeToArchive
import com.jay.todoapp.utils.hideKeyboard
import jp.wasabeef.recyclerview.animators.SlideInUpAnimator

class ArchiveFragment : Fragment(), SearchView.OnQueryTextListener {

    private val dbViewModel : ToDoDbViewModel by activityViewModels()

    // Binding
    private var _binding : FragmentListBinding? = null
    private val binding get() = _binding!!

    // RecyclerView Adapter
    private lateinit var mAdapter: ArchiveAdapter

    // Search View
    private lateinit var searchView : SearchView

    // This is to keep record of the present sorting option in dialog box
    private var selectedItem : Int = 0

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
            sortStatus.setOnClickListener{
                changeSorting()
            }
            noDataText.text = "No Archives Found"
            noDataTip.text = "Swipe left on any note to archive it!"
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
        dbViewModel.getAllArchiveNewFirst.observe(viewLifecycleOwner, {
            dbViewModel.checkIfArchiveEmpty(it)
            mAdapter.setData(it)
        })
        dbViewModel.isEmptyArchive.observe(viewLifecycleOwner, {
            changeVisibilityOfEmptyIndicators(it)
        })
        setUpRecyclerView()
        setHasOptionsMenu(true)
        hideKeyboard(requireActivity())
    }

    private fun changeVisibilityOfEmptyIndicators(isEmpty : Boolean) {
        val noDataImg = binding.noDataImage
        val noDataTxt = binding.noDataText
        val noDataTip = binding.noDataTip
        val sort = binding.sortStatus
        if(isEmpty) {
            sort.visibility = View.INVISIBLE
            noDataImg.visibility = View.VISIBLE
            noDataTxt.visibility = View.VISIBLE
            noDataTip.visibility = View.VISIBLE
        } else {
            sort.visibility = View.VISIBLE
            noDataImg.visibility = View.INVISIBLE
            noDataTxt.visibility = View.INVISIBLE
            noDataTip.visibility = View.INVISIBLE
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    private fun setUpRecyclerView() {
        // setting an adapter to the recycler view
        mAdapter = ArchiveAdapter ({
            updateShortcut(it)
        }, {data, view ->
            val popup = PopupMenu(requireContext(), view)
            popup.menuInflater.inflate(R.menu.item_long_press_menu, popup.menu)
            popup.menu.findItem(R.id.menu_press_archive).isVisible = false

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
                    R.id.menu_press_unarchive -> {
                        unarchiveShortcut(view, ToDoData(
                            data.oldId, data.priority, data.title, data.description
                        ), data)
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
        swipeToUnArchive(binding.notesListRecyclerView)
    }

    private fun updateShortcut(it : ToDoArchive) {
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

    private fun deleteShortcut(toDoArchive: ToDoArchive) {
        val alertDialogBuilder = AlertDialog.Builder(requireContext())
        alertDialogBuilder.setPositiveButton("Yes") { _, _ ->
            dbViewModel.deleteSingleArchive(toDoArchive)
            Toast.makeText(context, "Successfully Deleted", Toast.LENGTH_SHORT).show()
        }
        alertDialogBuilder.setNegativeButton("No") {_,_ -> } // Nothing should happen
        alertDialogBuilder.setTitle("Delete this Todo?")
        alertDialogBuilder.setMessage("Caution : This is an irreversible action")
        alertDialogBuilder.create().show()
    }

    private fun unarchiveShortcut(view: View, item: ToDoData, itemToBeArchived: ToDoArchive) {
        dbViewModel.deleteSingleArchive(itemToBeArchived)
        dbViewModel.insertData(item)
        val snackBar = Snackbar.make(view, "Removed from Archive", Snackbar.LENGTH_LONG)
        snackBar.anchorView = binding.extendedFab
        snackBar.show()
    }

    private fun swipeToUnArchive(recyclerView: RecyclerView) {
        val swipeToUnarchiveCallback = object : SwipeToArchive() {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val item = mAdapter.dataSet[viewHolder.adapterPosition]
                val restoredItem = ToDoData(
                    item.oldId,
                    item.priority,
                    item.title,
                    item.description
                )
                unarchiveShortcut(viewHolder.itemView, restoredItem, item)
            }
        }
        val itemTouchHelper = ItemTouchHelper(swipeToUnarchiveCallback)
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    private fun changeSorting(){
        val options = arrayOf(
            ListFragment.LATEST_SORT,
            ListFragment.OLDEST_SORT,
            ListFragment.HIGH_SORT,
            ListFragment.LOW_SORT
        )
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Select a sorting option")
        builder.setSingleChoiceItems(options, selectedItem) { dialogInterface: DialogInterface, item: Int ->
            selectedItem = item
        }
        builder.setPositiveButton("Sort") { dialogInterface: DialogInterface, p1: Int ->
            when(selectedItem) {
                0 -> {
                dbViewModel.getAllArchiveNewFirst.observe(viewLifecycleOwner, { mAdapter.setData(it) })
                binding.sortStatus.text = getString(R.string.sort_template,
                    ListFragment.LATEST_SORT
                )
            }
                1 -> {
                    dbViewModel.getAllArchive.observe(viewLifecycleOwner, { mAdapter.setData(it) })
                    binding.sortStatus.text = getString(R.string.sort_template,
                        ListFragment.OLDEST_SORT
                    )
                }
                2 -> {
                    dbViewModel.getArchiveByHighPriority.observe(viewLifecycleOwner, { mAdapter.setData(it) })
                    binding.sortStatus.text = getString(R.string.sort_template,
                        ListFragment.HIGH_SORT
                    )
                }
                3 -> {
                    dbViewModel.getArchiveByLowPriority.observe(viewLifecycleOwner, { mAdapter.setData(it) })
                    binding.sortStatus.text = getString(R.string.sort_template,
                        ListFragment.LOW_SORT
                    )
                }
            }
            dialogInterface.dismiss()
        }
        builder.create()
        builder.show();
    }

    /*
    * MENU OPTIONS
    * */

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.list_fragment_menu, menu)

        menu.findItem(R.id.menu_delete_all).isVisible = false
        menu.findItem(R.id.menu_account).isVisible = false

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