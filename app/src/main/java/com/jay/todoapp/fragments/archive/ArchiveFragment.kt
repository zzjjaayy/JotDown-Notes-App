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
import androidx.core.view.isInvisible
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.jay.todoapp.R
import com.jay.todoapp.data.model.ListSource
import com.jay.todoapp.data.model.SortOrder
import com.jay.todoapp.data.model.ToDo
import com.jay.todoapp.data.viewmodel.ToDoSharedViewModel
import com.jay.todoapp.databinding.FragmentListBinding
import com.jay.todoapp.utils.LOG_TAG
import com.jay.todoapp.utils.SwipeToArchive
import com.jay.todoapp.utils.hideKeyboard
import jp.wasabeef.recyclerview.animators.SlideInUpAnimator

class ArchiveFragment : Fragment(), SearchView.OnQueryTextListener {

    private var _binding : FragmentListBinding? = null
    private val binding get() = _binding!!

    private val sharedViewModel : ToDoSharedViewModel by activityViewModels()
    private lateinit var mAdapter: ArchiveAdapter
    private lateinit var searchView : SearchView

    private var selectedItem : Int = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        sharedViewModel.setSource(ListSource.ARCHIVE)
        requireActivity().onBackPressedDispatcher.addCallback(this){
            if(!searchView.isIconified) {
                searchView.setQuery("", true)
                searchView.isIconified = true
                binding.sortStatus.isInvisible = sharedViewModel.isArchivedListEmpty
            } else findNavController().navigate(R.id.action_archiveFragment_to_listFragment)
        }
        // Inflate the layout for this fragment
        _binding = FragmentListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            addButton.hide()

            sortStatus.text = getString(R.string.sort_template, sharedViewModel.getStatusText(ListSource.ARCHIVE))
            sortStatus.setOnClickListener{
                changeSorting()
            }
            noDataText.text = "No Archives Found"
            noDataTip.text = "Swipe left on any note to archive it!"
            archiveBtn.apply {
                setIconResource(R.drawable.ic_arrow_upward_24)
                text = getString(R.string.all_notes)
                updateLayoutParams<ConstraintLayout.LayoutParams> { endToEnd = view.id }
                setOnClickListener {
                    if(!searchView.isIconified) {
                        searchView.setQuery("", true)
                        searchView.isIconified = true
                        binding.sortStatus.visibility = View.VISIBLE
                    }
                    findNavController().navigate(R.id.action_archiveFragment_to_listFragment)
                }
            }
        }
        sharedViewModel.toDoList.observe(viewLifecycleOwner) {
            changeVisibilityOfEmptyIndicators(sharedViewModel.isArchivedListEmpty)
            mAdapter.setData(it)
        }
        setUpRecyclerView()
        setHasOptionsMenu(true)
        hideKeyboard(requireActivity())
    }

    private fun changeVisibilityOfEmptyIndicators(isEmpty : Boolean) {
        binding.apply {
            sortStatus.isInvisible = isEmpty
            noDataImage.isInvisible = !isEmpty
            noDataText.isInvisible = !isEmpty
            noDataTip.isInvisible = !isEmpty
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    private fun setUpRecyclerView() {
        mAdapter = ArchiveAdapter ({
            updateShortcut(it)
        }, {note, view ->
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
                Log.e(LOG_TAG, "Error showing menu icons.", e)
            } finally {
                popup.show()
            }

            popup.setOnMenuItemClickListener { menuItem: MenuItem ->
                when(menuItem.itemId){
                    R.id.menu_press_update -> updateShortcut(note)
                    R.id.menu_press_delete -> deleteShortcut(note)
                    R.id.menu_press_unarchive -> unarchiveShortcut(view, note)
                }
                true
            }
        })
        binding.notesListRecyclerView.adapter = mAdapter

        binding.notesListRecyclerView.itemAnimator = SlideInUpAnimator().apply{
            addDuration = 300
        }
        swipeToUnArchive(binding.notesListRecyclerView)
    }

    private fun updateShortcut(toDo: ToDo) {
        val action = ArchiveFragmentDirections.actionArchiveFragmentToUpdateFragment(toDo.id)
        findNavController().navigate(action)
    }

    private fun deleteShortcut(toDo: ToDo) {
        val alertDialogBuilder = AlertDialog.Builder(requireContext())
        alertDialogBuilder.setPositiveButton("Yes") { _, _ ->
            sharedViewModel.deleteNote(toDo.id)
            Toast.makeText(context, "Successfully Deleted", Toast.LENGTH_SHORT).show()
        }
        alertDialogBuilder.setNegativeButton("No") {_,_ -> } // Nothing should happen
        alertDialogBuilder.setTitle("Delete this Todo?")
        alertDialogBuilder.setMessage("Caution : This is an irreversible action")
        alertDialogBuilder.create().show()
    }

    private fun unarchiveShortcut(view: View, item: ToDo) {
        item.archivedTS = -1L
        item.isArchived = false
        sharedViewModel.updateNote(item)
        Snackbar.make(view, "Removed from Archive", Snackbar.LENGTH_LONG).apply {
            anchorView = binding.archiveBtn
            show()
        }
    }

    private fun swipeToUnArchive(recyclerView: RecyclerView) {
        val swipeToUnarchiveCallback = object : SwipeToArchive() {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val item = mAdapter.dataSet[viewHolder.adapterPosition]
                unarchiveShortcut(viewHolder.itemView, item)
            }
        }
        val itemTouchHelper = ItemTouchHelper(swipeToUnarchiveCallback)
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    private fun changeSorting(){
        val options = arrayOf("Latest First", "Oldest First", "High to Low Priority", "Low to High Priority")
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Select a sorting option")
        builder.setSingleChoiceItems(options, selectedItem) { dialogInterface: DialogInterface, item: Int ->
            selectedItem = item
        }
        builder.setPositiveButton("Sort") { dialogInterface: DialogInterface, p1: Int ->
            sharedViewModel.archivedSortOrder =  when(selectedItem) {
                1 -> SortOrder.OLDEST_FIRST
                2 -> SortOrder.HIGH_PRIORITY
                3 -> SortOrder.LOW_PRIORITY
                else -> SortOrder.LATEST_FIRST
            }
            sharedViewModel.setSortedListToLiveData(ListSource.ARCHIVE)
            binding.sortStatus.text = getString(R.string.sort_template, sharedViewModel.getStatusText(ListSource.ARCHIVE))
            dialogInterface.dismiss()
        }
        builder.create()
        builder.show();
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.list_fragment_menu, menu)

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

    override fun onQueryTextSubmit(query: String?): Boolean = true

    override fun onQueryTextChange(query: String?): Boolean {
        if(query != null) {
            searchQueryInDb(query)
        }
        return true
    }

    private fun searchQueryInDb(query: String) {
        mAdapter.setData(sharedViewModel.searchNotes(query))
    }

    private fun setItemsVisibility(menu: Menu, exception: MenuItem, visible: Boolean) {
        for (i in 0 until menu.size()) {
            val item = menu.getItem(i)
            if (item !== exception) item.isVisible = visible
        }
    }
}