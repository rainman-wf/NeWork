package ru.rainman.ui.fragments.publications.event

import android.view.LayoutInflater
import android.view.ViewGroup
import kotlinx.coroutines.flow.StateFlow
import ru.rainman.domain.model.Event
import ru.rainman.ui.databinding.CardEventBinding
import ru.rainman.ui.fragments.publications.PublicationsListAdapter
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
                    likeCount,
                    share,
                    header.root,
                    root
                )
                participate.isChecked = publication.participatedByMe
                participate.setOnClickListener {
                    onPublicationClickListener.onParticipateClicked(publication.id)
                }

                participantsCount.text = publication.participantsIds.size.toString()

                participantsCount.setOnClickListener {
                    onPublicationClickListener.onParticipantsCountClicked(publication.participantsIds.map { it.id })
                }

                participate.text = if (publication.participatedByMe) "Leave" else "Take part"
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

