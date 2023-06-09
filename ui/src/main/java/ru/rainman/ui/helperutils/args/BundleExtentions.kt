package ru.rainman.ui.helperutils.args

import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
import java.io.Serializable

fun Bundle.putString(key: ArgKey, value: String) {
    this.putString(key.name, value)
}

fun Bundle?.getString(key: ArgKey) : String? {
    return this?.getString(key.name)
}

fun Bundle.putObject(key: ArgKey, obj: Serializable) {
    putSerializable(key.name, obj)
}

fun Fragment.putResult(requestKey: RequestKey, result: Bundle) {
    setFragmentResult(requestKey.name, result)
}

@Suppress("DEPRECATION")
inline fun <reified T : Serializable> Bundle.getObject(key: ArgKey): T? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        getSerializable(key.name, T::class.java)
    } else {
        getSerializable(key.name) as? T
    }
}

fun Fragment.setResultListener(requestKey: RequestKey, body: (Bundle) -> Unit) {
    setFragmentResultListener(requestKey.name) { _, bundle ->
        body(bundle)
    }
}
