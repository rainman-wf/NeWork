package ru.rainman.ui.helperutils

import android.content.Context
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

object PlayerHolder {

    private var _instance: ExoPlayer? = null
    val instance: ExoPlayer get() = requireNotNull(_instance)

    private val _currentPlayedItem = MutableStateFlow<CurrentPlayedItemState?>(null)
    val currentPlayedItem: StateFlow<CurrentPlayedItemState?> get() = _currentPlayedItem

    private var hasListener = false

    interface PlayerListener {
        fun stateChangedListener(duration: Long, position: Long, playerStateReady: Boolean)
        fun isPlayingChanged(isPlaying: Boolean)
    }

    fun initPlayer(context: Context) {
        if (_instance == null) _instance = ExoPlayer.Builder(context).build()
    }

    fun listen(listener: PlayerListener) {
        if (!hasListener) {
            instance.addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    super.onPlaybackStateChanged(playbackState)
                    (playbackState == Player.STATE_READY).let {
                        listener.stateChangedListener(instance.duration, instance.currentPosition, it)
                    }
                }
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    super.onIsPlayingChanged(isPlaying)
                    listener.isPlayingChanged(isPlaying)
                }
            })
            hasListener = true
        }
    }

    fun setCurrentPlayedItem(item: CurrentPlayedItemState?) {
        CoroutineScope(Dispatchers.Main).launch {
            _currentPlayedItem.emit(item)
        }
    }

    fun playAudio(url: String, type: PubType, pubId: Long) {
        currentPlayedItem.value?.let {
            if (it.type == type && it.id == pubId) {
                if (it.isPlaying) instance.pause()
                else instance.play()
            } else playNew(url, type, pubId)
        } ?: playNew(url, type, pubId)
    }

    private fun playNew(url: String, type: PubType, pubId: Long) {
        instance.stop()
        instance.clearMediaItems()
        setCurrentPlayedItem(CurrentPlayedItemState(type, pubId, false))
        instance.addMediaItem(MediaItem.fromUri(url))
        instance.prepare()
        instance.play()
    }

    fun stopAudio() {
        instance.stop()
        clearCurrentPlayedItem()
        instance.clearMediaItems()
    }

    private fun clearCurrentPlayedItem() {
        setCurrentPlayedItem(null)
    }

    fun setIsPlaying(value: Boolean) {
        CoroutineScope(Dispatchers.Main).launch {
            _currentPlayedItem.value?.let {
                _currentPlayedItem.emit(it.copy(isPlaying = value))
            }
        }
    }
}