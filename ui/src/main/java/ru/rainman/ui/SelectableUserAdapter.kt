package ru.rainman.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.rainman.ui.databinding.CardSelectableUserBinding
import ru.rainman.ui.helperutils.SelectableUser

class SelectableUserAdapter (private val onItemClickListener: (SelectableUser) -> Unit) :
    ListAdapter<SelectableUser, SelectableUserAdapter.ViewHolder>(Diff()) {

    inner class ViewHolder(private val binding: CardSelectableUserBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(user: SelectableUser) {
            binding.selectableFullName.text = user.user.name
            binding.selectableLogin.text = user.user.login
            binding.selectedUser.isChecked = user.selected
            binding.root.setOnClickListener {
                onItemClickListener(user)
            }
        }
    }

    class Diff : DiffUtil.ItemCallback<SelectableUser>() {
        override fun areItemsTheSame(oldItem: SelectableUser, newItem: SelectableUser): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: SelectableUser, newItem: SelectableUser): Boolean {
            return oldItem.user.id == newItem.user.id
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            CardSelectableUserBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}