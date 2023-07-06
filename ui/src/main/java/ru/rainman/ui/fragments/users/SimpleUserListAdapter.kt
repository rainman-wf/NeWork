package ru.rainman.ui.fragments.users

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import ru.rainman.domain.model.User
import ru.rainman.ui.R
import ru.rainman.ui.databinding.CardUserPreviewSmallBinding

class SimpleUserListAdapter : ListAdapter<User, SimpleUserListAdapter.ViewHolder>(Diff()) {

    inner class ViewHolder(private val binding: CardUserPreviewSmallBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(user: User) {

            binding.name.text = user.name
            user.avatar?.let {
                Glide.with(binding.avatar)
                    .load(it)
                    .circleCrop()
                    .error(R.drawable.avatar_error)
                    .placeholder(R.drawable.avatar_empty)
                    .into(binding.avatar)
            } ?: Glide.with(binding.avatar).clear(binding.avatar)
        }
    }

    class Diff : DiffUtil.ItemCallback<User>() {
        override fun areItemsTheSame(oldItem: User, newItem: User): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: User, newItem: User): Boolean {
            return oldItem.id == newItem.id
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            CardUserPreviewSmallBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

}