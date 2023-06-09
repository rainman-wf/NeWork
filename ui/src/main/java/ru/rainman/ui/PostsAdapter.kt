package ru.rainman.ui

import android.content.Context
import android.text.TextUtils
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.core.view.isVisible
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.badge.BadgeDrawable
import com.google.android.material.badge.BadgeUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import ru.rainman.domain.model.Post
import ru.rainman.ui.databinding.CardPostBinding
import ru.rainman.ui.helperutils.CurrentPlayedItemState
import ru.rainman.ui.helperutils.PubType

class PostsAdapter(
    private val currentItemIsPLaying: StateFlow<CurrentPlayedItemState?>,
    private val onPostClickListener: OnPostClickListener
) : PagingDataAdapter<Post, PostsAdapter.ViewHolder>(Diff()) {

    private val scope = CoroutineScope(Dispatchers.Default)

    inner class ViewHolder(private val binding: CardPostBinding) : RecyclerView.ViewHolder(binding.root) {

        @androidx.annotation.OptIn(com.google.android.material.badge.ExperimentalBadgeUtils::class)
        fun  bind(post: Post) {

            binding.apply {

                post.author.avatar?.let {
                    Glide.with(binding.root.context)
                        .load(it)
                        .circleCrop()
                        .placeholder(R.drawable.avatar_empty)
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
                    scope.launch {
                        currentItemIsPLaying.collect {item ->
                            item?.let { item1 ->
                                attachmentView.setAudioPlayed(
                                    item1.id == post.id && item1.type == PubType.POST && item1.isPlaying
                                )
                            } ?: attachmentView.setAudioPlayed(false)
                        }
                    }
                } ?: attachmentView.recycle()

                like.isChecked = post.likedByMe

                like.viewTreeObserver.addOnGlobalLayoutListener {
                    BadgeDrawable.create(like.context).apply {
                        verticalOffset = 16
                        number = post.likeOwnerIds.size
                        backgroundColor = binding.root.context.getColorAttribute(com.google.android.material.R.attr.colorSecondary)
                        badgeTextColor = binding.root.context.getColorAttribute(com.google.android.material.R.attr.colorOnSecondary)
                        BadgeUtils.attachBadgeDrawable(this, like)
                    }
                }

                header.more.setOnClickListener {
                    showPopupMenu(binding.header.more, post)
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


        private fun showPopupMenu(view: View, post: Post) {
            with(PopupMenu(view.context, view)) {
                inflate(R.menu.publication_menu)
                setOnMenuItemClickListener {
                    when (it.itemId) {
                        R.id.edit -> setListener(onPostClickListener.onEditClicked(post.id))
                        R.id.delete -> setListener(onPostClickListener.onDeleteClicked(post.id))
                        else -> false
                    }
                }
                show()
            }
        }

        private val setListener: (Unit) -> Boolean = { true }

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

    @ColorInt
    fun Context.getColorAttribute(@AttrRes attr: Int) :  Int {
        val typedValue = TypedValue()
        theme.resolveAttribute(attr, typedValue, true)
        return typedValue.data
    }
}