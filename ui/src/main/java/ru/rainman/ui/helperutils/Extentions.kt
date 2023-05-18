package ru.rainman.ui.helperutils

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.snackbar.Snackbar
import java.io.File
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