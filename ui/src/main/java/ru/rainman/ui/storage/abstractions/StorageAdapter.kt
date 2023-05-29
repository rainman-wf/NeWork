package ru.rainman.ui.storage.abstractions

import android.net.Uri
import androidx.recyclerview.widget.ListAdapter
import androidx.viewbinding.ViewBinding

abstract class StorageAdapter<T : StorageItem, VB : ViewBinding>(
    private val onItemClicked: (Uri) -> Unit,
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