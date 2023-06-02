package ru.rainman.ui.storage

import android.view.LayoutInflater
import android.view.ViewGroup
import ru.rainman.domain.model.Attachment
import ru.rainman.ui.databinding.CardStorageAudioItemBinding
import ru.rainman.ui.helperutils.asDuration
import ru.rainman.ui.storage.abstractions.StorageAdapter

class AudioStorageAdapter(
    onItemClicked: (Attachment) -> Unit
) : StorageAdapter<Attachment.Audio, CardStorageAudioItemBinding>(
    onItemClicked, {
        play.setOnClickListener { _ ->
            onItemClicked(it)
        }
        artist.text = if (it.artist == "<unknown>") "Unknown artist" else it.artist
        title.text = it.title
        duration.text = it.duration.asDuration()
    }
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StorageViewHolder {
        return StorageViewHolder(
            CardStorageAudioItemBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }
}


