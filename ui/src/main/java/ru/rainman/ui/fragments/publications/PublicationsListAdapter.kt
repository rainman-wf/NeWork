package ru.rainman.ui.fragments.publications

import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import ru.rainman.domain.model.Publication
import ru.rainman.ui.R
import ru.rainman.ui.helperutils.CurrentPlayedItemState
import ru.rainman.ui.helperutils.PubType
import ru.rainman.ui.view.AttachmentView
import java.time.format.DateTimeFormatter

abstract class PublicationsListAdapter<T : Publication, VB : ViewBinding>(
    private val currentItemIsPLaying: StateFlow<CurrentPlayedItemState?>,
    private val onPublicationClickListener: OnPublicationClickListener<T>
) : PagingDataAdapter<T, PublicationsListAdapter<T, VB>.ViewHolder>(PublicationDiff()) {

    private val scope = CoroutineScope(Dispatchers.Default)

    abstract inner class ViewHolder(private val binding: VB) :
        RecyclerView.ViewHolder(binding.root) {

        abstract fun bind(publication: T)

        fun T.preBind(
            avatar: ImageView,
            attachmentView: AttachmentView,
            name: TextView,
            job: TextView,
            published: TextView,
            more: ImageButton,
            content: TextView,
            like: MaterialButton,
            count: MaterialButton,
            share: MaterialButton,
            headerRoot: View,
            root: View
        ) {
            setAvatar(avatar)
            setAttachment(attachmentView)
            setHeader(name, job, published, more)
            setContent(content)
            setFooter(like, count)
            setListeners(more,like,share, headerRoot, root)
        }

        private fun T.setAvatar(view: ImageView) {
            author.avatar?.let {
                Glide.with(binding.root.context)
                    .load(it)
                    .circleCrop()
                    .placeholder(R.drawable.avatar_stub_large)
                    .into(view)
            } ?: Glide.with(binding.root.context).clear(view)
        }

        private fun T.setAttachment(attachmentView: AttachmentView) {
            attachment?.let {
                attachmentView.setData(it)
                attachmentView.setOnPlayClickListener {
                    onPublicationClickListener.onPlayClicked(id, it)
                }
                CoroutineScope(Dispatchers.Default).launch {
                    currentItemIsPLaying.collectLatest { item ->
                        item?.let { item1 ->
                            attachmentView.setAudioPlayed(
                                item1.id == id && item1.type == PubType.EVENT && item1.isPlaying
                            )
                        } ?: attachmentView.setAudioPlayed(false)
                    }
                }
            } ?: attachmentView.recycle()
        }

        private fun T.setHeader(
            name: TextView,
            job: TextView,
            published: TextView,
            more: ImageButton
        ) {
            name.text = author.name
            job.text = author.currentJob?.name
            published.text = this.published.format(DateTimeFormatter.ofPattern("dd.MM.yyyy EEE HH:mm"))
            more.isVisible = ownedByMe
            job.isVisible = author.currentJob != null
        }

        private fun T.setContent(view: TextView) {
            view.text = content
        }

        @androidx.annotation.OptIn(com.google.android.material.badge.ExperimentalBadgeUtils::class)
        private fun T.setFooter(like: MaterialButton, count: MaterialButton) {
            like.isChecked = likedByMe

            like.setOnLongClickListener {
                onPublicationClickListener.onLikeClicked(id)
                true
            }


            count.isVisible = likeOwnerIds.isNotEmpty()
            count.text = likeOwnerIds.size.toString()

            count.setOnClickListener { onPublicationClickListener.onLikesCountClicked(likeOwnerIds.map { it.id }) }
        }

        private fun T.setListeners(
            more: View,
            like: View,
            share: View,
            headerRoot: View,
            root: View
        ) {
            more.setOnClickListener {
                showPopupMenu(more, this)
            }

            like.setOnClickListener {
                onPublicationClickListener.onLikeClicked(this.id)
            }

            share.setOnClickListener {
                onPublicationClickListener.onShareClicked(this.id)
            }

            headerRoot.setOnClickListener {
                onPublicationClickListener.onAuthorClicked(this.author.id)
            }

            root.setOnClickListener {
                onPublicationClickListener.onBodyClicked(this.id)
            }
        }

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        getItem(position)?.let { holder.bind(it) }
    }

    private fun showPopupMenu(view: View, publication: T) {
        with(PopupMenu(view.context, view)) {
            inflate(R.menu.publication_menu)
            setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.edit -> setListener(onPublicationClickListener.onEditClicked(publication.id))
                    R.id.delete -> setListener(onPublicationClickListener.onDeleteClicked(publication.id))
                    else -> false
                }
            }
            show()
        }
    }

    private val setListener: (Unit) -> Boolean = { true }

    class PublicationDiff<T : Publication> : DiffUtil.ItemCallback<T>() {
        override fun areItemsTheSame(oldItem: T, newItem: T): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: T, newItem: T): Boolean {
            return oldItem.id == newItem.id
        }
    }


}



