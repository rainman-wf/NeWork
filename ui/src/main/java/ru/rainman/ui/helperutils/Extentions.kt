package ru.rainman.ui.helperutils

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.snackbar.Snackbar
import ru.rainman.domain.model.geo.BusinessObjectData
import ru.rainman.domain.model.geo.GeoObject
import ru.rainman.domain.model.geo.Geometry
import ru.rainman.domain.model.geo.Point
import ru.rainman.domain.model.geo.SearchResult
import ru.rainman.domain.model.geo.ToponymObjectData
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

fun Fragment.snack(msg: String) {
    Snackbar.make(this.requireView(), msg, Snackbar.LENGTH_SHORT).show()
}

fun Fragment.toast(msg: String) {
    Toast.makeText(this.requireContext(), msg, Toast.LENGTH_SHORT).show()
}


fun Int.asDuration(): String {
    val seconds = this / 1000
    val durationMinutes = seconds / 60
    val durationSeconds = DecimalFormat("00").format(seconds % 60)
    return "$durationMinutes:$durationSeconds"
}

fun Fragment.showVideoDialog(uri: String) {
    val dialog = VideoPlayerDialogFragment()
    val bundle = Bundle()
    bundle.putString("uri", uri)
    dialog.arguments = bundle
    dialog.show(parentFragmentManager, null)
}

