package ru.rainman.ui.storage

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import androidx.loader.content.Loader
import com.example.common_utils.log
import ru.rainman.domain.model.Attachment
import ru.rainman.ui.storage.abstractions.MediaLoadManager

class ImageLoaderManager(
    viewModel: ImageStorageViewModel,
    context: Context
) : MediaLoadManager<Attachment.Image>(context, viewModel) {

    override val uri: Uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI

    override val projection = listOf(MediaStore.Images.Media._ID, MediaStore.Images.Media.DATA, MediaStore.Images.Media.DISPLAY_NAME)

    override fun onLoadFinished(loader: Loader<Cursor>, data: Cursor?) {

        data?.let {
            val columnIndexData = it.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)
            val columngData = it.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA)
            val colNmae = it.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME)
            while (it.moveToNext()) {

                log(it.getString(columngData))
                log(it.getString(colNmae))

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