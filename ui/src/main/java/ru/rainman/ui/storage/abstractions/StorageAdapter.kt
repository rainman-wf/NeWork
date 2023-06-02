package ru.rainman.ui.storage.abstractions

import androidx.recyclerview.widget.ListAdapter
import androidx.viewbinding.ViewBinding
import ru.rainman.domain.model.Attachment

abstract class StorageAdapter<T : Attachment, VB : ViewBinding>(
    val onItemClicked: ((Attachment) -> Unit),
    val bind: VB.(T) -> Unit
) : ListAdapter<T, StorageAdapter<T, VB>.StorageViewHolder>(Diff<T>()) {

    inner class StorageViewHolder(val binding: VB) : ViewHolder<T, VB>(binding) {

        override fun bind(model: T) {
            bind(binding, model)
        }
    }

    override fun onBindViewHolder(holder: StorageViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

}