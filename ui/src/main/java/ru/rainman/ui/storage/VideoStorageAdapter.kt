package ru.rainman.ui.storage

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import com.bumptech.glide.Glide
import ru.rainman.ui.R
import ru.rainman.ui.databinding.CardStorageVideoItemBinding
import ru.rainman.ui.helperutils.asDuration
import ru.rainman.ui.storage.abstractions.StorageAdapter
import ru.rainman.ui.storage.abstractions.StorageItem

class VideoStorageAdapter(
    onItemClicked: (Uri) -> Unit
) : StorageAdapter<StorageItem.Video, CardStorageVideoItemBinding>(
    onItemClicked, {
        Glide.with(root.context)
            .load(it.uri)
            .placeholder(R.drawable.outline_ondemand_video_24)
            .into(storageImageItem)
        duration.text = it.duration.asDuration()
        storageImageItem.setOnClickListener { _ ->
            onItemClicked(it.uri)
        }
    }
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StorageViewHolder {
        return StorageViewHolder(
            CardStorageVideoItemBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }
}