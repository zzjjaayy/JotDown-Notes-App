package com.jay.todoapp.fragments.update

import android.os.Bundle
import android.renderscript.RenderScript
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
import com.jay.todoapp.data.viewModel.ToDoViewModel
import com.jay.todoapp.databinding.FragmentUpdateBinding

class UpdateFragment : Fragment() {

    companion object {
        var CURRENT_TITLE = "currentTitle"
        var CURRENT_DESC = "currentDesc"
        var CURRENT_PRIORITY = "currentPriority"
        var CURRENT_ID = "currentId"
    }

    private val sharedViewModel : ToDoViewModel by activityViewModels()

    private var _binding: FragmentUpdateBinding? = null
    private val binding get() = _binding!!

    private lateinit var currentTitle: String
    private lateinit var currentDesc: String
    private lateinit var currentPriority: Priority
    private var currentId: Int? = null

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

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.update_fragment_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == R.id.menu_save) {
            updatingItem()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun updatingItem() {
        val toDoTitle : String = binding.currentEditTitleEditable.text.toString()
        val toDoDesc : String = binding.currentEditDescEditable.text.toString()
        val priorityLevel : String = binding.autocompleteTextView.text.toString()

        if(sharedViewModel.updateDataToDb(currentId!!.toInt(), toDoTitle, toDoDesc, priorityLevel)) {
            Toast.makeText(context, "Successfully Updated", Toast.LENGTH_SHORT).show()
            findNavController().navigate(R.id.action_updateFragment_to_listFragment)
        }
    }
}