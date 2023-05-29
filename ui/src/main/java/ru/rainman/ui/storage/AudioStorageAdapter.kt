package ru.rainman.ui.storage

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import ru.rainman.ui.databinding.CardStorageAudioItemBinding
import ru.rainman.ui.helperutils.asDuration
import ru.rainman.ui.storage.abstractions.StorageAdapter
import ru.rainman.ui.storage.abstractions.StorageItem

class AudioStorageAdapter(
    onItemClicked: (Uri) -> Unit
) : StorageAdapter<StorageItem.Audio, CardStorageAudioItemBinding>(
    onItemClicked, {
        play.setOnClickListener { _ ->
            onItemClicked(it.uri)
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


