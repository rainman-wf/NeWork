package ru.rainman.ui.storage.abstractions

import android.net.Uri

sealed class StorageItem(open val uri: Uri) {
    data class Image(override val uri: Uri) : StorageItem(uri)
    data class Video(override val uri: Uri, val duration: Int) : StorageItem(uri)
    data class Audio(
        override val uri: Uri,
        val duration: Int,
        val artist: String,
        val title: String
    ) : StorageItem(uri)
}