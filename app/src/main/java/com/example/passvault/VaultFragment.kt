package com.example.passvault

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.passvault.databinding.FragmentVaultBinding
import kotlinx.coroutines.launch
import java.util.UUID

class VaultFragment : Fragment() {
    private var _binding: FragmentVaultBinding? = null
    private val binding
        get() = checkNotNull(_binding) {
            "Null binding!"
        }

    private val vaultViewModel: VaultViewModel by viewModels()

    private val goToItem = { itemId: UUID ->
        findNavController().navigate(
            VaultFragmentDirections.showItem(itemId)
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentVaultBinding.inflate(inflater, container, false)
        binding.vaultRecyclerView.layoutManager = LinearLayoutManager(context)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                vaultViewModel.uiState.collect { uiState ->
                    binding.vaultRecyclerView.adapter = VaultAdapter(
                        uiState.items,
                        goToItem)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.fragment_vault, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.new_item -> {
                showNewItem()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showNewItem() {
        viewLifecycleOwner.lifecycleScope.launch {
            val newItem = Item(
                id = UUID.randomUUID(),
                title = "",
                userName = "",
                passWord = "",
                note = ""
            )
            vaultViewModel.addItem(newItem)
            findNavController().navigate(
                VaultFragmentDirections.showItem(newItem.id)
            )
        }
    }
}