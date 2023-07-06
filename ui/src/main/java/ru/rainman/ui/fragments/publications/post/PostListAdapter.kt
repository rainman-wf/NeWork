package ru.rainman.ui.fragments.publications.post

import android.text.TextUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import kotlinx.coroutines.flow.StateFlow
import ru.rainman.domain.model.Post
import ru.rainman.ui.databinding.CardPostBinding
import ru.rainman.ui.fragments.publications.PublicationsListAdapter
import ru.rainman.ui.helperutils.CurrentPlayedItemState
import ru.rainman.ui.helperutils.represent

class PostListAdapter(
    private val isWall: Boolean,
    currentItemIsPLaying: StateFlow<CurrentPlayedItemState?>,
    private val onPublicationClickListener: OnPostClickListener
) : PublicationsListAdapter<Post, CardPostBinding>(
    currentItemIsPLaying, onPublicationClickListener
) {

    inner class VH(private val binding: CardPostBinding) : ViewHolder(binding) {

        override fun bind(publication: Post) {

            binding.header.root.isVisible = !isWall

            binding.apply {
                publication.preBind(
                    header.avatar,
                    attachmentView,
                    header.name,
                    header.job,
                    header.published,
                    header.more,
                    content,
                    like,
                    likeCount,
                    share,
                    header.root,
                    root
                )

                var contentExpanded = false

                binding.expandContent.isVisible = content.lineCount >= 5

                expandContent.setOnClickListener {
                    contentExpanded = !contentExpanded
                    expandContent.isSelected = contentExpanded
                    content.ellipsize = if (contentExpanded) null else TextUtils.TruncateAt.END
                    content.maxLines = if (contentExpanded) Integer.MAX_VALUE else 5
                }

                link.root.isVisible = publication.link != null
                publication.link?.let { link.represent(it) }

                mentioned.isVisible = publication.mentioned.isNotEmpty()

                mentionedCount.isVisible = publication.mentioned.isNotEmpty()

                mentioned.isChecked = publication.mentionedMe

                mentionedCount.text = publication.mentioned.size.toString()

                mentioned.setOnClickListener {
                    onPublicationClickListener.onMentionedClicked(publication.mentioned.map { it.id })
                }

                mentionedCount.setOnClickListener {
                    onPublicationClickListener.onMentionedClicked(publication.mentioned.map { it.id })
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return VH(
            CardPostBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }
}