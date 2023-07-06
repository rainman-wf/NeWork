package ru.rainman.ui.fragments.users

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import ru.rainman.common.log
import ru.rainman.domain.model.User
import ru.rainman.ui.R
import ru.rainman.ui.databinding.CardUserPreviewBaseBinding


class UserPagingAdapter : PagingDataAdapter<User, UserPagingAdapter.UserViewHolder>(UserDiff()) {

    inner class UserViewHolder(private val binding: CardUserPreviewBaseBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(user: User) {

            with(binding) {
                name.text = user.name
                log(user.jobs)
                user.currentJob?.let { job.text = it.name }
                user.avatar?.let {
                    Glide.with(binding.root)
                        .load(it)
                        .placeholder(R.drawable.avatar_empty)
                        .error(R.drawable.avatar_error)
                        .circleCrop()
                        .into(avatar)
                } ?: Glide.with(binding.root.context).load(R.drawable.avatar_empty).into(avatar)

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