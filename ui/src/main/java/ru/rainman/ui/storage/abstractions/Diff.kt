package ru.rainman.ui.storage.abstractions

import androidx.recyclerview.widget.DiffUtil

open class Diff<T : StorageItem> : DiffUtil.ItemCallback<T>() {
    override fun areItemsTheSame(oldItem: T, newItem: T): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: T, newItem: T): Boolean {
        return oldItem.uri == newItem.uri
    }
}