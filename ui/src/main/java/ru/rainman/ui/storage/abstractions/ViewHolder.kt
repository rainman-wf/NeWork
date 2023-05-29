package ru.rainman.ui.storage.abstractions

import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding

abstract class ViewHolder<T : Any, VB : ViewBinding>(private val binding: VB) :
    RecyclerView.ViewHolder(binding.root) {
    abstract fun bind(model: T)
}