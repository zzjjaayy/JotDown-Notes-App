package com.jay.todoapp.fragments.update

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.jay.todoapp.R
import com.jay.todoapp.data.model.Priority
import com.jay.todoapp.data.model.ToDoData
import com.jay.todoapp.data.viewModel.ToDoDbViewModel
import com.jay.todoapp.ToDoSharedViewModel
import com.jay.todoapp.data.model.ToDoArchive
import com.jay.todoapp.databinding.FragmentUpdateBinding

class UpdateFragment : Fragment() {

    companion object {
        var CURRENT_TITLE = "currentTitle"
        var CURRENT_DESC = "currentDesc"
        var CURRENT_PRIORITY = "currentPriority"
        var CURRENT_ID = "currentId"
    }

    private val sharedViewModel : ToDoSharedViewModel by activityViewModels()
    private val dbViewModel : ToDoDbViewModel by activityViewModels()

    private var _binding: FragmentUpdateBinding? = null
    private val binding get() = _binding!!

    private lateinit var currentTitle: String
    private lateinit var currentDesc: String
    private lateinit var currentPriority: Priority
    private var currentId: Int? = null
    private var returnDestination : String? = null
    private var currentOldId : Int? = null

    /*
    * LIFECYCLE FUNCTIONS
    * */

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentUpdateBinding.inflate(inflater, container, false)

        arguments?.let {
            currentTitle = it.getString(CURRENT_TITLE).toString()
            currentDesc = it.getString(CURRENT_DESC).toString()
            currentPriority = Priority.valueOf(it.getString(CURRENT_PRIORITY).toString())
            currentId = it.getInt(CURRENT_ID)
            returnDestination = it.getString("returnDestination")
            currentOldId = it.getInt("currentOldId")
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Options Menu
        setHasOptionsMenu(true)

        // This is to set up the dropdown menu
        val items = listOf("High", "Medium", "Low")
        val adapter = ArrayAdapter(requireContext(), R.layout.dropdown_list_item, items)
        binding.autocompleteTextView.setAdapter(adapter)

        // item click Listener for the DropDown
        binding.autocompleteTextView.onItemClickListener =
            AdapterView.OnItemClickListener { parent, view, position, id ->
                when (position) {
                    0 -> {binding.autocompleteTextView.setTextColor(ContextCompat.getColor(requireContext(),R.color.red))}
                    1 -> {binding.autocompleteTextView.setTextColor(ContextCompat.getColor(requireContext(),R.color.yellow))}
                    2 -> {binding.autocompleteTextView.setTextColor(ContextCompat.getColor(requireContext(),R.color.green))}
                }
            }

        // Settings previous value
        binding.currentEditTitleEditable.setText(currentTitle)
        binding.currentEditDescEditable.setText(currentDesc)
        when(currentPriority) {
            Priority.HIGH -> {
                binding.autocompleteTextView.setText(items[0], false)
                binding.autocompleteTextView.setTextColor(ContextCompat.getColor(requireContext(),R.color.red))
            }
            Priority.MEDIUM -> {
                binding.autocompleteTextView.setText(items[1], false)
                binding.autocompleteTextView.setTextColor(ContextCompat.getColor(requireContext(),R.color.yellow))
            }
            Priority.LOW -> {
                binding.autocompleteTextView.setText(items[2], false)
                binding.autocompleteTextView.setTextColor(ContextCompat.getColor(requireContext(),R.color.green))
            }
        }
    }

    /*
    * MENU OPTION FUNCTIONS
    * */

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.update_fragment_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_save -> updatingItem()
            R.id.menu_delete -> confirmItemRemoval()
        }
        return super.onOptionsItemSelected(item)
    }

    /*
    * DELETION OF ITEM
    * */

    private fun confirmItemRemoval() {
        val alertDialogBuilder = AlertDialog.Builder(requireContext())
        alertDialogBuilder.setPositiveButton("Yes") { _, _ ->
            deleteItem()
            Toast.makeText(context, "Successfully Deleted", Toast.LENGTH_SHORT).show()
            findNavController().navigate(R.id.action_updateFragment_to_listFragment)
        }
        alertDialogBuilder.setNegativeButton("No") {_,_ -> } // Nothing should happen
        alertDialogBuilder.setTitle("Delete this Todo?")
        alertDialogBuilder.setMessage("Caution : This is an irreversible action")
        alertDialogBuilder.create().show()
    }

    private fun deleteItem() {
        val itemToBeDeleted = ToDoData(
            currentId!!.toInt(),
            currentPriority,
            currentTitle,
            currentDesc
        )
        dbViewModel.deleteSingleDataItem(itemToBeDeleted)
    }

    /*
    * UPDATING ITEM
    * */
    private fun updatingItem() {
        val toDoTitle : String = binding.currentEditTitleEditable.text.toString()
        val toDoDesc : String = binding.currentEditDescEditable.text.toString()
        val priorityLevel : String = binding.autocompleteTextView.text.toString()

        if(sharedViewModel.verifyUserData(toDoTitle, priorityLevel)) {
            when (returnDestination) {
                "Archive" -> {
                    Log.d("jayischecking", "Update To archive")
                    val itemToBeUpdated = ToDoArchive(
                        currentId!!.toInt(), currentOldId!!.toInt(), sharedViewModel.parseStringToPriority(priorityLevel), toDoTitle, toDoDesc
                    )
                    dbViewModel.updateArchive(itemToBeUpdated)
                    findNavController().navigate(R.id.action_updateFragment_to_archiveFragment)
                }
                "List" -> {
                    Log.d("jayischecking", "Update To List")
                    val itemToBeUpdated = ToDoData(
                        currentId!!.toInt(), sharedViewModel.parseStringToPriority(priorityLevel), toDoTitle, toDoDesc
                    )
                    dbViewModel.updateData(itemToBeUpdated)
                    findNavController().navigate(R.id.action_updateFragment_to_listFragment)
                }
            }
            Toast.makeText(context, "Successfully Updated", Toast.LENGTH_SHORT).show()
        }
    }
}