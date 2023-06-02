package ru.rainman.ui.storage.abstractions

import androidx.recyclerview.widget.DiffUtil
import ru.rainman.domain.model.Attachment

open class Diff<T : Attachment> : DiffUtil.ItemCallback<T>() {
    override fun areItemsTheSame(oldItem: T, newItem: T): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: T, newItem: T): Boolean {
        return oldItem.uri == newItem.uri
    }
}