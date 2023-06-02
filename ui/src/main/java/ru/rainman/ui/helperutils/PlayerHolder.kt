package ru.rainman.ui.helperutils

import android.content.Context
import androidx.media3.exoplayer.ExoPlayer

object PlayerHolder {

    private var _instance: ExoPlayer? = null
    val instance: ExoPlayer get() = requireNotNull(_instance)

    fun initPlayer(context: Context) {
        if (_instance == null) _instance = ExoPlayer.Builder(context).build()

    }
}