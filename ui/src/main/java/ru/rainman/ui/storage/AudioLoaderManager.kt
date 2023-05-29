package ru.rainman.ui.storage

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import androidx.loader.content.Loader
import ru.rainman.ui.storage.abstractions.MediaLoadManager
import ru.rainman.ui.storage.abstractions.StorageItem

class AudioLoaderManager(
    viewModel: AudioStorageViewModel,
    context: Context
) : MediaLoadManager<StorageItem.Audio>(context, viewModel) {

    override val projection = listOf(
        MediaStore.Audio.Media._ID,
        MediaStore.Audio.Media.DURATION,
        MediaStore.Audio.Media.ARTIST,
        MediaStore.Audio.Media.TITLE
    )

    override val uri: Uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

    override fun onLoadFinished(loader: Loader<Cursor>, data: Cursor?) {

        data?.let {
            val idColumn = it.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)
            val durationColumn = it.getColumnIndexOrThrow(MediaStore.MediaColumns.DURATION)
            val artistColumn = it.getColumnIndexOrThrow(MediaStore.MediaColumns.ARTIST)
            val titleColumn = it.getColumnIndexOrThrow(MediaStore.MediaColumns.TITLE)

            while (it.moveToNext()) {

                val uri = Uri.withAppendedPath(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    it.getString(idColumn)
                )
                val duration = it.getInt(durationColumn)
                val artist = it.getString(artistColumn)
                val title = it.getString(titleColumn)

                resultList.add(StorageItem.Audio(uri, duration, artist, title))
            }
        }
        loadData(resultList.reversed())
    }

    override fun onLoaderReset(loader: Loader<Cursor>) {}
}