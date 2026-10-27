package com.example.pulse.ui.common

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.pulse.databinding.ItemChoiceBinding

data class ChoiceItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val alreadyAdded: Boolean
)

class ChoiceAdapter(
    private val onSelectionChanged: (Int) -> Unit
) : ListAdapter<ChoiceItem, ChoiceAdapter.ChoiceViewHolder>(DiffCallback) {

    private val selectedIds = mutableSetOf<String>()

    fun getSelectedIds(): Set<String> = selectedIds.toSet()

    class ChoiceViewHolder(
        val binding: ItemChoiceBinding
    ) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChoiceViewHolder {
        val binding = ItemChoiceBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ChoiceViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChoiceViewHolder, position: Int) {
        val item = getItem(position)

        holder.binding.tvChoiceTitle.text = item.title
        holder.binding.tvChoiceSubtitle.text = item.subtitle
        holder.binding.checkChoice.isChecked = item.alreadyAdded || item.id in selectedIds
        holder.itemView.alpha = if (item.alreadyAdded) 0.5f else 1f

        holder.itemView.setOnClickListener {
            if (item.alreadyAdded) return@setOnClickListener
            if (!selectedIds.remove(item.id)) selectedIds.add(item.id)

            val pos = holder.bindingAdapterPosition
            if (pos != RecyclerView.NO_POSITION) notifyItemChanged(pos)
            onSelectionChanged(selectedIds.size)
        }
    }

    private object DiffCallback : DiffUtil.ItemCallback<ChoiceItem>() {
        override fun areItemsTheSame(old: ChoiceItem, new: ChoiceItem) = old.id == new.id
        override fun areContentsTheSame(old: ChoiceItem, new: ChoiceItem) = old == new
    }
}