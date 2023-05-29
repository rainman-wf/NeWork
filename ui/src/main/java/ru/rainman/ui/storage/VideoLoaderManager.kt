package ru.rainman.ui.storage

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import androidx.loader.content.Loader
import ru.rainman.ui.storage.abstractions.MediaLoadManager
import ru.rainman.ui.storage.abstractions.StorageItem

class VideoLoaderManager(
    viewModel: VideoStorageViewModel,
    context: Context
) : MediaLoadManager<StorageItem.Video>(context, viewModel) {

    override val projection = listOf(
        MediaStore.Video.Media._ID,
        MediaStore.Video.Media.DURATION
    )

    override val uri: Uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI

    override fun onLoadFinished(loader: Loader<Cursor>, data: Cursor?) {

        data?.let {
            val idColumn = it.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)
            val durationColumn = it.getColumnIndexOrThrow(MediaStore.MediaColumns.DURATION)
            while (it.moveToNext()) {
                val uri = Uri.withAppendedPath(
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                    it.getString(idColumn)
                )
                val duration = it.getInt(durationColumn)
                resultList.add(StorageItem.Video(uri,duration))
            }
        }
        loadData(resultList)
    }

    override fun onLoaderReset(loader: Loader<Cursor>) {}
}