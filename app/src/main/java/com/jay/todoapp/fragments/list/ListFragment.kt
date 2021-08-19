package com.jay.todoapp.fragments.list

import android.app.AlertDialog
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.jay.todoapp.R
import com.jay.todoapp.data.viewModel.ToDoViewModel
import com.jay.todoapp.databinding.FragmentListBinding

class ListFragment : Fragment() {

    private val sharedViewModel : ToDoViewModel by activityViewModels()

    private var _binding : FragmentListBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
           inflater: LayoutInflater, container: ViewGroup?,
           savedInstanceState: Bundle?
       ): View? {
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
            })

            // setting an adapter to the recycler view
            notesListRecyclerView.adapter = ToDoListAdapter {
                val action = ListFragmentDirections.actionListFragmentToUpdateFragment(
                    currentTitle = it.title,
                    currentDesc = it.description,
                    currentPriority = it.priority.name,
                    currentId = it.id
                )
                view.findNavController().navigate(action)
            }
        }
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.list_fragment_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == R.id.menu_delete_all) {
            confirmRemoval()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
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
}