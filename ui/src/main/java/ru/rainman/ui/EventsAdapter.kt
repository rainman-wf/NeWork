package ru.rainman.ui

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
import ru.rainman.domain.model.Event
import ru.rainman.ui.databinding.CardEventBinding
import ru.rainman.ui.helperutils.CurrentPlayedItemState
import ru.rainman.ui.helperutils.PubType

class EventsAdapter(
    private val currentItemIsPLaying: SharedFlow<CurrentPlayedItemState?>,
    private val onEventClickListener: OnEventClickListener
) : PagingDataAdapter<Event, EventsAdapter.ViewHolder>(Diff()) {

    inner class ViewHolder(private val binding: CardEventBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(event: Event) {

            binding.apply {

                event.author.avatar?.let {
                    Glide.with(binding.root.context)
                        .load(it)
                        .circleCrop()
                        .placeholder(R.drawable.avatar_stub_large)
                        .into(header.avatar)
                } ?: Glide.with(binding.root.context).clear(header.avatar)

                header.name.text = event.author.name
                header.job.text = event.author.currentJob?.name
                header.published.text = event.published.toString()
                header.more.isVisible = event.ownedByMe

                header.job.isVisible = event.author.currentJob != null

                content.text = event.content

                event.attachment?.let {
                    attachmentView.setData(it)
                    attachmentView.setOnPlayClickListener {
                        onEventClickListener.onPlayClicked(event.id, it)
                    }
                    CoroutineScope(Dispatchers.Default).launch {
                        currentItemIsPLaying.collectLatest {item ->
                            item?.let { item1 ->
                                attachmentView.setAudioPlayed(
                                    item1.id == event.id && item1.type == PubType.EVENT && item1.isPlaying
                                )
                            } ?: attachmentView.setAudioPlayed(false)
                        }
                    }
                } ?: attachmentView.recycle()

                like.isChecked = event.likedByMe
                participate.isChecked = event.participatedByMe

                header.more.setOnClickListener {
                    onEventClickListener.onMoreClicked(event.id)
                }

                like.setOnClickListener {
                    onEventClickListener.onLikeClicked(event.id)
                }

                participate.setOnClickListener {
                    onEventClickListener.onParticipateClicked(event.id)
                }

                share.setOnClickListener{
                    onEventClickListener.onShareClicked(event.id)
                }

                header.root.setOnClickListener {
                    onEventClickListener.onAuthorClicked(event.id)
                }

                root.setOnClickListener {
                    onEventClickListener.onEventClicked(event.id)
                }
            }
        }
    }

    class Diff : DiffUtil.ItemCallback<Event>() {
        override fun areItemsTheSame(oldItem: Event, newItem: Event): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: Event, newItem: Event): Boolean {
            return oldItem.id == newItem.id
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            CardEventBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        getItem(position)?.let { holder.bind(it) }
    }

}