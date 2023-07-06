package ru.rainman.common

import android.util.Log

fun Any.log(message: Any?) {
    Log.d("my_app_log", "${this::class.simpleName} : $message")
}

fun <T : Any> T.log(): T {
    Log.d("my_app_log", "${this::class.simpleName} : $this")
    return this
}

