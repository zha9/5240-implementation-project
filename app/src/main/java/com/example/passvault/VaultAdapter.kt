package com.example.passvault

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.passvault.databinding.ListItemBinding
import java.util.UUID

class ItemHolder(
    private val binding: ListItemBinding
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(item: Item, onItemClicked: (itemId: UUID) -> Unit) {
        binding.itemTitle.text = item.title
        binding.itemUsername.text = item.userName
        binding.root.setOnClickListener {
            onItemClicked(item.id)
        }
    }
}

class VaultAdapter(
    private val items: List<Item>,
    private val onItemClicked: (itemId: UUID) -> Unit
) : RecyclerView.Adapter<ItemHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ListItemBinding.inflate(inflater, parent, false)
        return ItemHolder(binding)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: ItemHolder, position: Int) {
        val item = items[position]
        holder.bind(item, onItemClicked)
    }
}