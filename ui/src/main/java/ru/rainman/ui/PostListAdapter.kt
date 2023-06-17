package ru.rainman.ui

import android.text.TextUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import kotlinx.coroutines.flow.StateFlow
import ru.rainman.domain.model.Post
import ru.rainman.ui.databinding.CardPostBinding
import ru.rainman.ui.helperutils.CurrentPlayedItemState

class PostListAdapter(
    currentItemIsPLaying: StateFlow<CurrentPlayedItemState?>,
    onPublicationClickListener: OnPublicationClickListener<Post>
) : PublicationsListAdapter<Post, CardPostBinding>(
    currentItemIsPLaying, onPublicationClickListener
) {

    inner class VH(private val binding: CardPostBinding) : ViewHolder(binding) {

        override fun bind(publication: Post) {
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
                    share,
                    header.root,
                    root
                )

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