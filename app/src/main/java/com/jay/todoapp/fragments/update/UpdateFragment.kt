package com.jay.todoapp.fragments.update

import android.os.Bundle
import android.renderscript.RenderScript
import android.view.*
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.jay.todoapp.R
import com.jay.todoapp.data.model.Priority
import com.jay.todoapp.databinding.FragmentUpdateBinding

class UpdateFragment : Fragment() {

    companion object {
        var CURRENT_TITLE = "currentTitle"
        var CURRENT_DESC = "currentDesc"
        var CURRENT_PRIORITY = "currentPriority"
    }

    private var _binding: FragmentUpdateBinding? = null
    private val binding get() = _binding!!

    private lateinit var currentTitle: String
    private lateinit var currentDesc: String
    private lateinit var currentPriority: Priority

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
}