package ru.rainman.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import kotlinx.coroutines.flow.StateFlow
import ru.rainman.domain.model.Event
import ru.rainman.ui.databinding.CardEventBinding
import ru.rainman.ui.helperutils.CurrentPlayedItemState

class EventListAdapter(
    currentItemIsPLaying: StateFlow<CurrentPlayedItemState?>,
    private val onPublicationClickListener: OnEventClickListener
) : PublicationsListAdapter<Event, CardEventBinding>(
    currentItemIsPLaying, onPublicationClickListener
) {

    inner class VH(private val binding: CardEventBinding) : ViewHolder(binding) {
        override fun bind(publication: Event) {
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
                participate.isChecked = publication.participatedByMe
                participate.setOnClickListener {
                    onPublicationClickListener.onParticipateClicked(publication.id)
                }
            }
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return VH(
            CardEventBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }
}

