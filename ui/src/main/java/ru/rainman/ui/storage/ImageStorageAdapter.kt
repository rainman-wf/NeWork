package ru.rainman.ui.storage

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import com.bumptech.glide.Glide
import ru.rainman.ui.R
import ru.rainman.ui.databinding.CardStorageImageItemBinding
import ru.rainman.ui.storage.abstractions.StorageAdapter
import ru.rainman.ui.storage.abstractions.StorageItem

class ImageStorageAdapter(
    onItemClicked: (Uri) -> Unit
) : StorageAdapter<StorageItem.Image, CardStorageImageItemBinding>(
    onItemClicked, {
        Glide.with(root.context)
            .load(it.uri)
            .placeholder(R.drawable.outline_ondemand_video_24)
            .into(storageImageItem)
        storageImageItem.setOnClickListener { _ ->
            onItemClicked(it.uri)
        }
    }
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StorageViewHolder {
        return StorageViewHolder(
            CardStorageImageItemBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }
}