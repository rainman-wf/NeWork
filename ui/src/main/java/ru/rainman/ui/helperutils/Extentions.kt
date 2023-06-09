package ru.rainman.ui.helperutils

import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.snackbar.Snackbar
import ru.rainman.domain.model.UploadMedia
import java.io.Serializable
import ru.rainman.ui.VideoPlayerDialogFragment
import java.io.File
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*


fun FragmentManager.getNavController(@IdRes host: Int) =
    (findFragmentById(host) as NavHostFragment).navController

fun Long.toStringDate(): String {
    return SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(this)
}

fun menuItemHandle(handler: () -> Any): Boolean {
    handler()
    return false
}

fun Uri.toFile(context: Context): File {
    val file = File.createTempFile("tmp_", null, context.cacheDir)
    file
        .outputStream()
        .use {
            val input = context.contentResolver.openInputStream(this)
            input?.copyTo(it)
            input?.close()
        }
    return file
}

fun Uri.toUploadMedia(context: Context): UploadMedia {

    val stream = context.contentResolver.openInputStream(this)
    val byteArray = stream?.readBytes()?.toList()

    stream?.close()

    val cursor = context.contentResolver.query(this, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
    val name = if (cursor != null && cursor.moveToFirst()) cursor.getString(0) else "file"

    cursor?.close()

    return UploadMedia(byteArray!!, name)
}

fun Fragment.snack(msg: String) {
    Snackbar.make(this.requireView(), msg, Snackbar.LENGTH_SHORT).show()
}

fun Fragment.toast(msg: String) {
    Toast.makeText(this.requireContext(), msg, Toast.LENGTH_SHORT).show()
}

fun Fragment.activityFragmentManager() : FragmentManager {
    return this.requireActivity().supportFragmentManager
}


fun Int.asDuration(): String {
    val seconds = this / 1000
    val durationMinutes = seconds / 60
    val durationSeconds = DecimalFormat("00").format(seconds % 60)
    return "$durationMinutes:$durationSeconds"
}

fun Fragment.showVideoDialog(uri: String, ratio: Float) {
    val dialog = VideoPlayerDialogFragment()
    val bundle = Bundle()
    bundle.putString("uri", uri)
    bundle.putFloat("ratio", ratio)
    dialog.arguments = bundle
    dialog.show(parentFragmentManager, null)
}

@Suppress("DEPRECATION")
inline fun <reified T : Serializable> Bundle.getObject(key: String): T? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        getSerializable(key, T::class.java)
    } else {
        getSerializable(key) as? T
    }
}
