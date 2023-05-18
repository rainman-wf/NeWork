package ru.rainman.ui

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.loader.app.LoaderManager
import androidx.loader.content.CursorLoader
import androidx.loader.content.Loader

class GalleryLoaderManager(
    private val viewModel: EventEditorViewModel,
    private val context: Context
) : LoaderManager.LoaderCallbacks<Cursor> {

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor> {

        val uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI

        val projection = listOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DATA,
            MediaStore.MediaColumns.DATA,
            MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
        )

        return CursorLoader(
            context,
            uri,
            projection.toTypedArray(),
            null,
            null,
            null
        )
    }

    override fun onLoadFinished(loader: Loader<Cursor>, data: Cursor?) {

        val list = mutableListOf<Uri>()

        data?.let {
            val columnIndexData = it.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)
            while (it.moveToNext()) {
                list.add(
                    Uri.withAppendedPath(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        it.getString(columnIndexData)
                    )
                )
            }
        }
        viewModel.loadGallery(list.reversed())
    }

    override fun onLoaderReset(loader: Loader<Cursor>) {}
}