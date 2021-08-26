package com.jay.todoapp.fragments.add

import android.os.Bundle
import android.view.*
import android.widget.*
import android.widget.AdapterView.OnItemClickListener
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.jay.todoapp.R
import com.jay.todoapp.data.model.ToDoData
import com.jay.todoapp.data.viewModel.ToDoDbViewModel
import com.jay.todoapp.ToDoSharedViewModel
import com.jay.todoapp.databinding.FragmentAddBinding


class AddFragment : Fragment() {

    private val sharedViewModel : ToDoSharedViewModel by activityViewModels()
    private val dbViewModel : ToDoDbViewModel by activityViewModels()

    private var _binding: FragmentAddBinding? = null
    private val binding get() = _binding!!

    /*
    * LIFECYCLE FUNCTIONS
    * */

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentAddBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onResume() {
        super.onResume()

        // This is to set up the dropdown menu
        val items = listOf("High", "Medium", "Low")
        val adapter = ArrayAdapter(requireContext(), R.layout.dropdown_list_item, items)
        binding.autocompleteTextView.setAdapter(adapter)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Options Menu
        setHasOptionsMenu(true)

        // item click Listener for the DropDown
        binding.autocompleteTextView.onItemClickListener =
            OnItemClickListener { parent, view, position, id ->
                when(position) {
                    0 -> {binding.autocompleteTextView.setTextColor(ContextCompat.getColor(requireContext(), R.color.red))}
                    1 -> {binding.autocompleteTextView.setTextColor(ContextCompat.getColor(requireContext(), R.color.yellow))}
                    2 -> {binding.autocompleteTextView.setTextColor(ContextCompat.getColor(requireContext(), R.color.green))}
                }
            }
    }

    /*
    * MENU OPTION FUNCTIONS
    * */

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.add_fragment_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == R.id.menu_add) {
            insertingNewData()
        }
        return super.onOptionsItemSelected(item)
    }

    /*
    * INSERTING DATA
    * */

    private fun insertingNewData() {
        val toDoTitle : String = binding.editTitle.text.toString()
        val toDoDesc : String = binding.editDesc.text.toString()
        val priorityLevel : String = binding.autocompleteTextView.text.toString()

        if(sharedViewModel.verifyUserData(toDoTitle, priorityLevel)) {
            val newData = ToDoData(
                0, // This is set to auto increment so room will handle it
                sharedViewModel.parseStringToPriority(priorityLevel),
                toDoTitle,
                toDoDesc
            )
            dbViewModel.insertData(newData)
            Toast.makeText(context, "Successfully Added", Toast.LENGTH_SHORT).show()
            findNavController().navigate(R.id.action_addFragment_to_listFragment)
        }
    }

}