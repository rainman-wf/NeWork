package ru.rainman.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collectLatest
import ru.rainman.domain.model.geo.SearchResult
import ru.rainman.domain.model.geo.Point
import ru.rainman.ui.databinding.ItemMapSearchResultsBinding

class AutocompleteSearchAdapter(
    private val onItemClickListener: (point: Point) -> Unit
) : ListAdapter<SearchResult, AutocompleteSearchAdapter.ViewHolder>(Diff()) {

    inner class ViewHolder(
        private val binding: ItemMapSearchResultsBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(searchResult: SearchResult) {

            binding.apply {
                geoObjectAddress.text = searchResult.shorAddress
                geoObjectName.text = searchResult.name
            }

            binding.root.setOnClickListener { onItemClickListener(searchResult.point) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemMapSearchResultsBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class Diff : DiffUtil.ItemCallback<SearchResult>() {
        override fun areItemsTheSame(oldItem: SearchResult, newItem: SearchResult): Boolean {
            return oldItem.point == newItem.point
        }

        override fun areContentsTheSame(oldItem: SearchResult, newItem: SearchResult): Boolean {
            return oldItem == newItem
        }
    }
}