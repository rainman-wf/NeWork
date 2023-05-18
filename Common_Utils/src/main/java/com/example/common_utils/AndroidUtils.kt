package com.example.common_utils

import android.util.Log

fun Any.log(message: Any?) {
    Log.d("my_app_log", "${this::class.simpleName} : $message")
}