package ru.rainman.ui

import android.text.TextUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import ru.rainman.domain.model.Post
import ru.rainman.ui.databinding.CardPostBinding

class PostsAdapter(
    private val currentItemIsPLaying: SharedFlow<CurrentPlayedItemState?>,
    private val onPostClickListener: OnPostClickListener
) : PagingDataAdapter<Post, PostsAdapter.ViewHolder>(Diff()) {

    inner class ViewHolder(private val binding: CardPostBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(post: Post) {

            binding.apply {

                post.author.avatar?.let {
                    Glide.with(binding.root.context)
                        .load(it)
                        .circleCrop()
                        .placeholder(R.drawable.avatar_stub_large)
                        .into(header.avatar)
                } ?: Glide.with(binding.root.context).clear(header.avatar)

                header.name.text = post.author.name
                header.job.text = post.author.currentJob?.name
                header.published.text = post.published.toString()
                header.more.isVisible = post.ownedByMe

                header.job.isVisible = post.author.currentJob != null

                content.text = post.content

                post.attachment?.let {
                    attachmentView.setData(it)
                    attachmentView.setOnPlayClickListener {
                        onPostClickListener.onPlayClicked(post.id, it)
                    }
                    CoroutineScope(Dispatchers.Default).launch {
                        currentItemIsPLaying.collectLatest {item ->
                            item?.let { item1 ->
                                attachmentView.setAudioPlayed(
                                    item1.id == post.id && item1.type == PubType.POST && item1.isPlaying
                                )
                            } ?: attachmentView.setAudioPlayed(false)
                        }
                    }
                } ?: attachmentView.recycle()

                like.isChecked = post.likedByMe

                header.more.setOnClickListener {
                    onPostClickListener.onMoreClicked(post.id)
                }

                like.setOnClickListener {
                    onPostClickListener.onLikeClicked(post.id)
                }

                share.setOnClickListener {
                    onPostClickListener.onShareClicked(post.id)
                }

                header.root.setOnClickListener {
                    onPostClickListener.onAuthorClicked(post.id)
                }

                root.setOnClickListener {
                    onPostClickListener.onPostClicked(post.id)
                }

                var contentExpanded = false

                expandContent.isVisible = content.lineCount >= 4

                expandContent.setOnClickListener {
                    contentExpanded = !contentExpanded
                    expandContent.isSelected = contentExpanded
                    content.ellipsize = if (contentExpanded) null else TextUtils.TruncateAt.END
                    content.maxLines = if (contentExpanded) Integer.MAX_VALUE else 4
                }
            }
        }
    }

    class Diff : DiffUtil.ItemCallback<Post>() {
        override fun areItemsTheSame(oldItem: Post, newItem: Post): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: Post, newItem: Post): Boolean {
            return oldItem.id == newItem.id
        }

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        getItem(position)?.let { holder.bind( it) }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            CardPostBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }
}