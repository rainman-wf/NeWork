package ru.rainman.ui.helperutils

import androidx.annotation.IdRes
import androidx.fragment.app.FragmentManager
import androidx.navigation.fragment.NavHostFragment
import java.text.SimpleDateFormat
import java.util.*

fun FragmentManager.getNavController(@IdRes host: Int) = (findFragmentById(host) as NavHostFragment).navController

fun Long.toStringDate(): String {
    return SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(this)
}