package com.jay.todoapp.fragments.list

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.isInvisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.jay.todoapp.R
import com.jay.todoapp.data.model.ListSource
import com.jay.todoapp.data.model.SortOrder
import com.jay.todoapp.data.model.ToDo
import com.jay.todoapp.data.viewmodel.ToDoSharedViewModel
import com.jay.todoapp.databinding.FragmentListBinding
import com.jay.todoapp.utils.*
import jp.wasabeef.recyclerview.animators.SlideInUpAnimator


class ListFragment : Fragment(), SearchView.OnQueryTextListener {

    private val sharedViewModel : ToDoSharedViewModel by activityViewModels()

    private var _binding : FragmentListBinding? = null
    private val binding get() = _binding!!

    private lateinit var mAdapter: ToDoAdapter
    private lateinit var searchView : SearchView
    private lateinit var mAuth: FirebaseAuth

    private var selectedItem : Int = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        (activity as AppCompatActivity?)?.supportActionBar?.show()
        sharedViewModel.setSource(ListSource.MAIN)
        requireActivity().onBackPressedDispatcher.addCallback(this){
            if(!searchView.isIconified) {
                searchView.setQuery("", true)
                searchView.isIconified = true
                binding.sortStatus.isInvisible = sharedViewModel.isMainListEmpty
            } else activity?.finish()
        }
        mAuth = FirebaseAuth.getInstance()
        _binding = FragmentListBinding.inflate(inflater, container, false)
        return binding.root
   }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            sortStatus.text = getString(R.string.sort_template, sharedViewModel.getStatusText(ListSource.MAIN))
            sortStatus.setOnClickListener { changeSorting() }
            noDataText.text = "No Notes Found"
            noDataTip.text = "Click the + icon to add one!"
            addButton.setOnClickListener {
                findNavController().navigate(R.id.action_listFragment_to_addFragment)
            }
            archiveBtn.setOnClickListener {
                if(!searchView.isIconified) {
                    searchView.setQuery("", true)
                    searchView.isIconified = true
                    binding.sortStatus.visibility = View.VISIBLE
                }
                val shouldLockArchive = requireActivity().getSharedPreferences(SECURE_ARCHIVE, Context.MODE_PRIVATE)
                    .getBoolean(LOCK_ARCHIVE, false)
                if(shouldLockArchive) {
                    BiometricHelper.getInstance(requireActivity()).bio { isAuthSuccessful ->
                        if(isAuthSuccessful) {
                            findNavController().navigate(R.id.action_listFragment_to_archiveFragment)
                        } else Toast.makeText(context, "Authentication Failed", Toast.LENGTH_SHORT).show()
                    }
                } else findNavController().navigate(R.id.action_listFragment_to_archiveFragment)
            }
        }

        sharedViewModel.toDoList.observe(viewLifecycleOwner) {
            changeVisibilityOfEmptyIndicators(sharedViewModel.isMainListEmpty)
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setUpRecyclerView() {
        mAdapter = ToDoAdapter ({
            updateShortcut(it)
        }, { note, view -> // when item long pressed
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
                    R.id.menu_press_update -> updateShortcut(note)
                    R.id.menu_press_delete -> deleteShortcut(note)
                    R.id.menu_press_archive -> archiveShortcut(view, note)
                }
                true
            }
        })
        binding.notesListRecyclerView.adapter = mAdapter
        binding.notesListRecyclerView.itemAnimator = SlideInUpAnimator().apply{
            addDuration = 300
        }
        swipeToArchive(binding.notesListRecyclerView)
    }

    private fun updateShortcut(it : ToDo) =
        findNavController().navigate(ListFragmentDirections.actionListFragmentToUpdateFragment(it.id))

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

    private fun archiveShortcut(view: View, item: ToDo) {
        item.archivedTS = System.currentTimeMillis()
        item.isArchived = true
        sharedViewModel.updateNote(item)
        restoreDeletedItem(view, item)
    }

    private fun swipeToArchive(recyclerView: RecyclerView) {
        val swipeToArchiveCallback = object : SwipeToArchive() {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val item = mAdapter.dataSet[viewHolder.adapterPosition]
                archiveShortcut(viewHolder.itemView, item)
            }
        }
        val itemTouchHelper = ItemTouchHelper(swipeToArchiveCallback)
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    private fun restoreDeletedItem(view: View, item: ToDo) {
        Snackbar.make(view, "Archived '${item.title}'", Snackbar.LENGTH_LONG).apply {
            anchorView = binding.addButton
            show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.list_fragment_menu, menu)

        val userIcon = menu.findItem(R.id.menu_account)
        if(mAuth.currentUser != null) {
            Glide.with(this)
                .asBitmap()
                .load(mAuth.currentUser?.photoUrl)
                .circleCrop()
                .into(object : CustomTarget<Bitmap>(){
                    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                        userIcon.icon = BitmapDrawable(resources, resource)
                    }
                    override fun onLoadCleared(placeholder: Drawable?) {}
                })
        } else {
            userIcon.setIcon(R.drawable.ic_default_account)
        }

        val search = menu.findItem(R.id.menu_search)
        searchView = (search.actionView as? SearchView)!!
        searchView.setOnQueryTextListener(this)

        searchView.setOnSearchClickListener {
            binding.sortStatus.visibility = View.GONE
            setMenuItemsVisibility(menu, search, false)
        }

        searchView.setOnCloseListener {
            binding.sortStatus.visibility = View.VISIBLE
            setMenuItemsVisibility(menu, search, true)
            false
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.menu_account) {
            findNavController().navigate(R.id.action_listFragment_to_userInfoFragment)
        }
        return super.onOptionsItemSelected(item)
    }

    private fun changeSorting(){
        val options = arrayOf("Latest First", "Oldest First", "High to Low Priority", "Low to High Priority")
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Select a sorting option")
        builder.setSingleChoiceItems(options, selectedItem) { dialogInterface: DialogInterface, item: Int ->
            selectedItem = item
        }
        builder.setPositiveButton("Sort") { dialogInterface: DialogInterface, p1: Int ->
            sharedViewModel.mainSortOrder =  when(selectedItem) {
                1 -> SortOrder.OLDEST_FIRST
                2 -> SortOrder.HIGH_PRIORITY
                3 -> SortOrder.LOW_PRIORITY
                else -> SortOrder.LATEST_FIRST
            }
            sharedViewModel.setSortedListToLiveData(ListSource.MAIN)
            binding.sortStatus.text = getString(R.string.sort_template, sharedViewModel.getStatusText(ListSource.MAIN))
            dialogInterface.dismiss()
        }
        builder.create()
        builder.show();
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

    private fun setMenuItemsVisibility(menu: Menu, exception: MenuItem, visible: Boolean) {
        for (i in 0 until menu.size()) {
            val item = menu.getItem(i)
            if (item !== exception) item.isVisible = visible
        }
    }
}