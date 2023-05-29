package ru.rainman.ui.storage.abstractions

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import androidx.loader.app.LoaderManager
import androidx.loader.content.CursorLoader
import androidx.loader.content.Loader

abstract class MediaLoadManager<T : StorageItem>(
    private val context: Context,
    private val viewModel: StorageViewModel<T>
) : LoaderManager.LoaderCallbacks<Cursor> {

    val resultList = mutableListOf<T>()

    abstract val projection: List<String>
    abstract val uri: Uri

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor> {
        return CursorLoader(context, uri, projection.toTypedArray(), null, null, null)
    }

    fun loadData(itemList: List<T>) {
        viewModel.loadData(itemList)
    }

}