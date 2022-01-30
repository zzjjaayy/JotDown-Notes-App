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
import androidx.navigation.fragment.navArgs
import com.jay.todoapp.R
import com.jay.todoapp.data.model.Priority
import com.jay.todoapp.data.model.ToDo
import com.jay.todoapp.data.viewmodel.ToDoSharedViewModel
import com.jay.todoapp.databinding.FragmentUpdateBinding
import com.jay.todoapp.utils.LOG_TAG

class UpdateFragment : Fragment() {

    private val sharedViewModel : ToDoSharedViewModel by activityViewModels()

    private var _binding: FragmentUpdateBinding? = null
    private val binding get() = _binding!!

    private val args : UpdateFragmentArgs by navArgs()
    private var toDo: ToDo? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentUpdateBinding.inflate(inflater, container, false)
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

        toDo = sharedViewModel.mapOfDocIdWithAllToDo[args.currentId] ?: run {
            findNavController().popBackStack()
            Toast.makeText(requireActivity(), "Could not find todo to edit!", Toast.LENGTH_SHORT).show()
            return
        }

        // Settings previous value
        binding.currentEditTitleEditable.setText(toDo?.title)
        binding.currentEditDescEditable.setText(toDo?.description)
        when(toDo!!.priority) {
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
            sharedViewModel.deleteNote(toDo!!.id)
            findNavController().popBackStack()
            Toast.makeText(context, "Successfully Deleted", Toast.LENGTH_SHORT).show()
        }
        alertDialogBuilder.setNegativeButton("No") {_,_ -> } // Nothing should happen
        alertDialogBuilder.setTitle("Delete this Todo?")
        alertDialogBuilder.setMessage("Caution : This is an irreversible action")
        alertDialogBuilder.create().show()
    }

    /*
    * UPDATING ITEM
    * */
    private fun updatingItem() {
        val toDoTitle : String = binding.currentEditTitleEditable.text.toString()
        val toDoDesc : String = binding.currentEditDescEditable.text.toString()
        val priorityLevel : String = binding.autocompleteTextView.text.toString()

        if(sharedViewModel.verifyUserData(toDoTitle, priorityLevel)) {
            Log.d(LOG_TAG, "Update To List")
            val itemToBeUpdated = ToDo(
                args.currentId,
                toDo!!.createdTS,
                toDo!!.archivedTS,
                toDo!!.isArchived,
                sharedViewModel.parseStringToPriority(priorityLevel),
                toDoTitle,
                toDoDesc
            )
            sharedViewModel.updateNote(itemToBeUpdated)
            findNavController().popBackStack()
            Toast.makeText(context, "Successfully Updated", Toast.LENGTH_SHORT).show()
        }
    }
}