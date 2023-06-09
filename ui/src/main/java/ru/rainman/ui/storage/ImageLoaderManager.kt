package ru.rainman.ui.storage

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import androidx.loader.content.Loader
import ru.rainman.domain.model.Attachment
import ru.rainman.ui.storage.abstractions.MediaLoadManager

class ImageLoaderManager(
    viewModel: ImageStorageViewModel,
    context: Context
) : MediaLoadManager<Attachment.Image>(context, viewModel) {

    override val uri: Uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI

    override val projection = listOf(MediaStore.Images.Media._ID)

    override fun onLoadFinished(loader: Loader<Cursor>, data: Cursor?) {

        data?.let {
            val columnIndexData = it.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)

            while (it.moveToNext()) {

                resultList.add(
                    Attachment.Image(
                        Uri.withAppendedPath(
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                            it.getString(columnIndexData)
                        ).toString(), 1.77f
                    )
                )
            }
        }
        loadData(resultList.reversed())
    }

    override fun onLoaderReset(loader: Loader<Cursor>) {}
}