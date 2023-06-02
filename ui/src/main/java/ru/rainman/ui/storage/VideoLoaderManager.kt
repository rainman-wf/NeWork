package ru.rainman.ui.storage

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import androidx.loader.content.Loader
import ru.rainman.domain.model.Attachment
import ru.rainman.ui.storage.abstractions.MediaLoadManager

class VideoLoaderManager(
    viewModel: VideoStorageViewModel,
    context: Context
) : MediaLoadManager<Attachment.Video>(context, viewModel) {

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
                resultList.add(Attachment.Video(uri.toString(),duration, 1.77f))
            }
        }
        loadData(resultList)
    }

    override fun onLoaderReset(loader: Loader<Cursor>) {}
}