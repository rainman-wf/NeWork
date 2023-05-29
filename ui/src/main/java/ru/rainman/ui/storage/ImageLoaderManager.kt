package ru.rainman.ui.storage

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import androidx.loader.content.Loader
import ru.rainman.ui.storage.abstractions.MediaLoadManager
import ru.rainman.ui.storage.abstractions.StorageItem

class ImageLoaderManager(
    viewModel: ImageStorageViewModel,
    context: Context
) : MediaLoadManager<StorageItem.Image>(context, viewModel) {

    override val uri: Uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI

    override val projection = listOf(MediaStore.Images.Media._ID)

    override fun onLoadFinished(loader: Loader<Cursor>, data: Cursor?) {

        data?.let {
            val columnIndexData = it.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)
            while (it.moveToNext()) {
                resultList.add(
                    StorageItem.Image(
                        Uri.withAppendedPath(
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                            it.getString(columnIndexData)
                        )
                    )
                )
            }
        }
        loadData(resultList.reversed())
    }

    override fun onLoaderReset(loader: Loader<Cursor>) {}
}