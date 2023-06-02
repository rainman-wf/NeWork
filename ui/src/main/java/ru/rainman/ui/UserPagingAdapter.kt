package ru.rainman.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import ru.rainman.domain.model.User
import ru.rainman.ui.databinding.CardUserPreviewBaseBinding


class UserPagingAdapter : PagingDataAdapter<User, UserPagingAdapter.UserViewHolder>(UserDiff()) {

    inner class UserViewHolder(private val binding: CardUserPreviewBaseBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(user: User) {

            with(binding) {
                name.text = user.name
                user.currentJob?.let { job.text = it.name }
                user.avatar?.let {
                    Glide.with(binding.root)
                        .load(it)
                        .circleCrop()
                        .into(avatar)
                } ?: avatar.setImageDrawable(null)

                isFavorite.isVisible = user.favorite
            }
        }
    }

    class UserDiff : DiffUtil.ItemCallback<User>() {
        override fun areItemsTheSame(oldItem: User, newItem: User): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: User, newItem: User): Boolean {
            return oldItem == newItem
        }
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        getItem(position)?.let { holder.bind(it) }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        return UserViewHolder(
            CardUserPreviewBaseBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }
}